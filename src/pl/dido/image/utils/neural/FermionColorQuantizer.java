package pl.dido.image.utils.neural;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

import pl.dido.image.utils.Gfx;

public class FermionColorQuantizer {

	public static Logger log = Logger.getLogger(FermionColorQuantizer.class.getCanonicalName());

	private final int numColors;
	private final Random random = new Random(42L);

	private float alphaRecon = 1.0f;
	private float alphaPauli = 0.01f;
	private float alphaOrtho = 0.01f;

	private final float contrastScale = 0.85f;
	private float[][] embeddings;

	private float[][] m;
	private float[][] v;
	private int step = 0;

	private final float learningRate = 0.02f;
	private final float beta1 = 0.9f;
	private final float beta2 = 0.999f;
	private final float epsilon = 1e-8f;

	// gradients
	private float[][] grad;

	public FermionColorQuantizer(final int numColors) {
		if (numColors <= 0) 
			throw new IllegalArgumentException("numColors must be > 0");
		
		this.numColors = numColors;
		initialize();
	}

	private void initialize() {
		embeddings = new float[numColors][4];
		grad = new float[numColors][4];
		
		m = new float[numColors][4];
		v = new float[numColors][4];

		for (int i = 0; i < numColors; i++) 
			for (int j = 0; j < 4; j++) 
				embeddings[i][j] = (float) random.nextGaussian();
	}

	public Palette forward() {
		final float[][] rgbPalette = new float[numColors][3];
		final float[][] spinPalette = new float[numColors][1];

		for (int i = 0; i < numColors; i++) {
			for (int j = 0; j < 3; j++) 
				rgbPalette[i][j] = sigmoid(contrastScale * embeddings[i][j]);

			spinPalette[i][0] = (float) Math.tanh(embeddings[i][3]);
		}

		return new Palette(rgbPalette, spinPalette);
	}

	public LossResult computeLossAndGradients(final float[][] pixelsBatch) {
		zeroGradients();
		final Palette p = forward();

		final float[][] rgbPal = p.rgb;
		final float[][] spinPal = p.spin;

		final int batchSize = pixelsBatch.length;
		if (batchSize == 0) 
			return new LossResult(0f, 0f, 0f, 0f);

		float quantizationLoss = 0f;

		for (int n = 0; n < batchSize; n++) {
			final float[] pixel = pixelsBatch[n];

			int bestIdx = 0;
			float bestDist = Float.POSITIVE_INFINITY;

			for (int c = 0; c < numColors; c++) {
				final float dist = squaredDistance(pixel, rgbPal[c]);
				
				if (dist < bestDist) {
					bestDist = dist;
					bestIdx = c;
				}
			}

			quantizationLoss += bestDist;

			for (int j = 0; j < 3; j++) {
				final float pred = rgbPal[bestIdx][j];
				final float target = pixel[j];
				
				final float dSigmoid = pred * (1f - pred) * contrastScale;
				grad[bestIdx][j] += (2f * (pred - target) * dSigmoid) / batchSize;
			}
		}

		quantizationLoss /= batchSize;

		final float[][] rgbNorm = new float[numColors][3];
		final float[] rgbNormLen = new float[numColors];

		for (int i = 0; i < numColors; i++) {
			rgbNormLen[i] = Math.max(l2Norm(rgbPal[i]), 1e-12f);
			
			for (int j = 0; j < 3; j++)
				rgbNorm[i][j] = rgbPal[i][j] / rgbNormLen[i];
		}

		float pauliLossSum = 0f;
		final float normFactor = (numColors > 1) ? 1f / (numColors * (numColors - 1f)) : 0f;

		for (int i = 0; i < numColors; i++) {
			float gradSpinRaw = 0f;

			for (int j = 0; j < numColors; j++) {
				if (i == j) 
					continue;

				final float simSpace = dot(rgbNorm[i], rgbNorm[j]);
				final float simSpin = spinPal[i][0] * spinPal[j][0];

				final float term = (simSpace * simSpace) * (simSpin * simSpin);
				pauliLossSum += term;

				// d/d(normRi) of simSpace^2 * simSpin^2
				final float coeff = 2f * simSpace * (simSpin * simSpin);
				final float[] dL_dNormRi = new float[3];

				for (int c = 0; c < 3; c++) 
					dL_dNormRi[c] = coeff * rgbNorm[j][c];

				final float[] dL_dRawRi = backpropThroughNormalization(rgbPal[i], rgbNorm[i], rgbNormLen[i], dL_dNormRi);
				for (int c = 0; c < 3; c++)
					grad[i][c] += alphaPauli * normFactor * dL_dRawRi[c];

				// spin gradient through tanh
				final float dL_dSi = 2f * simSpin * (simSpace * simSpace) * spinPal[j][0];
				gradSpinRaw += dL_dSi;
			}

			final float rawSpin = embeddings[i][3];
			final float dTanh = 1f - (float) Math.tanh(rawSpin) * (float) Math.tanh(rawSpin);
			grad[i][3] += alphaPauli * normFactor * gradSpinRaw * dTanh;
		}

		final float pauliLoss = pauliLossSum * normFactor;

		final float[][] centered = new float[numColors][3];
		final float[] mean = new float[3];

		for (int i = 0; i < numColors; i++) 
			for (int j = 0; j < 3; j++) 
				mean[j] += rgbPal[i][j];
		
		for (int j = 0; j < 3; j++) 
			mean[j] /= numColors;

		for (int i = 0; i < numColors; i++) 
			for (int j = 0; j < 3; j++) 
				centered[i][j] = rgbPal[i][j] - mean[j];

		final float[][] rgbNormAxis = normalizeColumns(centered);
		final float[][] gram = new float[3][3];

		for (int i = 0; i < 3; i++) 
			for (int j = 0; j < 3; j++) 
				gram[i][j] = dot(getColumn(rgbNormAxis, i), getColumn(rgbNormAxis, j));

		float orthoLoss = 0f;
		final float[][] gramMinusI = new float[3][3];
		
		for (int i = 0; i < 3; i++) 
			for (int j = 0; j < 3; j++) {
				final float target = (i == j) ? 1f : 0f;
				final float diff = gram[i][j] - target;
				
				gramMinusI[i][j] = diff;
				orthoLoss += diff * diff;
			}
		
		orthoLoss /= 9f;

		for (int i = 0; i < numColors; i++)
			for (int c = 0; c < 3; c++) {
				
				float gradCenter = 0f;
				for (int j = 0; j < 3; j++)
					gradCenter += rgbNormAxis[i][j] * gramMinusI[j][c];
				
				grad[i][c] += alphaOrtho * (4f / 9f) * gradCenter;
			}

		final float totalLoss = alphaRecon * quantizationLoss + alphaPauli * pauliLoss + alphaOrtho * orthoLoss;
		return new LossResult(totalLoss, quantizationLoss, pauliLoss, orthoLoss);
	}

	public void updateAdam() {
		step++;

		final float bc1 = 1f - (float) Math.pow(beta1, step);
		final float bc2 = 1f - (float) Math.pow(beta2, step);

		for (int i = 0; i < numColors; i++) {
			for (int j = 0; j < 4; j++) {
				final float g = grad[i][j];

				m[i][j] = beta1 * m[i][j] + (1f - beta1) * g;
				v[i][j] = beta2 * v[i][j] + (1f - beta2) * g * g;

				final float mHat = m[i][j] / bc1;
				final float vHat = v[i][j] / bc2;

				embeddings[i][j] -= learningRate * mHat / ((float) Math.sqrt(vHat) + epsilon);
			}
		}
	}

	public void zeroGradients() {
		for (int i = 0; i < numColors; i++)
			Arrays.fill(grad[i], 0f);
	}

	public Palette getPalette() {
		return forward();
	}

	private static float sigmoid(final float x) {
		return 1f / (1f + (float) Math.exp(-x));
	}

	private static float squaredDistance(final float[] a, final float[] b) {
		float s = 0f;
		
		for (int i = 0; i < 3; i++) {
			final float d = a[i] - b[i];
			s += d * d;
		}
		
		return s;
	}

	private static float dot(final float[] a, final float[] b) {
		float s = 0f;
		for (int i = 0; i < a.length; i++)
			s += a[i] * b[i];
		
		return s;
	}

	private static float l2Norm(final float[] v) {
		float s = 0f;
		for (final float x : v) 
			s += x * x;
		
		return (float) Math.sqrt(s);
	}

	private static float[][] normalizeColumns(final float[][] m) {
		final int rows = m.length;
		
		final int cols = m[0].length;
		final float[][] out = new float[rows][cols];

		for (int c = 0; c < cols; c++) {
			float norm = 0f;
			
			for (int r = 0; r < rows; r++) 
				norm += m[r][c] * m[r][c];
			
			norm = Math.max((float) Math.sqrt(norm), 1e-12f);

			for (int r = 0; r < rows; r++) 
				out[r][c] = m[r][c] / norm;
		}

		return out;
	}

	private static float[] getColumn(final float[][] m, final int c) {
		final float[] col = new float[m.length];
		for (int i = 0; i < m.length; i++)
			col[i] = m[i][c];
		
		return col;
	}

	private static float[] backpropThroughNormalization(
			final float[] raw,
			final float[] normed,
			final float norm,
			final float[] gradOut) {

		float dot = 0f;
		for (int i = 0; i < 3; i++)
			dot += gradOut[i] * normed[i];		

		final float[] grad = new float[3];
		for (int i = 0; i < 3; i++) 
			grad[i] = (gradOut[i] - normed[i] * dot) / Math.max(norm, 1e-12f);
		
		return grad;
	}

	public static class Palette {
		public final float[][] rgb;
		public final float[][] spin;

		public Palette(final float[][] rgb, final float[][] spin) {
			this.rgb = rgb;
			this.spin = spin;
		}
	}

	public static class LossResult {
		public final float totalLoss;
		public final float reconLoss;
		
		public final float pauliLoss;
		public final float orthoLoss;

		public LossResult(final float totalLoss, final float reconLoss, final float pauliLoss, final float orthoLoss) {
			this.totalLoss = totalLoss;
			this.reconLoss = reconLoss;
			
			this.pauliLoss = pauliLoss;
			this.orthoLoss = orthoLoss;
		}
	}

	public void updateDynamicLossWeights(final int currentEpoch, final int totalEpochs) {
		float progress = (float) currentEpoch / (float) totalEpochs;
		if (progress > 1.0f)
			progress = 1.0f;

		final float decayFactor = 0.5f * (1.0f + (float) Math.cos(progress * Math.PI));
		final float maxPauli = 0.05f;
		final float maxOrtho = 0.05f;

		this.alphaPauli = Math.max(0.001f, maxPauli * decayFactor);
		this.alphaOrtho = Math.max(0.001f, maxOrtho * decayFactor);
	}

	public static float[][] getImageSpectrum(final byte[] pixels, final int maxPoints) {
		final int totalPixels = pixels.length / 3;
		final int bitMask = 0xF8;

		final HashMap<Integer, float[]> colorGrid = new HashMap<>(8192);

		for (int i = 0; i < totalPixels; i++) {
			final int idx = i * 3;
			
			final int r = pixels[idx] & 0xff;
			final int g = pixels[idx + 1] & 0xff;
			final int b = pixels[idx + 2] & 0xff;

			final int gridKey = ((r & bitMask) << 16) | ((g & bitMask) << 8) | (b & bitMask);
			float[] accum = colorGrid.get(gridKey);
			
			if (accum == null) {
				accum = new float[] { 0f, 0f, 0f, 0f };
				colorGrid.put(gridKey, accum);
			}

			accum[0] += r;
			accum[1] += g;
			
			accum[2] += b;
			accum[3] += 1f;
		}

		final List<float[]> clusters = new ArrayList<>(colorGrid.values());
		clusters.sort((o1, o2) -> Float.compare(o2[3], o1[3]));

		final int finalPoints = Math.min(maxPoints, clusters.size());
		final float[][] spectrum = new float[finalPoints][3];

		for (int i = 0; i < finalPoints; i++) {
			final float[] clusterData = clusters.get(i);
			final float mass = Math.max(clusterData[3], 1f);

			spectrum[i][0] = (clusterData[0] / mass) / 255f;
			spectrum[i][1] = (clusterData[1] / mass) / 255f;
			spectrum[i][2] = (clusterData[2] / mass) / 255f;
		}

		return spectrum;
	}

	public static int[][] getQuantumPalette(final byte[] pixels, final int numColors) {
		final FermionColorQuantizer quantizer = new FermionColorQuantizer(numColors);
		
		final int epochs = 300;
		final float[][] batch = getImageSpectrum(pixels, 8192);

		for (int epoch = 1; epoch <= epochs; epoch++) {
			quantizer.updateDynamicLossWeights(epoch, epochs);
			
			quantizer.computeLossAndGradients(batch);
			quantizer.updateAdam();
		}

		final float[][] pal = quantizer.getPalette().rgb;
		final int[][] result = new int[pal.length][3];

		for (int i = 0; i < pal.length; i++) {
			result[i][0] = Gfx.saturate((int) (pal[i][0] * 255f));
			result[i][1] = Gfx.saturate((int) (pal[i][1] * 255f));
			result[i][2] = Gfx.saturate((int) (pal[i][2] * 255f));
		}

		return result;
	}
}
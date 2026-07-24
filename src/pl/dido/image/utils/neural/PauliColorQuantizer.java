package pl.dido.image.utils.neural;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

import pl.dido.image.utils.Gfx;

public class PauliColorQuantizer {

    public static Logger log = Logger.getLogger(PauliColorQuantizer.class.getCanonicalName());

    private final int numColors;
    private final Random random = new Random(42L);

    private float alphaRecon = 1.0f;
    private float alphaPauli = 0.01f;
    private float alphaOrtho = 0.01f;

    private final float contrastScale = 0.85f;

    private float[][] embeddings;
    private float[][] m;
    private float[][] v;
    private float[][] grad;

    private int step = 0;

    private final float learningRate = 0.02f;
    private final float beta1 = 0.9f;
    private final float beta2 = 0.999f;
    private final float epsilon = 1e-8f;

    private final float pauliRadius = 0.2f;
    private final float pauliTau = 1.0f;
    private final float stateEps = 1e-12f;

    public PauliColorQuantizer(final int numColors) {
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

        final int batchSize = pixelsBatch.length;
        if (batchSize == 0) {
            return new LossResult(0f, 0f, 0f, 0f);
        }

        final float[][] rgbPal = p.rgb;
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
                final float gradOut = (2f * (pred - target) * dSigmoid) / batchSize;

                grad[bestIdx][j] += gradOut;
            }
        }

        quantizationLoss /= batchSize;
        float pauliLoss = 0f;

        final float normFactor = (numColors > 1)
                ? 1f / (numColors * (numColors - 1f))
                : 0f;

        for (int i = 0; i < numColors; i++) {
            for (int j = i + 1; j < numColors; j++) {

                float squaredDistance = 0f;
                for (int d = 0; d < 4; d++) {

                	final float diff = embeddings[i][d] - embeddings[j][d];
                    squaredDistance += diff * diff;
                }

                final float stateDistance = (float) Math.sqrt(squaredDistance + stateEps);

                if (stateDistance < pauliRadius) {
                    final float ratio = stateDistance / pauliRadius;
                    
                    final float w = (float) Math.exp(
                            -(ratio * ratio) / (2.0f * pauliTau * pauliTau));

                    pauliLoss += w;

                    final float safeDist = Math.max(stateDistance, stateEps);
                    final float invDist = 1f / safeDist;

                    final float forceScale = alphaPauli * w * normFactor;

                    for (int d = 0; d < 4; d++) {
                        final float diff = embeddings[i][d] - embeddings[j][d];
                        final float force = forceScale * diff * invDist;

                        grad[i][d] += force;
                        grad[j][d] -= force;
                    }
                }
            }
        }

        pauliLoss *= normFactor;
        float orthoLoss = 0f;

        final float[] mean = new float[3];
        for (int i = 0; i < numColors; i++) 

        	for (int c = 0; c < 3; c++) 
                mean[c] += rgbPal[i][c];

        for (int c = 0; c < 3; c++) 
            mean[c] /= numColors;

        final float[][] centered = new float[numColors][3];
        for (int i = 0; i < numColors; i++)
        	
            for (int c = 0; c < 3; c++) {
                centered[i][c] = rgbPal[i][c] - mean[c];
                orthoLoss += centered[i][c] * centered[i][c];
            }

        orthoLoss /= (numColors * 3f);

        final float eps = 1e-12f;
        final float[] std = new float[3];
        
        for (int c = 0; c < 3; c++) {
            float var = 0f;
            for (int i = 0; i < numColors; i++) 
                var += centered[i][c] * centered[i][c];
            
            
            var /= Math.max(numColors, 1);
            std[c] = (float) Math.sqrt(Math.max(var, eps));
        }

        float corr01 = 0f;
        float corr02 = 0f;
        float corr12 = 0f;

        for (int i = 0; i < numColors; i++) {
            corr01 += (centered[i][0] / std[0]) * (centered[i][1] / std[1]);
            corr02 += (centered[i][0] / std[0]) * (centered[i][2] / std[2]);
            corr12 += (centered[i][1] / std[1]) * (centered[i][2] / std[2]);
        }

        corr01 /= numColors;
        corr02 /= numColors;
        corr12 /= numColors;

        orthoLoss += 0.5f * (corr01 * corr01 + corr02 * corr02 + corr12 * corr12);
        final float orthoScale = alphaOrtho / Math.max(numColors, 1);

        for (int i = 0; i < numColors; i++) {
            final float x = centered[i][0];
            final float y = centered[i][1];
            final float z = centered[i][2];

            grad[i][0] += orthoScale * (x + corr01 * y + corr02 * z);
            grad[i][1] += orthoScale * (y + corr01 * x + corr12 * z);
            grad[i][2] += orthoScale * (z + corr02 * x + corr12 * y);
        }

        final float totalLoss = alphaRecon * quantizationLoss + pauliLoss + alphaOrtho * orthoLoss;
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
        for (int i = 0; i < numColors; i++) {
            Arrays.fill(grad[i], 0f);
        }
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
        if (progress > 1.0f) {
            progress = 1.0f;
        }

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
        final PauliColorQuantizer quantizer = new PauliColorQuantizer(numColors);

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
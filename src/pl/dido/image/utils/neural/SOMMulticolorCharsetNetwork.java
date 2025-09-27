package pl.dido.image.utils.neural;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class SOMMulticolorCharsetNetwork {

	protected MulticolorNeuron matrix[][];
	protected int width, height;

	protected float radius = 0.6f;
	protected int epoch = 10;

	protected ArrayList<NetworkProgressListener> listeners;

	public SOMMulticolorCharsetNetwork(final int width, final int height) {
		this.width = width;
		this.height = height;
	}

	public void addProgressListener(final NetworkProgressListener listener) {
		if (listeners == null)
			listeners = new ArrayList<NetworkProgressListener>();

		listeners.add(listener);
	}

	public void notifyListeners(final String msg) {
		if (listeners != null)
			for (final NetworkProgressListener listener : listeners)
				listener.notifyProgress(msg);
	}

	protected void matrixInit(final SOMDataset<float[]> dataset) {
		matrix = new MulticolorNeuron[height][width];
		final ThreadLocalRandom rnd = ThreadLocalRandom.current();

		final ArrayList<float[]> samples = new ArrayList<float[]>();
		dataset.reset();
		
		for (int i = 0; i < dataset.size(); i++)
			samples.add(dataset.getNext().clone());
		
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				final MulticolorNeuron loc = new MulticolorNeuron();
				final float[] sample = samples.get(rnd.nextInt(samples.size()));
				
				for (int i = 0; i < 32; i++) {
					// przypisujemy próbkê + lekki szum gaussowski
					float val = sample[i] + (float) (rnd.nextGaussian() * 0.1);
					if (val < 0f)
						val = 0f;
					
					if (val > 3f)
						val = 3f;
					
					loc.set(i, val);
				}
				
				matrix[y][x] = loc;
			}
		}
	}

	public byte[] train(final SOMDataset<float[]> dataset) {
		matrixInit(dataset);
		
		final float delta_radius = radius / (epoch + 1);
		float currentEpoch = epoch, currentRadius = radius;

		while (currentEpoch-- > 0) {
			dataset.reset();

			for (int i = 0; i < dataset.size(); i++) {
				final float sample[] = dataset.getNext();
				final Position p = getBMU(sample);

				learn(p, sample, currentRadius);
			}

			currentRadius -= delta_radius;
			notifyListeners(String.valueOf(currentEpoch));
		}

		final byte result[] = new byte[width * height * 8];
		int index = 0;

		for (int y = 0; y < height; y++) {
			final MulticolorNeuron line[] = matrix[y];

			for (int x = 0; x < width; x++) {
				final float vec[] = line[x].getVector();

				byte data = 0;
				int j = 6;

				for (int i = 0; i < 32; i++) {
					final int b = Math.round(vec[i]);
					data |= b << j;

					if (j == 0) {
						result[index++] = data;
						data = 0;

						j = 6;
					} else
						j -= 2;
				}
			}
		}

		return result;
	}

	protected void learn(final Position best, final float sample[], final float radius) {
		for (int y = 0; y < height; y++) {
			final MulticolorNeuron line[] = matrix[y];

			for (int x = 0; x < width; x++) {
				final float gain = neighbourhood(distance(best.x, best.y, x, y), radius);
				if (gain != 0f)
					line[x].add(sample, gain);
			}
		}
	}

	protected static final float distance(final int x1, final int y1, final int x2, final int y2) {
		return (float) Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
	}

	protected static final float neighbourhood(final float d, final float r) {
		return (float) Math.exp((-1f * (d * d)) / (2f * (r * r)));
	}

	protected final static float diceSimilarity(final float[] a, final float[] b) {
		float tp = 0f, fn = 0f, fp = 0f;

		for (int color = 0; color < 4; color++)
			for (int i = 0; i < 32; i++) {
				final float ap = Math.round(a[i]);
				final float bp = Math.round(b[i]);
				
				tp += (ap == color) && (bp == color) ? 1f : 0f;
				fp += (ap != color) && (bp == color) ? 1f : 0f;
				fn += (ap == color) && (bp != color) ? 1f : 0f;
			}

		return 2f * tp / (2f * tp + fp + fn);
	}

	public Position getBMU(final float sample[]) {
		float max = 0;
		int bx = 0, by = 0;

		for (int y = 0; y < height; y++) {
			final MulticolorNeuron line[] = matrix[y];

			for (int x = 0; x < width; x++) {
				final float vec[] = line[x].getVector();
				final float m = diceSimilarity(vec, sample);

				if (m > max) {
					max = m;

					bx = x;
					by = y;
				}
			}
		}

		return new Position(bx, by);
	}
}

class MulticolorNeuron {
	private float vec[] = new float[32];
	private final float[] counters = new float[32];

	public void set(final int position, final float value) {
		counters[position] = value;
	}

	public void set(final int position) {
		counters[position]++;
	}

	public void clear(final int position) {
		counters[position]--;
	}

	public void add(final float vec[], final float gain) {
		for (int i = 0; i < 32; i++) {
			final float delta = vec[i] - counters[i];
			counters[i] += delta * gain;
		}
	}

	public float[] getVector() {
		for (int i = 0; i < 32; i++) {
			final float a = Math.round(counters[i]);
			vec[i] = a < 0f ? 0f : a > 3f ? 3f : a;
		}

		return vec;
	}
}
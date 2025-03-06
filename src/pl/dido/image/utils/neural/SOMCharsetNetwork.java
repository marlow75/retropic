package pl.dido.image.utils.neural;

import java.util.ArrayList;

import at.fhtw.ai.nn.utils.NetworkProgressListener;
import pl.dido.image.utils.BitVector;

public class SOMCharsetNetwork {

	protected Neuron matrix[][];
	protected int width, height;

	protected float radius = 0.4f;
	protected int epoch = 10;

	protected ArrayList<NetworkProgressListener> listeners;

	public SOMCharsetNetwork(final int width, final int height) {
		this.width = width;
		this.height = height;
	}

	public void addProgressListener(final NetworkProgressListener listener) {
		if (listeners == null)
			listeners = new ArrayList<NetworkProgressListener>();

		listeners.add(listener);
	}

	public void notifyListeners() {
		if (listeners != null)
			for (final NetworkProgressListener listener : listeners)
				listener.notifyProgress();
	}

	protected void matrixInit() {
		matrix = new Neuron[height][width];

		for (int y = 0; y < height; y++) {
			final Neuron line[] = matrix[y];

			for (int x = 0; x < width; x++) {
				final Neuron loc = new Neuron();
				line[x] = loc;
				
				final int bits = (int) (Math.random() * 64);
				for (int i = 0; i < bits; i++)
					loc.set((int)(Math.random() * 64));
			}
		}
	}

	public byte[] train(final SOMDataset<BitVector> dataset) {
		matrixInit();
		final float delta_radius = radius / (epoch + 1);

		while (epoch-- > 0) {
			dataset.reset();

			for (int i = 0; i < dataset.size(); i++) {
				final BitVector sample = dataset.getNext();
				learn(getBMU(sample), sample);
			}

			radius -= delta_radius;
			notifyListeners();
		}

		final byte result[] = new byte[width * height * 8];
		int index = 0;

		for (int y = 0; y < height; y++) {
			final Neuron line[] = matrix[y];

			for (int x = 0; x < width; x++) {
				final BitVector vec = line[x].getVector();
				byte data = 0;

				int j = 7;
				for (int i = 0; i < 64; i++) {
					if (vec.getQuick(i))
						data |= 1 << j;

					j--;
					if (i % 8 == 7) {
						result[index++] = data;
						data = 0;
						j = 7;
					}
				}
			}
		}

		return result;
	}

	protected void learn(final Position best, final BitVector sample) {
		for (int y = 0; y < height; y++) {
			final Neuron line[] = matrix[y];

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

	protected final static int hammingDistance(final BitVector a, final BitVector b) {
		int distance = 0;

		for (int j = a.size(); j-- > 0;)
			if (a.getQuick(j) != b.getQuick(j))
				distance++;

		return distance;
	}

	protected final static float diceSimilarity(final BitVector a, final BitVector b) {
		float tp = 0, fn = 0, fp = 0;

		for (int i = 0; i < 64; i++) {
			tp += a.getQuick(i) == b.getQuick(i) ? 1 : 0;
			fp += !a.getQuick(i) == b.getQuick(i) ? 1 : 0;
			fn += a.getQuick(i) == !b.getQuick(i) ? 1 : 0;
		}

		return 2 * tp / (2 * tp + fp + fn);
	}

	protected Position getBMU(final BitVector sample) {
		float max = 0;
		int bx = 0, by = 0;

		for (int y = 0; y < height; y++) {
			final Neuron line[] = matrix[y];

			for (int x = 0; x < width; x++) {
				final BitVector vec = line[x].getVector();
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

class Neuron {
	private static int counter;
	private int number;

	private final float[] counters = new float[64];

	public Neuron() {
		this.number = counter++;
	}

	public void set(final int position) {
		counters[position]++;
	}

	public void clear(final int position) {
		counters[position]--;
	}

	public void add(final BitVector vec, final float gain) {
		for (int i = 0; i < 64; i++)
			counters[i] += vec.getQuick(i) ? gain : -gain;
	}

	public BitVector getVector() {
		final BitVector vec = new BitVector(64);

		for (int i = 0; i < 64; i++)
			if (counters[i] > 0)
				vec.set(i);
			else
				vec.clear(i);

		return vec;
	}

	public String toString() {
		final BitVector vec = getVector();
		String s = "i: " + number;

		for (int i = 0; i < 64; i++) {
			if (i % 8 == 0)
				s += '\n';

			s += vec.getQuick(i) ? '1' : '.';
		}

		return s;
	}
}
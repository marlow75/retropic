package pl.dido.image.utils;

public class SOMFixedPalette {
	
	protected float matrix[][][];
	protected int width, height;

	protected float rate = 0.6f; // defaults
	protected float radius = 2f;

	protected int epoch = 10;
	protected float scale;

	private void initialize(final int width, final int height, final float rate, final float radius, final int epoch,
			final int bits) {
		this.width = width;
		this.height = height;

		this.rate = rate;
		this.radius = radius;
		this.epoch = epoch;

		this.scale = (float) Math.pow(2, 8 - bits); // 2 ^ (8 - bits);
	}

	public SOMFixedPalette(final int width, final int height, final int bits) {
		initialize(width, height, rate, radius, epoch, bits);
	}

	public SOMFixedPalette(final int width, final int height, final float rate, final float radius, final int epoch,
			final int bits) {
		initialize(width, height, rate, radius, epoch, bits);
	}

	protected void matrixInit() {
		matrix = new float[height][width][3];

		for (int y = 0; y < height; y++) {
			final float line[][] = matrix[y];

			for (int x = 0; x < width; x++) {
				final float pixel[] = line[x];

				pixel[0] = (float) (Math.random() * 255) / scale;
				pixel[1] = (float) (Math.random() * 255) / scale;
				pixel[2] = (float) (Math.random() * 255) / scale;
			}
		}
	}

	public int[][] train(final byte rgb[]) {
		matrixInit();

		final float delta_rate = rate / epoch;
		final float delta_radius = radius / epoch;
		
		final int len = rgb.length;

		while (epoch-- > 0) {
			for (int i = 0; i < len; i += 3) {
				// pickup sample
				final float r = ((rgb[i] & 0xff) / scale);
				final float g = ((rgb[i + 1] & 0xff) / scale);
				final float b = ((rgb[i + 2] & 0xff) / scale);

				// get best matching neuron and modify all neurons in radius
				learn(getBMU(r, g, b), r, g, b);
			}

			rate -= delta_rate;
			radius -= delta_radius;
		}

		final int result[][] = new int[width * height][3];
		for (int y = 0; y < height; y++) {
			final float line[][] = matrix[y];
			final int p = y * width;

			for (int x = 0; x < width; x++) {
				final int i = p + x;

				final float row1[] = line[x];
				final int row2[] = result[i];

				row2[0] = (int) (row1[0] * scale);
				row2[1] = (int) (row1[1] * scale);
				row2[2] = (int) (row1[2] * scale);
			}
		}

		return result;
	}

	protected void learn(final Position best, final float r, final float g, final float b) {
		for (int y = 0; y < height; y++) {
			final float line[][] = matrix[y];

			for (int x = 0; x < width; x++) {
				// learn rate
				final float n = rate * neighbourhood(distance(best.x, best.y, x, y), radius);
				final float row[] = line[x];

				row[0] += n * (r - row[0]);
				row[1] += n * (g - row[1]);
				row[2] += n * (b - row[2]);
			}
		}
	}
	
	protected static final float neighbourhood(final float d, final float r) {
		return (float) Math.exp((-1f * (d * d)) / (2f * (r * r)));
	}

	protected static final float distance(final int x1, final int y1, final int x2, final int y2) {
		return (float) Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
	}

	protected Position getBMU(final float red, final float green, final float blue) {
		int bx = 0, by = 0;
		float min = Float.MAX_VALUE;

		for (int y = 0; y < height; y++) {
			final float line[][] = matrix[y];

			for (int x = 0; x < width; x++) {
				final float row[] = line[x];

				final float r = row[0];
				final float g = row[1];
				final float b = row[2];

				// simple euclidean
				final float d = Utils.euclideanDistance(red, green, blue, r, g, b);

				if (d < min) {
					min = d;

					bx = x;
					by = y;
				}
			}
		}

		return new Position(bx, by);
	}
}
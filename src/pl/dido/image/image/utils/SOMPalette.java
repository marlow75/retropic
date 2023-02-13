package pl.dido.image.utils;

public class SOMPalette {

	protected int matrix[][][];
	protected int width, height;

	protected float rate = 0.6f;  // defaults
	protected float radius = 2f;
	protected int epoch = 10;
	
	public SOMPalette(final int width, final int height) {
		this.width = width;
		this.height = height;
	}
	
	public SOMPalette(final int width, final int height, final float rate, final float radius, int epoch) {
		this.width = width;
		this.height = height;
		
		this.rate = rate;
		this.radius = radius;
		this.epoch = epoch;
	}

	protected void matrixInit() {
		matrix = new int[height][width][3];
		
		for (int y = 0; y < height; y++) {
			final int line[][] = matrix[y];
			for (int x = 0; x < width; x++) {
				line[x][0] = (int) (Math.random() * 255);
				line[x][1] = (int) (Math.random() * 255);
				line[x][2] = (int) (Math.random() * 255);
			}
		}
	}

	public int[][] train(final byte rgb[]) {
		matrixInit();
				
		final float delta_rate = rate / epoch;
		final float delta_radius = radius / epoch;

		while (epoch-- > 0) {
			for (int i = 0; i < rgb.length; i += 3) {
				// pickup sample
				final int red   = rgb[i    ] & 0xff;
				final int green = rgb[i + 1] & 0xff;
				final int blue  = rgb[i + 2] & 0xff;

				// get best matching neuron and modify all neurons in radius
				learn(getBMU(red, green, blue), red, green, blue);
			}

			rate   -= delta_rate;
			radius -= delta_radius;
		}

		final int result[][] = new int[width * height][3];
		for (int y = 0; y < height; y++) {
			final int line[][] = matrix[y];
			
			for (int x = 0; x < width; x++) {
				final int i = y * width + x;
				
				result[i][0] = line[x][0];
				result[i][1] = line[x][1];
				result[i][2] = line[x][2];
			}
		}

		return result;
	}

	protected void learn(final Position best, final int red, final int green, final int blue) {
		for (int y = 0; y < height; y++) {
			final int line[][] = matrix[y];
			for (int x = 0; x < width; x++) {				
				// learn rate
				final float n = rate * neighbourhood(distance(best.x, best.y, x, y), radius);

				line[x][0] += (int) (n * (red   - line[x][0]));
				line[x][1] += (int) (n * (green - line[x][1]));
				line[x][2] += (int) (n * (blue  - line[x][2]));
			}
		}
	}

	protected static final float neighbourhood(final float d, final float r) {
		return (float) Math.exp((-1f * (d * d)) / (2f * (r * r)));
	}

	protected static final float distance(final int x1, final int y1, final int x2, final int y2) {
		return (float) Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
	}

	protected Position getBMU(final int red, final int green, final int blue) {
		int bx = 0, by = 0;
		float min = Float.MAX_VALUE;

		for (int y = 0; y < height; y++) {
			final int line[][] = matrix[y];
			
			for (int x = 0; x < width; x++) {				
				final int r = line[x][0];
				final int g = line[x][1];
				final int b = line[x][2];

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
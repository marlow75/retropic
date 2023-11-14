package pl.dido.image.utils.neural;

import pl.dido.image.utils.Gfx;

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
				final int rgb[] = line[x];
				
				rgb[0] = (int) (Math.random() * 255);
				rgb[1] = (int) (Math.random() * 255);
				rgb[2] = (int) (Math.random() * 255);
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
			
			for (int x = 0; x < width; x++)
				System.arraycopy(line[x], 0, result[y * width + x], 0, 3);
		}

		return result;
	}

	protected void learn(final Position best, final int r, final int g, final int b) {
		for (int y = 0; y < height; y++) {
			final int line[][] = matrix[y];
			
			for (int x = 0; x < width; x++) {				
				// learn rate
				final float n = rate * neighbourhood(distance(best.x, best.y, x, y), radius);
				final int rgb[] = line[x];

				rgb[0] += (int) (n * (r - rgb[0]));
				rgb[1] += (int) (n * (g - rgb[1]));
				rgb[2] += (int) (n * (b - rgb[2]));
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
				final int rgb[] = line[x];
				
				final int r = rgb[0];
				final int g = rgb[1];
				final int b = rgb[2];

				// simple euclidean				
				final float d = Gfx.euclideanDistance(red, green, blue, r, g, b);
				
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
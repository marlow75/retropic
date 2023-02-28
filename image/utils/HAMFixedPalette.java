package pl.dido.image.utils;

public class HAMFixedPalette extends SOMFixedPalette {
	
	public HAMFixedPalette(final int width, final int height, final int bits) {
		super(width, height, bits);
	}

	@Override
	public int[][] train(final byte rgb[]) {
		matrixInit();

		final float delta_rate = rate / epoch;
		final float delta_radius = radius / epoch;

		final int len = rgb.length;
		float or = 0, og = 0, ob = 0, a = 0f;
		
		while (epoch-- > 0) {
			for (int i = 0; i < len; i += 3) {

				// pickup sample
				final float r = ((rgb[i] & 0xff) / scale);
				final float g = ((rgb[i + 1] & 0xff) / scale);
				final float b = ((rgb[i + 2] & 0xff) / scale);
				
				final float d = (float) Math.sqrt(Utils.euclideanDistance(r, g, b, or, og, ob));				
				a = ((d + a) / 2) * 1.35f;
				
				if (d > a)
					learn(getBMU(r, g, b), r, g, b);			
				
				or = r;
				og = g;
				ob = b;
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
}
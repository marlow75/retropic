package pl.dido.image.utils.neural;

import pl.dido.image.utils.Gfx;

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
				
				final float d = (float) Math.sqrt(Gfx.euclideanDistance(r, g, b, or, og, ob));
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

		return getPalette();
	}
}
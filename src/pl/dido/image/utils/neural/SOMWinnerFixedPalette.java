package pl.dido.image.utils.neural;

import pl.dido.image.utils.Gfx;

public class SOMWinnerFixedPalette extends SOMFixedPalette {

	public SOMWinnerFixedPalette(final int width, final int height, final float rate,
			final int epoch, final int bits, final int skip) {
		super(width, height, rate, 0f, epoch, bits, skip);
	}
	
	public SOMWinnerFixedPalette(final int width, final int height, final int bits) {
		super(width, height, bits);
	}
	
	public SOMWinnerFixedPalette(final int width, final int height, final int bits, final int skip) {
		super(width, height, bits, skip);
	}

	@Override
	protected void learn(final Position best, final float r, final float g, final float b) {
		final float bmu[] = matrix[best.y][best.x];

		bmu[0] += rate * (r - bmu[0]);
		bmu[1] += rate * (g - bmu[1]);
		bmu[2] += rate * (b - bmu[2]);
	}
	
	@Override
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
				final float d = Gfx.perceptedDistance(red, green, blue, r, g, b);

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

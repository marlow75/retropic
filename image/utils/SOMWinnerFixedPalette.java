package pl.dido.image.utils;

public class SOMWinnerFixedPalette extends SOMFixedPalette {

	public SOMWinnerFixedPalette(final int width, final int height, final float rate, final float radius,
			final int epoch, final int bits, final int skip) {
		super(width, height, rate, radius, epoch, bits, skip);
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
}

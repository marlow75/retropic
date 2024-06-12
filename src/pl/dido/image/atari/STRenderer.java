package pl.dido.image.atari;

import java.awt.image.BufferedImage;

import pl.dido.image.renderer.AbstractPictureColorsRenderer;
import pl.dido.image.utils.Config;
import pl.dido.image.utils.Gfx;
import pl.dido.image.utils.Config.DITHERING;
import pl.dido.image.utils.neural.SOMFixedPalette;

public class STRenderer extends AbstractPictureColorsRenderer {

	protected int bitplanes[] = new int[4 * 20 * 200];

	public STRenderer(final BufferedImage image, final Config config) {
		super(image, config);
		palette = new int[512][3];
	}

	@Override
	protected void setupPalette() {
		int i = 0;
		final float k = 255 / 7f;

		for (int r = 0; r < 8; r++) {
			final int rk = Math.round(r * k);

			for (int g = 0; g < 8; g++) {
				final int gk = Math.round(g * k);

				for (int b = 0; b < 8; b++) {
					final int c[] = palette[i];

					c[0] = rk;
					c[1] = gk;
					c[2] = Math.round(b * k);

					i++;
				}
			}
		}
	}

	@Override
	protected void imagePostproces() {
		final SOMFixedPalette training = new SOMFixedPalette(4, 4, 3); // 4x4 = 16 colors
		
		pictureColors = normalizePalette(training.train(pixels));
		std16();
	}

	protected void std16() {
		final int[] work = Gfx.copy2Int(pixels);
		int r0, g0, b0;

		final int width3 = screenWidth * 3;
		int index = 0, shift = 15;

		for (int y = 0; y < screenHeight; y++) {
			final int k = y * width3;
			final int k1 = (y + 1) * width3;
			final int k2 = (y + 2) * width3;

			for (int x = 0; x < screenWidth; x++) {
				final int pyx = k + x * 3;
				final int py1x = k1 + x * 3;
				final int py2x = k2 + x * 3;

				r0 = Gfx.saturate(work[pyx]);
				g0 = Gfx.saturate(work[pyx + 1]);
				b0 = Gfx.saturate(work[pyx + 2]);
				
				final int color;
				
				if (config.dither_alg == DITHERING.BAYER) {
					r0 = Gfx.bayer4x4(x % 4, y % 4, r0, 7);
					g0 = Gfx.bayer4x4(x % 4, y % 4, g0, 7);
					b0 = Gfx.bayer4x4(x % 4, y % 4, b0, 7);
				}
				
				color = Gfx.getColorIndex(colorAlg, pictureColors, r0, g0, b0);
				
				final int c[] = pictureColors[color];
				final int r = c[0];
				final int g = c[1];
				final int b = c[2];

				work[pyx] = r;
				work[pyx + 1] = g;
				work[pyx + 2] = b;

				pixels[pyx] = (byte) r;
				pixels[pyx + 1] = (byte) g;
				pixels[pyx + 2] = (byte) b;

				final int value = color & 0xf;

				bitplanes[index + 3] |= ((value & 8) >> 3) << shift;
				bitplanes[index + 2] |= ((value & 4) >> 2) << shift;
				bitplanes[index + 1] |= ((value & 2) >> 1) << shift;
				bitplanes[index] |= (value & 1) << shift;

				if (shift == 0) {
					shift = 15;
					index += 4;
				} else
					shift--;

				if (config.dither_alg == DITHERING.ATKINSON || config.dither_alg == DITHERING.FLOYDS) {
					final int r_error = r0 - r;
					final int g_error = g0 - g;
					final int b_error = b0 - b;

					switch (config.dither_alg) {
					case FLOYDS:
						if (x < screenWidth - 1) {
							work[pyx + 3] += (r_error * 7) / 16;
							work[pyx + 3 + 1] += (g_error * 7) / 16;
							work[pyx + 3 + 2] += (b_error * 7) / 16;
						}
						if (y < screenHeight - 1) {
							work[py1x - 3] += (r_error * 3) / 16;
							work[py1x - 3 + 1] += (g_error * 3) / 16;
							work[py1x - 3 + 2] += (b_error * 3) / 16;

							work[py1x] += (r_error * 5) / 16;
							work[py1x + 1] += (g_error * 5) / 16;
							work[py1x + 2] += (b_error * 5) / 16;

							if (x < screenWidth - 1) {
								work[py1x + 3] += r_error / 16;
								work[py1x + 3 + 1] += g_error / 16;
								work[py1x + 3 + 2] += b_error / 16;
							}
						}
						break;
					case ATKINSON:
						if (x < screenWidth - 1) {
							work[pyx + 3] += r_error >> 3;
							work[pyx + 3 + 1] += g_error >> 3;
							work[pyx + 3 + 2] += b_error >> 3;

							if (x < screenWidth - 2) {
								work[pyx + 6] += r_error >> 3;
								work[pyx + 6 + 1] += g_error >> 3;
								work[pyx + 6 + 2] += b_error >> 3;
							}
						}
						if (y < screenHeight - 1) {
							work[py1x - 3] += r_error >> 3;
							work[py1x - 3 + 1] += g_error >> 3;
							work[py1x - 3 + 2] += b_error >> 3;

							work[py1x] += r_error >> 3;
							work[py1x + 1] += g_error >> 3;
							work[py1x + 2] += b_error >> 3;

							if (x < screenWidth - 1) {
								work[py1x + 3] += r_error >> 3;
								work[py1x + 3 + 1] += g_error >> 3;
								work[py1x + 3 + 2] += b_error >> 3;
							}

							if (y < screenHeight - 2) {
								work[py2x] += r_error >> 3;
								work[py2x + 1] += g_error >> 3;
								work[py2x + 2] += b_error >> 3;
							}
						}
						break;
					default:
						break;
					}
				}
			}
		}
	}
}
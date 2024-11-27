package pl.dido.image.amiga;

import java.awt.image.BufferedImage;

import pl.dido.image.renderer.AbstractPictureColorsRenderer;
import pl.dido.image.utils.Config;
import pl.dido.image.utils.Gfx;
import pl.dido.image.utils.Config.DITHERING;
import pl.dido.image.utils.neural.HAMFixedPalette;
import pl.dido.image.utils.neural.SOMFixedPalette;

public class Amiga1200Renderer extends AbstractPictureColorsRenderer {

	protected int bitplanes[][];

	public Amiga1200Renderer(final BufferedImage image, final Config config) {
		super(image, config);
		// do not use generated machine palette, it is true color
	}

	@Override
	protected void imageDithering() {
		// not needed, 16M palette
	}

	@Override
	protected void setupPalette() {
		// do not generate palette
	}

	@Override
	protected void imagePostproces() {
		final SOMFixedPalette training;

		switch (((Amiga1200Config) config).video_mode) {
		case HAM8_320x256:
		case HAM8_320x512:
		case HAM8_640x512:
			training = new HAMFixedPalette(8, 8, 8); // 8x8 = 64 colors (8 bits)
			pictureColors = training.train(pixels);

			ham8Encoded();
			break;
		case STD_320x256:
		case STD_320x512:
		case STD_640x512:
			training = new SOMFixedPalette(16, 16, 8, 4); // 16x16 = 256 colors (8 bits)
			pictureColors = training.train(pixels);

			switch (config.dither_alg) {
			case BAYER2x2:
			case BAYER4x4:
			case BAYER8x8:
			case BAYER16x16:
				bayer256();
				break;
			default:
				standard256();
				break;
			}
		}
	}

	protected void standard256() {
		final int[] work = Gfx.copy2Int(pixels);
		bitplanes = new int[(screenWidth >> 4) * screenHeight][8]; // 8 planes

		int r0, g0, b0;

		final int width3 = screenWidth * 3;
		int index = 0, shift = 15; // 16

		for (int y = 0; y < screenHeight; y++) {
			final int k = y * width3;
			final int k1 = (y + 1) * width3;
			final int k2 = ((y + 2) * width3);

			for (int x = 0; x < width3; x += 3) {
				final int pyx = k + x;
				final int py1x = k1 + x;
				final int py2x = k2 + x;

				r0 = Gfx.saturate(work[pyx]);
				g0 = Gfx.saturate(work[pyx + 1]);
				b0 = Gfx.saturate(work[pyx + 2]);

				final int color = Gfx.getColorIndex(colorAlg, pictureColors, r0, g0, b0); // 256 colors
				final int c[] = pictureColors[color];

				final int r = c[0];
				final int g = c[1];
				final int b = c[2];

				pixels[pyx] = (byte) r;
				pixels[pyx + 1] = (byte) g;
				pixels[pyx + 2] = (byte) b;

				bitplanes[index][7] |= ((color & 128) >> 7) << shift;
				bitplanes[index][6] |= ((color & 64) >> 6) << shift;
				bitplanes[index][5] |= ((color & 32) >> 5) << shift;
				bitplanes[index][4] |= ((color & 16) >> 4) << shift;
				bitplanes[index][3] |= ((color & 8) >> 3) << shift;
				bitplanes[index][2] |= ((color & 4) >> 2) << shift;
				bitplanes[index][1] |= ((color & 2) >> 1) << shift;
				bitplanes[index][0] |= (color & 1) << shift;

				if (shift == 0) {
					shift = 15;
					index += 1; // 8 planes
				} else
					shift--;

				if (config.dither_alg != DITHERING.NONE) {
					final int r_error = r0 - r;
					final int g_error = g0 - g;
					final int b_error = b0 - b;

					switch (config.dither_alg) {
					case FLOYDS:
						if (x < (screenWidth - 1) * 3) {
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

							if (x < (screenWidth - 1) * 3) {
								work[py1x + 3] += r_error / 16;
								work[py1x + 3 + 1] += g_error / 16;
								work[py1x + 3 + 2] += b_error / 16;
							}
						}
						break;
					case ATKINSON:
						if (x < (screenWidth - 1) * 3) {
							work[pyx + 3] += r_error >> 3;
							work[pyx + 3 + 1] += g_error >> 3;
							work[pyx + 3 + 2] += b_error >> 3;

							if (x < (screenWidth - 2) * 3) {
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

							if (x < (screenWidth - 1) * 3) {
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

	protected void bayer256() {
		final int[] work = Gfx.copy2Int(pixels);
		bitplanes = new int[(screenWidth >> 4) * screenHeight][8]; // 8 planes

		int r0, g0, b0;

		final int width3 = screenWidth * 3;
		int index = 0, shift = 15; // 16

		for (int y = 0; y < screenHeight; y++) {
			final int k = y * width3;

			for (int x = 0; x < screenWidth; x++) {
				final int pyx = k + x * 3;

				r0 = work[pyx];
				g0 = work[pyx + 1];
				b0 = work[pyx + 2];
				
				switch (config.dither_alg) {
				case BAYER2x2:
					r0 = Gfx.bayer2x2(x, y, r0, config.error_threshold);
					g0 = Gfx.bayer2x2(x, y, g0, config.error_threshold);
					b0 = Gfx.bayer2x2(x, y, b0, config.error_threshold);
					break;
				case BAYER4x4:
					r0 = Gfx.bayer4x4(x, y, r0, config.error_threshold);
					g0 = Gfx.bayer4x4(x, y, g0, config.error_threshold);
					b0 = Gfx.bayer4x4(x, y, b0, config.error_threshold);
					break;
				case BAYER8x8:
					r0 = Gfx.bayer8x8(x, y, r0, config.error_threshold);
					g0 = Gfx.bayer8x8(x, y, g0, config.error_threshold);
					b0 = Gfx.bayer8x8(x, y, b0, config.error_threshold);
					break;
				case BAYER16x16:
					r0 = Gfx.bayer16x16(x, y, r0, config.error_threshold);
					g0 = Gfx.bayer16x16(x, y, g0, config.error_threshold);
					b0 = Gfx.bayer16x16(x, y, b0, config.error_threshold);
					break;
				default:
					break;
				}

				final int color = Gfx.getColorIndex(colorAlg, pictureColors, r0, g0, b0); // 256 colors
				final int c[] = pictureColors[color];

				final int r = c[0];
				final int g = c[1];
				final int b = c[2];

				pixels[pyx] = (byte) r;
				pixels[pyx + 1] = (byte) g;
				pixels[pyx + 2] = (byte) b;

				bitplanes[index][7] |= ((color & 128) >> 7) << shift;
				bitplanes[index][6] |= ((color & 64) >> 6) << shift;
				bitplanes[index][5] |= ((color & 32) >> 5) << shift;
				bitplanes[index][4] |= ((color & 16) >> 4) << shift;
				bitplanes[index][3] |= ((color & 8) >> 3) << shift;
				bitplanes[index][2] |= ((color & 4) >> 2) << shift;
				bitplanes[index][1] |= ((color & 2) >> 1) << shift;
				bitplanes[index][0] |= (color & 1) << shift;

				if (shift == 0) {
					shift = 15;
					index += 1; // 8 planes
				} else
					shift--;
			}
		}
	}

	protected void ham8Encoded() {
		final float[] work = Gfx.copy2float(pixels);
		bitplanes = new int[(screenWidth >> 4) * screenHeight][8]; // 8 planes

		int r0, g0, b0, r = 0, g = 0, b = 0;
		final int width3 = screenWidth * 3;

		int index = 0, shift = 15; // WORD
		int modifyRed, modifyBlue;

		switch (image.getType()) {
		case BufferedImage.TYPE_3BYTE_BGR:
			modifyRed = 0b01000000;
			modifyBlue = 0b10000000;
			break;
		default:
			modifyRed = 0b10000000;
			modifyBlue = 0b01000000;
			break;
		}

		for (int y = 0; y < screenHeight; y++) {
			boolean nextPixel = false;
			final int k = y * width3;

			final int k1 = (y + 1) * width3;
			final int k2 = (y + 2) * width3;

			for (int x = 0; x < width3; x += 3) {
				final int pyx = k + x;
				final int py1x = k1 + x;

				final int py2x = k2 + x;

				// get picture RGB components
				r0 = Gfx.saturate((int) work[pyx]);
				g0 = Gfx.saturate((int) work[pyx + 1]);
				b0 = Gfx.saturate((int) work[pyx + 2]);

				// find closest palette color
				int action = Gfx.getColorIndex(colorAlg, pictureColors, r0, g0, b0); // 64 color palette
				final int pc[] = pictureColors[action];

				if (nextPixel) { // it's not first pixel in a row so use best matching color
					// distance to palette match
					final float dpc = Gfx.getDistance(colorAlg, r0, g0, b0, pc[0], pc[1], pc[2]);

					float min_r = Float.MAX_VALUE; // minimum red
					float min_g = min_r;
					float min_b = min_r;

					int ri = -1;
					int gi = -1;
					int bi = -1;

					// calculate all color change possibilities and measure distances
					for (int i = 0; i < 64; i++) {
						// scaled color
						final int scaled = (int) (i * 4.048f);

						// which component change gets minimum error?
						final float dr = Gfx.getDistance(colorAlg, r0, g0, b0, scaled, g, b);
						final float dg = Gfx.getDistance(colorAlg, r0, g0, b0, r, scaled, b);
						final float db = Gfx.getDistance(colorAlg, r0, g0, b0, r, g, scaled);

						if (dr < min_r) {
							ri = scaled;
							min_r = dr;
						}

						if (dg < min_g) {
							gi = scaled;
							min_g = dg;
						}

						if (db < min_b) {
							bi = scaled;
							min_b = db;
						}
					}

					final float ham = Gfx.min(min_r, min_g, min_b);

					// check which color is best, palette or HAM?
					if (ham < dpc) {
						// HAM is best, alter color
						if (ham == min_r) {
							// red
							r = ri;
							action = modifyRed | (ri >> 2);
						} else if (ham == min_g) {
							// green
							g = gi;
							action = 0b11000000 | (gi >> 2);
						} else if (ham == min_b) {
							// blue
							b = bi;
							action = modifyBlue | (bi >> 2);
						}
					} else {
						r = pc[0];
						g = pc[1];
						b = pc[2];
					}
				} else {
					nextPixel = true;
					r = pc[0];
					g = pc[1];
					b = pc[2];
				}

				bitplanes[index][7] |= ((action & 128) >> 7) << shift;
				bitplanes[index][6] |= ((action & 64) >> 6) << shift;

				bitplanes[index][5] |= ((action & 32) >> 5) << shift;
				bitplanes[index][4] |= ((action & 16) >> 4) << shift;

				bitplanes[index][3] |= ((action & 8) >> 3) << shift;
				bitplanes[index][2] |= ((action & 4) >> 2) << shift;

				bitplanes[index][1] |= ((action & 2) >> 1) << shift;
				bitplanes[index][0] |= (action & 1) << shift;

				if (shift == 0) {
					shift = 15; // WORD
					index += 1;
				} else
					shift--;

				pixels[pyx] = (byte) r;
				pixels[pyx + 1] = (byte) g;
				pixels[pyx + 2] = (byte) b;

				float r_error = r0 - r;
				float g_error = g0 - g;
				float b_error = b0 - b;

				switch (config.dither_alg) {
				case FLOYDS:
					if (x < (screenWidth - 1) * 3) {
						work[pyx + 3] += r_error * 7 / 16;
						work[pyx + 3 + 1] += g_error * 7 / 16;
						work[pyx + 3 + 2] += b_error * 7 / 16;
					}
					if (y < screenHeight - 1) {
						work[py1x - 3] += r_error * 3 / 16;
						work[py1x - 3 + 1] += g_error * 3 / 16;
						work[py1x - 3 + 2] += b_error * 3 / 16;

						work[py1x] += r_error * 5 / 16;
						work[py1x + 1] += g_error * 5 / 16;
						work[py1x + 2] += b_error * 5 / 16;

						if (x < (screenWidth - 1) * 3) {
							work[py1x + 3] += r_error / 16;
							work[py1x + 3 + 1] += g_error / 16;
							work[py1x + 3 + 2] += b_error / 16;
						}
					}
					break;
				case ATKINSON:
					if (x < (screenWidth - 1) * 3) {
						work[pyx + 3] += r_error * 1 / 8;
						work[pyx + 3 + 1] += g_error * 1 / 8;
						work[pyx + 3 + 2] += b_error * 1 / 8;

						if (x < (screenWidth - 2) * 3) {
							work[pyx + 6] += r_error * 1 / 8;
							work[pyx + 6 + 1] += g_error * 1 / 8;
							work[pyx + 6 + 2] += b_error * 1 / 8;
						}
					}
					if (y < screenHeight - 1) {
						work[py1x - 3] += r_error * 1 / 8;
						work[py1x - 3 + 1] += g_error * 1 / 8;
						work[py1x - 3 + 2] += b_error * 1 / 8;

						work[py1x] += r_error * 1 / 8;
						work[py1x + 1] += g_error * 1 / 8;
						work[py1x + 2] += b_error * 1 / 8;

						if (x < (screenWidth - 1) * 3) {
							work[py1x + 3] += r_error * 1 / 8;
							work[py1x + 3 + 1] += g_error * 1 / 8;
							work[py1x + 3 + 2] += b_error * 1 / 8;
						}

						if (y < screenHeight - 2) {
							work[py2x] += r_error * 1 / 8;
							work[py2x + 1] += g_error * 1 / 8;
							work[py2x + 2] += b_error * 1 / 8;
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
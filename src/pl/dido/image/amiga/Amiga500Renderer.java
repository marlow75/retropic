package pl.dido.image.amiga;

import java.awt.image.BufferedImage;

import pl.dido.image.renderer.AbstractPictureColorsRenderer;
import pl.dido.image.utils.Config;
import pl.dido.image.utils.Gfx;
import pl.dido.image.utils.Config.DITHERING;
import pl.dido.image.utils.neural.HAMFixedPalette;
import pl.dido.image.utils.neural.SOMFixedPalette;

public class Amiga500Renderer extends AbstractPictureColorsRenderer {

	protected int bitplanes[][];

	public Amiga500Renderer(final BufferedImage image, final Config config) {
		super(image, config);
		palette = new int[4096][3];
	}

	@Override
	protected void setupPalette() {
		int i = 0;

		for (int r = 0; r < 16; r++) {
			final int rk = r * 17;

			for (int g = 0; g < 16; g++) {
				final int gk = g * 17;

				for (int b = 0; b < 16; b++) {
					final int c[] = palette[i];

					c[0] = rk;
					c[1] = gk;
					c[2] = b * 17;

					i++;
				}
			}
		}
	}

	@Override
	protected void imageDithering() {
		switch (config.dither_alg) {
		case ATKINSON, FLOYDS:
			Gfx.dithering(pixels, palette, config);
			break;
		default:
			break;
		}	
	}

	@Override
	protected void imagePostproces() {
		final SOMFixedPalette training;

		switch (((Amiga500Config) config).video_mode) {
		case HAM6_320x256:
		case HAM6_320x512:
			training = new HAMFixedPalette(4, 4, 4); // 4x4 = 16 colors (4 bits)
			pictureColors = normalizePalette(training.train(pixels));

			ham6Encoded();
			break;
		case STD_320x256:
		case STD_320x512:
			training = new SOMFixedPalette(8, 4, 5); // 8x4 = 32 colors (5 bits)
			pictureColors = normalizePalette(training.train(pixels));

			switch (config.dither_alg) {
			case NONE, ATKINSON, FLOYDS:
				standard32();
				break;
			default:
				bayer32();
				break;
			}
		}
	}

	protected void standard32() {
		final int[] work = Gfx.copy2Int(pixels);
		bitplanes = new int[(screenWidth >> 4) * screenHeight][5]; // 5 planes

		int r0, g0, b0;

		final int width3 = screenWidth * 3;
		int index = 0, shift = 15; // 16

		for (int y = 0; y < screenHeight; y++) {
			final int k = y * width3;
			final int k1 = (y + 1) * width3;
			final int k2 = (y + 2) * width3;

			for (int x = 0; x < width3; x += 3) {
				final int pyx = k + x;
				final int py1x = k1 + x;
				final int py2x = k2 + x;

				r0 = Gfx.saturate(work[pyx]);
				g0 = Gfx.saturate(work[pyx + 1]);
				b0 = Gfx.saturate(work[pyx + 2]);

				final int color = Gfx.getColorIndex(colorAlg, pictureColors, r0, g0, b0);
				final int c[] = pictureColors[color];

				final int r = c[0];
				final int g = c[1];
				final int b = c[2];

				pixels[pyx] = (byte) r;
				pixels[pyx + 1] = (byte) g;
				pixels[pyx + 2] = (byte) b;

				final int value = color & 0xff;

				bitplanes[index][4] |= ((value & 16) >> 4) << shift;
				bitplanes[index][3] |= ((value & 8) >> 3) << shift;
				bitplanes[index][2] |= ((value & 4) >> 2) << shift;
				bitplanes[index][1] |= ((value & 2) >> 1) << shift;
				bitplanes[index][0] |= (value & 1) << shift;

				if (shift == 0) {
					shift = 15;
					index += 1; // 5 planes
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

	protected void bayer32() {
		final int[] work = Gfx.copy2Int(pixels);
		bitplanes = new int[(screenWidth >> 4) * screenHeight][5]; // 5 planes

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

				final int color = Gfx.getColorIndex(colorAlg, pictureColors, r0, g0, b0);
				final int c[] = pictureColors[color];

				final int r = c[0];
				final int g = c[1];
				final int b = c[2];

				pixels[pyx] = (byte) r;
				pixels[pyx + 1] = (byte) g;
				pixels[pyx + 2] = (byte) b;

				final int value = color & 0xff;

				bitplanes[index][4] |= ((value & 16) >> 4) << shift;
				bitplanes[index][3] |= ((value & 8) >> 3) << shift;
				bitplanes[index][2] |= ((value & 4) >> 2) << shift;
				bitplanes[index][1] |= ((value & 2) >> 1) << shift;
				bitplanes[index][0] |= (value & 1) << shift;

				if (shift == 0) {
					shift = 15;
					index += 1; // 5 planes
				} else
					shift--;
			}
		}
	}

	protected void ham6Encoded() {
		bayerDithering();
		
		final float[] work = Gfx.copy2float(pixels);
		bitplanes = new int[(screenWidth >> 4) * screenHeight][6]; // 6 planes

		int r0, g0, b0, r = 0, g = 0, b = 0;
		final int width3 = screenWidth * 3;

		int index = 0, shift = 15; // WORD
		int modifyRed, modifyBlue;

		switch (image.getType()) {
		case BufferedImage.TYPE_3BYTE_BGR:
			modifyRed = 0b010000;
			modifyBlue = 0b100000;
			break;
		default:
			modifyRed = 0b100000;
			modifyBlue = 0b010000;
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
				int action = Gfx.getColorIndex(colorAlg, pictureColors, r0, g0, b0); // 16 color palette
				final int pc[] = pictureColors[action];

				if (nextPixel) { // its not first pixel in a row so use best matching color
					// distance to palette match
					final float dpc = Gfx.getDistance(colorAlg, r0, g0, b0, pc[0], pc[1], pc[2]);

					float min_r = Float.MAX_VALUE; // minimum red
					float min_g = min_r;
					float min_b = min_r;

					int ri = -1;
					int gi = -1;
					int bi = -1;

					// calculate all color change possibilities and measure distances
					for (int i = 0; i < 16; i++) {
						// scaled color
						final int scaled = i | (i << 4);

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
					if (ham <= dpc) {
						// HAM is best or equal, alter color
						if (ham == min_r) {
							// red
							r = ri;
							action = modifyRed | (ri >> 4);
						} else if (ham == min_g) {
							// green
							g = gi;
							action = 0b110000 | (gi >> 4);
						} else if (ham == min_b) {
							// blue
							b = bi;
							action = modifyBlue | (bi >> 4);
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

				if (config.dither_alg != DITHERING.NONE) {
					final float r_error = r0 - r;
					final float g_error = g0 - g;
					final float b_error = b0 - b;

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
							work[pyx + 3] += r_error / 8;
							work[pyx + 3 + 1] += g_error / 8;
							work[pyx + 3 + 2] += b_error / 8;

							if (x < (screenWidth - 2) * 3) {
								work[pyx + 6] += r_error / 8;
								work[pyx + 6 + 1] += g_error / 8;
								work[pyx + 6 + 2] += b_error / 8;
							}
						}
						if (y < screenHeight - 1) {
							work[py1x - 3] += r_error / 8;
							work[py1x - 3 + 1] += g_error / 8;
							work[py1x - 3 + 2] += b_error / 8;

							work[py1x] += r_error / 8;
							work[py1x + 1] += g_error / 8;
							work[py1x + 2] += b_error / 8;

							if (x < (screenWidth - 1) * 3) {
								work[py1x + 3] += r_error / 8;
								work[py1x + 3 + 1] += g_error / 8;
								work[py1x + 3 + 2] += b_error / 8;
							}

							if (y < screenHeight - 2) {
								work[py2x] += r_error / 8;
								work[py2x + 1] += g_error / 8;
								work[py2x + 2] += b_error / 8;
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
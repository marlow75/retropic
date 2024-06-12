package pl.dido.image.c64;

import java.awt.image.BufferedImage;
import java.util.Arrays;

import pl.dido.image.renderer.AbstractRenderer;
import pl.dido.image.utils.C64PaletteCalculator;
import pl.dido.image.utils.Config.DITHERING;
import pl.dido.image.utils.Gfx;
import pl.dido.image.utils.neural.SOMPalette;

public class C64ExtraRenderer extends AbstractRenderer {

	protected final static int maxPosition = 320 * 200 * 3;

	protected final int bitmap1[] = new int[40 * 200];
	protected final int screen1[] = new int[1000];

	protected final int bitmap2[] = new int[40 * 200];
	protected final int screen2[] = new int[1000];

	protected final int nibbles[] = new int[1000];
	protected final int lumas[];

	protected int blend[][];
	protected int machinePalette[][];

	protected int backgroundColor;
	protected int lumaThreshold;

	public C64ExtraRenderer(final BufferedImage image, final C64ExtraConfig config) {
		super(image, config);

		lumas = C64PaletteCalculator.lumas;
		lumaThreshold = ((C64ExtraConfig) config).luma_threshold;
	}

	@Override
	protected void setupPalette() {
		palette = new int[136][3];
		blend = new int[136][2];

		machinePalette = C64PaletteCalculator.getCalculatedPalette();
		int index = 0;

		// get mixed colors
		for (int i = 0; i < machinePalette.length; i++) {
			final float l1 = lumas[i];

			for (int j = i; j < machinePalette.length; j++) {
				final float l2 = lumas[j];

				if (Math.abs(l2 - l1) <= lumaThreshold) {
					final int color1[] = machinePalette[i];
					final int color2[] = machinePalette[j];

					final int color[] = palette[index];

					color[0] = (int) ((color1[0] + color2[0]) / 2);
					color[1] = (int) ((color1[1] + color2[1]) / 2);
					color[2] = (int) ((color1[2] + color2[2]) / 2);

					// save colors
					blend[index][0] = i;
					blend[index][1] = j;

					index++;
				}
			}
		}

		// shrink palette to actual size
		palette = Arrays.copyOf(palette, index);
		blend = Arrays.copyOf(blend, index);
	}

	@Override
	protected void imagePostproces() {
		switch (((C64ExtraConfig) config).extra_mode) {
		case HIRES_INTERLACED:
			hiresInterlaced();
			break;
		case MULTI_COLOR_INTERLACED:
			multiColorInterlaced();
			verifyMCI();
			break;
		}
	}

	@Override
	protected void imageDithering() {
		if (config.dither_alg == DITHERING.BAYER)
			Gfx.bayer8x8(pixels, palette, colorAlg, screenWidth, screenHeight,
					((C64ExtraConfig) config).error_threshold);
		else
			super.imageDithering();
	}

	protected int getBlendedColorIndex(final int tilePalette[][], final int tileColors[], final int r, final int g,
			final int b, final int prevColorIndex) {

		int index = 0;
		float min = Float.MAX_VALUE;

		final float r0 = machinePalette[prevColorIndex][0];
		final float g0 = machinePalette[prevColorIndex][1];
		final float b0 = machinePalette[prevColorIndex][2];

		for (int i = 0; i < tilePalette.length; i++) {
			final int color = tileColors[i];
			final int c[] = machinePalette[color];

			final int r1 = (int) ((c[0] + r0) / 2);
			final int g1 = (int) ((c[1] + g0) / 2);
			final int b1 = (int) ((c[2] + b0) / 2);

			final float distance = Gfx.perceptedDistance(r, g, b, r1, g1, b1);

			if (distance < min) {
				min = distance;
				index = i;
			}
		}

		return index;
	}

	protected void hiresInterlaced() {
		final int work[] = Gfx.copy2Int(pixels);
		int r, g, b, bitmapIndex = 0;

		int f = 0, n = 0;
		int r_error, g_error, b_error;

		// get average color
		final int tilePalette[][] = new int[3][3];

		for (int y = 0; y < 200; y += 8) {
			final int p = y * 320 * 3;

			for (int x = 0; x < 320; x += 8) {
				final int offset = p + x * 3;

				final int data[] = new int[64 * 3];
				int index = 0;

				// 8x8 tile data
				for (int y0 = 0; y0 < 8; y0++) {
					final int k = offset + y0 * 320 * 3;

					for (int x0 = 0; x0 < 24; x0 += 3) {
						final int position = k + x0;

						data[index++] = work[position];
						data[index++] = work[position + 1];
						data[index++] = work[position + 2];
					}
				}

				final int colors[];

				switch (((C64ExtraConfig) config).rgb_approximation) {
				case CUBE:
					colors = Gfx.getRGBLinearColor(colorAlg, data, machinePalette);
					break;
				default:
					colors = Gfx.getRGBLinearColor(colorAlg, data, machinePalette);
					break;
				}

				f = colors[0];
				n = colors[1];

				final int cf[] = machinePalette[f];
				int cn[] = machinePalette[n];

				r = (int) ((cf[0] + cn[0]) / 2);
				g = (int) ((cf[1] + cn[1]) / 2);
				b = (int) ((cf[2] + cn[2]) / 2);

				tilePalette[0] = palette[getColorIndex(r, g, b)]; // blended color
				tilePalette[1] = cf;
				tilePalette[2] = cn;

				final int address = (y >> 3) * 40 + (x >> 3);

				screen1[address] = ((f & 0xf) << 4) | (n & 0xf); // f
				screen2[address] = ((n & 0xf) << 4) | (f & 0xf);

				int value1 = 0, value2 = 0, bitcount = 0;

				for (int y0 = 0; y0 < 8; y0++) {
					final int k = offset + y0 * 320 * 3;

					for (int x0 = 0; x0 < 24; x0 += 3) {
						final int position = k + x0;

						r = work[position];
						g = work[position + 1];
						b = work[position + 2];

						final int color = Gfx.getColorIndex(colorAlg, tilePalette, Gfx.saturate(r), Gfx.saturate(g),
								Gfx.saturate(b));
						cn = tilePalette[color];

						final int nr = cn[0];
						final int ng = cn[1];
						final int nb = cn[2];

						int v1 = 0, v2 = 0;
						switch (color) {
						case 0:
							if (y0 % 2 == 0) {
								v1 = 0;
								v2 = 0;
							} else {
								v1 = 1;
								v2 = 1;
							}
							break;
						case 1:
							v1 = 1;
							v2 = 0;
							break;
						case 2:
							v1 = 0;
							v2 = 1;
							break;
						}

						value1 = (value1 << 1) | v1;
						value2 = (value2 << 1) | v2;

						if (bitcount % 8 == 7) {
							bitmap1[bitmapIndex] = value1;
							bitmap2[bitmapIndex] = value2;

							value1 = value2 = 0;
							bitmapIndex++;
						}

						bitcount += 1;

						pixels[position] = (byte) nr;
						pixels[position + 1] = (byte) ng;
						pixels[position + 2] = (byte) nb;

						work[position] = nr;
						work[position + 1] = ng;
						work[position + 2] = nb;

						if (config.dither_alg == DITHERING.ATKINSON || config.dither_alg == DITHERING.FLOYDS) {
							final int maxError = (255 * ((C64ExtraConfig) config).error_threshold) / 20;

							r_error = Gfx.saturate(r - nr, maxError);
							g_error = Gfx.saturate(g - ng, maxError);
							b_error = Gfx.saturate(b - nb, maxError);

							final int position1 = position + 320 * 3;
							final int position2 = position + 320 * 6;

							switch (config.dither_alg) {
							case FLOYDS:
								if (x + x0 < 316) {
									work[position + 3] += (r_error * 7) / 16;
									work[position + 3 + 1] += (g_error * 7) / 16;
									work[position + 3 + 2] += (b_error * 7) / 16;
								}

								if (y + y0 < 199) {
									work[position1 - 3] += (r_error * 3) / 16;
									work[position1 - 3 + 1] += (g_error * 3) / 16;
									work[position1 - 3 + 2] += (b_error * 3) / 16;

									work[position1] += (r_error * 5) / 16;
									work[position1 + 1] += (g_error * 5) / 16;
									work[position1 + 2] += (b_error * 5) / 16;

									if (x + x0 < 316) {
										work[position1 + 3] += r_error / 16;
										work[position1 + 3 + 1] += g_error / 16;
										work[position1 + 3 + 2] += b_error / 16;
									}
								}
								break;
							case ATKINSON:
								if (x + x0 < 316) {
									work[position + 3] += r_error / 8;
									work[position + 3 + 1] += g_error / 8;
									work[position + 3 + 2] += b_error / 8;

									if (x + x0 < 313) {
										work[position + 6] += r_error / 8;
										work[position + 6 + 1] += g_error / 8;
										work[position + 6 + 2] += b_error / 8;
									}
								}
								if (y + y0 < 199) {
									work[position1 - 3] += r_error / 8;
									work[position1 - 3 + 1] += g_error / 8;
									work[position1 - 3 + 2] += b_error / 8;

									work[position1] += r_error / 8;
									work[position1 + 1] += g_error / 8;
									work[position1 + 2] += b_error / 8;

									if (x + x0 < 316) {
										work[position1 + 3] += r_error / 8;
										work[position1 + 3 + 1] += g_error / 8;
										work[position1 + 3 + 2] += b_error / 8;
									}

									if (y + y0 < 198) {
										work[position2] += r_error / 8;
										work[position2 + 1] += g_error / 8;
										work[position2 + 2] += b_error / 8;
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
	}

	private static final void safeAdd(final int[] work, final int i, final int value) {
		if (i < maxPosition)
			work[i] += value;
	}

	protected void verifyMCI() {
		int r, g, b, r1, g1, b1;
		int bitmapIndex = 0;

		for (int y = 0; y < 200; y += 8) {
			final int p = y * 320 * 3;

			final int prevColorIndex[] = new int[8];
			Arrays.fill(prevColorIndex, -1);

			for (int x = 0; x < 320; x += 8) {
				final int offset = p + x * 3;
				final int address = (y >> 3) * 40 + (x >> 3);

				final int tileColors[] = new int[] { backgroundColor, screen1[address] >> 4, screen1[address] & 0xf,
						nibbles[address] & 0xf };

				int even = 0;

				// 8x8 tile work copy
				for (int y0 = 0; y0 < 8; y0++) {
					int bitcount = 6;

					for (int x0 = 0; x0 < 24; x0 += 3) {
						final int colorIndex;

						if (even == 0)
							colorIndex = (bitmap1[bitmapIndex] >> bitcount) & 0b11;
						else {
							colorIndex = (bitmap2[bitmapIndex] >> bitcount) & 0b11;
							bitcount -= 2;
						}

						even ^= 1;

						r = machinePalette[tileColors[colorIndex]][0];
						g = machinePalette[tileColors[colorIndex]][1];
						b = machinePalette[tileColors[colorIndex]][2];

						if (prevColorIndex[y0] != -1) {

							r1 = machinePalette[prevColorIndex[y0]][0];
							g1 = machinePalette[prevColorIndex[y0]][1];
							b1 = machinePalette[prevColorIndex[y0]][2];

							r = (int) ((r + r1) / 2);
							g = (int) ((g + g1) / 2);
							b = (int) ((b + b1) / 2);
						}

						prevColorIndex[y0] = tileColors[colorIndex];
						final int position = offset + y0 * 320 * 3 + x0;

						pixels[position] = (byte) r;
						pixels[position + 1] = (byte) g;
						pixels[position + 2] = (byte) b;
					}

					bitmapIndex++;
				}
			}
		}
	}

	protected void multiColorInterlaced() {
		int bitmapIndex = 0;
		final int work[] = Gfx.copy2Int(pixels);

		int r = 0, g = 0, b = 0;
		int r_error, g_error, b_error;

		// calculate occurrence of colors
		for (int y = 0; y < 200; y++) {
			final int k = y * 320 * 3;

			for (int x = 0; x < 320; x++) {
				final int position = k + x * 3;

				r += work[position];
				g += work[position + 1];
				b += work[position + 2];
			}
		}

		r /= 320 * 200;
		g /= 320 * 200;
		b /= 320 * 200;

		backgroundColor = Gfx.getColorIndex(colorAlg, machinePalette, r, g, b);

		final int br = machinePalette[backgroundColor][0];
		final int bg = machinePalette[backgroundColor][1];
		final int bb = machinePalette[backgroundColor][2];

		final byte trainData1[] = new byte[64 * 3];
		final byte trainData2[] = new byte[32 * 3];

		for (int y = 0; y < 200; y += 8) {
			final int p = y * 320 * 3;

			final int prevColorIndex[] = new int[8];
			Arrays.fill(prevColorIndex, -1);

			for (int x = 0; x < 320; x += 8) {
				final int offset = p + x * 3;
				int index = 0, colorIndex;

				// get most fitted tile colors
				for (int y0 = 0; y0 < 8; y0++) {
					final int k = offset + y0 * 320 * 3;

					for (int x0 = 0; x0 < 24; x0 += 3) {
						final int position = k + x0;

						trainData1[index++] = pixels[position];
						trainData1[index++] = pixels[position + 1];
						trainData1[index++] = pixels[position + 2];
					}
				}

				// map all 4 colors to extra palette
				SOMPalette som = new SOMPalette(4, 4, 0.8f, 1f, 100);
				int tilePalette[][] = som.train(trainData1);

				// get blend colors
				for (int i = 0; i < 16; i++) {
					final int c[] = tilePalette[i];
					colorIndex = getColorIndex(c[0], c[1], c[2]);

					int q[] = machinePalette[blend[colorIndex][0]];

					trainData2[6 * i] = (byte) q[0];
					trainData2[6 * i + 1] = (byte) q[1];
					trainData2[6 * i + 2] = (byte) q[2];

					q = machinePalette[blend[colorIndex][1]];

					trainData2[6 * i + 3] = (byte) q[0];
					trainData2[6 * i + 4] = (byte) q[1];
					trainData2[6 * i + 5] = (byte) q[2];
				}

				// get machine colors
				som = new SOMPalette(2, 2, 0.8f, 1f, 100);
				tilePalette = som.train(trainData2);

				final int tileColors[] = new int[4];

				for (int i = 0; i < 4; i++) {
					final int c[] = tilePalette[i];
					colorIndex = Gfx.getColorIndex(colorAlg, machinePalette, c[0], c[1], c[2]);

					c[0] = machinePalette[colorIndex][0];
					c[1] = machinePalette[colorIndex][1];
					c[2] = machinePalette[colorIndex][2];

					tileColors[i] = colorIndex;
				}

				// map background color to the nearest color tile palette
				colorIndex = Gfx.getColorIndex(colorAlg, tilePalette, br, bg, bb);
				if (colorIndex != 0) {
					tilePalette[colorIndex][0] = tilePalette[0][0]; // first entry = background color
					tilePalette[colorIndex][1] = tilePalette[0][1];
					tilePalette[colorIndex][2] = tilePalette[0][2];

					tilePalette[0][0] = br; // background color
					tilePalette[0][1] = bg;
					tilePalette[0][2] = bb;

					tileColors[colorIndex] = tileColors[0];
					tileColors[0] = backgroundColor;
				}

				final int address = (y >> 3) * 40 + (x >> 3);

				// map tile colors to screen palette
				final int c01 = tileColors[1] << 4;
				final int c10 = tileColors[2];
				final int c11 = tileColors[3];

				screen1[address] = c01 | c10;
				nibbles[address] = c11;

				int even = 0, value1 = 0, value2 = 0, bitcount = 0;

				for (int y0 = 0; y0 < 8; y0++) {
					final int k = offset + y0 * 320 * 3;

					for (int x0 = 0; x0 < 24; x0 += 3) {
						final int position = k + x0;

						// get current picture color + noise
						r = work[position] + (int) (Math.random() * 100 - 50);
						g = work[position + 1] + (int) (Math.random() * 100 - 50);
						b = work[position + 2] + (int) (Math.random() * 100 - 50);

						int nr, ng, nb;

						if (prevColorIndex[y0] != -1) { // prevColorIndex is absolute, not local tilePalette index
							colorIndex = getBlendedColorIndex(tilePalette, tileColors, r, g, b, prevColorIndex[y0]);

							// new color as old and new combined
							final int p1[] = tilePalette[colorIndex];
							final int p2[] = machinePalette[prevColorIndex[y0]];

							nr = (p1[0] + p2[0]) / 2;
							ng = (p1[1] + p2[1]) / 2;
							nb = (p1[2] + p2[2]) / 2;
						} else {
							colorIndex = Gfx.getColorIndex(colorAlg, tilePalette, r, g, b);
							final int p1[] = tilePalette[colorIndex];

							nr = p1[0];
							ng = p1[1];
							nb = p1[2];
						}

						prevColorIndex[y0] = tileColors[colorIndex];

						if (even == 0)
							value1 = (value1 << 2) | (colorIndex & 0b11);
						else
							value2 = (value2 << 2) | (colorIndex & 0b11);

						even ^= 1;
						bitcount += 1;

						if (bitcount == 8) {
							bitmap1[bitmapIndex] = value1;
							bitmap2[bitmapIndex] = value2;

							value1 = value2 = 0;
							bitcount = 0;

							bitmapIndex++;
						}

						pixels[position] = (byte) nr;
						pixels[position + 1] = (byte) ng;
						pixels[position + 2] = (byte) nb;

						work[position] = nr;
						work[position + 1] = ng;
						work[position + 2] = nb;

						if (config.dither_alg == DITHERING.ATKINSON || config.dither_alg == DITHERING.FLOYDS) {
							// calculate color error
							final int maxError = (255 * ((C64ExtraConfig) config).error_threshold) / 20;

							r_error = Gfx.saturate(r - nr, maxError);
							g_error = Gfx.saturate(g - ng, maxError);
							b_error = Gfx.saturate(b - nb, maxError);

							final int position1 = position + 320 * 3;
							final int position2 = position + 320 * 6;

							switch (config.dither_alg) {
							case FLOYDS:
								safeAdd(work, position + 3, (r_error * 7) / 16);
								safeAdd(work, position + 3 + 1, (g_error * 7) / 16);
								safeAdd(work, position + 3 + 2, (b_error * 7) / 16);

								safeAdd(work, position1 - 3, (r_error * 3) / 16);
								safeAdd(work, position1 - 3 + 1, (g_error * 3) / 16);
								safeAdd(work, position1 - 3 + 2, (b_error * 3) / 16);

								safeAdd(work, position1, (r_error * 5) / 16);
								safeAdd(work, position1 + 1, (g_error * 5) / 16);
								safeAdd(work, position1 + 2, (b_error * 5) / 16);

								safeAdd(work, position1 + 3, r_error / 16);
								safeAdd(work, position1 + 3 + 1, g_error / 16);
								safeAdd(work, position1 + 3 + 2, b_error / 16);

								break;
							case ATKINSON:
								safeAdd(work, position + 3, r_error / 8);
								safeAdd(work, position + 3 + 1, g_error / 8);
								safeAdd(work, position + 3 + 2, b_error / 8);

								safeAdd(work, position + 6, r_error / 8);
								safeAdd(work, position + 6 + 1, g_error / 8);
								safeAdd(work, position + 6 + 2, b_error / 8);

								safeAdd(work, position1 - 3, r_error / 8);
								safeAdd(work, position1 - 3 + 1, g_error / 8);
								safeAdd(work, position1 - 3 + 2, b_error / 8);

								safeAdd(work, position1, r_error / 8);
								safeAdd(work, position1 + 1, g_error / 8);
								safeAdd(work, position1 + 2, b_error / 8);

								safeAdd(work, position1 + 3, r_error / 8);
								safeAdd(work, position1 + 3 + 1, g_error / 8);
								safeAdd(work, position1 + 3 + 2, b_error / 8);

								safeAdd(work, position2, r_error / 8);
								safeAdd(work, position2 + 1, g_error / 8);
								safeAdd(work, position2 + 2, b_error / 8);

								break;
							default:
								break;
							}
						}
					}
				}
			}
		}
	}
}
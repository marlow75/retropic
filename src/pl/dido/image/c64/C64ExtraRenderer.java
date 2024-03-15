package pl.dido.image.c64;

import java.awt.image.BufferedImage;
import java.util.Arrays;

import pl.dido.image.renderer.AbstractRenderer;
import pl.dido.image.utils.C64PaletteCalculator;
import pl.dido.image.utils.Gfx;
import pl.dido.image.utils.neural.SOMPalette;

public class C64ExtraRenderer extends AbstractRenderer {

	protected final int bitmap1[] = new int[40 * 200];
	protected final int screen1[] = new int[1000];

	protected final int bitmap2[] = new int[40 * 200];
	protected final int screen2[] = new int[1000];

	protected final int nibbles[] = new int[1000];
	protected final int lumas[];
	
	protected final float alphas[] = new float[16];

	protected int blend[][];
	protected int machinePalette[][];

	protected int backgroundColor;
	protected int lumaThreshold;

	public C64ExtraRenderer(final BufferedImage image, final C64ExtraConfig config) {
		super(image, config);

		lumas = C64PaletteCalculator.lumas;
		
		for (int i = 0; i < 16; i++)
			alphas[i] = 15 + Math.abs(15 - lumas[i]);

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
					final float a1 = alphas[i];
					final float a2 = alphas[j];
					final float sum = a1 + a2;
					
					final int color1[] = machinePalette[i];
					final int color2[] = machinePalette[j];
					
					final int color[] = palette[index];
					
					color[0] = (int) ((color1[0] * a1 + color2[0] * a2) / sum);
					color[1] = (int) ((color1[1] * a1 + color2[1] * a2) / sum);
					color[2] = (int) ((color1[2] * a1 + color2[2] * a2) / sum);

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

	protected int getBlendedColorIndex(final int tilePalette[][], final int tileColors[], final int r, final int g,
			final int b, final int prevColorIndex) {

		int index = 0;
		float min = Float.MAX_VALUE;

		final float a0 = alphas[prevColorIndex];

		final float a0r0 = a0 * machinePalette[prevColorIndex][0];
		final float a0g0 = a0 * machinePalette[prevColorIndex][1];
		final float a0b0 = a0 * machinePalette[prevColorIndex][2];

		for (int i = 0; i < tilePalette.length; i++) {
			final int color = tileColors[i];
			final int c[] = machinePalette[color];

			final float a1 = alphas[color];
			final float suma0a1 = a0 + a1;

			final int r1 = (int) ((c[0] * a1 + a0r0) / suma0a1);
			final int g1 = (int) ((c[1] * a1 + a0g0) / suma0a1);
			final int b1 = (int) ((c[2] * a1 + a0b0) / suma0a1);

			final float distance = Gfx.perceptedDistance(r, g, b, r1, g1, b1);

			if (distance < min) {
				min = distance;
				index = i;
			}
		}

		return index;
	}

	protected void hiresInterlaced() {
		final int work[] = new int[64 * 3];
		int bitmapIndex = 0, r, g, b;

		for (int y = 0; y < 200; y += 8) {
			final int p = y * 320 * 3;

			for (int x = 0; x < 320; x += 8) {
				final int offset = p + x * 3;
				int index = 0, f = 0, n = 0;

				float max = Float.MIN_VALUE;
				float min = Float.MAX_VALUE;

				// 8x8 tile
				for (int y0 = 0; y0 < 8; y0++) {
					for (int x0 = 0; x0 < 24; x0 += 3) {
						final int position = offset + y0 * 320 * 3 + x0;

						r = pixels[position] & 0xff;
						g = pixels[position + 1] & 0xff;
						b = pixels[position + 2] & 0xff;

						work[index++] = r;
						work[index++] = g;
						work[index++] = b;

						final float luma = Gfx.getLuma(r, g, b);
						if (luma > max) {
							max = luma;
							f = Gfx.getColorIndex(colorAlg, palette, r, g, b);
						}

						if (luma < min) {
							min = luma;
							n = Gfx.getColorIndex(colorAlg, palette, r, g, b);
						}
					}
				}

				final int tilePalette[][];
				if (Math.abs(max - min) <= (lumaThreshold * 255) / 32) {
					// 4 colors palette (blended new colors)
					tilePalette = new int[4][3];
					
					final int if0 = blend[f][0];
					final int in1 = blend[n][1];
					
					final int if1 = blend[f][1];
					final int in0 = blend[n][0];
					
					final int cf0[] = machinePalette[if0];
					final int cn1[] = machinePalette[in1];
					
					final float acf0 = alphas[if0];
					final float acn1 = alphas[in1];
					
					final float sum1 = acf0 + acn1;

					// calculate blended colors
					r = (int) ((cf0[0] * acf0 + cn1[0] * acn1) / sum1);
					g = (int) ((cf0[1] * acf0 + cn1[1] * acn1) / sum1);
					b = (int) ((cf0[2] * acf0 + cn1[2] * acn1) / sum1);
				
					final int i2 = Gfx.getColorIndex(colorAlg, palette, r, g, b);
					
					final int cf1[] = machinePalette[if1];
					final int cn0[] = machinePalette[in0];

					final float acn0 = alphas[in0];
					final float acf1 = alphas[if1];
					
					final float sum2 = acf1 + acn0;

					r = (int) ((cf1[0] * acf1 + cn0[0] * acn0) / sum2);
					g = (int) ((cf1[1] * acf1 + cn0[1] * acn0) / sum2);
					b = (int) ((cf1[2] * acf1 + cn0[2] * acn0) / sum2);
					
					final int i3 = Gfx.getColorIndex(colorAlg, palette, r, g, b);

					tilePalette[2] = palette[i2];
					tilePalette[3] = palette[i3];
				} else
					// 2 colors palette (no blended color)
					tilePalette = new int[2][3];

				tilePalette[0] = palette[n]; // first blended color
				tilePalette[1] = palette[f]; // second blended color

				final int address = (y >> 3) * 40 + (x >> 3);

				screen1[address] = ((blend[f][0] & 0xf) << 4) | (blend[n][0] & 0xf); // f & b
				screen2[address] = ((blend[f][1] & 0xf) << 4) | (blend[n][1] & 0xf);

				int value1 = 0, value2 = 0, bitcount = 0;
				for (int y0 = 0; y0 < 8; y0++) {
					final int k1 = (y0 + 1) * 24;
					final int k2 = (y0 + 2) * 24;

					for (int x0 = 0; x0 < 24; x0 += 3) {
						final int pyx0 = y0 * 24 + x0;
						final int py1x0 = k1 + x0;
						final int py2x0 = k2 + x0;

						r = Gfx.saturate(work[pyx0]);
						g = Gfx.saturate(work[pyx0 + 1]);
						b = Gfx.saturate(work[pyx0 + 2]);

						final int color = Gfx.getColorIndex(colorAlg, tilePalette, r, g, b);
						final int cn[] = tilePalette[color];

						final int nr = cn[0];
						final int ng = cn[1];
						final int nb = cn[2];

						int v1 = 0, v2 = 0;
						switch (color) {
						case 0:
							v1 = 0;
							v2 = 0;
							break;
						case 1:
							v1 = 1;
							v2 = 1;
							break;
						case 2:
							v1 = 1;
							v2 = 0;
							break;
						case 3:
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
						final int position = offset + y0 * 320 * 3 + x0;

						pixels[position] = (byte) nr;
						pixels[position + 1] = (byte) ng;
						pixels[position + 2] = (byte) nb;

						if (config.dithering) {
							final int r_error = r - nr;
							final int g_error = g - ng;
							final int b_error = b - nb;

							switch (config.dither_alg) {
							case STD_FS:
								if (x0 < 21) {
									work[pyx0 + 3] += (r_error * 7) / 16;
									work[pyx0 + 3 + 1] += (g_error * 7) / 16;
									work[pyx0 + 3 + 2] += (b_error * 7) / 16;
								}

								if (y0 < 7) {
									work[py1x0 - 3] += (r_error * 3) / 16;
									work[py1x0 - 3 + 1] += (g_error * 3) / 16;
									work[py1x0 - 3 + 2] += (b_error * 3) / 16;

									work[py1x0] += (r_error * 5) / 16;
									work[py1x0 + 1] += (g_error * 5) / 16;
									work[py1x0 + 2] += (b_error * 5) / 16;

									if (x0 < 21) {
										work[py1x0 + 3] += r_error / 16;
										work[py1x0 + 3 + 1] += g_error / 16;
										work[py1x0 + 3 + 2] += b_error / 16;
									}
								}
								break;
							case ATKINSON:
								if (x0 < 21) {
									work[pyx0 + 3] += r_error >> 3;
									work[pyx0 + 3 + 1] += g_error >> 3;
									work[pyx0 + 3 + 2] += b_error >> 3;

									if (x0 < 18) {
										work[pyx0 + 6] += r_error >> 3;
										work[pyx0 + 6 + 1] += g_error >> 3;
										work[pyx0 + 6 + 2] += b_error >> 3;
									}
								}
								if (y0 < 7) {
									work[py1x0 - 3] += r_error >> 3;
									work[py1x0 - 3 + 1] += g_error >> 3;
									work[py1x0 - 3 + 2] += b_error >> 3;

									work[py1x0] += r_error >> 3;
									work[py1x0 + 1] += g_error >> 3;
									work[py1x0 + 2] += b_error >> 3;

									if (x0 < 21) {
										work[py1x0 + 3] += r_error >> 3;
										work[py1x0 + 3 + 1] += g_error >> 3;
										work[py1x0 + 3 + 2] += b_error >> 3;
									}

									if (y0 < 6) {
										work[py2x0] += r_error >> 3;
										work[py2x0 + 1] += g_error >> 3;
										work[py2x0 + 2] += b_error >> 3;
									}
								}

								break;
							}
						}
					}
				}
			}
		}
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
				
				final int tileColors[] = new int[] {
					backgroundColor, screen1[address] >> 4, screen1[address] & 0xf, nibbles[address] & 0xf  
				};

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
						
						final float l1 = alphas[tileColors[colorIndex]];
						
						if (prevColorIndex[y0] != -1) {
						
							r1 = machinePalette[prevColorIndex[y0]][0];
							g1 = machinePalette[prevColorIndex[y0]][1];
							b1 = machinePalette[prevColorIndex[y0]][2];
							
							final float l2 = alphas[prevColorIndex[y0]];
							final float sum = l1 + l2;
							
							r = (int) ((r * l1 + r1 * l2) / sum);
							g = (int) ((g * l1 + g1 * l2) / sum);
							b = (int) ((b * l1 + b1 * l2) / sum);
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
		int r = 0, g = 0, b = 0;
		final int work[] = new int[64 * 3];

		int bitmapIndex = 0;

		// calculate occurrence of colors
		for (int y = 0; y < 200; y++) {
			final int p = y * 320 * 3;

			for (int x = 0; x < 320; x++) {
				final int position = p + x * 3;

				r += pixels[position] & 0xff;
				g += pixels[position + 1] & 0xff;
				b += pixels[position + 2] & 0xff;
			}
		}

		r /= 320 * 200;
		g /= 320 * 200;
		b /= 320 * 200;
		
		backgroundColor = Gfx.getColorIndex(colorAlg, machinePalette, r, g, b);

		final int br = machinePalette[backgroundColor][0];
		final int bg = machinePalette[backgroundColor][1];
		final int bb = machinePalette[backgroundColor][2];

		for (int y = 0; y < 200; y += 8) {
			final int p = y * 320 * 3;

			final int prevColorIndex[] = new int[8];
			Arrays.fill(prevColorIndex, -1);

			for (int x = 0; x < 320; x += 8) {	
				final int offset = p + x * 3;

				int index = 0;
				int colorIndex;

				// 8x8 tile work copy
				for (int y0 = 0; y0 < 8; y0++) {
					for (int x0 = 0; x0 < 24; x0 += 3) {
						final int position = offset + y0 * 320 * 3 + x0;

						work[index++] = pixels[position] & 0xff;
						work[index++] = pixels[position + 1] & 0xff;
						work[index++] = pixels[position + 2] & 0xff;
					}
				}

				// get most fitted tile colors
				byte trainData[] = new byte[64 * 3];

				for (int i = 0; i < 64 * 3; i++)
					trainData[i] = (byte) work[i];

				// map all 4 colors to extra palette
				SOMPalette som = new SOMPalette(4, 4, 0.8f, 1f, 100);
				int tilePalette[][] = som.train(trainData);

				// get blend colors
				trainData = new byte[32 * 3];
				for (int i = 0; i < 16; i++) {
					final int c[] = tilePalette[i];
					colorIndex = getColorIndex(c[0], c[1], c[2]);

					int q[] = machinePalette[blend[colorIndex][0]];

					trainData[6 * i] = (byte) q[0];
					trainData[6 * i + 1] = (byte) q[1];
					trainData[6 * i + 2] = (byte) q[2];

					q = machinePalette[blend[colorIndex][1]];

					trainData[6 * i + 3] = (byte) q[0];
					trainData[6 * i + 4] = (byte) q[1];
					trainData[6 * i + 5] = (byte) q[2];
				}

				// get machine colors
				som = new SOMPalette(2, 2, 0.8f, 1f, 100);
				tilePalette = som.train(trainData);
				
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
					final int k = y0 + 1;

					for (int x0 = 0; x0 < 24; x0 += 3) {
						final int pyx0 = y0 * 24 + x0;
						final int py1x0 = k * 24 + x0;

						// get current picture color
						r = Gfx.saturate(work[pyx0]);
						g = Gfx.saturate(work[pyx0 + 1]);
						b = Gfx.saturate(work[pyx0 + 2]);

						int nr, ng, nb;

						if (prevColorIndex[y0] != -1) { // prevColorIndex is absolute, not local tilePalette index
							final float a0 = alphas[prevColorIndex[y0]];
							colorIndex = getBlendedColorIndex(tilePalette, tileColors, r, g, b, prevColorIndex[y0]);

							final float a1 = alphas[tileColors[colorIndex]];
							final float sum = a0 + a1;

							// new color as old and new combined
							nr = (int) ((tilePalette[colorIndex][0] * a1 + machinePalette[prevColorIndex[y0]][0] * a0) / sum);
							ng = (int) ((tilePalette[colorIndex][1] * a1 + machinePalette[prevColorIndex[y0]][1] * a0) / sum);
							nb = (int) ((tilePalette[colorIndex][2] * a1 + machinePalette[prevColorIndex[y0]][2] * a0) / sum);
						} else {
							colorIndex = Gfx.getColorIndex(colorAlg, tilePalette, r, g, b);

							nr = tilePalette[colorIndex][0];
							ng = tilePalette[colorIndex][1];
							nb = tilePalette[colorIndex][2];
						}

						prevColorIndex[y0] = tileColors[colorIndex];

						if (even == 0)
							value1 = (value1 << 2) | (colorIndex & 0b11);
						else
							value2 = (value2 << 2) | (colorIndex & 0b11);

						bitcount += 1;
						even ^= 1;

						if (bitcount == 8) {
							bitmap1[bitmapIndex] = value1;
							bitmap2[bitmapIndex] = value2;

							value1 = value2 = 0;
							bitcount = 0;

							bitmapIndex++;
						}

						final int position = offset + y0 * 320 * 3 + x0;

						pixels[position] = (byte) nr;
						pixels[position + 1] = (byte) ng;
						pixels[position + 2] = (byte) nb;

						// calculate color error
						final int dr = r - nr;
						final int dg = g - ng;
						final int db = b - nb;

						// distribute error
						if (x0 < 21) {
							work[pyx0 + 3] += (dr * 7) / 16;
							work[pyx0 + 3 + 1] += (dg * 7) / 16;
							work[pyx0 + 3 + 2] += (db * 7) / 16;
						}

						if (y0 < 7) {
							work[py1x0 - 3] += (dr * 3) / 16;
							work[py1x0 - 3 + 1] += (dg * 3) / 16;
							work[py1x0 - 3 + 2] += (db * 3) / 16;

							work[py1x0] += (dr * 5) / 16;
							work[py1x0 + 1] += (dg * 5) / 16;
							work[py1x0 + 2] += (db * 5) / 16;

							if (x0 < 21) {
								work[py1x0 + 3] += dr / 16;
								work[py1x0 + 3 + 1] += dg / 16;
								work[py1x0 + 3 + 2] += db / 16;
							}
						}
					}
				}
			}
		}
	}
}
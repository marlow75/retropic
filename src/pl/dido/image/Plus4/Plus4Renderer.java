package pl.dido.image.plus4;

import java.awt.image.BufferedImage;
import java.util.Arrays;

import pl.dido.image.renderer.AbstractRenderer;
import pl.dido.image.utils.Config.DITHERING;
import pl.dido.image.utils.Gfx;

public class Plus4Renderer extends AbstractRenderer {

	// plus4 palette
	private final static int colors[] = new int[] { 
			0x000000, 0x171717, 0x46070a, 0x002a26, 0x3e0246, 0x003300, 0x0f0d70, 0x1f2100, 
			0x3e0e00, 0x301700, 0x0f2b00, 0x460326, 0x00310a, 0x031761, 0x1f0770, 0x033100,
			
			0x000000, 0x262626, 0x591417, 0x013b37, 0x510c59, 0x054501, 0x1e1c85, 0x303200, 
			0x511c01, 0x422700, 0x1e3c00, 0x590e37, 0x014217, 0x0f2675, 0x301385, 0x0f4300, 
			
			0x000000, 0x373737, 0x6d2327, 0x0c4e49, 0x641b6d, 0x12580c, 0x2e2c9b, 0x414400, 
			0x642c0c, 0x553800, 0x2e4e00, 0x6d1d49, 0x0c5527, 0x1d378a, 0x41229b, 0x1d5600, 
			
			0x000000, 0x4a4a4a, 0x813338, 0x1a615d, 0x792a82, 0x206c1a, 0x3f3db1, 0x545700,
			0x793d1a, 0x684a07, 0x3f6200, 0x812d5d, 0x1a6938, 0x2d49a0, 0x5433b1, 0x2d6907, 
			
			0x000000, 0x7b7b7b, 0xb86267, 0x449690, 0xaf58b9, 0x4ca144, 0x706deb, 0x878a1f, 
			0xaf6e44, 0x9d7c2b, 0x70961f, 0xb85a90, 0x449e67, 0x5b7bd9, 0x8762eb, 0x5b9e2b, 
			
			0x000000, 0x9b9b9b, 0xdb8186, 0x61b7b1, 0xd176dc, 0x69c360, 0x8f8cff, 0xa8ab38, 
			0xd18d60, 0xbf9c45, 0x8fb738, 0xdb79b1, 0x61c086, 0x799bfd, 0xa880ff, 0x79c045,
			
			0x000000, 0xe0e0e0, 0xffc3c9, 0xa0fef8, 0xffb7ff, 0xa9ff9f, 0xd3d0ff, 0xedf171, 
			0xffd19f, 0xffe081, 0xd3fe71, 0xffbaf8, 0xa0ffc9, 0xbbe0ff, 0xedc3ff, 0xbbff81, 
			
			0x000000, 0xffffff, 0xffffff, 0xfdffff, 0xffffff, 0xfffffd, 0xffffff, 0xffffc9, 
			0xfffffd, 0xffffdb, 0xffffc9, 0xffffff, 0xfdffff, 0xffffff, 0xffffff, 0xffffdb };

	protected int bitmap[] = new int[8000];
	protected int screen[] = new int[1000];

	protected int nibble[] = new int[1000];
	protected int backgroundColor1;
	protected int backgroundColor2;

	public Plus4Renderer(final BufferedImage image, final Plus4Config config) {
		super(image, config);

		palette = new int[128][3];
	}

	@Override
	protected void setupPalette() {
		for (int i = 0; i < colors.length; i++) {
			palette[i][0] = (colors[i] & 0x0000ff); // blue
			palette[i][1] = (colors[i] & 0x00ff00) >> 8; // green
			palette[i][2] = (colors[i] & 0xff0000) >> 16; // red
		}
	}

	@Override
	protected void imagePostproces() {
		switch (((Plus4Config) config).screen_mode) {
		case HIRES:
			switch (config.dither_alg) {
			case BAYER2x2:
			case BAYER4x4:
			case BAYER8x8:
			case BAYER16x16:
				hiresBayer();
				break;
			default:
				hires();
				break;
			}
			
			break;
		case MULTICOLOR:
			switch (config.dither_alg) {
			case BAYER2x2:
			case BAYER4x4:
			case BAYER8x8:
			case BAYER16x16:
				lowresBayer();
				break;
			default:
				lowres();
				break;
			}
			
			break;
		}
	}

	protected void hires() {
		final int work[] = new int[64 * 3];
		int bitmapIndex = 0;

		for (int y = 0; y < 200; y += 8) {
			final int p = y * 320 * 3;

			for (int x = 0; x < 320; x += 8) {
				final int offset = p + x * 3;

				float min = 255;
				float max = 0;

				int index = 0;
				int f = 0, n = 0;

				// 8x8 tile
				for (int y0 = 0; y0 < 8; y0++) {
					for (int x0 = 0; x0 < 24; x0 += 3) {
						final int position = offset + y0 * 320 * 3 + x0;

						final int r = pixels[position] & 0xff;
						final int g = pixels[position + 1] & 0xff;
						final int b = pixels[position + 2] & 0xff;

						work[index++] = r;
						work[index++] = g;
						work[index++] = b;

						switch (((Plus4Config) config).luma_pixels) {
						case OUTER:
							if (x0 > 6 && x0 < 18 && y0 > 2 && y0 < 6) // consider only outer pixels
								continue;
							break;
						case INNER:
							if (((y0 << 3) + x0) % 2 == 0) // gets only even pixels
								continue;
							break;
						}

						final float luma = Gfx.getLuma(r, g, b);

						if (luma > max) {
							max = luma;
							f = getColorIndex(r, g, b);
						}

						if (luma < min) {
							min = luma;
							n = getColorIndex(r, g, b);
						}
					}
				}
				
				final int lf = f / 16;
				final int ln = n / 16;

				final int pf = f - lf * 16;
				final int pn = n - ln * 16;

				int position = (y >> 3) * 40 + (x >> 3);

				// color
				screen[position] = ((pf & 0xf) << 4) | (pn & 0xf);
				// luminance
				nibble[position] = ((ln & 0xf) << 4) | (lf & 0xf);

				int value = 0, bitcount = 0;

				for (int y0 = 0; y0 < 8; y0++) {
					final int k1 = (y0 + 1) * 24;
					final int k2 = (y0 + 2) * 24;

					for (int x0 = 0; x0 < 24; x0 += 3) {
						final int pyx0 = y0 * 24 + x0;
						final int py1x0 = k1 + x0;
						final int py2x0 = k2 + x0;

						final int r = work[pyx0];
						final int g = work[pyx0 + 1];
						final int b = work[pyx0 + 2];

						final int cf[] = palette[f];
						final int fr = cf[0];
						final int fg = cf[1];
						final int fb = cf[2];

						final int cn[] = palette[n];
						int nr = cn[0];
						int ng = cn[1];
						int nb = cn[2];

						final float d1 = Gfx.getDistance(colorAlg, r, g, b, fr, fg, fb);
						final float d2 = Gfx.getDistance(colorAlg, r, g, b, nr, ng, nb);

						if (d1 < d2) {
							nr = fr;
							ng = fg;
							nb = fb;

							value = (value << 1) | 1;
						} else
							value <<= 1;

						if (bitcount % 8 == 7) {
							bitmap[bitmapIndex++] = value;
							value = 0;
						}

						bitcount += 1;
						position = offset + y0 * 320 * 3 + x0;

						pixels[position] = (byte) nr;
						pixels[position + 1] = (byte) ng;
						pixels[position + 2] = (byte) nb;

						if (config.dither_alg != DITHERING.NONE) {
							final int r_error = Gfx.saturateByte(r - nr);
							final int g_error = Gfx.saturateByte(g - ng);
							final int b_error = Gfx.saturateByte(b - nb);

							switch (config.dither_alg) {
							case FLOYDS:
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
							default:
								break;
							}
						}
					}
				}
			}
		}
		
		System.out.println();
	}

	protected void hiresBayer() {
		work = new int[64 * 3];
		int bitmapIndex = 0;

		for (int y = 0; y < 200; y += 8) {
			final int p = y * 320 * 3;

			for (int x = 0; x < 320; x += 8) {
				final int offset = p + x * 3;

				int index = 0;
				int f = 0, n = 0;

				final int occurrence[] = new int[128];

				// 8x8 tile
				for (int y0 = 0; y0 < 8; y0++) {
					for (int x0 = 0; x0 < 24; x0 += 3) {
						final int position = offset + y0 * 320 * 3 + x0;

						final int r = pixels[position] & 0xff;
						final int g = pixels[position + 1] & 0xff;
						final int b = pixels[position + 2] & 0xff;

						work[index++] = r;
						work[index++] = g;
						work[index++] = b;

						switch (((Plus4Config) config).luma_pixels) {
						case OUTER:
							if (x0 > 6 && x0 < 18 && y0 > 2 && y0 < 6) // consider only outer pixels
								continue;
							break;
						case INNER:
							if (((y0 << 3) + x0) % 2 == 0) // gets only even pixels
								continue;
							break;
						}

						occurrence[Gfx.getColorIndex(colorAlg, palette, r, g, b)]++;
					}
				}

				int max = 0, min = 0;
				for (int i = 0; i < palette.length; i++)
					if (occurrence[i] > max) {
						n = f;
						min = max;

						f = i;
						max = occurrence[i];
					} else if (occurrence[i] > min) {
						n = i;
						min = occurrence[i];
					}

				final int lf = f / 16;
				final int ln = n / 16;

				final int pf = f - lf * 16;
				final int pn = n - ln * 16;

				int position = (y >> 3) * 40 + (x >> 3);

				// color
				screen[position] = ((pf & 0xf) << 4) | (pn & 0xf);
				// luminance
				nibble[position] = ((ln & 0x7) << 4) | (lf & 0x7);

				int value = 0, bitcount = 0;
				final int localPalette[][] = new int[][] { { palette[n][0], palette[n][1], palette[n][2] },
						{ palette[f][0], palette[f][1], palette[f][2] } };

				for (int y0 = 0; y0 < 8; y0++) {
					final int k = y0 * 24;

					for (int x0 = 0; x0 < 8; x0++) {
						final int pyx0 = k + x0 * 3;

						final int r = work[pyx0];
						final int g = work[pyx0 + 1];
						final int b = work[pyx0 + 2];

						final int color = Gfx.getColorIndex(colorAlg, localPalette, r, g, b);
						if (color == 1)
							value = (value << 1) | 1;
						else
							value <<= 1;

						if (bitcount % 8 == 7) {
							bitmap[bitmapIndex++] = value;
							value = 0;
						}

						bitcount += 1;
						position = offset + y0 * 320 * 3 + x0 * 3;

						pixels[position] = (byte) localPalette[color][0];
						pixels[position + 1] = (byte) localPalette[color][1];
						pixels[position + 2] = (byte) localPalette[color][2];
					}
				}
			}
		}
	}

	protected void lowres() {
		final int[] newPixels = new int[160 * 200 * 3]; // 160x200
		int bitmapIndex = 0;

		final int occurrence[] = new int[128];
		work = new int[64 * 3];

		// shrinking 320x200 -> 160x200
		for (int y = 0; y < 200; y += 8) {
			final int p1 = y * 320 * 3;
			final int p2 = y * 160 * 3;

			for (int x = 0; x < 40; x++) {
				final int o1 = p1 + x * 8 * 3;
				final int o2 = p2 + x * 4 * 3;

				// 8x8 tile -> 4x8 tile
				for (int ty = 0; ty < 8; ty++) {
					for (int tx = 0, qx = 0; tx < 24; tx += 6, qx += 3) {
						final int ph = o1 + ty * 320 * 3 + tx;
						final int pl = o2 + ty * 160 * 3 + qx;

						final int r1 = pixels[ph] & 0xff;
						final int g1 = pixels[ph + 1] & 0xff;
						final int b1 = pixels[ph + 2] & 0xff;

						final int r2 = pixels[ph + 3] & 0xff;
						final int g2 = pixels[ph + 4] & 0xff;
						final int b2 = pixels[ph + 5] & 0xff;

						final int r, g, b;
						switch (((Plus4Config) config).pixel_merge) {
						case AVERAGE:
							// average color
							r = (r1 + r2) >> 1;
							g = (g1 + g2) >> 1;
							b = (b1 + b2) >> 1;
							break;
						default:
							final float l1 = Gfx.getLuma(r1, g1, b1);
							final float l2 = Gfx.getLuma(r2, g2, b2);

							final float sum = l1 + l2;

							r = (int) ((r1 * l1 + r2 * l2) / sum);
							g = (int) ((g1 * l1 + g2 * l2) / sum);
							b = (int) ((b1 * l1 + b2 * l2) / sum);

							break;
						}

						final int i = getColorIndex(r, g, b);
						occurrence[i]++;

						final int c[] = palette[i];
						newPixels[pl] = c[0];
						newPixels[pl + 1] = c[1];
						newPixels[pl + 2] = c[2];
					}
				}
			}
		}

		int m1 = 0, m2 = 0;
		int i1 = 0, i2 = 0;

		// 4x8 tile palette
		final int tilePalette[][] = new int[4][3];

		// 2 most popular colors
		for (int i = 0; i < 128; i++) {
			final int k = occurrence[i];
			if (k > m1) {
				i2 = i1;
				m2 = m1;

				i1 = i;
				m1 = k;
			} else if (k > m2) {
				i2 = i;
				m2 = k;
			}
		}

		backgroundColor1 = i1;
		backgroundColor2 = i2;

		// common colors
		tilePalette[0] = Arrays.copyOf(palette[i1], 3);
		tilePalette[3] = Arrays.copyOf(palette[i2], 3);

		for (int y = 0; y < 200; y += 8) {
			final int p1 = y * 160 * 3;

			for (int x = 0; x < 40; x++) {
				Arrays.fill(occurrence, 0);

				final int o1 = p1 + x * 4 * 3;
				int index = 0;

				for (int y0 = 0; y0 < 8; y0++) {
					for (int x0 = 0; x0 < 12; x0 += 3) {
						final int position = o1 + y0 * 160 * 3 + x0;

						final int r = newPixels[position];
						final int g = newPixels[position + 1];
						final int b = newPixels[position + 2];

						work[index++] = r;
						work[index++] = g;
						work[index++] = b;

						occurrence[getColorIndex(r, g, b)]++;
					}
				}

				m1 = 0;
				m2 = 0;
				i1 = 0;
				i2 = 0;

				// 2 most popular colors
				for (int i = 0; i < 128; i++) {
					final int k = occurrence[i];
					if (k > m1) {
						i2 = i1;
						m2 = m1;

						i1 = i;
						m1 = k;
					} else if (k > m2) {
						i2 = i;
						m2 = k;
					}
				}

				tilePalette[1] = Arrays.copyOf(palette[i1], 3);
				tilePalette[2] = Arrays.copyOf(palette[i2], 3);

				int position = (y >> 3) * 40 + x;

				final int l1 = i1 / 16;
				final int l2 = i2 / 16;

				final int q1 = i1 - l1 * 16;
				final int q2 = i2 - l2 * 16;

				// colors
				screen[position] = ((q1 & 0xf) << 4) | (q2 & 0xf);
				// luminance
				nibble[position] = ((l2 & 0xf) << 4) | (l1 & 0xf);

				int value = 0, bitcount = 0;
				for (int y0 = 0; y0 < 8; y0++) {
					final int k1 = (y0 + 1) * 12;
					final int k2 = (y0 + 2) * 12;

					for (int x0 = 0; x0 < 12; x0 += 3) {
						final int pyx0 = y0 * 12 + x0;
						final int py1x0 = k1 + x0;
						final int py2x0 = k2 + x0;

						final int r = work[pyx0];
						final int g = work[pyx0 + 1];
						final int b = work[pyx0 + 2];

						index = Gfx.getColorIndex(colorAlg, tilePalette, r, g, b);
						final int c[] = tilePalette[index];

						final int nr = c[0];
						final int ng = c[1];
						final int nb = c[2];

						work[pyx0] = nr;
						work[pyx0 + 1] = ng;
						work[pyx0 + 2] = nb;

						value = (value << 2) | (index & 0x3);
						if (bitcount % 4 == 3) {
							bitmap[bitmapIndex++] = value;
							value = 0;
						}

						bitcount += 1;

						if (config.dither_alg != DITHERING.NONE) {
							final int r_error = Gfx.saturateByte(r - nr);
							final int g_error = Gfx.saturateByte(g - ng);
							final int b_error = Gfx.saturateByte(b - nb);

							switch (config.dither_alg) {
							case FLOYDS:
								if (x0 < 9) {
									work[pyx0 + 3] += r_error * 7 / 16;
									work[pyx0 + 3 + 1] += g_error * 7 / 16;
									work[pyx0 + 3 + 2] += b_error * 7 / 16;
								}
								if (y0 < 7) {
									work[py1x0 - 3] += r_error * 3 / 16;
									work[py1x0 - 3 + 1] += g_error * 3 / 16;
									work[py1x0 - 3 + 2] += b_error * 3 / 16;

									work[py1x0] += r_error * 5 / 16;
									work[py1x0 + 1] += g_error * 5 / 16;
									work[py1x0 + 2] += b_error * 5 / 16;

									if (x0 < 9) {
										work[py1x0 + 3] += r_error / 16;
										work[py1x0 + 3 + 1] += g_error / 16;
										work[py1x0 + 3 + 2] += b_error / 16;
									}
								}
								break;
							case ATKINSON:
								if (x0 < 9) {
									work[pyx0 + 3] += r_error * 1 / 8;
									work[pyx0 + 3 + 1] += g_error * 1 / 8;
									work[pyx0 + 3 + 2] += b_error * 1 / 8;

									if (x0 < 6) {
										work[pyx0 + 6] += r_error * 1 / 8;
										work[pyx0 + 6 + 1] += g_error * 1 / 8;
										work[pyx0 + 6 + 2] += b_error * 1 / 8;
									}
								}
								if (y0 < 7) {
									work[py1x0 - 3] += r_error * 1 / 8;
									work[py1x0 - 3 + 1] += g_error * 1 / 8;
									work[py1x0 - 3 + 2] += b_error * 1 / 8;

									work[py1x0] += r_error * 1 / 8;
									work[py1x0 + 1] += g_error * 1 / 8;
									work[py1x0 + 2] += b_error * 1 / 8;

									if (x0 < 9) {
										work[py1x0 + 3] += r_error * 1 / 8;
										work[py1x0 + 3 + 1] += g_error * 1 / 8;
										work[py1x0 + 3 + 2] += b_error * 1 / 8;
									}

									if (y0 < 6) {
										work[py2x0] += r_error * 1 / 8;
										work[py2x0 + 1] += g_error * 1 / 8;
										work[py2x0 + 2] += b_error * 1 / 8;
									}
								}

								break;
							default:
								break;
							}
						}
					}
				}

				index = 0;

				// show results
				final int p2 = y * 320 * 3;
				final int o2 = p2 + x * 24;

				for (int ty = 0; ty < 8; ty++)
					for (int tx = 0; tx < 24; tx += 6) {
						position = o2 + ty * 320 * 3 + tx;

						pixels[position] = (byte) work[index];
						pixels[position + 3] = (byte) work[index++];

						pixels[position + 1] = (byte) work[index];
						pixels[position + 4] = (byte) work[index++];

						pixels[position + 2] = (byte) work[index];
						pixels[position + 5] = (byte) work[index++];
					}
			}
		}
	}

	protected void lowresBayer() {
		final int[] newPixels = new int[160 * 200 * 3]; // 160x200
		int bitmapIndex = 0;

		final int occurrence[] = new int[128];
		work = new int[64 * 3];

		// shrinking 320x200 -> 160x200
		for (int y = 0; y < 200; y += 8) {
			final int p1 = y * 320 * 3;
			final int p2 = y * 160 * 3;

			for (int x = 0; x < 40; x++) {
				final int o1 = p1 + x * 8 * 3;
				final int o2 = p2 + x * 4 * 3;

				// 8x8 tile -> 4x8 tile
				for (int ty = 0; ty < 8; ty++) {
					for (int tx = 0, qx = 0; tx < 24; tx += 6, qx += 3) {
						final int ph = o1 + ty * 320 * 3 + tx;
						final int pl = o2 + ty * 160 * 3 + qx;

						final int r1 = pixels[ph] & 0xff;
						final int g1 = pixels[ph + 1] & 0xff;
						final int b1 = pixels[ph + 2] & 0xff;

						final int r2 = pixels[ph + 3] & 0xff;
						final int g2 = pixels[ph + 4] & 0xff;
						final int b2 = pixels[ph + 5] & 0xff;

						final int r, g, b;
						switch (((Plus4Config) config).pixel_merge) {
						case AVERAGE:
							// average color
							r = (r1 + r2) >> 1;
							g = (g1 + g2) >> 1;
							b = (b1 + b2) >> 1;
							break;
						default:
							final float l1 = Gfx.getLuma(r1, g1, b1);
							final float l2 = Gfx.getLuma(r2, g2, b2);

							final float sum = l1 + l2;

							r = (int) ((r1 * l1 + r2 * l2) / sum);
							g = (int) ((g1 * l1 + g2 * l2) / sum);
							b = (int) ((b1 * l1 + b2 * l2) / sum);

							break;
						}

						final int i = getColorIndex(r, g, b);
						occurrence[i]++;

						final int c[] = palette[i];
						newPixels[pl] = c[0];
						newPixels[pl + 1] = c[1];
						newPixels[pl + 2] = c[2];
					}
				}
			}
		}

		int m1 = 0, m2 = 0;
		int i1 = 0, i2 = 0;

		// 2 most popular colors
		for (int i = 0; i < 128; i++) {
			final int k = occurrence[i];
			if (k > m1) {
				i2 = i1;
				m2 = m1;

				i1 = i;
				m1 = k;
			} else if (k > m2) {
				i2 = i;
				m2 = k;
			}
		}

		// 4x8 tile palette
		final int tilePalette[][] = new int[4][3];

		backgroundColor1 = i1;
		backgroundColor2 = i2;

		// common colors
		tilePalette[0] = Arrays.copyOf(palette[i1], 3);
		tilePalette[3] = Arrays.copyOf(palette[i2], 3);

		for (int y = 0; y < 200; y += 8) {
			final int p1 = y * 160 * 3;

			for (int x = 0; x < 40; x++) {
				Arrays.fill(occurrence, 0);

				final int o1 = p1 + x * 4 * 3;
				int index = 0;

				for (int y0 = 0; y0 < 8; y0++) {
					for (int x0 = 0; x0 < 12; x0 += 3) {
						final int position = o1 + y0 * 160 * 3 + x0;

						final int r = newPixels[position];
						final int g = newPixels[position + 1];
						final int b = newPixels[position + 2];

						work[index++] = r;
						work[index++] = g;
						work[index++] = b;

						final int color = getColorIndex(r, g, b);
						occurrence[color]++;
					}
				}

				m1 = 0;
				m2 = 0;
				i1 = 0;
				i2 = 0;

				// 3 most popular colors
				for (int i = 0; i < 128; i++) {
					final int k = occurrence[i];
					if (k > m1) {
						i2 = i1;
						m2 = m1;

						i1 = i;
						m1 = k;
					} else if (k > m2) {
						i2 = i;
						m2 = k;
					}
				}

				tilePalette[1] = Arrays.copyOf(palette[i1], 3);
				tilePalette[2] = Arrays.copyOf(palette[i2], 3);

				int position = (y >> 3) * 40 + x;

				final int l1 = i1 / 16;
				final int l2 = i2 / 16;

				final int q1 = i1 - l1 * 16;
				final int q2 = i2 - l2 * 16;

				// colors
				screen[position] = ((q1 & 0xf) << 4) | (q2 & 0xf);
				// luminance
				nibble[position] = ((l2 & 0xf) << 4) | (l1 & 0xf);

				int value = 0, bitcount = 0;
				for (int y0 = 0; y0 < 8; y0++) {

					for (int x0 = 0; x0 < 4; x0++) {
						final int pyx0 = y0 * 12 + x0 * 3;

						int r = work[pyx0];
						int g = work[pyx0 + 1];
						int b = work[pyx0 + 2];

						index = Gfx.getColorIndex(colorAlg, tilePalette, r, g, b);
						final int c[] = tilePalette[index];

						final int nr = c[0];
						final int ng = c[1];
						final int nb = c[2];

						work[pyx0] = nr;
						work[pyx0 + 1] = ng;
						work[pyx0 + 2] = nb;

						value = (value << 2) | (index & 0x3);
						if (bitcount % 4 == 3) {
							bitmap[bitmapIndex++] = value;
							value = 0;
						}

						bitcount += 1;
					}
				}

				index = 0;

				// show results
				final int p2 = y * 320 * 3;
				final int o2 = p2 + x * 24;

				for (int ty = 0; ty < 8; ty++)
					for (int tx = 0; tx < 24; tx += 6) {
						position = o2 + ty * 320 * 3 + tx;

						pixels[position] = (byte) work[index];
						pixels[position + 3] = (byte) work[index++];

						pixels[position + 1] = (byte) work[index];
						pixels[position + 4] = (byte) work[index++];

						pixels[position + 2] = (byte) work[index];
						pixels[position + 5] = (byte) work[index++];
					}
			}
		}
	}

	@Override
	protected int getColorBitDepth() {
		switch (config.dither_alg) {
		case BLUE16x16, BLUE8x8:
			return 4;
		default:
			return 3;
		} 
	}
}
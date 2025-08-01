package pl.dido.image.zx;

import java.awt.image.BufferedImage;

import pl.dido.image.renderer.AbstractRenderer;
import pl.dido.image.utils.Config.DITHERING;
import pl.dido.image.utils.Gfx;

public class ZXRenderer extends AbstractRenderer {

	// ZX spectrum palette
	private final static int colors[] = new int[] { 0x000000, 0x000000, 0x0000D7, 0x0000FF, 0xD70000, 0xFF0000,
			0xD700D7, 0xFF00FF, 0x00D700, 0x00FF00, 0x00D7D7, 0x00FFFF, 0xD7D700, 0xFFFF00, 0xD7D7D7, 0xFFFFFF };

	protected int attribs[] = new int[768];
	protected int bitmap[] = new int[32 * 192];

	protected int zx_line = 0;
	protected int zx_position = 0;

	public ZXRenderer(final BufferedImage image, final ZXConfig config) {
		super(image, config);
		palette = new int[16][3];
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
		//if (config.dither_alg == DITHERING.BAYER2x2)
			hiresBayer();
		//else
		//	hiresDithered();
	}

	protected void hiresDithered() {
		final int work[] = new int[256 * 192 * 3];
		int bitmapIndex = 0;

		for (int y = 0; y < 192; y += 8) { // every 8 line
			final int p = y * 256 * 3;

			for (int x = 0; x < 256; x += 8) { // every 8 pixel
				final int offset = p + x * 3;

				int min = 0;
				int max = 0;

				int f = 0, n = 0;
				int rf = 0, gf = 0, bf = 0, rb = 0, gb = 0, bb = 0;

				// 8x8 tile
				for (int y0 = 0; y0 < 8; y0 += 1) {
					for (int x0 = 0; x0 < 24; x0 += 3) {
						final int position = offset + y0 * 256 * 3 + x0;

						final int r = pixels[position] & 0xff;
						final int g = pixels[position + 1] & 0xff;
						final int b = pixels[position + 2] & 0xff;

						work[position] = r;
						work[position + 1] = g;
						work[position + 2] = b;

						final float luma = Gfx.getLuma(r, g, b);

						if (luma >= 128) {
							rf += r;
							gf += g;
							bf += b;

							max += 1;
						}

						if (luma < 128) {
							rb += r;
							gb += g;
							bb += b;

							min++;
						}
					}
				}

				if (max > 0) {
					rf /= max;
					gf /= max;
					bf /= max;
				}

				if (min > 0) {
					rb /= min;
					gb /= min;
					bb /= min;
				}

				f = getColorIndex(rf, gf, bf);
				n = getColorIndex(rb, gb, bb);

				final int fr = palette[f][0];
				final int fg = palette[f][1];
				final int fb = palette[f][2];

				final int nr = palette[n][0];
				final int ng = palette[n][1];
				final int nb = palette[n][2];

				final int address = (y >> 3) * 32 + (x >> 3);

				int ink = f >> 1;
				int paper = n >> 1;
				int bright = (f % 1 | n % 1) << 6;

				attribs[address] = ((paper & 0xf) << 3) | (ink & 0x7) | bright;
				int value = 0, bitcount = 0;

				for (int y0 = 0; y0 < 8; y0++) {
					final int k0 = offset + y0 * 256 * 3;

					final int k1 = offset + (y0 + 1) * 256 * 3;
					final int k2 = offset + (y0 + 2) * 256 * 3;

					for (int x0 = 0; x0 < 8 * 3; x0 += 3) {
						final int pyx0 = k0 + x0;

						final int py1x0 = k1 + x0;
						final int py2x0 = k2 + x0;

						final int r = Gfx.saturate(work[pyx0]);
						final int g = Gfx.saturate(work[pyx0 + 1]);
						final int b = Gfx.saturate(work[pyx0 + 2]);

						final float d1 = Gfx.getDistance(colorAlg, r, g, b, fr, fg, fb);
						final float d2 = Gfx.getDistance(colorAlg, r, g, b, nr, ng, nb);

						int cr = nr, cg = ng, cb = nb;
						if (d1 < d2) {
							cr = fr;
							cg = fg;
							cb = fb;

							value = (value << 1) | 1;
						} else
							value = value << 1;

						if (bitcount % 8 == 7) {
							bitmap[translate(bitmapIndex++)] = value;
							value = 0;
						}

						bitcount += 1;

						pixels[pyx0] = (byte) cr;
						pixels[pyx0 + 1] = (byte) cg;
						pixels[pyx0 + 2] = (byte) cb;

						if (config.dither_alg != DITHERING.NONE) {
							final int r_error = r - cr;
							final int g_error = g - cg;
							final int b_error = b - cb;

							if (x0 < 9) {
								work[pyx0 + 3] += r_error >> 3;
								work[pyx0 + 3 + 1] += g_error >> 3;
								work[pyx0 + 3 + 2] += b_error >> 3;

								if (x0 < 6) {
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

								if (x0 < 9) {
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
						}
					}
				}
			}
		}
	}

	protected void hiresBayer() {
		int bitmapIndex = 0;

		for (int y = 0; y < 192; y += 8) { // every 8 line
			final int p = y * 256 * 3;

			for (int x = 0; x < 256; x += 8) { // every 8 pixel
				final int offset = p + x * 3;
				int f = 0, n = 0;
				
				final int occurrence[] = new int[16];

				// get 8x8 tile palette
				for (int y0 = 0; y0 < 8; y0 += 1) {
					for (int x0 = 0; x0 < 24; x0 += 3) {
						final int position = offset + y0 * 256 * 3 + x0;

						final int r = pixels[position + 0] & 0xff;
						final int g = pixels[position + 1] & 0xff;
						final int b = pixels[position + 2] & 0xff;

						occurrence[getColorIndex(r, g, b)]++;
					}
				}
				
				int m1 = 0, m2 = 0;
				
				for (int i = 0; i < 16; i++) {
					if (occurrence[i] > m1) {
						m2 = m1;
						n = f;
						
						m1 = occurrence[i];
						f = i;
					} else
					if (occurrence[i] > m2) {
						m2 = occurrence[i];
						n = i;
					}	
				}
				
				final int fr = palette[f][0];
				final int fg = palette[f][1];
				final int fb = palette[f][2];

				final int nr = palette[n][0];
				final int ng = palette[n][1];
				final int nb = palette[n][2];

				final int localPalette[][] = new int[][] { { fr, fg, fb }, { nr, ng, nb } };
				final int address = (y >> 3) * 32 + (x >> 3);

				final int ink = f >> 1;
				final int paper = n >> 1;
				final int bright = (f % 1 | n % 1) << 6;

				attribs[address] = ((paper & 0xf) << 3) | (ink & 0x7) | bright;
				int value = 0, bitcount = 0;

				for (int y0 = 0; y0 < 8; y0++) {
					final int k0 = offset + y0 * 256 * 3;

					for (int x0 = 0; x0 < 8; x0++) {
						final int pyx0 = k0 + x0 * 3;

						final int r = pixels[pyx0 + 0] & 0xff;
						final int g = pixels[pyx0 + 1] & 0xff;
						final int b = pixels[pyx0 + 2] & 0xff;
												
						final int color = Gfx.getColorIndex(colorAlg, localPalette, r, g, b);						
						value = (color == 0) ? (value << 1) | 1 : value << 1;

						if (bitcount % 8 == 7) {
							bitmap[translate(bitmapIndex++)] = value;
							value = 0;
						}

						bitcount += 1;

						pixels[pyx0 + 0] = (byte) localPalette[color][0];
						pixels[pyx0 + 1] = (byte) localPalette[color][1];
						pixels[pyx0 + 2] = (byte) localPalette[color][2];
					}
				}
			}
		}
	}

	private int translate(final int address) {
		final int base = (address & 0b1100000000000) >>> 11;
		final int zx_address = base * 2048 + zx_line * 256 + zx_position;

		if (++zx_line % 8 == 0) {
			zx_line = 0;

			if (++zx_position % 256 == 0)
				zx_position = 0;
		}

		return zx_address;
	}

	@Override
	protected int getColorBitDepth() {
		switch (config.dither_alg) {
		case BAYER2x2:
			return 3;
		default:
			return 5;
		}
	}
}
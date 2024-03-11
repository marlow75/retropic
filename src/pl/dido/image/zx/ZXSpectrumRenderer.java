package pl.dido.image.zx;

import java.awt.image.BufferedImage;

import pl.dido.image.renderer.AbstractRenderer;
import pl.dido.image.utils.Gfx;

public class ZXSpectrumRenderer extends AbstractRenderer {

	// ZX spectrum palette
	private final static int colors[] = new int[] { 0x000000, 0x000000, 0x0000D7, 0x0000FF, 0xD70000, 0xFF0000,
			0xD700D7, 0xFF00FF, 0x00D700, 0x00FF00, 0x00D7D7, 0x00FFFF, 0xD7D700, 0xFFFF00, 0xD7D7D7, 0xFFFFFF };

	protected int attribs[] = new int[768];
	protected int bitmap[] = new int[32 * 192];

	protected int zx_line = 0;
	protected int zx_position = 0;

	public ZXSpectrumRenderer(final BufferedImage image, final ZXConfig config) {
		super(image, config);
		palette = new int[16][3];
	}

	@Override
	protected void setupPalette() {
		switch (image.getType()) {
		case BufferedImage.TYPE_3BYTE_BGR:
			for (int i = 0; i < colors.length; i++) {
				palette[i][0] = (colors[i] & 0x0000ff); // blue
				palette[i][1] = (colors[i] & 0x00ff00) >> 8; // green
				palette[i][2] = (colors[i] & 0xff0000) >> 16; // red
			}
			break;
		case BufferedImage.TYPE_INT_RGB:
			for (int i = 0; i < colors.length; i++) {
				palette[i][0] = (colors[i] & 0xff0000) >> 16; // red
				palette[i][1] = (colors[i] & 0x00ff00) >> 8; // green
				palette[i][2] = (colors[i] & 0x0000ff); // blue
			}
			break;
		default:
			throw new RuntimeException("Unsupported Pixel format !!!");
		}
	}

	@Override
	protected void imagePostproces() {
		hiresLumaDithered();
	}

	protected void hiresLumaDithered() {
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

						final float luma = Gfx.getLumaByCM(pixelType, r, g, b);

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

						final int fr = palette[f][0];
						final int fg = palette[f][1];
						final int fb = palette[f][2];

						int nr = palette[n][0];
						int ng = palette[n][1];
						int nb = palette[n][2];

						final float d1 = Gfx.getDistanceByCM(colorAlg, pixelType, r, g, b, fr, fg, fb);
						final float d2 = Gfx.getDistanceByCM(colorAlg, pixelType, r, g, b, nr, ng, nb);

						if (d1 < d2) {
							nr = fr;
							ng = fg;
							nb = fb;

							value = (value << 1) | 1;
						} else
							value = value << 1;

						if (bitcount % 8 == 7) {
							bitmap[translate(bitmapIndex++)] = value;
							value = 0;
						}

						bitcount += 1;

						pixels[pyx0] = (byte) nr;
						pixels[pyx0 + 1] = (byte) ng;
						pixels[pyx0 + 2] = (byte) nb;

						if (config.dithering) {
							final int r_error = r - nr;
							final int g_error = g - ng;
							final int b_error = b - nb;

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
}
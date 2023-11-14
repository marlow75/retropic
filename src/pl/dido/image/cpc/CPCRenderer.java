package pl.dido.image.cpc;

import java.awt.image.BufferedImage;

import pl.dido.image.renderer.AbstractRenderer;
import pl.dido.image.utils.Gfx;
import pl.dido.image.utils.neural.SOMFixedPalette;
import pl.dido.image.utils.neural.SOMWinnerFixedPalette;

public class CPCRenderer extends AbstractRenderer {

	// CPC palette 27 colors
	// real palette
//	private final static int colors[] = new int[] { 0x000201, 0x00026B, 0x0C02F4, 0x6C0201, 0x690268, 0x6C02F2,
//			0xF30506, 0xF00268, 0xF302F4, 0x027801, 0x007868, 0x0C7BF4, 0x6E7B01, 0x6E7D6B, 0x6E7BF6, 0xF37D0D,
//			0xF37D6B, 0xFA80F9, 0x02F001, 0x00F36B, 0x0FF3F2, 0x71F504, 0x71F36B, 0x71F3F4, 0xF3F30D, 0xF3F36D,
//			0xFFF3F9 };

	// palette
	private final static int colors[] = new int[] { 0x040404, 0x00007f, 0x0000ff, 0x800000, 0x800080, 0x7f00ff,
			0xff0000, 0xff0080, 0xff00ff, 0x008000, 0x008080, 0x0080ff, 0x808000, 0x808080, 0x8080ff, 0xff7f00,
			0xff8080, 0xff80ff, 0x01ff00, 0x01ff80, 0x01ffff, 0x80ff00, 0x80ff80, 0x80ffff, 0xffff00, 0xffff80, 
			0xffffff };

	protected int bitmap[] = new int[16384];
	protected int pictureColors[][];

	protected int firmwareIndexes[];

	protected int colorMapping[] = new int[] { 0x54, 0x44, 0x55, 0x5C, 0x58, 0x5D, 0x4C, 0x45, 0x4D, 0x56, 0x46, 0x57,
			0x5E, 0x40, 0x5F, 0x4E, 0x47, 0x4F, 0x52, 0x42, 0x53, 0x5A, 0x59, 0x5B, 0x4A, 0x43, 0x4B };

	public CPCRenderer(final BufferedImage image, final CPCConfig config) {
		super(image, config);
		palette = new int[27][3];
	}

	@Override
	protected void setupPalette() {
		switch (colorModel) {
		case BufferedImage.TYPE_3BYTE_BGR:
			for (int i = 0; i < colors.length; i++) {
				final int pixel[] = palette[i];

				pixel[0] = (colors[i] & 0x0000ff); // blue
				pixel[1] = (colors[i] & 0x00ff00) >> 8; // green
				pixel[2] = (colors[i] & 0xff0000) >> 16; // red
			}
			break;
		case BufferedImage.TYPE_INT_RGB:
			for (int i = 0; i < colors.length; i++) {
				final int pixel[] = palette[i];

				pixel[0] = (colors[i] & 0xff0000) >> 16; // red
				pixel[1] = (colors[i] & 0x00ff00) >> 8; // green
				pixel[2] = (colors[i] & 0x0000ff); // blue
			}
			break;
		default:
			throw new RuntimeException("Unsupported Pixel format !!!");
		}
	}

	@Override
	protected void imagePostproces() {
		pictureColors = modePalette(((CPCConfig) config).screen_mode);

		switch (((CPCConfig) config).screen_mode) {
		case MODE1:
			mode1();
			break;
		case MODE0:
			mode0();
			break;
		}
	}

	private int[][] modePalette(final CPCConfig.SCREEN_MODE mode) {
		final int p[][];
		final SOMFixedPalette som;

		switch (mode) {
		case MODE0:
			som = new SOMWinnerFixedPalette(4, 4, 2);
			p = som.train(pixels);

			break;
		default:
			som = new SOMWinnerFixedPalette(2, 2, 2);
			p = som.train(pixels);

			break;
		}

		final int size = p.length;
		firmwareIndexes = new int[size];

		for (int i = 0; i < size; i++) {
			final int pixel[] = p[i];
			final int index = getColorIndex(pixel[0], pixel[1], pixel[2]); // color

			pixel[0] = palette[index][0];
			pixel[1] = palette[index][1];
			pixel[2] = palette[index][2];

			firmwareIndexes[i] = index;
		}

		if (((CPCConfig) config).replace_white) {
			// replace brightest with white
			float min = Float.MAX_VALUE;
			float max = 0;
			int ix = 0, im = 0;

			for (int i = 0; i < size; i++) {
				final int c[] = p[i];
				final float luma = Gfx.getLumaByCM(colorModel, c[0], c[1], c[2]);

				if (luma < min) {
					min = luma;
					im = i;
				}

				if (luma > max) {
					max = luma;
					ix = i;
				}
			}

			int c[] = p[ix];
			// dimmed white - yellow
			c[0] = 255;
			c[1] = 255;
			c[2] = 255;

			c = p[im];
			c[0] = 0;
			c[1] = 0;
			c[2] = 0;

			firmwareIndexes[ix] = 25;
			firmwareIndexes[im] = 0;
		}

		return p;
	}

	protected void mode1() {
		final int[] work = Gfx.copy2Int(pixels);

		int r0, g0, b0;

		final int width3 = width * 3;
		int bit0 = 128, bit1 = 8;

		for (int y = 0; y < height; y++) {
			int index = 0;

			final int i = y >> 3;
			final int j = y - (i << 3);

			final int offset = i * 80 + j * 2048;

			for (int x = 0; x < width3; x += 3) {
				final int pyx = y * width3 + x;
				final int py1x = (y + 1) * width3 + x;
				final int py2x = (y + 2) * width3 + x;

				r0 = work[pyx];
				g0 = work[pyx + 1];
				b0 = work[pyx + 2];

				final int color = Gfx.getColorIndex(colorAlg, colorModel, pictureColors, r0, g0, b0);
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

				final int data = ((color & 1) != 0 ? bit0 : 0) | ((color & 2) != 0 ? bit1 : 0);
				bitmap[offset + index] |= data;

				if (bit0 == 16) {
					bit0 = 128;
					bit1 = 8;

					index += 1;
				} else {
					bit0 >>= 1;
					bit1 >>= 1;
				}

				if (config.dithering) {
					final int r_error = Gfx.saturate(r0 - r);
					final int g_error = Gfx.saturate(g0 - g);
					final int b_error = Gfx.saturate(b0 - b);

					if (x < (width - 1) * 3) {
						work[pyx + 3] += r_error >> 3;
						work[pyx + 3 + 1] += g_error >> 3;
						work[pyx + 3 + 2] += b_error >> 3;

						if (x < (width - 2) * 3) {
							work[pyx + 6] += r_error >> 3;
							work[pyx + 6 + 1] += g_error >> 3;
							work[pyx + 6 + 2] += b_error >> 3;
						}
					}
					if (y < (height - 1)) {
						work[py1x - 3] += r_error >> 3;
						work[py1x - 3 + 1] += g_error >> 3;
						work[py1x - 3 + 2] += b_error >> 3;

						work[py1x] += r_error >> 3;
						work[py1x + 1] += g_error >> 3;
						work[py1x + 2] += b_error >> 3;

						if (x < (width - 1) * 3) {
							work[py1x + 3] += r_error >> 3;
							work[py1x + 3 + 1] += g_error >> 3;
							work[py1x + 3 + 2] += b_error >> 3;
						}

						if (y < (height - 2)) {
							work[py2x] += r_error >> 3;
							work[py2x + 1] += g_error >> 3;
							work[py2x + 2] += b_error >> 3;
						}
					}
				}
			}
		}
	}

	protected void mode0() {
		final int[] newPixels = new int[160 * 200 * 3]; // 160x200
		int bit0 = 128, bit1 = 8, bit2 = 0, bit3 = 0;

		// shrinking 320x200 -> 160x200
		for (int y = 0; y < 200; y++) {
			final int p1 = y * 320 * 3;
			final int p2 = y * 160 * 3;

			final int i = y >> 3;
			final int j = y - (i << 3);

			int index = 0;
			final int offset = i * 80 + j * 2048;

			for (int x = 0; x < 160; x++) {
				final int ph = p1 + x * 3 * 2;
				final int pl = p2 + x * 3;

				final int r1 = pixels[ph] & 0xff;
				final int g1 = pixels[ph + 1] & 0xff;
				final int b1 = pixels[ph + 2] & 0xff;

				final int r2 = pixels[ph + 3] & 0xff;
				final int g2 = pixels[ph + 4] & 0xff;
				final int b2 = pixels[ph + 5] & 0xff;

				final int r, g, b;
				switch (((CPCConfig) config).pixel_merge) {
				case AVERAGE:
					// average color
					r = (r1 + r2) >> 1;
					g = (g1 + g2) >> 1;
					b = (b1 + b2) >> 1;
					break;
				default:
					if (Gfx.getLumaByCM(colorModel, r1, g1, b1) > Gfx.getLumaByCM(colorModel, r2, g2, b2)) {
						r = r1;
						g = g1;
						b = b1;
					} else {
						r = r2;
						g = g2;
						b = b2;
					}

					break;
				}

				final int color = Gfx.getColorIndex(colorAlg, colorModel, pictureColors, r, g, b);
				final int data = ((color & 1) != 0 ? bit0 : 0) | ((color & 2) != 0 ? bit1 : 0)
						| ((color & 4) != 0 ? bit2 : 0) | ((color & 8) != 0 ? bit3 : 0);

				bitmap[offset + index] |= data;

				if (bit0 == 64) {
					bit0 = 128;
					bit1 = 8;
					bit2 = 32;
					bit3 = 2;

					index += 1;
				} else {
					bit0 >>= 1;
					bit1 >>= 1;
					bit2 >>= 1;
					bit3 >>= 1;
				}

				final int c[] = pictureColors[color];

				newPixels[pl] = c[0];
				newPixels[pl + 1] = c[1];
				newPixels[pl + 2] = c[2];
			}
		}

		// show results
		for (int y0 = 0; y0 < 200; y0++)
			for (int x0 = 0; x0 < 160; x0++) {
				final int pl = y0 * 160 * 3 + x0 * 3;
				final int ph = y0 * 320 * 3 + x0 * 2 * 3;

				pixels[ph] = (byte) newPixels[pl];
				pixels[ph + 3] = (byte) newPixels[pl];

				pixels[ph + 1] = (byte) newPixels[pl + 1];
				pixels[ph + 4] = (byte) newPixels[pl + 1];

				pixels[ph + 2] = (byte) newPixels[pl + 2];
				pixels[ph + 5] = (byte) newPixels[pl + 2];
			}
	}
}
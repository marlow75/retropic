package pl.dido.image.cpc;

import java.awt.image.BufferedImage;
import java.util.HashSet;

import pl.dido.image.renderer.AbstractRenderer;
import pl.dido.image.utils.Config.DITHERING;
import pl.dido.image.utils.Gfx;
import pl.dido.image.utils.neural.SOMFixedPalette;
import pl.dido.image.utils.neural.SOMWinnerFixedPalette;

public class CPCRenderer extends AbstractRenderer {

	// CPC palette 27 colors
	private final static int colors[] = new int[] { 0x000201, 0x00026B, 0x0C02F4, 0x6C0201, 0x690268, 0x6C02F2,
			0xF30506, 0xF00268, 0xF302F4, 0x027801, 0x007868, 0x0C7BF4, 0x6E7B01, 0x6E7D6B, 0x6E7BF6, 0xF37D0D,
			0xF37D6B, 0xFA80F9, 0x02F001, 0x00F36B, 0x0FF3F2, 0x71F504, 0x71F36B, 0x71F3F4, 0xF3F30D, 0xF3F36D,
			0xFFF3F9 };

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
		for (int i = 0; i < colors.length; i++) {
			final int pixel[] = palette[i];

			pixel[0] = (colors[i] & 0x0000ff); // blue
			pixel[1] = (colors[i] & 0x00ff00) >> 8; // green
			pixel[2] = (colors[i] & 0xff0000) >> 16; // red
		}
	}

	@Override
	protected void imagePostproces() {
		pictureColors = modePalette(((CPCConfig) config).screen_mode);

		switch (((CPCConfig) config).screen_mode) {
		case MODE1:
			switch (config.dither_alg) {
			case BAYER2x2:
			case BAYER4x4:
			case BAYER8x8:
			case BAYER16x16:
			case BLUE8x8:
			case BLUE16x16:
				mode1Bayer();
				break;
			default:
				mode1();
				break;
			}
			break;
		case MODE0:
			switch (config.dither_alg) {
			case BAYER2x2:
			case BAYER4x4:
			case BAYER8x8:
			case BAYER16x16:
			case BLUE8x8:
			case BLUE16x16:
				mode0Bayer();
				break;
			default:
				mode0();
				break;
			}
			break;
		}
	}

	private int[][] modePalette(final CPCConfig.SCREEN_MODE mode) {
		final int p[][];
		final SOMFixedPalette som;

		switch (mode) {
		case MODE0:
			//som = new SOMWinnerFixedPalette(4, 4, 2);
			som = new SOMFixedPalette(4, 4, 2);
			p = som.train(pixels);

			break;
		default:
			//som = new SOMWinnerFixedPalette(2, 2, 2);
			som = new SOMWinnerFixedPalette(2, 2, 3);
			p = som.train(pixels);

			break;
		}

		final int size = p.length;
		firmwareIndexes = new int[size];

		// map calculated colors to machine palette
		final HashSet<Integer> colors = new HashSet<Integer>();
		for (int i = 0; i < size; i++) {
			final int pixel[] = p[i];

			int index = getColorIndex(pixel[0], pixel[1], pixel[2]); // color
			while (colors.contains(index))
				index = (index + 1) % 27;

			pixel[0] = palette[index][0];
			pixel[1] = palette[index][1];
			pixel[2] = palette[index][2];

			firmwareIndexes[i] = index;
			colors.add(index);
		}

		if (((CPCConfig) config).replace_white) {
			// replace brightest with white
			float min = Float.MAX_VALUE;
			float max = 0;
			int ix = 0, im = 0;

			for (int i = 0; i < size; i++) {
				final int c[] = p[i];
				final float luma = Gfx.getLuma(c[0], c[1], c[2]);

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
			// white
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

		final int width3 = screenWidth * 3;
		int bit0 = 128, bit1 = 8;

		for (int y = 0; y < screenHeight; y++) {
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

				final int color = Gfx.getColorIndex(colorAlg, pictureColors, r0, g0, b0);
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

				if (config.dither_alg != DITHERING.NONE) {
					final int r_error = Gfx.saturate(r0 - r);
					final int g_error = Gfx.saturate(g0 - g);
					final int b_error = Gfx.saturate(b0 - b);

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
					if (y < (screenHeight - 1)) {
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

						if (y < (screenHeight - 2)) {
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
		int bit0 = 128, bit1 = 8, bit2 = 0, bit3 = 0;

		// shrinking 320x200 -> 160x200
		for (int y = 0; y < 200; y++) {
			final int p1 = y * 320 * 3;

			final int i = y >> 3;
			final int j = y - (i << 3);

			int index = 0;

			int r, g, b;
			final int offset = i * 80 + j * 2048;

			for (int x = 0; x < 160; x++) {
				final int ph = p1 + x * 3 * 2;

				final int r1 = pixels[ph] & 0xff;
				final int g1 = pixels[ph + 1] & 0xff;
				final int b1 = pixels[ph + 2] & 0xff;

				final int r2 = pixels[ph + 3] & 0xff;
				final int g2 = pixels[ph + 4] & 0xff;
				final int b2 = pixels[ph + 5] & 0xff;

				switch (((CPCConfig) config).pixel_merge) {
				case AVERAGE:
					// average color
					r = (r1 + r2) >> 1;
					g = (g1 + g2) >> 1;
					b = (b1 + b2) >> 1;
					break;
				default:
					final float l1 = Gfx.getLuma(r1, g1, b1) / 255;
					final float l2 = Gfx.getLuma(r2, g2, b2) / 255;

					final float sum = (l1 + l2);

					r = (int) ((r1 * l1 + r2 * l2) / sum);
					g = (int) ((g1 * l1 + g2 * l2) / sum);
					b = (int) ((b1 * l1 + b2 * l2) / sum);

					break;
				}

				final int color = Gfx.getColorIndex(colorAlg, pictureColors, r, g, b);
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
				r = c[0];
				g = c[1];
				b = c[2];

				pixels[ph] = (byte) r;
				pixels[ph + 3] = (byte) r;

				pixels[ph + 1] = (byte) g;
				pixels[ph + 4] = (byte) g;

				pixels[ph + 2] = (byte) b;
				pixels[ph + 5] = (byte) b;
			}
		}
	}

	protected void mode1Bayer() {
		int r0, g0, b0;
		int bit0 = 128, bit1 = 8;

		for (int y = 0; y < screenHeight; y++) {
			int index = 0;
			final int width3 = screenWidth * 3;

			final int i = y >> 3;
			final int j = y - (i << 3);

			final int offset = i * 80 + j * 2048;

			for (int x = 0; x < screenWidth; x++) {
				final int pyx = y * width3 + x * 3;

				r0 = pixels[pyx] & 0xff;
				g0 = pixels[pyx + 1] & 0xff;
				b0 = pixels[pyx + 2] & 0xff;

				final int color = Gfx.getColorIndex(colorAlg, pictureColors, r0, g0, b0);

				final int r = pictureColors[color][0];
				final int g = pictureColors[color][1];
				final int b = pictureColors[color][2];

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
			}
		}
	}

	protected void mode0Bayer() {
		final int[] newPixels = new int[160 * 200 * 3]; // 160x200
		int bit0 = 128, bit1 = 8, bit2 = 0, bit3 = 0;

		// shrinking 320x200 -> 160x200
		for (int y = 0; y < 200; y++) {
			final int p1 = y * 320 * 3;
			final int p2 = y * 160 * 3;

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
					final float l1 = Gfx.getLuma(r1, g1, b1) / 255;
					final float l2 = Gfx.getLuma(r2, g2, b2) / 255;

					final float sum = (l1 + l2);

					r = (int) ((r1 * l1 + r2 * l2) / sum);
					g = (int) ((g1 * l1 + g2 * l2) / sum);
					b = (int) ((b1 * l1 + b2 * l2) / sum);

					break;
				}

				newPixels[pl] = r;
				newPixels[pl + 1] = g;
				newPixels[pl + 2] = b;
			}
		}

		// show results
		for (int y = 0; y < 200; y++) {
			final int i = y >> 3;
			final int j = y - (i << 3);

			int index = 0;
			final int offset = i * 80 + j * 2048;

			for (int x = 0; x < 160; x++) {
				final int pl = y * 160 * 3 + x * 3;
				final int ph = y * 320 * 3 + x * 2 * 3;

				int r = newPixels[pl];
				int g = newPixels[pl + 1];
				int b = newPixels[pl + 2];

				final int color = Gfx.getColorIndex(colorAlg, pictureColors, r, g, b);
				r = pictureColors[color][0];
				g = pictureColors[color][1];
				b = pictureColors[color][2];

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

				pixels[ph] = (byte) r;
				pixels[ph + 3] = (byte) r;

				pixels[ph + 1] = (byte) g;
				pixels[ph + 4] = (byte) g;

				pixels[ph + 2] = (byte) b;
				pixels[ph + 5] = (byte) b;
			}
		}
	}

	@Override
	protected int getColorBitDepth() {
		switch (((CPCConfig) config).dither_alg) {
		case BLUE8x8, BLUE16x16:
			return 16;
		case NOISE:
			return 2;
		default:
			return 3;
		}
	}
}
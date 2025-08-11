package pl.dido.image.c128;

import java.awt.image.BufferedImage;

import pl.dido.image.renderer.AbstractRenderer;
import pl.dido.image.utils.Gfx;

public class C128Renderer extends AbstractRenderer {

	protected int bitmap[] = new int[80 * 200]; // 640x200x2 in tiles of 8x2 pixels
	protected int attribs[] = new int[2000 * 4];

	// VDC palette 16 colors
	private final static int colors[] = new int[] { 0x010101, 0x555555, 0x0000aa, 0x5555ff, 0x00aa00, 0x55ff55,
			0x00aaaa, 0x55ffff, 0xaa0000, 0xff5555, 0xaa00aa, 0xff55ff, 0xaa5500, 0xffff55, 0xaaaaaa, 0xffffff };

	public C128Renderer(final BufferedImage image, final C128Config config) {
		super(image, config);
		palette = new int[16][3];
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
		hires640x200();
	}
	
	protected void hires640x200() {
		final int work[] = new int[16 * 3];
		final int bytes[] = new int[2];
		
		for (int y = 0; y < 200; y += 2) {
			final int p = y * 640 * 3;

			for (int x = 0; x < 640; x += 8) {
				final int offset = p + x * 3;

				float min = 255;
				float max = 0;

				int index = 0;
				int f = 0, n = 0;

				// 8x2 tile
				for (int y0 = 0; y0 < 2; y0++) {
					for (int x0 = 0; x0 < 24; x0 += 3) {
						final int position = offset + y0 * 640 * 3 + x0;

						final int r = pixels[position] & 0xff;
						final int g = pixels[position + 1] & 0xff;
						final int b = pixels[position + 2] & 0xff;

						work[index++] = r;
						work[index++] = g;
						work[index++] = b;

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

				// if same colors = black backgroun
				if (f == n)
					n = 0;

				final int address1 = (y >> 1) * 80 + (x >> 3);
				final int address2 = y * 80 + (x >> 3);
				
				attribs[address1] = ((n & 0xf) << 4) | (f & 0xf);
				int value = 0, bitcount = 0;

				for (int y0 = 0; y0 < 2; y0++)
					for (int x0 = 0; x0 < 24; x0 += 3) {
						final int pyx0 = y0 * 24 + x0;

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
							bytes[y0] = value;
							value = 0;
						}

						bitcount += 1;
						final int position = offset + y0 * 640 * 3 + x0;

						pixels[position] = (byte) nr;
						pixels[position + 1] = (byte) ng;
						pixels[position + 2] = (byte) nb;
					}
				bitmap[address2] = bytes[0];
				bitmap[address2 + 80] = bytes[1];
			}
		}
	}

	@Override
	protected int getColorBitDepth() {
		switch (config.dither_alg) {
		case BLUE16x16, BLUE8x8:
			return 7;
		case NOISE:
			return 3;
		default:
			return 8;
		}
	}
}

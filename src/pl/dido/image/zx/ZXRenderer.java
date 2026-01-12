package pl.dido.image.zx;

import java.awt.image.BufferedImage;

import pl.dido.image.renderer.AbstractRenderer;
import pl.dido.image.utils.Gfx;

public class ZXRenderer extends AbstractRenderer {

	// ZX spectrum palette
	private final static int colors[] = new int[] { 0x000000, 0x000000, 0x0000D7, 0x0000FF, 0xD70000, 0xFF0000,
			0xD700D7, 0xFF00FF, 0x00D700, 0x00FF00, 0x00D7D7, 0x00FFFF, 0xD7D700, 0xFFFF00, 0xD7D7D7, 0xFFFFFF };

	protected int attribs[] = new int[768];
	protected int bitmap[] = new int[32 * 192];
	
	protected float coefficients[];

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
		
		super.setupPalette();
	}

	@Override
	protected void imagePostproces() {
		hiresBayer();
	}

	protected void hiresBayer() {
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
					} else if (occurrence[i] > m2) {
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
							final int zxOffset = (((y + y0) & 0x07) << 8) | (((y + y0) & 0x38) << 2)
									| (((y + y0) & 0xC0) << 5) | (x >> 3);
							
							bitmap[zxOffset] = value;
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

	@Override
	protected int getColorBitDepth() {
		switch (config.dither_alg) {
		case BAYER2x2:
			return 3;
		default:
			return 4;
		}
	}
}
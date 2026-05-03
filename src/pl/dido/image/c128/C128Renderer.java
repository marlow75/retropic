package pl.dido.image.c128;

import java.awt.image.BufferedImage;

import pl.dido.image.renderer.AbstractRenderer;
import pl.dido.image.utils.Gfx;

public class C128Renderer extends AbstractRenderer {

	protected int bitmap[] = new int[80 * 200]; // 640x200x2 in tiles of 8x2 pixels
	protected int attribs[] = new int[2000 * 4];

	protected float coefficients[];
	protected final float[] dBaseArr = new float[16];

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
			final int color[] = palette[i];

			color[0] = (colors[i] & 0x0000ff); // blue
			color[1] = (colors[i] & 0x00ff00) >> 8; // green
			color[2] = (colors[i] & 0xff0000) >> 16; // red
		}

		super.setupPalette();
	}

	@Override
	protected void imagePostproces() {
		hires640x200();
	}

	protected int secondColorMinError(final int[] work, final int baseIndex) {
		final int[] base = palette[baseIndex];
		final int br = base[0], bg = base[1], bb = base[2];

		final float[] dBaseArr = new float[16]; // albo pole klasy, żeby nie alokować
		int p = 0;
		
		for (int i = 0; i < 16; i++) {
			final int r = work[p++], g = work[p++], b = work[p++];
			dBaseArr[i] = getDistance(r, g, b, br, bg, bb);
		}

		int best = baseIndex;
		float bestCost = Float.MAX_VALUE;

		for (int c = 0; c < palette.length; c++) {
			if (c == baseIndex)
				continue;

			final int[] alt = palette[c];
			final int ar = alt[0], ag = alt[1], ab = alt[2];

			float cost = 0f;
			p = 0;
			
			for (int i = 0; i < 16; i++) {
				final int r = work[p++], g = work[p++], b = work[p++];

				float dAlt = getDistance(r, g, b, ar, ag, ab);
				float dMin = dBaseArr[i] < dAlt ? dBaseArr[i] : dAlt;
				
				cost += dMin;
				if (cost >= bestCost)
					break;
			}

			if (cost < bestCost) {
				bestCost = cost;
				best = c;
			}
		}
		
		return best;
	}

	protected void hires640x200() {
		final int work[] = new int[16 * 3];
		final int bytes[] = new int[2];

		final int rowStride = 640 * 3;

		for (int y = 0; y < 200; y += 2) {
			final int p = y * rowStride;

			for (int x = 0; x < 640; x += 8) {
				final int offset = p + x * 3;

				float min = Float.MAX_VALUE;
				float max = 0;

				int index = 0;
				int f = 0, n = 0;

				// 8x2 tile
				for (int y0 = 0; y0 < 2; y0++) {
					for (int x0 = 0; x0 < 24; x0 += 3) {
						final int position = offset + y0 * rowStride + x0;

						int r = pixels[position] & 0xff;
						int g = pixels[position + 1] & 0xff;
						int b = pixels[position + 2] & 0xff;

						work[index++] = r;
						work[index++] = g;
						work[index++] = b;

						final int color = getColorIndex(r, g, b);
						final float luma = Gfx.getLuma(r, g, b);

						if (luma > max) {
							max = luma;
							f = color;
						}

						if (luma < min) {
							min = luma;
							n = color;
						}
					}
				}

				if (f == n) {
					final int base = n;
					final int second = secondColorMinError(work, base);

					n = base;
					f = second;
				}

				final int address1 = (y >> 1) * 80 + (x >> 3);
				final int address2 = y * 80 + (x >> 3);

				attribs[address1] = ((n & 0xf) << 4) | (f & 0xf);

				final int cf[] = palette[f];
				final int fr = cf[0];
				final int fg = cf[1];
				final int fb = cf[2];

				final int cn[] = palette[n];
				final int br = cn[0];
				final int bg = cn[1];
				final int bb = cn[2];

				for (int y0 = 0; y0 < 2; y0++) {
				    int v = 0;
				    final int basePos = offset + y0 * rowStride;

				    for (int x0 = 0; x0 < 24; x0 += 3) {
				        final int i = y0 * 24 + x0;
				        final int r = work[i], g = work[i+1], b = work[i+2];

				        final boolean useF = getDistance(r,g,b, fr,fg,fb) < getDistance(r,g,b, br,bg,bb);
				        v = (v << 1) | (useF ? 1 : 0);

				        int pos = basePos + x0;
				        pixels[pos]     = (byte)(useF ? fr : br);
				        pixels[pos + 1] = (byte)(useF ? fg : bg);
				        pixels[pos + 2] = (byte)(useF ? fb : bb);
				    }
				    
				    bytes[y0] = v;
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
			return 28;
		case NOISE:
			return 3;
		default:
			return 8;
		}
	}
}

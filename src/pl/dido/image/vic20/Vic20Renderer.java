package pl.dido.image.vic20;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Comparator;

import pl.dido.image.renderer.AbstractRenderer;
import pl.dido.image.utils.BitVector;
import pl.dido.image.utils.Config;
import pl.dido.image.utils.Gfx;
import pl.dido.image.utils.Config.NEAREST_COLOR;
import pl.dido.image.utils.neural.NetworkProgressListener;
import pl.dido.image.utils.neural.Position;
import pl.dido.image.utils.neural.SOMCharsetNetwork;
import pl.dido.image.utils.neural.SOMDataset;
import pl.dido.image.utils.neural.SOMMulticolorCharsetNetwork;

public class Vic20Renderer extends AbstractRenderer implements NetworkProgressListener {
	protected final static int colors[] = new int[] { 0x000000, 0xFFFFFF, 0xF00000, 0x00F0F0, 0x600060, 0x00A000,
			0x0000F0, 0xD0D000, 0xC0A000, 0xFFA000, 0xF08080, 0x00FFFF, 0xFF00FF, 0x00FF00, 0x00A0FF, 0xFFFF00 };

	protected final static int power2[] = new int[] { 128, 64, 32, 16, 8, 4, 2, 1 };

	protected final static String PETSCII_NETWORK_L1 = "vic20.L1network";
	protected final static String PETSCII_ENCODER = "vic20.autoencoder";

	protected final static String PETSCII_CHARSET = "vic20petscii.bin";

	protected int screen[] = new int[22 * 23];
	protected int nibble[] = new int[22 * 23];

	protected int backgroundColor;
	protected int auxiliaryColor;

	protected int borderColor;
	protected int foregroundPalette[][];

	protected int locPalette[][] = null;
	protected byte charset[]; // charset 8x8 pixels per char

	protected final int occurrence[] = new int[8];
	protected final Integer indexes[] = new Integer[] { 0, 1, 2, 3, 4, 5, 6, 7 };

	protected final int[] newPixels88 = new int[88 * 184 * 3]; // 88x184

	protected SOMCharsetNetwork som1;
	protected SOMMulticolorCharsetNetwork som2;

	protected void initialize() {
		foregroundPalette = new int[8][3];
		palette = new int[16][3];
	}

	public Vic20Renderer(final Config config) {
		super(config);
		initialize();
	}

	public Vic20Renderer(final BufferedImage image, final Config config) {
		super(image, config);
		initialize();
	}

	@Override
	protected void setupPalette() {
		for (int i = 0; i < colors.length / 2; i++) {
			final int pixel1[] = palette[i];
			final int pixel2[] = foregroundPalette[i];

			pixel1[0] = (colors[i] & 0x0000ff); // blue
			pixel1[1] = (colors[i] & 0x00ff00) >> 8; // green
			pixel1[2] = (colors[i] & 0xff0000) >> 16; // red

			pixel2[0] = pixel1[0];
			pixel2[1] = pixel1[1];
			pixel2[2] = pixel1[2];
		}

		for (int i = colors.length / 2; i < colors.length; i++) {
			final int pixel[] = palette[i];

			pixel[0] = (colors[i] & 0x0000ff); // blue
			pixel[1] = (colors[i] & 0x00ff00) >> 8; // green
			pixel[2] = (colors[i] & 0xff0000) >> 16; // red
		}

		super.setupPalette();
	}

	@Override
	protected void imagePostproces() {
		switch (((Vic20Config) config).mode) {
		case HIRES: {
			final SOMDataset<BitVector> dataset = new SOMDataset<BitVector>();
			som1 = new SOMCharsetNetwork(16, 16);

			final SOMDataset<BitVector> item = getChargenHires();
			for (int i = 0; i < 5; i++) // learn same 5 times
				dataset.addAll(item);

			charset = som1.train(dataset);
			hires();

			break;
		}
		default: {
			final SOMDataset<float[]> dataset = new SOMDataset<float[]>();
			som2 = new SOMMulticolorCharsetNetwork(16, 16);

			final SOMDataset<float[]> item = getChargenLowres();
			for (int i = 0; i < 5; i++) // learn same 5 times
				dataset.addAll(item);

			charset = som2.train(dataset);
			lowres();

			break;
		}
		}
	}

	protected SOMDataset<BitVector> getChargenHires() {
		final SOMDataset<BitVector> dataset = new SOMDataset<BitVector>();

		// tiles screen and pattern
		final int work[] = new int[64 * 3];

		// calculate average
		int nr = 0, ng = 0, nb = 0;
		final int occurrence[] = new int[16];

		for (int i = 0; i < pixels.length; i += 3) {
			nr = pixels[i + 0] & 0xff;
			ng = pixels[i + 1] & 0xff;
			nb = pixels[i + 2] & 0xff;

			occurrence[getColorIndex(nr, ng, nb)]++;
		}

		// get background color with maximum occurrence
		int k = 0;
		float count = occurrence[0] * 256;
		
		for (int i = 1; i < 16; i++) {
			final float o = occurrence[i] * (255f - Gfx.getLuma(palette[i][0], palette[i][1], palette[2][2]));
			if (count < o) {
				count = o;
				k = i;
			}
		}
		
		// most occurrence color as background
		nr = palette[k][0];
		ng = palette[k][1];
		nb = palette[k][2];

		final float backLuma = Gfx.getLuma(nr, ng, nb);

		for (int y = 0; y < 184; y += 8) {
			final int p = y * 176 * 3;

			for (int x = 0; x < 176; x += 8) {
				final int offset = p + x * 3;

				int index = 0, f = 0;
				float maxDistance = 0;

				// pickup most distant color in 8x8 tile
				for (int y0 = 0; y0 < 8; y0++) {
					for (int x0 = 0; x0 < 24; x0 += 3) {
						final int position = offset + y0 * 176 * 3 + x0;

						final int r = pixels[position + 0] & 0xff;
						final int g = pixels[position + 1] & 0xff;
						final int b = pixels[position + 2] & 0xff;

						work[index++] = r;
						work[index++] = g;
						work[index++] = b;

						final float distance = Math.abs(Gfx.getLuma(r, g, b) - backLuma);
						if (maxDistance < distance) {
							maxDistance = distance;

							if (colorAlg == NEAREST_COLOR.MAHALANOBIS)
								f = Gfx.getMahalanobisColorIndex(foregroundPalette, coefficients, r, g, b);
							else
								f = Gfx.getColorIndex(colorAlg, foregroundPalette, r, g, b);
						}
					}
				}

				// foreground color
				final int cf[] = foregroundPalette[f];
				final int fr = cf[0];
				final int fg = cf[1];
				final int fb = cf[2];

				final BitVector vec = new BitVector(64);

				for (int y0 = 0; y0 < 8; y0++)
					for (int x0 = 0; x0 < 8; x0++) {
						final int pyx0 = y0 * 24 + x0 * 3;

						final int r = work[pyx0];
						final int g = work[pyx0 + 1];
						final int b = work[pyx0 + 2];

						final float df;
						final float db;

						// fore or background color?
						df = getDistance(r, g, b, fr, fg, fb);
						db = getDistance(r, g, b, nr, ng, nb);

						// ones as color of the bright pixels
						if (df <= db)
							vec.set(y0 * 8 + x0);
					}

				dataset.add(vec);
			}
		}

		return dataset;
	}

	protected void hires() {
		// tiles screen and pattern
		final int work[] = new int[64 * 3];

		int nr = 0, ng = 0, nb = 0, count = 0;
		final int occurrence[] = new int[16];

		for (int i = 0; i < pixels.length; i += 3) {
			nr = pixels[i] & 0xff;
			ng = pixels[i + 1] & 0xff;
			nb = pixels[i + 2] & 0xff;

			// dimmer better
			occurrence[getColorIndex(nr, ng, nb)] += (int) Math.round(255f - Gfx.getLuma(nr, ng, nb));
		}

		// get background color with maximum occurrence
		int k = 0;
		for (int i = 0; i < 16; i++) {
			final int o = occurrence[i];
			if (count < o) {
				count = o;
				k = i;
			}
		}

		// most occurrence color as background
		backgroundColor = k;

		nr = palette[backgroundColor][0];
		ng = palette[backgroundColor][1];
		nb = palette[backgroundColor][2];

		final float backLuma = Gfx.getLuma(nr, ng, nb);
		final BitVector vec = new BitVector(64);

		for (int y = 0; y < 184; y += 8) {
			final int p = y * 176 * 3;

			for (int x = 0; x < 176; x += 8) {
				final int offset = p + x * 3;

				int index = 0, f = 0;
				float maxDistance = 0;

				// pickup brightest color in 8x8 tile
				for (int y0 = 0; y0 < 8; y0++) {
					for (int x0 = 0; x0 < 24; x0 += 3) {
						final int position = offset + y0 * 176 * 3 + x0;

						final int r = pixels[position] & 0xff;
						final int g = pixels[position + 1] & 0xff;
						final int b = pixels[position + 2] & 0xff;

						work[index++] = r;
						work[index++] = g;
						work[index++] = b;

						final float distance = Math.abs(Gfx.getLuma(r, g, b) - backLuma);
						if (maxDistance < distance) {
							maxDistance = distance;

							if (colorAlg == NEAREST_COLOR.MAHALANOBIS)
								f = Gfx.getMahalanobisColorIndex(foregroundPalette, coefficients, r, g, b);
							else
								f = Gfx.getColorIndex(colorAlg, foregroundPalette, r, g, b);
						}
					}
				}

				// foreground color
				final int cf[] = foregroundPalette[f];
				final int fr = cf[0];
				final int fg = cf[1];
				final int fb = cf[2];

				vec.clear();
				for (int y0 = 0; y0 < 8; y0++)
					for (int x0 = 0; x0 < 8; x0++) {
						final int pyx0 = y0 * 24 + x0 * 3;

						final int r = work[pyx0];
						final int g = work[pyx0 + 1];
						final int b = work[pyx0 + 2];

						final float df;
						final float db;

						df = getDistance(r, g, b, fr, fg, fb);
						db = getDistance(r, g, b, nr, ng, nb);

						if (df <= db)
							vec.set(y0 * 8 + x0);
					}

				final Position pos = som1.getBMU(vec);
				final int code = pos.y * 16 + pos.x;

				// colors
				final int address = (y >> 3) * 22 + (x >> 3);
				nibble[address] = f;
				screen[address] = code;

				// draw character
				for (int y0 = 0; y0 < 8; y0++) {
					final int charByte = charset[code * 8 + y0];

					for (int x0 = 0; x0 < 8; x0++) {
						final int bitValue = power2[x0];
						final int screenPos = offset + y0 * 176 * 3 + x0 * 3;

						if ((charByte & bitValue) == bitValue) {
							pixels[screenPos] = (byte) fr;
							pixels[screenPos + 1] = (byte) fg;
							pixels[screenPos + 2] = (byte) fb;
						} else {
							pixels[screenPos] = (byte) nr;
							pixels[screenPos + 1] = (byte) ng;
							pixels[screenPos + 2] = (byte) nb;
						}
					}
				}
			}
		}
	}

	protected void shrink88(final int newPixels[], final int occurrence[]) {
		int index = 0;

		// shrinking 176x184 -> 88x184
		for (int y = 0; y < 184; y++) {
			final int p = y * 176 * 3;

			for (int x = 0; x < 176; x += 2) {
				final int o = p + x * 3;

				final int r1 = pixels[o + 0] & 0xff;
				final int g1 = pixels[o + 1] & 0xff;
				final int b1 = pixels[o + 2] & 0xff;

				final int r2 = pixels[o + 3] & 0xff;
				final int g2 = pixels[o + 4] & 0xff;
				final int b2 = pixels[o + 5] & 0xff;

				// average color
				final int r = (r1 + r2) >> 1;
				final int g = (g1 + g2) >> 1;
				final int b = (b1 + b2) >> 1;

				final int i;
				if (colorAlg == NEAREST_COLOR.MAHALANOBIS)
					i = Gfx.getMahalanobisColorIndex(foregroundPalette, coefficients, r, g, b);
				else
					i = Gfx.getColorIndex(colorAlg, foregroundPalette, r, g, b);

				final int c[] = foregroundPalette[i];

				newPixels[index++] = c[0];
				newPixels[index++] = c[1];
				newPixels[index++] = c[2];

				occurrence[i]++;
			}
		}
	}

	protected SOMDataset<float[]> getChargenLowres() {
		final SOMDataset<float[]> dataset = new SOMDataset<float[]>();

		Arrays.fill(occurrence, 0);
		shrink88(newPixels88, occurrence);

		Arrays.sort(indexes, new Comparator<Integer>() {
			public int compare(final Integer o1, final Integer o2) {
				return -Integer.compare(occurrence[o1], occurrence[o2]);
			}
		});

		final int c0 = indexes[0];
		final int c1 = indexes[1];
		final int c2 = indexes[2];
		final int c3 = indexes[3];

		backgroundColor = c0;
		borderColor = c1;
		auxiliaryColor = c3;

		locPalette = new int[][] { { foregroundPalette[c0][0], foregroundPalette[c0][1], foregroundPalette[c0][2] },
				{ foregroundPalette[c1][0], foregroundPalette[c1][1], foregroundPalette[c1][2] },
				{ foregroundPalette[c2][0], foregroundPalette[c2][1], foregroundPalette[c2][2] },
				{ foregroundPalette[c3][0], foregroundPalette[c3][1], foregroundPalette[c3][2] } };

		for (int y = 0; y < 184; y += 8)
			for (int x = 0; x < 88; x += 4) {
				final int offset = (y * 88 + x) * 3;
				final float vec[] = new float[32];

				for (int y0 = 0; y0 < 8; y0++)
					for (int x0 = 0; x0 < 4; x0++) {
						final int py0x0 = offset + 3 * (y0 * 88 + x0);

						final int r = newPixels88[py0x0 + 0];
						final int g = newPixels88[py0x0 + 1];
						final int b = newPixels88[py0x0 + 2];

						final int color;

						if (colorAlg == NEAREST_COLOR.MAHALANOBIS)
							color = Gfx.getMahalanobisColorIndex(locPalette, coefficients, r, g, b);
						else
							color = Gfx.getColorIndex(colorAlg, locPalette, r, g, b);

						vec[y0 * 4 + x0] = color & 0x3;
					}

				dataset.add(vec);
			}

		return dataset;
	}

	protected void lowres() {
		final int c0 = indexes[0];
		final int c1 = indexes[1];
		final int c2 = indexes[2];
		final int c3 = indexes[3];

		backgroundColor = c0;
		borderColor = c1;
		auxiliaryColor = c3;

		final float vec[] = new float[32];
		int index = 0;

		for (int y = 0; y < 184; y += 8)
			for (int x = 0; x < 88; x += 4) {
				final int offset = 3 * (y * 88 + x);

				for (int y0 = 0; y0 < 8; y0++)
					for (int x0 = 0; x0 < 4; x0++) {
						final int py0x0 = offset + 3 * (y0 * 88 + x0);

						final int r = newPixels88[py0x0 + 0];
						final int g = newPixels88[py0x0 + 1];
						final int b = newPixels88[py0x0 + 2];

						final int color;
						if (colorAlg == NEAREST_COLOR.MAHALANOBIS)
							color = Gfx.getMahalanobisColorIndex(locPalette, coefficients, r, g, b);
						else
							color = Gfx.getColorIndex(colorAlg, locPalette, r, g, b);

						vec[y0 * 4 + x0] = color & 0x3;
					}

				final Position pos = som2.getBMU(vec);
				final int code = pos.y * 16 + pos.x;

				nibble[index] = c2 + 8; // multicolor mode
				screen[index] = code;
				index++;
			}

		// draw image
		index = 0;
		for (int y0 = 0; y0 < 184; y0 += 8)
			for (int x0 = 0; x0 < 176; x0 += 8) {
				final int offset = (y0 * 176 + x0) * 3;
				final int code = screen[index++];

				for (int y = 0; y < 8; y++) {
					int b = charset[code * 8 + y] & 0xff;

					for (int x = 0; x < 24; x += 6) {
						final int color = (b & 192) >> 6;
						final int p = offset + (y * 176 * 3 + x);

						final int lp[] = locPalette[color];

						pixels[p + 0] = (byte) lp[0];
						pixels[p + 1] = (byte) lp[1];
						pixels[p + 2] = (byte) lp[2];

						pixels[p + 3] = (byte) lp[0];
						pixels[p + 4] = (byte) lp[1];
						pixels[p + 5] = (byte) lp[2];

						b <<= 2;
					}
				}
			}
	}

	@Override
	protected int getColorBitDepth() {
		switch (config.dither_alg) {
		case BLUE16x16, BLUE8x8:
			return 3;
		default:
			return 2;
		}
	}

	@Override
	public void notifyProgress(final String msg) {
		System.out.println(msg);
	}
}
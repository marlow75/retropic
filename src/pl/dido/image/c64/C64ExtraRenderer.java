package pl.dido.image.c64;

import java.awt.image.BufferedImage;
import java.util.Arrays;

import pl.dido.image.renderer.AbstractRenderer;
import pl.dido.image.utils.C64PaletteCalculator;
import pl.dido.image.utils.Config.NEAREST_COLOR;
import pl.dido.image.utils.Gfx;

public class C64ExtraRenderer extends AbstractRenderer {

	protected final static int maxPosition = 320 * 200 * 3;

	protected final int bitmap1[] = new int[40 * 200];
	protected final int screen1[] = new int[1000];

	protected final int bitmap2[] = new int[40 * 200];
	protected final int screen2[] = new int[1000];

	protected final int nibbles[] = new int[1000];
	protected final int lumas[];

	protected int blend[][];
	protected int machinePalette[][];

	protected int backgroundColor1;
	protected int backgroundColor2;

	protected int lumaThreshold;

	public C64ExtraRenderer(final BufferedImage image, final C64ExtraConfig config) {
		super(image, config);

		lumas = C64PaletteCalculator.lumas;
		lumaThreshold = ((C64ExtraConfig) config).luma_threshold;
	}

	@Override
	protected void setupPalette() {
		palette = new int[136][3];
		blend = new int[136][2];

		machinePalette = C64PaletteCalculator.getCalculatedPalette();
		final int len = machinePalette.length;
		int index = 0;

		// get mixed colors
		for (int i = 0; i < len; i++) {
			final float l1 = lumas[i];

			for (int j = i; j < len; j++) {
				final float l2 = lumas[j];

				if (Math.abs(l2 - l1) <= lumaThreshold) {
					final int color1[] = machinePalette[i];
					final int color2[] = machinePalette[j];

					final int color[] = palette[index];

					color[0] = (int) ((color1[0] + color2[0]) >> 1);
					color[1] = (int) ((color1[1] + color2[1]) >> 1);
					color[2] = (int) ((color1[2] + color2[2]) >> 1);

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
			hires();
			break;
		case MULTI_COLOR_INTERLACED:
			multicolor();
			verifyMCI();
			break;
		}
	}
	
	protected int getBlendedColorIndex(final int tilePalette[][], final int tileColors[], final int r, final int g,
			final int b, final int prevColorIndex) {
		float min = Float.MAX_VALUE;
		int mp[] = machinePalette[prevColorIndex];

		final int r0 = mp[0];
		final int g0 = mp[1];
		final int b0 = mp[2];

		int index = -1, len = tilePalette.length;

		for (int i = 0; i < len; i++) {
			final int color = tileColors[i];
			final int c[] = machinePalette[color];

			final int r1 = (c[0] + r0) >> 1;
			final int g1 = (c[1] + g0) >> 1;
			final int b1 = (c[2] + b0) >> 1;

			final float dist = Gfx.euclideanDistance(r, g, b, r1, g1, b1);
			if (dist < min) {
				min = dist;
				index = i;
			}
		}

		if (index == -1)
			return Gfx.getColorIndex(NEAREST_COLOR.EUCLIDEAN, tilePalette, r0, g0, b0);

		return index;
	}

	protected void hires() {
		final int work[] = Gfx.copy2Int(pixels);
		int r, g, b, bitmapIndex = 0;

		int f = 0, n = 0;

		// get average color
		final int tilePalette[][] = new int[3][3];

		for (int y = 0; y < 200; y += 8) {
			final int p = y * 320 * 3;

			for (int x = 0; x < 320; x += 8) {
				final int offset = p + x * 3;

				final int data[] = new int[64 * 3];
				int index = 0;

				// 8x8 tile data
				for (int y0 = 0; y0 < 8; y0++) {
					final int k = offset + y0 * 320 * 3;

					for (int x0 = 0; x0 < 24; x0 += 3) {
						final int position = k + x0;

						data[index++] = work[position];
						data[index++] = work[position + 1];
						data[index++] = work[position + 2];
					}
				}

				final int colors[];

				switch (((C64ExtraConfig) config).rgb_approximation) {
				case CUBE:
					colors = Gfx.get2RGBCubeColor(colorAlg, data, machinePalette);
					break;
				default:
					colors = Gfx.get2RGBLinearColor(colorAlg, data, machinePalette);
					break;
				}

				f = colors[0];
				n = colors[1];

				final int cf[] = machinePalette[f];
				int cn[] = machinePalette[n];

				r = (cf[0] + cn[0]) >> 1;
				g = (cf[1] + cn[1]) >> 1;
				b = (cf[2] + cn[2]) >> 1;

				tilePalette[0] = palette[getColorIndex(r, g, b)]; // blended color
				tilePalette[1] = cf;
				tilePalette[2] = cn;

				final int address = (y >> 3) * 40 + (x >> 3);

				screen1[address] = ((f & 0xf) << 4) | (n & 0xf);
				screen2[address] = ((n & 0xf) << 4) | (f & 0xf);

				int value1 = 0, value2 = 0, bitcount = 0;

				for (int y0 = 0; y0 < 8; y0++) {
					final int k = offset + y0 * 320 * 3;

					for (int x0 = 0; x0 < 24; x0 += 3) {
						final int position = k + x0;

						r = work[position];
						g = work[position + 1];
						b = work[position + 2];

						final int color = Gfx.getColorIndex(colorAlg, tilePalette, Gfx.saturate(r), Gfx.saturate(g),
								Gfx.saturate(b));
						cn = tilePalette[color];

						final int nr = cn[0];
						final int ng = cn[1];
						final int nb = cn[2];

						int v1 = 0, v2 = 0;
						switch (color) {
						case 0:
							if (y0 % 2 == 0) {
								v1 = 0;
								v2 = 0;
							} else {
								v1 = 1;
								v2 = 1;
							}
							break;
						case 1:
							v1 = 1;
							v2 = 0;
							break;
						case 2:
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

						pixels[position] = (byte) nr;
						pixels[position + 1] = (byte) ng;
						pixels[position + 2] = (byte) nb;

						work[position] = nr;
						work[position + 1] = ng;
						work[position + 2] = nb;
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

				final int tileColors1[] = new int[] { backgroundColor1, screen1[address] >> 4, screen1[address] & 0xf,
						nibbles[address] & 0xf };
				final int tileColors2[] = new int[] { backgroundColor2, screen2[address] >> 4, screen2[address] & 0xf,
						nibbles[address] & 0xf };

				int tileColors[];
				int even = 0;

				// 8x8 tile work copy
				for (int y0 = 0; y0 < 8; y0++) {
					int bitcount = 6;

					for (int x0 = 0; x0 < 24; x0 += 3) {
						final int colorIndex;

						if (even == 0) {
							colorIndex = (bitmap1[bitmapIndex] >> bitcount) & 0b11;
							tileColors = tileColors1;
						} else {
							colorIndex = (bitmap2[bitmapIndex] >> bitcount) & 0b11;
							bitcount -= 2;
							tileColors = tileColors2;
						}

						even ^= 1;

						int mp[] = machinePalette[tileColors[colorIndex]];

						r = mp[0];
						g = mp[1];
						b = mp[2];

						if (prevColorIndex[y0] != -1) {
							mp = machinePalette[prevColorIndex[y0]];

							r1 = mp[0];
							g1 = mp[1];
							b1 = mp[2];

							r = (r + r1) >> 1;
							g = (g + g1) >> 1;
							b = (b + b1) >> 1;
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

	protected void multicolor() {
		int bitmapIndex = 0;
		final int work[] = Gfx.copy2Int(pixels);

		int r = 0, g = 0, b = 0;

		// calculate average of colors
		for (int y = 0; y < 200; y++) {
			final int k = y * 320 * 3;

			for (int x = 0; x < 320; x++) {
				final int position = k + x * 3;

				r += work[position];
				g += work[position + 1];
				b += work[position + 2];
			}
		}

		r /= 64000;
		g /= 64000;
		b /= 64000;

		// get background color
		final int color = Gfx.getColorIndex(colorAlg, palette, r, g, b);
		backgroundColor1 = blend[color][0];
		backgroundColor2 = blend[color][1];

		int mp[] = machinePalette[backgroundColor1];

		final int br1 = mp[0];
		final int bg1 = mp[1];
		final int bb1 = mp[2];

		mp = machinePalette[backgroundColor2];

		final int br2 = mp[0];
		final int bg2 = mp[1];
		final int bb2 = mp[2];

		final byte trainData[] = new byte[(64 + 8) * 3];

		for (int y = 0; y < 200; y += 8) {
			final int p = y * 320 * 3;

			// reset previous colors
			final int prevColorIndex[] = new int[8];
			Arrays.fill(prevColorIndex, -1);

			for (int x = 0; x < 320; x += 8) {
				final int offset = p + x * 3;
				int index = 0, colorIndex;

				// get most fitted tile colors
				for (int y0 = 0; y0 < 8; y0++) {
					final int k = offset + y0 * 320 * 3;

					for (int x0 = 0; x0 < 24; x0 += 3) {
						final int position = k + x0;

						// colors from 136 color palette
						trainData[index++] = pixels[position];
						trainData[index++] = pixels[position + 1];
						trainData[index++] = pixels[position + 2];
					}
				}

				if (prevColorIndex[0] != -1)
					for (int i = 0; i < 8; i++) {
						r = machinePalette[prevColorIndex[i]][0];
						g = machinePalette[prevColorIndex[i]][1];
						b = machinePalette[prevColorIndex[i]][2];

						trainData[index++] = (byte) r;
						trainData[index++] = (byte) g;
						trainData[index++] = (byte) b;
					}
				else
					for (int i = 0; i < 8; i++) {
						trainData[index++] = trainData[0];
						trainData[index++] = trainData[1];
						trainData[index++] = trainData[2];
					}

				// map all 4 colors
				int tilePalette[][] = Gfx.getTile4x4Palette(trainData);

				final int tilePalette1[][] = new int[4][3];
				final int tilePalette2[][] = new int[4][3];

				final int tileColors1[] = new int[4];
				final int tileColors2[] = new int[4];

				int tileColors[];

				// get blend colors
				for (int i = 0; i < 4; i++) {
					final int c[] = tilePalette[i];
					colorIndex = getColorIndex(c[0], c[1], c[2]);

					tileColors1[i] = blend[colorIndex][0];
					tileColors2[i] = blend[colorIndex][1];

					mp = machinePalette[tileColors1[i]];

					tilePalette1[i][0] = mp[0];
					tilePalette1[i][1] = mp[1];
					tilePalette1[i][2] = mp[2];

					mp = machinePalette[tileColors2[i]];

					tilePalette2[i][0] = mp[0];
					tilePalette2[i][1] = mp[1];
					tilePalette2[i][2] = mp[2];
				}

				// map background color to the nearest color tile palette
				colorIndex = Gfx.getColorIndex(colorAlg, tilePalette1, br1, bg1, bb1);
				if (colorIndex != 0) {
					mp = tilePalette1[0];

					tilePalette1[colorIndex][0] = mp[0]; // first entry = background color
					tilePalette1[colorIndex][1] = mp[1];
					tilePalette1[colorIndex][2] = mp[2];

					tilePalette1[0][0] = br1; // background color
					tilePalette1[0][1] = bg1;
					tilePalette1[0][2] = bb1;

					tileColors1[colorIndex] = tileColors1[0];
					tileColors1[0] = backgroundColor1;
				}

				// map background color to the nearest color tile palette
				colorIndex = Gfx.getColorIndex(colorAlg, tilePalette2, br2, bg2, bb2);
				if (colorIndex != 0) {
					mp = tilePalette2[0];

					tilePalette2[colorIndex][0] = mp[0]; // first entry = background color
					tilePalette2[colorIndex][1] = mp[1];
					tilePalette2[colorIndex][2] = mp[2];

					tilePalette2[0][0] = br2; // background color
					tilePalette2[0][1] = bg2;
					tilePalette2[0][2] = bb2;

					tileColors2[colorIndex] = tileColors2[0];
					tileColors2[0] = backgroundColor2;
				}

				mp = tilePalette1[3];
				// map 3 color to the nearest color tile palette nr2
				colorIndex = Gfx.getColorIndex(colorAlg, tilePalette2, mp[0], mp[1], mp[2]);
				if (colorIndex != 3) {
					mp = tilePalette2[3];

					tilePalette2[colorIndex][0] = mp[0]; // swap colors
					tilePalette2[colorIndex][1] = mp[1];
					tilePalette2[colorIndex][2] = mp[2];

					mp = tilePalette1[3];

					tilePalette2[3][0] = mp[0]; // background color
					tilePalette2[3][1] = mp[1];
					tilePalette2[3][2] = mp[2];

					tileColors2[colorIndex] = tileColors2[3];
					tileColors2[3] = tileColors1[3];
				}

				final int address = (y >> 3) * 40 + (x >> 3);

				// map tile colors to screen palette
				final int c01s1 = tileColors1[1] << 4;
				final int c10s1 = tileColors1[2];
				final int c11s1 = tileColors1[3];

				// map tile colors to screen palette
				final int c01s2 = tileColors2[1] << 4;
				final int c10s2 = tileColors2[2];

				screen1[address] = c01s1 | c10s1;
				screen2[address] = c01s2 | c10s2;
				nibbles[address] = c11s1;

				int even = 0, value1 = 0, value2 = 0, bitcount = 0;

				for (int y0 = 0; y0 < 8; y0++) {
					final int k = offset + y0 * 320 * 3;

					for (int x0 = 0; x0 < 24; x0 += 3) {
						final int position = k + x0;

						r = work[position];
						g = work[position + 1];
						b = work[position + 2];

						int nr, ng, nb;

						if (even == 0) {
							tilePalette = tilePalette1;
							tileColors = tileColors1;
						} else {
							tilePalette = tilePalette2;
							tileColors = tileColors2;
						}

						if (prevColorIndex[y0] != -1) { // prevColorIndex is absolute, not local tilePalette index
							colorIndex = getBlendedColorIndex(tilePalette, tileColors, r, g, b, prevColorIndex[y0]);

							// new color as old and new combined
							final int p1[] = tilePalette[colorIndex];
							final int p2[] = machinePalette[prevColorIndex[y0]];

							nr = (p1[0] + p2[0]) >> 1;
							ng = (p1[1] + p2[1]) >> 1;
							nb = (p1[2] + p2[2]) >> 1;
						} else {
							colorIndex = Gfx.getColorIndex(colorAlg, tilePalette, r, g, b);
							final int p1[] = tilePalette[colorIndex];

							nr = p1[0];
							ng = p1[1];
							nb = p1[2];
						}

						prevColorIndex[y0] = tileColors[colorIndex];

						if (even == 0)
							value1 = (value1 << 2) | (colorIndex & 0b11);
						else
							value2 = (value2 << 2) | (colorIndex & 0b11);

						even ^= 1;
						bitcount += 1;

						if (bitcount == 8) {
							bitmap1[bitmapIndex] = value1;
							bitmap2[bitmapIndex] = value2;

							value1 = value2 = 0;
							bitcount = 0;

							bitmapIndex++;
						}

						pixels[position] = (byte) nr;
						pixels[position + 1] = (byte) ng;
						pixels[position + 2] = (byte) nb;

						work[position] = nr;
						work[position + 1] = ng;
						work[position + 2] = nb;
					}
				}
			}
		}
	}
}
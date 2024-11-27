package pl.dido.image.Plus4;

import java.awt.image.BufferedImage;
import java.util.Arrays;

import pl.dido.image.renderer.AbstractRenderer;
import pl.dido.image.utils.Config.NEAREST_COLOR;
import pl.dido.image.utils.Gfx;
import pl.dido.image.utils.neural.SOMPalette;

public class Plus4ExtraRenderer extends AbstractRenderer {

	protected final static int maxPosition = 320 * 200 * 3;

	protected final int bitmap1[] = new int[40 * 200];
	protected final int screen1[] = new int[1000];

	protected final int bitmap2[] = new int[40 * 200];
	protected final int screen2[] = new int[1000];

	protected final int nibble1[] = new int[1000];
	protected final int nibble2[] = new int[1000];

	protected int blend[][];
	protected int machinePalette[][];

	protected int backgroundColor1;
	protected int backgroundColor2;

	protected int lumaThreshold;

	// plus4 palette
	private final static int colors[] = new int[] { 0x000000, 0x171717, 0x46070a, 0x002a26, 0x3e0246, 0x003300,
			0x0f0d70, 0x1f2100, 0x3e0e00, 0x301700, 0x0f2b00, 0x460326, 0x00310a, 0x031761, 0x1f0770, 0x033100,
			0x000000, 0x262626, 0x591417, 0x013b37, 0x510c59, 0x054501, 0x1e1c85, 0x303200, 0x511c01, 0x422700,
			0x1e3c00, 0x590e37, 0x014217, 0x0f2675, 0x301385, 0x0f4300, 0x000000, 0x373737, 0x6d2327, 0x0c4e49,
			0x641b6d, 0x12580c, 0x2e2c9b, 0x414400, 0x642c0c, 0x553800, 0x2e4e00, 0x6d1d49, 0x0c5527, 0x1d378a,
			0x41229b, 0x1d5600, 0x000000, 0x4a4a4a, 0x813338, 0x1a615d, 0x792a82, 0x206c1a, 0x3f3db1, 0x545700,
			0x793d1a, 0x684a07, 0x3f6200, 0x812d5d, 0x1a6938, 0x2d49a0, 0x5433b1, 0x2d6907, 0x000000, 0x7b7b7b,
			0xb86267, 0x449690, 0xaf58b9, 0x4ca144, 0x706deb, 0x878a1f, 0xaf6e44, 0x9d7c2b, 0x70961f, 0xb85a90,
			0x449e67, 0x5b7bd9, 0x8762eb, 0x5b9e2b, 0x000000, 0x9b9b9b, 0xdb8186, 0x61b7b1, 0xd176dc, 0x69c360,
			0x8f8cff, 0xa8ab38, 0xd18d60, 0xbf9c45, 0x8fb738, 0xdb79b1, 0x61c086, 0x799bfd, 0xa880ff, 0x79c045,
			0x000000, 0xe0e0e0, 0xffc3c9, 0xa0fef8, 0xffb7ff, 0xa9ff9f, 0xd3d0ff, 0xedf171, 0xffd19f, 0xffe081,
			0xd3fe71, 0xffbaf8, 0xa0ffc9, 0xbbe0ff, 0xedc3ff, 0xbbff81, 0x000000, 0xffffff, 0xffffff, 0xfdffff,
			0xffffff, 0xfffffd, 0xffffff, 0xffffc9, 0xfffffd, 0xffffdb, 0xffffc9, 0xffffff, 0xfdffff, 0xffffff,
			0xffffff, 0xffffdb };

	public Plus4ExtraRenderer(final BufferedImage image, final Plus4ExtraConfig config) {
		super(image, config);
		lumaThreshold = ((Plus4ExtraConfig) config).luma_threshold;
	}
	
	@Override
	protected void setupPalette() {
		machinePalette = new int[128][3];
		final int len = machinePalette.length;

		palette = new int[16384][3];
		blend = new int[16384][2];
		
		final int lumas[] = new int[len];

		for (int i = 0; i < colors.length; i++) {
			machinePalette[i][0] = (colors[i] & 0x0000ff); // blue
			machinePalette[i][1] = (colors[i] & 0x00ff00) >> 8; // green
			machinePalette[i][2] = (colors[i] & 0xff0000) >> 16; // red
			lumas[i] = (int)Gfx.getLuma(machinePalette[i][0], machinePalette[i][1], machinePalette[i][2]);
		}

		int index = 0;
		final int lt = lumaThreshold * 256 / 32;
		// get mixed colors
		for (int i = 0; i < len; i++) {
			final float l1 = lumas[i];

			for (int j = i; j < len; j++) {
				final float l2 = lumas[j];

				if (Math.abs(l2 - l1) <= lt) {
					final int color1[] = machinePalette[i];
					final int color2[] = machinePalette[j];

					final int color[] = palette[index];

					color[0] = (color1[0] + color2[0]) >> 1;
					color[1] = (color1[1] + color2[1]) >> 1;
					color[2] = (color1[2] + color2[2]) >> 1;

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
		switch (((Plus4ExtraConfig) config).extra_mode) {
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

				switch (((Plus4ExtraConfig) config).rgb_approximation) {
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
				
				final int lf = f / 16;
				final int ln = n / 16;

				final int pf = f - lf * 16;
				final int pn = n - ln * 16;

				screen1[address] = ((pf & 0xf) << 4) | (pn & 0xf); // f
				screen2[address] = ((pn & 0xf) << 4) | (pf & 0xf);
				
				nibble1[address] = ((ln & 0xf) << 4) | (lf & 0xf);
				nibble2[address] = ((lf & 0xf) << 4) | (ln & 0xf);

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

				int q = screen1[address];
				int l = nibble1[address];

				int c00 = (backgroundColor1 >> 4) * 16 + (backgroundColor1 & 0xf);
				int c11 = (backgroundColor2 >> 4) * 16 + (backgroundColor2 & 0xf);

				int c01 = (l & 0xf) * 16 + (q >> 4);
				int c10 = (l >> 4) * 16 + (q & 0xf);

				final int tileColors1[] = new int[] { c00, c01, c10, c11 };

				q = screen2[address];
				l = nibble2[address];

				c01 = (l & 0xf) * 16 + (q >> 4);
				c10 = (l >> 4) * 16 + (q & 0xf);

				final int tileColors2[] = new int[] { c00, c01, c10, c11 };

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

		int r, g, b;
		
		final SOMPalette som = new SOMPalette(2, 1, 0.8f, 0.5f, 20);
		final int colors[][] = som.train(pixels);
		
		final int bck1 = Gfx.getColorIndex(colorAlg, machinePalette, colors[0][0], colors[0][1], colors[0][2]);
		final int bck2 = Gfx.getColorIndex(colorAlg, machinePalette, colors[1][0], colors[1][1], colors[1][2]);

		// get background color
		int l = bck1 / 16;
		int q = bck1 - l * 16;
		backgroundColor1 = ((l & 0xf) << 4) | (q & 0xf);

		l = bck2 / 16;
		q = bck2 - l * 16;
		backgroundColor2 = ((l & 0xf) << 4) | (q & 0xf);

		int mp[] = machinePalette[bck1];
		final int br1 = mp[0];
		final int bg1 = mp[1];
		final int bb1 = mp[2];

		mp = machinePalette[bck2];
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
					tileColors1[0] = bck1;
				}

				// map background color to the nearest color tile palette
				colorIndex = Gfx.getColorIndex(colorAlg, tilePalette2, br1, bg1, bb1);
				if (colorIndex != 0) {
					mp = tilePalette2[0];

					tilePalette2[colorIndex][0] = mp[0]; // first entry = background color
					tilePalette2[colorIndex][1] = mp[1];
					tilePalette2[colorIndex][2] = mp[2];

					tilePalette2[0][0] = br1; // background color
					tilePalette2[0][1] = bg1;
					tilePalette2[0][2] = bb1;

					tileColors2[colorIndex] = tileColors2[0];
					tileColors2[0] = bck1;
				}

				// map 3 color palette1
				colorIndex = Gfx.getColorIndex(colorAlg, tilePalette1, br2, bg2, bb2);
				if (colorIndex != 3) {
					mp = tilePalette1[3];

					tilePalette1[colorIndex][0] = mp[0]; // swap colors
					tilePalette1[colorIndex][1] = mp[1];
					tilePalette1[colorIndex][2] = mp[2];

					tilePalette1[3][0] = br2; // background color
					tilePalette1[3][1] = bg2;
					tilePalette1[3][2] = bb2;

					tileColors1[colorIndex] = tileColors1[3];
					tileColors1[3] = bck2;
				}

				// map 3 color palette2
				colorIndex = Gfx.getColorIndex(colorAlg, tilePalette2, br2, bg2, bb2);
				if (colorIndex != 3) {
					mp = tilePalette2[3];

					tilePalette2[colorIndex][0] = mp[0]; // swap colors
					tilePalette2[colorIndex][1] = mp[1];
					tilePalette2[colorIndex][2] = mp[2];

					tilePalette2[3][0] = br2; // background color
					tilePalette2[3][1] = bg2;
					tilePalette2[3][2] = bb2;

					tileColors2[colorIndex] = tileColors2[3];
					tileColors2[3] = bck2;
				}

				final int address = (y >> 3) * 40 + (x >> 3);

				// map tile colors to screen palette
				final int c01s1 = tileColors1[1];
				final int c10s1 = tileColors1[2];

				// map tile colors to screen palette
				final int c01s2 = tileColors2[1];
				final int c10s2 = tileColors2[2];

				int l1 = c01s1 / 16;
				int q1 = c01s1 - l1 * 16;

				int l2 = c10s1 / 16;
				int q2 = c10s1 - l2 * 16;

				// colors
				screen1[address] = ((q1 & 0xf) << 4) | (q2 & 0xf);
				nibble1[address] = ((l2 & 0xf) << 4) | (l1 & 0xf);

				l1 = c01s2 / 16;
				q1 = c01s2 - l1 * 16;

				l2 = c10s2 / 16;
				q2 = c10s2 - l2 * 16;

				screen2[address] = ((q1 & 0xf) << 4) | (q2 & 0xf);
				nibble2[address] = ((l2 & 0xf) << 4) | (l1 & 0xf);

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
package pl.dido.image.plus4;

import java.awt.image.BufferedImage;
import java.util.Arrays;

import pl.dido.image.c64.C64ExtraConfig;
import pl.dido.image.renderer.AbstractRenderer;
import pl.dido.image.utils.ColorBuffer;
import pl.dido.image.utils.Config;
import pl.dido.image.utils.Config.NEAREST_COLOR;
import pl.dido.image.utils.Gfx;
import pl.dido.image.utils.neural.SOMPalette;

public class Plus4ExtraRenderer extends AbstractRenderer {

	protected final static int maxPosition = 320 * 200 * 3;

	protected final int bitmap1[] = new int[40 * 200];
	protected final int screen1[] = new int[1000];

	protected final int bitmap2[] = new int[40 * 200];
	protected final int screen2[] = new int[1000];

	protected final int nibbles1[] = new int[1000];
	protected final int nibbles2[] = new int[1000];

	protected int blend[][];
	protected int machinePalette[][];

	protected int backgroundColor1;
	protected int backgroundColor2;

	protected int lumaThreshold;
	protected float flickeringFactor;

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

		flickeringFactor = ((C64ExtraConfig) config).flickering_factor;
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
			lumas[i] = (int) Gfx.getLuma(machinePalette[i][0], machinePalette[i][1], machinePalette[i][2]);
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
			multicolorFilter();
			verifyMCI();
			break;
		}
	}
	
	protected void multicolorFilter() {
		final int screenBuf[] = new int[1000];
		
		for (int y = 0; y < 25; y++) {
			final int p = y * 40;
			
			screenBuf[p] = screen1[p];
			screenBuf[p + 39] = screen1[p + 39];
		
			for (int x = 1; x < 39; x++) {
				int a1 = p + x - 1;
				int a2 = p + x;
				int a3 = p + x + 1;
				
				int q1 = screen1[a1] >> 4;
				int q2 = screen1[a1] & 0xf;

				int l2 = nibbles1[a1] >> 4;
				int l1 = nibbles1[a1] & 0xf;

				int c001 = q1 + (l1 << 4);
				int c010 = q2 + (l2 << 4);
				
				q1 = screen1[a2] >> 4;
				q2 = screen1[a2] & 0xf;

				l2 = nibbles1[a2] >> 4;
				l1 = nibbles1[a2] & 0xf;
				
				int c101 = q1 + (l1 << 4);
				int c110 = q2 + (l2 << 4);

				q1 = screen1[a3] >> 4;
				q2 = screen1[a3] & 0xf;

				l2 = nibbles1[a3] >> 4;
				l1 = nibbles1[a3] & 0xf;
				
				int c201 = q1 + (l1 << 4);
				int c210 = q2 + (l2 << 4);
				
				int r = (machinePalette[c001][0] + machinePalette[c101][0] + machinePalette[c201][0]) / 3;
				int g = (machinePalette[c001][1] + machinePalette[c101][1] + machinePalette[c201][1]) / 3;
				int b = (machinePalette[c001][2] + machinePalette[c101][2] + machinePalette[c201][2]) / 3;
				
				int newc01 = Gfx.getColorIndex(colorAlg, machinePalette, r, g, b);
				
				r = (machinePalette[c010][0] + machinePalette[c110][0] + machinePalette[c210][0]) / 3;
				g = (machinePalette[c010][1] + machinePalette[c110][1] + machinePalette[c210][1]) / 3;
				b = (machinePalette[c010][2] + machinePalette[c110][2] + machinePalette[c210][2]) / 3;
				
				int newc10 = Gfx.getColorIndex(colorAlg, machinePalette, r, g, b);
				
				l1 = newc01 / 16;
				l2 = newc10 / 16;

				q1 = newc01 - l1 * 16;
				q2 = newc10 - l2 * 16;

				// change only color definition
				screenBuf[a2] = ((q1 & 0xf) << 4) | (q2 & 0xf);
			}
		}
		
		System.arraycopy(screenBuf, 0, screen1, 0, 1000);
		System.arraycopy(screenBuf, 0, screen2, 0, 1000);
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

				nibbles1[address] = ((ln & 0xf) << 4) | (lf & 0xf);
				nibbles2[address] = ((lf & 0xf) << 4) | (ln & 0xf);

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

		final int prevColorIndex[] = new int[8];

		final int tileColors1[] = new int[4];
		final int tileColors2[] = new int[4];

		tileColors1[0] = backgroundColor1;
		tileColors1[3] = backgroundColor2;

		tileColors2[0] = backgroundColor1;
		tileColors2[3] = backgroundColor2;

		for (int y = 0; y < 200; y += 8) {
			final int p = y * 320 * 3;
			Arrays.fill(prevColorIndex, -1);

			for (int x = 0; x < 320; x += 8) {
				final int offset = p + x * 3;
				final int address = (y >> 3) * 40 + (x >> 3);

				final int q1 = screen1[address] >> 4;
				final int q2 = screen1[address] & 0xf;

				final int l2 = nibbles1[address] >> 4;
				final int l1 = nibbles1[address] & 0xf;

				int c01 = q1 + (l1 << 4);
				int c10 = q2 + (l2 << 4);

				tileColors1[1] = c01;
				tileColors1[2] = c10;

				// for simplicity
				tileColors2[1] = c01;
				tileColors2[2] = c10;

				int tileColors[];
				int even = 0;

				// 8x8 tile work copy
				for (int y0 = 0; y0 < 8; y0++) {
					int bitcount = 6;

					for (int x0 = 0; x0 < 24; x0 += 3) {
						final int colorIndex;

						if (even == 0) {
							colorIndex = (bitmap1[bitmapIndex] >> bitcount) & 0x3;
							tileColors = tileColors1;
						} else {
							colorIndex = (bitmap2[bitmapIndex] >> bitcount) & 0x3;
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

		// calculate average color
		for (int y = 0; y < 200; y++) {
			final int k = y * 320 * 3;

			for (int x = 0; x < 320; x++) {
				final int position = k + x * 3;

				r += work[position + 0];
				g += work[position + 1];
				b += work[position + 2];
			}
		}

		r /= 320 * 200;
		g /= 320 * 200;
		b /= 320 * 200;

		int br1 = Gfx.saturate((r * 120) / 100);
		int bg1 = Gfx.saturate((g * 120) / 100);
		int bb1 = Gfx.saturate((b * 120) / 100);

		int br2 = Gfx.saturate((r * 80) / 100);
		int bg2 = Gfx.saturate((g * 80) / 100);
		int bb2 = Gfx.saturate((b * 80) / 100);

		// average as background - common color
		backgroundColor1 = Gfx.getColorIndex(colorAlg, machinePalette, br1, bg1, bb1);
		backgroundColor2 = Gfx.getColorIndex(colorAlg, machinePalette, br2, bg2, bb2);

		br1 = machinePalette[backgroundColor1][0];
		bg1 = machinePalette[backgroundColor1][1];
		bb1 = machinePalette[backgroundColor1][2];

		br2 = machinePalette[backgroundColor2][0];
		bg2 = machinePalette[backgroundColor2][1];
		bb2 = machinePalette[backgroundColor2][2];

		final byte trainData1[] = new byte[64 * 3];
		final byte trainData2[] = new byte[32 * 3];

		final int prevColorIndex[] = new int[8];
		final int tileColors[] = new int[4];

		// 8 pixels in a raw
		final ColorBuffer buf[] = new ColorBuffer[8];
		for (int i = 0; i < 8; i++)
			buf[i] = new ColorBuffer(8);

		for (int y = 0; y < 200; y += 8) {
			final int p = y * 320 * 3;
			Arrays.fill(prevColorIndex, -1);

			// clear line buffer
			for (int i = 0; i < 8; i++)
				buf[i].clear();

			for (int x = 0; x < 320; x += 8) {
				final int offset = p + x * 3;
				int index = 0, colorIndex;

				// get most fitted tile colors
				for (int y0 = 0; y0 < 8; y0++) {
					final int k = offset + y0 * 320 * 3;

					for (int x0 = 0; x0 < 24; x0 += 3) {
						final int position = k + x0;

						trainData1[index++] = pixels[position];
						trainData1[index++] = pixels[position + 1];
						trainData1[index++] = pixels[position + 2];
					}
				}

				// map all 4 colors to extra palette
				SOMPalette som = new SOMPalette(4, 4, 0.8f, 1f, 30);
				int tilePalette[][] = som.train(trainData1);

				// get blend colors
				for (int i = 0; i < 16; i++) {
					final int c[] = tilePalette[i];
					colorIndex = getColorIndex(c[0], c[1], c[2]);

					int q[] = machinePalette[blend[colorIndex][0]];

					trainData2[6 * i] = (byte) q[0];
					trainData2[6 * i + 1] = (byte) q[1];
					trainData2[6 * i + 2] = (byte) q[2];

					q = machinePalette[blend[colorIndex][1]];

					trainData2[6 * i + 3] = (byte) q[0];
					trainData2[6 * i + 4] = (byte) q[1];
					trainData2[6 * i + 5] = (byte) q[2];
				}

				// get machine colors
				som = new SOMPalette(2, 2, 0.8f, 1f, 30);
				tilePalette = som.train(trainData2);

				for (int i = 0; i < 4; i++) {
					final int c[] = tilePalette[i];
					colorIndex = Gfx.getColorIndex(colorAlg, machinePalette, c[0], c[1], c[2]);

					c[0] = machinePalette[colorIndex][0];
					c[1] = machinePalette[colorIndex][1];
					c[2] = machinePalette[colorIndex][2];

					tileColors[i] = colorIndex;
				}

				// map background1 to the nearest color tile palette
				colorIndex = Gfx.getColorIndex(colorAlg, tilePalette, br1, bg1, bb1);
				if (colorIndex != 0) {
					tilePalette[colorIndex][0] = tilePalette[0][0]; // first entry = background color
					tilePalette[colorIndex][1] = tilePalette[0][1];
					tilePalette[colorIndex][2] = tilePalette[0][2];

					tilePalette[0][0] = br1; // background color
					tilePalette[0][1] = bg1;
					tilePalette[0][2] = bb1;

					tileColors[colorIndex] = tileColors[0];
					tileColors[0] = backgroundColor1;
				}

				// map background2 to the nearest color tile palette
				colorIndex = Gfx.getColorIndex(colorAlg, tilePalette, br2, bg2, bb2);
				if (colorIndex != 3) {
					if (colorIndex != 0) {
						tilePalette[colorIndex][0] = tilePalette[3][0]; // first entry = background color
						tilePalette[colorIndex][1] = tilePalette[3][1];
						tilePalette[colorIndex][2] = tilePalette[3][2];

						tileColors[colorIndex] = tileColors[3];
					}

					tilePalette[3][0] = br2; // background color
					tilePalette[3][1] = bg2;
					tilePalette[3][2] = bb2;

					tileColors[3] = backgroundColor2;
				}

				for (int y0 = 0; y0 < 8; y0++) {
					final int k = offset + y0 * 320 * 3;

					for (int x0 = 0; x0 < 24; x0 += 3) {
						final int position = k + x0;

						// get current picture color
						r = Gfx.saturate(work[position]);
						g = Gfx.saturate(work[position + 1]);
						b = Gfx.saturate(work[position + 2]);

						if (prevColorIndex[y0] != -1) // prevColorIndex is absolute, not local tilePalette index
							colorIndex = getBlendedColorIndex(tilePalette, tileColors, r, g, b, prevColorIndex[y0]);
						else
							colorIndex = Gfx.getColorIndex(colorAlg, tilePalette, r, g, b);

						final int color = tileColors[colorIndex];
						buf[y0].write(color);
						prevColorIndex[y0] = color;
					}
				}

				int even = 0, value1 = 0, value2 = 0, bitcount = 0;

				for (int y0 = 0; y0 < 8; y0++) {
					int flickered[] = buf[y0].getBuffer();

					if (flickeringFactor != 0f)
						flickered = getUnflickered(flickered);

					for (int x0 = 0; x0 < 8; x0++) {
						colorIndex = flickered[x0];

						if (colorIndex < 0) {
							colorIndex = -colorIndex - 1;
							colorIndex = Gfx.getColorIndex(colorAlg, machinePalette, palette[colorIndex][0],
									palette[colorIndex][1], palette[colorIndex][2]);

							final int k = Gfx.getColorIndex(colorAlg, new int[][] { tilePalette[1], tilePalette[2] },
									machinePalette[colorIndex][0], machinePalette[colorIndex][1],
									machinePalette[colorIndex][2]);

							tilePalette[k + 1][0] = machinePalette[colorIndex][0];
							tilePalette[k + 1][1] = machinePalette[colorIndex][1];
							tilePalette[k + 1][2] = machinePalette[colorIndex][2];

							tileColors[k + 1] = colorIndex;
						}

						colorIndex = Gfx.getColorIndex(colorAlg, tilePalette, machinePalette[colorIndex][0],
								machinePalette[colorIndex][1], machinePalette[colorIndex][2]);

						if (even == 0)
							value1 = (value1 << 2) | (colorIndex & 0x3);
						else
							value2 = (value2 << 2) | (colorIndex & 0x3);

						even ^= 1;
						bitcount += 1;

						if (bitcount == 8) {
							bitmap1[bitmapIndex] = value1;
							bitmap2[bitmapIndex] = value2;

							value1 = value2 = 0;
							bitcount = 0;

							bitmapIndex++;
						}
					}
				}

				final int address = (y >> 3) * 40 + (x >> 3);

				// map tile colors to screen palette
				final int c01 = tileColors[1];
				final int c10 = tileColors[2];

				final int l1 = c01 / 16;
				final int l2 = c10 / 16;

				final int q1 = c01 - l1 * 16;
				final int q2 = c10 - l2 * 16;

				screen1[address] = ((q1 & 0xf) << 4) | (q2 & 0xf);
				screen2[address] = screen1[address];

				nibbles1[address] = ((l2 & 0xf) << 4) | (l1 & 0xf);
				nibbles2[address] = nibbles1[address];
			}
		}
	}

	public int[] getUnflickered(final int buffer[]) {
		final int p[] = new int[2];
		p[0] = buffer[0];
		p[1] = buffer[1];

		int start = 0, len = 0;
		int end = buffer.length;

		for (int i = 2; i < end; i += 2) {
			final int x1 = buffer[i];
			final int x2 = buffer[i + 1];

			if (x1 == p[0] && x2 == p[1])
				len++;
			else {
				if (len > 0) {
					final int q1[] = machinePalette[p[0]];
					final int q2[] = machinePalette[p[1]];

					final float l1 = Gfx.getLuma(q1[0], q1[1], q1[2]);
					final float l2 = Gfx.getLuma(q2[0], q2[1], q2[2]);

					final float delta = Math.abs(l2 - l1);

					if (delta > lumaThreshold)
						if (delta < flickeringFactor * lumaThreshold) {
							int even = 0;
							for (int j = start; j < start + 2 * (len + 1); j += 2) {
								buffer[j + 0] = p[even];
								even ^= 1;
								buffer[j + 1] = p[even];
							}
						} else {
							final int color = Gfx.getColorIndex(colorAlg, palette, (q1[0] + q2[0]) / 2,
									(q1[1] + q2[1]) / 2, (q1[2] + q2[2]) / 2);
							for (int j = start; j < start + 2 * (len + 1); j++)
								buffer[j] = -color - 1;
						}

					len = 0;
				}

				start = i + 2;
				p[0] = x1;
				p[1] = x2;
			}
		}

		if (len > 0) {
			final int q1[] = machinePalette[p[0]];
			final int q2[] = machinePalette[p[1]];

			final float l1 = Gfx.getLuma(q1[0], q1[1], q1[2]);
			final float l2 = Gfx.getLuma(q2[0], q2[1], q2[2]);

			final float delta = Math.abs(l2 - l1);

			if (delta > lumaThreshold)
				if (delta < flickeringFactor * lumaThreshold) {
					int even = 0;
					for (int j = start; j < end; j += 2) {
						buffer[j + 0] = p[even];
						even ^= 1;
						buffer[j + 1] = p[even];
					}
				} else {
					final int color = Gfx.getColorIndex(colorAlg, palette, (q1[0] + q2[0]) / 2, (q1[1] + q2[1]) / 2,
							(q1[2] + q2[2]) / 2);
					for (int j = start; j < end; j++)
						buffer[j] = -color - 1;
				}
		}

		return buffer;
	}

	@Override
	protected int getGraphicModeColorsNumber(final Config config) {
		switch (config.dither_alg) {
		case NOISE16x16, NOISE8x8:
			return 32;
		default:
			return 16;
		}
	}
}
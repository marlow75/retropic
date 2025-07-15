package pl.dido.image.c64;

import java.awt.image.BufferedImage;
import java.util.Arrays;

import pl.dido.image.renderer.AbstractRenderer;
import pl.dido.image.utils.C64PaletteCalculator;
import pl.dido.image.utils.ColorBuffer;
import pl.dido.image.utils.Config;
import pl.dido.image.utils.Config.NEAREST_COLOR;
import pl.dido.image.utils.Gfx;
import pl.dido.image.utils.neural.SOMPalette;

public class C64ExtraRenderer extends AbstractRenderer {
	protected final static int[] colorRamp = new int[] { 0, 9, 11, 12, 15, 1 };
	protected final static int maxPosition = 320 * 200 * 3;

	protected final int bitmap1[] = new int[40 * 200];
	protected final int screen1[] = new int[1000];

	protected final int bitmap2[] = new int[40 * 200];
	protected final int screen2[] = new int[1000];

	protected final int nibbles[] = new int[1000];
	protected final int lumas[];

	protected int blend[][];
	protected int machinePalette[][];

	protected int backgroundColor;
	protected int lumaThreshold;
	
	protected float flickeringFactor;

	public C64ExtraRenderer(final BufferedImage image, final C64ExtraConfig config) {
		super(image, config);

		lumas = C64PaletteCalculator.lumas;
		lumaThreshold = ((C64ExtraConfig) config).luma_threshold;
		
		flickeringFactor = ((C64ExtraConfig) config).flickering_factor;
	}

	@Override
	protected void setupPalette() {
		palette = new int[136][3];
		blend = new int[136][2];

		machinePalette = C64PaletteCalculator.getCalculatedPalette();
		int index = 0;

		if (!((C64ExtraConfig) config).color_ramp) {
			final int len = machinePalette.length;
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
		} else {
			for (int i : colorRamp) {
				final float l1 = lumas[i];

				for (int j : colorRamp) {
					if (i > j)
						continue;

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
			//hiresFilter();
			break;
		case MULTI_COLOR_INTERLACED:
			multicolor();
			verifyMCI();
			break;
		}
	}

	protected void hiresFilter() {
		final int screen1buf[] = new int[1000];
		final int screen2buf[] = new int[1000];
		
		for (int y = 0; y < 25; y++) {
			final int p = y * 40;
			
			screen1buf[p] = screen1[p];
			screen1buf[p + 39] = screen1[p + 39];
			
			screen2buf[p] = screen2[p];
			screen2buf[p + 39] = screen2[p + 39];
		
			for (int x = 1; x < 39; x++) {
				int a1 = p + x - 1;
				int a2 = p + x;
				int a3 = p + x + 1;
				
				int c1[] = machinePalette[screen1[a1] & 0xf];
				int c2[] = machinePalette[screen1[a2] & 0xf];
				int c3[] = machinePalette[screen1[a3] & 0xf];
				
				int r = (c1[0] + c2[0] + c3[0]) / 3;
				int g = (c1[1] + c2[1] + c3[1]) / 3;
				int b = (c1[2] + c2[2] + c3[2]) / 3;
				
				screen1buf[a2] = Gfx.getColorIndex(colorAlg, machinePalette, r, g, b);
				
				c1 = machinePalette[screen1[a1] >> 4];
				c2 = machinePalette[screen1[a2] >> 4];
				c3 = machinePalette[screen1[a3] >> 4];
				
				r = (c1[0] + c2[0] + c3[0]) / 3;
				g = (c1[1] + c2[1] + c3[1]) / 3;
				b = (c1[2] + c2[2] + c3[2]) / 3;
				
				screen1buf[a2] |= (Gfx.getColorIndex(colorAlg, machinePalette, r, g, b) << 4);
				
				c1 = machinePalette[screen2[a1] & 0xf];
				c2 = machinePalette[screen2[a2] & 0xf];
				c3 = machinePalette[screen2[a3] & 0xf];
				
				r = (c1[0] + c2[0] + c3[0]) / 3;
				g = (c1[1] + c2[1] + c3[1]) / 3;
				b = (c1[2] + c2[2] + c3[2]) / 3;
				
				screen2buf[a2] = Gfx.getColorIndex(colorAlg, machinePalette, r, g, b);
				
				c1 = machinePalette[screen2[a1] >> 4];
				c2 = machinePalette[screen2[a2] >> 4];
				c3 = machinePalette[screen2[a3] >> 4];
				
				r = (c1[0] + c2[0] + c3[0]) / 3;
				g = (c1[1] + c2[1] + c3[1]) / 3;
				b = (c1[2] + c2[2] + c3[2]) / 3;
				
				screen2buf[a2] |= (Gfx.getColorIndex(colorAlg, machinePalette, r, g, b) << 4);
			}
		}
		
		System.arraycopy(screen1buf, 0, screen1, 0, 1000);
		System.arraycopy(screen2buf, 0, screen2, 0, 1000);
	}
	
	protected void multicolorFilter() {
		final int screen1buf[] = new int[1000];
		final int screen2buf[] = new int[1000];
		
		for (int y = 0; y < 25; y++) {
			final int p = y * 40;
			
			screen1buf[p] = screen1[p];
			screen1buf[p + 39] = screen1[p + 39];
			
			screen2buf[p] = screen2[p];
			screen2buf[p + 39] = screen2[p + 39];
		
			for (int x = 1; x < 39; x++) {
				int a1 = p + x - 1;
				int a2 = p + x;
				int a3 = p + x + 1;
				
				int c1[] = machinePalette[screen1[a1] & 0xf];
				int c2[] = machinePalette[screen1[a2] & 0xf];
				int c3[] = machinePalette[screen1[a3] & 0xf];
				
				int r = (c1[0] + c2[0] + c3[0]) / 3;
				int g = (c1[1] + c2[1] + c3[1]) / 3;
				int b = (c1[2] + c2[2] + c3[2]) / 3;
				
				screen1buf[a2] = Gfx.getColorIndex(colorAlg, machinePalette, r, g, b);
				
				c1 = machinePalette[screen1[a1] >> 4];
				c2 = machinePalette[screen1[a2] >> 4];
				c3 = machinePalette[screen1[a3] >> 4];
				
				r = (c1[0] + c2[0] + c3[0]) / 3;
				g = (c1[1] + c2[1] + c3[1]) / 3;
				b = (c1[2] + c2[2] + c3[2]) / 3;
				
				screen1buf[a2] |= (Gfx.getColorIndex(colorAlg, machinePalette, r, g, b) << 4);
				
				c1 = machinePalette[nibbles[a1] & 0xf];
				c2 = machinePalette[nibbles[a2] & 0xf];
				c3 = machinePalette[nibbles[a3] & 0xf];
				
				r = (c1[0] + c2[0] + c3[0]) / 3;
				g = (c1[1] + c2[1] + c3[1]) / 3;
				b = (c1[2] + c2[2] + c3[2]) / 3;
				
				screen2buf[a2] = Gfx.getColorIndex(colorAlg, machinePalette, r, g, b);
			}
		}
		
		System.arraycopy(screen1buf, 0, screen1, 0, 1000);
		System.arraycopy(screen2buf, 0, nibbles, 0, 1000);
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

		final int prevColorIndex[] = new int[8];
		final int tileColors[] = new int[4];

		tileColors[0] = backgroundColor;

		for (int y = 0; y < 200; y += 8) {
			final int p = y * 320 * 3;
			Arrays.fill(prevColorIndex, -1);

			for (int x = 0; x < 320; x += 8) {
				final int offset = p + x * 3;
				final int address = (y >> 3) * 40 + (x >> 3);

				tileColors[1] = screen1[address] >> 4;
				tileColors[2] = screen1[address] & 0xf;
				tileColors[3] = nibbles[address] & 0xf;

				int even = 0;

				// 8x8 tile work copy
				for (int y0 = 0; y0 < 8; y0++) {
					int bitcount = 6;
					final int p1 = y0 * 320 * 3;

					for (int x0 = 0; x0 < 24; x0 += 3) {
						final int colorIndex;

						if (even == 0)
							colorIndex = (bitmap1[bitmapIndex] >> bitcount) & 0x3;
						else {
							colorIndex = (bitmap2[bitmapIndex] >> bitcount) & 0x3;
							bitcount -= 2;
						}

						even ^= 1;

						r = machinePalette[tileColors[colorIndex]][0];
						g = machinePalette[tileColors[colorIndex]][1];
						b = machinePalette[tileColors[colorIndex]][2];

						if (prevColorIndex[y0] != -1) {

							r1 = machinePalette[prevColorIndex[y0]][0];
							g1 = machinePalette[prevColorIndex[y0]][1];
							b1 = machinePalette[prevColorIndex[y0]][2];

							r = (int) ((r + r1) >> 1);
							g = (int) ((g + g1) >> 1);
							b = (int) ((b + b1) >> 1);
						}

						prevColorIndex[y0] = tileColors[colorIndex];
						final int position = offset + p1 + x0;

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

				r += work[position];
				g += work[position + 1];
				b += work[position + 2];
			}
		}

		r /= 320 * 200;
		g /= 320 * 200;
		b /= 320 * 200;

		// average as background - common color
		backgroundColor = Gfx.getColorIndex(colorAlg, machinePalette, r, g, b);

		final int br = machinePalette[backgroundColor][0];
		final int bg = machinePalette[backgroundColor][1];
		final int bb = machinePalette[backgroundColor][2];

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

				// map background color to the nearest color tile palette
				colorIndex = Gfx.getColorIndex(colorAlg, tilePalette, br, bg, bb);
				if (colorIndex != 0) {
					tilePalette[colorIndex][0] = tilePalette[0][0]; // first entry = background color
					tilePalette[colorIndex][1] = tilePalette[0][1];
					tilePalette[colorIndex][2] = tilePalette[0][2];

					tilePalette[0][0] = br; // background color
					tilePalette[0][1] = bg;
					tilePalette[0][2] = bb;

					tileColors[colorIndex] = tileColors[0];
				}

				tileColors[0] = backgroundColor;

				for (int y0 = 0; y0 < 8; y0++) {
					final int k = offset + y0 * 320 * 3;
					
					for (int x0 = 0; x0 < 24; x0 += 3) {
						final int position = k + x0;

						// get current picture color
						r = Gfx.saturate(work[position + 0]);
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
							colorIndex = Gfx.getColorIndex(colorAlg, machinePalette, 
									palette[colorIndex][0], 
									palette[colorIndex][1], 
									palette[colorIndex][2]);
							
							final int k = Gfx.getColorIndex(colorAlg, new int[][] { tilePalette[1], tilePalette[2], tilePalette[3] }, 
									machinePalette[colorIndex][0],
									machinePalette[colorIndex][1],
									machinePalette[colorIndex][2]);
							
							tilePalette[k + 1][0] = machinePalette[colorIndex][0];
							tilePalette[k + 1][1] = machinePalette[colorIndex][1];
							tilePalette[k + 1][2] = machinePalette[colorIndex][2];
							
							tileColors[k + 1] = colorIndex;
						}
						
						colorIndex = Gfx.getColorIndex(colorAlg, tilePalette, 
							machinePalette[colorIndex][0], 
							machinePalette[colorIndex][1], 
							machinePalette[colorIndex][2]);
					
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
				final int c01 = tileColors[1] << 4;
				final int c10 = tileColors[2];
				final int c11 = tileColors[3];

				screen1[address] = c01 | c10;
				nibbles[address] = c11;
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
							final int color = Gfx.getColorIndex(colorAlg, palette, (q1[0] + q2[0]) / 2, (q1[1] + q2[1]) / 2, (q1[2] + q2[2]) / 2);
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
					final int color = Gfx.getColorIndex(colorAlg, palette, (q1[0] + q2[0]) / 2, (q1[1] + q2[1]) / 2, (q1[2] + q2[2]) / 2);
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
			return 8;
		} 
	}
}
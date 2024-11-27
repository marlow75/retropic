package pl.dido.image.utils;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.Raster;
import java.util.Arrays;

import pl.dido.image.utils.Config.DITHERING;
import pl.dido.image.utils.Config.NEAREST_COLOR;

public class Gfx {

	public static final int M8x8[][] = new int[][] { // 65 colors
			{ 0, 32, 8, 40, 2, 34, 10, 42 }, { 48, 16, 56, 24, 50, 18, 58, 26 }, { 12, 44, 4, 36, 14, 46, 6, 38 },
			{ 60, 28, 52, 20, 62, 30, 54, 22 }, { 3, 35, 11, 43, 1, 33, 9, 41 }, { 51, 19, 59, 27, 49, 17, 57, 25 },
			{ 15, 47, 7, 39, 13, 45, 5, 37 }, { 63, 31, 55, 23, 61, 29, 53, 21 } };

	public static final int M4x4[][] = new int[][] { // 17 colors
			{ 15, 195, 60, 240 }, { 135, 75, 180, 120 }, { 45, 225, 30, 210 }, { 165, 105, 150, 90 } };

	public static final int M2x2[][] = new int[][] { // 5 colors
			{ 51, 206 }, { 153, 102 } };

	public static final int M16x16[][] = new int[][] { // 256 colors
			{ 0, 191, 48, 239, 12, 203, 60, 251, 3, 194, 51, 242, 15, 206, 63, 254 },
			{ 127, 64, 175, 112, 139, 76, 187, 124, 130, 67, 178, 115, 142, 79, 190, 127 },
			{ 32, 223, 16, 207, 44, 235, 28, 219, 35, 226, 19, 210, 47, 238, 31, 222 },
			{ 159, 96, 143, 80, 171, 108, 155, 92, 162, 99, 146, 83, 174, 111, 158, 95 },
			{ 8, 199, 56, 247, 4, 195, 52, 243, 11, 202, 59, 250, 7, 198, 55, 246 },
			{ 135, 72, 183, 120, 131, 68, 179, 116, 138, 75, 186, 123, 134, 71, 182, 119 },
			{ 40, 231, 24, 215, 36, 227, 20, 211, 43, 234, 27, 218, 39, 230, 23, 214 },
			{ 167, 104, 151, 88, 163, 100, 147, 84, 170, 107, 154, 91, 166, 103, 150, 87 },
			{ 2, 193, 50, 241, 14, 205, 62, 253, 1, 192, 49, 240, 13, 204, 61, 252 },
			{ 129, 66, 177, 114, 141, 78, 189, 126, 128, 65, 176, 113, 140, 77, 188, 125 },
			{ 34, 225, 18, 209, 46, 237, 30, 221, 33, 224, 17, 208, 45, 236, 29, 220 },
			{ 161, 98, 145, 82, 173, 110, 157, 94, 160, 97, 144, 81, 172, 109, 156, 93 },
			{ 10, 201, 58, 249, 6, 197, 54, 245, 9, 200, 57, 248, 5, 196, 53, 244 },
			{ 137, 74, 185, 122, 133, 70, 181, 118, 136, 73, 184, 121, 132, 69, 180, 117 },
			{ 42, 233, 26, 217, 38, 229, 22, 213, 41, 232, 25, 216, 37, 228, 21, 212 },
			{ 169, 106, 153, 90, 165, 102, 149, 86, 168, 105, 152, 89, 164, 101, 148, 85 } };

	public static final void rgb2YUV(final int b, final int g, final int r, final int yuv[], final int i) {
		yuv[i] = Math.round(r * .299000f + g * .587000f + b * .114000f);
		yuv[i + 1] = Math.round(r * -.168736f + g * -.331264f + b * .500000f + 128f);
		yuv[i + 2] = Math.round(r * .500000f + g * -.418688f + b * -.081312f + 128f);
	}

	public static final void yuv2RGB(final int y, final int u, final int v, final byte pixels[], final int i) {
		final float u128 = u - 128;
		final float v128 = v - 128;

		final float r = y + 1.402f * v128;
		final float g = y - 0.34414f * u128 - 0.71414f * v128;
		final float b = y + 1.772f * u128;

		// clamp to [0,255]
		pixels[i + 2] = (byte) (r > 0 ? (r > 255 ? 255 : r) : 0);
		pixels[i + 1] = (byte) (g > 0 ? (g > 255 ? 255 : g) : 0);
		pixels[i] = (byte) (b > 0 ? (b > 255 ? 255 : b) : 0);
	}

	public static final int saturate(final int i) {
		return i > 255 ? 255 : i < 0 ? 0 : i;
	}

	public static final int saturateByte(final int i) {
		return i > Byte.MAX_VALUE ? Byte.MAX_VALUE : i < Byte.MIN_VALUE ? Byte.MIN_VALUE : i;
	}

	public static final int saturate(final int i, final int value) {
		return i > value ? value : i < -value ? -value : i;
	}

	public static final int[] copy2Int(final byte[] pixels) {
		final int len = pixels.length;
		final int array[] = new int[len];

		for (int i = 0; i < len; i += 4) {
			array[i] = pixels[i] & 0xff;
			array[i + 1] = pixels[i + 1] & 0xff;
			array[i + 2] = pixels[i + 2] & 0xff;
			array[i + 3] = pixels[i + 3] & 0xff;
		}

		return array;
	}

	public static final float[] copy2float(final byte[] pixels) {
		final int len = pixels.length;
		final float array[] = new float[len];

		for (int i = 0; i < len; i += 4) {
			array[i] = pixels[i] & 0xff;
			array[i + 1] = pixels[i + 1] & 0xff;
			array[i + 2] = pixels[i + 2] & 0xff;
			array[i + 3] = pixels[i + 3] & 0xff;
		}

		return array;
	}

	public static final float euclideanDistance(final int b, final int g, final int r, final int pb, final int pg,
			final int pr) {
		final int rpr = r - pr;
		final int gpg = g - pg;
		final int bpb = b - pb;

		return (rpr * rpr) + (gpg * gpg) + (bpb * bpb);
	}

	public static final float perceptedDistance(final int b, final int g, final int r, final int pb, final int pg,
			final int pr) {
		final int rpr = r - pr;
		final int gpg = g - pg;
		final int bpb = b - pb;

		final int delta = (r + pr) >> 1;
		return ((2 + (delta >> 8)) * rpr * rpr) + (4 * (gpg * gpg)) + ((2 + ((255 - delta) >> 8)) * bpb * bpb);
	}

	public static final float getLuma(final int b, final int g, final int r) {
		return 0.299f * r + 0.587f * g + 0.114f * b;
	}

	public static final float euclideanDistance(final float pb, final float pg, final float pr, final float b,
			final float g, final float r) {
		final float rpr = r - pr;
		final float gpg = g - pg;
		final float bpb = b - pb;

		return (rpr * rpr) + (gpg * gpg) + (bpb * bpb);
	}

	public static final float perceptedDistance(final float b, final float g, final float r, final float pb,
			final float pg, final float pr) {

		final float rpr = r - pr;
		final float gpg = g - pg;

		final float bpb = b - pb;
		final float delta = (r + pr) / 2;

		return ((2 + (delta / 256)) * rpr * rpr) + (4 * (gpg * gpg)) + ((2 + (255 - delta) / 256) * bpb * bpb);
	}

	public static final int max(final int a, final int b, final int c) {
		final int w = a > b ? a : b;
		return w > c ? w : c;
	}

	public static final float min(final float a, final float b, final float c) {
		final float w = a < b ? a : b;
		return w < c ? w : c;
	}

	public static final BufferedImage scaleWithStretching(final BufferedImage image, final int maxX, final int maxY) {
		final int x = image.getWidth();
		final int y = image.getHeight();

		final float sx = maxX / (float) x;
		final float sy = maxY / (float) y;

		final BufferedImage scaled = new BufferedImage(maxX, maxY, image.getType());
		final AffineTransform si = AffineTransform.getScaleInstance(sx, sy);

		final AffineTransformOp transform = new AffineTransformOp(si, AffineTransformOp.TYPE_BICUBIC);
		transform.filter(image, scaled);

		return scaled;
	}

	public static final BufferedImage scaleWithPreservedAspect(final BufferedImage image, final int maxX,
			final int maxY) {
		final BufferedImage img = new BufferedImage(maxX, maxY, image.getType());
		final Graphics2D g = img.createGraphics();

		g.setPaint(new Color(0, 0, 0)); // white background
		g.fillRect(0, 0, maxX, maxY);

		final float x = image.getWidth();
		final float y = image.getHeight();

		final float ratio = Math.min(maxX / x, maxY / y);

		final BufferedImage scaled = new BufferedImage(maxX, maxY, image.getType());
		final AffineTransform si = AffineTransform.getScaleInstance(ratio, ratio);

		final AffineTransformOp scale = new AffineTransformOp(si, AffineTransformOp.TYPE_BICUBIC);
		scale.filter(image, scaled);

		final int px = (int) ((maxX - x * ratio) / 2);
		final int py = (int) ((maxY - y * ratio) / 2);

		g.drawImage(scaled, px, py, null);
		return img;
	}

	private static final int lumaBlend(final int l1, final int l2) {
		return (l1 * 9 + l2) / 10;
	}

	private static final int cdfScale(final int cdf[], final int luma, final int max) {
		return (cdf[luma] * 255) / cdf[max];
	}

	private static final void calcCdf(final int cdf[], final int histogram[]) {
		// cdf - cumulative distributed function
		cdf[0] = histogram[0];
		for (int i = 1; i < 256; i++)
			cdf[i] = cdf[i - 1] + histogram[i];
	}

	public static final void HE(final byte pixels[]) {
		final int histogram[] = new int[256];
		final int cdf[] = new int[256];

		final int len = pixels.length;
		final int yuv[] = new int[len];

		int r, g, b, max = 0;
		for (int i = 0; i < len; i += 3) {
			r = pixels[i] & 0xff;
			g = pixels[i + 1] & 0xff;
			b = pixels[i + 2] & 0xff;

			rgb2YUV(r, g, b, yuv, i);
			final int luma = yuv[i];
			histogram[luma]++;

			if (luma > max)
				max = luma;
		}

		calcCdf(cdf, histogram);

		for (int i = 0; i < len; i += 3)
			yuv2RGB(cdfScale(cdf, yuv[i], max), yuv[i + 1], yuv[i + 2], pixels, i);
	}

	private static final void clipHistogram(final int histogram[], final int brightness) {
		// clip the brightest
		int clippedCount;

		// distribute surplus
		do {
			int total = 0;
			clippedCount = 0;

			for (int i = 0; i < 256; i++)
				// clip if is above brightness
				if (histogram[i] > brightness) {
					total += histogram[i] - brightness;
					histogram[i] = brightness;

					// add to total clipping sum
					clippedCount++;
				}

			if (clippedCount > 0) {
				total = (int) Math.ceil(total / (256f - clippedCount));

				// add to all slots
				for (int i = 0; i < 256; i++)
					if (histogram[i] < brightness)
						histogram[i] += total;
			}

		} while (clippedCount != 0);
	}

	// SWAHE
	public final static void SWAHE(final byte pixels[], final int window, int brightness, final int width,
			final int height) {
		// cdf & yuv
		final int cdf[] = new int[256];
		final int yuv[] = new int[6]; // work + center pixels

		final int window3 = 3 * window;
		final int midY = window / 2;
		final int midX = window3 / 2;

		final int maxX = width * 3;
		final int maxY = height;

		final int maxX1 = maxX - 3;
		final int maxY1 = maxY - 1;

		// histogram
		final int histogram[] = new int[256];
		brightness *= 3;

		int r, g, b, max = 0;

		// process sliding window
		for (int y = 0; y < maxY; y++)
			for (int x = 0; x < maxX; x += 3) {
				// compute histogram for window
				Arrays.fill(histogram, 0);
				int wp = 0;

				// calculate window histogram
				for (int yw = -midY; yw < midY; yw++) {
					int y0 = y + yw; // y screen position
					if (y0 < 0) // upper corner
						y0 += midY;
					else if (y0 > maxY1)
						y0 -= midY;

					final int sp0 = y0 * maxX;
					for (int xw = -midX; xw < midX; xw += 3) {
						int x0 = x + xw;
						if (x0 < 0) // left corner
							x0 += midX;
						else if (x0 > maxX1)
							x0 -= midX;

						// screen position
						final int sp = x0 + sp0;

						r = pixels[sp] & 0xff;
						g = pixels[sp + 1] & 0xff;
						b = pixels[sp + 2] & 0xff;

						final int luma;
						// mind pixel format & convert RGB to YUV
						if (xw == 0 && yw == 0) {
							wp = sp;

							rgb2YUV(r, g, b, yuv, 3);
							luma = yuv[3];

							histogram[luma]++; // add center pixel to histogram
						} else {
							rgb2YUV(r, g, b, yuv, 0);
							luma = yuv[0];

							histogram[luma]++; // add current pixel to histogram
						}

						if (luma > max)
							max = luma;
					}
				}

				// clip histogram
				clipHistogram(histogram, brightness);

				// cdf - cumulative distributed function
				calcCdf(cdf, histogram);

				// window center pixel - luma
				yuv2RGB(cdfScale(cdf, yuv[3], max), yuv[3 + 1], yuv[3 + 2], pixels, wp);
			}
	}

	// CLAHE
	public static final void CLAHE(final byte pixels[], final int window, int brightness, final int width,
			final int height) {

		final int yuv[] = new int[pixels.length];
		final int lumas[] = new int[pixels.length];

		// cdf & yuv
		final int cdf[] = new int[256];

		final int maxX = width;
		final int maxY = height;

		final int midX = window / 2;
		final int midY = window / 2;

		// histogram
		final int histogram[] = new int[256];

		int r, g, b;
		brightness *= 3;

		// process all inner pixel image to get central pixel luma
		for (int y = 0; y < maxY; y += window)
			for (int x = 0; x < maxX; x += window) {
				// compute histogram for window
				Arrays.fill(histogram, 0);

				int max = 0;
				final int wp = ((x + midX) + (y + midY) * maxX) * 3;

				// calculate window histogram
				for (int yw = 0; yw < window; yw++) {
					final int y0 = y + yw;

					final int sp0 = y0 * maxX;
					for (int xw = 0; xw < window; xw++) {
						final int x0 = x + xw;

						// screen position
						final int sp = 3 * (x0 + sp0);

						r = pixels[sp] & 0xff;
						g = pixels[sp + 1] & 0xff;
						b = pixels[sp + 2] & 0xff;

						rgb2YUV(r, g, b, yuv, sp);

						final int luma = yuv[sp];
						histogram[luma]++;

						if (luma > max)
							max = luma;
					}
				}

				clipHistogram(histogram, brightness);

				// cdf - cumulative distributed function
				calcCdf(cdf, histogram);

				// window center pixel - luma
				lumas[wp] = cdfScale(cdf, yuv[wp], max);
			}

		// bilinear interpolation
		for (int y = midY; y < maxY - midY; y += window) {
			final int y1 = y;
			final int y2 = y + window;

			for (int x = midX; x < maxX - midX; x += window) {
				final int x1 = x;
				final int x2 = x + window;

				final int y1maxX = y1 * maxX;
				final int y2maxX = y2 * maxX;

				final int wp1 = (x1 + y1maxX) * 3;
				final int wp2 = (x2 + y1maxX) * 3;
				final int wp3 = (x1 + y2maxX) * 3;
				final int wp4 = (x2 + y2maxX) * 3;

				final int l1 = lumas[wp1];
				final int l2 = lumas[wp2];
				final int l3 = lumas[wp3];
				final int l4 = lumas[wp4];

				final float dy21 = y2 - y1;
				final float dx21 = x2 - x1;

				for (int yw = y; yw < y + window; yw++) {
					final float dy2w = y2 - yw;
					final float dwy1 = yw - y1;

					for (int xw = x; xw < x + window; xw++) {
						// bilinear approximation
						final float dx2w = x2 - xw;
						final float dwx1 = xw - x1;

						final float p = dx2w / dx21;
						final float q = dwx1 / dx21;

						final float i1 = p * l1 + q * l2;
						final float i2 = p * l3 + q * l4;

						final int a = Math.round(dy2w / dy21 * i1 + dwy1 / dy21 * i2);
						final int sp = 3 * (xw + yw * maxX);

						yuv2RGB(lumaBlend(yuv[sp], a), yuv[sp + 1], yuv[sp + 2], pixels, sp);
					}
				}
			}
		}

		// up and down
		for (int x = midX; x < maxX - midX; x += window) {
			final int y1 = midY;
			final int y2 = maxY - midY;

			for (int xw = x; xw < x + window; xw++) {
				final int x1 = x;
				final int x2 = x + window;

				final int y1maxX = y1 * maxX;
				final int y2maxX = y2 * maxX;

				final int wp1 = (x1 + y1maxX) * 3;
				final int wp2 = (x2 + y1maxX) * 3;

				final int wp3 = (x1 + y2maxX) * 3;
				final int wp4 = (x2 + y2maxX) * 3;

				final int l1 = lumas[wp1];
				final int l2 = lumas[wp2];

				final int l3 = lumas[wp3];
				final int l4 = lumas[wp4];

				final float dx21 = x2 - x1;
				final float dx2w = x2 - xw;
				final float dwx1 = xw - x1;

				final float p = dx2w / dx21;
				final float q = dwx1 / dx21;

				final int a1 = Math.round(p * l1 + q * l2);
				final int a2 = Math.round(p * l3 + q * l4);

				for (int yw1 = 0; yw1 < midY; yw1++) {
					final int yw2 = (maxY - 1) - yw1;

					final int sp1 = 3 * (xw + yw1 * maxX);
					final int sp2 = 3 * (xw + yw2 * maxX);

					yuv2RGB(lumaBlend(yuv[sp1], a1), yuv[sp1 + 1], yuv[sp1 + 2], pixels, sp1);
					yuv2RGB(lumaBlend(yuv[sp2], a2), yuv[sp2 + 1], yuv[sp2 + 2], pixels, sp2);
				}
			}
		}

		// left right
		for (int y = midY; y < maxY - midY; y += window) {
			final int x1 = midX;
			final int x2 = maxX - midX;

			final float dx21 = x2 - x1;
			for (int yw = y; yw < y + window; yw++) {
				final int y1 = y;
				final int y2 = y + window;

				final int y1maxX = y1 * maxX;
				final int y2maxX = y2 * maxX;

				final int wp1 = (x1 + y1maxX) * 3;
				final int wp2 = (x2 + y1maxX) * 3;

				final int wp3 = (x1 + y2maxX) * 3;
				final int wp4 = (x2 + y2maxX) * 3;

				final int l1 = lumas[wp1];
				final int l2 = lumas[wp2];

				final int l3 = lumas[wp3];
				final int l4 = lumas[wp4];

				for (int xw1 = 0; xw1 < midX; xw1++) {
					float dx2w = x2 - xw1;
					float dwx1 = xw1 - x1;

					float p = dx2w / dx21;
					float q = dwx1 / dx21;

					final int a1 = Math.round(p * l1 + q * l2);
					final int xw2 = (maxX - 1) - xw1;

					dx2w = x2 - xw2;
					dwx1 = xw2 - x1;

					p = dx2w / dx21;
					q = dwx1 / dx21;

					final int a2 = Math.round(p * l3 + q * l4);

					final int sp1 = 3 * (xw1 + yw * maxX);
					final int sp2 = 3 * (xw2 + yw * maxX);

					yuv2RGB(lumaBlend(yuv[sp1], a1), yuv[sp1 + 1], yuv[sp1 + 2], pixels, sp1);
					yuv2RGB(lumaBlend(yuv[sp2], a2), yuv[sp2 + 1], yuv[sp2 + 2], pixels, sp2);
				}
			}
		}
	}

	public static byte[] makeScanlines(final byte pixels[], final int window) {
		final int ymax = pixels.length / 3 / window;
		final byte out[] = new byte[pixels.length * 2];

		for (int y = 0; y < ymax; y++) { // y -> 2*y origin & scan line
			final int ypos = y * window * 3; // origin y position
			final int ydest = 2 * ypos; // destination 2*y position

			final int ydest0 = (2 * y - 1) * window * 3; // destination 2*y+1 position

			for (int x = 0; x < window * 3; x += 3) {
				final int xys = ypos + x; // origin pixel position
				final int xyd = ydest + x; // destination pixel position
				final int xyd0 = ydest0 + x; // destination next line pixel position

				final byte r = pixels[xys]; // read pixel
				final byte g = pixels[xys + 1];
				final byte b = pixels[xys + 2];

				if (y > 0) {
					out[xyd0] = (byte) Math.round(r * 0.6f);
					out[xyd0 + 1] = (byte) Math.round(g * 0.6f);
					out[xyd0 + 2] = (byte) Math.round(b * 0.6f);
				}

				out[xyd] = r;
				out[xyd + 1] = g;
				out[xyd + 2] = b;
			}
		}

		return out;
	}

	public static void dithering(final byte pixels[], final int palette[][], final Config config) {
		final int work[] = Gfx.copy2Int(pixels);

		final int width = config.getScreenWidth();
		final int height = config.getScreenHeight();

		final DITHERING dithering = config.dither_alg;
		final NEAREST_COLOR colorAlg = config.color_alg;

		final int width3 = width * 3;

		int r0, g0, b0;
		int r_error = 0, g_error = 0, b_error = 0;

		for (int y = 0; y < height; y++) {
			final int k = y * width3;
			final int k1 = ((y + 1) * width3);
			final int k2 = ((y + 2) * width3);

			for (int x = 0; x < width3; x += 3) {
				final int pyx = k + x;
				final int py1x = k1 + x;
				final int py2x = k2 + x;

				r0 = Gfx.saturate(work[pyx]);
				g0 = Gfx.saturate(work[pyx + 1]);
				b0 = Gfx.saturate(work[pyx + 2]);

				final int color = getColorIndex(colorAlg, palette, r0, g0, b0);
				final int pixel[] = palette[color];

				final int r = pixel[0];
				final int g = pixel[1];
				final int b = pixel[2];

				pixels[pyx] = (byte) r;
				pixels[pyx + 1] = (byte) g;
				pixels[pyx + 2] = (byte) b;

				r_error = r0 - r;
				g_error = g0 - g;
				b_error = b0 - b;

				switch (dithering) {
				case FLOYDS:
					if (x < (width - 1) * 3) {
						work[pyx + 3] += (r_error * 7) / 16;
						work[pyx + 3 + 1] += (g_error * 7) / 16;
						work[pyx + 3 + 2] += (b_error * 7) / 16;
					}
					if (y < height - 1) {
						work[py1x - 3] += (r_error * 3) / 16;
						work[py1x - 3 + 1] += (g_error * 3) / 16;
						work[py1x - 3 + 2] += (b_error * 3) / 16;

						work[py1x] += (r_error * 5) / 16;
						work[py1x + 1] += (g_error * 5) / 16;
						work[py1x + 2] += (b_error * 5) / 16;

						if (x < (width - 1) * 3) {
							work[py1x + 3] += r_error / 16;
							work[py1x + 3 + 1] += g_error / 16;
							work[py1x + 3 + 2] += b_error / 16;
						}
					}
					break;
				case ATKINSON:
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
					if (y < height - 1) {
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

						if (y < height - 2) {
							work[py2x] += r_error >> 3;
							work[py2x + 1] += g_error >> 3;
							work[py2x + 2] += b_error >> 3;
						}
					}
					break;
				default:
					// no dithering
					break;
				}
			}
		}
	}

	public static void bayer16x16(final byte pixels[], final int palette[][], final NEAREST_COLOR colorAlg,
			final int width, final int height, final int bpp) {
		bayer(M16x16, pixels, palette, colorAlg, width, height, bpp);
	}

	public static void bayer8x8(final byte pixels[], final int palette[][], final NEAREST_COLOR colorAlg,
			final int width, final int height, final int bpp) {
		bayer(M8x8, pixels, palette, colorAlg, width, height, bpp);
	}

	public static void bayer4x4(final int pixels[], final int palette[][], final NEAREST_COLOR colorAlg,
			final int width, final int height, final int bpp) {
		bayer(M4x4, pixels, palette, colorAlg, width, height, bpp);
	}

	public static void bayer4x4(final byte pixels[], final int palette[][], final NEAREST_COLOR colorAlg,
			final int width, final int height, final int bpp) {
		bayer(M8x8, pixels, palette, colorAlg, width, height, bpp);
	}
	
	public static void bayer2x2(final byte pixels[], final int palette[][], final NEAREST_COLOR colorAlg,
			final int width, final int height, final int bpp) {
		bayer(M2x2, pixels, palette, colorAlg, width, height, bpp);
	}

	public static void bayer(final int matrix[][], final int pixels[], final int palette[][],
			final NEAREST_COLOR colorAlg, final int width, final int height, final int bpp) {

		for (int y = 0; y < height; y++) {
			final int width3 = width * 3;

			for (int x = 0; x < width; x++) {
				final int pyx = y * width3 + x * 3;

				int r = pixels[pyx];
				int g = pixels[pyx + 1];
				int b = pixels[pyx + 2];

				r = Gfx.bayer(matrix, x, y, r, bpp);
				g = Gfx.bayer(matrix, x, y, g, bpp);
				b = Gfx.bayer(matrix, x, y, b, bpp);

				final int color = Gfx.getColorIndex(colorAlg, palette, r, g, b);

				pixels[pyx] = (byte) palette[color][0];
				pixels[pyx + 1] = (byte) palette[color][1];
				pixels[pyx + 2] = (byte) palette[color][2];
			}
		}
	}

	public static void bayer(final int matrix[][], final byte pixels[], final int palette[][],
			final NEAREST_COLOR colorAlg, final int width, final int height, final int bpp) {

		for (int y = 0; y < height; y++) {
			final int width3 = width * 3;

			for (int x = 0; x < width; x++) {
				final int pyx = y * width3 + x * 3;

				int r = pixels[pyx] & 0xff;
				int g = pixels[pyx + 1] & 0xff;
				int b = pixels[pyx + 2] & 0xff;

				r = Gfx.bayer(matrix, x, y, r, bpp);
				g = Gfx.bayer(matrix, x, y, g, bpp);
				b = Gfx.bayer(matrix, x, y, b, bpp);

				final int color = Gfx.getColorIndex(colorAlg, palette, r, g, b);

				pixels[pyx] = (byte) palette[color][0];
				pixels[pyx + 1] = (byte) palette[color][1];
				pixels[pyx + 2] = (byte) palette[color][2];
			}
		}
	}

	public final static int bayer2x2(final int x0, final int y0, final int c, final int f, final int b) {
		return c < M2x2[x0 % 2][y0 % 2] ? b : f;
	}

	public final static int bayer2x2RGB(final int x0, final int y0, final int r, final int g, final int b) {
		return getLuma(r, g, b) < M2x2[x0 % 2][y0 % 2] ? 0 : 1;
	}

	public static final int bayer(final int matrix[][], final int x0, final int y0, final int c, final float bpp) {
		final int mod = matrix.length;

		final float divider = 255 / bpp;
		final float e = matrix[y0 % mod][x0 % mod] / bpp;

		float i = (c + e) / divider;
		if (i == 0)
			return 0;

		if (i > bpp)
			i = bpp;
		i *= divider;

		return (int) (i > 255f ? 255 : i);
	}

	public static final int bayer2x2(final int x0, final int y0, final int c, final float bpp) {
		return bayer(M2x2, x0, y0, c, bpp);
	}

	public static final int bayer4x4(final int x0, final int y0, final int c, final float bpp) {
		return bayer(M4x4, x0, y0, c, bpp);
	}

	public static final int bayer8x8(final int x0, final int y0, final int c, final float bpp) {
		return bayer(M8x8, x0, y0, c, bpp);
	}

	public static final int bayer16x16(final int x0, final int y0, final int c, final float bpp) {
		return bayer(M16x16, x0, y0, c, bpp);
	}

	public static BufferedImage byteArrayToBGRImage(final byte[] data, final int width, final int height) {

		final BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
		final Raster raster = Raster.createRaster(bufferedImage.getSampleModel(), new DataBufferByte(data, data.length),
				new Point());

		bufferedImage.setData(raster);
		return bufferedImage;
	}

	public final static BufferedImage scaleImage(final BufferedImage image, final int width, final int height,
			final boolean preserveAspect) {
		if (image.getWidth() != width || image.getHeight() != height)
			if (preserveAspect)
				return Gfx.scaleWithPreservedAspect(image, width, height);
			else
				return Gfx.scaleWithStretching(image, width, height);

		return image;
	}

	public final static float getDistance(final NEAREST_COLOR color, final int r0, final int g0, final int b0,
			final int r1, final int g1, final int b1) {
		switch (color) {
		case EUCLIDEAN:
			return Gfx.euclideanDistance(r0, g0, b0, r1, g1, b1);
		case PERCEPTED:
			return Gfx.perceptedDistance(b0, g0, r0, b1, g1, r1);
		default:
			return Gfx.euclideanDistance(r0, g0, b0, r1, g1, b1);
		}
	}

	public final static int getColorIndex(final NEAREST_COLOR color, final int palette[][], final int r0, final int g0,
			final int b0) {
		switch (color) {
		case EUCLIDEAN:
			return getEuclideanColorIndex(palette, r0, g0, b0);
		case PERCEPTED:
			return getPerceptedColorIndex(palette, r0, g0, b0);
		case LUMA_WEIGHTED:
			return getLumaColorIndex(palette, r0, g0, b0);
		default:
			return getEuclideanColorIndex(palette, r0, g0, b0);
		}
	}

	protected static final int[] matchingEuclideanColor(final int palette[][], final int r, final int g, final int b) {
		return palette[getEuclideanColorIndex(palette, r, g, b)];
	}

	protected static final int getEuclideanColorIndex(final int palette[][], final int r, final int g, final int b) {
		int index = 0;
		int color[] = palette[0];
		float min = Gfx.euclideanDistance(r, g, b, color[0], color[1], color[2]);

		for (int i = 1; i < palette.length; i++) { // euclidean distance
			color = palette[i];
			final float distance = Gfx.euclideanDistance(r, g, b, color[0], color[1], color[2]);

			if (distance < min) {
				min = distance;
				index = i;
			}
		}

		return index;
	}

	protected static int getPerceptedColorIndex(final int palette[][], final int r, final int g, final int b) {
		int index = 0;
		int color[] = palette[0];
		float min = perceptedDistance(r, g, b, color[0], color[1], color[2]);

		for (int i = 1; i < palette.length; i++) { // distance
			color = palette[i];
			final float distance = perceptedDistance(r, g, b, color[0], color[1], color[2]);

			if (distance < min) {
				min = distance;
				index = i;
			}
		}

		return index;
	}

	protected int[] matchingLumaColor(final int palette[][], final int r, final int g, final int b) {
		return palette[getLumaColorIndex(palette, r, g, b)];
	}

	protected static int getLumaColorIndex(final int palette[][], final int r, final int g, final int b) {
		int index = 0, old_index = 0;
		float y1 = 0, oy1 = 0;

		float min = Float.MAX_VALUE;
		final float y = getLuma(r, g, b);
		final int len = palette.length;

		for (int i = len; i-- > 0;) { // euclidean distance
			final int color[] = palette[i];
			final int pr = color[0];
			final int pg = color[1];
			final int pb = color[2];

			final float distance = Gfx.euclideanDistance(r, g, b, pr, pg, pb);

			if (distance < min) {
				min = distance;

				old_index = index;
				index = i;

				oy1 = y1;
				y1 = getLuma(pr, pg, pb);
			}
		}

		return Math.abs(y1 - y) < Math.abs(oy1 - y) ? index : old_index;
	}

	public static final void colorScale(final float r, final float g, final float b, final int pixels[]) {
		for (int i = 0; i < pixels.length; i += 3) {
			pixels[i] = saturate((int) (pixels[i] + pixels[i] * r));
			pixels[i + 1] = saturate((int) (pixels[i + 1] + pixels[i + 1] * g));
			pixels[i + 2] = saturate((int) (pixels[i + 2] + pixels[i + 2] * b));
		}
	}

	public static final BufferedImage grey2BGR(final BufferedImage image) {
		final byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
		final byte[] data = new byte[pixels.length * 3];

		for (int i = 0; i < pixels.length; i++) {
			final int address = i * 3;
			data[address] = pixels[i];
			data[address + 1] = pixels[i];
			data[address + 2] = pixels[i];
		}

		return Gfx.byteArrayToBGRImage(data, image.getWidth(), image.getHeight());
	}

	public static final BufferedImage rgb2BGR(final BufferedImage image) {
		final int[] pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
		final byte[] data = new byte[pixels.length * 3];

		for (int i = 0; i < pixels.length; i++) {
			final int address = i * 3;
			final int r = pixels[i] & 0xff0000;
			final int g = pixels[i] & 0xff0000;
			final int b = pixels[i] & 0xff0000;

			data[address] = (byte) r;
			data[address + 1] = (byte) g;
			data[address + 2] = (byte) b;
		}

		return Gfx.byteArrayToBGRImage(data, image.getWidth(), image.getHeight());
	}

	public static float cosineSimilarity(final float[] vectorA, final float[] vectorB) {
		float sum = 0.0f;
		float powA = 0.0f;
		float powB = 0.0f;

		for (int i = 0; i < vectorA.length; i++) {
			sum += vectorA[i] * vectorB[i];
			powA += vectorA[i] * vectorA[i];
			powB += vectorB[i] * vectorB[i];
		}

		if (powA * powB > 0f)
			return sum / (float) (Math.sqrt(powA) * Math.sqrt(powB));
		else
			return -1f;
	}

	public static int[] get2RGBCubeColor(final NEAREST_COLOR colorAlg, final int work[], final int palette[][]) {
		int sr = 0, sg = 0, sb = 0;
		int r = 0, g = 0, b = 0;

		int r0 = 0, g0 = 0, b0 = 0;
		int r1 = 0, g1 = 0, b1 = 0;

		int len = work.length;

		for (int i = 0; i < len; i += 3) {
			sr += work[i];
			sg += work[i + 1];
			sb += work[i + 2];
		}

		len /= 3;

		// average color
		sr /= len;
		sg /= len;
		sb /= len;

		float max = -Float.MAX_VALUE;

		for (int i = 0; i < len; i += 3) {
			r = work[i];
			g = work[i + 1];
			b = work[i + 2];

			final float dist = getDistance(colorAlg, r, g, b, sr, sg, sb);
			if (dist > max) {
				max = dist;

				r0 = r;
				g0 = g;
				b0 = b;
			}
		}

		r1 = 2 * sr - r0;
		g1 = 2 * sg - g0;
		b1 = 2 * sb - b0;

		return new int[] { getColorIndex(colorAlg, palette, r0, g0, b0), getColorIndex(colorAlg, palette, r1, g1, b1) };
	}

	public static int[] get2RGBLinearColor(final NEAREST_COLOR colorAlg, final int work[], final int palette[][]) {
		int r1, g1, b1;

		final int value[] = new int[] { 0, 64, 128, 192, 255 };
		final int len = value.length;

		final float max[][][] = new float[len][len][len];
		final float min[][][] = new float[len][len][len];

		final int f[][][] = new int[len][len][len];
		final int n[][][] = new int[len][len][len];

		for (int r = 0; r < len; r++)
			for (int g = 0; g < len; g++)
				for (int b = 0; b < len; b++) {
					max[r][g][b] = -Float.MAX_VALUE;
					min[r][g][b] = Float.MAX_VALUE;

					for (int i = 0; i < work.length; i += 3) {
						r1 = work[i];
						g1 = work[i + 1];
						b1 = work[i + 2];

						final int color = getColorIndex(colorAlg, palette, work[i], work[i + 1], work[i + 2]);

						r1 = palette[color][0];
						g1 = palette[color][1];
						b1 = palette[color][2];

						// get minimum/maximum color distance
						final float dist = getDistance(colorAlg, r1, g1, b1, r, g, b);

						if (max[r][g][b] < dist) {
							max[r][g][b] = dist;
							f[r][g][b] = color;
						}

						if (min[r][g][b] > dist) {
							min[r][g][b] = dist;
							n[r][g][b] = color;
						}
					}
				}

		float maxDistance = -Float.MAX_VALUE;
		int f0 = 1, n0 = 1;

		for (int r = 0; r < value.length; r++)
			for (int g = 0; g < value.length; g++)
				for (int b = 0; b < value.length; b++) {

					final float d = (float) (Math.sqrt(max[r][g][b]) - Math.sqrt(min[r][g][b]));
					if (d > maxDistance) {
						maxDistance = d;

						f0 = f[r][g][b];
						n0 = n[r][g][b];
					}
				}

		return new int[] { f0, n0 };
	}

	public static float cosineSimilarity(final int[] vectorA, final int[] vectorB) {
		int sum = 0;
		int powA = 0;
		int powB = 0;

		for (int i = 0; i < vectorA.length; i++) {
			sum += vectorA[i] * vectorB[i];
			powA += vectorA[i] * vectorA[i];
			powB += vectorB[i] * vectorB[i];
		}

		if (powA * powB > 0)
			return sum / (float) (Math.sqrt(powA) * Math.sqrt(powB));
		else
			return -1f;
	}

	public static int[][] getTile4x4Palette(final byte data[]) {
		final int len = data.length;
		final int len3 = len / 3;

		int sr = 0, sg = 0, sb = 0;
		for (int i = 0; i < len; i += 3) {
			sr += data[i] & 0xff;
			sg += data[i + 1] & 0xff;
			sb += data[i + 2] & 0xff;
		}

		sr /= len3;
		sg /= len3;
		sb /= len3;

		float m = Integer.MIN_VALUE;
		int i1 = 0;

		for (int i = 0; i < len; i += 3) {
			final int r = data[i] & 0xff;
			final int g = data[i + 1] & 0xff;
			final int b = data[i + 2] & 0xff;

			// try to find most distant proportional vector (similarity = 1)
			// vector with 0,0,0 base
			final float d = Gfx.euclideanDistance(sr, sg, sb, r, g, b)
					* cosineSimilarity(new int[] { sr, sg, sb }, new int[] { r, g, b });
			if (d > m) {
				i1 = i;
				m = d;
			}
		}

		int r1 = data[i1] & 0xff;
		int g1 = data[i1 + 1] & 0xff;
		int b1 = data[i1 + 2] & 0xff;
		
		final float l1 = Gfx.getLuma(r1, g1, b1);

		r1 -= sr;
		g1 -= sg;
		b1 -= sb;
		
		m = Integer.MAX_VALUE;
		int i2 = 0;

		for (int i = 0; i < len; i += 3)
			if (i != i1) {
				int r = (data[i] & 0xff);
				int g = (data[i + 1] & 0xff);
				int b = (data[i + 2] & 0xff);

				final float luma = 1 - Math.abs(Gfx.getLuma(r, g, b) - l1) / 255;
				
				// vector base sr, sg, sb
				r -= sr;
				g -= sg;
				b -= sb;
				
				final float dist = Gfx.euclideanDistance(r1, g1, b1, r, g, b);
				// try to find most distant opposite vector (similarity = -1)
				final float sim = cosineSimilarity(new int[] { r1, g1, b1 }, new int[] { r, g, b });
				
				if (dist * sim * luma < m) {
					i2 = i;
					m = dist;
				}
			}

		final int r2 = (data[i2] & 0xff) - sr;
		final int g2 = (data[i2 + 1] & 0xff) - sg;
		final int b2 = (data[i2 + 2] & 0xff) - sb;

		m = Integer.MIN_VALUE;
		int i3 = 0;

		for (int i = 0; i < len; i += 3)
			if (i != i1 && i != i2) {
				int r = data[i] & 0xff;
				int g = data[i + 1] & 0xff;
				int b = data[i + 2] & 0xff;
				
				final float luma = 1 - Math.abs(Gfx.getLuma(r, g, b) - l1) / 255;
				
				// vector base sr, sg, sb
				r -= sr;
				g -= sg;
				b -= sb;

				final float sim = cosineSimilarity(new int[] { r2, g2, b2 }, new int[] { r, g, b });
				// try to find most distant orthogonal vector (similarity = 0)
				final float dist = Gfx.euclideanDistance(r2, g2, b2, r, g, b);;
				
				if (dist * luma * (1 - Math.abs(sim)) > m) {
					i3 = i;
					m = dist;
				}
			}

		final int r3 = (data[i3] & 0xff) - sr;
		final int g3 = (data[i3 + 1] & 0xff) - sg;
		final int b3 = (data[i3 + 2] & 0xff) - sb;

		m = Integer.MAX_VALUE;
		int i4 = 0;

		for (int i = 0; i < len; i += 3)
			if (i != i1 && i != i2 && i != i3) {
				int r = data[i] & 0xff;
				int g = data[i + 1] & 0xff;
				int b = data[i + 2] & 0xff;
				
				final float luma = 1 - Math.abs(Gfx.getLuma(r, g, b) - l1) / 255;
				
				// vector base sr, sg, sb
				r -= sr;
				g -= sg;
				b -= sb;

				final float dist = Gfx.euclideanDistance(r3, g3, b3, r, g, b);
				// try to find most distant opposite vector (similarity = -1)
				final float sim = cosineSimilarity(new int[] { r3, g3, b3 }, new int[] { r, g, b });
				if (dist * sim * luma < m) {
					i4 = i;
					m = dist;
				}
			}

		final int r4 = data[i4] & 0xff;
		final int g4 = data[i4 + 1] & 0xff;
		final int b4 = data[i4 + 2] & 0xff;

		return new int[][] { { r1 + sr, g1 + sg, b1 + sb }, { r2 + sr, g2 + sg, b2 + sb },
				{ r3 + sr, g3 + sg, b3 + sb }, { r4, g4, b4 } };
	}
}
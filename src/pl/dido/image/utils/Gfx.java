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
	public static final int sharpenKernel[][] = new int[][] { { 0, -1, 0 }, { -1, 5, -1 }, { 0, -1, 0 } };
	public static final int embossKernel[][] = new int[][] { { -2, -1, 0 }, { -1, 1, 1 }, { 0, 1, 2 } };
	public static final int edgeKernel[][] = new int[][] { { -1, -1, -1 }, { -1, 8, -1 }, { -1, -1, -1 } };

	public static final int M2x1[][] = new int[][] { { 77, 171 } }; // 3 colors
	public static final int M1x2[][] = new int[][] { { 77 }, { 171 } }; // 3 colors

	public static final int M2x2[][] = new int[][] { // 5 colors
			{ 0, 2 }, { 3, 1 } };

	public static final int M4x4[][] = new int[][] { // 17 colors
			{ 0, 8, 2, 10 }, { 12, 4, 14, 6 }, { 3, 11, 1, 9 }, { 15, 7, 13, 5 } };

	public static final int M8x8[][] = new int[][] { // 65 colors
			{ 0, 32, 8, 40, 2, 34, 10, 42 }, { 48, 16, 56, 24, 50, 18, 58, 26 }, { 12, 44, 4, 36, 14, 46, 6, 38 },
			{ 60, 28, 52, 20, 62, 30, 54, 22 }, { 3, 35, 11, 43, 1, 33, 9, 41 }, { 51, 19, 59, 27, 49, 17, 57, 25 },
			{ 15, 47, 7, 39, 13, 45, 5, 37 }, { 63, 31, 55, 23, 61, 29, 53, 21 } };

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

	public static final int BLUE16x16[][] = new int[][] {
			{ 68, 158, 7, 145, 21, 253, 105, 81, 6, 153, 186, 130, 222, 149, 211, 25 },
			{ 249, 200, 83, 227, 122, 40, 166, 213, 117, 205, 71, 17, 86, 167, 1, 94 },
			{ 44, 133, 109, 176, 53, 187, 135, 63, 31, 174, 250, 108, 51, 238, 140, 188 },
			{ 61, 214, 29, 237, 93, 2, 224, 241, 87, 144, 39, 216, 193, 74, 113, 229 },
			{ 126, 13, 160, 69, 202, 151, 24, 101, 197, 11, 163, 131, 23, 155, 35, 173 },
			{ 183, 100, 252, 141, 80, 124, 170, 47, 119, 184, 58, 91, 204, 247, 10, 88 },
			{ 206, 48, 18, 191, 34, 230, 207, 67, 254, 220, 75, 232, 123, 103, 64, 223 },
			{ 77, 150, 116, 218, 55, 111, 9, 139, 157, 32, 3, 147, 177, 41, 161, 136 },
			{ 5, 234, 178, 89, 164, 242, 98, 181, 82, 112, 199, 50, 245, 16, 192, 240 },
			{ 107, 37, 132, 26, 65, 195, 22, 43, 236, 168, 95, 134, 209, 84, 115, 54 },
			{ 198, 72, 210, 248, 120, 146, 226, 128, 212, 57, 19, 225, 70, 30, 154, 171 },
			{ 20, 99, 152, 182, 0, 52, 78, 159, 8, 189, 148, 102, 180, 125, 215, 255 },
			{ 142, 228, 46, 85, 219, 201, 106, 246, 66, 118, 251, 42, 231, 4, 92, 62 },
			{ 12, 165, 114, 27, 169, 137, 36, 179, 90, 28, 203, 76, 162, 138, 49, 190 },
			{ 221, 129, 243, 60, 97, 233, 15, 127, 217, 143, 172, 14, 110, 196, 244, 79 },
			{ 104, 38, 185, 208, 73, 156, 194, 56, 239, 45, 96, 235, 59, 33, 121, 175 } };

	public static final int BLUE8x8[][] = new int[][] { { 53, 24, 12, 1, 17, 25, 4, 59 },
			{ 40, 34, 50, 28, 56, 37, 47, 8 }, { 2, 21, 58, 9, 33, 52, 13, 31 }, { 16, 42, 46, 5, 18, 44, 22, 62 },
			{ 55, 11, 27, 61, 39, 0, 26, 49 }, { 36, 32, 23, 54, 14, 35, 57, 6 }, { 19, 3, 48, 7, 30, 51, 10, 45 },
			{ 15, 63, 38, 43, 60, 20, 41, 29 } };

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
		pixels[i + 0] = (byte) (b > 0 ? (b > 255 ? 255 : b) : 0);
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

		g.setPaint(new Color(0, 0, 0)); // black background
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
	
	public static float triangularDistribution(final float a, final float c, final float b) {
        final float u = (float) Math.random();
        final float fc = (c - a) / (b - a);

        if (u < fc)
            return a + (float)Math.sqrt(u * (b - a) * (c - a));
        else
            return b - (float)Math.sqrt((1 - u) * (b - a) * (b - c));
    }
	
	public static void bayerDithering(final byte pixels[], final int palette[][], final int colorBitDepth, final Config config)  {
		final int colors = (int) (colorBitDepth + (-config.error_threshold) * 0.15f * colorBitDepth);

		final DITHERING dithering = config.dither_alg;
		final NEAREST_COLOR colorAlg = config.color_alg;

		final int screenWidth = config.getScreenWidth();
		final int screenHeight = config.getScreenHeight();
		
		switch (dithering) {
		case BAYER2x2:
			Gfx.bayer2x2(pixels, palette, colorAlg, screenWidth, screenHeight, colors);
			break;
		case BAYER4x4:
			Gfx.bayer4x4(pixels, palette, colorAlg, screenWidth, screenHeight, colors);
			break;
		case BAYER8x8:
			Gfx.bayer8x8(pixels, palette, colorAlg, screenWidth, screenHeight, colors);
			break;
		case BAYER16x16:
			Gfx.bayer16x16(pixels, palette, colorAlg, screenWidth, screenHeight, colors);
			break;
		case BLUE8x8:
			Gfx.blue8x8(pixels, palette, colorAlg, screenWidth, screenHeight, colors);
			break;
		case BLUE16x16:
			Gfx.blue16x16(pixels, palette, colorAlg, screenWidth, screenHeight, colors);
			break;
		default:
			break;
		}
	}
	
	public static void downsampling(final byte pixels[], final int bitsPerColor, final int error) {
		int x = 1 << bitsPerColor - 1;
		final int y = 1 << (8 - bitsPerColor);
		
		float or = 0;
		float og = 0;
		float ob = 0;
		
		float er = 0;
		float eg = 0;
		float eb = 0;
		
		float prvr = 0, prvg = 0, prvb = 0;
		
		for (int i = 0; i < pixels.length; i += 3) {
			or = (int) (pixels[i + 0] & 0xff);
			og = (int) (pixels[i + 1] & 0xff);
			ob = (int) (pixels[i + 2] & 0xff);
			
			// error feedback
			int r0 = (int) (or - error * 0.05f * er);
			int g0 = (int) (og - error * 0.05f * eg);
			int b0 = (int) (ob - error * 0.05f * eb);
			
			float rnd = (float) (2 * Math.random() - 1);
			final int r = Math.round(r0 + x * (rnd - prvr)) / y * y;
			
			er = r - or;
			prvr = rnd;
			
			rnd = (float) (2 * Math.random() - 1);
			final int g = Math.round(g0 + x * (rnd - prvg)) / y * y;
			
			eg = g - og;
			prvg = rnd;
			
			rnd = (float) (2 * Math.random() - 1);
			final int b = Math.round(b0 + x * (rnd - prvb)) / y * y;
			
			eb = b - ob;
			prvb = rnd;
			
			pixels[i + 0] = (byte) (r < 0 ? 0 : r > 255 ? 255 : r);
			pixels[i + 1] = (byte) (g < 0 ? 0 : g > 255 ? 255 : g);
			pixels[i + 2] = (byte) (b < 0 ? 0 : b > 255 ? 255 : b);	
		}
	}

	public static void errorDiffuseDithering(final byte pixels[], final int palette[][], final Config config) {
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
		bayerByte(M16x16, pixels, palette, colorAlg, width, height, bpp);
	}

	public static void bayer8x8(final byte pixels[], final int palette[][], final NEAREST_COLOR colorAlg,
			final int width, final int height, final int bpp) {
		bayerByte(M8x8, pixels, palette, colorAlg, width, height, bpp);
	}

	public static void bayer4x4(final int pixels[], final int palette[][], final NEAREST_COLOR colorAlg,
			final int width, final int height, final int bpp) {
		bayerInt(M4x4, pixels, palette, colorAlg, width, height, bpp);
	}

	public static void bayer4x4(final byte pixels[], final int palette[][], final NEAREST_COLOR colorAlg,
			final int width, final int height, final int bpp) {
		bayerByte(M8x8, pixels, palette, colorAlg, width, height, bpp);
	}

	public static void bayer2x2(final byte pixels[], final int palette[][], final NEAREST_COLOR colorAlg,
			final int width, final int height, final int bpp) {
		bayerByte(M2x2, pixels, palette, colorAlg, width, height, bpp);
	}

	public static void bayer2x1(final byte pixels[], final int palette[][], final NEAREST_COLOR colorAlg,
			final int width, final int height, final int bpp) {
		bayerByte(M2x1, pixels, palette, colorAlg, width, height, bpp);
	}

	public static void bayer1x2(final byte pixels[], final int palette[][], final NEAREST_COLOR colorAlg,
			final int width, final int height, final int bpp) {
		bayerByte(M1x2, pixels, palette, colorAlg, width, height, bpp);
	}

	public static void bayerInt(final int matrix[][], final int pixels[], final int palette[][],
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

	public static void bayerByte(final int matrix[][], final byte pixels[], final int palette[][],
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

	public static void blue16x16(final int pixels[], final int palette[][], final NEAREST_COLOR colorAlg,
			final int width, final int height, final int error) {

		for (int y = 0; y < height; y++) {
			final int width3 = width * 3;

			for (int x = 0; x < width; x++) {
				final int pyx = y * width3 + x * 3;

				int r = pixels[pyx];
				int g = pixels[pyx + 1];
				int b = pixels[pyx + 2];

				r = Gfx.bayer(BLUE16x16, x, y, r, error);
				g = Gfx.bayer(BLUE16x16, x, y, g, error);
				b = Gfx.bayer(BLUE16x16, x, y, b, error);

				final int color = Gfx.getColorIndex(colorAlg, palette, r, g, b);

				pixels[pyx] = (byte) palette[color][0];
				pixels[pyx + 1] = (byte) palette[color][1];
				pixels[pyx + 2] = (byte) palette[color][2];
			}
		}
	}

	public static void blue16x16(final byte pixels[], final int palette[][], final NEAREST_COLOR colorAlg,
			final int width, final int height, final int error) {

		for (int y = 0; y < height; y++) {
			final int width3 = width * 3;

			for (int x = 0; x < width; x++) {
				final int pyx = y * width3 + x * 3;

				int r = pixels[pyx] & 0xff;
				int g = pixels[pyx + 1] & 0xff;
				int b = pixels[pyx + 2] & 0xff;

				r = Gfx.bayer(BLUE16x16, x, y, r, error);
				g = Gfx.bayer(BLUE16x16, x, y, g, error);
				b = Gfx.bayer(BLUE16x16, x, y, b, error);

				final int color = Gfx.getColorIndex(colorAlg, palette, r, g, b);

				pixels[pyx] = (byte) palette[color][0];
				pixels[pyx + 1] = (byte) palette[color][1];
				pixels[pyx + 2] = (byte) palette[color][2];
			}
		}
	}

	public static void blue8x8(final int pixels[], final int palette[][], final NEAREST_COLOR colorAlg,
			final int width, final int height, final int error) {

		for (int y = 0; y < height; y++) {
			final int width3 = width * 3;

			for (int x = 0; x < width; x++) {
				final int pyx = y * width3 + x * 3;

				int r = pixels[pyx];
				int g = pixels[pyx + 1];
				int b = pixels[pyx + 2];

				r = Gfx.bayer(BLUE8x8, x, y, r, error);
				g = Gfx.bayer(BLUE8x8, x, y, g, error);
				b = Gfx.bayer(BLUE8x8, x, y, b, error);

				final int color = Gfx.getColorIndex(colorAlg, palette, r, g, b);

				pixels[pyx] = (byte) palette[color][0];
				pixels[pyx + 1] = (byte) palette[color][1];
				pixels[pyx + 2] = (byte) palette[color][2];
			}
		}
	}

	public static void blue8x8(final byte pixels[], final int palette[][], final NEAREST_COLOR colorAlg,
			final int width, final int height, final int error) {

		for (int y = 0; y < height; y++) {
			final int width3 = width * 3;

			for (int x = 0; x < width; x++) {
				final int pyx = y * width3 + x * 3;

				int r = pixels[pyx] & 0xff;
				int g = pixels[pyx + 1] & 0xff;
				int b = pixels[pyx + 2] & 0xff;

				r = Gfx.bayer(BLUE8x8, x, y, r, error);
				g = Gfx.bayer(BLUE8x8, x, y, g, error);
				b = Gfx.bayer(BLUE8x8, x, y, b, error);

				final int color = Gfx.getColorIndex(colorAlg, palette, r, g, b);

				pixels[pyx] = (byte) palette[color][0];
				pixels[pyx + 1] = (byte) palette[color][1];
				pixels[pyx + 2] = (byte) palette[color][2];
			}
		}
	}

	public static void lowpassFilter(final byte pixels[], final float gain) {
		int r = pixels[0] & 0xff;
		int g = pixels[1] & 0xff;
		int b = pixels[2] & 0xff;

		for (int i = 3; i < pixels.length; i += 3) {
			r = (int) ((gain * r + (pixels[i + 0] & 0xff)) / (gain + 1));
			g = (int) ((gain * g + (pixels[i + 1] & 0xff)) / (gain + 1));
			b = (int) ((gain * b + (pixels[i + 2] & 0xff)) / (gain + 1));

			pixels[i + 0] = (byte) r;
			pixels[i + 1] = (byte) g;
			pixels[i + 2] = (byte) b;
		}
	}

	public static void blend(final byte pixels1[], final byte pixels2[]) {
		for (int i = 0; i < pixels1.length; i += 3) {
			final int r1 = pixels1[i + 0] & 0xff;
			final int g1 = pixels1[i + 1] & 0xff;
			final int b1 = pixels1[i + 2] & 0xff;

			final int r2 = pixels2[i + 0] & 0xff;
			final int g2 = pixels2[i + 1] & 0xff;
			final int b2 = pixels2[i + 2] & 0xff;

			pixels1[i + 0] = (byte) Gfx.saturate(r1 - r2 / 4);
			pixels1[i + 1] = (byte) Gfx.saturate(g1 - g2 / 4);
			pixels1[i + 2] = (byte) Gfx.saturate(b1 - b2 / 4);
		}
	}

	protected static void filter(final byte pixels[], final int kernel[][], final int width, final int height) {
		final byte work[] = new byte[pixels.length];
		final int width3 = width * 3;

		final int yuv[] = new int[3];
		final byte rgb[] = new byte[3];

		for (int y = 0; y < height; y++)
			for (int x = 0; x < width; x++) {
				int w = 0;

				for (int yk = 0; yk < 3; yk++) {
					final int yp = y + yk - 1;
					if (yp < 0 || yp >= height - 1)
						continue;

					for (int xk = 0; xk < 3; xk++) {
						final int xp = x + xk - 1;
						if (xp < 0 || xp >= width - 1)
							continue;

						final int p = kernel[yk][xk];
						final int address = yp * width3 + xp * 3;

						int r = (pixels[address + 0] & 0xff);
						int g = (pixels[address + 1] & 0xff);
						int b = (pixels[address + 3] & 0xff);

						Gfx.rgb2YUV(r, g, b, yuv, 0);
						w += p * yuv[0];
					}
				}

				final int address = y * width3 + x * 3;
				int r = (pixels[address + 0] & 0xff);
				int g = (pixels[address + 1] & 0xff);
				int b = (pixels[address + 2] & 0xff);

				Gfx.rgb2YUV(r, g, b, yuv, 0);
				int u = yuv[1];
				int v = yuv[2];

				Gfx.yuv2RGB(w, u, v, rgb, 0);

				work[address + 0] = rgb[0];
				work[address + 1] = rgb[1];
				work[address + 2] = rgb[2];
			}

		System.arraycopy(work, 0, pixels, 0, work.length);
	}

	public static void sharpen(final byte pixels[], final int width, final int height) {
		Gfx.filter(pixels, sharpenKernel, width, height);
	}

	public static void edge(final byte pixels[], final int width, final int height) {
		Gfx.filter(pixels, edgeKernel, width, height);
	}

	public static void emboss(final byte pixels[], final int width, final int height) {
		Gfx.filter(pixels, embossKernel, width, height);
	}

	public final static int bayer2x2(final int x0, final int y0, final int c, final int f, final int b) {
		return c < M2x2[x0 % 2][y0 % 2] ? b : f;
	}

	public final static int bayer2x2RGB(final int x0, final int y0, final int r, final int g, final int b) {
		return getLuma(r, g, b) < M2x2[x0 % 2][y0 % 2] ? 0 : 1;
	}

	public static final int bayer(final int matrix[][], final int x0, final int y0, final int c, final int colors) {
		final int mod = matrix.length;
		final float r = 255f / colors;

		final float cp = c + r * (matrix[y0 % mod][x0 % mod] / (mod * mod * 1f) - 0.5f);
		return (int) (cp > 255f ? 255 : cp < 0 ? 0 : cp);
	}

	public static final int bayer2x2(final int x0, final int y0, final int c, final int colors) {
		return bayer(M2x2, x0, y0, c, colors);
	}

	public static final int bayer4x4(final int x0, final int y0, final int c, final int colors) {
		return bayer(M4x4, x0, y0, c, colors);
	}

	public static final int bayer8x8(final int x0, final int y0, final int c, final int colors) {
		return bayer(M8x8, x0, y0, c, colors);
	}

	public static final int bayer16x16(final int x0, final int y0, final int c, final int colors) {
		return bayer(M16x16, x0, y0, c, colors);
	}
	
	public static final int blue8x8(final int x0, final int y0, final int c, final int colors) {
		return bayer(BLUE8x8, x0, y0, c, colors);
	}

	public static final int blue16x16(final int x0, final int y0, final int c, final int colors) {
		return bayer(BLUE16x16, x0, y0, c, colors);
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

	protected static int getLumaColorIndex(final int palette[][], final int r, final int g, final int b) {
		int index = palette.length;
		float min = Float.MAX_VALUE;

		float y = getLuma(r, g, b);
		final int len = index - 1;

		for (int i = len; i-- > 0;) {
			final int color[] = palette[i];
			final float d = Math.abs(getLuma(color[0], color[1], color[2]) - y);

			if (d < min) {
				min = d;
				index = i;
			}
		}

		return index;
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
			sr += work[i + 0];
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
			r = work[i + 0];
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

	public static void mono(final byte[] pixels) {
		for (int i = 0; i < pixels.length; i += 3) {
			int r = pixels[i + 0] & 0xff;
			int g = pixels[i + 1] & 0xff;
			int b = pixels[i + 2] & 0xff;
			
			r = (int) Gfx.getLuma(b, g, r);
			g = r;
			b = r;
			
			pixels[i + 0] = (byte) r;
			pixels[i + 1] = (byte) g;
			pixels[i + 2] = (byte) b;
		}
	}
}
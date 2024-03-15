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

		final AffineTransformOp transform = new AffineTransformOp(si, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
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

		final AffineTransformOp scale = new AffineTransformOp(si, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
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
	public final static void SWAHE(final byte pixels[], final int window, int brightness,
			final int width, final int height) {
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
	public static final void CLAHE(final byte pixels[], final int window, int brightness,
			final int width, final int height) {

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

		final int yuv[] = new int[3];

		for (int y = 0; y < ymax; y++) {
			final int ywindow = y * window * 3;
			final int ywindow2 = 2 * ywindow;
			final int ywindow21 = (2 * y + 1) * window * 3;

			for (int x = 0; x < window * 3; x += 3) {
				final int ys = ywindow + x;
				final int y0 = ywindow2 + x;
				final int y1 = ywindow21 + x;

				final byte r = pixels[ys];
				final byte g = pixels[ys + 1];
				final byte b = pixels[ys + 2];

				out[y0] = r;
				out[y0 + 1] = g;
				out[y0 + 2] = b;

				if (y < ymax - 1) {
					// scanline as dimmed previous line
					rgb2YUV(r, g, b, yuv, 0);
					yuv2RGB(Math.round(yuv[0] * 0.67f), yuv[1], yuv[2], out, y1);
				}
			}
		}

		return out;
	}

	public static void dithering(final byte pixels[], final int palette[][], final Config config) {
		final int work[] = Gfx.copy2Int(pixels);

		final int width = config.getScreenWidth();
		final int height = config.getScreenHeight();

		final DITHERING dither = config.dither_alg;
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

				switch (dither) {
				case STD_FS:
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
				}
			}
		}
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

	public static int getColorIndex(final NEAREST_COLOR color, final int palette[][], final int r0,
			final int g0, final int b0) {
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

	protected int[] matchingEuclideanColor(final int palette[][], final int r, final int g, final int b) {
		return palette[getEuclideanColorIndex(palette, r, g, b)];
	}

	protected static int getEuclideanColorIndex(final int palette[][], final int r, final int g, final int b) {
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

	protected static int getPerceptedColorIndex(final int palette[][], final int r, final int g,
			final int b) {
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

	protected int[] matchingLumaColor(final int palette[][], final int r, final int g,
			final int b) {
		return palette[getLumaColorIndex(palette, r, g, b)];
	}

	protected static int getLumaColorIndex(final int palette[][], final int r, final int g,
			final int b) {
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

	public static BufferedImage rgb2BGR(final BufferedImage image) {
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
}
package pl.dido.image.utils;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.io.ByteArrayOutputStream;

public class Utils {

	public static final void rgb2YUV(int r, int g, int b, int yuv[], int i) {
		yuv[i] = (int) Math.round(r * .299000 + g * .587000 + b * .114000);
		yuv[i + 1] = (int) Math.round(r * -.168736 + g * -.331264 + b * .500000 + 128);
		yuv[i + 2] = (int) Math.round(r * .500000 + g * -.418688 + b * -.081312 + 128);
	}

	public static final void yuv2RGB(int y, int u, int v, byte pixels[], int i) {
		final float r = y + 1.402f * (v - 128);
		final float g = y - 0.34414f * (u - 128) - 0.71414f * (v - 128);
		final float b = y + 1.772f * (u - 128);

		// clamp to [0,255]
		pixels[i] = (byte) (r > 0 ? (r > 255 ? 255 : r) : 0);
		pixels[i + 1] = (byte) (g > 0 ? (g > 255 ? 255 : g) : 0);
		pixels[i + 2] = (byte) (b > 0 ? (b > 255 ? 255 : b) : 0);
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

	public static final int gammaCorrection(final int component, float gamma) {
		final float ginv = 1 / gamma;

		return (int) (255f * Math.pow(component / 255.0f, ginv));
	}

	public static final float euclideanDistance(final int r, final int g, final int b, final int pr, final int pg,
			final int pb) {
		return (((r - pr) * (r - pr)) + ((g - pg) * (g - pg)) + ((b - pb) * (b - pb)));
	}

	public static final float perceptedDistance(final int r, final int g, final int b, final int pr, final int pg,
			final int pb) {

		final int delta = (r + pr) / 2;
		return (((2 + delta / 256) * (r - pr) * (r - pr)) + (4 * (g - pg) * (g - pg))
				+ ((2 + (255 - delta) / 256) * (b - pb) * (b - pb)));
	}

	public static final float getLuma(final int r, final int g, final int b) {
		return 0.299f * r + 0.587f * g + 0.114f * b;
	}

	public static final float euclideanDistance(final float pr, final float pg, final float pb, final float r,
			final float g, final float b) {
		return ((r - pr) * (r - pr)) + ((g - pg) * (g - pg)) + ((b - pb) * (b - pb));
	}

	public static final float perceptedDistance(final float r, final float g, final float b, final float pr,
			final float pg, final float pb) {

		final float delta = (r + pr) / 2;
		return (((2 + delta / 256) * (r - pr) * (r - pr)) + (4 * (g - pg) * (g - pg))
				+ ((2 + (255 - delta) / 256) * (b - pb) * (b - pb)));
	}

	public static final int max(final int a, final int b, final int c) {
		final int w = a > b ? a : b;
		return w > c ? w : c;
	}

	public static final float min(final float a, final float b, final float c) {
		final float w = a < b ? a : b;
		return w < c ? w : c;
	}

	public static final BufferedImage scale(final BufferedImage image, final int maxX, final int maxY) {
		final int x = image.getWidth();
		final int y = image.getHeight();

		final double sx = maxX / (double) x;
		final double sy = maxY / (double) y;

		final BufferedImage scaled = new BufferedImage(maxX, maxY, image.getType());
		final AffineTransform si = AffineTransform.getScaleInstance(sx, sy);

		final AffineTransformOp transform = new AffineTransformOp(si, AffineTransformOp.TYPE_BILINEAR);
		transform.filter(image, scaled);

		return scaled;
	}

	public static InputStream getResourceAsStream(final String fileName) {
		final ClassLoader classLoader = Utils.class.getClassLoader();
		return classLoader.getResourceAsStream(fileName);
	}

	public static URL getResourceAsURL(final String fileName) {
		final ClassLoader classLoader = Utils.class.getClassLoader();
		return classLoader.getResource(fileName);
	}

	public static String createDirectory(final String directory) throws IOException {
		final Path path = Paths.get(directory);
		return (!Files.isDirectory(path)) ? Files.createDirectory(path).toString() : path.toString();
	}

	public static byte[] loadCharset(final InputStream is) throws IOException {
		final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

		int nRead;
		final byte[] data = new byte[8];

		while ((nRead = is.read(data, 0, data.length)) != -1)
			buffer.write(data, 0, nRead);

		buffer.flush();
		return buffer.toByteArray();
	}

	private static final int lumaBlend(final int l1, final int l2) {
		return (l1 * 9 + l2) / 10;
	}

	private static final int cdfScale(final int cdf[], final int luma, final int max) {
		return (cdf[luma] * 255) / cdf[max];
	}

	private static final void rgb2YUV(final int type, final int r, final int g, final int b, final int yuv[],
			final int i) {
		switch (type) {
		case BufferedImage.TYPE_3BYTE_BGR:
			Utils.rgb2YUV(b, g, r, yuv, i);
			break;
		case BufferedImage.TYPE_INT_RGB:
			Utils.rgb2YUV(r, g, b, yuv, i);
			break;
		default:
			throw new RuntimeException("Unsupported pixel format !!!");
		}
	}

	private static final void yuv2RGB(final int type, final int y, final int u, final int v, final byte[] pixels,
			final int i) {
		Utils.yuv2RGB(y, u, v, pixels, i);

		switch (type) {
		case BufferedImage.TYPE_3BYTE_BGR:
			final byte t = pixels[i];
			pixels[i] = pixels[i + 2];
			pixels[i + 2] = t;

			break;
		}
	}

	private static final void calcCdf(final int cdf[], final int histogram[]) {

		// cdf - cumulative distributed function
		cdf[0] = histogram[0];
		for (int i = 1; i < 256; i++)
			cdf[i] = cdf[i - 1] + histogram[i];
	}

	public static final void HE(final byte pixels[], final int pixelFormat) {
		final int histogram[] = new int[256];
		final int cdf[] = new int[256];

		final int len = pixels.length;
		final int yuv[] = new int[len];

		int r, g, b, max = 0;
		for (int i = 0; i < len; i += 3) {
			r = pixels[i] & 0xff;
			g = pixels[i + 1] & 0xff;
			b = pixels[i + 2] & 0xff;

			rgb2YUV(pixelFormat, r, g, b, yuv, i);
			final int luma = yuv[i]; 
			histogram[luma]++;

			if (luma > max)
				max = luma;
		}
		
		calcCdf(cdf, histogram);

		for (int i = 0; i < len; i += 3)
			yuv2RGB(pixelFormat, cdfScale(cdf, yuv[i], max), yuv[i + 1], yuv[i + 2], pixels, i);
	}
	
	private static final void clipHistogram(final int histogram[], final int brightness) {
		// clip the brightest
		int clippedCount;

		// distribute surplus
		do {
			int total = 0;
			clippedCount = 0;

			for (int i = 0; i < 256; i++)
				// clip if is above dmax
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
	public final static void SWAHE(final byte pixels[], final int pixelFormat, final int window, int brightness,
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

					for (int xw = -midX; xw < midX; xw += 3) {
						int x0 = x + xw;
						if (x0 < 0) // left corner
							x0 += midX;
						else if (x0 > maxX1)
							x0 -= midX;

						// screen position
						final int sp = x0 + y0 * maxX;

						r = pixels[sp] & 0xff;
						g = pixels[sp + 1] & 0xff;
						b = pixels[sp + 2] & 0xff;

						final int luma;
						// mind pixel format & convert RGB to YUV
						if (xw == 0 && yw == 0) {
							wp = sp;

							rgb2YUV(pixelFormat, r, g, b, yuv, 3);
							luma = yuv[3];

							histogram[luma]++; // add center pixel to histogram
						} else {
							rgb2YUV(pixelFormat, r, g, b, yuv, 0);
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
				yuv2RGB(pixelFormat, cdfScale(cdf, yuv[3], max), yuv[3 + 1], yuv[3 + 2], pixels, wp);
			}
	}

	// CLAHE
	public static final void CLAHE(final byte pixels[], final int pixelFormat, final int window,
			int brightness, final int width, final int height) {

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
		final int type = pixelFormat;
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

					for (int xw = 0; xw < window; xw++) {
						final int x0 = x + xw;

						// screen position
						final int sp = 3 * (x0 + y0 * maxX);

						r = pixels[sp] & 0xff;
						g = pixels[sp + 1] & 0xff;
						b = pixels[sp + 2] & 0xff;

						rgb2YUV(type, r, g, b, yuv, sp);

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
		for (int y = midY; y < maxY - midY; y += window)
			for (int x = midX; x < maxX - midX; x += window) {
				final int x1 = x;
				final int y1 = y;

				final int x2 = x + window;
				final int y2 = y + window;

				final int wp1 = (x1 + y1 * maxX) * 3;
				final int wp2 = (x2 + y1 * maxX) * 3;
				final int wp3 = (x1 + y2 * maxX) * 3;
				final int wp4 = (x2 + y2 * maxX) * 3;

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

						yuv2RGB(type, lumaBlend(yuv[sp], a), yuv[sp + 1], yuv[sp + 2], pixels, sp);
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

				final int wp1 = (x1 + y1 * maxX) * 3;
				final int wp2 = (x2 + y1 * maxX) * 3;

				final int wp3 = (x1 + y2 * maxX) * 3;
				final int wp4 = (x2 + y2 * maxX) * 3;

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

					yuv2RGB(type, lumaBlend(yuv[sp1], a1), yuv[sp1 + 1], yuv[sp1 + 2], pixels, sp1);
					yuv2RGB(type, lumaBlend(yuv[sp2], a2), yuv[sp2 + 1], yuv[sp2 + 2], pixels, sp2);
				}
			}
		}

		// left right
		for (int y = midY; y < maxY - midY; y += window) {
			final int x1 = midX;
			final int x2 = maxX - midX;

			for (int yw = y; yw < y + window; yw++) {
				final int y1 = y;
				final int y2 = y + window;

				final int wp1 = (x1 + y1 * maxX) * 3;
				final int wp2 = (x2 + y1 * maxX) * 3;

				final int wp3 = (x1 + y2 * maxX) * 3;
				final int wp4 = (x2 + y2 * maxX) * 3;

				final int l1 = lumas[wp1];
				final int l2 = lumas[wp2];

				final int l3 = lumas[wp3];
				final int l4 = lumas[wp4];

				for (int xw1 = 0; xw1 < midX; xw1++) {
					float dx21 = x2 - x1;
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

					yuv2RGB(type, lumaBlend(yuv[sp1], a1), yuv[sp1 + 1], yuv[sp1 + 2], pixels, sp1);
					yuv2RGB(type, lumaBlend(yuv[sp2], a2), yuv[sp2 + 1], yuv[sp2 + 2], pixels, sp2);
				}
			}
		}
	}
}
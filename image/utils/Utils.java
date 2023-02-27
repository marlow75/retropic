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

public class Utils {

	protected final static int qIteration(final int iteration, final int partition[][], int partitionIndex, 
			final int[] red, final int[] green, final int[] blue, final int begin, final int end) {
		
		if (iteration == 3)
			return partitionIndex;

		if (iteration == 2) {
			partition[partitionIndex][0] = begin;
			partition[partitionIndex][1] = end;
			
			partitionIndex++;
		}

		int ar = 0, ag = 0, ab = 0;
		int r_min = red[begin], r_max = r_min;

		int g_min = green[begin], g_max = g_min;
		int b_min = blue[begin], b_max = b_min;

		for (int i = begin; i < end; i++) {
			ar += red[i];
			ag += green[i];
			ab += blue[i];
		}

		final int len = end - begin;
		ar /= len; ag /= len; ab /= len;

		int rv = 0, gv = 0, bv = 0;

		for (int i = begin; i < end; i++) {
			int p = red[i];

			if (p > r_max)
				r_max = p;
			else
			if (p < r_min)
				r_min = p;

			rv += (p - ar) * (p - ar);
			p = green[i];

			if (p > g_max)
				g_max = p;
			else
			if (p < g_min)
				g_min = p;

			gv += (p - ag) * (p - ag);
			p = blue[i];

			if (p > b_max)
				b_max = p;
			else
			if (p < b_min)
				b_min = p;

			bv += (p - ab) * (p - ab);
		}

		rv *= r_max - r_min;
		gv *= g_max - g_min;
		bv *= b_max - b_min;

		int result = rv > gv ? rv : gv;
		result = result > bv ? result : bv;

		if (gv == result)
			Utils.qsort(green, blue, red, begin, end);
		else 
		if (rv == result)
			Utils.qsort(red, green, blue, begin, end);
		else 
		if (bv == result)
			Utils.qsort(blue, green, red, begin, end);
		
		final int cutPoint = len >> 1;
		partitionIndex = qIteration(iteration + 1, partition, partitionIndex, red, green, blue, begin, begin + cutPoint - 1);
		return qIteration(iteration + 1, partition, partitionIndex, red, green, blue, begin + cutPoint, end);
	}

	public static final int[][] colorQuantization(final int[] work, final int colors, final int red[], final int green[], final int blue[]) {		
		final int partition[][] = new int[colors][2];
		final int size = work.length / 3;
		
		int r_min = 255, r_max = 0;
		int g_min = 255, g_max = 0;
		int b_min = 255, b_max = 0;

		int ar = 0, ag = 0, ab = 0;

		// color range
		for (int i = 0, j = 0; j < size; i += 3, j++) {
			int p = work[i];
			red[j] = p;
			ar += p;

			if (p > r_max)
				r_max = p;
			else
			if (p < r_min)
				r_min = p;

			p = work[i + 1];
			green[j] = p;
			ag += p;

			if (p > g_max)
				g_max = p;
			else
			if (p < g_min)
				g_min = p;

			p = work[i + 2];
			blue[j] = p;
			ab += p;

			if (p > b_max)
				b_max = p;
			else
			if (p < b_min)
				b_min = p;
		}

		ar /= size;
		ag /= size;
		ab /= size;

		int rv = 0, gv = 0, bv = 0;

		// variance calculation
		for (int j = 0; j < size; j++) {
			int p = red[j];
			rv += (ar - p) * (ar - p);

			p = green[j];
			gv += (ag - p) * (ag - p);

			p = blue[j];
			bv += (ab - p) * (ab - p);
		}

		rv *= r_max - r_min;
		gv *= g_max - g_min;
		bv *= b_max - b_min;

		int result = rv > gv ? rv : gv;
		result = result > bv ? result : bv;

		final int len = green.length - 1;

		if (gv == result)
			Utils.qsort(green, blue, red, 0, len);
		else if (rv == result)
			Utils.qsort(red, green, blue, 0, len);	
		else if (bv == result)
			Utils.qsort(blue, green, red, 0, len);

		final int cutPoint = len >> 1;

		final int partitionIndex = qIteration(1, partition, 0, red, green, blue, 0, cutPoint - 1);
		qIteration(1, partition, partitionIndex, red, green, blue, cutPoint, len);
		
		final int palette[][] = new int[colors][3];
		for (int i = 0; i < colors; i++) {
			final int begin = partition[i][0];
			final int end = partition[i][1];

			int sr = 0, sg = 0, sb = 0;

			for (int j = begin; j < end; j++) {
				sr += red[j];
				sg += green[j];
				sb += blue[j];
			}
			final int sum = end - begin;

			palette[i][0] = sr / sum;
			palette[i][1] = sg / sum;
			palette[i][2] = sb / sum;
		}

		return palette;
	}

	private final static int partition(final int a[], final int b[], final int c[], final int begin, final int end) {
		final int pivot = a[end];
		int i = begin - 1;
		int swap;

		for (int j = begin; j < end; j++) {
			if (a[j] <= pivot) {
				swap = a[++i];
				a[i] = a[j];
				a[j] = swap;

				swap = b[i];
				b[i] = b[j];
				b[j] = swap;

				if (c != null) {
					swap = c[i];
					c[i] = c[j];
					c[j] = swap;
				}
			}
		}

		swap = a[++i];
		a[i] = a[end];
		a[end] = swap;

		swap = b[i];
		b[i] = b[end];
		b[end] = swap;

		if (c != null) {
			swap = c[i];
			c[i] = c[end];
			c[end] = swap;
		}

		return i;
	}

	public static final void qsort(final int a[], final int b[], final int c[], final int begin, final int end) {
		if (begin < end) {
			final int partitionIndex = partition(a, b, c, begin, end);

			qsort(a, b, c, begin, partitionIndex - 1);
			qsort(a, b, c, partitionIndex + 1, end);
		}
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
			array[i]     = pixels[i] & 0xff;
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
			array[i]     = pixels[i] & 0xff;
			array[i + 1] = pixels[i + 1] & 0xff;
			array[i + 2] = pixels[i + 2] & 0xff;
			array[i + 3] = pixels[i + 3] & 0xff;
		}
		
		return array;
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

	public static final float euclideanDistance(final float pr, final float pg, final float pb, 
			final float r, final float g, final float b) {
		return ((r - pr) * (r - pr)) + ((g - pg) * (g - pg)) + ((b - pb) * (b - pb));
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
}
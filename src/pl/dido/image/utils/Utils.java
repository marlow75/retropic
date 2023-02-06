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

	public static final int euclideanDistance(final int r, final int g, final int b, final int pr, final int pg,
			final int pb) {
		return ((r - pr) * (r - pr)) + ((g - pg) * (g - pg)) + ((b - pb) * (b - pb));
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
		if (!Files.isDirectory(path))
			return Files.createDirectory(path).toString();
		else
			return path.toString();
	}
}
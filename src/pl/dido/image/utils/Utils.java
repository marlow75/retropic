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
import java.io.ByteArrayOutputStream;

public class Utils {

	public static final void rgb2YUV(int r, int g, int b, int yuv[], int i) {
		yuv[i] = (int) Math.round(r * .299000 + g * .587000 + b * .114000);
		yuv[i + 1] = (int) Math.round(r * -.168736 + g * -.331264 + b * .500000 + 128);
		yuv[i + 2] = (int) Math.round(r * .500000 + g * -.418688 + b * -.081312 + 128);
	}

	public static final void yuv2RGB(int y, int u, int v, byte pixels[], int i) {
		final int COLOR_MAX = 255;

		float r = y + 1.402f * (v - 128);
		float g = y - 0.34414f * (u - 128) - 0.71414f * (v - 128);
		float b = y + 1.772f * (u - 128);

		// clamp to [0,255]
		pixels[i] = (byte) Math.max(0, Math.min(COLOR_MAX, r));
		pixels[i + 1] = (byte) Math.max(0, Math.min(COLOR_MAX, g));
		pixels[i + 2] = (byte) Math.max(0, Math.min(COLOR_MAX, b));
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
}
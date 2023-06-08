package pl.dido.image.renderer;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.Arrays;

import javax.swing.JFrame;
import javax.swing.JMenuBar;

import pl.dido.image.Config;
import pl.dido.image.utils.Utils;

public abstract class AbstractRenderer extends Thread {

	protected int palette[][];
	protected byte pixels[];

	protected int width;
	protected int height;

	protected JFrame frame;
	protected int[] work = null;

	protected BufferedImage image;
	protected Canvas canvas;

	protected BufferStrategy bufferStrategy;
	protected Config config;

	protected String fileName;
	protected boolean windowVisible = false;

	public AbstractRenderer(final BufferedImage image, final String fileName, final Config config) {
		// copy of configuration
		try {
			this.config = (Config) config.clone();
		} catch (final CloneNotSupportedException ex) {
			this.config = config;
		}

		this.image = scaleImage(image);
		initializeView();

		pixels = ((DataBufferByte) this.image.getRaster().getDataBuffer()).getData();
		this.fileName = fileName;
	}

	protected BufferedImage scaleImage(final BufferedImage image) {
		final int maxx = getWidth();
		final int maxy = getHeight();

		if (image.getWidth() != maxx || image.getHeight() != maxy)
			return Utils.scale(image, maxx, maxy);

		return image;
	}

	protected void initializeView() {
		width = image.getWidth();
		height = image.getHeight();

		frame = new JFrame(getTitle() + config.getConfigString());
		frame.setJMenuBar(getMenuBar());
		frame.setIconImage(Toolkit.getDefaultToolkit().getImage(Utils.getResourceAsURL("retro.png")));

		frame.addWindowListener(new ImageWindowListener(this));

		frame.setSize(getScreenWidth(), getScreenHeight());
		frame.setLocationRelativeTo(null);

		frame.setResizable(false);
		frame.setVisible(true);

		canvas = new PictureCanvas();
		frame.setLocationRelativeTo(null);

		canvas.setSize(getScreenWidth(), getScreenHeight());
		canvas.setBackground(Color.BLACK);

		canvas.setVisible(true);
		canvas.setFocusable(false);

		frame.add(canvas);
		frame.pack();

		canvas.createBufferStrategy(2);
		bufferStrategy = canvas.getBufferStrategy();
	}

	protected void showImage() {
		final Graphics gfx = bufferStrategy.getDrawGraphics();

		gfx.drawImage(image, 0, 0, canvas.getWidth(), canvas.getHeight(), 0, 0, width, height, null);
		gfx.dispose();

		bufferStrategy.show();
	}

	public void run() {
		while (!windowVisible)
			try {
				Thread.sleep(100);
			} catch (final InterruptedException e) {
				e.printStackTrace();
			}

		try {
			canvas.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

			// contrast correction
			switch (config.highContrast) {
			case HE:
				HE();
				break;
			case SWAHE:
				switch (image.getType()) {
				case BufferedImage.TYPE_3BYTE_BGR:				
					SWAHEBGR(config.swaheWindowSize);
					break;
				case BufferedImage.TYPE_INT_RGB:
					SWAHERGB(config.swaheWindowSize);
					break;					
				}
				break;
			default:
				break;
			}

			showImage();
			setupPalette();

			if (config.dithering)
				imageDithering();

			imagePostproces();
			addWaterMark();

			// show image after
			showImage();
		} finally {
			canvas.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		}
	}

	protected void addWaterMark() {
		final Graphics gfx = image.createGraphics();

		gfx.setColor(Color.WHITE);
		gfx.setFont(new Font("Tahoma", Font.BOLD, 8));
		gfx.drawString("RetroPIC", width - 40, height - 5);
		gfx.dispose();
	}

	protected abstract void imagePostproces();

	protected abstract void setupPalette();

	protected abstract String getTitle();

	protected abstract JMenuBar getMenuBar();

	protected abstract int getHeight();

	protected abstract int getWidth();

	protected abstract int getScreenHeight();

	protected abstract int getScreenWidth();

	protected void imageDithering() {
		final float work[] = Utils.copy2float(pixels);
		final int width3 = width * 3;

		int r0, g0, b0;
		float r_error = 0, g_error = 0, b_error = 0;

		for (int y = 0; y < height; y++) {
			final int k = y * width3;
			final int k1 = ((y + 1) * width3);
			final int k2 = ((y + 2) * width3);

			for (int x = 0; x < width3; x += 3) {
				final int pyx = k + x;
				final int py1x = k1 + x;
				final int py2x = k2 + x;

				r0 = Utils.saturate((int) work[pyx]);
				g0 = Utils.saturate((int) work[pyx + 1]);
				b0 = Utils.saturate((int) work[pyx + 2]);

				final int color = getColorIndex(r0, g0, b0);
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

				switch (config.dither_alg) {
				case STD_FS:
					if (x < (width - 1) * 3) {
						work[pyx + 3] += r_error * 7 / 16;
						work[pyx + 3 + 1] += g_error * 7 / 16;
						work[pyx + 3 + 2] += b_error * 7 / 16;
					}
					if (y < height - 1) {
						work[py1x - 3] += r_error * 3 / 16;
						work[py1x - 3 + 1] += g_error * 3 / 16;
						work[py1x - 3 + 2] += b_error * 3 / 16;

						work[py1x] += r_error * 5 / 16;
						work[py1x + 1] += g_error * 5 / 16;
						work[py1x + 2] += b_error * 5 / 16;

						if (x < (width - 1) * 3) {
							work[py1x + 3] += r_error / 16;
							work[py1x + 3 + 1] += g_error / 16;
							work[py1x + 3 + 2] += b_error / 16;
						}
					}
					break;
				case ATKINSON:
					if (x < (width - 1) * 3) {
						work[pyx + 3] += r_error * 1 / 8;
						work[pyx + 3 + 1] += g_error * 1 / 8;
						work[pyx + 3 + 2] += b_error * 1 / 8;

						if (x < (width - 2) * 3) {
							work[pyx + 6] += r_error * 1 / 8;
							work[pyx + 6 + 1] += g_error * 1 / 8;
							work[pyx + 6 + 2] += b_error * 1 / 8;
						}
					}
					if (y < height - 1) {
						work[py1x - 3] += r_error * 1 / 8;
						work[py1x - 3 + 1] += g_error * 1 / 8;
						work[py1x - 3 + 2] += b_error * 1 / 8;

						work[py1x] += r_error * 1 / 8;
						work[py1x + 1] += g_error * 1 / 8;
						work[py1x + 2] += b_error * 1 / 8;

						if (x < (width - 1) * 3) {
							work[py1x + 3] += r_error * 1 / 8;
							work[py1x + 3 + 1] += g_error * 1 / 8;
							work[py1x + 3 + 2] += b_error * 1 / 8;
						}

						if (y < height - 2) {
							work[py2x] += r_error * 1 / 8;
							work[py2x + 1] += g_error * 1 / 8;
							work[py2x + 2] += b_error * 1 / 8;
						}
					}
					break;
				}
			}
		}
	}

	protected final float getDistance(final int r0, final int g0, final int b0, final int r1, final int g1,
			final int b1) {
		switch (config.color_alg) {
		case EUCLIDEAN:
			return Utils.euclideanDistance(r0, g0, b0, r1, g1, b1);
		case PERCEPTED:
			return Utils.perceptedDistance(r0, g0, b0, r1, g1, b1);
		default:
			return Utils.euclideanDistance(r0, g0, b0, r1, g1, b1);
		}
	}

	protected int getColorIndex(final int palette[][], final int r0, final int g0, final int b0) {
		switch (config.color_alg) {
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

	protected int getColorIndex(final int r0, final int g0, final int b0) {
		switch (config.color_alg) {
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

	protected int[] matchingEuclideanColor(final int r, final int g, final int b) {
		return matchingEuclideanColor(this.palette, r, g, b);
	}

	protected int[] matchingEuclideanColor(final int palette[][], final int r, final int g, final int b) {
		return palette[getEuclideanColorIndex(palette, r, g, b)];
	}

	protected int getEuclideanColorIndex(final int palette[][], final int r, final int g, final int b) {
		int index = 0;
		float min = Float.MAX_VALUE;
		final int len = palette.length;

		for (int i = len; i-- > 0;) { // euclidean distance
			final int color[] = palette[i];
			final float distance = Utils.euclideanDistance(r, g, b, color[0], color[1], color[2]);

			if (distance < min) {
				min = distance;
				index = i;
			}
		}

		return index;
	}

	protected int getPerceptedColorIndex(final int palette[][], final int r, final int g, final int b) {
		int index = 0;
		float min = Float.MAX_VALUE;

		for (int i = palette.length; i-- > 0;) { // euclidean distance
			final int color[] = palette[i];
			final float distance = perceptedDistanceCM(r, g, b, color[0], color[1], color[2]);

			if (distance < min) {
				min = distance;
				index = i;
			}
		}

		return index;
	}

	protected int[] matchingLumaColor(final int r, final int g, final int b) {
		return palette[getLumaColorIndex(this.palette, r, g, b)];
	}

	protected int[] matchingLumaColor(final int palette[][], final int r, final int g, final int b) {
		return palette[getLumaColorIndex(palette, r, g, b)];
	}

	protected int getLumaColorIndex(final int palette[][], final int r, final int g, final int b) {
		int index = 0, old_index = 0;
		float y1 = 0, oy1 = 0;

		float min = Float.MAX_VALUE;
		final float y = getLumaByCM(r, g, b);
		final int len = palette.length;

		for (int i = len; i-- > 0;) { // euclidean distance
			final int color[] = palette[i];
			final int pr = color[0];
			final int pg = color[1];
			final int pb = color[2];

			final float distance = Utils.euclideanDistance(r, g, b, pr, pg, pb);

			if (distance < min) {
				min = distance;

				old_index = index;
				index = i;

				oy1 = y1;
				y1 = getLumaByCM(pr, pg, pb);
			}
		}

		return Math.abs(y1 - y) < Math.abs(oy1 - y) ? index : old_index;
	}

	protected float perceptedDistanceCM(final int r, final int g, final int b, final int pr, final int pg,
			final int pb) {
		switch (image.getType()) {
		case BufferedImage.TYPE_3BYTE_BGR:
			return Utils.perceptedDistance(b, g, r, pb, pg, pr);
		case BufferedImage.TYPE_INT_RGB:
			return Utils.perceptedDistance(r, g, b, pr, pg, pb);
		default:
			throw new RuntimeException("Unsupported pixel format !!!");
		}
	}

	protected float getLumaByCM(final int pr, final int pg, final int pb) {
		switch (image.getType()) {
		case BufferedImage.TYPE_3BYTE_BGR:
			return Utils.getLuma(pb, pg, pr);
		case BufferedImage.TYPE_INT_RGB:
			return Utils.getLuma(pr, pg, pb);
		default:
			throw new RuntimeException("Unsupported pixel format !!!");
		}
	}

	protected final float getDistanceByCM(final int r1, final int g1, final int b1, final int r2, final int g2,
			final int b2) {
		switch (image.getType()) {
		case BufferedImage.TYPE_3BYTE_BGR:
			return getDistance(b1, g1, r1, b2, g2, r2);
		case BufferedImage.TYPE_INT_RGB:
			return getDistance(r1, g1, b1, r2, g2, b2);
		default:
			throw new RuntimeException("Unsupported pixel format !!!");
		}
	}

	protected final void HE() {
		final int histogram[] = new int[256];
		final int cdf[] = new int[256];

		final int len = pixels.length;
		final int yuv[] = new int[len];

		int r, g, b;
		for (int i = 0; i < len; i += 3) {
			r = pixels[i] & 0xff;
			g = pixels[i + 1] & 0xff;
			b = pixels[i + 2] & 0xff;

			// mind pixel format
			switch (image.getType()) {
			case BufferedImage.TYPE_3BYTE_BGR:
				Utils.rgb2YUV(b, g, r, yuv, i);
				break;
			case BufferedImage.TYPE_INT_RGB:
				Utils.rgb2YUV(r, g, b, yuv, i);
				break;
			default:
				throw new RuntimeException("Unsupported pixel format !!!");
			}

			histogram[yuv[i]]++;
		}

		final int N = len / 3;

		// cdf - cumulative distributed function
		cdf[0] = histogram[0];
		for (int i = 1; i < 256; i++)
			cdf[i] = cdf[i - 1] + histogram[i];

		byte t;
		for (int i = 0; i < len; i += 3) {
			final int luma = Math.min(255, Math.round((cdf[yuv[i]] * 255f) / N));
			Utils.yuv2RGB(luma, yuv[i + 1], yuv[i + 2], pixels, i);

			switch (image.getType()) {
			case BufferedImage.TYPE_3BYTE_BGR:
				t = pixels[i];
				pixels[i] = pixels[i + 2];
				pixels[i + 2] = t;

				break;
			}
		}
	}
	
	// SWAHE for BGR format 
	protected final void SWAHEBGR(final int window) {
		// histogram
		final byte newPixels[] = new byte[pixels.length];

		final int N = window * window;
		final int len = N * 3;

		// cdf & yuv
		final float cdf[] = new float[256];
		final int yuv[] = new int[len];

		final int window3 = 3 * window;
		final int midY = window / 2;
		
		final int midX = window3 / 2;
		final int center = midX + midY * window3;

		final int maxX = getWidth() * 3;
		final int maxY = getHeight();

		final int maxX1 = maxX - 3;
		final int maxY1 = maxY - 1;
		
		final float histogram[] = new float[256];
		final float brightness = config.swaheBrightness * 0.05f;
		
		int r, g, b;

		for (int y = 0; y < maxY; y++)
			for (int x = 0; x < maxX; x += 3) {
				// compute histogram for window
				Arrays.fill(histogram, 0);

				int sp, wp;
				// calculate window
				for (int yw = -midY; yw < midY; yw++) {
					int y0 = y + yw;
					if (y0 < 0) // upper corner
						y0 = -y0;
					else if (y0 > maxY1)
						y0 = maxY1 - midY;

					for (int xw = -midX; xw < midX; xw += 3) {
						int x0 = x + xw;
						if (x0 < 0) // left corner
							x0 = -x0;
						else if (x0 > maxX1)
							x0 = maxX1 - midX;

						// screen position
						sp = x0 + y0 * maxX;
						// window position
						wp = xw + midX + (yw + midY) * window3;

						r = pixels[sp] & 0xff;
						g = pixels[sp + 1] & 0xff;
						b = pixels[sp + 2] & 0xff;

						// mind pixel format & convert RGB to YUV
						Utils.rgb2YUV(b, g, r, yuv, wp);

						// add to histogram
						histogram[yuv[wp]]++;
					}
				}

				// clip histogram to cut out noise
				boolean clipped;
				float delta, total, avg = 0;

				int count = 0;
				// calculate average
				for (int i = 0; i < 256; i++)
					if (histogram[i] > 0) {
						avg += histogram[i];
						count++;
					}

				avg /= count;
				// clip the brightest
				final float dmax = brightness * count + avg;

				int clippedCount = 0;
				// distribute surplus
				do {
					clipped = false;
					total = 0;

					for (int i = 0; i < 256; i++)
						// clip if is above dmax
						if (histogram[i] > dmax) {
							delta = histogram[i] - dmax;
							histogram[i] = dmax;

							// add to total clipping sum
							total += delta;
							clippedCount++;
							
							clipped = true;
						}

					if (clipped) {
						total /= count - clippedCount;
						// add to all none zero slots
						for (int i = 0; i < 256; i++) {
							final float p = histogram[i];
							if (p > 0 && p < dmax)
								histogram[i] += total;
						}
					}

				} while (clipped);

				// cdf - cumulative distributed function
				cdf[0] = histogram[0];
				for (int i = 1; i < 256; i++)
					cdf[i] = cdf[i - 1] + histogram[i];

				// window center pixel - luma
				final int luma = Math.round((cdf[yuv[center]] * 255f) / cdf[255]);

				// pixel screen position
				sp = x + y * maxX;
				Utils.yuv2RGB(luma, yuv[center + 1], yuv[center + 2], newPixels, sp);

				// swap R & B components
				final byte t = newPixels[sp];
				newPixels[sp] = newPixels[sp + 2];
				newPixels[sp + 2] = t;
			}

		// copy to screen
		System.arraycopy(newPixels, 0, pixels, 0, pixels.length);
	}

	// SWAHE for RGB format 
	protected final void SWAHERGB(final int window) {
		// histogram
		final byte newPixels[] = new byte[pixels.length];

		final int N = window * window;
		final int len = N * 3;

		// cdf & yuv
		final float cdf[] = new float[256];
		final int yuv[] = new int[len];

		final int window3 = 3 * window;
		final int midY = window / 2;
		
		final int midX = window3 / 2;
		final int center = midX + midY * window3;

		final int maxX = getWidth() * 3;
		final int maxY = getHeight();

		final int maxX1 = maxX - 3;
		final int maxY1 = maxY - 1;
		
		final float histogram[] = new float[256];
		final float brightness = config.swaheBrightness * 0.05f;
		
		int r, g, b;

		for (int y = 0; y < maxY; y++)
			for (int x = 0; x < maxX; x += 3) {
				// compute histogram for window
				Arrays.fill(histogram, 0);

				int sp, wp;
				// calculate window
				for (int yw = -midY; yw < midY; yw++) {
					int y0 = y + yw;
					if (y0 < 0) // upper corner
						y0 = -y0;
					else if (y0 > maxY1)
						y0 = maxY1 - midY;

					for (int xw = -midX; xw < midX; xw += 3) {
						int x0 = x + xw;
						if (x0 < 0) // left corner
							x0 = -x0;
						else if (x0 > maxX1)
							x0 = maxX1 - midX;

						// screen position
						sp = x0 + y0 * maxX;
						// window position
						wp = xw + midX + (yw + midY) * window3;

						r = pixels[sp] & 0xff;
						g = pixels[sp + 1] & 0xff;
						b = pixels[sp + 2] & 0xff;

						// mind pixel format & convert RGB to YUV
						Utils.rgb2YUV(r, g, b, yuv, wp);

						// add to histogram
						histogram[yuv[wp]]++;
					}
				}

				// clip histogram to cut out noise
				boolean clipped;
				float delta, total, avg = 0;

				int count = 0;
				// calculate average
				for (int i = 0; i < 256; i++)
					if (histogram[i] > 0) {
						avg += histogram[i];
						count++;
					}

				avg /= count;
				// clip the brightest
				final float dmax = brightness * count + avg;

				int clippedCount = 0;
				// distribute surplus
				do {
					clipped = false;
					total = 0;

					for (int i = 0; i < 256; i++)
						// clip if is above dmax
						if (histogram[i] > dmax) {
							delta = histogram[i] - dmax;
							histogram[i] = dmax;

							// add to total clipping sum
							total += delta;
							clippedCount++;
							
							clipped = true;
						}

					if (clipped) {
						total /= count - clippedCount;
						// add to all none zero slots
						for (int i = 0; i < 256; i++) {
							final float p = histogram[i];
							if (p > 0 && p < dmax)
								histogram[i] += total;
						}
					}

				} while (clipped);

				// cdf - cumulative distributed function
				cdf[0] = histogram[0];
				for (int i = 1; i < 256; i++)
					cdf[i] = cdf[i - 1] + histogram[i];

				// window center pixel - luma
				final int luma = Math.round((cdf[yuv[center]] * 255f) / cdf[255]);

				// pixel screen position
				sp = x + y * maxX;
				Utils.yuv2RGB(luma, yuv[center + 1], yuv[center + 2], newPixels, sp);
			}

		// copy to screen
		System.arraycopy(newPixels, 0, pixels, 0, pixels.length);
	}
	
	private class ImageWindowListener extends WindowAdapter {
		private AbstractRenderer renderer;

		public ImageWindowListener(final AbstractRenderer renderer) {
			this.renderer = renderer;
		}

		@Override
		public void windowOpened(final WindowEvent event) {
			this.renderer.windowVisible = true;
		}
	}

	private class PictureCanvas extends Canvas {
		private static final long serialVersionUID = -5252519951411648799L;

		@Override
		public void paint(final Graphics g) {
			showImage();
		}
	}
}
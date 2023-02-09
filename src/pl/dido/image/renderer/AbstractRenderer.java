package pl.dido.image.renderer;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

import javax.swing.JFrame;
import javax.swing.JMenuBar;

import pl.dido.image.Config;
import pl.dido.image.utils.Utils;

public abstract class AbstractRenderer extends Thread {

	protected int palette[][];
	protected byte pixels[];
	
	protected int[] work = null; 

	protected int width;
	protected int height;

	protected JFrame frame;

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

		frame.setSize(width * 2, height * 2);
		frame.setLocationRelativeTo(null);

		frame.setResizable(false);
		frame.setVisible(true);

		canvas = new PictureCanvas();

		canvas.setSize(width * 2, height * 2);
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
			// show image before
			showImage();

			canvas.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			setupPalette();

			imageDithering();
			imagePostproces();

			// show image after
			showImage();
		} finally {
			canvas.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		}
	}

	protected abstract void imagePostproces();
	protected abstract void setupPalette();
	
	protected abstract String getTitle();
	protected abstract JMenuBar getMenuBar();	
	
	protected abstract int getHeight();
	protected abstract int getWidth();

	protected void imageDithering() {
		final int work[] = Utils.copy2Int(pixels);

		int r0, g0, b0;
		int r_error = 0, g_error = 0, b_error = 0;	

		final int width3 = width * 3;

		for (int y = 0; y < height; y++) {
			final int k = y * width3;
			final int k1 = ((y + 1) * width3);
			final int k2 = ((y + 2) * width3);
			
			for (int x = 0; x < width3; x += 3) {

				final int pyx = k + x;
				final int py1x = k1 + x;
				final int py2x = k2 + x;

				r0 = work[pyx];
				g0 = work[pyx + 1];
				b0 = work[pyx + 2];

				final int color = getColorIndex(r0, g0, b0);
				final int pixel[] = palette[color];

				final int r = pixel[0];
				final int g = pixel[1];
				final int b = pixel[2];

				pixels[pyx] = (byte) r;
				pixels[pyx + 1] = (byte) g;
				pixels[pyx + 2] = (byte) b;

				r_error = Utils.saturate(r0 - r);
				g_error = Utils.saturate(g0 - g);
				b_error = Utils.saturate(b0 - b);
				
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
							work[py1x + 3] += r_error >> 4;
							work[py1x + 3 + 1] += g_error >> 4;
							work[py1x + 3 + 2] += b_error >> 4;
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
		final int len = palette.length;

		for (int i = len; i-- > 0;) { // euclidean distance
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

	protected float perceptedDistanceCM(final int r, final int g, final int b, final int pr, final int pg, final int pb) {
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
package pl.dido.image.renderer;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferStrategy;

import javax.swing.JFrame;
import javax.swing.JMenuBar;

import pl.dido.image.utils.Gfx;
import pl.dido.image.utils.Utils;

public abstract class AbstractRendererRunner implements Runnable {
	private AbstractRenderer renderer;

	protected int windowWidth;
	protected int windowHeight;

	protected JFrame frame;
	protected Canvas canvas;

	protected BufferStrategy bufferStrategy;
	protected boolean windowVisible = false;

	protected String fileName;

	public AbstractRendererRunner(final AbstractRenderer renderer, final String fileName) {
		this.renderer = renderer;
		this.fileName = fileName;
	}

	protected void initializeView() {
		windowWidth = renderer.config.getWindowWidth();
		windowHeight = renderer.config.getWindowHeight();
		
		frame = new JFrame(getTitle() + renderer.config.getConfigString());
		frame.setJMenuBar(getMenuBar());
		frame.setIconImage(Toolkit.getDefaultToolkit().getImage(Utils.getResourceAsURL("retro.png")));

		frame.addWindowListener(new ImageWindowListener(this));
		
		frame.setSize(windowWidth, windowHeight);
		frame.setLocationRelativeTo(null);

		frame.setResizable(false);
		frame.setVisible(true);

		canvas = new PictureCanvas();
		frame.setLocationRelativeTo(null);

		canvas.setSize(windowWidth, windowHeight);
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
		
		gfx.drawImage(Gfx.scaleImage(renderer.image, windowWidth, windowHeight, false), 0, 0, canvas.getWidth(), canvas.getHeight(), 0, 0, windowWidth, windowHeight, null);
		gfx.dispose();

		bufferStrategy.show();
	}

	public void run() {
		initializeView();

		try {
			// wait for window
			while (!windowVisible)
				Thread.sleep(100);

			canvas.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			renderer.imageProcess();
		
			// show image after
			showImage();
		} catch (final InterruptedException ie) {
			ie.printStackTrace();
		} finally {
			canvas.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		}
	}

	private class ImageWindowListener extends WindowAdapter {
		private AbstractRendererRunner runner;

		public ImageWindowListener(final AbstractRendererRunner runner) {
			this.runner = runner;
		}

		@Override
		public void windowOpened(final WindowEvent event) {
			this.runner.windowVisible = true;
		}
	}

	private class PictureCanvas extends Canvas {
		private static final long serialVersionUID = -5252519951411648799L;

		@Override
		public void paint(final Graphics g) {
			showImage();
		}
	}

	protected abstract String getTitle();
	protected abstract JMenuBar getMenuBar();
}
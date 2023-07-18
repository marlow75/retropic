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

import javax.swing.JFrame;
import javax.swing.JMenuBar;

import pl.dido.image.utils.Utils;

public abstract class AbstractRendererRunner implements Runnable {
	private AbstractRenderer renderer;
	
	protected int width;
	protected int height;
	
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
		width = renderer.image.getWidth();
		height = renderer.image.getHeight();

		frame = new JFrame(getTitle() + renderer.config.getConfigString());
		frame.setJMenuBar(getMenuBar());
		frame.setIconImage(Toolkit.getDefaultToolkit().getImage(Utils.getResourceAsURL("retro.png")));

		frame.addWindowListener(new ImageWindowListener(this));

		final int width = renderer.config.getScreenWidth();
		final int height = renderer.config.getScreenHeight();

		frame.setSize(width, height);
		frame.setLocationRelativeTo(null);

		frame.setResizable(false);
		frame.setVisible(true);

		canvas = new PictureCanvas();
		frame.setLocationRelativeTo(null);

		canvas.setSize(width, height);
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

		gfx.drawImage(renderer.image, 0, 0, canvas.getWidth(), canvas.getHeight(), 0, 0, width, height, null);
		gfx.dispose();

		bufferStrategy.show();
	}

	protected void addWaterMark() {
		final Graphics gfx = renderer.image.createGraphics();

		gfx.setColor(Color.WHITE);
		gfx.setFont(new Font("Tahoma", Font.BOLD, 8));
		gfx.drawString("RetroPIC", width - 40, height - 5);
		gfx.dispose();
	}

	public void run() {
		initializeView();
		
		// wait for window
		while (!windowVisible)
			try {
				Thread.sleep(100);
			} catch (final InterruptedException e) {
				e.printStackTrace();
			}

		try {
			canvas.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			
			renderer.imageProcess();
			addWaterMark();

			// show image after
			showImage();			
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

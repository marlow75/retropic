package pl.dido.image.utils;

import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

public class ImageCanvas extends Canvas {
	private static final long serialVersionUID = 7126069230112530254L;
	private BufferedImage img;

	public ImageCanvas(final String fileName) {
		try {
			img = ImageIO.read(Utils.getResourceAsStream(fileName));
		} catch (final IOException ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public Dimension getPreferredSize() {
		return img == null ? new Dimension(32, 32) : new Dimension(img.getWidth(), img.getHeight());
	}

	@Override
	public void paint(final Graphics g) {
		super.paint(g);

		if (img != null) {
			final int x = (getWidth() - img.getWidth()) / 2;
			final int y = (getHeight() - img.getHeight()) / 2;
			g.drawImage(img, x, y, this);
		}
	}
}
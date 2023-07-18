package pl.dido.image.renderer;

import java.awt.image.BufferedImage;

import pl.dido.image.utils.Config;

public abstract class AbstractPictureColorsRenderer extends AbstractRenderer {

	public int pictureColors[][];
		
	public AbstractPictureColorsRenderer(final BufferedImage image, final Config config) {
		super(image, config);
	}
}
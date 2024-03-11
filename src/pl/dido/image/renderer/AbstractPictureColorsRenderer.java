package pl.dido.image.renderer;

import java.awt.image.BufferedImage;

import pl.dido.image.utils.Config;

public abstract class AbstractPictureColorsRenderer extends AbstractRenderer {

	public int pictureColors[][];
	
	protected int[][] normalizePalette(final int p[][]) {
		for (int i = 0; i < p.length; i++) {
			final int c[] = p[i];
			final int ci = getColorIndex(c[0], c[1], c[2]);
			
			final int[] color = palette[ci];
			
			c[0] = color[0];
			c[1] = color[1];
			c[2] = color[2];
		}
		
		return p;
	}
		
	public AbstractPictureColorsRenderer(final BufferedImage image, final Config config) {
		super(image, config);
	}
}
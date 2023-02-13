package pl.dido.image.renderer;

import java.awt.image.BufferedImage;

import pl.dido.image.Config;
import pl.dido.image.utils.ColorCache;

public abstract class AbstractCachedRenderer extends AbstractRenderer {

	protected int pictureColors[][];
	protected ColorCache cache;
		
	public AbstractCachedRenderer(final BufferedImage image, final String fileName, final Config config) {
		super(image, fileName, config);
		cache = new ColorCache();
	}
			
	@Override
	protected int getColorIndex(final int r, final int g, final int b) {
		final int p = cache.get(r, g, b);
		return p != -1 ? p : cache.put(r, g, b, super.getColorIndex(r, g, b));
	}	
}
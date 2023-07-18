package pl.dido.image.renderer;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

import pl.dido.image.amiga.AmigaConfig;
import pl.dido.image.utils.Config;
import pl.dido.image.utils.Config.NEAREST_COLOR;
import pl.dido.image.utils.Gfx;

public abstract class AbstractRenderer {

	protected int palette[][];
	protected byte pixels[];

	protected int width;
	protected int height;

	protected int[] work = null;

	protected BufferedImage image;
	protected NEAREST_COLOR colorAlg;

	public Config config;
	public int colorModel;

	private void initialize(final Config config) {
		// copy of configuration
		try {
			this.config = (Config) config.clone();
		} catch (final CloneNotSupportedException ex) {
			this.config = config;
		}

		width = config.getWidth();
		height = config.getHeight();

		colorAlg = config.color_alg;
	}

	public AbstractRenderer(final Config config) {
		initialize(config);
	}

	public AbstractRenderer(final BufferedImage image, final Config config) {
		initialize(config);
		setImage(image);
	}

	public void setImage(final BufferedImage image) {
		this.image = scaleImage(image);

		pixels = ((DataBufferByte) this.image.getRaster().getDataBuffer()).getData();
		colorModel = image.getType();
	}

	public BufferedImage getImage() {
		return image;
	}

	protected BufferedImage scaleImage(final BufferedImage image) {
		if (image.getWidth() != width || image.getHeight() != height)
			if (config.keepAspect)
				return Gfx.scaleWithPreservedAspect(image, width, height);
			else
				return Gfx.scaleWithStretching(image, width, height);

		return image;
	}

	public void imageProcess() {
		final int width = config.getWidth();
		final int height = config.getHeight();

		// contrast correction
		switch (config.highContrast) {
		case HE:
			Gfx.HE(pixels, colorModel);
			break;
		case CLAHE:
			final int window = config instanceof AmigaConfig ? 16 : 8;
			Gfx.CLAHE(pixels, colorModel, window, config.details, width, height);
			break;
		case SWAHE:
			Gfx.SWAHE(pixels, colorModel, config.windowSize, config.details, width, height);
			break;
		default:
			break;
		}

		setupPalette();

		if (config.dithering)
			imageDithering();

		imagePostproces();
	}

	protected abstract void imagePostproces();

	protected abstract void setupPalette();

	protected void imageDithering() {
		Gfx.dithering(pixels, colorModel, palette, config);
	}

	protected int getColorIndex(final int r, final int g, final int b) {
		return Gfx.getColorIndex(colorAlg, colorModel, palette, r, g, b);
	}
}
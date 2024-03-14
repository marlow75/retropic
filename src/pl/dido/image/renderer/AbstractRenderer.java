package pl.dido.image.renderer;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.IOException;

import pl.dido.image.amiga.AmigaConfig;
import pl.dido.image.utils.Config;
import pl.dido.image.utils.Config.NEAREST_COLOR;
import pl.dido.image.utils.Gfx;

public abstract class AbstractRenderer {

	public int palette[][];
	public byte pixels[];

	// screen size 
	protected int screenWidth;
	protected int screenHeight;

	protected int[] work = null;
	protected AbstractRendererRunner runner; 

	protected BufferedImage image;
	protected NEAREST_COLOR colorAlg;

	public Config config;
	public int pixelType;

	private void initialize(final Config config) {
		this.config = config;
		
		screenWidth = config.getScreenWidth();
		screenHeight = config.getScreenHeight();

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
		this.image = Gfx.scaleImage(image, screenWidth, screenHeight, config.preserveAspect);
		
		pixels = ((DataBufferByte) this.image.getRaster().getDataBuffer()).getData();
		pixelType = image.getType();
	}

	public BufferedImage getImage() {
		return image;
	}

	public void imageProcess() {
		processContrast();
		setupPalette();
		
		if (config.dithering)
			imageDithering();
		
		imagePostproces();
		if (config.scanline)
			rasterizeImage();
	}
	
	private void processContrast() {
		// contrast correction
		switch (config.highContrast) {
		case HE:
			Gfx.HE(pixels, pixelType);
			break;
		case CLAHE:
			Gfx.CLAHE(pixels, pixelType, config instanceof AmigaConfig ? 16 : 8, config.details, screenWidth, screenHeight);
			break;
		case SWAHE:
			Gfx.SWAHE(pixels, pixelType, config.windowSize, config.details, screenWidth, screenHeight);
			break;
		default:
			break;
		}
	}
	
	private void rasterizeImage() {
		try {			
			this.image = Gfx.byteArrayToImage(Gfx.makeScanlines(pixels, pixelType, image.getWidth()), screenWidth, 2 * screenHeight, pixelType);
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	protected abstract void imagePostproces();

	protected abstract void setupPalette();

	protected void imageDithering() {
		Gfx.dithering(pixels, pixelType, palette, config);
	}

	protected int getColorIndex(final int r, final int g, final int b) {
		return Gfx.getColorIndex(colorAlg, pixelType, palette, r, g, b);
	}
}
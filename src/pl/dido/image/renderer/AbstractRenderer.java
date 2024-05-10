package pl.dido.image.renderer;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

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

	protected BufferedImage image;
	protected NEAREST_COLOR colorAlg;

	public Config config;
	protected AbstractRendererRunner runner;

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
	}

	public BufferedImage getImage() {
		return image;
	}

	public void imageProcess() {
		processContrast();
		setupPalette();
		
		if (config.dithering)
			imageDithering();
		
		//runner.showImage();
		
		imagePostproces();
		if (config.scanline)
			makeScanlines();
	}
	
	private void processContrast() {
		// contrast correction
		switch (config.highContrast) {
		case HE:
			Gfx.HE(pixels);
			break;
		case CLAHE:
			Gfx.CLAHE(pixels, config instanceof AmigaConfig ? 16 : 8, config.details, screenWidth, screenHeight);
			break;
		case SWAHE:
			Gfx.SWAHE(pixels, config.windowSize, config.details, screenWidth, screenHeight);
			break;
		default:
			break;
		}
	}
	
	protected void makeScanlines() {
		image = Gfx.byteArrayToBGRImage(Gfx.makeScanlines(pixels, image.getWidth()), screenWidth, 2 * screenHeight);
	}

	protected abstract void imagePostproces();

	protected abstract void setupPalette();

	protected void imageDithering() {
		Gfx.dithering(pixels, palette, config);
	}

	protected final int getColorIndex(final int r, final int g, final int b) {
		return Gfx.getColorIndex(colorAlg, palette, r, g, b);
	}
	
	public void savePreview(final String exportFileName) {
		try {
			final BufferedImage img;
			
			if (config.scanline)
				img = Gfx.scaleImage(image, screenWidth, screenHeight, config.preserveAspect);
			else
				img = image;
			
			ImageIO.write(img, "jpg", new File(exportFileName + ".jpg"));
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	public void setRunner(final AbstractRendererRunner runner) {
		this.runner = runner;
	}
}
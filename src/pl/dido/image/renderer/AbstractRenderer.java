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
import pl.dido.image.utils.PALcodec;

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
		this.image = Gfx.scaleImage(image, screenWidth, screenHeight, config.preserve_aspect);
		pixels = ((DataBufferByte) this.image.getRaster().getDataBuffer()).getData();
	}

	public BufferedImage getImage() {
		return image;
	}

	public void imageProcess() {
		if (config.low_pass_filter)
			Gfx.lowpassFilter(pixels);
		
		processContrast();
		setupPalette();

		imageDithering();
		imagePostproces();
		
		if (config.pal_view)
			emuPAL();
	}

	protected void processContrast() {
		// contrast correction
		switch (config.high_contrast) {
		case HE:
			Gfx.HE(pixels);
			break;
		case CLAHE:
			Gfx.CLAHE(pixels, config instanceof AmigaConfig ? 16 : 8, config.details, screenWidth, screenHeight);
			break;
		case SWAHE:
			Gfx.SWAHE(pixels, config.window_size, config.details, screenWidth, screenHeight);
			break;
		default:
			break;
		}
	}

	protected void emuPAL() {
		final BufferedImage crt = new BufferedImage(PALcodec.WIDTH, PALcodec.HEIGHT, BufferedImage.TYPE_3BYTE_BGR);
		final byte[] data = ((DataBufferByte) crt.getRaster().getDataBuffer()).getData();

		PALcodec.encodeYC(image.getWidth(), image.getHeight(), pixels, config.black_white);
		PALcodec.decodeYC(data, config.black_white);

		image = Gfx.byteArrayToBGRImage(data, PALcodec.WIDTH, PALcodec.HEIGHT);
	}

	protected abstract void imagePostproces();

	protected abstract void setupPalette();

	protected void imageDithering() {
		switch (config.dither_alg) {
		case BAYER2x2, BAYER4x4, BAYER8x8, BAYER16x16:
			bayerDithering();
		default:
			Gfx.dithering(pixels, palette, config);
		}
	}

	protected void bayerDithering() {
		switch (config.dither_alg) {
		case BAYER2x2:
			Gfx.bayer2x2(pixels, palette, colorAlg, screenWidth, screenHeight, config.error_threshold);
			break;
		case BAYER4x4:
			Gfx.bayer4x4(pixels, palette, colorAlg, screenWidth, screenHeight, config.error_threshold);
			break;
		case BAYER8x8:
			Gfx.bayer8x8(pixels, palette, colorAlg, screenWidth, screenHeight, config.error_threshold);
			break;
		case BAYER16x16:
			Gfx.bayer16x16(pixels, palette, colorAlg, screenWidth, screenHeight, config.error_threshold);
			break;
		default:
			break;
		}
	}

	protected final int getColorIndex(final int r, final int g, final int b) {
		return Gfx.getColorIndex(colorAlg, palette, r, g, b);
	}

	public void savePreview(final String exportFileName) {
		try {
			final BufferedImage img = config.pal_view
					? Gfx.scaleImage(image, screenWidth, screenHeight, config.preserve_aspect)
					: image;

			ImageIO.write(img, "jpg", new File(exportFileName + ".jpg"));
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	public void setRunner(final AbstractRendererRunner runner) {
		this.runner = runner;
	}
}
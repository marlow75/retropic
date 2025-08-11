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
import pl.dido.image.utils.SimplePALcodec;

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

	protected abstract int getColorBitDepth();

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
		switch (config.filter) {
		case NONE:
			break;
		case EDGES_BLEND:
			final byte buffer[] = new byte[pixels.length];
			System.arraycopy(pixels, 0, buffer, 0, pixels.length);
			
			Gfx.mono(buffer);
			Gfx.edge(buffer, screenWidth, screenHeight);
			
			Gfx.blend(pixels, buffer); // pixels as magnitude
			break;
		case EMBOSS:
			Gfx.emboss(pixels, screenWidth, screenHeight);
			break;
		case LOWPASS:
			Gfx.lowpassFilter(pixels, config.lowpass_gain);
			break;
		case SHARPEN:
			Gfx.sharpen(pixels, screenWidth, screenHeight);
			break;
		default:
			break;
		}

		processContrast();
		setupPalette();

		imageDithering();
//		runner.showImage();

		imagePostproces();
		generatePALView();
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

	protected void generatePALView() {
		if (config.pal_view) {
			final BufferedImage crt = new BufferedImage(SimplePALcodec.WIDTH, SimplePALcodec.HEIGHT, BufferedImage.TYPE_3BYTE_BGR);
			final byte[] data = ((DataBufferByte) crt.getRaster().getDataBuffer()).getData();

			SimplePALcodec.encodeYC(image.getWidth(), image.getHeight(), pixels, config.black_white);
			SimplePALcodec.decodeYC(data, config.black_white);

			image = Gfx.byteArrayToBGRImage(data, SimplePALcodec.WIDTH, SimplePALcodec.HEIGHT);
		}
	}

	protected abstract void imagePostproces();
	protected abstract void setupPalette();

	protected void imageDithering() {
		switch (config.dither_alg) {
		case BAYER2x2, BAYER4x4, BAYER8x8, BAYER16x16, BLUE8x8, BLUE16x16:
			Gfx.bayerDithering(pixels, palette, getColorBitDepth(), config);
			break;
		case NOISE:
			Gfx.downsampling(pixels, getColorBitDepth(), config.error_threshold);
			break;
		default:
			Gfx.errorDiffuseDithering(pixels, palette, config);
		}
	}

	protected int getUsedColors() {
		return palette.length;
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
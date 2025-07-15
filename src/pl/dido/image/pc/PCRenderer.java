package pl.dido.image.pc;

import java.awt.image.BufferedImage;
import java.io.IOException;

import at.fhtw.ai.nn.utils.Autoencoder;
import at.fhtw.ai.nn.utils.HL1SoftmaxNetwork;
import at.fhtw.ai.nn.utils.Network;
import pl.dido.image.renderer.AbstractRenderer;
import pl.dido.image.utils.Config;
import pl.dido.image.utils.Gfx;
import pl.dido.image.utils.Utils;

public class PCRenderer extends AbstractRenderer {
	// CGA palette
	private final static int cgaColors[] = new int[] { 0x000000, 0x0000AA, 0x00AA00, 0x00AAAA, 0xAA0000, 0xAA00AA,
			0xAA5500, 0xAAAAAA, 0x555555, 0x5555FF, 0x55FF55, 0x55FFFF, 0xFF5555, 0xFF55FF, 0xFFFF55, 0xFFFFFF };

	private final static int vgaColors[] = new int[] { 0x000000, 0x0000aa, 0x00aa00, 0x00aaaa, 0xaa0000, 0xaa00aa,
			0xaa5500, 0xaaaaaa, 0x555555, 0x5555ff, 0x55ff55, 0x55ffff, 0xff5555, 0xff55ff, 0xffff55, 0xffffff };

	private final static int power2[] = new int[] { 128, 64, 32, 16, 8, 4, 2, 1 };

	protected final static String PCASCII_NETWORK_L1 = "cga.L1network";
	protected final static String PCASCII_ENCODER = "cga.autoencoder";
	
	protected final static String PC_CHARSET = "cga.bin";
	protected int screen[], color[];

	protected Network neural, encoder; // matches pattern with petscii
	protected byte charset[]; // charset 8x8 pixels per char

	protected int background[][] = new int[8][3];

	protected void initialize() {
		palette = new int[16][3];
		
		try {
			neural = new HL1SoftmaxNetwork(64, 16, 256);
			
			charset = Utils.loadCharset(Utils.getResourceAsStream(PC_CHARSET));
			neural.load(Utils.getResourceAsStream(PCASCII_NETWORK_L1));
			
			if (config.denoise) {
				encoder = new Autoencoder(64, 32, 64);
				encoder.load(Utils.getResourceAsStream(PCASCII_ENCODER));	
			} 
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}

		switch (((PCConfig)config).video_mode) {
		case CGA_TEXT:
			screen = new int[2000];
			color = new int[2000];

			break;
		case VESA_TEXT:
			screen = new int[6600];
			color = new int[6600];

			break;
		}
	}

	public int[] getScreen() {
		return screen;
	}

	public int[] getColor() {
		return color;
	}

	public byte[] getCharset() {
		return charset;
	}

	public PCRenderer(final Config config) {
		super(config);
		initialize();
	}

	public PCRenderer(final BufferedImage image, final Config config) {
		super(image, config);
		initialize();
	}

	@Override
	protected void setupPalette() {
		switch (((PCConfig) config).video_mode) {
		case CGA_TEXT:
			for (int i = 0; i < cgaColors.length; i++) {
				palette[i][0] = (cgaColors[i] & 0x0000ff); // blue
				palette[i][1] = (cgaColors[i] & 0x00ff00) >> 8; // green
				palette[i][2] = (cgaColors[i] & 0xff0000) >> 16; // red
			}

			for (int i = 0; i < cgaColors.length / 2; i++) {
				background[i][0] = (cgaColors[i] & 0x0000ff); // blue
				background[i][1] = (cgaColors[i] & 0x00ff00) >> 8; // green
				background[i][2] = (cgaColors[i] & 0xff0000) >> 16; // red
			}

			break;
		case VESA_TEXT:
			for (int i = 0; i < vgaColors.length; i++) {
				palette[i][0] = (vgaColors[i] & 0x0000ff); // blue
				palette[i][1] = (vgaColors[i] & 0x00ff00) >> 8; // green
				palette[i][2] = (vgaColors[i] & 0xff0000) >> 16; // red
			}

			for (int i = 0; i < vgaColors.length / 2; i++) {
				background[i][0] = (vgaColors[i] & 0x0000ff); // blue
				background[i][1] = (vgaColors[i] & 0x00ff00) >> 8; // green
				background[i][2] = (vgaColors[i] & 0xff0000) >> 16; // red
			}

			break;
		}
	}

	@Override
	protected void imagePostproces() {
		ascii();
	}

	protected void ascii() {
		// tiles screen and pattern
		final int work[] = new int[64 * 3];
		final float tile[] = new float[64];

		final int width = config.getScreenWidth();
		final int height = config.getScreenHeight();
		
		final int txtWidth = width / 8;

		for (int y = 0; y < height; y += 8) {
			final int p = y * width * 3;

			for (int x = 0; x < width; x += 8) {
				final int offset = p + x * 3;

				int index = 0, f = 0, n = 0;
				float mf = 0, mn = Float.MAX_VALUE;

				// pickup brightest color in 8x8 tile
				for (int y0 = 0; y0 < 8; y0++) {
					for (int x0 = 0; x0 < 24; x0 += 3) {
						final int position = offset + y0 * width * 3 + x0;

						final int r = pixels[position] & 0xff;
						final int g = pixels[position + 1] & 0xff;
						final int b = pixels[position + 2] & 0xff;

						work[index++] = r;
						work[index++] = g;
						work[index++] = b;

						final float luma = Gfx.getLuma(r, g, b);
						if (luma > mf) {
							mf = luma;
							f = Gfx.getColorIndex(colorAlg, palette, r, g, b);
						}
						
						if (luma < mn) {
							mn = luma;
							n = Gfx.getColorIndex(colorAlg, background, r, g, b);
						}
					}
				}

				// foreground color
				final int cf[] = palette[f];

				final int fr = cf[0];
				final int fg = cf[1];
				final int fb = cf[2];

				// foreground color
				final int cn[] = background[n];

				final int nr = cn[0];
				final int ng = cn[1];
				final int nb = cn[2];

				for (int y0 = 0; y0 < 8; y0++)
					for (int x0 = 0; x0 < 8; x0++) {
						final int pyx0 = y0 * 24 + x0 * 3;

						final int r = work[pyx0];
						final int g = work[pyx0 + 1];
						final int b = work[pyx0 + 2];

						// fore or background color?
						final float df = Gfx.getDistance(colorAlg, r, g, b, fr, fg, fb);
						final float db = Gfx.getDistance(colorAlg, r, g, b, nr, ng, nb);

						// ones as color of the bright pixels
						tile[(y0 << 3) + x0] = (df <= db) ? 1 : 0;
					}

				// pattern match character
				if (config.denoise) {
					encoder.forward(tile);
					neural.forward(encoder.getResult());
				} else
					neural.forward(tile);

				final float[] result = neural.getResult();
				
				int code = 0;
				float value = result[0];
				
				// get code of character in charset
				for (int i = 0; i < 256; i++) {
					final float d = result[i];
					if (d > value) {
						code = i;
						value = d;
					}
				}

				// colors
				final int address = (y >> 3) * txtWidth + (x >> 3);
				
				color[address] = f | (n << 4);
				screen[address] = code;

				// draw character
				for (int y0 = 0; y0 < 8; y0++) {
					final int charset_pos = code * 8 + y0;
					final int charByte = charset[charset_pos];

					for (int x0 = 0; x0 < 8; x0++) {
						final int bitValue = power2[x0];
						final int screen_pos = offset + y0 * width * 3 + x0 * 3;

						if ((charByte & bitValue) == bitValue) {
							pixels[screen_pos] = (byte) fr;
							pixels[screen_pos + 1] = (byte) fg;
							pixels[screen_pos + 2] = (byte) fb;
						} else {
							pixels[screen_pos] = (byte) nr;
							pixels[screen_pos + 1] = (byte) ng;
							pixels[screen_pos + 2] = (byte) nb;
						}
					}
				}
			}
		}
	}

	@Override
	protected int getGraphicModeColorsNumber(final Config config) {
		return 3;
	}
}
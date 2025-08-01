package pl.dido.image.petscii;

import java.awt.image.BufferedImage;
import java.io.IOException;

import at.fhtw.ai.nn.utils.HL1SoftmaxNetwork;
import at.fhtw.ai.nn.utils.Network;
import pl.dido.image.utils.C64PaletteCalculator;
import pl.dido.image.utils.Config;
import pl.dido.image.utils.Gfx;
import pl.dido.image.utils.Utils;

public class PetsciiRenderer2 extends PetsciiRenderer {

	protected Network neural2; // matches pattern with petscii
	protected byte charset2[]; // charset 8x8 pixels per char

	public PetsciiRenderer2(final Config config) {
		super(config);
		initialize();
	}

	public PetsciiRenderer2(final BufferedImage image, final Config config) {
		super(image, config);
		initialize();
	}
	
	protected void initialize() {
		super.initialize();

		try {
			neural2 = new HL1SoftmaxNetwork(64, 16, 256);
			charset2 = Utils.loadCharset(Utils.getResourceAsStream(PETSCII_CHARSET));

			neural2.load(Utils.getResourceAsStream(PETSCII_NETWORK_L1));
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected void draw() {
		// tiles screen and pattern
		final int work[] = new int[64 * 3];
		final float tile[] = new float[64];

		int nr = 0, ng = 0, nb = 0, count = 0;
		final int occurrence[] = new int[16];

		byte charset[];

		for (int i = 0; i < pixels.length; i += 3) {
			nr = pixels[i] & 0xff;
			ng = pixels[i + 1] & 0xff;
			nb = pixels[i + 2] & 0xff;

			// dimmer better
			occurrence[Gfx.getColorIndex(colorAlg, palette, nr, ng, nb)] += (255 - Gfx.getLuma(nr, ng, nb));
		}

		// get background color with maximum occurrence
		int k = 0;
		for (int i = 0; i < 16; i++) {
			final int o = occurrence[i];
			if (count < o) {
				count = o;
				k = i;
			}
		}

		// most occurrence color as background
		setBackgroundColor(k);

		nr = palette[k][0];
		ng = palette[k][1];
		nb = palette[k][2];

		final float back_luma = Gfx.getLuma(nr, ng, nb);

		for (int y = 0; y < 200; y += 8) {
			final int p = y * 320 * 3;

			for (int x = 0; x < 320; x += 8) {
				final int offset = p + x * 3;

				int index = 0, f = 0;
				float max_distance = 0;

				// pickup brightest color in 8x8 tile
				for (int y0 = 0; y0 < 8; y0++) {
					for (int x0 = 0; x0 < 24; x0 += 3) {
						final int position = offset + y0 * 320 * 3 + x0;

						final int r = pixels[position] & 0xff;
						final int g = pixels[position + 1] & 0xff;
						final int b = pixels[position + 2] & 0xff;

						work[index++] = r;
						work[index++] = g;
						work[index++] = b;

						final float distance = Gfx.getLuma(r, g, b) - back_luma;
						if (max_distance < distance) {
							max_distance = distance;
							f = Gfx.getColorIndex(colorAlg, palette, r, g, b);
						}
					}
				}

				// foreground color
				final int cf[] = palette[f];
				final int fr = cf[0];
				final int fg = cf[1];
				final int fb = cf[2];

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

				final float[] result;
				if (y < 100) {
					neural.forward(tile);
					result = neural.getResult();

					charset = this.charset;
				} else {
					neural2.forward(tile);
					result = neural2.getResult();

					charset = charset2;
				}

				int code = 160;
				float value = result[160];

				// get code of character in charset
				for (int i = 0; i < 256; i++) {
					final float d = result[i];
					if (d > value) {
						code = i;
						value = d;
					}
				}

				// colors
				final int address = (y >> 3) * 40 + (x >> 3);
				nibble[address] = f;
				screen[address] = code;

				// draw character
				for (int y0 = 0; y0 < 8; y0++) {
					final int charset_pos = code * 8 + y0;
					final int charByte = charset[charset_pos];

					for (int x0 = 0; x0 < 8; x0++) {
						final int bitValue = power2[x0];
						final int screen_pos = offset + y0 * 320 * 3 + x0 * 3;

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

	public int getBackgroundColor() {
		return backgroundColor;
	}

	public int[] getScreen() {
		return screen;
	}

	public int[] getNibble() {
		return nibble;
	}

	public byte[] getCharset1() {
		return charset;
	}

	public byte[] getCharset2() {
		return charset2;
	}

	public void setBackgroundColor(final int backgroundColor) {
		this.backgroundColor = backgroundColor;
	}

	@Override
	protected void setupPalette() {
		palette = C64PaletteCalculator.getCalculatedPalette();
	}

	@Override
	protected void imagePostproces() {
		draw();
	}

	@Override
	protected int getColorBitDepth() {
		return 3;
	}
}
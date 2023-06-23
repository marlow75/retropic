package pl.dido.image.petscii;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import pl.dido.image.Config;
import pl.dido.image.renderer.AbstractRenderer;
import pl.dido.image.utils.Utils;
import pl.dido.image.utils.neural.*;

public class PetsciiRenderer extends AbstractRenderer {

	// C64 palette
	private final static int colors[] = new int[] { 0, 0xFFFFFF, 0x68372B, 0x70A4B2, 0x6F3D86, 0x588D43, 0x352879,
			0xB8C76F, 0x6F4F25, 0x433900, 0x9A6759, 0x444444, 0x6C6C6C, 0x9AD284, 0x6C5EB5, 0x959595 };

	private final static int power2[] = new int[] { 128, 64, 32, 16, 8, 4, 2, 1 };

	private final static String PETSCII_NETWORK_L1 = "petscii.L1network";
	private final static String PETSCII_NETWORK_L2 = "petscii.L2network";
	
	private final static String PETSCII_CHARSET = "petscii.bin";

	protected int bitmap[] = new int[40 * 200];
	protected int screen[] = new int[1000];

	protected int nibble[] = new int[1000];
	protected int backgroundColor = 0;

	public PetsciiRenderer(final BufferedImage image, final String fileName, final PetsciiConfig config) {
		super(image, fileName, config);

		palette = new int[16][3];
	}
	
	@Override
	protected void setupPalette() {
		switch (image.getType()) {
		case BufferedImage.TYPE_3BYTE_BGR:
			for (int i = 0; i < colors.length; i++) {
				palette[i][0] = colors[i] & 0x0000ff; // blue
				palette[i][1] = (colors[i] & 0x00ff00) >> 8; // green
				palette[i][2] = (colors[i] & 0xff0000) >> 16; // red
			}
			break;
		case BufferedImage.TYPE_INT_RGB:
			for (int i = 0; i < colors.length; i++) {
				palette[i][0] = (colors[i] & 0xff0000) >> 16; // red
				palette[i][1] = (colors[i] & 0x00ff00) >> 8; // green
				palette[i][2] = colors[i] & 0x0000ff; // blue
			}
			break;
		default:
			throw new RuntimeException("Unsupported Pixel format !!!");
		}
	}

	@Override
	protected void imagePostproces() {
		petscii();
	}

	private void petsciiExportPRG(final String fileName) {
		try {
			final BufferedInputStream in = new BufferedInputStream(Utils.getResourceAsStream("petscii.prg"), 8192);
			final BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(new File(fileName)), 8192);

			// loading address BASIC
			out.write(0x01);
			out.write(0x08);

			int data;
			in.read(); // skip loading address
			in.read();

			while ((data = in.read()) != -1)
				out.write(data);

			in.close();

			// first background color
			out.write(backgroundColor & 0xf);

			// bitmap
			for (int i = 0; i < 1000; i++)
				out.write(screen[i] & 0xff);

			// color nibbles
			for (int i = 0; i < 1000; i++)
				out.write(nibble[i] & 0xf);

			out.close();

			frame.setTitle(frame.getTitle() + " SAVED");
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	protected void petscii() {
		// matches pattern with petscii
		final Network neural;
		
		// charset 8x8 pixels per char
		final byte charset[];
		final String networkFile;
		
		switch (((PetsciiConfig) config).network) {
		case L2:
			neural = new HL2Network(64, 128, 256);
			networkFile = PETSCII_NETWORK_L2;
			
			break;
		default:
			neural = new HL1Network(64, 128, 256);
			networkFile = PETSCII_NETWORK_L1;
			
			break;
		}

		try {
			charset = Utils.loadCharset(Utils.getResourceAsStream(PETSCII_CHARSET));
			neural.load(Utils.getResourceAsStream(networkFile));
		} catch (final IOException e) {
			// mass hysteria
			throw new RuntimeException(e);
		}

		// tiles screen and pattern
		final int work[] = new int[64 * 3];
		final float tile[] = new float[64];

		// calculate average
		int nr = 0, ng = 0, nb = 0, count = 0;
		final int occurrence[] = new int[16];

		for (int i = 0; i < pixels.length; i += 3) {
			nr = pixels[i] & 0xff;
			ng = pixels[i + 1] & 0xff;
			nb = pixels[i + 2] & 0xff;

			// dimmer better
			occurrence[getColorIndex(palette, nr, ng, nb)] += (255 - getLumaByCM(nr, ng, nb));
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
		backgroundColor = k;

		nr = palette[k][0];
		ng = palette[k][1];
		nb = palette[k][2];

		final float backLuma = getLumaByCM(nr, ng, nb);

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

						final float distance = Math.abs(getLumaByCM(r, g, b) - backLuma);
						if (max_distance < distance) {
							max_distance = distance;
							f = getColorIndex(r, g, b);
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
						final float df = getDistanceByCM(r, g, b, fr, fg, fb);
						final float db = getDistanceByCM(r, g, b, nr, ng, nb);

						// ones as color of the bright pixels
						tile[(y0 << 3) + x0] = (df <= db) ? 1 : 0;
					}

				// pattern match character
				neural.forward(new Dataset(tile));
				final float[] result = neural.getResult();

				int code = 0;
				float value = result[0];

				// get code of character in charset
				for (int i = 1; i < 256; i++)
					if (result[i] > value) {
						code = i;
						value = result[i];
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

	@Override
	protected JMenuBar getMenuBar() {
		final JMenu menuFile = new JMenu("File");
		menuFile.setMnemonic(KeyEvent.VK_F);

		final JMenuItem miExecutable = new JMenuItem("Export as executable... ");
		miExecutable.setMnemonic(KeyEvent.VK_E);
		miExecutable.setAccelerator(
				KeyStroke.getKeyStroke(KeyEvent.VK_E, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
		miExecutable.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				try {
					final String exportFileName = Utils.createDirectory(Config.export_path) + "/" + fileName + ".prg";
					final int result = JOptionPane.showConfirmDialog(null, "Export " + exportFileName + "?", "Confirm",
							JOptionPane.YES_NO_OPTION);
					
					if (result == 0)
						petsciiExportPRG(exportFileName);
				} catch (final IOException ex) {
					JOptionPane.showMessageDialog(null, "Error", ex.getMessage(), JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		menuFile.add(miExecutable);

		final JMenuBar menuBar = new JMenuBar();
		menuBar.add(menuFile);

		return menuBar;
	}

	@Override
	protected String getTitle() {
		return "PETSCII ";
	}

	@Override
	protected int getHeight() {
		return 200;
	}

	@Override
	protected int getWidth() {
		return 320;
	}

	@Override
	protected int getScreenHeight() {
		return 400;
	}

	@Override
	protected int getScreenWidth() {
		return 640;
	}
}
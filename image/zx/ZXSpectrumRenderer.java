package pl.dido.image.zx;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
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
import pl.dido.image.renderer.AbstractOldiesRenderer;
import pl.dido.image.utils.Utils;

public class ZXSpectrumRenderer extends AbstractOldiesRenderer {

	// ZX spectrum palette
	private final static int colors[] = new int[] { 0x000000, 0x000000, 0x0000d8, 0x0000ff, 0xd80000, 0xff0000,
			0xd800d8, 0xff00ff, 0x00d800, 0x00ff00, 0x00d8d8, 0x00ffff, 0xd8d800, 0xffff00, 0xd8d8d8, 0xffffff };

	protected int attribs[] = new int[768];
	protected int bitmap[] = new int[32 * 192];

	protected int zx_line = 0;
	protected int zx_position = 0;

	public ZXSpectrumRenderer(final BufferedImage image, final String fileName, final ZXConfig config) {
		super(image, fileName, config);
		palette = new int[16][3];
	}

	@Override
	protected void setupPalette() {
		switch (image.getType()) {
		case BufferedImage.TYPE_3BYTE_BGR:
			for (int i = 0; i < colors.length; i++) {
				palette[i][0] = (colors[i] & 0x0000ff); // blue
				palette[i][1] = (colors[i] & 0x00ff00) >> 8; // green
				palette[i][2] = (colors[i] & 0xff0000) >> 16; // red
			}
			break;
		case BufferedImage.TYPE_INT_RGB:
			for (int i = 0; i < colors.length; i++) {
				palette[i][0] = (colors[i] & 0xff0000) >> 16; // red
				palette[i][1] = (colors[i] & 0x00ff00) >> 8; // green
				palette[i][2] = (colors[i] & 0x0000ff); // blue
			}
			break;
		default:
			throw new RuntimeException("Unsupported Pixel format !!!");
		}
	}

	@Override
	protected void imageDithering() {
		if (config.dithering)
			super.imageDithering();
	}

	@Override
	protected void imagePostproces() {
		hiresLumaDithered();
	}

	private void hiresExport(final String fileName) {
		try {
			final BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(new File(fileName)), 8192);

			// bitmap
			for (int i = 0; i < bitmap.length; i++)
				out.write(bitmap[i] & 0xff);

			// attributes
			for (int i = 0; i < attribs.length; i++)
				out.write(attribs[i] & 0xff);

			out.close();
			frame.setTitle(frame.getTitle() + " SAVED");
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	protected void hiresLumaDithered() {
		final int work[] = new int[width * height * 3];
		int bitmapIndex = 0;

		for (int y = 0; y < 192; y += 8) { // every 8 line
			final int p = y * 256 * 3;

			for (int x = 0; x < 256; x += 8) { // every 8 pixel
				final int offset = p + x * 3;

				float min = 255;
				float max = 0;

				int f = 0, n = 0;

				// 8x8 tile
				for (int y0 = 0; y0 < 8; y0 += 1) {
					for (int x0 = 0; x0 < 24; x0 += 3) {
						final int position = offset + y0 * 256 * 3 + x0;

						final int r = pixels[position] & 0xff;
						final int g = pixels[position + 1] & 0xff;
						final int b = pixels[position + 2] & 0xff;

						work[position] = r;
						work[position + 1] = g;
						work[position + 2] = b;

						final float luma = getLumaByCM(r, g, b);

						if (luma > max) {
							max = luma;
							f = getColorIndex(r, g, b);
						} else
						if (luma < min) {
							min = luma;
							n = getColorIndex(r, g, b);
						}
					}
				}
				final int address = (y >> 3) * 32 + (x >> 3);

				int ink = f >> 1;
				int paper = n >> 1;
				int bright = (f % 1 | n % 1) << 6;

				attribs[address] = ((paper & 0xf) << 3) | (ink & 0x7) | bright;

				int value = 0, bitcount = 0;
				int r_error, g_error, b_error;

				for (int y0 = 0; y0 < 8; y0++)
					for (int x0 = 0; x0 < 8 * 3; x0 += 3) {
						final int pyx0 = offset + y0 * 256 * 3 + x0;
						final int py1x0 = offset + (y0 + 1) * 256 * 3 + x0;

						final int r = Utils.saturate((int) work[pyx0]);
						final int g = Utils.saturate((int) work[pyx0 + 1]);
						final int b = Utils.saturate((int) work[pyx0 + 2]);

						final int fr = palette[f][0];
						final int fg = palette[f][1];
						final int fb = palette[f][2];

						int nr = palette[n][0];
						int ng = palette[n][1];
						int nb = palette[n][2];

						final float d1 = getDistanceByCM(r, g, b, fr, fg, fb);
						final float d2 = getDistanceByCM(r, g, b, nr, ng, nb);

						if (d1 < d2) {
							nr = fr;
							ng = fg;
							nb = fb;

							value = (value << 1) | 1;
						} else
							value = value << 1;

						if (bitcount % 8 == 7) {
							bitmap[translate(bitmapIndex++)] = value;
							value = 0;
						}

						bitcount += 1;

						pixels[pyx0] = (byte) nr;
						pixels[pyx0 + 1] = (byte) ng;
						pixels[pyx0 + 2] = (byte) nb;

						r_error = r - nr;
						g_error = g - ng;
						b_error = b - nb;

						if (x < 248) {
							work[pyx0 + 3]     += r_error * 7 / 16;
							work[pyx0 + 3 + 1] += g_error * 7 / 16;
							work[pyx0 + 3 + 2] += b_error * 7 / 16;
						}
						if (y < 184) {
							work[py1x0 - 3]     += r_error * 3 / 16;
							work[py1x0 - 3 + 1] += g_error * 3 / 16;
							work[py1x0 - 3 + 2] += b_error * 3 / 16;

							work[py1x0] 	+= r_error * 5 / 16;
							work[py1x0 + 1] += g_error * 5 / 16;
							work[py1x0 + 2] += b_error * 5 / 16;


							if (x < 248) {
								work[py1x0 + 3] 	+= r_error / 16;
								work[py1x0 + 3 + 1] += g_error / 16;
								work[py1x0 + 3 + 2] += b_error / 16;
							}
						}
					}
			}
		}
	}

	private int translate(final int address) {
		final int base = (address & 0b1100000000000) >>> 11;
		final int zx_address = base * 2048 + zx_line * 256 + zx_position;

		if (++zx_line % 8 == 0) {
			zx_line = 0;

			if (++zx_position % 256 == 0)
				zx_position = 0;
		}

		return zx_address;
	}

	@Override
	protected JMenuBar getMenuBar() {
		final JMenu menuFile = new JMenu("File");
		menuFile.setMnemonic(KeyEvent.VK_F);

		final JMenuItem miArtStudio = new JMenuItem("Export as picture... ");
		miArtStudio.setMnemonic(KeyEvent.VK_S);
		miArtStudio.setAccelerator(
				KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));

		miArtStudio.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				try {
					final String exportFileName = Utils.createDirectory(Config.export_path) + "/" + fileName + ".scr";

					final int result = JOptionPane.showConfirmDialog(null, "Export " + exportFileName + "?", "Confirm",
							JOptionPane.YES_NO_OPTION);
					if (result == 0)
						hiresExport(exportFileName);
				} catch (final IOException ex) {
					JOptionPane.showMessageDialog(null, "Error", ex.getMessage(), JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		menuFile.add(miArtStudio);

		final JMenuBar menuBar = new JMenuBar();
		menuBar.add(menuFile);

		return menuBar;
	}

	@Override
	protected String getTitle() {
		return "ZX 256x192x2 ";
	}

	@Override
	protected int getWidth() {
		return 256;
	}

	@Override
	protected int getHeight() {
		return 192;
	}
}
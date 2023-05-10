package pl.dido.image.atari;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import pl.dido.image.Config;
import pl.dido.image.renderer.AbstractCachedRenderer;
import pl.dido.image.utils.SOMFixedPalette;
import pl.dido.image.utils.Utils;

public class STRenderer extends AbstractCachedRenderer {

	protected int bitplanes[] = new int[4 * 20 * 200];

	public STRenderer(final BufferedImage image, final String fileName, final Config config) {
		super(image, fileName, config);
		palette = new int[512][3];
	}

	@Override
	protected void imageDithering() {
		if (config.dithering)
			super.imageDithering();
	}

	@Override
	protected void setupPalette() {
		int i = 0;
		final float k = 255 / 7f;

		for (int r = 0; r < 8; r++) {
			final int rk = (int) (r * k);

			for (int g = 0; g < 8; g++) {
				final int gk = (int) (g * k);

				for (int b = 0; b < 8; b++) {
					final int c[] = palette[i];

					c[0] = rk;
					c[1] = gk;
					c[2] = (int) (b * k);

					i++;
				}
			}
		}
	}

	@Override
	protected void imagePostproces() {
		final SOMFixedPalette training = new SOMFixedPalette(4, 4, 3); // 4x4 = 16 colors
		pictureColors = training.train(pixels);

		if (((STConfig) config).replace_colors) {
			int im = 0, in = 0, max = 0, min = 0;

			int color[];
			for (int i = 0; i < pictureColors.length; i++) {
				color = pictureColors[i];

				final int luma = color[0] + color[1] + color[2];
				if (luma > max) {
					max = luma;
					im = i;
				}

				if (luma < min) {
					min = luma;
					in = i;
				}
			}

			color = pictureColors[im];
			color[0] = 255;
			color[1] = 255;
			color[2] = 255;

			color = pictureColors[in];
			color[0] = 0;
			color[1] = 0;
			color[2] = 0;
		}

		doPicturePaletteDithering();
	}

	protected void doPicturePaletteDithering() {
		final float[] work = Utils.copy2float(pixels);

		int r0, g0, b0;

		final int width3 = width * 3;
		int index = 0, shift = 15;

		for (int y = 0; y < height; y++) {
			final int k1 = (y + 1) * width3;
			final int k2 = (y + 2) * width3;

			for (int x = 0; x < width3; x += 3) {
				final int pyx = y * width3 + x;
				final int py1x = k1 + x;
				final int py2x = k2 + x;

				r0 = Utils.saturate((int) work[pyx]);
				g0 = Utils.saturate((int) work[pyx + 1]);
				b0 = Utils.saturate((int) work[pyx + 2]);

				final int color = getColorIndex(pictureColors, r0, g0, b0);
				final int c[] = pictureColors[color];

				final int r = c[0];
				final int g = c[1];
				final int b = c[2];

				work[pyx] = r;
				work[pyx + 1] = g;
				work[pyx + 2] = b;

				pixels[pyx] = (byte) r;
				pixels[pyx + 1] = (byte) g;
				pixels[pyx + 2] = (byte) b;

				final int value = color & 0xf;

				bitplanes[index + 3] |= ((value & 8) >> 3) << shift;
				bitplanes[index + 2] |= ((value & 4) >> 2) << shift;
				bitplanes[index + 1] |= ((value & 2) >> 1) << shift;
				bitplanes[index] |= (value & 1) << shift;

				if (shift == 0) {
					shift = 15;
					index += 4;
				} else
					shift--;

				if (config.dithering) {
					final float r_error = r0 - r;
					final float g_error = g0 - g;
					final float b_error = b0 - b;

					switch (config.dither_alg) {
					case STD_FS:
						if (x < (width - 1) * 3) {
							work[pyx + 3] += r_error * 7 / 16;
							work[pyx + 3 + 1] += g_error * 7 / 16;
							work[pyx + 3 + 2] += b_error * 7 / 16;
						}
						if (y < height - 1) {
							work[py1x - 3] += r_error * 3 / 16;
							work[py1x - 3 + 1] += g_error * 3 / 16;
							work[py1x - 3 + 2] += b_error * 3 / 16;

							work[py1x] += r_error * 5 / 16;
							work[py1x + 1] += g_error * 5 / 16;
							work[py1x + 2] += b_error * 5 / 16;

							if (x < (width - 1) * 3) {
								work[py1x + 3] += r_error / 16;
								work[py1x + 3 + 1] += g_error / 16;
								work[py1x + 3 + 2] += b_error / 16;
							}
						}
						break;
					case ATKINSON:
						if (x < (width - 1) * 3) {
							work[pyx + 3] += r_error * 1 / 8;
							work[pyx + 3 + 1] += g_error * 1 / 8;
							work[pyx + 3 + 2] += b_error * 1 / 8;

							if (x < (width - 2) * 3) {
								work[pyx + 6] += r_error * 1 / 8;
								work[pyx + 6 + 1] += g_error * 1 / 8;
								work[pyx + 6 + 2] += b_error * 1 / 8;
							}
						}
						if (y < height - 1) {
							work[py1x - 3] += r_error * 1 / 8;
							work[py1x - 3 + 1] += g_error * 1 / 8;
							work[py1x - 3 + 2] += b_error * 1 / 8;

							work[py1x] += r_error * 1 / 8;
							work[py1x + 1] += g_error * 1 / 8;
							work[py1x + 2] += b_error * 1 / 8;

							if (x < (width - 1) * 3) {
								work[py1x + 3] += r_error * 1 / 8;
								work[py1x + 3 + 1] += g_error * 1 / 8;
								work[py1x + 3 + 2] += b_error * 1 / 8;
							}

							if (y < height - 2) {
								work[py2x] += r_error * 1 / 8;
								work[py2x + 1] += g_error * 1 / 8;
								work[py2x + 2] += b_error * 1 / 8;
							}
						}
						break;
					}
				}
			}
		}
	}

	@SuppressWarnings("deprecation")
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
					String path = Utils.createDirectory(Config.export_path) + "/";

					final int result = JOptionPane.showConfirmDialog(null, "Export " + fileName + "?", "Confirm",
							JOptionPane.YES_NO_OPTION);
					if (result == 0)
						exportDegas(path, fileName);
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

	protected void exportDegas(final String path, String fileName) {
		try {
			// PI1
			if (fileName.length() > 8)
				fileName = fileName.substring(0, 7);

			fileName = fileName.toUpperCase().replaceAll("[_ ]", "-");
			final BufferedOutputStream chk = new BufferedOutputStream(new FileOutputStream(path + fileName + ".PI1"),
					8192);

			chk.write(0x0); // screen resolution
			chk.write(0x0);

			// palette
			final int len = pictureColors.length;
			for (int i = 0; i < len; i++) {

				final int r;
				final int g;
				final int b;

				final int color[] = pictureColors[i];
				switch (image.getType()) {
				case BufferedImage.TYPE_3BYTE_BGR:
					b = color[0] / 32; // 8 -> 3 bits
					g = color[1] / 32;
					r = color[2] / 32;
					break;
				case BufferedImage.TYPE_INT_RGB:
					r = color[0] / 32; // 8 -> 3 bits
					g = color[1] / 32;
					b = color[2] / 32;
					break;
				default:
					throw new RuntimeException("Unsupported pixel format !!!");
				}

				final int value = (r << 8) | (g << 4) | b;

				final int hi = (value & 0xff00) >> 8;
				final int lo = value & 0xff;

				chk.write(hi); // big endian
				chk.write(lo);
			}

			// bit planes
			for (int i = 0; i < bitplanes.length; i++) {
				final int word = bitplanes[i];
				final int hi = (word & 0xff00) >> 8;
				final int lo = word & 0xff;

				chk.write(hi); // big endian
				chk.write(lo);
			}

			chk.close();
			frame.setTitle(frame.getTitle() + " SAVED");
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected String getTitle() {
		return "ST 320x200x16 ";
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
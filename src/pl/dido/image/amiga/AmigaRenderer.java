package pl.dido.image.amiga;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
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
import pl.dido.image.utils.SOMWinnerFixedPalette;
import pl.dido.image.utils.Utils;

public class AmigaRenderer extends AbstractCachedRenderer {

	protected int bitplanes[][];

	public AmigaRenderer(final BufferedImage image, final String fileName, final Config config) {
		super(image, fileName, config);
		palette = new int[4096][3];
	}

	@Override
	protected void imageDithering() {
		if (config.dithering)
			super.imageDithering();
	}

	@Override
	protected void setupPalette() {
		int i = 0;
		final float k = 255 / 16f;

		for (int r = 0; r < 16; r++) {
			final int rk = (int) (r * k);
			
			for (int g = 0; g < 16; g++) {
				final int gk = (int) (g * k);
				
				for (int b = 0; b < 16; b++) {
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
		final SOMFixedPalette training;

		switch (((AmigaConfig) config).color_mode) {
		case HAM6:
			training = new SOMWinnerFixedPalette(4, 4, 4); // 4x4 = 16 colors (4 bits)
			pictureColors = training.train(pixels);

			ham6WithDithering();
			break;
		case STD32:
			training = new SOMFixedPalette(8, 4, 5); // 8x4 = 32 colors (5 bits)
			pictureColors = training.train(pixels);

			standardWithDithering();
			break;
		}
	}

	protected void standardWithDithering() {
		final int[] work = Utils.copy2Int(pixels);
		bitplanes = new int[(width >> 4) * height][5]; // 5 planes

		int r0, g0, b0;
		int r_error = 0, g_error = 0, b_error = 0;

		final int width3 = width * 3;
		int index = 0, shift = 15; // 16

		for (int y = 0; y < height; y++) {
			final int k = y * width3;
			final int k1 = (y + 1) * width3;
			
			for (int x = 0; x < width3; x += 3) {
				final int pyx = k + x;
				final int py1x = k1 + x;

				r0 = work[pyx];
				g0 = work[pyx + 1];
				b0 = work[pyx + 2];

				final int color = getColorIndex(pictureColors, r0, g0, b0);
				final int c[] = pictureColors[color];

				final int r = c[0];
				final int g = c[1];
				final int b = c[2];

				pixels[pyx] = (byte) r;
				pixels[pyx + 1] = (byte) g;
				pixels[pyx + 2] = (byte) b;

				final int value = color & 0xff;

				bitplanes[index][4] |= (((value & 16) != 0) ? 1 : 0) << shift;
				bitplanes[index][3] |= (((value & 8) != 0) ? 1 : 0) << shift;
				bitplanes[index][2] |= (((value & 4) != 0) ? 1 : 0) << shift;
				bitplanes[index][1] |= (((value & 2) != 0) ? 1 : 0) << shift;
				bitplanes[index][0] |= (value & 1) << shift;

				if (shift == 0) {
					shift = 15;
					index += 1; // 5 planes
				} else
					shift--;

				r_error = Utils.saturate(r0 - r);
				g_error = Utils.saturate(g0 - g);
				b_error = Utils.saturate(b0 - b);

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
						work[py1x + 3] += r_error >> 4;
						work[py1x + 3 + 1] += g_error >> 4;
						work[py1x + 3 + 2] += b_error >> 4;
					}
				}
			}
		}
	}

	protected void ham6WithDithering() {
		final int[] work = Utils.copy2Int(pixels);
		bitplanes = new int[(width >> 4) * height][6]; // 6 planes

		int r0, g0, b0, r, g, b;
		int r_error = 0, g_error = 0, b_error = 0;

		final int width3 = width * 3;
		int index = 0, shift = 15; // WORD

		int modifyRed, modifyBlue;

		switch (image.getType()) {
		case BufferedImage.TYPE_3BYTE_BGR:
			modifyRed = 0b010000;
			modifyBlue = 0b100000;
			break;
		default:
			modifyRed = 0b100000;
			modifyBlue = 0b010000;
			break;
		}

		for (int y = 0; y < height; y++) {
			int oc[] = null;

			for (int x = 0; x < width3; x += 3) {
				final int pyx = y * width3 + x;
				final int py1x = (y + 1) * width3 + x;

				// get picture RGB components
				r0 = work[pyx];
				g0 = work[pyx + 1];
				b0 = work[pyx + 2];

				// find closest palette color
				final int color = getColorIndex(pictureColors, r0, g0, b0); // 16 color palette
				final int pc[] = pictureColors[color];
				
				int action = color & 0xf;
				if (oc != null) { // its not first pixel in a row so use best matching color
					// distance to palette match
					final float dpc = getDistanceByCM(r0, g0, b0, pc[0], pc[1], pc[2]);

					float min_r = Float.MAX_VALUE; // minimum red
					float min_g = min_r;
					float min_b = min_r;

					int ri = -1; int gi = -1; int bi = -1;
					r = oc[0]; g = oc[1]; b = oc[2]; // old color components

					// calculate all color change possibilities and measure distances
					for (int i = 0; i < 16; i++) {
						// scaled color
						final int scaled = (int) (i * 15.9f);

						// which component change gets minimum error?
						final float dr = getDistanceByCM(r0, g0, b0, scaled, g, b);
						final float dg = getDistanceByCM(r0, g0, b0, r, scaled, b);
						final float db = getDistanceByCM(r0, g0, b0, r, g, scaled);

						if (dr < min_r) {
							ri = scaled;
							min_r = dr;
						}

						if (dg < min_g) {
							gi = scaled;
							min_g = dg;
						}

						if (db < min_b) {
							bi = scaled;
							min_b = db;
						}
					}

					final float ham = Utils.min(min_r, min_g, min_b);

					// check which color is best, palette or HAM?
					if (ham < dpc) {
						// HAM is best, alter color
						if (ham == min_r) {
							// red]
							oc[0] = ri;
							action = modifyRed | (ri >> 4);
						} else if (ham == min_g) {
							// green
							oc[1] = gi;
							action = 0b110000 | (gi >> 4);
						} else if (ham == min_b) {
							// blue
							oc[2] = bi;
							action = modifyBlue | (bi >> 4);
						}
					} else {
						// pc.clone();
						oc[0] = pc[0];
						oc[1] = pc[1];
						oc[2] = pc[2];
					}
				} else	
					oc = pc.clone();
				
				bitplanes[index][5] |= (((action & 32) != 0) ? 1 : 0) << shift;
				bitplanes[index][4] |= (((action & 16) != 0) ? 1 : 0) << shift;

				bitplanes[index][3] |= (((action & 8)  != 0) ? 1 : 0) << shift;
				bitplanes[index][2] |= (((action & 4)  != 0) ? 1 : 0) << shift;

				bitplanes[index][1] |= (((action & 2)  != 0) ? 1 : 0) << shift;
				bitplanes[index][0] |= (action & 1) << shift;

				if (shift == 0) {
					shift = 15; // WORD
					index += 1;
				} else
					shift--;

				r = oc[0]; g = oc[1]; b = oc[2];

				pixels[pyx] = (byte) r;
				pixels[pyx + 1] = (byte) g;
				pixels[pyx + 2] = (byte) b;

				r_error = Utils.saturate(r0 - r);
				g_error = Utils.saturate(g0 - g);
				b_error = Utils.saturate(b0 - b);

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
						work[py1x + 3] += r_error >> 4;
						work[py1x + 3 + 1] += g_error >> 4;
						work[py1x + 3 + 2] += b_error >> 4;
					}
				}
			}
		}
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
					String path = Utils.createDirectory(Config.export_path) + "/";

					final int result = JOptionPane.showConfirmDialog(null, "Export " + fileName + "?", "Confirm",
							JOptionPane.YES_NO_OPTION);
					if (result == 0)
						exportIFF(path, fileName);
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

	protected byte[] bigEndianDWORD(final int data) {
		final byte p[] = new byte[4];

		p[0] = (byte) ((data & 0xff000000) >> 24);
		p[1] = (byte) ((data & 0xff0000) >> 16);
		p[2] = (byte) ((data & 0xff00) >> 8);
		p[3] = (byte) (data & 0xff);

		return p;
	}

	protected byte[] bigEndianWORD(final int data) {
		final byte p[] = new byte[2];

		p[0] = (byte) ((data & 0xff00) >> 8);
		p[1] = (byte) (data & 0xff);

		return p;
	}

	protected byte[] chunk(final String name, final byte[]... data) throws IOException {
		int size = 0;

		for (final byte[] p : data)
			size += p.length;

		final ByteArrayOutputStream mem = new ByteArrayOutputStream(size);

		mem.write(name.getBytes());
		mem.write(bigEndianDWORD(size));

		for (final byte[] p : data)
			mem.write(p);

		return mem.toByteArray();
	}

	protected byte[] getSTDBitmap() throws IOException {
		final ByteArrayOutputStream mem = new ByteArrayOutputStream((width * height / 8) * 5); // 5 bit planes

		final int size = width >> 4;
		for (int y = 0; y < height; y++) {
			for (int plane = 0; plane < 5; plane++)
				for (int x = 0; x < size; x++) {
					final int a = size * y + x;
					final int d = bitplanes[a][plane]; // WORD 2xbytes

					mem.write(bigEndianWORD(d));
				}
		}

		return mem.toByteArray();
	}

	protected byte[] getHAM6Bitmap() throws IOException {
		final ByteArrayOutputStream mem = new ByteArrayOutputStream((width * height / 8) * 6); // 6 bit planes

		final int size = width >> 4;
		for (int y = 0; y < height; y++) {
			for (int plane = 0; plane < 6; plane++)
				for (int x = 0; x < size; x++) {
					final int a = size * y + x;
					final int d = bitplanes[a][plane];

					mem.write(bigEndianWORD(d));
				}
		}

		return mem.toByteArray();
	}

	protected byte[] getILBMHD() throws IOException {
		final ByteArrayOutputStream mem = new ByteArrayOutputStream(28);

		mem.write(bigEndianWORD(width));
		mem.write(bigEndianWORD(height));
		mem.write(bigEndianWORD(0)); // position on screen X,Y
		mem.write(bigEndianWORD(0));

		if (((AmigaConfig) config).color_mode == AmigaConfig.COLOR_MODE.HAM6)
			mem.write(6); // hamcode
		else
			mem.write(5); // bitplanes

		mem.write(0); // mask
		mem.write(0); // compress 0 = none
		mem.write(0); // padding
		mem.write(bigEndianWORD(0)); // transparent background color
		mem.write(1); // aspect X
		mem.write(1); // aspect Y
		mem.write(bigEndianWORD(width)); // page width
		mem.write(bigEndianWORD(height)); // page height

		return mem.toByteArray();
	}

	protected byte[] getILBMFormat(final byte[]... chunk) throws IOException {
		final ByteArrayOutputStream mem = new ByteArrayOutputStream(64 * 1024);

		mem.write("FORM".getBytes());

		int size = 0;
		for (final byte[] p: chunk)
			size += p.length;

		mem.write(bigEndianDWORD(size + 4)); // includes ILBM
		mem.write("ILBM".getBytes()); // planar data

		for (final byte[] p: chunk)
			mem.write(p);

		return mem.toByteArray();
	}

	protected byte[] getCMAP() throws IOException {
		final int size = pictureColors.length;
		final ByteArrayOutputStream mem = new ByteArrayOutputStream(size * 3);

		for (int i = 0; i < size; i++) {
			final int color[] = pictureColors[i];

			switch (image.getType()) {
			case BufferedImage.TYPE_3BYTE_BGR:
				mem.write(color[2]);
				mem.write(color[1]);
				mem.write(color[0]);
				break;
			case BufferedImage.TYPE_INT_RGB:
				mem.write(color[0]);
				mem.write(color[1]);
				mem.write(color[2]);
				break;
			}
		}

		return mem.toByteArray();
	}

	protected void exportIFF(final String path, String fileName) {
		try {
			// PI1
			if (fileName.length() > 8)
				fileName = fileName.substring(0, 7);

			fileName = fileName.toUpperCase().replaceAll("[_ ]", "-");
			final BufferedOutputStream chk = new BufferedOutputStream(new FileOutputStream(path + fileName + ".iff"),
					8192);

			switch (((AmigaConfig) config).color_mode) {
			case HAM6:
				chk.write(getILBMFormat(
						chunk("BMHD", getILBMHD()), 
						chunk("CMAP", getCMAP()),
						chunk("CAMG", bigEndianDWORD(0x800)),
						chunk("BODY", getHAM6Bitmap())));
				break;
			case STD32:
				chk.write(getILBMFormat(
						chunk("BMHD", getILBMHD()), 
						chunk("CMAP", getCMAP()), 
						chunk("CAMG", bigEndianDWORD(0x1000)),
						chunk("BODY", getSTDBitmap())));
				break;
			}

			chk.close();

			frame.setTitle(frame.getTitle() + " SAVED");
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected String getTitle() {
		return "A500 320x256";
	}

	@Override
	protected int getHeight() {
		return 256;
	}

	@Override
	protected int getWidth() {
		return 320;
	}
}
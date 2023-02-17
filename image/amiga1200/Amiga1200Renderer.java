package pl.dido.image.amiga1200;

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
import pl.dido.image.utils.Utils;

public class Amiga1200Renderer extends AbstractCachedRenderer {

	protected int bitplanes[][];

	public Amiga1200Renderer(final BufferedImage image, final String fileName, final Config config) {
		super(image, fileName, config);
		// do not use generated machine palette, it is true color
	}

	@Override
	protected void imageDithering() {
		// not needed, 16M palette
	}

	@Override
	protected void setupPalette() {
		// do not generate palette
	}

	@Override
	protected int getScale() {
		switch (((Amiga1200Config) config).video_mode) {
		case HAM8_320x256:
		case STD_320x256:
			return 2;
		default:
			return 1;
		}
	}

	@Override
	protected void imagePostproces() {
		final SOMFixedPalette training;

		switch (((Amiga1200Config) config).video_mode) {
		case HAM8_320x256:
		case HAM8_320x512:
		case HAM8_640x512:
			training = new SOMFixedPalette(8, 8, 8, 4); // 8x8 = 64 colors (8 bit per component)
			pictureColors = training.train(pixels);

			ham8Encoded();
			break;
		case STD_320x256:
		case STD_320x512:
		case STD_640x512:
			training = new SOMFixedPalette(16, 16, 8, 4); // 16x16 = 256 colors (8 bits)
			pictureColors = training.train(pixels);

			standard256();
			break;
		}
	}

	protected void standard256() {
		final float[] work = Utils.copy2float(pixels);
		bitplanes = new int[(width >> 4) * height][8]; // 8 planes

		int r0, g0, b0;
		float r_error = 0, g_error = 0, b_error = 0;

		final int width3 = width * 3;
		int index = 0, shift = 15; // 16

		for (int y = 0; y < height; y++) {
			final int k = y * width3;
			final int k1 = (y + 1) * width3;
			final int k2 = ((y + 2) * width3);

			for (int x = 0; x < width3; x += 3) {
				final int pyx = k + x;
				final int py1x = k1 + x;
				final int py2x = k2 + x;

				r0 = Utils.saturate((int) work[pyx]);
				g0 = Utils.saturate((int) work[pyx + 1]);
				b0 = Utils.saturate((int) work[pyx + 2]);

				final int color = getColorIndex(pictureColors, r0, g0, b0); // 256 colors
				final int c[] = pictureColors[color];

				final int r = c[0];
				final int g = c[1];
				final int b = c[2];

				pixels[pyx] = (byte) r;
				pixels[pyx + 1] = (byte) g;
				pixels[pyx + 2] = (byte) b;

				bitplanes[index][7] |= ((color & 128) >> 7) << shift;
				bitplanes[index][6] |= ((color & 64) >> 6) << shift;
				bitplanes[index][5] |= ((color & 32) >> 5) << shift;
				bitplanes[index][4] |= ((color & 16) >> 4) << shift;
				bitplanes[index][3] |= ((color & 8) >> 3) << shift;
				bitplanes[index][2] |= ((color & 4) >> 2) << shift;
				bitplanes[index][1] |= ((color & 2) >> 1) << shift;
				bitplanes[index][0] |= (color & 1) << shift;

				if (shift == 0) {
					shift = 15;
					index += 1; // 8 planes
				} else
					shift--;

				if (config.dithering) {
					r_error = r0 - r;
					g_error = g0 - g;
					b_error = b0 - b;

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

	protected void ham8Encoded() {
		final float[] work = Utils.copy2float(pixels);
		bitplanes = new int[(width >> 4) * height][8]; // 8 planes

		int r0, g0, b0, r = 0, g = 0, b = 0;
		final int width3 = width * 3;

		int index = 0, shift = 15; // WORD
		int modifyRed, modifyBlue;

		switch (image.getType()) {
		case BufferedImage.TYPE_3BYTE_BGR:
			modifyRed  = 0b01000000;
			modifyBlue = 0b10000000;
			break;
		default:
			modifyRed  = 0b10000000;
			modifyBlue = 0b01000000;
			break;
		}

		for (int y = 0; y < height; y++) {
			boolean nextPixel = false;
			final int k = y * width3;
			
			final int k1 = (y + 1) * width3;
			final int k2 = (y + 2) * width3;

			for (int x = 0; x < width3; x += 3) {
				final int pyx = k + x;
				final int py1x = k1 + x;
				
				final int py2x = k2 + x;

				// get picture RGB components
				r0 = Utils.saturate((int) work[pyx]);
				g0 = Utils.saturate((int) work[pyx + 1]);
				b0 = Utils.saturate((int) work[pyx + 2]);

				// find closest palette color
				int action = getColorIndex(pictureColors, r0, g0, b0); // 64 color palette
				final int pc[] = pictureColors[action];

				if (nextPixel) { // it's not first pixel in a row so use best matching color
					// distance to palette match
					final float dpc = getDistanceByCM(r0, g0, b0, pc[0], pc[1], pc[2]);

					float min_r = Float.MAX_VALUE; // minimum red
					float min_g = min_r;
					float min_b = min_r;

					int ri = -1;
					int gi = -1;
					int bi = -1;

					// calculate all color change possibilities and measure distances
					for (int i = 0; i < 64; i++) {
						// scaled color
						final int scaled = (int) Math.ceil(i * 3.98f);

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
							// red
							r = ri;
							action = modifyRed | (ri >> 2);
						} else if (ham == min_g) {
							// green
							g = gi;
							action = 0b11000000 | (gi >> 2);
						} else if (ham == min_b) {
							// blue
							b = bi;
							action = modifyBlue | (bi >> 2);
						}
					} else {
						r = pc[0];
						g = pc[1];
						b = pc[2];
					}
				} else {
					nextPixel = true;
					r = pc[0];
					g = pc[1];
					b = pc[2];
				}

				bitplanes[index][7] |= ((action & 128) >> 7) << shift;
				bitplanes[index][6] |= ((action & 64) >> 6) << shift;

				bitplanes[index][5] |= ((action & 32) >> 5) << shift;
				bitplanes[index][4] |= ((action & 16) >> 4) << shift;
				
				bitplanes[index][3] |= ((action & 8) >> 3) << shift;
				bitplanes[index][2] |= ((action & 4) >> 2) << shift;
				
				bitplanes[index][1] |= ((action & 2) >> 1) << shift;
				bitplanes[index][0] |= (action & 1) << shift;
				
				if (shift == 0) {
					shift = 15; // WORD
					index += 1;
				} else
					shift--;

				pixels[pyx] = (byte) r;
				pixels[pyx + 1] = (byte) g;
				pixels[pyx + 2] = (byte) b;
				
				float r_error = r0 - r;
				float g_error = g0 - g;
				float b_error = b0 - b;
				
				switch (config.dither_alg) {
				case STD_FS:
					if (x < (width - 1) * 3) {
						work[pyx + 3]     += r_error * 7 / 16;
						work[pyx + 3 + 1] += g_error * 7 / 16;
						work[pyx + 3 + 2] += b_error * 7 / 16;
					}
					if (y < height - 1) {
						work[py1x - 3]     += r_error * 3 / 16;
						work[py1x - 3 + 1] += g_error * 3 / 16;
						work[py1x - 3 + 2] += b_error * 3 / 16;

						work[py1x]     += r_error * 5 / 16;
						work[py1x + 1] += g_error * 5 / 16;
						work[py1x + 2] += b_error * 5 / 16;

						if (x < (width - 1) * 3) {
							work[py1x + 3]     += r_error / 16;
							work[py1x + 3 + 1] += g_error / 16;
							work[py1x + 3 + 2] += b_error / 16;
						}
					}							
					break;
				case ATKINSON:
					if (x < (width - 1) * 3) {
						work[pyx + 3]     += r_error * 1 / 8;
						work[pyx + 3 + 1] += g_error * 1 / 8;
						work[pyx + 3 + 2] += b_error * 1 / 8;
						
						if (x < (width - 2) * 3) {
							work[pyx + 6]     += r_error * 1 / 8;
							work[pyx + 6 + 1] += g_error * 1 / 8;
							work[pyx + 6 + 2] += b_error * 1 / 8;
						}
					}
					if (y < height - 1) {
						work[py1x - 3]     += r_error * 1 / 8;
						work[py1x - 3 + 1] += g_error * 1 / 8;
						work[py1x - 3 + 2] += b_error * 1 / 8;

						work[py1x]     += r_error * 1 / 8;
						work[py1x + 1] += g_error * 1 / 8;
						work[py1x + 2] += b_error * 1 / 8;

						if (x < (width - 1) * 3) {
							work[py1x + 3]     += r_error * 1 / 8;
							work[py1x + 3 + 1] += g_error * 1 / 8;
							work[py1x + 3 + 2] += b_error * 1 / 8;
						}
						
						if (y < height - 2) {
							work[py2x]     += r_error * 1 / 8;
							work[py2x + 1] += g_error * 1 / 8;
							work[py2x + 2] += b_error * 1 / 8;
						}
					}					
					break;					
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

	protected byte[] getBitmap() throws IOException {
		final ByteArrayOutputStream mem = new ByteArrayOutputStream(width * height); // 8 bit planes

		final int size = width >> 4;
		for (int y = 0; y < height; y++) {
			for (int plane = 0; plane < 8; plane++)
				for (int x = 0; x < size; x++) {
					final int a = size * y + x;
					final int d = bitplanes[a][plane]; // WORD 2xbytes

					mem.write(bigEndianWORD(d));
				}
		}

		return mem.toByteArray();
	}

	protected byte[] getILBMHD(final int aspectX, final int aspectY) throws IOException {
		final ByteArrayOutputStream mem = new ByteArrayOutputStream(28);

		mem.write(bigEndianWORD(width));
		mem.write(bigEndianWORD(height));
		mem.write(bigEndianWORD(0)); // position on screen X,Y
		mem.write(bigEndianWORD(0));

		mem.write(8); // HAM & 256

		mem.write(0); // mask
		mem.write(0); // compress 0 = none
		mem.write(0); // padding
		mem.write(bigEndianWORD(0)); // transparent background color
		mem.write(aspectX); // aspect X
		mem.write(aspectY); // aspect Y
		mem.write(bigEndianWORD(width)); // page width
		mem.write(bigEndianWORD(height)); // page height

		return mem.toByteArray();
	}

	protected byte[] getILBMFormat(final byte[]... chunk) throws IOException {
		final ByteArrayOutputStream mem = new ByteArrayOutputStream(64 * 1024);

		mem.write("FORM".getBytes());

		int size = 0;
		for (final byte[] p : chunk)
			size += p.length;

		mem.write(bigEndianDWORD(size + 4)); // includes ILBM
		mem.write("ILBM".getBytes()); // planar data

		for (final byte[] p : chunk)
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
			if (fileName.length() > 8)
				fileName = fileName.substring(0, 7);

			fileName = fileName.toUpperCase().replaceAll("[_ ]", "-");
			final BufferedOutputStream chk = new BufferedOutputStream(new FileOutputStream(path + fileName + ".iff"),
					8192);

			int videoMode = 0, aspectX = 0, aspectY = 0;
			switch (((Amiga1200Config) config).video_mode) {
			case STD_320x256:
				videoMode = 0x0000;
				aspectX = 44;
				aspectY = 44;
				break;
			case HAM8_320x256:
				videoMode = 0x0800;
				aspectX = 44;
				aspectY = 44;
				break;
			case HAM8_320x512:
				videoMode = 0x0804;
				aspectX = 22;
				aspectY = 44;
				break;
			case STD_320x512:
				videoMode = 0x0004;
				aspectX = 22;
				aspectY = 44;
				break;
			case HAM8_640x512:
				videoMode = 0x8804;
				aspectX = 44;
				aspectY = 44;
				break;
			case STD_640x512:
				videoMode = 0x8004;
				aspectX = 44;
				aspectY = 44;
				break;
			}

			chk.write(getILBMFormat(chunk("BMHD", getILBMHD(aspectX, aspectY)), chunk("CMAP", getCMAP()),
					chunk("CAMG", bigEndianDWORD(videoMode)), chunk("BODY", getBitmap())));
			chk.close();

			frame.setTitle(frame.getTitle() + " SAVED");
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected String getTitle() {
		return "A1200 " + getWidth() + "x" + getHeight();
	}

	@Override
	protected int getHeight() {
		switch (((Amiga1200Config) config).video_mode) {
		case HAM8_320x256:
		case STD_320x256:
			return 256;

		case HAM8_320x512:
		case HAM8_640x512:
		case STD_640x512:
		case STD_320x512:
			return 512;
		}

		return -1;
	}

	@Override
	protected int getWidth() {
		switch (((Amiga1200Config) config).video_mode) {
		case HAM8_320x256:
		case STD_320x256:
		case HAM8_320x512:
		case STD_320x512:
			return 320;

		case HAM8_640x512:
		case STD_640x512:
			return 640;
		}

		return -1;
	}
}
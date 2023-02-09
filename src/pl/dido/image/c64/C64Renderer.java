package pl.dido.image.c64;

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

public class C64Renderer extends AbstractRenderer {

	// C64 palette
	private final static int colors[] = new int[] { 0, 0xFFFFFF, 0x68372B, 0x70A4B2, 0x6F3D86, 0x588D43, 0x352879,
			0xB8C76F, 0x6F4F25, 0x433900, 0x9A6759, 0x444444, 0x6C6C6C, 0x9AD284, 0x6C5EB5, 0x959595 };

	protected int bitmap[] = new int[40 * 200];
	protected int screen[] = new int[1000];

	protected int nibble[] = new int[1000];
	protected int backgroundColor = 0;

	public C64Renderer(final BufferedImage image, final String fileName, final C64Config config) {
		super(image, fileName, config);

		palette = new int[16][3];
	}

	@Override
	protected void imageDithering() {
		if (config.dithering)
			super.imageDithering();
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
	protected void imagePostproces() {
		switch (((C64Config) config).screen_mode) {
		case HIRES:
			hiresLumaDithered();
			break;
		case MULTICOLOR:
			lowresOccurrenceDithered();
			break;
		}
	}

	private void hiresExport(final String fileName) {
		try {
			final BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(new File(fileName)), 8192);

			// loading address
			out.write(0);
			out.write(0x20);

			// bitmap
			for (int i = 0; i < bitmap.length; i++)
				out.write(bitmap[i] & 0xff);

			// attributes
			for (int i = 0; i < screen.length; i++)
				out.write(screen[i] & 0xff);

			out.close();
			frame.setTitle(frame.getTitle() + " SAVED");
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	private void hiresExportPRG(final String fileName) {
		try {
			final BufferedInputStream in = new BufferedInputStream(Utils.getResourceAsStream("high.prg"), 8192);
			final BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(new File(fileName)), 8192);

			// loading address BASIC
			out.write(0x01);
			out.write(0x08);

			int data, prg_len = 0;
			in.read(); // skip loading address
			in.read();

			while ((data = in.read()) != -1) {
				out.write(data);
				prg_len += 1;
			}

			in.close();

			// spare bytes
			int spare = 6143 - prg_len;
			for (int i = 0; i < spare; i++)
				out.write(0xff);

			// bitmap
			for (int i = 0; i < bitmap.length; i++)
				out.write(bitmap[i] & 0xff);

			// attributes
			for (int i = 0; i < screen.length; i++)
				out.write(screen[i] & 0xff);

			out.close();
			frame.setTitle(frame.getTitle() + " SAVED");
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	private void lowresExportPRG(final String fileName) {
		try {
			final BufferedInputStream in = new BufferedInputStream(Utils.getResourceAsStream("low.prg"), 8192);
			final BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(new File(fileName)), 8192);

			// loading address BASIC
			out.write(0x01);
			out.write(0x08);

			int data, prg_len = 0;
			in.read(); // skip loading address
			in.read();

			while ((data = in.read()) != -1) {
				out.write(data);
				prg_len += 1;
			}

			in.close();

			// spare bytes
			int spare = 6143 - prg_len;
			for (int i = 0; i < spare; i++)
				out.write(0xff);

			// bitmap
			for (int i = 0; i < 8000; i++)
				out.write(bitmap[i] & 0xff);

			// attributes
			for (int i = 0; i < 1000; i++)
				out.write(screen[i] & 0xff);

			// color nibbles
			for (int i = 0; i < 1000; i++)
				out.write(nibble[i] & 0xf);

			out.write(backgroundColor & 0xff);

			out.close();
			frame.setTitle(frame.getTitle() + " SAVED");
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	private void lowresExport(final String fileName) {
		try {
			final BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(new File(fileName)), 8192);

			// header
			out.write(0);
			out.write(0x60);

			// bitmap
			for (int i = 0; i < 8000; i++)
				out.write(bitmap[i] & 0xff);

			// attributes
			for (int i = 0; i < 1000; i++)
				out.write(screen[i] & 0xff);

			// color nibbles
			for (int i = 0; i < 1000; i++)
				out.write(nibble[i] & 0xf);

			out.write(backgroundColor & 0xff);

			out.close();
			frame.setTitle(frame.getTitle() + " SAVED");
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	protected void hiresLumaDithered() {
		final int work[] = new int[64 * 3];
		int bitmapIndex = 0;

		for (int y = 0; y < 200; y += 8) {
			final int p = y * 320 * 3;

			for (int x = 0; x < 320; x += 8) {
				final int offset = p + x * 3;

				float min = 255;
				float max = 0;

				int index = 0;
				int f = 0, n = 0;

				// 8x8 tile
				for (int y0 = 0; y0 < 8; y0++) {
					for (int x0 = 0; x0 < 24; x0 += 3) {
						final int position = offset + y0 * 320 * 3 + x0;

						final int r = pixels[position] & 0xff;
						final int g = pixels[position + 1] & 0xff;
						final int b = pixels[position + 2] & 0xff;

						work[index++] = r;
						work[index++] = g;
						work[index++] = b;

						switch (((C64Config) config).luma_pixels) {
						case OUTER:
							if (x0 > 6 && x0 < 18 && y0 > 2 && y0 < 6) // consider only outer pixels
								continue;
							break;
						case INNER:
							if (((y0 << 3) + x0) % 2 == 0) // gets only even pixels
								continue;
							break;
						}

						final float luma = getLumaByCM(r, g, b);

						if (luma > max) {
							max = luma;
							f = getColorIndex(r, g, b);
						}

						if (luma < min) {
							min = luma;
							n = getColorIndex(r, g, b);
						}
					}
				}

				screen[(y >> 3) * 40 + (x >> 3)] = ((f & 0xf) << 4) | (n & 0xf);
				int value = 0, bitcount = 0;

				for (int y0 = 0; y0 < 8; y0++)
					for (int x0 = 0; x0 < 24; x0 += 3) {
						final int pyx0 = y0 * 24 + x0;
						final int py1x0 = (y0 + 1) * 24 + x0;

						final int r = work[pyx0];
						final int g = work[pyx0 + 1];
						final int b = work[pyx0 + 2];

						final int cf[] = palette[f];
						final int fr = cf[0];
						final int fg = cf[1];
						final int fb = cf[2];

						final int cn[] = palette[n];
						int nr = cn[0];
						int ng = cn[1];
						int nb = cn[2];

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
							bitmap[bitmapIndex++] = value;
							value = 0;
						}

						bitcount += 1;
						final int position = offset + y0 * 320 * 3 + x0;

						pixels[position] = (byte) nr;
						pixels[position + 1] = (byte) ng;
						pixels[position + 2] = (byte) nb;

						final int r_error = Utils.saturate(r - nr);
						final int g_error = Utils.saturate(g - ng);
						final int b_error = Utils.saturate(b - nb);

						if (x0 < 21) {
							work[pyx0 + 3] += r_error * 7 / 16;
							work[pyx0 + 3 + 1] += g_error * 7 / 16;
							work[pyx0 + 3 + 2] += b_error * 7 / 16;
						}

						if (y0 < 7) {
							work[py1x0 - 3] += r_error * 3 / 16;
							work[py1x0 - 3 + 1] += g_error * 3 / 16;
							work[py1x0 - 3 + 2] += b_error * 3 / 16;

							work[py1x0] += r_error * 5 / 16;
							work[py1x0 + 1] += g_error * 5 / 16;
							work[py1x0 + 2] += b_error * 5 / 16;

							if (x0 < 21) {
								work[py1x0 + 3] += r_error >> 4;
								work[py1x0 + 3 + 1] += g_error >> 4;
								work[py1x0 + 3 + 2] += b_error >> 4;
							}
						}
					}
			}
		}
	}

	protected void lowresOccurrenceDithered() {
		final int[] newPixels = new int[160 * 200 * 3]; // 160x200
		int bitmapIndex = 0;
		int sr = 0, sg = 0, sb = 0;

		// shrinking 320x200 -> 160x200
		for (int y = 0; y < 200; y += 8) {
			final int p1 = y * 320 * 3;
			final int p2 = y * 160 * 3;

			for (int x = 0; x < 40; x++) {
				final int o1 = p1 + x * 8 * 3;
				final int o2 = p2 + x * 4 * 3;

				// 8x8 tile -> 4x8 tile
				for (int ty = 0; ty < 8; ty++) {
					for (int tx = 0, qx = 0; tx < 24; tx += 6, qx += 3) {
						final int ph = o1 + ty * 320 * 3 + tx;
						final int pl = o2 + ty * 160 * 3 + qx;

						final int r1 = pixels[ph] & 0xff;
						final int g1 = pixels[ph + 1] & 0xff;
						final int b1 = pixels[ph + 2] & 0xff;

						final int r2 = pixels[ph + 3] & 0xff;
						final int g2 = pixels[ph + 4] & 0xff;
						final int b2 = pixels[ph + 5] & 0xff;

						final int r, g, b;
						switch (((C64Config) config).pixel_merge) {
						case AVERAGE:
							// average color
							r = (r1 + r2) >> 1;
							g = (g1 + g2) >> 1;
							b = (b1 + b2) >> 1;
							break;
						default:
							if (getLumaByCM(r1, g1, b1) > getLumaByCM(r2, g2, b2)) {
								r = r1;
								g = g1;
								b = b1;
							} else {
								r = r2;
								g = g2;
								b = b2;
							}

							break;
						}

						final int i = getColorIndex(r, g, b);
						sr += r;
						sg += g;
						sb += b;

						final int c[] = palette[i];
						newPixels[pl] = c[0];
						newPixels[pl + 1] = c[1];
						newPixels[pl + 2] = c[2];
					}
				}
			}
		}

		final int work[] = new int[32 * 3];
		sr /= 160 * 200;
		sg /= 160 * 200;
		sb /= 160 * 200;

		// 4x8 tile palette
		final int tilePalette[][] = new int[4][3];
		backgroundColor = getColorIndex(sr, sg, sb);

		for (int y = 0; y < 200; y += 8) {
			final int p1 = y * 160 * 3;

			for (int x = 0; x < 40; x++) {
				final int occurrence[] = new int[16];
				
				final int o1 = p1 + x * 4 * 3;
				int index = 0;

				// common color
				tilePalette[0][0] = sr;
				tilePalette[0][1] = sg;
				tilePalette[0][2] = sb;

				for (int y0 = 0; y0 < 8; y0++) {
					for (int x0 = 0; x0 < 12; x0 += 3) {
						final int position = o1 + y0 * 160 * 3 + x0;

						final int r = newPixels[position];
						final int g = newPixels[position + 1];
						final int b = newPixels[position + 2];

						work[index++] = r;
						work[index++] = g;
						work[index++] = b;

						occurrence[getColorIndex(r, g, b)]++;
					}
				}

				int m1 = 0, m2 = 0, m3 = Integer.MAX_VALUE;
				int i1 = 0, i2 = 0, i3 = 0;

				// 3 most popular colors
				for (int i = 0; i < 16; i++) {
					final int k = occurrence[i];
					if (k > m1) {
						i3 = i2;
						m3 = m2;

						i2 = i1;
						m2 = m1;

						i1 = i;
						m1 = k;
					} else if (k > m2) {
						i3 = i2;
						m3 = m2;

						i2 = i;
						m2 = k;
					} else if (k > m3) {
						i3 = i;
						m3 = k;
					}
				}
				
				int t[] = tilePalette[1];
				int p[] = palette[i1];

				t[0] = p[0];
				t[1] = p[1];
				t[2] = p[2];
				
				t = tilePalette[2];
				p = palette[i2];
				
				t[0] = p[0];
				t[1] = p[1];
				t[2] = p[2];

				t = tilePalette[3];
				p = palette[i3];

				t[0] = p[0];
				t[1] = p[1];
				t[2] = p[2];

				int position = (y >> 3) * 40 + x;
				screen[position] = ((i1 & 0xf) << 4) | (i2 & 0xf);
				nibble[position] = i3;

				int value = 0, bitcount = 0;
				for (int y0 = 0; y0 < 8; y0++)
					for (int x0 = 0; x0 < 12; x0 += 3) {
						final int pyx0 = y0 * 12 + x0;
						final int py1x0 = (y0 + 1) * 12 + x0;

						final int r = work[pyx0];
						final int g = work[pyx0 + 1];
						final int b = work[pyx0 + 2];

						index = getColorIndex(tilePalette, r, g, b);
						final int c[] = tilePalette[index]; 
						final int nr = c[0];
						final int ng = c[1];
						final int nb = c[2];

						work[pyx0] = nr;
						work[pyx0 + 1] = ng;
						work[pyx0 + 2] = nb;

						value = (value << 2) | (index & 0x3);
						if (bitcount % 4 == 3) {
							bitmap[bitmapIndex++] = value;
							value = 0;
						}

						bitcount += 1;

						final int r_error = Utils.saturate(r - nr);
						final int g_error = Utils.saturate(g - ng);
						final int b_error = Utils.saturate(b - nb);

						if (x0 < 9) {
							work[pyx0 + 3] += r_error * 7 / 16;
							work[pyx0 + 3 + 1] += g_error * 7 / 16;
							work[pyx0 + 3 + 2] += b_error * 7 / 16;
						}

						if (y0 < 7) {
							work[py1x0 - 3] += r_error * 3 / 16;
							work[py1x0 - 3 + 1] += g_error * 3 / 16;
							work[py1x0 - 3 + 2] += b_error * 3 / 16;

							work[py1x0] += r_error * 5 / 16;
							work[py1x0 + 1] += g_error * 5 / 16;
							work[py1x0 + 2] += b_error * 5 / 16;

							if (x0 < 9) {
								work[py1x0 + 3] += r_error >> 4;
								work[py1x0 + 3 + 1] += g_error >> 4;
								work[py1x0 + 3 + 2] += b_error >> 4;
							}
						}	
					}

				index = 0;

				// show results
				final int p2 = y * 320 * 3;
				final int o2 = p2 + x * 24;

				for (int ty = 0; ty < 8; ty++)
					for (int tx = 0; tx < 24; tx += 6) {
						position = o2 + ty * 320 * 3 + tx;

						pixels[position] = (byte) work[index];
						pixels[position + 3] = (byte) work[index++];

						pixels[position + 1] = (byte) work[index];
						pixels[position + 4] = (byte) work[index++];

						pixels[position + 2] = (byte) work[index];
						pixels[position + 5] = (byte) work[index++];
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
					String exportFileName = Utils.createDirectory(Config.export_path) + "/";

					switch (((C64Config) config).screen_mode) {
					case HIRES:
						exportFileName += fileName + ".art";
						break;
					case MULTICOLOR:
						exportFileName += fileName + ".koa";
						break;
					}

					final int result = JOptionPane.showConfirmDialog(null, "Export " + exportFileName + "?", "Confirm",
							JOptionPane.YES_NO_OPTION);
					if (result == 0)
						switch (((C64Config) config).screen_mode) {
						case HIRES:
							hiresExport(exportFileName);
							break;
						case MULTICOLOR:
							lowresExport(exportFileName);
							break;
						}
				} catch (final IOException ex) {
					JOptionPane.showMessageDialog(null, "Error", ex.getMessage(), JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		menuFile.add(miArtStudio);

		final JMenuItem miExecutable = new JMenuItem("Export as executable... ");
		miExecutable.setMnemonic(KeyEvent.VK_E);
		miExecutable.setAccelerator(
				KeyStroke.getKeyStroke(KeyEvent.VK_E, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		miExecutable.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				try {
					final String exportFileName = Utils.createDirectory(Config.export_path) + "/" + fileName + ".prg";

					final int result = JOptionPane.showConfirmDialog(null, "Export " + exportFileName + "?", "Confirm",
							JOptionPane.YES_NO_OPTION);
					if (result == 0)
						switch (((C64Config) config).screen_mode) {
						case HIRES:
							hiresExportPRG(exportFileName);
							break;
						case MULTICOLOR:
							lowresExportPRG(exportFileName);
							break;
						}
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
		return "C64 ";
	}

	@Override
	protected int getHeight() {
		return 200;
	}

	@Override
	protected int getWidth() {
		return 320;
	}
}
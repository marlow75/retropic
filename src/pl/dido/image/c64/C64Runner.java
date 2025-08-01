package pl.dido.image.c64;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
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

import pl.dido.image.renderer.AbstractRenderer;
import pl.dido.image.renderer.AbstractRendererRunner;
import pl.dido.image.utils.Config;
import pl.dido.image.utils.Utils;

public class C64Runner extends AbstractRendererRunner {
	
	protected C64Renderer c64;

	public C64Runner(final AbstractRenderer renderer, final String fileName) {
		super(renderer, fileName);
		c64 = (C64Renderer) renderer;
	}

	private void hiresExport(final String fileName) {
		try {
			final BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(new File(fileName)), 8192);

			// loading address
			out.write(0);
			out.write(0x20);

			// bitmap
			for (int i = 0; i < c64.bitmap.length; i++)
				out.write(c64.bitmap[i] & 0xff);

			// attributes
			for (int i = 0; i < c64.screen.length; i++)
				out.write(c64.screen[i] & 0xff);

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
			for (int i = 0; i < c64.bitmap.length; i++)
				out.write(c64.bitmap[i] & 0xff);

			// attributes
			for (int i = 0; i < c64.screen.length; i++)
				out.write(c64.screen[i] & 0xff);

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
				out.write(c64.bitmap[i] & 0xff);

			// attributes
			for (int i = 0; i < 1000; i++)
				out.write(c64.screen[i] & 0xff);

			// color nibbles
			for (int i = 0; i < 1000; i++)
				out.write(c64.nibbles[i] & 0xf);

			out.write(c64.backgroundColor & 0xff);

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
				out.write(c64.bitmap[i] & 0xff);

			// attributes
			for (int i = 0; i < 1000; i++)
				out.write(c64.screen[i] & 0xff);

			// color nibbles
			for (int i = 0; i < 1000; i++)
				out.write(c64.nibbles[i] & 0xf);

			out.write(c64.backgroundColor & 0xff);

			out.close();
			frame.setTitle(frame.getTitle() + " SAVED");
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	protected JMenuBar getMenuBar() {
		final JMenu menuFile = new JMenu("File");
		menuFile.setMnemonic(KeyEvent.VK_F);

		final JMenuItem miFile = new JMenuItem("Export as picture... ");
		miFile.setMnemonic(KeyEvent.VK_S);
		miFile.setAccelerator(
				KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
		miFile.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				try {
					String exportFileName = Utils.createDirectory(Config.export_path) + "/";

					switch (((C64Config) c64.config).screen_mode) {
					case HIRES:
						exportFileName += fileName + ".art";
						break;
					case MULTICOLOR:
						exportFileName += fileName + ".koa";
						break;
					}
					
					final int result = JOptionPane.showConfirmDialog(null, "Export " + exportFileName + "?", "Confirm",
							JOptionPane.YES_NO_OPTION);
					if (result == 0) {
						c64.savePreview(exportFileName);
						
						switch (((C64Config) c64.config).screen_mode) {
						case HIRES:
							hiresExport(exportFileName);
							break;
						case MULTICOLOR:
							lowresExport(exportFileName);
							break;
						}
					}
				} catch (final IOException ex) {
					JOptionPane.showMessageDialog(null, "Error", ex.getMessage(), JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		menuFile.add(miFile);

		final JMenuItem miExecutable = new JMenuItem("Export as executable... ");
		miExecutable.setMnemonic(KeyEvent.VK_E);
		miExecutable.setAccelerator(
				KeyStroke.getKeyStroke(KeyEvent.VK_E, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
		miExecutable.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				try {
					final String exportFileName = Utils.createDirectory(Config.export_path) + "/" + fileName + ".prg";
					c64.savePreview(exportFileName);
					
					final int result = JOptionPane.showConfirmDialog(null, "Export " + exportFileName + "?", "Confirm",
							JOptionPane.YES_NO_OPTION);
					if (result == 0)
						switch (((C64Config) c64.config).screen_mode) {
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
}
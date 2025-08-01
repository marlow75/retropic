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

import pl.dido.image.c64.C64ExtraConfig.EXTRA_MODE;
import pl.dido.image.renderer.AbstractRenderer;
import pl.dido.image.renderer.AbstractRendererRunner;
import pl.dido.image.utils.Config;
import pl.dido.image.utils.Utils;

public class C64ExtraRunner extends AbstractRendererRunner {

	protected C64ExtraRenderer c64Extra;

	public C64ExtraRunner(final AbstractRenderer renderer, final String fileName) {
		super(renderer, fileName);
		c64Extra = (C64ExtraRenderer) renderer;
	}

	private void hiresExportPRG(final String fileName) {
		try {
			final BufferedInputStream in = new BufferedInputStream(Utils.getResourceAsStream("super-hires.prg"), 8192);
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
			final int spare = 1021 - prg_len;
			for (int i = 0; i < spare; i++)
				out.write(0xff);

			// background1
			out.write(c64Extra.backgroundColor);

			// background2
			out.write(c64Extra.backgroundColor);

			// attributes 1
			for (int i = 0; i < 1000; i++)
				out.write(c64Extra.screen1[i] & 0xff);

			// trim to kb
			for (int i = 0; i < 24 + 4096; i++)
				out.write(0xff);

			// bitmap 1
			for (int i = 0; i < 8000; i++)
				out.write(c64Extra.bitmap1[i] & 0xff);

			// trim to kb
			for (int i = 0; i < 192; i++)
				out.write(0xff);

			// bitmap 2
			for (int i = 0; i < 8000; i++)
				out.write(c64Extra.bitmap2[i] & 0xff);

			// trim to kb
			for (int i = 0; i < 192; i++)
				out.write(0xff);

			// attributes 2
			for (int i = 0; i < 1000; i++)
				out.write(c64Extra.screen2[i] & 0xff);

			out.close();
			frame.setTitle(frame.getTitle() + " SAVED");
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	private void mciExportPRG(final String fileName) {
		try {
			final BufferedInputStream in = new BufferedInputStream(Utils.getResourceAsStream("mci.prg"), 8192);
			final BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(new File(fileName)), 8192);

			// MCI File Format
			// $0801 - loader and player
			// $0BFF - background color - 1 byte
			// $0C00 - attributes 1 - 1000 bytes
			// $1000 - attributes 2 - 1000 bytes
			// $2000 - bitmap 1 - 8000 bytes
			// $4000 - bitmap 2 - 8000 bytes
			// $6000 - nibbles - 1000 bytes

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
			final int spare = 1021 - prg_len;
			for (int i = 0; i < spare; i++)
				out.write(0xff);

			// background 1
			out.write(c64Extra.backgroundColor);

			// background 2
			out.write(c64Extra.backgroundColor);

			// attributes 1
			for (int i = 0; i < 1000; i++)
				out.write(c64Extra.screen1[i] & 0xff);

			// fill the gap
			for (int i = 0; i < 24; i++)
				out.write(0xff);

			// attributes 2
			for (int i = 0; i < 1000; i++)
				out.write(c64Extra.screen1[i] & 0xff);

			// fill the gap
			for (int i = 0; i < 24 + 3072; i++)
				out.write(0xff);

			// bitmap 1
			for (int i = 0; i < 8000; i++)
				out.write(c64Extra.bitmap1[i] & 0xff);

			// trim to kb
			for (int i = 0; i < 192; i++)
				out.write(0xff);

			// bitmap 2
			for (int i = 0; i < 8000; i++)
				out.write(c64Extra.bitmap2[i] & 0xff);

			// trim to kb
			for (int i = 0; i < 192; i++)
				out.write(0xff);

			// nibbles
			for (int i = 0; i < 1000; i++)
				out.write(c64Extra.nibbles[i] & 0xff);

			out.close();
			frame.setTitle(frame.getTitle() + " SAVED");
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	private void mciExportTruePaint(final String fileName) {
		try {
			final BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(new File(fileName)), 8192);

			// True Paint File Format
			// $0000 - $03E8 Screen RAM 1
			// $03E9 - Background
			// $0400 - $2340 Bitmap 1
			// $2400 - $4340 Bitmap 2
			// $4400 - $47E8 Screen RAM 2
			// $4800 - $4BE8 Color RAM

			// loading address
			out.write(0x00);
			out.write(0x00);

			// attributes 1
			for (int i = 0; i < 1000; i++)
				out.write(c64Extra.screen1[i] & 0xff);

			out.write(c64Extra.backgroundColor & 0xff);

			// fill the gap
			for (int i = 0; i < 23; i++)
				out.write(0xff);

			// bitmap 1
			for (int i = 0; i < 8000; i++)
				out.write(c64Extra.bitmap1[i] & 0xff);

			// trim to kb
			for (int i = 0; i < 192; i++)
				out.write(0xff);

			// bitmap 2
			for (int i = 0; i < 8000; i++)
				out.write(c64Extra.bitmap2[i] & 0xff);

			// trim to kb
			for (int i = 0; i < 192; i++)
				out.write(0xff);

			// attributes 2
			for (int i = 0; i < 1000; i++)
				out.write(c64Extra.screen2[i] & 0xff);

			// fill the gap
			for (int i = 0; i < 24; i++)
				out.write(0xff);

			// nibbles
			for (int i = 0; i < 1000; i++)
				out.write(c64Extra.nibbles[i] & 0xff);

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

					if (result == 0) {
						switch (((C64ExtraConfig) c64Extra.config).extra_mode) {
						case HIRES_INTERLACED:
							hiresExportPRG(exportFileName);
							break;
						case MULTI_COLOR_INTERLACED:
							mciExportPRG(exportFileName);
							break;
						}
					}
				} catch (final IOException ex) {
					JOptionPane.showMessageDialog(null, "Error", ex.getMessage(), JOptionPane.ERROR_MESSAGE);
				}
			}
		});

		menuFile.add(miExecutable);

		if (((C64ExtraConfig) c64Extra.config).extra_mode == EXTRA_MODE.MULTI_COLOR_INTERLACED) {
			final JMenuItem miTruePaint = new JMenuItem("Export as True Paint MCI ... ");
			miTruePaint.setMnemonic(KeyEvent.VK_T);
			miTruePaint.setAccelerator(
					KeyStroke.getKeyStroke(KeyEvent.VK_T, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
			miTruePaint.addActionListener(new ActionListener() {
				public void actionPerformed(final ActionEvent e) {
					try {
						final String exportFileName = Utils.createDirectory(Config.export_path) + "/" + fileName
								+ ".prg";
						final int result = JOptionPane.showConfirmDialog(null, "Export " + exportFileName + "?",
								"Confirm", JOptionPane.YES_NO_OPTION);

						if (result == 0)
							mciExportTruePaint(exportFileName);

					} catch (final IOException ex) {
						JOptionPane.showMessageDialog(null, "Error", ex.getMessage(), JOptionPane.ERROR_MESSAGE);
					}
				}
			});
			menuFile.add(miTruePaint);
		}

		final JMenuBar menuBar = new JMenuBar();
		menuBar.add(menuFile);

		return menuBar;
	}

	@Override
	protected String getTitle() {
		return "C64 ";
	}
}
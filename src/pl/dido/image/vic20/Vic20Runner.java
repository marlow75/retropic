package pl.dido.image.vic20;

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

public class Vic20Runner extends AbstractRendererRunner {

	protected Vic20Renderer petscii;

	public Vic20Runner(final AbstractRenderer renderer, final String fileName) {
		super(renderer, fileName);
		petscii = (Vic20Renderer) renderer;
	}

	private void petsciiExportPRG(final String fileName) {
		try {
			final BufferedInputStream in = new BufferedInputStream(Utils.getResourceAsStream("vic20.prg"), 8192);
			final BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(new File(fileName)), 8192);

			// loading address BASIC
			out.write(0x01);
			out.write(0x10);

			int data;
			in.read(); // skip loading address
			in.read();

			while ((data = in.read()) != -1)
				out.write(data);

			in.close();

			// first background color
			out.write(((petscii.backgroundColor & 0xf) << 4) + 8);

			int table[] = petscii.screen;
			// bitmap
			for (int i = 0; i < 506; i++)
				out.write(table[i] & 0xff);

			table = petscii.nibble;
			// color nibbles
			for (int i = 0; i < 506; i++)
				out.write(table[i] & 0xf);

			out.close();
			frame.setTitle(frame.getTitle() + " SAVED");
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	private void hiresExportPRG(final String fileName) {
		try {
			final BufferedInputStream in = new BufferedInputStream(Utils.getResourceAsStream("vic20hires.prg"), 8192);
			final BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(new File(fileName)), 8192);

			// loading address BASIC
			out.write(0x01);
			out.write(0x12);

			int data, prgLen = 0;
			in.read(); // skip loading address
			in.read();

			while ((data = in.read()) != -1) {
				out.write(data);
				prgLen++;
			}

			in.close();

			// first background color
			out.write(((petscii.backgroundColor & 0xf) << 4) + 8);

			int table[] = petscii.screen;
			// bitmap
			for (int i = 0; i < 506; i++)
				out.write(table[i] & 0xff);

			table = petscii.nibble;
			// color nibbles
			for (int i = 0; i < 506; i++)
				out.write(table[i] & 0xf);

			for (int i = 0; i < 1535 - (prgLen + 1 + 1012); i++)
				out.write(0x80);

			// charset
			for (int i = 0; i < 2048; i++)
				out.write(petscii.charset[i]);

			out.close();
			frame.setTitle(frame.getTitle() + " SAVED");
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	private void lowresExportPRG(final String fileName) {
		try {
			final BufferedInputStream in = new BufferedInputStream(Utils.getResourceAsStream("vic20lowres.prg"), 8192);
			final BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(new File(fileName)), 8192);

			// loading address BASIC
			out.write(0x01);
			out.write(0x12);

			int data, prgLen = 0;
			in.read(); // skip loading address
			in.read();

			while ((data = in.read()) != -1) {
				out.write(data);
				prgLen++;
			}

			in.close();

			// first background color
			out.write(((petscii.backgroundColor & 0xf) << 4) | (petscii.borderColor & 0xf));
			out.write((petscii.auxiliaryColor & 0xf) * 16);

			int table[] = petscii.screen;
			// bitmap
			for (int i = 0; i < 506; i++)
				out.write(table[i] & 0xff);

			table = petscii.nibble;
			// color nibbles
			for (int i = 0; i < 506; i++)
				out.write(table[i] & 0xf);

			for (int i = 0; i < 1535 - (prgLen + 2 + 1012); i++)
				out.write(0x80);

			// charset
			for (int i = 0; i < 2048; i++)
				out.write(petscii.charset[i]);

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

		switch (((Vic20Config) petscii.config).mode) {
		case PETSCII:
			final JMenuItem miExecutable = new JMenuItem("Export as executable... ");
			miExecutable.setMnemonic(KeyEvent.VK_E);
			miExecutable.setAccelerator(
					KeyStroke.getKeyStroke(KeyEvent.VK_E, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
			miExecutable.addActionListener(new ActionListener() {
				public void actionPerformed(final ActionEvent e) {
					try {
						final String exportFileName = Utils.createDirectory(Config.export_path) + "/" + fileName
								+ ".prg";
						final int result = JOptionPane.showConfirmDialog(null, "Export " + exportFileName + "?",
								"Confirm", JOptionPane.YES_NO_OPTION);

						if (result == 0) {
							petscii.savePreview(exportFileName);
							petsciiExportPRG(exportFileName);
						}
					} catch (final IOException ex) {
						JOptionPane.showMessageDialog(null, "Error", ex.getMessage(), JOptionPane.ERROR_MESSAGE);
					}
				}
			});

			menuFile.add(miExecutable);
			break;
		case HIRES:
			final JMenuItem miHiExecutable = new JMenuItem("Export as hires charset exec... ");
			miHiExecutable.setMnemonic(KeyEvent.VK_E);
			miHiExecutable.setAccelerator(
					KeyStroke.getKeyStroke(KeyEvent.VK_C, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
			miHiExecutable.addActionListener(new ActionListener() {
				public void actionPerformed(final ActionEvent e) {
					try {
						final String exportFileName = Utils.createDirectory(Config.export_path) + "/" + fileName
								+ ".prg";
						final int result = JOptionPane.showConfirmDialog(null, "Export " + exportFileName + "?",
								"Confirm", JOptionPane.YES_NO_OPTION);

						if (result == 0) {
							petscii.savePreview(exportFileName);
							hiresExportPRG(exportFileName);
						}
					} catch (final IOException ex) {
						JOptionPane.showMessageDialog(null, "Error", ex.getMessage(), JOptionPane.ERROR_MESSAGE);
					}
				}
			});

			menuFile.add(miHiExecutable);
			break;
		case LOWRES:
			final JMenuItem miLoExecutable = new JMenuItem("Export as lowres charset exec... ");
			miLoExecutable.setMnemonic(KeyEvent.VK_E);
			miLoExecutable.setAccelerator(
					KeyStroke.getKeyStroke(KeyEvent.VK_C, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
			miLoExecutable.addActionListener(new ActionListener() {
				public void actionPerformed(final ActionEvent e) {
					try {
						final String exportFileName = Utils.createDirectory(Config.export_path) + "/" + fileName
								+ ".prg";
						final int result = JOptionPane.showConfirmDialog(null, "Export " + exportFileName + "?",
								"Confirm", JOptionPane.YES_NO_OPTION);

						if (result == 0) {
							petscii.savePreview(exportFileName);
							lowresExportPRG(exportFileName);
						}
					} catch (final IOException ex) {
						JOptionPane.showMessageDialog(null, "Error", ex.getMessage(), JOptionPane.ERROR_MESSAGE);
					}
				}
			});
			
			menuFile.add(miLoExecutable);
			break;
		}

		final JMenuBar menuBar = new JMenuBar();
		menuBar.add(menuFile);

		return menuBar;
	}

	@Override
	protected String getTitle() {
		return "VIC20 ";
	}
}
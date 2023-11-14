package pl.dido.image.cpc;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
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
import pl.dido.image.utils.ChecksumOutputStream;
import pl.dido.image.utils.Config;
import pl.dido.image.utils.Utils;

public class CPCRunner extends AbstractRendererRunner {
	
	protected CPCRenderer cpc;

	public CPCRunner(final AbstractRenderer renderer, final String fileName) {
		super(renderer, fileName);
		cpc = (CPCRenderer) renderer;
	}

	private void writeAMSDOSFileHeader(final ChecksumOutputStream chk, final String fileName, final String ext,
			final int fileType, final int loadAddress, final int entryAddress, final int length) throws IOException {

		// write file header
		chk.write(0x0); // user number

		// file name
		for (int i = 0; i < fileName.length(); i++)
			chk.write(fileName.charAt(i));

		for (int i = 0; i < 8 - fileName.length(); i++)
			chk.write(0x20);

		for (int i = 0; i < 3; i++)
			chk.write(ext.charAt(i));

		chk.write(0x0);
		chk.write(0x0);
		chk.write(0x0);
		chk.write(0x0);

		chk.write(0x0);
		chk.write(0x0);

		chk.write(fileType & 0xff); // file type
		chk.write(0x0);
		chk.write(0x0);

		chk.write(loadAddress & 0xff); // load address
		chk.write((loadAddress & 0xff00) >> 8);

		chk.write(0x0);

		chk.write(length & 0xff); // file length
		chk.write((length & 0xff00) >> 8);

		chk.write(entryAddress & 0xff); // entry address
		chk.write((entryAddress & 0xff00) >> 8);

		// 36 unused
		for (int i = 0; i < 36; i++)
			chk.write(0x0);

		chk.write(length & 0xff); // file length
		chk.write((length & 0xff00) >> 8);
		chk.write(0x0);

		final int checksum = chk.getChecksum();
		chk.write(checksum & 0xff);
		chk.write((checksum & 0xff00) >> 8);

		// 59 unused
		for (int i = 0; i < 59; i++)
			chk.write(0x0);
	}

	private void exportArtStudio(final String path, String fileName, final int mode) {
		try {
			// SCR file
			if (fileName.length() > 8)
				fileName = fileName.substring(0, 7);

			fileName = fileName.toUpperCase().replaceAll("[_ ]", "-");
			ChecksumOutputStream chk = new ChecksumOutputStream(new FileOutputStream(path + fileName + ".SCR"), 8192);
			writeAMSDOSFileHeader(chk, fileName, "SCR", 2, 0x4000, 0x4000, 0x3fff);

			// bitmap
			for (int i = 0; i < cpc.bitmap.length; i++)
				chk.write(cpc.bitmap[i] & 0xff);

			chk.close();

			// palette file
			chk = new ChecksumOutputStream(new FileOutputStream(new File(path + fileName + ".PAL")), 8192);
			writeAMSDOSFileHeader(chk, fileName, "PAL", 2, 0x8809, 0x8809, 239);

			chk.write(mode); // mode?
			chk.write(0x00); // no color animation
			chk.write(0x00); // no delay time no animation

			// palette
			final int len = cpc.pictureColors.length;
			for (int i = 0; i < 16 / len; i++)
				for (int j = 0; j < len; j++) {
					final int data = cpc.colorMapping[cpc.firmwareIndexes[j]];

					for (int k = 0; k < 12; k++)
						chk.write(data); // 12 same colors
				}

			for (int i = 0; i < 44; i++)
				chk.write(0x0);

			chk.close();
			frame.setTitle(frame.getTitle() + " SAVED");
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	protected JMenuBar getMenuBar() {
		final JMenu menuFile = new JMenu("File");
		menuFile.setMnemonic(KeyEvent.VK_F);

		final JMenuItem miArtStudio = new JMenuItem("Export as picture... ");
		miArtStudio.setMnemonic(KeyEvent.VK_S);
		miArtStudio.setAccelerator(
				KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
		miArtStudio.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				try {
					String path = Utils.createDirectory(Config.export_path) + "/";

					final int result = JOptionPane.showConfirmDialog(null, "Export " + fileName + "?", "Confirm",
							JOptionPane.YES_NO_OPTION);
					if (result == 0)
						switch (((CPCConfig) cpc.config).screen_mode) {
						case MODE1:
							exportArtStudio(path, fileName, 1);
							break;
						case MODE0:
							exportArtStudio(path, fileName, 0);
							break;
						}
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
		return "CPC ";
	}
}
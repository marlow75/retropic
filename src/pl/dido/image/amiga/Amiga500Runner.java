package pl.dido.image.amiga;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.BufferedOutputStream;
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
import pl.dido.image.utils.IFF;
import pl.dido.image.utils.Utils;

public class Amiga500Runner extends AbstractRendererRunner {
	
	protected Amiga500Renderer a500;

	public Amiga500Runner(final AbstractRenderer renderer, final String fileName) {
		super(renderer, fileName);
		a500 = (Amiga500Renderer) renderer;
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

	protected void exportIFF(final String path, String fileName) {
		try {
			if (fileName.length() > 8)
				fileName = fileName.substring(0, 7);

			fileName = fileName.toUpperCase().replaceAll("[_ ]", "-");
			final BufferedOutputStream chk = new BufferedOutputStream(new FileOutputStream(path + fileName + ".iff"),
					8192);

			int videoMode = 0, aspectX = 0, aspectY = 0, planes = 0;
			switch (((Amiga500Config) a500.config).video_mode) {
			case STD_320x256:
				videoMode = 0x0000;
				aspectX = 44;
				aspectY = 44;
				planes = 5;
				break;
			case HAM6_320x256:
				videoMode = 0x0800;
				aspectX = 44;
				aspectY = 44;
				planes = 6;
				break;
			case STD_320x512:
				videoMode = 0x0004;
				aspectX = 22;
				aspectY = 44;
				planes = 5;
				break;
			case HAM6_320x512:
				videoMode = 0x0804;
				aspectX = 22;
				aspectY = 44;
				planes = 6;
				break;
			}

			final boolean compressed = ((AmigaConfig) a500.config).rleCompress;
			chk.write(IFF.getILBMFormat(
					IFF.chunk("BMHD", IFF.getILBMHD(width, height, aspectX, aspectY, planes, compressed)),
					IFF.chunk("CMAP", IFF.getCMAP(a500.pictureColors, a500.colorModel)),
					IFF.chunk("CAMG", IFF.bigEndianDWORD(videoMode)),
					IFF.chunk("BODY", IFF.getBitmap(width, height, a500.bitplanes, compressed))));

			chk.close();

			frame.setTitle(frame.getTitle() + " SAVED");
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected String getTitle() {
		return "A500 " + a500.config.getWidth() + "x" + a500.config.getHeight();
	}
}

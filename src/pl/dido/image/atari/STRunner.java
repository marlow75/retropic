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

import pl.dido.image.renderer.AbstractRenderer;
import pl.dido.image.renderer.AbstractRendererRunner;
import pl.dido.image.utils.Config;
import pl.dido.image.utils.Utils;

public class STRunner extends AbstractRendererRunner {
	
	protected STRenderer st;

	public STRunner(final AbstractRenderer renderer, final String fileName) {
		super(renderer, fileName);
		st = (STRenderer) renderer;
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
			final int len = st.pictureColors.length;
			for (int i = 0; i < len; i++) {

				final int r;
				final int g;
				final int b;

				final int color[] = st.pictureColors[i];
				switch (st.colorModel) {
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
			for (int i = 0; i < st.bitplanes.length; i++) {
				final int word = st.bitplanes[i];
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
}

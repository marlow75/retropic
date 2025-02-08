package pl.dido.image.pc;

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

public class PCRunner extends AbstractRendererRunner {
	
	protected PCRenderer ascii;

	public PCRunner(final AbstractRenderer renderer, final String fileName) {
		super(renderer, fileName);
		ascii = (PCRenderer) renderer;
	}

	private void asciiExportCOM(final String fileName) {
		try {
			final BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(new File(fileName)), 8192);
			final BufferedInputStream in;
			
			int size;
			if (((PCConfig)ascii.config).video_mode == PCConfig.VIDEO_MODE.CGA_TEXT) {
				in = new BufferedInputStream(Utils.getResourceAsStream("cgaviewer.com"), 512);
				size = 2000;
			}
			else {
				in = new BufferedInputStream(Utils.getResourceAsStream("vesaviewer.com"), 512);
				size = 6600;
			}
			
			int data;

			while ((data = in.read()) != -1)
				out.write(data);

			in.close();
			
			final int table[] = ascii.getScreen();
			final int color[] = ascii.getColor();
			
			// bitmap
			for (int i = 0; i < size; i++) {
				out.write(table[i] & 0xff);
				out.write(color[i] & 0xff);
			}	
			
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

		final JMenuItem miExecutable = new JMenuItem("Export as com... ");
		miExecutable.setMnemonic(KeyEvent.VK_E);
		miExecutable.setAccelerator(
				KeyStroke.getKeyStroke(KeyEvent.VK_E, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
		miExecutable.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				try {
					final String exportFileName = Utils.createDirectory(Config.export_path) + "/" + fileName + ".com";
					final int result = JOptionPane.showConfirmDialog(null, "Export " + exportFileName + "?", "Confirm",
							JOptionPane.YES_NO_OPTION);

					if (result == 0)
						asciiExportCOM(exportFileName);
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
		return "PC ASCII ";
	}
}
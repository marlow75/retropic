package pl.dido.image.c128;

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

public class C128Runner extends AbstractRendererRunner {
	
	protected C128Renderer c128;

	public C128Runner(final AbstractRenderer renderer, final String fileName) {
		super(renderer, fileName);
		c128 = (C128Renderer) renderer;
	}

	@Override
	protected String getTitle() {
		return "C128 ";
	}

	private void hiresExportPRG(final String fileName) {
		try {
			final BufferedInputStream in = new BufferedInputStream(Utils.getResourceAsStream("c128.prg"), 8192);
			final BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(new File(fileName)), 8192);

			// loading address BASIC
			out.write(0x01);
			out.write(0x1c);

			int data;
			in.read(); // skip loading address
			in.read();

			while ((data = in.read()) != -1)
				out.write(data);

			in.close();

			// bitmap
			for (int i = 0; i < c128.bitmap.length; i++)
				out.write(c128.bitmap[i] & 0xff);
			
			// attributes
			for (int i = 0; i < c128.attribs.length; i++)
				out.write(c128.attribs[i] & 0xff);

			out.close();
			frame.setTitle(frame.getTitle() + " SAVED");
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected JMenuBar getMenuBar() {
		final JMenu menuFile = new JMenu("File");

		final JMenuItem miExecutable = new JMenuItem("Export as executable... ");
		miExecutable.setMnemonic(KeyEvent.VK_E);
		miExecutable.setAccelerator(
				KeyStroke.getKeyStroke(KeyEvent.VK_E, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
		miExecutable.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				try {
					final String exportFileName = Utils.createDirectory(Config.export_path) + "/" + fileName + ".prg";
					c128.savePreview(exportFileName);
					
					final int result = JOptionPane.showConfirmDialog(null, "Export " + exportFileName + "?", "Confirm",
							JOptionPane.YES_NO_OPTION);
					if (result == 0)
						hiresExportPRG(exportFileName);
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
}

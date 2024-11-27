package pl.dido.image.Plus4;

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

public class Plus4ExtraRunner extends AbstractRendererRunner {

	protected Plus4ExtraRenderer plus4Extra;

	public Plus4ExtraRunner(final AbstractRenderer renderer, final String fileName) {
		super(renderer, fileName);
		plus4Extra = (Plus4ExtraRenderer) renderer;
	}

	private void hiresExportPRG(final String fileName) {
		try {
			final BufferedInputStream in = new BufferedInputStream(Utils.getResourceAsStream("super-hiresplus4.prg"), 8192);
			final BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(new File(fileName)), 8192);

			// loading address BASIC
			out.write(0x01);
			out.write(0x10);

			int data, prg_len = 0;
			in.read(); // skip loading address
			in.read();

			while ((data = in.read()) != -1) {
				out.write(data);
				prg_len += 1;
			}

			in.close();

			// spare bytes
			final int spare = 0x7ff - prg_len;
			for (int i = 0; i < spare; i++)
				out.write(0x80);
			
			// luma $1800
			for (int i = 0; i < plus4Extra.nibble1.length; i++)
				out.write(plus4Extra.nibble1[i] & 0xff);
			
			for (int i = 0; i < 24; i++)
				out.write(0x80);
			
			// color $1c00
			for (int i = 0; i < plus4Extra.screen1.length; i++)
				out.write(plus4Extra.screen1[i] & 0xff);

			for (int i = 0; i < 24; i++)
				out.write(0x80);
			
			// bitmap $2000
			for (int i = 0; i < plus4Extra.bitmap1.length; i++)
				out.write(plus4Extra.bitmap1[i] & 0xff);
			
			for (int i = 0; i < 192; i++)
				out.write(0x80);
			
			// bitmap $4000
			for (int i = 0; i < plus4Extra.bitmap2.length; i++)
				out.write(plus4Extra.bitmap2[i] & 0xff);
			
			for (int i = 0; i < 192; i++)
				out.write(0x80);
			
			// luma $6000
			for (int i = 0; i < plus4Extra.nibble2.length; i++)
				out.write(plus4Extra.nibble2[i] & 0xff);
			
			for (int i = 0; i < 24; i++)
				out.write(0x80);
			
			// color $6400
			for (int i = 0; i < plus4Extra.screen2.length; i++)
				out.write(plus4Extra.screen2[i] & 0xff);

			for (int i = 0; i < 24; i++)
				out.write(0x80);

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
						switch (((Plus4ExtraConfig) plus4Extra.config).extra_mode) {
						case HIRES_INTERLACED:
							hiresExportPRG(exportFileName);
							break;
						default:
							break;
						}
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
		return "Plus4 ";
	}
}
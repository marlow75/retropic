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

public class Plus4Runner extends AbstractRendererRunner {
	
	protected Plus4Renderer plus4;

	public Plus4Runner(final AbstractRenderer renderer, final String fileName) {
		super(renderer, fileName);
		plus4 = (Plus4Renderer) renderer;
	}
	
	private void hiresExportPRG(final String fileName) {
		try {
			final BufferedInputStream in = new BufferedInputStream(Utils.getResourceAsStream("highplus4.prg"), 8192);
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
			for (int i = 0; i < plus4.nibble.length; i++)
				out.write(plus4.nibble[i] & 0xff);
			
			for (int i = 0; i < 24; i++)
				out.write(0x80);
			
			// color $1c00
			for (int i = 0; i < plus4.screen.length; i++)
				out.write(plus4.screen[i] & 0xff);

			for (int i = 0; i < 24; i++)
				out.write(0x80);
			
			// bitmap $2000
			for (int i = 0; i < plus4.bitmap.length; i++)
				out.write(plus4.bitmap[i] & 0xff);

			out.close();
			frame.setTitle(frame.getTitle() + " SAVED");
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	private void lowresExportPRG(final String fileName) {
		try {
			final BufferedInputStream in = new BufferedInputStream(Utils.getResourceAsStream("lowplus4.prg"), 8192);
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
			final int spare = 0x7ff - prg_len - 2;
			for (int i = 0; i < spare; i++)
				out.write(0x80);

			out.write(plus4.backgroundColor1 & 0xff);
			out.write(plus4.backgroundColor2 & 0xff);
			
			// luma $1800
			for (int i = 0; i < plus4.nibble.length; i++)
				out.write(plus4.nibble[i] & 0xff);
			
			for (int i = 0; i < 24; i++)
				out.write(0x80);
			
			// color $1c00
			for (int i = 0; i < plus4.screen.length; i++)
				out.write(plus4.screen[i] & 0xff);

			for (int i = 0; i < 24; i++)
				out.write(0x80);
			
			// bitmap $2000
			for (int i = 0; i < plus4.bitmap.length; i++)
				out.write(plus4.bitmap[i] & 0xff);

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
					plus4.savePreview(exportFileName);
					
					final int result = JOptionPane.showConfirmDialog(null, "Export " + exportFileName + "?", "Confirm",
							JOptionPane.YES_NO_OPTION);
					if (result == 0)
						switch (((Plus4Config) plus4.config).screen_mode) {
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
		return "Commodore Plus4 ";
	}
}
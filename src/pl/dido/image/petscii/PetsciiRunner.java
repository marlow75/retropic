package pl.dido.image.petscii;

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

public class PetsciiRunner extends AbstractRendererRunner {
	
	protected PetsciiRenderer petscii;

	public PetsciiRunner(final AbstractRenderer renderer, final String fileName) {
		super(renderer, fileName);
		petscii = (PetsciiRenderer) renderer;
	}

	private void petsciiExportPRG(final String fileName) {
		try {
			final BufferedInputStream in = new BufferedInputStream(Utils.getResourceAsStream("petscii.prg"), 8192);
			final BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(new File(fileName)), 8192);

			// loading address BASIC
			out.write(0x01);
			out.write(0x08);

			int data;
			in.read(); // skip loading address
			in.read();

			while ((data = in.read()) != -1)
				out.write(data);

			in.close();

			// first background color
			out.write(petscii.getBackgroundColor() & 0xf);

			int table[] = petscii.getScreen();
			// bitmap
			for (int i = 0; i < 1000; i++)
				out.write(table[i] & 0xff);

			table = petscii.getNibble();
			// color nibbles
			for (int i = 0; i < 1000; i++)
				out.write(table[i] & 0xf);

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

					if (result == 0)
						petsciiExportPRG(exportFileName);
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
		return "PETSCII ";
	}
}

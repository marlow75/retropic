package pl.dido.image;

import java.io.IOException;

import javax.swing.JPanel;
import javax.swing.JTextPane;

import pl.dido.image.utils.Utils;

public class AboutGui {
	
	public static JPanel aboutTab() {
		final JPanel panelAbout = new JPanel();
		panelAbout.setLayout(null);

		final JTextPane aboutText = new JTextPane();
		aboutText.setEditable(false);
		
		try {
			aboutText.setPage(Utils.getResourceAsURL("about.htm"));
		} catch (final IOException e) {
			// nothing
		}
		
		aboutText.setBounds(20, 23, 460, 250);
		panelAbout.add(aboutText);
		
		return panelAbout;
	}
}

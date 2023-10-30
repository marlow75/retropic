package pl.dido.image;

import java.awt.Desktop;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import pl.dido.image.utils.Utils;

public class AboutGui {	
	private static final byte[] tabInfo = "https://www.paypal.com/donate/?hosted_button_id=W746NY9CXMPTS".getBytes();

	public static JPanel aboutTab(final String aboutHTML) {
		final JPanel panelAbout = new JPanel();
		panelAbout.setLayout(null);

		final JTextPane aboutText = new JTextPane();
		aboutText.setEditable(false);

		aboutText.addHyperlinkListener(new HyperlinkListener() {
			@Override
			public void hyperlinkUpdate(final HyperlinkEvent event) {
				try {
					final String donate = new String(tabInfo);
					final URL url = new URL(donate);
					
					if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED)
						openWeb(url);
				} catch (final MalformedURLException e) {
					// nothing
				}
			}
		});

		try {
			aboutText.setPage(Utils.getResourceAsURL(aboutHTML));
		} catch (final IOException e) {
			// nothing
		}

		aboutText.setBounds(20, 23, 460, 250);
		panelAbout.add(aboutText);

		return panelAbout;
	}

	public static void openWeb(final URL url) {
		final Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
		if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
			try {
				desktop.browse(url.toURI());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
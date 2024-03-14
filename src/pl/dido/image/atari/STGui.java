package pl.dido.image.atari;

import java.awt.Canvas;

import javax.swing.JPanel;

import pl.dido.image.GuiUtils;
import pl.dido.image.utils.ImageCanvas;

public class STGui {

	public static JPanel stTab(final STConfig config) {
		final JPanel panelST = new JPanel();
		panelST.setLayout(null);

		GuiUtils.addDASControls(panelST, config);

		final Canvas stLogo = new ImageCanvas("st.png");
		stLogo.setBounds(310, 15, 200, 85);
		panelST.add(stLogo);

		GuiUtils.addContrastControls(panelST, config);
		GuiUtils.addColorControls(panelST, config);
		
		return panelST;
	}
}
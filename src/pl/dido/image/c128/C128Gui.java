package pl.dido.image.c128;

import java.awt.Canvas;

import javax.swing.JPanel;

import pl.dido.image.GuiUtils;
import pl.dido.image.utils.ImageCanvas;

public class C128Gui {

	public static JPanel c128Tab(final C128Config config) {
		final JPanel panelC128 = new JPanel();
		panelC128.setLayout(null);
		GuiUtils.addDASControls(panelC128, config);

		final Canvas c128Logo = new ImageCanvas("c64.png");
		c128Logo.setBounds(381, 7, 100, 96);
		panelC128.add(c128Logo);
		
		GuiUtils.addContrastControls(panelC128, config);
		GuiUtils.addColorControls(panelC128, config);
		
		GuiUtils.addFiltersControls(panelC128, config);
		return panelC128;
	}
}

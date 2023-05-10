package pl.dido.image.atari;

import java.awt.Canvas;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import pl.dido.image.GuiUtils;
import pl.dido.image.utils.ImageCanvas;

public class STGui {

	public static JPanel stTab(final STConfig config) {
		final JPanel panelST = new JPanel();
		panelST.setLayout(null);
		
		GuiUtils.addDitheringControls(panelST, config);	

		final Canvas stLogo = new ImageCanvas("st.png");
		stLogo.setBounds(310, 15, 200, 85);
		panelST.add(stLogo);
		
		final JCheckBox chkReplaceBox = new JCheckBox("adjust contrast");
		chkReplaceBox.setToolTipText("Gives more/less contrast picture");
		chkReplaceBox.setFont(GuiUtils.std);
		chkReplaceBox.setBounds(46, 100, 169, 23);
		chkReplaceBox.setSelected(config.replace_colors);
		chkReplaceBox.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				config.replace_colors = !config.replace_colors; 		
			}});

		panelST.add(chkReplaceBox);		
		GuiUtils.addColorControls(panelST, config);
						
		return panelST;
	}
}
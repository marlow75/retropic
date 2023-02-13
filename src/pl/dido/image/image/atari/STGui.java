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
		final JPanel stPanel = new JPanel();
		stPanel.setLayout(null);

		final JCheckBox chckbxVividCheckBox = new JCheckBox("floyds dithering");
		chckbxVividCheckBox.setToolTipText("Enables Floyds-Steinberg predithering");
		chckbxVividCheckBox.setFont(GuiUtils.std);
		chckbxVividCheckBox.setBounds(20, 23, 171, 44);
		chckbxVividCheckBox.setSelected(config.dithering);
		
		chckbxVividCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				config.dithering = !config.dithering;
			}});
		
		stPanel.add(chckbxVividCheckBox);

		final Canvas stLogo = new ImageCanvas("st.png");
		stLogo.setBounds(281, 17, 200, 87);
		stPanel.add(stLogo);
		
		final JCheckBox chkReplaceBox = new JCheckBox("adjust contrast");
		chkReplaceBox.setToolTipText("Gives more/less contrast picture");
		chkReplaceBox.setFont(GuiUtils.std);
		chkReplaceBox.setBounds(20, 76, 169, 23);
		chkReplaceBox.setSelected(config.replace_colors);
		chkReplaceBox.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				config.replace_colors = !config.replace_colors; 		
			}});

		stPanel.add(chkReplaceBox);		
		GuiUtils.addColorControls(stPanel, config);
						
		return stPanel;
	}
}
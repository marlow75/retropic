package pl.dido.image.zx;

import java.awt.Canvas;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import pl.dido.image.GuiUtils;
import pl.dido.image.utils.ImageCanvas;

public class ZXGui {
	
	public static JPanel zxTab(final ZXConfig config) {
		final JPanel panelZX = new JPanel();
		panelZX.setLayout(null);

		final JCheckBox chckbxVividCheckBox = new JCheckBox("try apple dithering");
		chckbxVividCheckBox.setToolTipText("Enables picture predithering");
		chckbxVividCheckBox.setFont(GuiUtils.std);
		chckbxVividCheckBox.setBounds(20, 23, 171, 44);
		chckbxVividCheckBox.setSelected(config.dithering);
		
		chckbxVividCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				config.dithering = !config.dithering;
			}});
		
		panelZX.add(chckbxVividCheckBox);
		
		final Canvas zxLogo = new ImageCanvas("sinclair.png");
		zxLogo.setBounds(230, 7, 252, 65);
		panelZX.add(zxLogo);
		
		GuiUtils.addColorControls(panelZX, config);		
		
		return panelZX;
	}
}
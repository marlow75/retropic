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

		final JCheckBox chckbxVividCheckBox = new JCheckBox("apple dithering");
		chckbxVividCheckBox.setToolTipText("Enables picture atkinson predithering");
		chckbxVividCheckBox.setFont(GuiUtils.std);
		chckbxVividCheckBox.setBounds(20, 23, 100, 20);
		chckbxVividCheckBox.setSelected(config.dithering);
		
		chckbxVividCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				config.dithering = !config.dithering;
			}});
		
		panelZX.add(chckbxVividCheckBox);
		
		final JCheckBox chckbxAspectCheckBox = new JCheckBox("aspect");
		chckbxAspectCheckBox.setToolTipText("Preserve orginal image aspect ratio");
		chckbxAspectCheckBox.setFont(GuiUtils.std);
		chckbxAspectCheckBox.setBounds(20, 50, 100, 20);
		chckbxAspectCheckBox.setSelected(config.preserveAspect);
		
		chckbxAspectCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				config.preserveAspect = !config.preserveAspect;
			}});
		
		panelZX.add(chckbxAspectCheckBox);
		
		final JCheckBox chckbxRasterCheckBox = new JCheckBox("scan");
		chckbxRasterCheckBox.setToolTipText("Scan line simulation");
		chckbxRasterCheckBox.setFont(GuiUtils.std);
		chckbxRasterCheckBox.setBounds(150, 23, 70, 20);
		chckbxRasterCheckBox.setSelected(config.scanline);
		
		chckbxRasterCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				config.scanline = !config.scanline;
			}});
		
		panelZX.add(chckbxRasterCheckBox);
		
		final Canvas zxLogo = new ImageCanvas("sinclair.png");
		zxLogo.setBounds(230, 7, 252, 65);
		panelZX.add(zxLogo);
		
		GuiUtils.addContrastControls(panelZX, config);
		GuiUtils.addColorControls(panelZX, config);		
		
		return panelZX;
	}
}
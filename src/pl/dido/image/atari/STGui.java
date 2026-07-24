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

		GuiUtils.addDASControls(panelST, config);

		final Canvas stLogo = new ImageCanvas("st.png");
		stLogo.setBounds(310, 15, 200, 85);
		panelST.add(stLogo);
		
		final JCheckBox chckbxQuantumCheckBox = new JCheckBox("quantum quantizer");
		
		chckbxQuantumCheckBox.setToolTipText("Enables quantum like color quantizer disables SOM");
		chckbxQuantumCheckBox.setFont(GuiUtils.std);
		chckbxQuantumCheckBox.setBounds(20, 110, 250, 20);
		chckbxQuantumCheckBox.setSelected(config.fermionic_quantizer);
		
		chckbxQuantumCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				config.fermionic_quantizer = !config.fermionic_quantizer;
			}});
		
		panelST.add(chckbxQuantumCheckBox);
		
		GuiUtils.addContrastControls(panelST, config);
		GuiUtils.addColorControls(panelST, config);
		GuiUtils.addFiltersControls(panelST, config);
		
		return panelST;
	}
}
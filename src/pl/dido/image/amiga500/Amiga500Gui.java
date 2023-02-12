package pl.dido.image.amiga500;

import java.awt.Canvas;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import pl.dido.image.GuiUtils;
import pl.dido.image.utils.ImageCanvas;

public class Amiga500Gui {

	public static JPanel amigaTab(final Amiga500Config config) {
		final JPanel panelAmiga = new JPanel();
		panelAmiga.setLayout(null);
		GuiUtils.addDitheringControls(panelAmiga, config);		

		final Canvas amigaLogo = new ImageCanvas("amiga.png");
		amigaLogo.setBounds(340, 17, 200, 87);
		panelAmiga.add(amigaLogo);
		
		final JLabel lblConvertLabel = new JLabel("Converter mode:");
		lblConvertLabel.setFont(GuiUtils.bold);
		lblConvertLabel.setBounds(20, 112, 250, 23);
		panelAmiga.add(lblConvertLabel);

		final JRadioButton rdbtnStdButton = new JRadioButton("PAL 320x256 - 32 colors");
		rdbtnStdButton.setToolTipText("Standard PAL, 32 colors from 4096 palette");
		rdbtnStdButton.setFont(GuiUtils.std);
		rdbtnStdButton.setBounds(46, 130, 250, 57);
		rdbtnStdButton.setSelected(config.color_mode == Amiga500Config.COLOR_MODE.STD32);
		rdbtnStdButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				config.color_mode = Amiga500Config.COLOR_MODE.STD32;
			}});

		panelAmiga.add(rdbtnStdButton);
		
		final JRadioButton rdbtnHAMButton = new JRadioButton("PAL 320x256 - Hold And Modify");
		rdbtnHAMButton.setToolTipText("Standard PAL HAM encoding");
		rdbtnHAMButton.setFont(GuiUtils.std);
		rdbtnHAMButton.setBounds(46, 193, 400, 23);
		rdbtnHAMButton.setSelected(config.color_mode == Amiga500Config.COLOR_MODE.HAM6);
		rdbtnHAMButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				config.color_mode = Amiga500Config.COLOR_MODE.HAM6;
			}});

		panelAmiga.add(rdbtnHAMButton);
		
		final ButtonGroup groupColor = new ButtonGroup();
		groupColor.add(rdbtnStdButton);
		groupColor.add(rdbtnHAMButton);				
				
		GuiUtils.addColorControls(panelAmiga, config);
						
		return panelAmiga;
	}
}
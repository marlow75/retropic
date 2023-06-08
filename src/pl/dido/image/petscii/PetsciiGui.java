package pl.dido.image.petscii;

import java.awt.Canvas;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import pl.dido.image.GuiUtils;
import pl.dido.image.petscii.PetsciiConfig.NETWORK;
import pl.dido.image.utils.ImageCanvas;

public class PetsciiGui {

	public static JPanel petsciiTab(final PetsciiConfig config) {
		final JPanel petsciiC64 = new JPanel();
		petsciiC64.setLayout(null);
		GuiUtils.addDitheringControls(petsciiC64, config);

		final JLabel lblConvertLabel = new JLabel("Converter mode:");
		lblConvertLabel.setFont(GuiUtils.bold);
		lblConvertLabel.setBounds(20, 86, 169, 14);
		petsciiC64.add(lblConvertLabel);

		final JRadioButton rdbtnL1Button = new JRadioButton("One hidden layer, sigmoid activation");
		rdbtnL1Button.setToolTipText("Simple and fast network architecture");
		rdbtnL1Button.setFont(GuiUtils.std);
		rdbtnL1Button.setBounds(46, 112, 331, 23);
		rdbtnL1Button.setSelected(config.network == NETWORK.L1);
		rdbtnL1Button.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				config.network = NETWORK.L1;
			}
		});

		petsciiC64.add(rdbtnL1Button);

		final JRadioButton rdbtnL2Button = new JRadioButton("Two hidden layers, sigmoid activation");
		rdbtnL2Button.setToolTipText("Robust but a kind of slow network architecture");
		rdbtnL2Button.setFont(GuiUtils.std);
		rdbtnL2Button.setBounds(46, 153, 331, 23);
		rdbtnL2Button.setSelected(config.network == NETWORK.L2);
		rdbtnL2Button.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				config.network = NETWORK.L2;
			}
		});

		petsciiC64.add(rdbtnL2Button);

		final ButtonGroup groupResolution = new ButtonGroup();
		groupResolution.add(rdbtnL1Button);
		groupResolution.add(rdbtnL2Button);

		final Canvas c64Logo = new ImageCanvas("c64.png");
		c64Logo.setBounds(381, 7, 100, 96);
		
		petsciiC64.add(c64Logo);
		
		GuiUtils.addContrastControls(petsciiC64, config);
		GuiUtils.addColorControls(petsciiC64, config);

		return petsciiC64;
	}
}
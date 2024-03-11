package pl.dido.image.c64;

import java.awt.Canvas;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import pl.dido.image.GuiUtils;
import pl.dido.image.c64.C64ExtraConfig.EXTRA_MODE;
import pl.dido.image.utils.ImageCanvas;

public class C64ExtraGui {

	public static JPanel c64Extra(final C64ExtraConfig config) {
		final JPanel panelC64Extra = new JPanel();
		panelC64Extra.setLayout(null);
		GuiUtils.addDitheringControls(panelC64Extra, config);

		final JLabel lblConvertLabel = new JLabel("Converter mode:");
		lblConvertLabel.setFont(GuiUtils.bold);
		lblConvertLabel.setBounds(20, 66, 169, 14);
		panelC64Extra.add(lblConvertLabel);

		final JRadioButton rdbtnHiresButton = new JRadioButton("Hires interlaced");
		rdbtnHiresButton.setToolTipText(
				"High resolution mode. Three colors in 8x8 block");
		rdbtnHiresButton.setFont(GuiUtils.std);
		rdbtnHiresButton.setBounds(46, 82, 150, 23);
		rdbtnHiresButton.setSelected(config.extraMode == EXTRA_MODE.HIRES_INTERLACED);
		rdbtnHiresButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				config.extraMode = EXTRA_MODE.HIRES_INTERLACED;
			}});

		panelC64Extra.add(rdbtnHiresButton);
		
		final JRadioButton rdbtnMCIButton = new JRadioButton("Multicolor interlaced");
		rdbtnMCIButton.setToolTipText(
				"High resolution mode. 16 colors in 8x8 block");
		rdbtnMCIButton.setFont(GuiUtils.std);
		rdbtnMCIButton.setBounds(200, 82, 150, 23);
		rdbtnMCIButton.setSelected(config.extraMode == EXTRA_MODE.MULTI_COLOR_INTERLACED);
		rdbtnMCIButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				config.extraMode = EXTRA_MODE.MULTI_COLOR_INTERLACED;
			}});

		panelC64Extra.add(rdbtnMCIButton);
		
		final ButtonGroup groupMode = new ButtonGroup();
		groupMode.add(rdbtnHiresButton);
		groupMode.add(rdbtnMCIButton);
		
		final Canvas c64Logo = new ImageCanvas("c64Extra.png");
		c64Logo.setBounds(381, 7, 100, 96);
		panelC64Extra.add(c64Logo);
		
		final JLabel sizeLabel = new JLabel("luma threshold");
		sizeLabel.setFont(GuiUtils.bold);
		sizeLabel.setBounds(46, 120, 120, 20);
		panelC64Extra.add(sizeLabel);

		final JSlider sldLuma = new JSlider(JSlider.HORIZONTAL, 1, 32, config.lumaThreshold);
		sldLuma.setBounds(41, 140, 220, 35);
		sldLuma.setFont(GuiUtils.std);
		sldLuma.addChangeListener(new ChangeListener() {
			public void stateChanged(final ChangeEvent e) {
				final JSlider source = (JSlider) e.getSource();

				if (!source.getValueIsAdjusting())
					config.lumaThreshold = source.getValue();
			}
		});

		sldLuma.setMajorTickSpacing(5);
		sldLuma.setPaintLabels(true);
		panelC64Extra.add(sldLuma);
				
		GuiUtils.addContrastControls(panelC64Extra, config);
		GuiUtils.addColorControls(panelC64Extra, config);

		return panelC64Extra;
	}
}
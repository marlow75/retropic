package pl.dido.image.Plus4;

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
import pl.dido.image.c64.C64ExtraConfig.RGB_APPROXIMATION;
import pl.dido.image.utils.ImageCanvas;

public class Plus4ExtraGui {

	public static JPanel plus4ExtraTab(final Plus4ExtraConfig config) {
		final JPanel panelPlus4Extra = new JPanel();
		panelPlus4Extra.setLayout(null);
		GuiUtils.addDASControls(panelPlus4Extra, config);
		
		final JRadioButton rdbtnLinearButton = new JRadioButton("linear");
		final JRadioButton rdbtnCubeButton = new JRadioButton("cube");

		final JLabel lblConvertLabel = new JLabel("Converter mode:");
		lblConvertLabel.setFont(GuiUtils.bold);
		lblConvertLabel.setBounds(20, 70, 169, 14);
		panelPlus4Extra.add(lblConvertLabel);

		final JRadioButton rdbtnHiresButton = new JRadioButton("Hires interlaced");
		rdbtnHiresButton.setToolTipText(
				"High resolution mode. 3 colors in 8x8 block");
		rdbtnHiresButton.setFont(GuiUtils.std);
		rdbtnHiresButton.setBounds(46, 85, 150, 23);
		rdbtnHiresButton.setSelected(config.extra_mode == EXTRA_MODE.HIRES_INTERLACED);
		rdbtnHiresButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				config.extra_mode = EXTRA_MODE.HIRES_INTERLACED;
				rdbtnLinearButton.setEnabled(true);
				rdbtnCubeButton.setEnabled(true);
			}});

		panelPlus4Extra.add(rdbtnHiresButton);
		
		final ButtonGroup groupMode = new ButtonGroup();
		groupMode.add(rdbtnHiresButton);
		
		final Canvas plus4ExtraLogo = new ImageCanvas("plus4.png");
		plus4ExtraLogo.setBounds(381, 7, 100, 96);
		panelPlus4Extra.add(plus4ExtraLogo);
		
		final JLabel thresholdLabel = new JLabel("luma threshold");
		thresholdLabel.setFont(GuiUtils.bold);
		thresholdLabel.setBounds(46, 120, 120, 20);
		panelPlus4Extra.add(thresholdLabel);

		final JSlider sldLuma = new JSlider(JSlider.HORIZONTAL, 1, 32, config.luma_threshold);
		sldLuma.setBounds(41, 140, 150, 35);
		sldLuma.setFont(GuiUtils.std);
		sldLuma.addChangeListener(new ChangeListener() {
			public void stateChanged(final ChangeEvent e) {
				final JSlider source = (JSlider) e.getSource();

				if (!source.getValueIsAdjusting())
					config.luma_threshold = source.getValue();
			}
		});
		
		sldLuma.setMajorTickSpacing(5);
		sldLuma.setPaintLabels(true);
		panelPlus4Extra.add(sldLuma);
		
		final JLabel approxLabel = new JLabel("color approximation");
		approxLabel.setFont(GuiUtils.bold);
		approxLabel.setBounds(215, 120, 120, 20);
		panelPlus4Extra.add(approxLabel);
		
		rdbtnLinearButton.setToolTipText(
				"Linear color approximation. Most distant colors");
		rdbtnLinearButton.setFont(GuiUtils.std);
		rdbtnLinearButton.setBounds(210, 140, 60, 23);
		rdbtnLinearButton.setSelected(config.rgb_approximation == RGB_APPROXIMATION.LINEAR);
		rdbtnLinearButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				config.rgb_approximation = RGB_APPROXIMATION.LINEAR;
			}});

		panelPlus4Extra.add(rdbtnLinearButton);
		
		rdbtnCubeButton.setToolTipText(
				"Cube color approximation. Calculated within RGB cube");
		rdbtnCubeButton.setFont(GuiUtils.std);
		rdbtnCubeButton.setBounds(290, 140, 60, 23);
		rdbtnCubeButton.setSelected(config.rgb_approximation == RGB_APPROXIMATION.CUBE);
		rdbtnCubeButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				config.rgb_approximation = RGB_APPROXIMATION.CUBE;
			}});

		panelPlus4Extra.add(rdbtnCubeButton);
		
		final ButtonGroup groupApprox = new ButtonGroup();
		groupApprox.add(rdbtnLinearButton);
		groupApprox.add(rdbtnCubeButton);
				
		GuiUtils.addContrastControls(panelPlus4Extra, config);
		GuiUtils.addColorControls(panelPlus4Extra, config);

		return panelPlus4Extra;
	}
}
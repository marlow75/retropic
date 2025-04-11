package pl.dido.image.plus4;

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
		final JSlider sldFlickering = new JSlider(JSlider.HORIZONTAL, 0, 3, (int) (config.flickering_factor * 1.3f));
		sldFlickering.setEnabled(config.extra_mode == EXTRA_MODE.MULTI_COLOR_INTERLACED);

		final JLabel lblConvertLabel = new JLabel("Converter mode:");
		lblConvertLabel.setFont(GuiUtils.bold);
		lblConvertLabel.setBounds(20, 70, 169, 14);
		panelPlus4Extra.add(lblConvertLabel);

		final JRadioButton rdbtnHiresButton = new JRadioButton("Hires Int");
		rdbtnHiresButton.setToolTipText(
				"High resolution interlaced. 3 colors in 8x8 block");
		rdbtnHiresButton.setFont(GuiUtils.std);
		rdbtnHiresButton.setBounds(46, 85, 80, 23);
		rdbtnHiresButton.setSelected(config.extra_mode == EXTRA_MODE.HIRES_INTERLACED);
		rdbtnHiresButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				config.extra_mode = EXTRA_MODE.HIRES_INTERLACED;
				
				rdbtnLinearButton.setEnabled(true);
				rdbtnCubeButton.setEnabled(true);
				sldFlickering.setEnabled(false);
			}});

		panelPlus4Extra.add(rdbtnHiresButton);
		
		final JRadioButton rdbtnMCIButton = new JRadioButton("MultiColor Int");
		rdbtnMCIButton.setToolTipText(
				"High resolution interlaced. 8 colors in 8x8 block");
		rdbtnMCIButton.setFont(GuiUtils.std);
		rdbtnMCIButton.setBounds(135, 85, 100, 23);
		rdbtnMCIButton.setSelected(config.extra_mode == EXTRA_MODE.MULTI_COLOR_INTERLACED);
		rdbtnMCIButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				config.extra_mode = EXTRA_MODE.MULTI_COLOR_INTERLACED;
				
				rdbtnLinearButton.setEnabled(false);
				rdbtnCubeButton.setEnabled(false);
				sldFlickering.setEnabled(true);
			}});

		panelPlus4Extra.add(rdbtnMCIButton);
		
		final ButtonGroup groupMode = new ButtonGroup();
		groupMode.add(rdbtnHiresButton);
		groupMode.add(rdbtnMCIButton);
		
		final Canvas plus4ExtraLogo = new ImageCanvas("plus4.png");
		plus4ExtraLogo.setBounds(381, 7, 100, 96);
		panelPlus4Extra.add(plus4ExtraLogo);

		final JLabel approxLabel = new JLabel("color approximation");
		approxLabel.setFont(GuiUtils.bold);
		approxLabel.setBounds(25, 120, 120, 20);
		panelPlus4Extra.add(approxLabel);
		
		rdbtnLinearButton.setToolTipText(
				"Linear color approximation. Most distant colors");
		rdbtnLinearButton.setFont(GuiUtils.std);
		rdbtnLinearButton.setBounds(46, 140, 60, 23);
		rdbtnLinearButton.setSelected(config.rgb_approximation == RGB_APPROXIMATION.LINEAR);
		rdbtnLinearButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				config.rgb_approximation = RGB_APPROXIMATION.LINEAR;
			}});

		panelPlus4Extra.add(rdbtnLinearButton);
		
		rdbtnCubeButton.setToolTipText(
				"Cube color approximation. Calculated within RGB cube");
		rdbtnCubeButton.setFont(GuiUtils.std);
		rdbtnCubeButton.setBounds(106, 140, 50, 23);
		rdbtnCubeButton.setSelected(config.rgb_approximation == RGB_APPROXIMATION.CUBE);
		rdbtnCubeButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				config.rgb_approximation = RGB_APPROXIMATION.CUBE;
			}});

		panelPlus4Extra.add(rdbtnCubeButton);
		
		final ButtonGroup groupApprox = new ButtonGroup();
		groupApprox.add(rdbtnLinearButton);
		groupApprox.add(rdbtnCubeButton);

		
		final JLabel thresholdLabel = new JLabel("luma threshold");
		thresholdLabel.setFont(GuiUtils.bold);
		thresholdLabel.setBounds(165, 120, 120, 20);
		panelPlus4Extra.add(thresholdLabel);

		final JSlider sldLuma = new JSlider(JSlider.HORIZONTAL, 1, 32, config.luma_threshold);
		sldLuma.setBounds(160, 140, 100, 35);
		sldLuma.setFont(GuiUtils.std);
		sldLuma.addChangeListener(new ChangeListener() {
			public void stateChanged(final ChangeEvent e) {
				final JSlider source = (JSlider) e.getSource();

				if (!source.getValueIsAdjusting())
					config.luma_threshold = source.getValue();
			}
		});
		
		sldLuma.setMajorTickSpacing(10);
		sldLuma.setPaintLabels(true);
		panelPlus4Extra.add(sldLuma);
				
		final JLabel flickeringLabel = new JLabel("flickering");
		flickeringLabel.setFont(GuiUtils.bold);
		flickeringLabel.setBounds(285, 120, 100, 20);
		panelPlus4Extra.add(flickeringLabel);

		sldFlickering.setBounds(280, 140, 100, 35);
		sldFlickering.setFont(GuiUtils.std);
		sldFlickering.addChangeListener(new ChangeListener() {
			public void stateChanged(final ChangeEvent e) {
				final JSlider source = (JSlider) e.getSource();

				if (!source.getValueIsAdjusting())
					config.flickering_factor = source.getValue() * 1.3f;
			}
		});
		
		sldFlickering.setMajorTickSpacing(1);
		sldFlickering.setPaintLabels(true);
		panelPlus4Extra.add(sldFlickering);
				
		GuiUtils.addContrastControls(panelPlus4Extra, config);
		GuiUtils.addColorControls(panelPlus4Extra, config);
		GuiUtils.addFiltersControls(panelPlus4Extra, config);

		return panelPlus4Extra;
	}
}
package pl.dido.image.c64;

import java.awt.Canvas;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
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

public class C64ExtraGui {

	public static JPanel c64Extra(final C64ExtraConfig config) {
		final JPanel panelC64Extra = new JPanel();
		panelC64Extra.setLayout(null);
		GuiUtils.addDASControls(panelC64Extra, config);
		
		final JRadioButton rdbtnLinearButton = new JRadioButton("linear");
		final JRadioButton rdbtnCubeButton = new JRadioButton("cube");
		final JSlider sldFlickering = new JSlider(JSlider.HORIZONTAL, 0, 3, (int) (config.flickering_factor * 1.8f));
		sldFlickering.setEnabled(config.extra_mode == EXTRA_MODE.MULTI_COLOR_INTERLACED);
		
		final JLabel lblConvertLabel = new JLabel("Converter mode:");
		lblConvertLabel.setFont(GuiUtils.bold);
		lblConvertLabel.setBounds(20, 70, 169, 14);
		panelC64Extra.add(lblConvertLabel);

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

		panelC64Extra.add(rdbtnHiresButton);
		
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

		panelC64Extra.add(rdbtnMCIButton);
		
		final ButtonGroup groupMode = new ButtonGroup();
		groupMode.add(rdbtnHiresButton);
		groupMode.add(rdbtnMCIButton);
		
		final JCheckBox chckbxColorRamp = new JCheckBox("Monochrome");
		chckbxColorRamp.setToolTipText("Reduce colors to 21 shades of grey");
		chckbxColorRamp.setFont(GuiUtils.std);
		chckbxColorRamp.setBounds(240, 85, 100, 20);
		chckbxColorRamp.setSelected(config.color_ramp);
		
		chckbxColorRamp.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				config.color_ramp = !config.color_ramp;
			}});
		
		panelC64Extra.add(chckbxColorRamp);
				
		final Canvas c64Logo = new ImageCanvas("c64Extra.png");
		c64Logo.setBounds(381, 7, 100, 96);
		panelC64Extra.add(c64Logo);
		
		final JLabel approxLabel = new JLabel("Color approximation:");
		approxLabel.setFont(GuiUtils.bold);
		approxLabel.setBounds(20, 120, 120, 20);
		panelC64Extra.add(approxLabel);
		
		rdbtnLinearButton.setToolTipText(
				"Linear color approximation. Most distant colors");
		rdbtnLinearButton.setFont(GuiUtils.std);
		rdbtnLinearButton.setBounds(46, 140, 60, 23);
		rdbtnLinearButton.setSelected(config.rgb_approximation == RGB_APPROXIMATION.LINEAR);
		rdbtnLinearButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				config.rgb_approximation = RGB_APPROXIMATION.LINEAR;
			}});

		panelC64Extra.add(rdbtnLinearButton);
		
		rdbtnCubeButton.setToolTipText(
				"Cube color approximation. Calculated within RGB cube");
		rdbtnCubeButton.setFont(GuiUtils.std);
		rdbtnCubeButton.setBounds(106, 140, 50, 23);
		rdbtnCubeButton.setSelected(config.rgb_approximation == RGB_APPROXIMATION.CUBE);
		rdbtnCubeButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				config.rgb_approximation = RGB_APPROXIMATION.CUBE;
			}});

		panelC64Extra.add(rdbtnCubeButton);
		
		final ButtonGroup groupApprox = new ButtonGroup();
		groupApprox.add(rdbtnLinearButton);
		groupApprox.add(rdbtnCubeButton);

		
		final JLabel thresholdLabel = new JLabel("luma threshold");
		thresholdLabel.setFont(GuiUtils.bold);
		thresholdLabel.setBounds(165, 120, 120, 20);
		panelC64Extra.add(thresholdLabel);

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
		panelC64Extra.add(sldLuma);
				
		final JLabel flickeringLabel = new JLabel("flickering");
		flickeringLabel.setFont(GuiUtils.bold);
		flickeringLabel.setBounds(285, 120, 100, 20);
		panelC64Extra.add(flickeringLabel);

		sldFlickering.setBounds(280, 140, 100, 35);
		sldFlickering.setFont(GuiUtils.std);
		sldFlickering.addChangeListener(new ChangeListener() {
			public void stateChanged(final ChangeEvent e) {
				final JSlider source = (JSlider) e.getSource();

				if (!source.getValueIsAdjusting())
					config.flickering_factor = source.getValue() * 1.8f;
			}
		});
		
		sldFlickering.setMajorTickSpacing(1);
		sldFlickering.setPaintLabels(true);
		panelC64Extra.add(sldFlickering);
				
		GuiUtils.addContrastControls(panelC64Extra, config);
		GuiUtils.addColorControls(panelC64Extra, config);
		GuiUtils.addFiltersControls(panelC64Extra, config);

		return panelC64Extra;
	}
}
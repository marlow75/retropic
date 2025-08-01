package pl.dido.image;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import pl.dido.image.utils.Config;
import pl.dido.image.utils.Config.FILTER;
import pl.dido.image.utils.Config.NEAREST_COLOR;
import pl.dido.image.utils.DitheringComboBox;

public class GuiUtils {

	public final static Font std = new Font("Tahoma", Font.BOLD, 10);
	public final static Font title = new Font("Tahoma", Font.PLAIN, 12);
	
	public final static Font bold = new Font("Tahoma", Font.BOLD, 10);
	public final static Font options = new Font("Tahoma", Font.BOLD, 8);

	public static final JPanel addDASControls(final JPanel panel, final Config config) {
		return addDASControls(panel, config, null, true);
	}
	
	public static final JPanel addDASControls(final JPanel panel, final Config config, final boolean ditherOptions[]) {
		return addDASControls(panel, config, ditherOptions, true);
	}

	public static final JPanel addDASControls(final JPanel panel, final Config config, final boolean ditherOptions[], final boolean pal) {
		final JLabel lblDitherLabel = new JLabel("Dithering & aspect & pal & bw:");
		lblDitherLabel.setFont(bold);
		lblDitherLabel.setBounds(20, 8, 200, 20);
		panel.add(lblDitherLabel);

		final JSlider sldError = new JSlider(JSlider.HORIZONTAL, 0, 4, config.error_threshold);
		final DitheringComboBox cmbDithering = ditherOptions != null ? new DitheringComboBox(config, ditherOptions) : new DitheringComboBox(config);
		
		cmbDithering.setToolTipText("Dithering options");
		cmbDithering.setFont(std);
		cmbDithering.setBounds(46, 30, 100, 20);
		cmbDithering.setSelectedIndex(config.dither_alg.ordinal());
		cmbDithering.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				sldError.setEnabled(cmbDithering.getSelectedIndex() > 2);
			}
		});

		cmbDithering.setSelectedIndex(config.dither_alg.ordinal());
		panel.add(cmbDithering);

		final JLabel errorLabel = new JLabel("error:");
		errorLabel.setFont(GuiUtils.bold);
		errorLabel.setBounds(210, 30, 30, 20);
		panel.add(errorLabel);

		sldError.setBounds(240, 30, 80, 30);
		sldError.setFont(GuiUtils.std);
		sldError.addChangeListener(new ChangeListener() {
			public void stateChanged(final ChangeEvent e) {
				final JSlider source = (JSlider) e.getSource();

				if (!source.getValueIsAdjusting())
					config.error_threshold = source.getValue();
			}
		});

		sldError.setMajorTickSpacing(2);
		sldError.setPaintLabels(true);
		panel.add(sldError);
		
		final JCheckBox chckbxAspectCheckBox = new JCheckBox("asp");
		chckbxAspectCheckBox.setToolTipText("Preserve orginal image aspect ratio");
		chckbxAspectCheckBox.setFont(GuiUtils.std);
		chckbxAspectCheckBox.setBounds(46, 60, 50, 20);
		chckbxAspectCheckBox.setSelected(config.preserve_aspect);

		chckbxAspectCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				config.preserve_aspect = !config.preserve_aspect;
			}
		});

		panel.add(chckbxAspectCheckBox);

		if (pal) {
			final JCheckBox chckbxBWCheckBox = new JCheckBox("bw");
			chckbxBWCheckBox.setToolTipText("Black/White PAL");
			chckbxBWCheckBox.setFont(GuiUtils.std);
			chckbxBWCheckBox.setBounds(146, 60, 40, 20);
			chckbxBWCheckBox.setSelected(config.black_white);

			chckbxBWCheckBox.addActionListener(new ActionListener() {
				public void actionPerformed(final ActionEvent e) {
					config.black_white = !config.black_white;
				}
			});

			panel.add(chckbxBWCheckBox);

			final JCheckBox chckbxPALCheckBox = new JCheckBox("pal");
			chckbxPALCheckBox.setToolTipText("Simple PAL emulation");
			chckbxPALCheckBox.setFont(GuiUtils.std);
			chckbxPALCheckBox.setBounds(96, 60, 40, 20);
			chckbxPALCheckBox.setSelected(config.pal_view);

			chckbxPALCheckBox.addActionListener(new ActionListener() {
				public void actionPerformed(final ActionEvent e) {
					config.pal_view = !config.pal_view;
					chckbxBWCheckBox.setEnabled(config.pal_view);
				}
			});

			panel.add(chckbxPALCheckBox);
		}

		return panel;
	}

	public static final void addContrastControls(final JPanel panel, final Config config) {
		final JLabel contrastLabel = new JLabel("Contrast processing:");
		contrastLabel.setFont(bold);
		contrastLabel.setBounds(20, 228, 300, 20);
		panel.add(contrastLabel);

		final JLabel sizeLabel = new JLabel("window size");
		sizeLabel.setFont(bold);
		sizeLabel.setBounds(225, 265, 120, 18);
		panel.add(sizeLabel);

		final JSlider sldWindow = new JSlider(JSlider.HORIZONTAL, 1, 3,
				config.window_size == 20 ? 1 : config.window_size == 30 ? 2 : 3);
		sldWindow.setBounds(220, 282, 120, 32);
		sldWindow.setFont(options);
		sldWindow.setEnabled(config.high_contrast == Config.HIGH_CONTRAST.SWAHE);
		sldWindow.addChangeListener(new ChangeListener() {
			public void stateChanged(final ChangeEvent e) {
				final JSlider source = (JSlider) e.getSource();

				if (!source.getValueIsAdjusting())
					switch (source.getValue()) {
					case 1:
						config.window_size = 20;
						break;
					case 2:
						config.window_size = 30;
						break;
					case 3:
						config.window_size = 40;
						break;
					}

			}
		});

		// create the label table
		final Hashtable<Integer, JLabel> labelTable1 = new Hashtable<Integer, JLabel>();
		labelTable1.put(1, new JLabel("20"));
		labelTable1.put(2, new JLabel("30"));
		labelTable1.put(3, new JLabel("40"));

		sldWindow.setLabelTable(labelTable1);
		sldWindow.setSnapToTicks(true);
		sldWindow.setPaintLabels(true);

		panel.add(sldWindow);

		final JLabel brightLabel = new JLabel("details");
		brightLabel.setFont(bold);
		brightLabel.setBounds(355, 265, 120, 18);
		panel.add(brightLabel);

		final JSlider sldBrightness = new JSlider(JSlider.HORIZONTAL, 1, 5, config.details);
		sldBrightness.setEnabled(config.high_contrast == Config.HIGH_CONTRAST.SWAHE
				|| config.high_contrast == Config.HIGH_CONTRAST.CLAHE);
		sldBrightness.setFont(options);
		sldBrightness.setBounds(350, 282, 120, 32);
		sldBrightness.addChangeListener(new ChangeListener() {
			public void stateChanged(final ChangeEvent e) {
				final JSlider source = (JSlider) e.getSource();

				if (!source.getValueIsAdjusting())
					config.details = source.getValue();
			}
		});

		sldBrightness.setPaintLabels(true);

		// create the label table
		final Hashtable<Integer, JLabel> labelTable2 = new Hashtable<Integer, JLabel>();
		labelTable2.put(1, new JLabel("1.0"));
		labelTable2.put(2, new JLabel("2.0"));
		labelTable2.put(3, new JLabel("3.0"));
		labelTable2.put(4, new JLabel("4.0"));
		labelTable2.put(5, new JLabel("5.0"));
		sldBrightness.setLabelTable(labelTable2);

		panel.add(sldBrightness);

		final JRadioButton rdbtnNoContrastExpanderButton = new JRadioButton("none");
		rdbtnNoContrastExpanderButton.setToolTipText("No contrast processing");
		rdbtnNoContrastExpanderButton.setFont(std);
		rdbtnNoContrastExpanderButton.setBounds(46, 248, 60, 20);
		rdbtnNoContrastExpanderButton.setSelected(config.high_contrast == Config.HIGH_CONTRAST.NONE);

		rdbtnNoContrastExpanderButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				config.high_contrast = Config.HIGH_CONTRAST.NONE;
				sldWindow.setEnabled(false);
				sldBrightness.setEnabled(false);
			}
		});

		panel.add(rdbtnNoContrastExpanderButton);

		final JRadioButton rdbtnHEButton = new JRadioButton("HE");
		rdbtnHEButton.setToolTipText("Histogram Equalizer");
		rdbtnHEButton.setFont(std);
		rdbtnHEButton.setBounds(116, 248, 50, 20);
		rdbtnHEButton.setSelected(config.high_contrast == Config.HIGH_CONTRAST.HE);

		rdbtnHEButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				config.high_contrast = Config.HIGH_CONTRAST.HE;
				sldWindow.setEnabled(false);
				sldBrightness.setEnabled(false);
			}
		});

		panel.add(rdbtnHEButton);

		final JRadioButton rdbtnCLAHEButton = new JRadioButton("CLAHE");
		rdbtnCLAHEButton.setToolTipText("Clipped Adaptive Histogram Equalizer");
		rdbtnCLAHEButton.setFont(std);
		rdbtnCLAHEButton.setBounds(186, 248, 70, 20);
		rdbtnCLAHEButton.setSelected(config.high_contrast == Config.HIGH_CONTRAST.CLAHE);

		rdbtnCLAHEButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				config.high_contrast = Config.HIGH_CONTRAST.CLAHE;
				sldWindow.setEnabled(false);
				sldBrightness.setEnabled(true);
			}
		});

		panel.add(rdbtnCLAHEButton);

		final JRadioButton rdbtnSWAHEButton = new JRadioButton("SWAHE");
		rdbtnSWAHEButton.setToolTipText("Sliding Window Adaptive Histogram Equalizer");
		rdbtnSWAHEButton.setFont(std);
		rdbtnSWAHEButton.setBounds(256, 248, 70, 20);
		rdbtnSWAHEButton.setSelected(config.high_contrast == Config.HIGH_CONTRAST.SWAHE);

		rdbtnSWAHEButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				config.high_contrast = Config.HIGH_CONTRAST.SWAHE;
				sldWindow.setEnabled(true);
				sldBrightness.setEnabled(true);
			}
		});

		panel.add(rdbtnSWAHEButton);

		final ButtonGroup groupContrast = new ButtonGroup();
		groupContrast.add(rdbtnNoContrastExpanderButton);
		groupContrast.add(rdbtnHEButton);
		groupContrast.add(rdbtnCLAHEButton);
		groupContrast.add(rdbtnSWAHEButton);
	}

	public static final void addColorControls(final JPanel panel, final Config config) {
		final JLabel lblColorLabel = new JLabel("Color distance:");
		lblColorLabel.setFont(bold);
		lblColorLabel.setBounds(20, 300, 198, 16);
		panel.add(lblColorLabel);

		final JRadioButton rdbtnEuclideanButton = new JRadioButton("simple euclidean");
		rdbtnEuclideanButton.setToolTipText("Simple euclidean distance");
		rdbtnEuclideanButton.setFont(std);
		rdbtnEuclideanButton.setBounds(46, 320, 150, 18);
		rdbtnEuclideanButton.setSelected(config.color_alg == NEAREST_COLOR.EUCLIDEAN);
		rdbtnEuclideanButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				config.color_alg = NEAREST_COLOR.EUCLIDEAN;
			}
		});
		panel.add(rdbtnEuclideanButton);

		final JRadioButton rdbtnPerceptedButton = new JRadioButton("percepted");
		rdbtnPerceptedButton.setToolTipText("Perception weighted distance");
		rdbtnPerceptedButton.setFont(std);
		rdbtnPerceptedButton.setBounds(202, 320, 113, 18);
		rdbtnPerceptedButton.setSelected(config.color_alg == NEAREST_COLOR.PERCEPTED);
		rdbtnPerceptedButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				config.color_alg = NEAREST_COLOR.PERCEPTED;
			}
		});

		panel.add(rdbtnPerceptedButton);

		final ButtonGroup groupDistance = new ButtonGroup();
		groupDistance.add(rdbtnEuclideanButton);
		groupDistance.add(rdbtnPerceptedButton);

		if (config.allow_luminance) {
			final JRadioButton rdbtnLumaButton = new JRadioButton("luminance");
			rdbtnLumaButton.setToolTipText("Luminance distance for BW screens");
			rdbtnLumaButton.setFont(std);
			rdbtnLumaButton.setBounds(347, 320, 139, 18);
			rdbtnLumaButton.setSelected(config.color_alg == NEAREST_COLOR.LUMA_WEIGHTED);
			rdbtnLumaButton.addActionListener(new ActionListener() {
				public void actionPerformed(final ActionEvent e) {
					config.color_alg = NEAREST_COLOR.LUMA_WEIGHTED;
				}
			});

			panel.add(rdbtnLumaButton);
			groupDistance.add(rdbtnLumaButton);
		}
	}
	
	public static final void addFiltersControls(final JPanel panel, final Config config) {
		final JLabel lblFilterLabel = new JLabel("Image filters:");
		lblFilterLabel.setFont(bold);
		lblFilterLabel.setBounds(20, 350, 100, 16);
		panel.add(lblFilterLabel);

		final JRadioButton rdbtnNoneButton = new JRadioButton("none");
		rdbtnNoneButton.setToolTipText("None of filter is applied");
		rdbtnNoneButton.setFont(std);
		rdbtnNoneButton.setBounds(46, 370, 80, 18);
		rdbtnNoneButton.setSelected(config.filter == FILTER.NONE);
		rdbtnNoneButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				config.filter = FILTER.NONE;
			}
		});
		panel.add(rdbtnNoneButton);

		final JRadioButton rdbtnLowpassButton = new JRadioButton("lowpass");
		rdbtnLowpassButton.setToolTipText("Lowpass filter");
		rdbtnLowpassButton.setFont(std);
		rdbtnLowpassButton.setBounds(126, 370, 80, 18);
		rdbtnLowpassButton.setSelected(config.filter == FILTER.LOWPASS);
		rdbtnLowpassButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				config.filter = FILTER.LOWPASS;
			}
		});

		panel.add(rdbtnLowpassButton);

		final JRadioButton rdbtnSharpenButton = new JRadioButton("sharpen");
		rdbtnSharpenButton.setToolTipText("More details");
		rdbtnSharpenButton.setFont(std);
		rdbtnSharpenButton.setBounds(206, 370, 80, 18);
		rdbtnSharpenButton.setSelected(config.filter == FILTER.SHARPEN);
		rdbtnSharpenButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				config.filter = FILTER.SHARPEN;
			}
		});

		panel.add(rdbtnSharpenButton);	

		final JRadioButton rdbtnEmbossButton = new JRadioButton("emboss");
		rdbtnEmbossButton.setToolTipText("Simple emboss filter");
		rdbtnEmbossButton.setFont(std);
		rdbtnEmbossButton.setBounds(286, 370, 80, 18);
		rdbtnEmbossButton.setSelected(config.filter == FILTER.EMBOSS);
		rdbtnEmbossButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				config.filter = FILTER.EMBOSS;
			}
		});

		panel.add(rdbtnEmbossButton);	

		final JRadioButton rdbtnEdgeButton = new JRadioButton("edge");
		rdbtnEdgeButton.setToolTipText("More details less color, try with HE");
		rdbtnEdgeButton.setFont(std);
		rdbtnEdgeButton.setBounds(366, 370, 80, 18);
		rdbtnEdgeButton.setSelected(config.filter == FILTER.EDGES_BLEND);
		rdbtnEdgeButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				config.filter = FILTER.EDGES_BLEND;
			}
		});

		panel.add(rdbtnEdgeButton);	
		
		final ButtonGroup filterDistance = new ButtonGroup();
		filterDistance.add(rdbtnNoneButton);
		
		filterDistance.add(rdbtnLowpassButton);
		filterDistance.add(rdbtnSharpenButton);
		
		filterDistance.add(rdbtnEmbossButton);
		filterDistance.add(rdbtnEdgeButton);
	}
}

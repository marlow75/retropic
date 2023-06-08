package pl.dido.image;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import pl.dido.image.Config.NEAREST_COLOR;

public class GuiUtils {

	public final static Font std = new Font("Tahoma", Font.BOLD, 10);
	public final static Font bold = new Font("Tahoma", Font.BOLD, 10);
	
	public static final JPanel addDitheringControls(final JPanel panel, final Config config) {		
		final JLabel lblDitherLabel = new JLabel("Dithering:");
		lblDitherLabel.setFont(bold);
		lblDitherLabel.setBounds(20, 10, 100, 20);
		panel.add(lblDitherLabel);
		
		final JRadioButton rdbtnNoDitherButton = new JRadioButton("none");
		rdbtnNoDitherButton.setToolTipText("No dithering at all");
		rdbtnNoDitherButton.setFont(std);
		rdbtnNoDitherButton.setBounds(46, 30, 80, 20);
		rdbtnNoDitherButton.setSelected(!config.dithering);
		
		rdbtnNoDitherButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				config.dithering = false;
			}});
		
		panel.add(rdbtnNoDitherButton);
		
		final JRadioButton rdbtnFSButton = new JRadioButton("floyds");
		rdbtnFSButton.setToolTipText("Floyd-Steinberg dithering, global color error");
		rdbtnFSButton.setFont(std);
		rdbtnFSButton.setBounds(140, 30, 80, 20);
		rdbtnFSButton.setSelected(config.dithering && (config.dither_alg == Config.DITHERING.STD_FS));
		
		rdbtnFSButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				config.dither_alg = Config.DITHERING.STD_FS;
				config.dithering = true;
			}});
		
		panel.add(rdbtnFSButton);
		
		final JRadioButton rdbtnAtkinsonButton = new JRadioButton("apple");
		rdbtnAtkinsonButton.setToolTipText("Atkinson dithering, lokal color error");
		rdbtnAtkinsonButton.setFont(std);
		rdbtnAtkinsonButton.setBounds(230, 30, 80, 20);
		rdbtnAtkinsonButton.setSelected(config.dithering && (config.dither_alg == Config.DITHERING.ATKINSON));
		
		rdbtnAtkinsonButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				config.dither_alg = Config.DITHERING.ATKINSON;
				config.dithering = true;
			}});
		
		panel.add(rdbtnAtkinsonButton);
		
		final ButtonGroup groupContrast = new ButtonGroup();
		groupContrast.add(rdbtnNoDitherButton);
		groupContrast.add(rdbtnFSButton);
		groupContrast.add(rdbtnAtkinsonButton);
		
		return panel;
	}
	
	public static final void addContrastControls(final JPanel panel, final Config config) {
		final JLabel contrastLabel = new JLabel("Contrast processing:");
		contrastLabel.setFont(bold);
		contrastLabel.setBounds(20, 190, 300, 20);
		panel.add(contrastLabel);

		final JLabel sizeLabel = new JLabel("window size");
		sizeLabel.setFont(bold);
		sizeLabel.setBounds(225, 235, 120, 20);
		panel.add(sizeLabel);

		final JSlider sldWindow = new JSlider(JSlider.HORIZONTAL, 20, 40, config.swaheWindowSize);
		sldWindow.setBounds(220, 255, 120, 35);
		sldWindow.setFont(std);
		sldWindow.setEnabled(config.highContrast == Config.HIGH_CONTRAST.SWAHE);
		sldWindow.addChangeListener(new ChangeListener() {
			public void stateChanged(final ChangeEvent e) {
				final JSlider source = (JSlider) e.getSource();

				if (!source.getValueIsAdjusting()) {
					int value = (int) source.getValue();
					if (value < 30)
						value = 20;
					else
						value = 40;

					sldWindow.setValue(value);
					config.swaheWindowSize = value;
				}
			}
		});

		sldWindow.setPaintLabels(true);

		// create the label table
		final Hashtable<Integer, JLabel> labelTable1 = new Hashtable<Integer, JLabel>();
		labelTable1.put(20, new JLabel("20"));
		labelTable1.put(40, new JLabel("40"));
		sldWindow.setLabelTable(labelTable1);

		panel.add(sldWindow);
		
		final JLabel brightLabel = new JLabel("brigthness");
		brightLabel.setFont(bold);
		brightLabel.setBounds(355, 235, 120, 20);
		panel.add(brightLabel);

		final JSlider sldBrightness = new JSlider(JSlider.HORIZONTAL, 10, 40, (int)(config.swaheBrightness * 10));
		sldBrightness.setEnabled(config.highContrast == Config.HIGH_CONTRAST.SWAHE);
		sldBrightness.setFont(GuiUtils.std);
		sldBrightness.setBounds(350, 255, 120, 35);
		sldBrightness.addChangeListener(new ChangeListener() {
			public void stateChanged(final ChangeEvent e) {
				final JSlider source = (JSlider) e.getSource();

				if (!source.getValueIsAdjusting()) {
					final float value = source.getValue() / 10f;
					config.swaheBrightness = value;
				}
			}
		});

		sldBrightness.setPaintLabels(true);

		// create the label table
		final Hashtable<Integer, JLabel> labelTable2 = new Hashtable<Integer, JLabel>();
		labelTable2.put(10, new JLabel("1.0"));
		labelTable2.put(20, new JLabel("2.0"));
		labelTable2.put(30, new JLabel("3.0"));
		labelTable2.put(40, new JLabel("4.0"));
		sldBrightness.setLabelTable(labelTable2);

		panel.add(sldBrightness);
		
		final JRadioButton rdbtnNoContrastExpanderButton = new JRadioButton("none");
		rdbtnNoContrastExpanderButton.setToolTipText("No contrast processing");
		rdbtnNoContrastExpanderButton.setFont(std);
		rdbtnNoContrastExpanderButton.setBounds(46, 213, 80, 20);
		rdbtnNoContrastExpanderButton.setSelected(config.highContrast == Config.HIGH_CONTRAST.NONE);

		rdbtnNoContrastExpanderButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				config.highContrast = Config.HIGH_CONTRAST.NONE;
				sldWindow.setEnabled(false);
				sldBrightness.setEnabled(false);
			}
		});

		panel.add(rdbtnNoContrastExpanderButton);

		final JRadioButton rdbtnHEButton = new JRadioButton("HE");
		rdbtnHEButton.setToolTipText("Histogram Equalizer");
		rdbtnHEButton.setFont(std);
		rdbtnHEButton.setBounds(140, 213, 80, 20);
		rdbtnHEButton.setSelected(config.highContrast == Config.HIGH_CONTRAST.HE);

		rdbtnHEButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				config.highContrast = Config.HIGH_CONTRAST.HE;
				sldWindow.setEnabled(false);
				sldBrightness.setEnabled(false);
			}
		});

		panel.add(rdbtnHEButton);

		final JRadioButton rdbtnSWAHEButton = new JRadioButton("SWAHE");
		rdbtnSWAHEButton.setToolTipText("Sliding Window Adaptive Histogram Equalizer");
		rdbtnSWAHEButton.setFont(std);
		rdbtnSWAHEButton.setBounds(230, 213, 80, 20);
		rdbtnSWAHEButton.setSelected(config.highContrast == Config.HIGH_CONTRAST.SWAHE);

		rdbtnSWAHEButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				config.highContrast = Config.HIGH_CONTRAST.SWAHE;
				sldWindow.setEnabled(true);
				sldBrightness.setEnabled(true);
			}
		});

		panel.add(rdbtnSWAHEButton);

		final ButtonGroup groupContrast = new ButtonGroup();
		groupContrast.add(rdbtnNoContrastExpanderButton);
		groupContrast.add(rdbtnHEButton);
		groupContrast.add(rdbtnSWAHEButton);
	}
	
	public static final void addColorControls(final JPanel panel, final Config config) {		
		final JLabel lblColorLabel = new JLabel("Color distance:");
		lblColorLabel.setFont(bold);
		lblColorLabel.setBounds(20, 275, 198, 16);
		panel.add(lblColorLabel);
		
		final JRadioButton rdbtnEuclideanButton = new JRadioButton("simple euclidean");
		rdbtnEuclideanButton.setToolTipText("Simple euclidean distance");
		rdbtnEuclideanButton.setFont(std);
		rdbtnEuclideanButton.setBounds(46, 295, 150, 18);
		rdbtnEuclideanButton.setSelected(config.color_alg == NEAREST_COLOR.EUCLIDEAN);
		rdbtnEuclideanButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				config.color_alg = NEAREST_COLOR.EUCLIDEAN;		
			}});
		panel.add(rdbtnEuclideanButton);
		
		final JRadioButton rdbtnPerceptedButton = new JRadioButton("percepted");
		rdbtnPerceptedButton.setToolTipText("Perception weighted distance");
		rdbtnPerceptedButton.setFont(std);
		rdbtnPerceptedButton.setBounds(202, 295, 113, 18);
		rdbtnPerceptedButton.setSelected(config.color_alg == NEAREST_COLOR.PERCEPTED);
		rdbtnPerceptedButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				config.color_alg = NEAREST_COLOR.PERCEPTED;		
			}});

		panel.add(rdbtnPerceptedButton);
		
		final JRadioButton rdbtnLumaButton = new JRadioButton("luma weighted");
		rdbtnLumaButton.setToolTipText("Luma weighted euclidean");
		rdbtnLumaButton.setFont(std);
		rdbtnLumaButton.setBounds(347, 295, 139, 18);
		rdbtnLumaButton.setSelected(config.color_alg == NEAREST_COLOR.LUMA_WEIGHTED);
		rdbtnLumaButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				config.color_alg = NEAREST_COLOR.LUMA_WEIGHTED;		
			}});

		panel.add(rdbtnLumaButton);
		
		final ButtonGroup groupDistance = new ButtonGroup();
		groupDistance.add(rdbtnEuclideanButton);
		groupDistance.add(rdbtnPerceptedButton);
		groupDistance.add(rdbtnLumaButton);		
	}
}

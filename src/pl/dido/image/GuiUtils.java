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
import pl.dido.image.utils.Config.NEAREST_COLOR;

public class GuiUtils {

	public final static Font std = new Font("Tahoma", Font.BOLD, 10);
	public final static Font title = new Font("Tahoma", Font.PLAIN, 12);
	public final static Font bold = new Font("Tahoma", Font.BOLD, 10);
	
	public static final JPanel addDASControls(final JPanel panel, final Config config) {
		return addDASControls(panel, config, true);
	}
	
	public static final JPanel addDASControls(final JPanel panel, final Config config, final boolean extra) {		
		final JLabel lblDitherLabel = new JLabel("Dithering & aspect & scanline:");
		lblDitherLabel.setFont(bold);
		lblDitherLabel.setBounds(20, 10, 200, 20);
		panel.add(lblDitherLabel);
		
		final JRadioButton rdbtnNoDitherButton = new JRadioButton("none");
		rdbtnNoDitherButton.setToolTipText("No dithering at all");
		rdbtnNoDitherButton.setFont(std);
		rdbtnNoDitherButton.setBounds(46, 30, 50, 20);
		rdbtnNoDitherButton.setSelected(!config.dithering);
		
		rdbtnNoDitherButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				config.dithering = false;
			}});
		
		panel.add(rdbtnNoDitherButton);
		
		final JRadioButton rdbtnFSButton = new JRadioButton("floyds");
		rdbtnFSButton.setToolTipText("Floyd-Steinberg dithering, global color error");
		rdbtnFSButton.setFont(std);
		rdbtnFSButton.setBounds(106, 30, 60, 20);
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
		rdbtnAtkinsonButton.setBounds(166, 30, 55, 20);
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
		
		final JCheckBox chckbxAspectCheckBox = new JCheckBox("aspect");
		chckbxAspectCheckBox.setToolTipText("Preserve orginal image aspect ratio");
		chckbxAspectCheckBox.setFont(GuiUtils.std);
		chckbxAspectCheckBox.setBounds(220, 30, 58, 20);
		chckbxAspectCheckBox.setSelected(config.preserveAspect);
		
		chckbxAspectCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				config.preserveAspect = !config.preserveAspect;
			}});
		
		panel.add(chckbxAspectCheckBox);
		
		if (extra) {
			final JCheckBox chckbxRasterCheckBox = new JCheckBox("scan");
			chckbxRasterCheckBox.setToolTipText("Scan line simulation");
			chckbxRasterCheckBox.setFont(GuiUtils.std);
			chckbxRasterCheckBox.setBounds(274, 30, 58, 20);
			chckbxRasterCheckBox.setSelected(config.scanline);
			
			chckbxRasterCheckBox.addActionListener(new ActionListener() {
				public void actionPerformed(final ActionEvent e) {
					config.scanline = !config.scanline;
				}});
			
			panel.add(chckbxRasterCheckBox);
		}
		
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

		final JSlider sldWindow = new JSlider(JSlider.HORIZONTAL, 1, 3, config.windowSize == 20 ? 1 : config.windowSize == 30 ? 2 : 3);
		sldWindow.setBounds(220, 255, 120, 35);
		sldWindow.setFont(std);
		sldWindow.setEnabled(config.highContrast == Config.HIGH_CONTRAST.SWAHE);
		sldWindow.addChangeListener(new ChangeListener() {
			public void stateChanged(final ChangeEvent e) {
				final JSlider source = (JSlider) e.getSource();

				if (!source.getValueIsAdjusting())
					switch (source.getValue()) {
					case 1:
						config.windowSize = 20;
						break;
					case 2:
						config.windowSize = 30;
						break;
					case 3:
						config.windowSize = 40;
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
		brightLabel.setBounds(355, 235, 120, 20);
		panel.add(brightLabel);

		final JSlider sldBrightness = new JSlider(JSlider.HORIZONTAL, 1, 5, config.details);
		sldBrightness.setEnabled(config.highContrast == Config.HIGH_CONTRAST.SWAHE || config.highContrast == Config.HIGH_CONTRAST.CLAHE);
		sldBrightness.setFont(std);
		sldBrightness.setBounds(350, 255, 120, 35);
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
		rdbtnNoContrastExpanderButton.setBounds(46, 213, 50, 20);
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
		rdbtnHEButton.setBounds(116, 213, 50, 20);
		rdbtnHEButton.setSelected(config.highContrast == Config.HIGH_CONTRAST.HE);

		rdbtnHEButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				config.highContrast = Config.HIGH_CONTRAST.HE;
				sldWindow.setEnabled(false);
				sldBrightness.setEnabled(false);
			}
		});

		panel.add(rdbtnHEButton);
		
		final JRadioButton rdbtnCLAHEButton = new JRadioButton("CLAHE");
		rdbtnCLAHEButton.setToolTipText("Clipped Adaptive Histogram Equalizer");
		rdbtnCLAHEButton.setFont(std);
		rdbtnCLAHEButton.setBounds(186, 213, 70, 20);
		rdbtnCLAHEButton.setSelected(config.highContrast == Config.HIGH_CONTRAST.CLAHE);

		rdbtnCLAHEButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				config.highContrast = Config.HIGH_CONTRAST.CLAHE;
				sldWindow.setEnabled(false);
				sldBrightness.setEnabled(true);
			}
		});

		panel.add(rdbtnCLAHEButton);

		final JRadioButton rdbtnSWAHEButton = new JRadioButton("SWAHE");
		rdbtnSWAHEButton.setToolTipText("Sliding Window Adaptive Histogram Equalizer");
		rdbtnSWAHEButton.setFont(std);
		rdbtnSWAHEButton.setBounds(256, 213, 70, 20);
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
		groupContrast.add(rdbtnCLAHEButton);
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

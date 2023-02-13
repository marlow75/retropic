package pl.dido.image;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import pl.dido.image.Config.NEAREST_COLOR;

public class GuiUtils {

	public final static Font std = new Font("Tahoma", Font.PLAIN, 16);
	public final static Font bold = new Font("Tahoma", Font.BOLD, 16);
	
	public static final JPanel addDitheringControls(final JPanel panel, final Config config) {		
		final JLabel lblDitherLabel = new JLabel("Dithering");
		lblDitherLabel.setFont(bold);
		lblDitherLabel.setBounds(20, 10, 100, 20);
		panel.add(lblDitherLabel);
		
		final JRadioButton rdbtnNoDitherButton = new JRadioButton("none");
		rdbtnNoDitherButton.setToolTipText("No predithering at all");
		rdbtnNoDitherButton.setFont(std);
		rdbtnNoDitherButton.setBounds(46, 33, 80, 44);
		rdbtnNoDitherButton.setSelected(!config.dithering);
		
		rdbtnNoDitherButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				config.dithering = false;
			}});
		
		panel.add(rdbtnNoDitherButton);
		
		final JRadioButton rdbtnFSButton = new JRadioButton("floyds");
		rdbtnFSButton.setToolTipText("Floyd-Steinberg dithering, global color error");
		rdbtnFSButton.setFont(std);
		rdbtnFSButton.setBounds(140, 33, 80, 44);
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
		rdbtnAtkinsonButton.setBounds(230, 33, 80, 44);
		rdbtnAtkinsonButton.setSelected(config.dithering && (config.dither_alg == Config.DITHERING.ATKINSON));
		
		rdbtnAtkinsonButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				config.dither_alg = Config.DITHERING.ATKINSON;
				config.dithering = true;
			}});
		
		panel.add(rdbtnAtkinsonButton);
		
		final ButtonGroup groupDithering = new ButtonGroup();
		groupDithering.add(rdbtnNoDitherButton);
		groupDithering.add(rdbtnFSButton);
		groupDithering.add(rdbtnAtkinsonButton);
		
		return panel;
	}
	
	public static final void addColorControls(final JPanel panel, final Config config) {		
		final JLabel lblColorLabel = new JLabel("Color distance:");
		lblColorLabel.setFont(GuiUtils.bold);
		lblColorLabel.setBounds(20, 256, 198, 16);
		panel.add(lblColorLabel);
		
		final JRadioButton rdbtnEuclideanButton = new JRadioButton("simple euclidean");
		rdbtnEuclideanButton.setToolTipText("Simple euclidean distance");
		rdbtnEuclideanButton.setFont(new Font("Tahoma", Font.PLAIN, 16));
		rdbtnEuclideanButton.setBounds(46, 287, 172, 18);
		rdbtnEuclideanButton.setSelected(config.color_alg == NEAREST_COLOR.EUCLIDEAN);
		rdbtnEuclideanButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				config.color_alg = NEAREST_COLOR.EUCLIDEAN;		
			}});
		panel.add(rdbtnEuclideanButton);
		
		final JRadioButton rdbtnPerceptedButton = new JRadioButton("percepted");
		rdbtnPerceptedButton.setToolTipText("Perception weighted distance");
		rdbtnPerceptedButton.setFont(new Font("Tahoma", Font.PLAIN, 16));
		rdbtnPerceptedButton.setBounds(222, 287, 113, 18);
		rdbtnPerceptedButton.setSelected(config.color_alg == NEAREST_COLOR.PERCEPTED);
		rdbtnPerceptedButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				config.color_alg = NEAREST_COLOR.PERCEPTED;		
			}});

		panel.add(rdbtnPerceptedButton);
		
		final JRadioButton rdbtnLumaButton = new JRadioButton("luma weighted");
		rdbtnLumaButton.setToolTipText("Luma weighted euclidean");
		rdbtnLumaButton.setFont(new Font("Tahoma", Font.PLAIN, 16));
		rdbtnLumaButton.setBounds(347, 287, 139, 18);
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

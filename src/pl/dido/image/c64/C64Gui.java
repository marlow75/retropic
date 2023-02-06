package pl.dido.image.c64;

import java.awt.Canvas;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import pl.dido.image.Config.NEAREST_COLOR;
import pl.dido.image.c64.C64Config.LUMA_PIXELS;
import pl.dido.image.c64.C64Config.PIXEL_MERGE;
import pl.dido.image.c64.C64Config.SCREEN_MODE;
import pl.dido.image.utils.ImageCanvas;

public class C64Gui {

	public static JPanel c64Tab(final C64Config config) {
		final JPanel panelC64 = new JPanel();
		panelC64.setLayout(null);

		final JLabel lblConvertLabel = new JLabel("Converter mode:");
		lblConvertLabel.setFont(new Font("Tahoma", Font.BOLD, 16));
		lblConvertLabel.setBounds(20, 86, 169, 14);
		panelC64.add(lblConvertLabel);

		final JRadioButton rdbtnHiresButton = new JRadioButton("320x200 hires - highest/lowest luminance");
		rdbtnHiresButton.setToolTipText(
				"High resolution mode. Two colors, first with most luminance and second with lowest");
		rdbtnHiresButton.setFont(new Font("Tahoma", Font.PLAIN, 16));
		rdbtnHiresButton.setBounds(46, 112, 331, 23);
		rdbtnHiresButton.setSelected(config.screen_mode == SCREEN_MODE.HIRES);
		rdbtnHiresButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				config.screen_mode = SCREEN_MODE.HIRES;
			}});

		panelC64.add(rdbtnHiresButton);

		final JRadioButton rdbtnMulticolorButton = new JRadioButton("160x200 multicolor - color popularity");
		rdbtnMulticolorButton.setToolTipText(
				"Multicolour mode, colors are chosen by how frequently they apears");
		rdbtnMulticolorButton.setFont(new Font("Tahoma", Font.PLAIN, 16));
		rdbtnMulticolorButton.setBounds(46, 193, 331, 23);
		rdbtnMulticolorButton.setSelected(config.screen_mode == SCREEN_MODE.MULTICOLOR);
		rdbtnMulticolorButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				config.screen_mode = SCREEN_MODE.MULTICOLOR;
			}});

		panelC64.add(rdbtnMulticolorButton);

		final ButtonGroup groupResolution = new ButtonGroup();
		groupResolution.add(rdbtnHiresButton);
		groupResolution.add(rdbtnMulticolorButton);

		final JCheckBox chckbxVividCheckBox = new JCheckBox("VIVID colors");
		chckbxVividCheckBox.setToolTipText("Enables picture predithering");
		chckbxVividCheckBox.setFont(new Font("Tahoma", Font.PLAIN, 16));
		chckbxVividCheckBox.setBounds(20, 23, 171, 44);
		chckbxVividCheckBox.setSelected(config.vivid);
		
		chckbxVividCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				config.vivid = !config.vivid;
			}});
		
		panelC64.add(chckbxVividCheckBox);

		final JRadioButton rdbtnOuterButton = new JRadioButton("8x8 outer pixels");
		rdbtnOuterButton.setToolTipText("Gets luminance values at the edges");
		rdbtnOuterButton.setBounds(148, 147, 139, 23);
		rdbtnOuterButton.setSelected(config.luma_pixels == LUMA_PIXELS.OUTER);
		rdbtnOuterButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				config.luma_pixels = LUMA_PIXELS.OUTER;		
			}});

		panelC64.add(rdbtnOuterButton);

		final JRadioButton rdbtnInnerButton = new JRadioButton("8x8 even pixels");
		rdbtnInnerButton.setToolTipText("Gets luminance values of even pixels");
		rdbtnInnerButton.setBounds(337, 147, 119, 23);
		rdbtnInnerButton.setSelected(config.luma_pixels == LUMA_PIXELS.INNER);
		rdbtnInnerButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				config.luma_pixels = LUMA_PIXELS.INNER;		
			}});
		
		panelC64.add(rdbtnInnerButton);

		final ButtonGroup groupLuminance = new ButtonGroup();
		groupLuminance.add(rdbtnOuterButton);
		groupLuminance.add(rdbtnInnerButton);

		final Canvas mesh1 = new ImageCanvas("mesh1.png");
		mesh1.setBounds(290, 130, 58, 57);
		panelC64.add(mesh1);

		final Canvas mesh2 = new ImageCanvas("mesh2.png");
		mesh2.setBounds(102, 130, 59, 57);
		panelC64.add(mesh2);

		final Canvas c64Logo = new ImageCanvas("c64.png");
		c64Logo.setBounds(381, 7, 100, 96);
		panelC64.add(c64Logo);
		
		final JLabel lblColorLabel = new JLabel("Color distance:");
		lblColorLabel.setFont(new Font("Tahoma", Font.BOLD, 16));
		lblColorLabel.setBounds(20, 256, 198, 16);
		panelC64.add(lblColorLabel);
		
		final JRadioButton rdbtnEuclideanButton = new JRadioButton("simple euclidean");
		rdbtnEuclideanButton.setToolTipText("Simple euclidean distance");
		rdbtnEuclideanButton.setFont(new Font("Tahoma", Font.PLAIN, 16));
		rdbtnEuclideanButton.setBounds(46, 287, 172, 18);
		rdbtnEuclideanButton.setSelected(config.color_alg == NEAREST_COLOR.EUCLIDEAN);
		rdbtnEuclideanButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				config.color_alg = NEAREST_COLOR.EUCLIDEAN;		
			}});
		panelC64.add(rdbtnEuclideanButton);
		
		final JRadioButton rdbtnPerceptedButton = new JRadioButton("percepted");
		rdbtnPerceptedButton.setToolTipText("Perception weighted distance");
		rdbtnPerceptedButton.setFont(new Font("Tahoma", Font.PLAIN, 16));
		rdbtnPerceptedButton.setBounds(222, 287, 113, 18);
		rdbtnPerceptedButton.setSelected(config.color_alg == NEAREST_COLOR.PERCEPTED);
		rdbtnPerceptedButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				config.color_alg = NEAREST_COLOR.PERCEPTED;		
			}});

		panelC64.add(rdbtnPerceptedButton);
		
		final JRadioButton rdbtnLumaButton = new JRadioButton("luma weighted");
		rdbtnLumaButton.setToolTipText("Luma weighted euclidean");
		rdbtnLumaButton.setFont(new Font("Tahoma", Font.PLAIN, 16));
		rdbtnLumaButton.setBounds(347, 287, 139, 18);
		rdbtnLumaButton.setSelected(config.color_alg == NEAREST_COLOR.LUMA_WEIGHTED);
		rdbtnLumaButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				config.color_alg = NEAREST_COLOR.LUMA_WEIGHTED;		
			}});

		panelC64.add(rdbtnLumaButton);
		
		final ButtonGroup groupDistance = new ButtonGroup();
		groupDistance.add(rdbtnEuclideanButton);
		groupDistance.add(rdbtnPerceptedButton);
		groupDistance.add(rdbtnLumaButton);
		
		final JRadioButton rdbtnAverageMergeButton = new JRadioButton("averge merge");
		rdbtnAverageMergeButton.setBounds(111, 226, 113, 18);
		rdbtnAverageMergeButton.setToolTipText("calculate average color");
		rdbtnAverageMergeButton.setSelected(config.pixel_merge == PIXEL_MERGE.AVERAGE);
		rdbtnAverageMergeButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				config.pixel_merge = PIXEL_MERGE.AVERAGE;		
			}});

		panelC64.add(rdbtnAverageMergeButton);
				
		final JRadioButton rdbtnBrightestMergeRadioButton = new JRadioButton("brightest merge");
		rdbtnBrightestMergeRadioButton.setToolTipText("gets brightest pixel");
		rdbtnBrightestMergeRadioButton.setBounds(298, 228, 152, 18);
		rdbtnBrightestMergeRadioButton.setSelected(config.pixel_merge == PIXEL_MERGE.BRIGHTEST);
		rdbtnBrightestMergeRadioButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				config.pixel_merge = PIXEL_MERGE.BRIGHTEST;		
			}});

		panelC64.add(rdbtnBrightestMergeRadioButton);
		
		final ButtonGroup groupMerge = new ButtonGroup();
		groupMerge.add(rdbtnAverageMergeButton);
		groupMerge.add(rdbtnBrightestMergeRadioButton);

		return panelC64;
	}
}

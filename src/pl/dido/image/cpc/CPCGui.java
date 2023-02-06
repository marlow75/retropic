package pl.dido.image.cpc;

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
import pl.dido.image.utils.ImageCanvas;

public class CPCGui {

	public static JPanel cpcTab(final CPCConfig config) {
		final JPanel cpcPanel = new JPanel();
		cpcPanel.setLayout(null);

		final JCheckBox chckbxVividCheckBox = new JCheckBox("dithering");
		chckbxVividCheckBox.setToolTipText("Enables picture predithering");
		chckbxVividCheckBox.setFont(new Font("Tahoma", Font.PLAIN, 16));
		chckbxVividCheckBox.setBounds(20, 23, 171, 44);
		chckbxVividCheckBox.setSelected(config.vivid);
		
		chckbxVividCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				config.vivid = !config.vivid;
			}});
		
		cpcPanel.add(chckbxVividCheckBox);

		final Canvas c64Logo = new ImageCanvas("amstrad.png");
		c64Logo.setBounds(300, 0, 200, 150);
		cpcPanel.add(c64Logo);

		final JCheckBox chkReplaceBox = new JCheckBox("replace brightest");
		chkReplaceBox.setToolTipText("Replaces brightest with dimmed yellow");
		chkReplaceBox.setFont(new Font("Tahoma", Font.PLAIN, 16));
		chkReplaceBox.setBounds(20, 76, 169, 23);
		chkReplaceBox.setSelected(config.replace_white);
		chkReplaceBox.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				config.replace_white = !config.replace_white; 		
			}});

		cpcPanel.add(chkReplaceBox);
		
		final JLabel lblConvertLabel = new JLabel("Converter mode:");
		lblConvertLabel.setFont(new Font("Tahoma", Font.BOLD, 16));
		lblConvertLabel.setBounds(20, 112, 250, 23);
		cpcPanel.add(lblConvertLabel);

		final JRadioButton rdbtnHiresButton = new JRadioButton("320x200 mode1 - 4 colors");
		rdbtnHiresButton.setToolTipText("Mode 1, colors 211");
		rdbtnHiresButton.setFont(new Font("Tahoma", Font.PLAIN, 16));
		rdbtnHiresButton.setBounds(46, 130, 250, 57);
		rdbtnHiresButton.setSelected(config.screen_mode == CPCConfig.SCREEN_MODE.MODE1);
		rdbtnHiresButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				config.screen_mode = CPCConfig.SCREEN_MODE.MODE1;
			}});

		cpcPanel.add(rdbtnHiresButton);
		
		final JRadioButton rdbtnMulticolorButton = new JRadioButton("160x200 mode0 - 16 colors");
		rdbtnMulticolorButton.setToolTipText("Mode 0, colors 322 + most popular");
		rdbtnMulticolorButton.setFont(new Font("Tahoma", Font.PLAIN, 16));
		rdbtnMulticolorButton.setBounds(46, 193, 331, 23);
		rdbtnMulticolorButton.setSelected(config.screen_mode == CPCConfig.SCREEN_MODE.MODE0);
		rdbtnMulticolorButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				config.screen_mode = CPCConfig.SCREEN_MODE.MODE0;
			}});

		cpcPanel.add(rdbtnMulticolorButton);

		final ButtonGroup groupResolution = new ButtonGroup();
		groupResolution.add(rdbtnHiresButton);
		groupResolution.add(rdbtnMulticolorButton);
		
		final JLabel lblColorLabel = new JLabel("Color distance:");
		lblColorLabel.setFont(new Font("Tahoma", Font.BOLD, 16));
		lblColorLabel.setBounds(20, 256, 198, 16);
		cpcPanel.add(lblColorLabel);
		
		final JRadioButton rdbtnEuclideanButton = new JRadioButton("simple euclidean");
		rdbtnEuclideanButton.setToolTipText("Simple euclidean distance");
		rdbtnEuclideanButton.setFont(new Font("Tahoma", Font.PLAIN, 16));
		rdbtnEuclideanButton.setBounds(46, 287, 172, 18);
		rdbtnEuclideanButton.setSelected(config.color_alg == NEAREST_COLOR.EUCLIDEAN);
		rdbtnEuclideanButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				config.color_alg = NEAREST_COLOR.EUCLIDEAN;		
			}});
		cpcPanel.add(rdbtnEuclideanButton);
		
		final JRadioButton rdbtnPerceptedButton = new JRadioButton("percepted");
		rdbtnPerceptedButton.setToolTipText("Perception weighted distance");
		rdbtnPerceptedButton.setFont(new Font("Tahoma", Font.PLAIN, 16));
		rdbtnPerceptedButton.setBounds(222, 287, 113, 18);
		rdbtnPerceptedButton.setSelected(config.color_alg == NEAREST_COLOR.PERCEPTED);
		rdbtnPerceptedButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				config.color_alg = NEAREST_COLOR.PERCEPTED;		
			}});

		cpcPanel.add(rdbtnPerceptedButton);
		
		final JRadioButton rdbtnLumaButton = new JRadioButton("luma weighted");
		rdbtnLumaButton.setToolTipText("Luma weighted euclidean");
		rdbtnLumaButton.setFont(new Font("Tahoma", Font.PLAIN, 16));
		rdbtnLumaButton.setBounds(347, 287, 139, 18);
		rdbtnLumaButton.setSelected(config.color_alg == NEAREST_COLOR.LUMA_WEIGHTED);
		rdbtnLumaButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				config.color_alg = NEAREST_COLOR.LUMA_WEIGHTED;		
			}});

		cpcPanel.add(rdbtnLumaButton);
		
		final ButtonGroup groupDistance = new ButtonGroup();
		groupDistance.add(rdbtnEuclideanButton);
		groupDistance.add(rdbtnPerceptedButton);
		groupDistance.add(rdbtnLumaButton);
		
		final JRadioButton rdbtnAverageMergeButton = new JRadioButton("averge merge");
		rdbtnAverageMergeButton.setToolTipText("calculate average color");
		rdbtnAverageMergeButton.setBounds(111, 226, 113, 18);
		rdbtnAverageMergeButton.setSelected(config.pixel_merge == CPCConfig.PIXEL_MERGE.AVERAGE);
		rdbtnAverageMergeButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				config.pixel_merge = CPCConfig.PIXEL_MERGE.AVERAGE;		
			}});

		cpcPanel.add(rdbtnAverageMergeButton);		
		
		final JRadioButton rdbtnBrightestMergeRadioButton = new JRadioButton("brightest merge");
		rdbtnBrightestMergeRadioButton.setToolTipText("gets brightest pixel");
		rdbtnBrightestMergeRadioButton.setBounds(298, 228, 152, 18);
		rdbtnBrightestMergeRadioButton.setSelected(config.pixel_merge == CPCConfig.PIXEL_MERGE.BRIGHTEST);
		rdbtnBrightestMergeRadioButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				config.pixel_merge = CPCConfig.PIXEL_MERGE.BRIGHTEST;
			}});

		cpcPanel.add(rdbtnBrightestMergeRadioButton);
		
		final ButtonGroup groupMerge = new ButtonGroup();
		groupMerge.add(rdbtnAverageMergeButton);
		groupMerge.add(rdbtnBrightestMergeRadioButton);

		return cpcPanel;
	}
}

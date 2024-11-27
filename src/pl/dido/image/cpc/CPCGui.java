package pl.dido.image.cpc;

import java.awt.Canvas;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import pl.dido.image.GuiUtils;
import pl.dido.image.utils.ImageCanvas;
import pl.dido.image.utils.Config.DITHERING;

public class CPCGui {

	public static JPanel cpcTab(final CPCConfig config) {
		final JPanel cpcPanel = new JPanel();
		cpcPanel.setLayout(null);

		final JLabel lblDitherLabel = new JLabel("Dithering & aspect & scanline:");
		lblDitherLabel.setFont(GuiUtils.bold);
		lblDitherLabel.setBounds(20, 10, 250, 20);
		cpcPanel.add(lblDitherLabel);

		final JRadioButton rdbtnNoDitherButton = new JRadioButton("none");
		rdbtnNoDitherButton.setToolTipText("No dithering at all");
		rdbtnNoDitherButton.setFont(GuiUtils.std);
		rdbtnNoDitherButton.setBounds(20, 33, 50, 20);
		rdbtnNoDitherButton.setSelected(config.dither_alg == DITHERING.NONE);

		rdbtnNoDitherButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				config.dither_alg = DITHERING.NONE;
			}
		});

		cpcPanel.add(rdbtnNoDitherButton);

		final JRadioButton rdbtnBayerButton = new JRadioButton("bayer");
		rdbtnBayerButton.setToolTipText("Bayer ordered dithering");
		rdbtnBayerButton.setFont(GuiUtils.std);
		rdbtnBayerButton.setBounds(90, 33, 60, 20);
		rdbtnBayerButton.setSelected(config.dither_alg == DITHERING.BAYER4x4);

		rdbtnBayerButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				config.dither_alg = DITHERING.BAYER4x4;
			}
		});

		cpcPanel.add(rdbtnBayerButton);
		
		final JRadioButton rdbtnAtkinsonButton = new JRadioButton("apple");
		rdbtnAtkinsonButton.setToolTipText("Atkinson dithering, local color error");
		rdbtnAtkinsonButton.setFont(GuiUtils.std);
		rdbtnAtkinsonButton.setBounds(160, 33, 80, 20);
		rdbtnAtkinsonButton.setSelected(config.dither_alg == DITHERING.ATKINSON);

		rdbtnAtkinsonButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				config.dither_alg = DITHERING.ATKINSON;
			}
		});

		cpcPanel.add(rdbtnAtkinsonButton);
		
		final ButtonGroup groupDither = new ButtonGroup();
		groupDither.add(rdbtnNoDitherButton);
		groupDither.add(rdbtnBayerButton);
		groupDither.add(rdbtnAtkinsonButton);
		
		final JCheckBox chkReplaceBox = new JCheckBox("hi contrast");
		chkReplaceBox.setToolTipText("Replaces brightest and dimmest with white nad black");
		chkReplaceBox.setFont(GuiUtils.std);
		chkReplaceBox.setBounds(20, 60, 80, 20);
		chkReplaceBox.setSelected(config.replace_white);
		chkReplaceBox.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				config.replace_white = !config.replace_white;
			}
		});

		cpcPanel.add(chkReplaceBox);

		final JCheckBox chckbxRasterCheckBox = new JCheckBox("pal");
		chckbxRasterCheckBox.setToolTipText("Simple PAL emulation");
		chckbxRasterCheckBox.setFont(GuiUtils.std);
		chckbxRasterCheckBox.setBounds(100, 60, 50, 20);
		chckbxRasterCheckBox.setSelected(config.emuPAL);

		chckbxRasterCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				config.emuPAL = !config.emuPAL;
			}
		});

		cpcPanel.add(chckbxRasterCheckBox);
		
		final JCheckBox chckbxAspectCheckBox = new JCheckBox("aspect");
		chckbxAspectCheckBox.setToolTipText("Preserve orginal image aspect ratio");
		chckbxAspectCheckBox.setFont(GuiUtils.std);
		chckbxAspectCheckBox.setBounds(150, 60, 80, 20);
		chckbxAspectCheckBox.setSelected(config.preserveAspect);

		chckbxAspectCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				config.preserveAspect = !config.preserveAspect;
			}
		});

		cpcPanel.add(chckbxAspectCheckBox);


		final Canvas cpcLogo = new ImageCanvas("amstrad.png");
		cpcLogo.setBounds(290, 0, 200, 100);
		cpcPanel.add(cpcLogo);

		final JLabel lblConvertLabel = new JLabel("Converter mode:");
		lblConvertLabel.setFont(GuiUtils.bold);
		lblConvertLabel.setBounds(20, 95, 250, 20);
		cpcPanel.add(lblConvertLabel);

		final JRadioButton rdbtnHiresButton = new JRadioButton("320x200 mode1 - 4 colors");
		rdbtnHiresButton.setToolTipText("Mode 1, colors 211");
		rdbtnHiresButton.setFont(GuiUtils.std);
		rdbtnHiresButton.setBounds(46, 120, 250, 20);
		rdbtnHiresButton.setSelected(config.screen_mode == CPCConfig.SCREEN_MODE.MODE1);
		rdbtnHiresButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				config.screen_mode = CPCConfig.SCREEN_MODE.MODE1;
			}
		});

		cpcPanel.add(rdbtnHiresButton);

		final JRadioButton rdbtnMulticolorButton = new JRadioButton("160x200 mode0 - 16 colors");
		rdbtnMulticolorButton.setToolTipText("Mode 0, colors 322 + most popular");
		rdbtnMulticolorButton.setFont(GuiUtils.std);
		rdbtnMulticolorButton.setBounds(46, 145, 331, 20);
		rdbtnMulticolorButton.setSelected(config.screen_mode == CPCConfig.SCREEN_MODE.MODE0);
		rdbtnMulticolorButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				config.screen_mode = CPCConfig.SCREEN_MODE.MODE0;
			}
		});

		cpcPanel.add(rdbtnMulticolorButton);

		final ButtonGroup groupResolution = new ButtonGroup();
		groupResolution.add(rdbtnHiresButton);
		groupResolution.add(rdbtnMulticolorButton);

		final JRadioButton rdbtnAverageMergeButton = new JRadioButton("averge merge");
		rdbtnAverageMergeButton.setToolTipText("calculate average color");
		rdbtnAverageMergeButton.setFont(GuiUtils.bold);
		rdbtnAverageMergeButton.setBounds(111, 170, 113, 20);
		rdbtnAverageMergeButton.setSelected(config.pixel_merge == CPCConfig.PIXEL_MERGE.AVERAGE);
		rdbtnAverageMergeButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				config.pixel_merge = CPCConfig.PIXEL_MERGE.AVERAGE;
			}
		});

		cpcPanel.add(rdbtnAverageMergeButton);

		final JRadioButton rdbtnBrightestMergeRadioButton = new JRadioButton("brightest merge");
		rdbtnBrightestMergeRadioButton.setToolTipText("gets brightest pixel");
		rdbtnBrightestMergeRadioButton.setFont(GuiUtils.bold);
		rdbtnBrightestMergeRadioButton.setBounds(298, 170, 152, 20);
		rdbtnBrightestMergeRadioButton.setSelected(config.pixel_merge == CPCConfig.PIXEL_MERGE.BRIGHTEST);
		rdbtnBrightestMergeRadioButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				config.pixel_merge = CPCConfig.PIXEL_MERGE.BRIGHTEST;
			}
		});

		cpcPanel.add(rdbtnBrightestMergeRadioButton);

		final ButtonGroup groupMerge = new ButtonGroup();
		groupMerge.add(rdbtnAverageMergeButton);
		groupMerge.add(rdbtnBrightestMergeRadioButton);

		GuiUtils.addContrastControls(cpcPanel, config);
		GuiUtils.addColorControls(cpcPanel, config);

		return cpcPanel;
	}
}

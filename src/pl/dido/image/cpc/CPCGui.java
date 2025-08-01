package pl.dido.image.cpc;

import java.awt.Canvas;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import pl.dido.image.GuiUtils;
import pl.dido.image.utils.Config.DITHERING;
import pl.dido.image.utils.ImageCanvas;

public class CPCGui {

	public static JPanel cpcTab(final CPCConfig config) {
		final JPanel cpcPanel = new JPanel();
		cpcPanel.setLayout(null);
		
		GuiUtils.addDASControls(cpcPanel, config);

		final Canvas cpcLogo = new ImageCanvas("amstrad.png");
		cpcLogo.setBounds(310, 0, 200, 150);
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
				config.dither_alg = DITHERING.BAYER4x4;
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
				config.dither_alg = DITHERING.BLUE8x8;
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
		GuiUtils.addFiltersControls(cpcPanel, config);

		return cpcPanel;
	}
}

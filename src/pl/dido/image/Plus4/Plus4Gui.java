package pl.dido.image.plus4;

import java.awt.Canvas;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import pl.dido.image.GuiUtils;
import pl.dido.image.c64.C64Config.LUMA_PIXELS;
import pl.dido.image.c64.C64Config.PIXEL_MERGE;
import pl.dido.image.c64.C64Config.SCREEN_MODE;
import pl.dido.image.utils.ImageCanvas;

public class Plus4Gui {
	
	public static JPanel plus4Tab(final Plus4Config config) {
		final JPanel panelPlus4 = new JPanel();
		panelPlus4.setLayout(null);
		GuiUtils.addDASControls(panelPlus4, config);

		final JLabel lblConvertLabel = new JLabel("Converter mode:");
		lblConvertLabel.setFont(GuiUtils.bold);
		lblConvertLabel.setBounds(20, 70, 169, 14);
		panelPlus4.add(lblConvertLabel);

		final JRadioButton rdbtnHiresButton = new JRadioButton("320x200 hires - highest/lowest luminance");
		rdbtnHiresButton.setToolTipText(
				"High resolution mode. Two colors, first with most luminance and second with lowest");
		rdbtnHiresButton.setFont(GuiUtils.std);
		rdbtnHiresButton.setBounds(46, 85, 331, 23);
		rdbtnHiresButton.setSelected(config.screen_mode == SCREEN_MODE.HIRES);
		rdbtnHiresButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				config.screen_mode = SCREEN_MODE.HIRES;
			}});

		panelPlus4.add(rdbtnHiresButton);

		final JRadioButton rdbtnMulticolorButton = new JRadioButton("160x200 multicolor - color popularity");
		rdbtnMulticolorButton.setToolTipText(
				"Multicolour mode, colors are chosen by how frequently they apears");
		rdbtnMulticolorButton.setFont(GuiUtils.std);
		rdbtnMulticolorButton.setBounds(46, 143, 331, 23);
		rdbtnMulticolorButton.setSelected(config.screen_mode == SCREEN_MODE.MULTICOLOR);
		rdbtnMulticolorButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				config.screen_mode = SCREEN_MODE.MULTICOLOR;
			}});

		panelPlus4.add(rdbtnMulticolorButton);

		final ButtonGroup groupResolution = new ButtonGroup();
		groupResolution.add(rdbtnHiresButton);
		groupResolution.add(rdbtnMulticolorButton);

		final JRadioButton rdbtnOuterButton = new JRadioButton("8x8 outer pixels");
		rdbtnOuterButton.setToolTipText("Gets luminance values at the edges");
		rdbtnOuterButton.setFont(GuiUtils.bold);
		rdbtnOuterButton.setBounds(148, 112, 139, 23);
		rdbtnOuterButton.setSelected(config.luma_pixels == LUMA_PIXELS.OUTER);
		rdbtnOuterButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				config.luma_pixels = LUMA_PIXELS.OUTER;		
			}});

		panelPlus4.add(rdbtnOuterButton);

		final JRadioButton rdbtnInnerButton = new JRadioButton("8x8 even pixels");
		rdbtnInnerButton.setToolTipText("Gets luminance values of even pixels");
		rdbtnInnerButton.setFont(GuiUtils.bold);
		rdbtnInnerButton.setBounds(337, 112, 119, 23);
		rdbtnInnerButton.setSelected(config.luma_pixels == LUMA_PIXELS.INNER);
		rdbtnInnerButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				config.luma_pixels = LUMA_PIXELS.INNER;		
			}});
		
		panelPlus4.add(rdbtnInnerButton);

		final ButtonGroup groupLuminance = new ButtonGroup();
		groupLuminance.add(rdbtnOuterButton);
		groupLuminance.add(rdbtnInnerButton);

		final Canvas mesh1 = new ImageCanvas("mesh1.png");
		mesh1.setBounds(290, 95, 58, 57);
		panelPlus4.add(mesh1);

		final Canvas mesh2 = new ImageCanvas("mesh2.png");
		mesh2.setBounds(102, 95, 59, 57);
		panelPlus4.add(mesh2);

		final Canvas c64Logo = new ImageCanvas("plus4.png");
		c64Logo.setBounds(381, 7, 100, 96);
		panelPlus4.add(c64Logo);
		
		final JRadioButton rdbtnAverageMergeButton = new JRadioButton("averge merge");
		rdbtnAverageMergeButton.setBounds(111, 170, 113, 18);
		rdbtnAverageMergeButton.setFont(GuiUtils.bold);
		rdbtnAverageMergeButton.setToolTipText("calculate average color");
		rdbtnAverageMergeButton.setSelected(config.pixel_merge == PIXEL_MERGE.AVERAGE);
		rdbtnAverageMergeButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				config.pixel_merge = PIXEL_MERGE.AVERAGE;		
			}});

		panelPlus4.add(rdbtnAverageMergeButton);
				
		final JRadioButton rdbtnBrightestMergeRadioButton = new JRadioButton("brightest merge");
		rdbtnBrightestMergeRadioButton.setToolTipText("gets brightest pixel");
		rdbtnBrightestMergeRadioButton.setFont(GuiUtils.bold);
		rdbtnBrightestMergeRadioButton.setBounds(298, 170, 152, 18);
		rdbtnBrightestMergeRadioButton.setSelected(config.pixel_merge == PIXEL_MERGE.BRIGHTEST);
		rdbtnBrightestMergeRadioButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				config.pixel_merge = PIXEL_MERGE.BRIGHTEST;
			}});

		panelPlus4.add(rdbtnBrightestMergeRadioButton);
		
		final ButtonGroup groupMerge = new ButtonGroup();
		groupMerge.add(rdbtnAverageMergeButton);
		groupMerge.add(rdbtnBrightestMergeRadioButton);
		
		GuiUtils.addContrastControls(panelPlus4, config);
		GuiUtils.addColorControls(panelPlus4, config);
		GuiUtils.addFiltersControls(panelPlus4, config);

		return panelPlus4;
	}
}

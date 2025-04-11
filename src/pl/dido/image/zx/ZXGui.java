package pl.dido.image.zx;

import java.awt.Canvas;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JPanel;

import pl.dido.image.GuiUtils;
import pl.dido.image.utils.Config.DITHERING;
import pl.dido.image.utils.ImageCanvas;

public class ZXGui {
	
	public static JPanel zxTab(final ZXConfig config) {
		final JPanel panelZX = new JPanel();
		panelZX.setLayout(null);
		
		final JLabel lblAspectLabel = new JLabel("Aspect & scanline:");
		lblAspectLabel.setFont(GuiUtils.bold);
		lblAspectLabel.setBounds(20, 16, 200, 20);
		panelZX.add(lblAspectLabel);
		
		final JCheckBox chckbxRasterCheckBox = new JCheckBox("pal");
		chckbxRasterCheckBox.setToolTipText("Simple PAL emulation");
		chckbxRasterCheckBox.setFont(GuiUtils.std);
		chckbxRasterCheckBox.setBounds(20, 43, 50, 20);
		chckbxRasterCheckBox.setSelected(config.pal_view);
		
		chckbxRasterCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				config.pal_view = !config.pal_view;
			}});
		
		panelZX.add(chckbxRasterCheckBox);
		
		final JCheckBox chckbxAspectCheckBox = new JCheckBox("aspect");
		chckbxAspectCheckBox.setToolTipText("Preserve orginal image aspect ratio");
		chckbxAspectCheckBox.setFont(GuiUtils.std);
		chckbxAspectCheckBox.setBounds(80, 43, 100, 20);
		chckbxAspectCheckBox.setSelected(config.preserve_aspect);
		
		chckbxAspectCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				config.preserve_aspect = !config.preserve_aspect;
			}});
		
		panelZX.add(chckbxAspectCheckBox);
		
		final JLabel lblDitherLabel = new JLabel("Dithering:");
		lblDitherLabel.setFont(GuiUtils.bold);
		lblDitherLabel.setBounds(20, 73, 200, 20);
		panelZX.add(lblDitherLabel);
		
		final JRadioButton rdbtnNoDitherButton = new JRadioButton("none");
		rdbtnNoDitherButton.setToolTipText("No dithering at all");
		rdbtnNoDitherButton.setFont(GuiUtils.std);
		rdbtnNoDitherButton.setBounds(20, 100, 70, 20);
		rdbtnNoDitherButton.setSelected(config.dither_alg == DITHERING.NONE);
		
		rdbtnNoDitherButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				config.dither_alg = DITHERING.NONE;
			}});
		
		panelZX.add(rdbtnNoDitherButton);

		final JRadioButton rdbtnBayerButton = new JRadioButton("bayer");
		rdbtnBayerButton.setToolTipText("Enables bayer ordered dithering");
		rdbtnBayerButton.setFont(GuiUtils.std);
		rdbtnBayerButton.setBounds(90, 100, 70, 20);
		rdbtnBayerButton.setSelected(config.dither_alg == DITHERING.BAYER2x2);
		
		rdbtnBayerButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				config.dither_alg = DITHERING.BAYER2x2;
			}});
		
		panelZX.add(rdbtnBayerButton);
		
		final JRadioButton rdbtnAtkinsonButton = new JRadioButton("apple");
		rdbtnAtkinsonButton.setToolTipText("Enables atkinson dithering");
		rdbtnAtkinsonButton.setFont(GuiUtils.std);
		rdbtnAtkinsonButton.setBounds(160, 100, 70, 20);
		rdbtnAtkinsonButton.setSelected(config.dither_alg == DITHERING.ATKINSON);
		
		rdbtnAtkinsonButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				config.dither_alg = DITHERING.ATKINSON;
			}});
		
		panelZX.add(rdbtnAtkinsonButton);
		
		final ButtonGroup groupDither = new ButtonGroup();
		groupDither.add(rdbtnNoDitherButton);
		groupDither.add(rdbtnBayerButton);
		groupDither.add(rdbtnAtkinsonButton);
		
		final Canvas zxLogo = new ImageCanvas("sinclair.png");
		zxLogo.setBounds(230, 7, 252, 65);
		panelZX.add(zxLogo);
		
		GuiUtils.addContrastControls(panelZX, config);
		GuiUtils.addColorControls(panelZX, config);
		GuiUtils.addFiltersControls(panelZX, config);
		
		return panelZX;
	}
}
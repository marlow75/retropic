package pl.dido.image.cpc;

import java.awt.Canvas;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import pl.dido.image.GuiUtils;
import pl.dido.image.utils.ImageCanvas;

public class CPCGui {
	
	public static final String MODE0 = "PAL 160x200x16";
	public static final String MODE1 = "PAL 320x200x4";
	
	final private static String[] modesStrings = { MODE1, MODE0 };

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
		
		final JComboBox<String> modesList = new JComboBox<String>(modesStrings);
		modesList.setToolTipText("Choose available video mode");
		modesList.setFont(GuiUtils.std);
		
		modesList.setBounds(46, 140, 250, 20);
		modesList.addActionListener(new ActionListener() {
		
			public void actionPerformed(final ActionEvent e) {
				
				@SuppressWarnings("unchecked")
				final JComboBox<String> cb = (JComboBox<String>)e.getSource();
		        final String modeName = (String) cb.getSelectedItem();
		        
		        switch (modeName) {
		        case MODE0:
		        	config.screen_mode = CPCConfig.SCREEN_MODE.MODE0;
		        	break;
		        case MODE1: 
		        	config.screen_mode = CPCConfig.SCREEN_MODE.MODE1;
		        	break;
		        }
		}});
		
		cpcPanel.add(modesList);
		
		final JCheckBox chckbxQuantizerCheckBox = new JCheckBox("quantum quantizer");
		
		chckbxQuantizerCheckBox.setToolTipText("Enables quantum like color quantizer disables SOM");
		chckbxQuantizerCheckBox.setFont(GuiUtils.std);
		chckbxQuantizerCheckBox.setBounds(45, 115, 250, 20);
		chckbxQuantizerCheckBox.setSelected(config.fermionic_quantizer);
		
		chckbxQuantizerCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				config.fermionic_quantizer = !config.fermionic_quantizer;
			}});
		
		cpcPanel.add(chckbxQuantizerCheckBox);
		
		final JLabel lblPixelMergeLabel = new JLabel("160x200 merge mode:");
		lblPixelMergeLabel.setFont(GuiUtils.bold);
		lblPixelMergeLabel.setBounds(20, 175, 250, 20);
		cpcPanel.add(lblPixelMergeLabel);

		final JRadioButton rdbtnAverageMergeButton = new JRadioButton("averge merge");
		rdbtnAverageMergeButton.setToolTipText("calculate average color");
		rdbtnAverageMergeButton.setFont(GuiUtils.bold);
		rdbtnAverageMergeButton.setBounds(46, 195, 113, 20);
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
		rdbtnBrightestMergeRadioButton.setBounds(185, 195, 152, 20);
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
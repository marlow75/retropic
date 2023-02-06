package pl.dido.image.amiga;

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

public class AmigaGui {

	public static JPanel amigaTab(final AmigaConfig config) {
		final JPanel amigaPanel = new JPanel();
		amigaPanel.setLayout(null);

		final JCheckBox chckbxVividCheckBox = new JCheckBox("dithering");
		chckbxVividCheckBox.setToolTipText("Enables picture predithering");
		chckbxVividCheckBox.setFont(new Font("Tahoma", Font.PLAIN, 16));
		chckbxVividCheckBox.setBounds(20, 23, 171, 44);
		chckbxVividCheckBox.setSelected(config.vivid);
		
		chckbxVividCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				config.vivid = !config.vivid;
			}});
		
		amigaPanel.add(chckbxVividCheckBox);

		final Canvas amigaLogo = new ImageCanvas("amiga.png");
		amigaLogo.setBounds(340, 17, 200, 87);
		amigaPanel.add(amigaLogo);
		
		final JLabel lblConvertLabel = new JLabel("Converter mode:");
		lblConvertLabel.setFont(new Font("Tahoma", Font.BOLD, 16));
		lblConvertLabel.setBounds(20, 112, 250, 23);
		amigaPanel.add(lblConvertLabel);

		final JRadioButton rdbtnStdButton = new JRadioButton("PAL 320x256 - 32 colors");
		rdbtnStdButton.setToolTipText("Standard PAL, 32 colors from 4096 palette");
		rdbtnStdButton.setFont(new Font("Tahoma", Font.PLAIN, 16));
		rdbtnStdButton.setBounds(46, 130, 250, 57);
		rdbtnStdButton.setSelected(config.color_mode == AmigaConfig.COLOR_MODE.STD32);
		rdbtnStdButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				config.color_mode = AmigaConfig.COLOR_MODE.STD32;
			}});

		amigaPanel.add(rdbtnStdButton);
		
		final JRadioButton rdbtnHAMButton = new JRadioButton("PAL 320x256 - Hold And Modify (experimental)");
		rdbtnHAMButton.setToolTipText("Standard PAL HAM encoding");
		rdbtnHAMButton.setFont(new Font("Tahoma", Font.PLAIN, 16));
		rdbtnHAMButton.setBounds(46, 193, 400, 23);
		rdbtnHAMButton.setSelected(config.color_mode == AmigaConfig.COLOR_MODE.HAM6);
		rdbtnHAMButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				config.color_mode = AmigaConfig.COLOR_MODE.HAM6;
			}});

		amigaPanel.add(rdbtnHAMButton);
		
		final ButtonGroup groupColor = new ButtonGroup();
		groupColor.add(rdbtnStdButton);
		groupColor.add(rdbtnHAMButton);				
				
		final JLabel lblColorLabel = new JLabel("Color distance:");
		lblColorLabel.setFont(new Font("Tahoma", Font.BOLD, 16));
		lblColorLabel.setBounds(20, 256, 198, 16);
		amigaPanel.add(lblColorLabel);
		
		final JRadioButton rdbtnEuclideanButton = new JRadioButton("simple euclidean");
		rdbtnEuclideanButton.setToolTipText("Simple euclidean distance");
		rdbtnEuclideanButton.setFont(new Font("Tahoma", Font.PLAIN, 16));
		rdbtnEuclideanButton.setBounds(46, 287, 172, 18);
		rdbtnEuclideanButton.setSelected(config.color_alg == NEAREST_COLOR.EUCLIDEAN);
		rdbtnEuclideanButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				config.color_alg = NEAREST_COLOR.EUCLIDEAN;		
			}});
		amigaPanel.add(rdbtnEuclideanButton);
		
		final JRadioButton rdbtnPerceptedButton = new JRadioButton("percepted");
		rdbtnPerceptedButton.setToolTipText("Perception weighted distance");
		rdbtnPerceptedButton.setFont(new Font("Tahoma", Font.PLAIN, 16));
		rdbtnPerceptedButton.setBounds(222, 287, 113, 18);
		rdbtnPerceptedButton.setSelected(config.color_alg == NEAREST_COLOR.PERCEPTED);
		rdbtnPerceptedButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				config.color_alg = NEAREST_COLOR.PERCEPTED;		
			}});

		amigaPanel.add(rdbtnPerceptedButton);
		
		final JRadioButton rdbtnLumaButton = new JRadioButton("luma weighted");
		rdbtnLumaButton.setToolTipText("Luma weighted euclidean");
		rdbtnLumaButton.setFont(new Font("Tahoma", Font.PLAIN, 16));
		rdbtnLumaButton.setBounds(347, 287, 139, 18);
		rdbtnLumaButton.setSelected(config.color_alg == NEAREST_COLOR.LUMA_WEIGHTED);
		rdbtnLumaButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				config.color_alg = NEAREST_COLOR.LUMA_WEIGHTED;		
			}});

		amigaPanel.add(rdbtnLumaButton);
		
		final ButtonGroup groupDistance = new ButtonGroup();
		groupDistance.add(rdbtnEuclideanButton);
		groupDistance.add(rdbtnPerceptedButton);
		groupDistance.add(rdbtnLumaButton);
						
		return amigaPanel;
	}
}
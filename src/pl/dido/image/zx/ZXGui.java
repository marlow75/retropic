package pl.dido.image.zx;

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

public class ZXGui {
	
	public static JPanel zxTab(final ZXConfig config) {
		final JPanel panelZX = new JPanel();
		panelZX.setLayout(null);

		final JCheckBox chckbxVividCheckBox = new JCheckBox("try REAL colors");
		chckbxVividCheckBox.setToolTipText("Enables picture predithering");
		chckbxVividCheckBox.setFont(new Font("Tahoma", Font.PLAIN, 16));
		chckbxVividCheckBox.setBounds(20, 23, 171, 44);
		chckbxVividCheckBox.setSelected(config.vivid);
		
		chckbxVividCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				config.vivid = !config.vivid;				
			}});
		
		panelZX.add(chckbxVividCheckBox);
		
		final Canvas zxLogo = new ImageCanvas("sinclair.png");
		zxLogo.setBounds(230, 7, 252, 65);
		panelZX.add(zxLogo);
		
		final JLabel lblColorLabel = new JLabel("Color distance:");
		lblColorLabel.setFont(new Font("Tahoma", Font.BOLD, 16));
		lblColorLabel.setBounds(20, 256, 198, 16);
		panelZX.add(lblColorLabel);
		
		final JRadioButton rdbtnEuclideanButton = new JRadioButton("simple euclidean");
		rdbtnEuclideanButton.setToolTipText("Simple euclidean distance");
		rdbtnEuclideanButton.setFont(new Font("Tahoma", Font.PLAIN, 16));
		rdbtnEuclideanButton.setBounds(46, 287, 172, 18);
		rdbtnEuclideanButton.setSelected(config.color_alg == NEAREST_COLOR.EUCLIDEAN);
		rdbtnEuclideanButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				config.color_alg = NEAREST_COLOR.EUCLIDEAN;		
			}});
		panelZX.add(rdbtnEuclideanButton);
		
		final JRadioButton rdbtnPerceptedButton = new JRadioButton("percepted");
		rdbtnPerceptedButton.setToolTipText("Perception weighted distance");
		rdbtnPerceptedButton.setFont(new Font("Tahoma", Font.PLAIN, 16));
		rdbtnPerceptedButton.setBounds(222, 287, 113, 18);
		rdbtnPerceptedButton.setSelected(config.color_alg == NEAREST_COLOR.PERCEPTED);
		rdbtnPerceptedButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				config.color_alg = NEAREST_COLOR.PERCEPTED;		
			}});

		panelZX.add(rdbtnPerceptedButton);
		
		final JRadioButton rdbtnLumaButton = new JRadioButton("luma weighted");
		rdbtnLumaButton.setToolTipText("Luma weighted euclidean");
		rdbtnLumaButton.setFont(new Font("Tahoma", Font.PLAIN, 16));
		rdbtnLumaButton.setBounds(347, 287, 139, 18);
		rdbtnLumaButton.setSelected(config.color_alg == NEAREST_COLOR.LUMA_WEIGHTED);
		rdbtnLumaButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				config.color_alg = NEAREST_COLOR.LUMA_WEIGHTED;		
			}});

		panelZX.add(rdbtnLumaButton);
		
		final ButtonGroup groupDistance = new ButtonGroup();
		groupDistance.add(rdbtnEuclideanButton);
		groupDistance.add(rdbtnPerceptedButton);
		groupDistance.add(rdbtnLumaButton);		
		
		return panelZX;
	}
}

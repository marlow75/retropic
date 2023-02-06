package pl.dido.image.atari;

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

public class STGui {

	public static JPanel stTab(final STConfig config) {
		final JPanel stPanel = new JPanel();
		stPanel.setLayout(null);

		final JCheckBox chckbxVividCheckBox = new JCheckBox("VIVID colors");
		chckbxVividCheckBox.setToolTipText("Enables picture predithering");
		chckbxVividCheckBox.setFont(new Font("Tahoma", Font.PLAIN, 16));
		chckbxVividCheckBox.setBounds(20, 23, 171, 44);
		chckbxVividCheckBox.setSelected(config.vivid);
		
		chckbxVividCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				config.vivid = !config.vivid;
			}});
		
		stPanel.add(chckbxVividCheckBox);

		final Canvas stLogo = new ImageCanvas("st.png");
		stLogo.setBounds(281, 17, 200, 87);
		stPanel.add(stLogo);
		
		final JCheckBox chkReplaceBox = new JCheckBox("adjust contrast");
		chkReplaceBox.setToolTipText("Gives more/less contrast picture");
		chkReplaceBox.setFont(new Font("Tahoma", Font.PLAIN, 16));
		chkReplaceBox.setBounds(20, 76, 169, 23);
		chkReplaceBox.setSelected(config.replace_colors);
		chkReplaceBox.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				config.replace_colors = !config.replace_colors; 		
			}});

		stPanel.add(chkReplaceBox);
		
		final JLabel lblColorLabel = new JLabel("Color distance:");
		lblColorLabel.setFont(new Font("Tahoma", Font.BOLD, 16));
		lblColorLabel.setBounds(20, 256, 198, 16);
		stPanel.add(lblColorLabel);
		
		final JRadioButton rdbtnEuclideanButton = new JRadioButton("simple euclidean");
		rdbtnEuclideanButton.setToolTipText("Simple euclidean distance");
		rdbtnEuclideanButton.setFont(new Font("Tahoma", Font.PLAIN, 16));
		rdbtnEuclideanButton.setBounds(46, 287, 172, 18);
		rdbtnEuclideanButton.setSelected(config.color_alg == NEAREST_COLOR.EUCLIDEAN);
		rdbtnEuclideanButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				config.color_alg = NEAREST_COLOR.EUCLIDEAN;		
			}});
		stPanel.add(rdbtnEuclideanButton);
		
		final JRadioButton rdbtnPerceptedButton = new JRadioButton("percepted");
		rdbtnPerceptedButton.setToolTipText("Perception weighted distance");
		rdbtnPerceptedButton.setFont(new Font("Tahoma", Font.PLAIN, 16));
		rdbtnPerceptedButton.setBounds(222, 287, 113, 18);
		rdbtnPerceptedButton.setSelected(config.color_alg == NEAREST_COLOR.PERCEPTED);
		rdbtnPerceptedButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				config.color_alg = NEAREST_COLOR.PERCEPTED;		
			}});

		stPanel.add(rdbtnPerceptedButton);
		
		final JRadioButton rdbtnLumaButton = new JRadioButton("luma weighted");
		rdbtnLumaButton.setToolTipText("Luma weighted euclidean");
		rdbtnLumaButton.setFont(new Font("Tahoma", Font.PLAIN, 16));
		rdbtnLumaButton.setBounds(347, 287, 139, 18);
		rdbtnLumaButton.setSelected(config.color_alg == NEAREST_COLOR.LUMA_WEIGHTED);
		rdbtnLumaButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				config.color_alg = NEAREST_COLOR.LUMA_WEIGHTED;		
			}});

		stPanel.add(rdbtnLumaButton);
		
		final ButtonGroup groupDistance = new ButtonGroup();
		groupDistance.add(rdbtnEuclideanButton);
		groupDistance.add(rdbtnPerceptedButton);
		groupDistance.add(rdbtnLumaButton);
						
		return stPanel;
	}
}
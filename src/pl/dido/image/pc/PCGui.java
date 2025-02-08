package pl.dido.image.pc;

import java.awt.Canvas;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import pl.dido.image.GuiUtils;
import pl.dido.image.pc.PCConfig.NETWORK;
import pl.dido.image.pc.PCConfig.VIDEO_MODE;
import pl.dido.image.utils.ImageCanvas;

public class PCGui {
	
	final private static String[] modesStrings = { "CGA 80x25", "VESA 132x50" };

	public static JPanel pcTab(final PCConfig config) {
		final JPanel pcPanel = new JPanel();
		pcPanel.setLayout(null);
		GuiUtils.addDASControls(pcPanel, config);

		final JLabel lblModelLabel = new JLabel("Text mode:");
		lblModelLabel.setFont(GuiUtils.bold);
		lblModelLabel.setBounds(20, 76, 169, 14);
		pcPanel.add(lblModelLabel);
		
		final JComboBox<String> modesList = new JComboBox<String>(modesStrings);
		modesList.setToolTipText("Choose available video mode");
		modesList.setFont(GuiUtils.std);
		modesList.setBounds(46, 100, 150, 20);
		modesList.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				
				@SuppressWarnings("unchecked")
				final JComboBox<String> cb = (JComboBox<String>)e.getSource();
		        final String modeName = (String) cb.getSelectedItem();
		        
		        switch (modeName) {
		        case "CGA 80x25":
		        	config.video_mode = VIDEO_MODE.CGA_TEXT; 
		        	break;
		        case "VESA 132x50":
		        	config.video_mode = VIDEO_MODE.VESA_TEXT;
		        	break;
		        }
		}});
		
		pcPanel.add(modesList);
		
		final JLabel lblConvertLabel = new JLabel("Converter mode:");
		lblConvertLabel.setFont(GuiUtils.bold);
		lblConvertLabel.setBounds(20, 130, 169, 14);
		pcPanel.add(lblConvertLabel);

		final JRadioButton rdbtnL1Button = new JRadioButton("One hidden layer, semigraphics");
		rdbtnL1Button.setToolTipText("Simple and fast network architecture");
		rdbtnL1Button.setFont(GuiUtils.std);
		rdbtnL1Button.setBounds(46, 145, 331, 23);
		rdbtnL1Button.setSelected(config.network == NETWORK.L1);
		rdbtnL1Button.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				config.network = NETWORK.L1;
			}
		});

		pcPanel.add(rdbtnL1Button);

		final JRadioButton rdbtnL2Button = new JRadioButton("Two hidden layers, characters");
		rdbtnL2Button.setToolTipText("Robust but a kind of slow network architecture");
		rdbtnL2Button.setFont(GuiUtils.std);
		rdbtnL2Button.setBounds(46, 165, 331, 23);
		rdbtnL2Button.setSelected(config.network == NETWORK.L2);
		rdbtnL2Button.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				config.network = NETWORK.L2;
			}
		});

		pcPanel.add(rdbtnL2Button);
		
		final ButtonGroup groupResolution = new ButtonGroup();
		groupResolution.add(rdbtnL1Button);
		groupResolution.add(rdbtnL2Button);

		final Canvas c64Logo = new ImageCanvas("pc.png");
		c64Logo.setBounds(381, 7, 100, 96);
		
		pcPanel.add(c64Logo);
		
		GuiUtils.addContrastControls(pcPanel, config);
		GuiUtils.addColorControls(pcPanel, config);

		return pcPanel;
	}
}
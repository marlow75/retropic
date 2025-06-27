package pl.dido.image.pc;

import java.awt.Canvas;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import pl.dido.image.GuiUtils;
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
		
		final JLabel lblConvertLabel = new JLabel("lowpass threshold:");
		lblConvertLabel.setFont(GuiUtils.bold);
		lblConvertLabel.setBounds(20, 130, 169, 14);
		pcPanel.add(lblConvertLabel);
		
		final JSlider sldDetect = new JSlider(JSlider.HORIZONTAL, 0, 4, (int)config.lowpass_gain);
		sldDetect.setBounds(40, 146, 100, 35);
		sldDetect.setFont(GuiUtils.std);
		sldDetect.addChangeListener(new ChangeListener() {
			public void stateChanged(final ChangeEvent e) {
				final JSlider source = (JSlider) e.getSource();

				if (!source.getValueIsAdjusting())
					config.lowpass_gain = 1.2f * source.getValue();
			}
		});
		
		sldDetect.setMajorTickSpacing(2);
		sldDetect.setPaintLabels(true);
		pcPanel.add(sldDetect);
		
		final Canvas c64Logo = new ImageCanvas("pc.png");
		c64Logo.setBounds(381, 7, 100, 96);
		
		pcPanel.add(c64Logo);
		
		GuiUtils.addContrastControls(pcPanel, config);
		GuiUtils.addColorControls(pcPanel, config);
		GuiUtils.addFiltersControls(pcPanel, config);

		return pcPanel;
	}
}
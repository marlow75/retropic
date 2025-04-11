package pl.dido.image.amiga;

import java.awt.Canvas;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import pl.dido.image.GuiUtils;
import pl.dido.image.utils.ImageCanvas;

public class Amiga500Gui {
	
	public static final String PAL320x256x32 = "PAL 320x256x32";
	public static final String PAL320x512x32 = "PAL 320x512x32";
	
	public static final String PAL320x256_HAM = "PAL 320x256 HAM";
	public static final String PAL320x512_HAM = "PAL 320x512 HAM";
	
	final private static String[] modesStrings = { PAL320x256x32, PAL320x512x32, PAL320x256_HAM, PAL320x512_HAM };

	public static JPanel amigaTab(final Amiga500Config config) {
		final JPanel panelAmiga = new JPanel();
		panelAmiga.setLayout(null);
		GuiUtils.addDASControls(panelAmiga, config, true);		

		final Canvas amigaLogo = new ImageCanvas("amiga.png");
		amigaLogo.setBounds(340, 17, 200, 87);
		panelAmiga.add(amigaLogo);
		
		final JLabel lblConvertLabel = new JLabel("Converter mode:");
		lblConvertLabel.setFont(GuiUtils.bold);
		lblConvertLabel.setBounds(20, 85, 250, 20);
		panelAmiga.add(lblConvertLabel);

		final JComboBox<String> modesList = new JComboBox<String>(modesStrings);
		modesList.setToolTipText("Choose available video mode");
		modesList.setFont(GuiUtils.std);
		modesList.setBounds(46, 130, 250, 20);
		modesList.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				
				@SuppressWarnings("unchecked")
				final JComboBox<String> cb = (JComboBox<String>)e.getSource();
		        final String modeName = (String) cb.getSelectedItem();
		        
		        switch (modeName) {
		        case PAL320x256x32:
		        	config.video_mode = Amiga500Config.VIDEO_MODE.STD_320x256;
		        	break;
		        case PAL320x512x32:
		        	config.video_mode = Amiga500Config.VIDEO_MODE.STD_320x512;
		        	break;
		        case PAL320x256_HAM:
		        	config.video_mode = Amiga500Config.VIDEO_MODE.HAM6_320x256;
		        	break;
		        case PAL320x512_HAM:
		        	config.video_mode = Amiga500Config.VIDEO_MODE.HAM6_320x512;
		        	break;
		        }
		}});
		
		panelAmiga.add(modesList);
		panelAmiga.add(getRLECheckBox(config));
		
		GuiUtils.addContrastControls(panelAmiga, config);
		GuiUtils.addColorControls(panelAmiga, config);
		GuiUtils.addFiltersControls(panelAmiga, config);
						
		return panelAmiga;
	}
	
	public static Component getRLECheckBox(final AmigaConfig config) {
		final JCheckBox chckbxRLECheckBox = new JCheckBox("export with RLE compression");
		
		chckbxRLECheckBox.setToolTipText("Enables RLE compression");
		chckbxRLECheckBox.setFont(GuiUtils.std);
		chckbxRLECheckBox.setBounds(45, 105, 250, 20);
		chckbxRLECheckBox.setSelected(config.rleCompress);
		
		chckbxRLECheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				config.rleCompress = !config.rleCompress;
			}});
		
		return chckbxRLECheckBox;
	}
}
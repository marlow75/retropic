package pl.dido.image.amiga;

import java.awt.Canvas;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import pl.dido.image.GuiUtils;
import pl.dido.image.utils.ImageCanvas;

public class Amiga1200Gui {
	
	public static final String PAL320x256x256 = "PAL 320x256x256";
	public static final String PAL320x512x256 = "PAL 320x512x256";
	public static final String PAL640x512x256 = "PAL 640x512x256";
	
	public static final String PAL320x256_HAM = "PAL 320x256 HAM";
	public static final String PAL320x512_HAM = "PAL 320x512 HAM";
	public static final String PAL640x512_HAM = "PAL 640x512 HAM";
	
	final private static String[] modesStrings = { PAL320x256x256, PAL320x512x256, PAL640x512x256, 
			PAL320x256_HAM, PAL320x512_HAM, PAL640x512_HAM };

	public static JPanel amigaTab(final Amiga1200Config config) {
		final JPanel panelAmiga = new JPanel();
		panelAmiga.setLayout(null);
		GuiUtils.addDASControls(panelAmiga, config, false);		

		final Canvas amigaLogo = new ImageCanvas("amiga.png");
		amigaLogo.setBounds(340, 17, 200, 87);
		panelAmiga.add(amigaLogo);
		
		final JLabel lblConvertLabel = new JLabel("Converter mode:");
		lblConvertLabel.setFont(GuiUtils.bold);
		lblConvertLabel.setBounds(20, 90, 250, 20);
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
		        case PAL320x256x256:
		        	config.video_mode = Amiga1200Config.VIDEO_MODE.STD_320x256;
		        	break;
		        case PAL320x512x256:
		        	config.video_mode = Amiga1200Config.VIDEO_MODE.STD_320x512;
		        	break;
		        case PAL640x512x256:
		        	config.video_mode = Amiga1200Config.VIDEO_MODE.STD_640x512;
		        	break;
		        case PAL320x256_HAM:
		        	config.video_mode = Amiga1200Config.VIDEO_MODE.HAM8_320x256;
		        	break;
		        case PAL320x512_HAM:
		        	config.video_mode = Amiga1200Config.VIDEO_MODE.HAM8_320x512;
		        	break;
		        case PAL640x512_HAM:
		        	config.video_mode = Amiga1200Config.VIDEO_MODE.HAM8_640x512;
		        	break;
		        }
		}});
		
		panelAmiga.add(modesList);
		panelAmiga.add(Amiga500Gui.getRLECheckBox(config));
		
		GuiUtils.addContrastControls(panelAmiga, config);
		GuiUtils.addColorControls(panelAmiga, config);
						
		return panelAmiga;
	}
}
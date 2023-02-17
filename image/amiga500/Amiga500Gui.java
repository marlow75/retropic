package pl.dido.image.amiga500;

import java.awt.Canvas;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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
		GuiUtils.addDitheringControls(panelAmiga, config);		

		final Canvas amigaLogo = new ImageCanvas("amiga.png");
		amigaLogo.setBounds(340, 17, 200, 87);
		panelAmiga.add(amigaLogo);
		
		final JLabel lblConvertLabel = new JLabel("Converter mode:");
		lblConvertLabel.setFont(GuiUtils.bold);
		lblConvertLabel.setBounds(20, 112, 250, 23);
		panelAmiga.add(lblConvertLabel);

		final JComboBox<String> modesList = new JComboBox<String>(modesStrings);
		modesList.setToolTipText("Choose available video mode");
		modesList.setFont(GuiUtils.std);
		modesList.setBounds(46, 150, 250, 40);
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
		GuiUtils.addColorControls(panelAmiga, config);
						
		return panelAmiga;
	}
}
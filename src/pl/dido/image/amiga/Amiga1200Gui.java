package pl.dido.image.amiga;

import java.awt.Canvas;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import pl.dido.image.GuiUtils;
import pl.dido.image.utils.DitheringComboBox;
import pl.dido.image.utils.ImageCanvas;

public class Amiga1200Gui {
	
	public static final String PAL320x256x256 = "PAL 320x256x256";
	public static final String PAL320x512x256 = "PAL 320x512x256";
	public static final String PAL640x512x256 = "PAL 640x512x256";
	
	public static final String PAL320x256_HAM = "PAL 320x256 HAM";
	public static final String PAL320x512_HAM = "PAL 320x512 HAM";
	public static final String PAL640x512_HAM = "PAL 640x512 HAM";
	
	private final static String[] modesStrings = { PAL320x256x256, PAL320x512x256, PAL640x512x256, 
			PAL320x256_HAM, PAL320x512_HAM, PAL640x512_HAM };
	
	private static final boolean d256Setup[] = new boolean[] { true, true, true, true, true, true, true, true, true, true };
	private static final boolean dHAMSetup[] = new boolean[] { true, true, true, false, false, false, false, false, false, false };
	
	private static final String ditherOptions[] = new String[] { "none", "floyds", "apple", "noise", "bayer2x2", "bayer4x4", "bayer8x8", "bayer16x16", "blue8x8", "blue16x16" };
	private static DitheringComboBox cmbDithering;
	
	public static final JPanel addDASControls(final JPanel panel, final AmigaConfig config) {
		final JLabel lblDitherLabel = new JLabel("Dithering & aspect & pal & bw:");
		lblDitherLabel.setFont(GuiUtils.bold);
		lblDitherLabel.setBounds(20, 8, 200, 20);
		panel.add(lblDitherLabel);

		final JSlider sldError = new JSlider(JSlider.HORIZONTAL, 0, 4, config.error_threshold);
		cmbDithering = ditherOptions != null ? new DitheringComboBox(config, d256Setup) : new DitheringComboBox(config);
		
		cmbDithering.setToolTipText("Dithering options");
		cmbDithering.setFont(GuiUtils.std);
		cmbDithering.setBounds(46, 30, 100, 20);
		cmbDithering.setSelectedIndex(0);
		cmbDithering.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				sldError.setEnabled(cmbDithering.getSelectedIndex() > 2);
			}
		});

		cmbDithering.setSelectedIndex(config.dither_alg.ordinal());
		panel.add(cmbDithering);

		final JLabel errorLabel = new JLabel("error:");
		errorLabel.setFont(GuiUtils.bold);
		errorLabel.setBounds(210, 30, 30, 20);
		panel.add(errorLabel);

		sldError.setBounds(240, 30, 80, 30);
		sldError.setFont(GuiUtils.std);
		sldError.addChangeListener(new ChangeListener() {
			public void stateChanged(final ChangeEvent e) {
				final JSlider source = (JSlider) e.getSource();

				if (!source.getValueIsAdjusting())
					config.error_threshold = source.getValue();
			}
		});

		sldError.setMajorTickSpacing(2);
		sldError.setPaintLabels(true);
		panel.add(sldError);
		
		final JCheckBox chckbxAspectCheckBox = new JCheckBox("asp");
		chckbxAspectCheckBox.setToolTipText("Preserve orginal image aspect ratio");
		chckbxAspectCheckBox.setFont(GuiUtils.std);
		chckbxAspectCheckBox.setBounds(46, 60, 50, 20);
		chckbxAspectCheckBox.setSelected(config.preserve_aspect);

		chckbxAspectCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				config.preserve_aspect = !config.preserve_aspect;
			}
		});

		panel.add(chckbxAspectCheckBox);

		final JCheckBox chckbxBWCheckBox = new JCheckBox("bw");
		chckbxBWCheckBox.setToolTipText("Black/White PAL");
		chckbxBWCheckBox.setFont(GuiUtils.std);
		chckbxBWCheckBox.setBounds(146, 60, 40, 20);
		chckbxBWCheckBox.setSelected(config.black_white);

		chckbxBWCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				config.black_white = !config.black_white;
			}
		});

		panel.add(chckbxBWCheckBox);

		final JCheckBox chckbxPALCheckBox = new JCheckBox("pal");
		chckbxPALCheckBox.setToolTipText("Simple PAL emulation");
		chckbxPALCheckBox.setFont(GuiUtils.std);
		chckbxPALCheckBox.setBounds(96, 60, 40, 20);
		chckbxPALCheckBox.setSelected(config.pal_view);

		chckbxPALCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				config.pal_view = !config.pal_view;
				chckbxBWCheckBox.setEnabled(config.pal_view);
			}
		});

		panel.add(chckbxPALCheckBox);
		return panel;
	}

	public static JPanel amigaTab(final Amiga1200Config config) {
		final JPanel panelAmiga = new JPanel();
		panelAmiga.setLayout(null);
		addDASControls(panelAmiga, config);		

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
		        case PAL320x256x256:
		        	config.video_mode = Amiga1200Config.VIDEO_MODE.STD_320x256;
		        	cmbDithering.setEnablers(d256Setup);
		        	break;
		        case PAL320x512x256:
		        	config.video_mode = Amiga1200Config.VIDEO_MODE.STD_320x512;
		        	cmbDithering.setEnablers(d256Setup);
		        	break;
		        case PAL640x512x256:
		        	config.video_mode = Amiga1200Config.VIDEO_MODE.STD_640x512;
		        	cmbDithering.setEnablers(d256Setup);
		        	break;
		        case PAL320x256_HAM:
		        	config.video_mode = Amiga1200Config.VIDEO_MODE.HAM8_320x256;
		        	cmbDithering.setEnablers(dHAMSetup);
		        	break;
		        case PAL320x512_HAM:
		        	config.video_mode = Amiga1200Config.VIDEO_MODE.HAM8_320x512;
		        	cmbDithering.setEnablers(dHAMSetup);
		        	break;
		        case PAL640x512_HAM:
		        	config.video_mode = Amiga1200Config.VIDEO_MODE.HAM8_640x512;
		        	cmbDithering.setEnablers(dHAMSetup);
		        	break;
		        }
		}});
		
		panelAmiga.add(modesList);
		panelAmiga.add(Amiga500Gui.getRLECheckBox(config));
		
		GuiUtils.addContrastControls(panelAmiga, config);
		GuiUtils.addColorControls(panelAmiga, config);
		GuiUtils.addFiltersControls(panelAmiga, config);
						
		return panelAmiga;
	}
}
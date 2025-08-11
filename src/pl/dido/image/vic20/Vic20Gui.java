package pl.dido.image.vic20;

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
import pl.dido.image.utils.ImageCanvas;
import pl.dido.image.vic20.Vic20Config.VIDEO_MODE;

public class Vic20Gui {
	public static final String HIRES  = "Hires 174x184x2";
	public static final String LOWRES = "Lowres 88x184x4";
	
	final private static String[] modesStrings = { HIRES, LOWRES };
	
	public static JPanel vic20Tab(final Vic20Config config) {
		final JPanel vic20Panel = new JPanel();
		vic20Panel.setLayout(null);
		GuiUtils.addDASControls(vic20Panel, config, new boolean[] { true, true, true, true, false, false, false, false, false, false });
		
		final JLabel lblModelLabel = new JLabel("Generation mode:");
		lblModelLabel.setFont(GuiUtils.bold);
		lblModelLabel.setBounds(20, 86, 169, 14);
		vic20Panel.add(lblModelLabel);
		
		final JComboBox<String> modesList = new JComboBox<String>(modesStrings);
		modesList.setToolTipText("Choose charset mode");
		modesList.setFont(GuiUtils.std);
		modesList.setBounds(46, 110, 150, 20);
		modesList.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				
				@SuppressWarnings("unchecked")
				final JComboBox<String> cb = (JComboBox<String>)e.getSource();
		        final String modeName = (String) cb.getSelectedItem();
		        
		        switch (modeName) {
		        case HIRES:
		        	config.mode = VIDEO_MODE.HIRES;
		        	break;
		        case LOWRES:
		        	config.mode = VIDEO_MODE.LOWRES;
		        	break;
		        }
		}});
		
		vic20Panel.add(modesList);

		final JLabel lblConvertLabel = new JLabel("lowpass threshold:");
		lblConvertLabel.setFont(GuiUtils.bold);
		lblConvertLabel.setBounds(20, 146, 169, 14);
		vic20Panel.add(lblConvertLabel);
		
		final JSlider sldDetect = new JSlider(JSlider.HORIZONTAL, 0, 4, (int)config.lowpass_gain);
		sldDetect.setBounds(40, 166, 100, 35);
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
		vic20Panel.add(sldDetect);

		final Canvas vic20Logo = new ImageCanvas("vic20.png");
		vic20Logo.setBounds(335, 7, 150, 96);
		
		vic20Panel.add(vic20Logo);
		
		GuiUtils.addContrastControls(vic20Panel, config);
		GuiUtils.addColorControls(vic20Panel, config);
		GuiUtils.addFiltersControls(vic20Panel, config);

		return vic20Panel;
	}
}
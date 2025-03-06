package pl.dido.image.vic20;

import java.awt.Canvas;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import pl.dido.image.GuiUtils;
import pl.dido.image.petscii.PetsciiConfig.NETWORK;
import pl.dido.image.utils.ImageCanvas;
import pl.dido.image.vic20.Vic20Config.VIDEO_MODE;

public class Vic20Gui {
	public static final String TEXT   = "Text 22x23x2";
	public static final String HIRES  = "Hires 174x184x2";
	public static final String LOWRES = "Lowres 88x184x4";
	
	final private static String[] modesStrings = { TEXT, HIRES, LOWRES };
	
	public static JPanel vic20Tab(final Vic20Config config) {
		final JPanel vic20Panel = new JPanel();
		vic20Panel.setLayout(null);
		GuiUtils.addDASControls(vic20Panel, config);
		
		final JLabel lblModelLabel = new JLabel("Generation mode:");
		lblModelLabel.setFont(GuiUtils.bold);
		lblModelLabel.setBounds(20, 76, 169, 14);
		vic20Panel.add(lblModelLabel);
		
		final JComboBox<String> modesList = new JComboBox<String>(modesStrings);
		modesList.setToolTipText("Choose charset mode");
		modesList.setFont(GuiUtils.std);
		modesList.setBounds(46, 100, 150, 20);
		modesList.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				
				@SuppressWarnings("unchecked")
				final JComboBox<String> cb = (JComboBox<String>)e.getSource();
		        final String modeName = (String) cb.getSelectedItem();
		        
		        switch (modeName) {
		        case TEXT:
		        	config.mode = VIDEO_MODE.PETSCII; 
		        	break;
		        case HIRES:
		        	config.mode = VIDEO_MODE.HIRES;
		        	break;
		        case LOWRES:
		        	config.mode = VIDEO_MODE.LOWRES;
		        	break;
		        }
		}});
		
		vic20Panel.add(modesList);

		final JLabel lblConvertLabel = new JLabel("Converter mode:");
		lblConvertLabel.setFont(GuiUtils.bold);
		lblConvertLabel.setBounds(20, 130, 169, 14);
		vic20Panel.add(lblConvertLabel);

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

		vic20Panel.add(rdbtnL1Button);

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

		vic20Panel.add(rdbtnL2Button);
		
		final ButtonGroup groupResolution = new ButtonGroup();
		groupResolution.add(rdbtnL1Button);
		groupResolution.add(rdbtnL2Button);

		final Canvas c64Logo = new ImageCanvas("vic20.png");
		c64Logo.setBounds(335, 7, 150, 96);
		
		vic20Panel.add(c64Logo);
		
		GuiUtils.addContrastControls(vic20Panel, config);
		GuiUtils.addColorControls(vic20Panel, config);

		return vic20Panel;
	}
}
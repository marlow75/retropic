package pl.dido.image.utils;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import pl.dido.image.utils.Config.DITHERING;

public class DitheringComboBox extends JExComboBox implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3767692387095515209L;
	private Config config;
	
	private static final boolean standardSetup[] = new boolean[] { true, true, true, true, true, true, true, true, true, true };
	private static final String ditherOptions[] = new String[] { "none", "floyds", "apple", "noise", "bayer2x2", "bayer4x4", "bayer8x8", "bayer16x16", "blue8x8", "blue16x16" };

	public DitheringComboBox(final Config config, final boolean[] enabled) {
		super(ditherOptions, enabled);
		
		this.config = config;
		addActionListener(this);
	}

	public DitheringComboBox(final Config config) {
		super(ditherOptions, standardSetup);
		
		this.config = config;
		addActionListener(this);
	}
	
	public void actionPerformed(final ActionEvent e) {
		switch (getSelectedIndex()) {
		case 0:
			config.dither_alg = DITHERING.NONE;
			break;
		case 1:
			config.dither_alg = DITHERING.FLOYDS;
			break;
		case 2:
			config.dither_alg = DITHERING.ATKINSON;
			break;
		case 3:
			config.dither_alg = DITHERING.NOISE;
			break;
		case 4:
			config.dither_alg = DITHERING.BAYER2x2;
			break;
		case 5:
			config.dither_alg = DITHERING.BAYER4x4;
			break;
		case 6:
			config.dither_alg = DITHERING.BAYER8x8;
			break;
		case 7:
			config.dither_alg = DITHERING.BAYER16x16;
			break;
		case 8:
			config.dither_alg = DITHERING.BLUE8x8;
			break;
		case 9:
			config.dither_alg = DITHERING.BLUE16x16;
			break;
		}
	}
}

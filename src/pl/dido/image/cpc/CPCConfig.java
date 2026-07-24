package pl.dido.image.cpc;

import pl.dido.image.utils.Config;

public class CPCConfig extends Config {
	public enum SCREEN_MODE {
		MODE0, MODE1
	};
	
	public enum PIXEL_MERGE {
		AVERAGE, BRIGHTEST
	}
	
	public SCREEN_MODE screen_mode;
	public PIXEL_MERGE pixel_merge;
	
	public boolean replace_white;
	public boolean fermionic_quantizer;
	
	public CPCConfig() {	
		dither_alg = DITHERING.BAYER2x2;
		high_contrast = HIGH_CONTRAST.SWAHE;
		
		screen_mode = SCREEN_MODE.MODE1;
		pixel_merge = PIXEL_MERGE.AVERAGE;
		
		replace_white = false;
		color_alg = NEAREST_COLOR.PERCEPTED;
		
		pal_view = false;
		allow_palette_distance = true;
	}
	
	@Override
	public String getConfigString() {
		String configString = "";
		
		switch (screen_mode) {
		case MODE1:
			if (fermionic_quantizer)
				configString += "320x200x4 quantum ";
			else
				configString += "320x200x4 ";
			break;
		case MODE0:
			if (fermionic_quantizer)
				configString += "160x200x16 quantum ";
			else
				configString += "160x200x16 ";
			switch (pixel_merge) {
			case AVERAGE:
				configString += "average ";
				break;
			default:
				configString += "brightest ";
				break;
			}
			
			break;
		}
		
		return configString + super.getConfigString();
	}
	
	@Override
	public int getScreenHeight() {
		return 200;
	}

	@Override
	public int getScreenWidth() {
		return 320;
	}	
}
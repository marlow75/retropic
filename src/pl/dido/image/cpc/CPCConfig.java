package pl.dido.image.cpc;

import pl.dido.image.utils.Config;

public class CPCConfig extends Config {
	public enum SCREEN_MODE {
		MODE0, MODE1;
	};
	
	public enum PIXEL_MERGE {
		AVERAGE, BRIGHTEST
	}
	
	public SCREEN_MODE screen_mode;
	public PIXEL_MERGE pixel_merge;
	public boolean replace_white;
	
	public CPCConfig() {	
		dither_alg = DITHERING.ATKINSON;
		
		dithering = true;
		highContrast = HIGH_CONTRAST.SWAHE;
		
		screen_mode = SCREEN_MODE.MODE1;
		pixel_merge = PIXEL_MERGE.AVERAGE;
		
		replace_white = false;
		color_alg = NEAREST_COLOR.PERCEPTED;
	}
	
	@Override
	public String getConfigString() {
		String configString = "";
		switch (screen_mode) {
		case MODE1:
			configString += "320x200x4 ";
			break;
		default:
			configString += "160x200x16 ";
			switch (pixel_merge) {
			case AVERAGE:
				configString += "average pixel ";
				break;
			default:
				configString += "brightest pixel ";
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
	@Override
	public int getWindowHeight() {
		if (scanline)
			return 400;
		
		return 300;
	}

	@Override
	public int getWindowWidth() {
		if (scanline)
			return 640;
		
		return 480;
	}
}
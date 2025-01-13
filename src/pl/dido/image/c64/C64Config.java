package pl.dido.image.c64;

import pl.dido.image.utils.Config;

public class C64Config extends Config {
	public enum SCREEN_MODE {
		HIRES, MULTICOLOR;
	};
	
	public enum LUMA_PIXELS {
		INNER, OUTER;
	};
	
	public enum PIXEL_MERGE {
		AVERAGE, BRIGHTEST
	}
	
	public SCREEN_MODE screen_mode;	
	public LUMA_PIXELS luma_pixels;
	public PIXEL_MERGE pixel_merge; 
	
	public C64Config() {
		super();
		screen_mode = SCREEN_MODE.HIRES;
		color_alg = NEAREST_COLOR.PERCEPTED;		
		
		high_contrast = HIGH_CONTRAST.SWAHE;
		dither_alg = DITHERING.ATKINSON;
		
		luma_pixels = LUMA_PIXELS.OUTER;		
		pixel_merge = PIXEL_MERGE.AVERAGE;
		
		filter = true;
	}
	
	public String getColorsNumber() {
		String configString = "";
		
		switch (screen_mode) {
		case HIRES:
			configString += "x2 ";
			break;
		case MULTICOLOR:
			configString += "x4 ";
		}
		
		return configString;
	}
	
	@Override
	public String getConfigString() {
		String configString = "";
		
		switch (screen_mode) {
		case HIRES:
			configString += "320x200" + getColorsNumber();
			break;
		default:
			configString += "160x200" + getColorsNumber();
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
	public int getScreenWidth() {
		return 320;
	}

	@Override
	public int getScreenHeight() {
		return 200;
	}
}
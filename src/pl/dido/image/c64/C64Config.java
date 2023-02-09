package pl.dido.image.c64;

import pl.dido.image.Config;

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
		
		dithering = true;
		luma_pixels = LUMA_PIXELS.OUTER;
		
		pixel_merge = PIXEL_MERGE.AVERAGE;
	}
	
	@Override
	public String getConfigString() {
		String configString = "";
		
		switch (screen_mode) {
		case HIRES:
			configString += "320x200x2 ";
			break;
		default:
			configString += "160x200x4 ";
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
}
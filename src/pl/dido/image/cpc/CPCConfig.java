package pl.dido.image.cpc;

import pl.dido.image.Config;

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
		color_alg = NEAREST_COLOR.PERCEPTED;			
		vivid = true;
		
		screen_mode = SCREEN_MODE.MODE1;
		pixel_merge = PIXEL_MERGE.AVERAGE;
		
		replace_white = false;
	}
}
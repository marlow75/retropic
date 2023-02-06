package pl.dido.image.amiga;

import pl.dido.image.Config;

public class AmigaConfig extends Config {
	
	public enum COLOR_MODE {
		STD32, HAM6;
	};
	
	public COLOR_MODE color_mode;
	
	public AmigaConfig() {
		super();
		color_mode = COLOR_MODE.STD32;
		vivid = true;
	}
}
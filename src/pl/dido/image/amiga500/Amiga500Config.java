package pl.dido.image.amiga500;

import pl.dido.image.Config;

public class Amiga500Config extends Config {
	
	public enum COLOR_MODE {
		STD32, HAM6;
	};
	
	public COLOR_MODE color_mode;
	
	public Amiga500Config() {
		super();
		color_mode = COLOR_MODE.STD32;
		dithering = true;
	}

	@Override
	public String getConfigString() {
		String configString = "";
		
		switch (color_mode) {
		case STD32:
			configString += "x32 ";
			break;
		case HAM6:
			configString += " HAM ";
			break;
		}

		return configString + super.getConfigString();
	}
}
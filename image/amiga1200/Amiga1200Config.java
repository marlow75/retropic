package pl.dido.image.amiga1200;

import pl.dido.image.Config;

public class Amiga1200Config extends Config {
	
	public enum COLOR_MODE {
		STD256, HAM8;
	};
	
	public COLOR_MODE color_mode;
	
	public Amiga1200Config() {
		super();

		color_mode = COLOR_MODE.STD256;
		dithering = true;
	}

	@Override
	public String getConfigString() {
		String configString = "";		
		
		switch (color_mode) {
		case STD256:
			configString += "x256 " + super.getConfigString();
			
			break;
		case HAM8:
			configString += " HAM8 ";

			switch (color_alg) {
			case EUCLIDEAN:
				configString += "euclidean";
				break;
			case LUMA_WEIGHTED:
				configString += "luma";
				break;
			case PERCEPTED:
				configString += "percepted";
				break;
			}

			break; 
		}
		
		return configString;
	}
}
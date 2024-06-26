package pl.dido.image.c64;

public class C64ExtraConfig extends C64Config {

	public enum EXTRA_MODE {
		HIRES_INTERLACED, MULTI_COLOR_INTERLACED;
	}
	
	public enum RGB_APPROXIMATION {
		LINEAR, CUBE;
	}
	
	public int luma_threshold;
	public int error_threshold;
	
	public RGB_APPROXIMATION rgb_approximation;
	public EXTRA_MODE extra_mode;
	
	public C64ExtraConfig() {
		super();
		
		extra_mode = EXTRA_MODE.HIRES_INTERLACED;
		luma_threshold = 32;
		
		rgb_approximation = RGB_APPROXIMATION.LINEAR;
		error_threshold = 2;
	}
	
	@Override
	public String getColorsNumber() {
		String configString = "";
		
		switch (extra_mode) {
		case HIRES_INTERLACED:
			configString += "x3 ";
			break;
		case MULTI_COLOR_INTERLACED:
			configString += "x10 ";
		default:
			break;
		}
		
		return configString;
	}

	@Override
	public String getConfigString() {
		String configString = "";
		
		switch (extra_mode) {
		case HIRES_INTERLACED:
			configString += "laced ";
			break;
		case MULTI_COLOR_INTERLACED:
			configString += "MCI ";
		default:
			break;
		}
		
		return configString + super.getConfigString();
	}
}

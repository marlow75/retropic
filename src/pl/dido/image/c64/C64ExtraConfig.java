package pl.dido.image.c64;

public class C64ExtraConfig extends C64Config {

	public enum EXTRA_MODE {
		HIRES_INTERLACED, MULTI_COLOR_INTERLACED;
	}
	
	public int luma_threshold;
	public EXTRA_MODE extra_mode;
	
	public C64ExtraConfig() {
		super();
		
		extra_mode = EXTRA_MODE.MULTI_COLOR_INTERLACED;
		luma_threshold = 6;
	}
	
	@Override
	public String getColorsNumber() {
		String configString = "";
		
		switch (extra_mode) {
		case HIRES_INTERLACED:
			configString += "x4 ";
			break;
		case MULTI_COLOR_INTERLACED:
			configString += "x16 ";
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

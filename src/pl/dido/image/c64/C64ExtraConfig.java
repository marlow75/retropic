package pl.dido.image.c64;

public class C64ExtraConfig extends C64Config {

	public enum EXTRA_MODE {
		HIRES_INTERLACED, MULTI_COLOR_INTERLACED;
	}
	
	public int lumaThreshold;
	public EXTRA_MODE extraMode;
	
	public C64ExtraConfig() {
		super();
		
		extraMode = EXTRA_MODE.MULTI_COLOR_INTERLACED;
		lumaThreshold = 11;
	}

	@Override
	public String getConfigString() {
		String configString = "";
		
		switch (screen_mode) {
		case HIRES:
			configString += "laced ";
			break;
		case MULTICOLOR:
			configString += "MCI ";
		default:
			break;
		}
		
		return configString + super.getConfigString();
	}
}

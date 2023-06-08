package pl.dido.image;

public class Config implements Cloneable {
	
	public enum HIGH_CONTRAST {
		HE, SWAHE, NONE;
	}

	public enum DITHERING {
		STD_FS, ATKINSON
	};

	public enum NEAREST_COLOR {
		EUCLIDEAN, PERCEPTED, LUMA_WEIGHTED
	};

	public boolean dithering;
	public HIGH_CONTRAST highContrast;
	
	public int swaheWindowSize;
	public float swaheBrightness;
	
	public static String default_path;
	public static String export_path;

	public DITHERING dither_alg;
	public NEAREST_COLOR color_alg;

	public Config() {
		dither_alg = DITHERING.STD_FS;
		color_alg = NEAREST_COLOR.PERCEPTED;

		dithering = false;
		highContrast = HIGH_CONTRAST.NONE;
		
		swaheWindowSize = 40;
		swaheBrightness = 1f;

		default_path = "pic";
		export_path = "export";
	}
	
	public String getConfigString() {
		String configString = "";
		
		if (dithering)
			switch (dither_alg) {
			case ATKINSON:
				configString += "apple ";
				break;
			case STD_FS:
				configString += "floyds ";
				break;
			}

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
		
		switch (highContrast) {
		case HE:
			configString += " HE ";
			break;
		case SWAHE:
			configString += " SWAHE W" + this.swaheWindowSize + " B" + this.swaheBrightness + " ";
		default:
			break;
		}
		
		return configString;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
}
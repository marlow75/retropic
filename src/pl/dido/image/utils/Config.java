package pl.dido.image.utils;

abstract public class Config implements Cloneable {
	
	public enum HIGH_CONTRAST {
		HE, SWAHE, NONE, CLAHE;
	}

	public enum DITHERING {
		STD_FS, ATKINSON
	};

	public enum NEAREST_COLOR {
		EUCLIDEAN, PERCEPTED, LUMA_WEIGHTED
	};

	public boolean dithering;
	public boolean keepAspect; 
	
	public HIGH_CONTRAST highContrast;	
	public int windowSize;
	public int details;
	
	public static String fileName;
	public static String export_path;

	public DITHERING dither_alg;
	public NEAREST_COLOR color_alg;

	public Config() {
		dither_alg = DITHERING.STD_FS;
		color_alg = NEAREST_COLOR.PERCEPTED;

		dithering = false;
		keepAspect = false;
		
		highContrast = HIGH_CONTRAST.NONE;		
		windowSize = 40;
		details = 1;

		export_path = "export";
	}	
	
	public abstract int getWidth();
	public abstract int getHeight();
	
	public abstract int getScreenWidth();
	public abstract int getScreenHeight();
	
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
		case CLAHE:
			configString += " CLAHE D" + this.details + " ";
			break;
		case SWAHE:
			configString += " SWAHE W" + this.windowSize + " D" + this.details + " ";
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
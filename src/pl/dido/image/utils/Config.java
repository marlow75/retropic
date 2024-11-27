package pl.dido.image.utils;

abstract public class Config implements Cloneable {

	public enum HIGH_CONTRAST {
		HE, SWAHE, NONE, CLAHE;
	}

	public enum DITHERING {
		NONE, FLOYDS, ATKINSON, BAYER2x2, BAYER4x4, BAYER8x8, BAYER16x16
	};

	public enum NEAREST_COLOR {
		EUCLIDEAN, PERCEPTED, LUMA_WEIGHTED
	};

	public boolean preserveAspect;
	public boolean emuPAL;

	public HIGH_CONTRAST highContrast;
	public int windowSize;
	
	public int details;
	public int error_threshold;
	
	public static String export_path;

	public DITHERING dither_alg;
	public NEAREST_COLOR color_alg;

	public Config() {
		dither_alg = DITHERING.ATKINSON;
		color_alg = NEAREST_COLOR.PERCEPTED;

		preserveAspect = false;

		highContrast = HIGH_CONTRAST.NONE;
		windowSize = 40;

		details = 3;
		export_path = "export";

		emuPAL = true;
		error_threshold = 4;
	}

	public abstract int getScreenWidth();

	public abstract int getScreenHeight();

	public int getWindowHeight() {
		return 457;
	}

	public int getWindowWidth() {
		return 540;
	}

	public String getConfigString() {
		String configString = "";

		switch (dither_alg) {
		case ATKINSON:
			configString += "apple ";
			break;
		case FLOYDS:
			configString += "floyds ";
			break;
		case BAYER2x2: 
		case BAYER4x4:
		case BAYER8x8:
		case BAYER16x16:
			configString += "bayer ";
			break;
		default:
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
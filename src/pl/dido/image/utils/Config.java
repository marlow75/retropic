package pl.dido.image.utils;

abstract public class Config implements Cloneable {
	
	public enum FILTER {
		NONE, LOWPASS, SHARPEN, EMBOSS, EDGES_BLEND;
	}

	public enum HIGH_CONTRAST {
		HE, SWAHE, NONE, CLAHE;
	}

	public enum DITHERING {
		NONE, FLOYDS, ATKINSON, BAYER2x2, BAYER4x4, 
		BAYER8x8, BAYER16x16, NOISE8x8, NOISE16x16
	};

	public enum NEAREST_COLOR {
		EUCLIDEAN, PERCEPTED, LUMA_WEIGHTED
	};

	public boolean preserve_aspect;
	public boolean pal_view;

	public HIGH_CONTRAST high_contrast;
	public int window_size;
	
	public int details;
	public int error_threshold;
	
	public static String export_path;
	public FILTER filter;
	
	public float lowpass_gain;
	public boolean denoise;
	
	public DITHERING dither_alg;
	public NEAREST_COLOR color_alg;
	
	public boolean black_white;
	public boolean allow_luminance;
	
	public Config() {
		dither_alg = DITHERING.ATKINSON;
		color_alg = NEAREST_COLOR.PERCEPTED;

		preserve_aspect = false;
		black_white = false;
		
		high_contrast = HIGH_CONTRAST.NONE;
		window_size = 40;

		details = 3;
		export_path = "export";

		pal_view = true;
		filter = FILTER.NONE;
		
		error_threshold = 0;
		allow_luminance = true;
		
		lowpass_gain = 1.1f;
		denoise = false;
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
		case NOISE8x8:
		case NOISE16x16:
			configString += "noise ";
			break;
		default:
			break;
		}

		switch (high_contrast) {
		case HE:
			configString += "HE ";
			break;
		case CLAHE:
			configString += "CLAHE D" + this.details + " ";
			break;
		case SWAHE:
			configString += "SWAHE W" + this.window_size + " D" + this.details + " ";
		default:
			break;
		}

		switch (color_alg) {
		case EUCLIDEAN:
			configString += "euclidean ";
			break;
		case LUMA_WEIGHTED:
			configString += "luma ";
			break;
		case PERCEPTED:
			configString += "percepted ";
			break;
		}

		switch (filter) {
		case EDGES_BLEND:
			configString += "edge ";
			break;
		case EMBOSS:
			configString += "emboss ";
			break;
		case LOWPASS:
			configString += "lowpass ";
			break;
		case NONE:
			break;
		case SHARPEN:
			configString += "sharpen ";
			break;
		}
		
		configString += denoise ? "denoiser " : "";
		return configString;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
}
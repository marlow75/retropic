package pl.dido.image.vic20;

import pl.dido.image.petscii.PetsciiConfig;

public class Vic20Config extends PetsciiConfig {
	public enum VIDEO_MODE { PETSCII, HIRES, LOWRES };
	
	public VIDEO_MODE mode;
	
	public Vic20Config() {
		super();
		
		mode = VIDEO_MODE.HIRES;
		dither_alg = DITHERING.NONE;
		
		filter = FILTER.SHARPEN;
		high_contrast = HIGH_CONTRAST.SWAHE;
		
		allow_palette = true;
	}
		
	@Override
	public String getConfigString() {
		String configString;
		
		switch (mode) {
		default:
			configString = "176x184x2 ";
			break;
		case LOWRES:
			configString = "88x184x4 ";
			break;
		}
		
		if (posterize_level > 0)
			configString += "P" + posterize_level + " ";
		
		return configString;
	}

	@Override
	public int getScreenWidth() {
		return 176;
	}

	@Override
	public int getScreenHeight() {
		return 184;
	}	
}
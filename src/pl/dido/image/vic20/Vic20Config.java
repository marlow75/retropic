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
	}
		
	@Override
	public String getConfigString() {
		switch (mode) {
		default:
			return "176x184x2 ";
		case LOWRES:
			return "88x184x4 ";
		}
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
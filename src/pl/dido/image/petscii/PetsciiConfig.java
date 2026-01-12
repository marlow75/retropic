package pl.dido.image.petscii;

import pl.dido.image.utils.Config;

public class PetsciiConfig extends Config {
	public PetsciiConfig() {
		super();
		
		color_alg = NEAREST_COLOR.PERCEPTED;		
		dither_alg = DITHERING.NONE;
		
		high_contrast = HIGH_CONTRAST.NONE;
		filter = FILTER.NONE;
		
		filter = FILTER.LOWPASS;
		allow_palette = true;
	}
	
	@Override
	public String getConfigString() {
		return "40x25x2 " + super.getConfigString();
	}

	@Override
	public int getScreenWidth() {
		return 320;
	}

	@Override
	public int getScreenHeight() {
		return 200;
	}
}
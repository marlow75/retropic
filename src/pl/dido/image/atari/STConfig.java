package pl.dido.image.atari;

import pl.dido.image.utils.Config;

public class STConfig extends Config {
	
	public STConfig() {
		super();
		
		pal_view = false;
		dither_alg = DITHERING.BAYER4x4;
	}

	@Override
	public int getScreenHeight() {
		return 200;
	}

	@Override
	public int getScreenWidth() {
		return 320;
	}
}

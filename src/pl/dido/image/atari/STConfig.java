package pl.dido.image.atari;

import pl.dido.image.utils.Config;

public class STConfig extends Config {
	
	public STConfig() {
		super();
		dither_alg = DITHERING.BAYER4x4;
		error_threshold = 3;
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

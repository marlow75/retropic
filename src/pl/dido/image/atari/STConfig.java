package pl.dido.image.atari;

import pl.dido.image.utils.Config;

public class STConfig extends Config {
	
	public STConfig() {
		super();
		
		dither_alg = Config.DITHERING.STD_FS;
		dithering = true;
	}

	@Override
	public int getScreenHeight() {
		return 200;
	}

	@Override
	public int getScreenWidth() {
		return 320;
	}
	
	@Override
	public int getWindowHeight() {
		return 400;
	}

	@Override
	public int getWindowWidth() {
		return 640;
	}
}

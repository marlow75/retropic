package pl.dido.image.atari;

import pl.dido.image.utils.Config;

public class STConfig extends Config {
	
	public STConfig() {
		super();
		
		dither_alg = Config.DITHERING.STD_FS;
		dithering = true;
	}

	@Override
	public int getHeight() {
		return 200;
	}

	@Override
	public int getWidth() {
		return 320;
	}
	
	@Override
	public int getScreenHeight() {
		return 300;
	}

	@Override
	public int getScreenWidth() {
		return 480;
	}
}

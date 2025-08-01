package pl.dido.image.c128;

import pl.dido.image.utils.Config;

public class C128Config extends Config {
	
	public C128Config() {
		super();
		pal_view = false;
		dither_alg = DITHERING.NOISE;
	}
	
	@Override
	public String getConfigString() {
		final String configString = "640x200x2 ";	
		return configString + super.getConfigString();
	}

	@Override
	public int getScreenWidth() {
		return 640;
	}

	@Override
	public int getScreenHeight() {
		return 200;
	}
	
	public int getWindowHeight() {
		return 400;
	}

	public int getWindowWidth() {
		return 640;
	}
}

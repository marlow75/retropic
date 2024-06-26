package pl.dido.image.zx;

import pl.dido.image.utils.Config;

public class ZXConfig extends Config {

	
	public ZXConfig() {
		super();
		color_alg = NEAREST_COLOR.PERCEPTED;
		dither_alg = DITHERING.BAYER;
		
		highContrast = HIGH_CONTRAST.SWAHE;
		windowSize = 20;
	}

	@Override
	public int getScreenWidth() {
		return 256;
	}

	@Override
	public int getScreenHeight() {
		return 192;
	}
	
	@Override
	public int getWindowWidth() {
		return 512;
	}

	@Override
	public int getWindowHeight() {
		return 384;
	}
}
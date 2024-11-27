package pl.dido.image.zx;

import pl.dido.image.utils.Config;

public class ZXConfig extends Config {
	
	public ZXConfig() {
		super();
		color_alg = NEAREST_COLOR.PERCEPTED;
		dither_alg = DITHERING.BAYER2x2;
		
		highContrast = HIGH_CONTRAST.SWAHE;
		windowSize = 20;
		
		error_threshold = 4;
	}

	@Override
	public int getScreenWidth() {
		return 256;
	}

	@Override
	public int getScreenHeight() {
		return 192;
	}	
}
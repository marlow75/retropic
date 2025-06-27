package pl.dido.image.zx;

import pl.dido.image.utils.Config;

public class ZXConfig extends Config {
	
	public ZXConfig() {
		super();
		color_alg = NEAREST_COLOR.PERCEPTED;
		dither_alg = DITHERING.BAYER2x2;
		
		high_contrast = HIGH_CONTRAST.SWAHE;
		window_size = 30;
		
		allow_luminance = false;
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
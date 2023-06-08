package pl.dido.image.zx;

import pl.dido.image.Config;

public class ZXConfig extends Config {	
	public ZXConfig() {
		super();
		color_alg = NEAREST_COLOR.PERCEPTED;
		dither_alg = DITHERING.ATKINSON;
		
		dithering = true;
		highContrast = HIGH_CONTRAST.SWAHE;
		
		swaheWindowSize = 20;
	}
}
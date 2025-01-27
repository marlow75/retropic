package pl.dido.image.pc;

import pl.dido.image.utils.Config;

public class CGAConfig extends Config {
	
	public enum NETWORK {
		L1, L2;
	};
	
	public NETWORK network;
		
	public CGAConfig() {
		super();
		
		network = NETWORK.L1;
		color_alg = NEAREST_COLOR.PERCEPTED;		
		dither_alg = DITHERING.NONE;
		
		high_contrast = HIGH_CONTRAST.NONE;
		error_threshold = 3;
		
		filter = true;
	}
	
	@Override
	public String getConfigString() {
		final String n;
		
		switch (network) {
		case L1:
			n = "L1 ";
			break;
		default:
			n = "L2 ";
			break;
		}
		
		return "80x25x2 " + n + super.getConfigString();
	}

	@Override
	public int getScreenWidth() {
		return 640;
	}

	@Override
	public int getScreenHeight() {
		return 200;
	}
}
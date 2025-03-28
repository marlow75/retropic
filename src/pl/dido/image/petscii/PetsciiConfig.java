package pl.dido.image.petscii;

import pl.dido.image.utils.Config;

public class PetsciiConfig extends Config {
	
	public enum NETWORK {
		L1, L2;
	};
	
	public NETWORK network;
		
	public PetsciiConfig() {
		super();
		
		network = NETWORK.L1;
		color_alg = NEAREST_COLOR.PERCEPTED;		
		dither_alg = DITHERING.NONE;
		
		high_contrast = HIGH_CONTRAST.NONE;
		error_threshold = 3;
		
		low_pass_filter = true;
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
		
		return "40x25x2 " + n + super.getConfigString();
	}

	@Override
	public int getScreenWidth() {
		return 320;
	}

	@Override
	public int getScreenHeight() {
		return 200;
	}
}
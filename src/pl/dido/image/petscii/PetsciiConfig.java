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
		
		highContrast = HIGH_CONTRAST.NONE;
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
	
	@Override
	public int getWindowHeight() {
		return 400;
	}

	@Override
	public int getWindowWidth() {
		return 640;
	}
}
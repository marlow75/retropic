package pl.dido.image.petscii;

import pl.dido.image.Config;

public class PetsciiConfig extends Config {
	
	public enum NETWORK {
		L1, L2;
	};
	
	public NETWORK network;
		
	public PetsciiConfig() {
		super();
		
		network = NETWORK.L1;
		color_alg = NEAREST_COLOR.PERCEPTED;		
		
		dithering = false;
		dither_alg = DITHERING.ATKINSON;
		
		highContrast = HIGH_CONTRAST.NONE;
	}
	
	@Override
	public String getConfigString() {
		final String n;
		
		switch (network) {
		case L1:
			n = "L1 ";
			break;
		case L2:
			n = "L2 ";
			break;
		default:
			n = "SOFTMAX ";
		}
		
		return "40x25x2 " + n + super.getConfigString();
	}
}
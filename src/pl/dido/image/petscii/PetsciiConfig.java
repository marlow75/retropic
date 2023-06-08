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
		return "40x25x2 " + super.getConfigString();
	}
}
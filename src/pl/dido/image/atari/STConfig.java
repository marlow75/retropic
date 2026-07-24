package pl.dido.image.atari;

import pl.dido.image.utils.Config;

public class STConfig extends Config {
	
	public boolean fermionic_quantizer;
	
	public STConfig() {
		super();
		
		pal_view = false;
		fermionic_quantizer = false;
		dither_alg = DITHERING.BAYER4x4;
	}
	
	@Override
	public String getConfigString() {
		String configString;
		
		if (fermionic_quantizer)
			configString = "quantum";
		else
			configString = "SOM";
		
		return super.getConfigString() + configString;
	}

	@Override
	public int getScreenHeight() {
		return 200;
	}

	@Override
	public int getScreenWidth() {
		return 320;
	}
}

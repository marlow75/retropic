package pl.dido.image.amiga500;

import pl.dido.image.Config;

public class AmigaConfig extends Config {
	
	public boolean rleCompress;
	
	public AmigaConfig() {
		super();
		
		rleCompress = false;
	}
}

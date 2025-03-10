package pl.dido.image.amiga;

import pl.dido.image.utils.Config;

public abstract class AmigaConfig extends Config {
	
	public boolean rleCompress;
	
	public AmigaConfig() {
		super();
		
		rleCompress = false;
		pal_view = false;
	}
}

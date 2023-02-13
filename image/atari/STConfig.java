package pl.dido.image.atari;

import pl.dido.image.Config;

public class STConfig extends Config {
	
	public boolean replace_colors;
	
	public STConfig() {
		super();		
		dithering = true;
		replace_colors = false;
	}
}

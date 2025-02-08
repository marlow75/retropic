package pl.dido.image.vic20;

import pl.dido.image.petscii.PetsciiConfig;

public class Vic20Config extends PetsciiConfig {
	
	public boolean gen_charset;
	
	public Vic20Config() {
		super();
		this.gen_charset = false;
		this.dither_alg = DITHERING.BAYER2x2;
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
		
		if (this.gen_charset)
			return "176x184x2 " + n;
		else
			return "22x23x2 " + n;
	}

	@Override
	public int getScreenWidth() {
		return 176;
	}

	@Override
	public int getScreenHeight() {
		return 184;
	}
}
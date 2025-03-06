package pl.dido.image.vic20;

import pl.dido.image.petscii.PetsciiConfig;

public class Vic20Config extends PetsciiConfig {
	public enum VIDEO_MODE { PETSCII, HIRES, LOWRES };
	
	public VIDEO_MODE mode;
	
	public Vic20Config() {
		super();
		this.mode = VIDEO_MODE.PETSCII;
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
		
		switch (mode) {
		case HIRES:
			return "176x184x2 " + n;
		case LOWRES:
			return "88x184x2 " + n;
		default:
			return "22x23x2 " + n;
		}
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
package pl.dido.image.amiga;

public class Amiga500Config extends AmigaConfig {
	
	public enum VIDEO_MODE {
		STD_320x256, HAM6_320x256, STD_320x512, HAM6_320x512;
	};
	
	public VIDEO_MODE video_mode;
	
	public Amiga500Config() {
		super();
		video_mode = VIDEO_MODE.STD_320x256;
		dithering = true;
	}

	@Override
	public String getConfigString() {
		String configString = "";
		
		switch (video_mode) {
		case STD_320x512:
		case STD_320x256:
			configString += "x32 ";
			break;
		case HAM6_320x512:
		case HAM6_320x256:
			configString += " HAM ";
			break;
		}

		return configString + super.getConfigString();
	}

	@Override
	public int getHeight() {
		switch (video_mode) {
		case HAM6_320x256:
		case STD_320x256:
			return 256;

		case HAM6_320x512:
		case STD_320x512:
			return 512;
		}

		return -1;
	}

	@Override
	public int getWidth() {
		return 320;
	}
	
	@Override
	public int getScreenHeight() {
		return 384;
	}

	@Override
	public int getScreenWidth() {
		return 480;
	}
}
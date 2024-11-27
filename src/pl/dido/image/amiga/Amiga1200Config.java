package pl.dido.image.amiga;

public class Amiga1200Config extends AmigaConfig {
	
	public enum VIDEO_MODE {
		STD_320x256, HAM8_320x256, STD_320x512, HAM8_320x512, STD_640x512, HAM8_640x512; 
	};
	
	public VIDEO_MODE video_mode;
	
	public Amiga1200Config() {
		super();
		video_mode = VIDEO_MODE.STD_320x256;
	}

	@Override
	public String getConfigString() {
		String configString = "";		
		
		switch (video_mode) {
		case STD_320x256:
		case STD_320x512:
		case STD_640x512:
			configString += "x256 ";
			
			break;
		case HAM8_320x256:
		case HAM8_320x512:
		case HAM8_640x512:
			configString += " HAM8 ";

			break; 
		}
		
		return configString + super.getConfigString();
	}

	@Override
	public int getScreenHeight() {
		switch (video_mode) {
		case HAM8_320x256:
		case STD_320x256:
			return 256;

		case HAM8_320x512:
		case HAM8_640x512:
		case STD_640x512:
		case STD_320x512:
			return 512;
		}

		return -1;
	}

	@Override
	public int getScreenWidth() {
		switch (video_mode) {
		case HAM8_320x256:
		case STD_320x256:
		case HAM8_320x512:
		case STD_320x512:
			return 320;

		case HAM8_640x512:
		case STD_640x512:
			return 640;
		}

		return -1;
	}

	@Override
	public int getWindowWidth() {
		return 480;
	}

	@Override
	public int getWindowHeight() {
		return 384;
	}
}
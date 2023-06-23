package pl.dido.image.amiga;

public class Amiga1200Config extends AmigaConfig {
	
	public enum VIDEO_MODE {
		STD_320x256, HAM8_320x256, STD_320x512, HAM8_320x512, STD_640x512, HAM8_640x512; 
	};
	
	public VIDEO_MODE video_mode;
	
	public Amiga1200Config() {
		super();

		video_mode = VIDEO_MODE.STD_320x256;
		dithering = true;
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
}
package pl.dido.image.pc;

import pl.dido.image.utils.Config;

public class PCConfig extends Config {
	
	public enum NETWORK {
		L1, L2;
	};
	
	public enum VIDEO_MODE {
		CGA_TEXT, VESA_TEXT
	};
	
	public NETWORK network;
	public VIDEO_MODE video_mode;
		
	public PCConfig() {
		super();
		
		network = NETWORK.L1;
		color_alg = NEAREST_COLOR.PERCEPTED;		
		dither_alg = DITHERING.NONE;
		
		high_contrast = HIGH_CONTRAST.NONE;
		error_threshold = 3;
		
		low_pass_filter = true;
		video_mode = VIDEO_MODE.CGA_TEXT;
	}
	
	@Override
	public String getConfigString() {
		String n;
		
		switch (network) {
		case L1:
			n = "L1 ";
			break;
		default:
			n = "L2 ";
			break;
		}
		
		switch (video_mode) {
		case CGA_TEXT:
			n += "80x25x2 ";
			break;
		case VESA_TEXT:
			n += "132x50x2 ";
			break;
		}
		
		return n + super.getConfigString();
	}

	@Override
	public int getScreenWidth() {
		switch (video_mode) {
		case CGA_TEXT:
			return 640;
		case VESA_TEXT:
			return 1056;
		}
		
		return 640;
	}

	@Override
	public int getScreenHeight() {
		switch (video_mode) {
		case CGA_TEXT:
			return 200;
		case VESA_TEXT:
			return 400;
		}
		
		return 200;
	}
}
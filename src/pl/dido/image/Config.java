package pl.dido.image;

public class Config implements Cloneable {

	public enum DITHERING {
		STD_FS
	};

	public enum NEAREST_COLOR {
		EUCLIDEAN, PERCEPTED, LUMA_WEIGHTED
	};

	public boolean vivid;
	public static String default_path;
	public static String export_path;

	public DITHERING dither_alg;
	public NEAREST_COLOR color_alg;

	public Config() {
		dither_alg = DITHERING.STD_FS;
		color_alg = NEAREST_COLOR.PERCEPTED;

		vivid = false;

		default_path = "pic";
		export_path = "export";
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
}
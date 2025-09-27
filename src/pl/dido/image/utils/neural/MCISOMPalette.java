package pl.dido.image.utils.neural;

public class MCISOMPalette extends SOMPalette {
	
	protected byte rgb[];

	public MCISOMPalette(final float rate, final float radius, final int epoch, final byte rgb[]) {
		super(2, 2, rate, radius, epoch);
		this.rgb = rgb;
	}
}
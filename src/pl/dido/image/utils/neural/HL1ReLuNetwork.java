package pl.dido.image.utils.neural;

public class HL1ReLuNetwork extends HL1Network {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Override
	protected float activation(final float x) {
		return x >= 0 ? Math.min(0.2f * x, 1f) : 0.01f * x;
	}
	
	@Override
	protected float derivative(final float x) {
		return x >= 0 ? 0.2f : 0.01f;
	}

	public HL1ReLuNetwork(final int in, final int hid, final int out) {
		super(in, hid, out);
	}
}
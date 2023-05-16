package pl.dido.image.utils.neural;

public class Dataset {
	private float[] input;
	private float[] sample;

	public float getInput(final int i) {
		return input[i];
	}

	public float getSample(final int i) {
		return sample[i];
	}

	public Dataset(final float[] input) {
		this.input = new float[input.length];
		for (int i = 0; i < input.length; i++)
			this.input[i] = input[i];
	}

	public Dataset(final float[] input, final float[] sample) {
		this.input = new float[input.length];
		this.sample = new float[sample.length];
		
		for (int i = 0; i < input.length; i++)
			this.input[i] = input[i];
		
		for (int t = 0; t < sample.length; t++)
			this.sample[t] = sample[t];
	}
}

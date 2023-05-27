package pl.dido.image.utils.neural;

public class Dataset {
	private float[] input;
	private float[] output;

	public float getInput(final int i) {
		return input[i];
	}

	public float getOutput(final int i) {
		return output[i];
	}

	public Dataset(final float[] input) {
		this.input = new float[input.length];
		for (int i = 0; i < input.length; i++)
			this.input[i] = input[i];
	}

	public Dataset(final float[] input, final float[] sample) {

		if (input != null) {
			this.input = new float[input.length];

			for (int i = 0; i < input.length; i++)
				this.input[i] = input[i];
		}

		this.output = new float[sample.length];
		for (int t = 0; t < sample.length; t++)
			this.output[t] = sample[t];
	}
}

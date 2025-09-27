package pl.dido.image.utils.neural;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Random;
import java.util.Vector;
import java.util.logging.Logger;

public class FastAutoencoder implements Network {
	public static Logger log = Logger.getLogger(FastAutoencoder.class.getCanonicalName());

	public float EPOCHS = 1_000;
	public float ERR_LIMIT = 0.05f;

	private int inputSize;
	private int hiddenSize;
	private int outputSize;
	private float learningRate = 0.001f;

	private float[][] weightsInputHidden;
	private float[][] weightsHiddenOutput;

	private float[] biasHidden;
	private float[] biasOutput;
	
	// best model parameters
	private float[][] bestWeightsInputHidden;
	private float[][] bestWeightsHiddenOutput;

	private float[] bestBiasHidden;
	private float[] bestBiasOutput;

	private float[] hiddenLayer;
	private float[] hiddenLayerPreActivation;

	private float[] outputLayer;
	private float[] outputLayerPreActivation;

	// temporary data for backpropagation
	private float[] deltaOutput;
	private float[] deltaHidden;

	// adam hyperparameters
	private static float beta1 = 0.78f;
	private static float beta2 = 0.999f;
	private static float epsilon = 1e-8f;

	// adam moments for weights and biases
	private float[][] mWih;
	private float[][] vWih;

	private float[] mBh;
	private float[] vBh;

	private float[][] mWho;
	private float[][] vWho;

	private float[] mBo;
	private float[] vBo;

	private float[][] gradWho;
	private float[][] gradWih;

	private int t = 0; // Adam step counter
	private ArrayList<NetworkProgressListener> listeners = new ArrayList<NetworkProgressListener>();

	public FastAutoencoder(final int inputSize, final int hiddenSize, final int outputSize) {
		final Random rand = new Random(System.currentTimeMillis());

		this.inputSize = inputSize;
		this.hiddenSize = hiddenSize;
		this.outputSize = outputSize;

		hiddenLayer = new float[hiddenSize];
		hiddenLayerPreActivation = new float[hiddenSize];

		outputLayer = new float[outputSize];
		outputLayerPreActivation = new float[outputSize];

		deltaOutput = new float[outputSize];
		deltaHidden = new float[hiddenSize];

		weightsInputHidden = new float[inputSize][hiddenSize];
		weightsHiddenOutput = new float[hiddenSize][outputSize];

		biasHidden = new float[hiddenSize];
		biasOutput = new float[outputSize];

		bestWeightsInputHidden = new float[inputSize][hiddenSize];
		bestWeightsHiddenOutput = new float[hiddenSize][outputSize];

		bestBiasHidden = new float[hiddenSize];
		bestBiasOutput = new float[outputSize];
		
		final float stddev1 = (float) Math.sqrt(2.0 / inputSize); // He for ReLU
		final float stddev2 = (float) Math.sqrt(2.0 / (hiddenSize + outputSize)); // Xavier for output

		for (int i = 0; i < inputSize; i++)
			for (int j = 0; j < hiddenSize; j++)
				weightsInputHidden[i][j] = (float) (rand.nextGaussian() * stddev1);

		for (int i = 0; i < hiddenSize; i++)
			biasHidden[i] = 0.0f;

		for (int i = 0; i < hiddenSize; i++)
			for (int j = 0; j < outputSize; j++)
				weightsHiddenOutput[i][j] = (float) (rand.nextGaussian() * stddev2);

		for (int i = 0; i < outputSize; i++)
			biasOutput[i] = 0.0f;

		mWih = new float[inputSize][hiddenSize];
		vWih = new float[inputSize][hiddenSize];

		mBh = new float[hiddenSize];
		vBh = new float[hiddenSize];

		mWho = new float[hiddenSize][outputSize];
		vWho = new float[hiddenSize][outputSize];

		mBo = new float[outputSize];
		vBo = new float[outputSize];

		gradWho = new float[hiddenSize][outputSize];
		gradWih = new float[inputSize][hiddenSize];
	}

	@Override
	public void forward(final float[] input) {
		for (int i = 0; i < hiddenSize; i++) {
			float sum = biasHidden[i];

			for (int j = 0; j < inputSize; j++)
				sum += input[j] * weightsInputHidden[j][i];

			hiddenLayerPreActivation[i] = sum;
			hiddenLayer[i] = lrelu(sum);
		}

		for (int i = 0; i < outputSize; i++) {
			float sum = biasOutput[i];

			for (int j = 0; j < hiddenSize; j++)
				sum += hiddenLayer[j] * weightsHiddenOutput[j][i];

			outputLayerPreActivation[i] = sum;
			outputLayer[i] = sigmoid(sum); // SIGMOID na wyjœciu
		}
	}

	@Override
	public void load(final InputStream inputStream) throws IOException {
		final DataInputStream dis = new DataInputStream(inputStream);

		for (int i = 0; i < inputSize; i++)
			for (int j = 0; j < hiddenSize; j++)
				weightsInputHidden[i][j] = dis.readFloat();

		for (int i = 0; i < hiddenSize; i++)
			for (int j = 0; j < outputSize; j++)
				weightsHiddenOutput[i][j] = dis.readFloat();

		for (int j = 0; j < hiddenSize; j++)
			biasHidden[j] = dis.readFloat();

		for (int j = 0; j < outputSize; j++)
			biasOutput[j] = dis.readFloat();

		dis.close();
	}

	@Override
	public void save(final OutputStream outputStream) throws IOException {
		final DataOutputStream dos = new DataOutputStream(outputStream);

		for (int i = 0; i < inputSize; i++)
			for (int j = 0; j < hiddenSize; j++)
				dos.writeFloat(weightsInputHidden[i][j]);

		for (int i = 0; i < hiddenSize; i++)
			for (int j = 0; j < outputSize; j++)
				dos.writeFloat(weightsHiddenOutput[i][j]);

		for (int j = 0; j < hiddenSize; j++)
			dos.writeFloat(biasHidden[j]);

		for (int j = 0; j < outputSize; j++)
			dos.writeFloat(biasOutput[j]);

		dos.close();
	}

	public void train(final Vector<Dataset> samples) {
		final Vector<Dataset> noisedSamples = new Vector<Dataset>();
		final Random rnd = new Random();

		for (final Dataset ds : samples) {
			for (int x = 0; x < 3; x++) {
				final Dataset newDS = ds.clone();

				for (int i = 0; i < inputSize; i++)
					newDS.input[i] = (float) ((Math.random() < 0.1) ? (ds.input[i] + 0.2f * (float) rnd.nextGaussian())
							: ds.input[i]);

				noisedSamples.add(newDS);
			}
		}

		log.info("Learning...");
		for (int loop = 0; loop < EPOCHS; loop++) {
			Collections.shuffle(noisedSamples);
			float error;

			do {
				error = 0f;
				int count = 0;

				for (final Enumeration<Dataset> e = noisedSamples.elements(); e.hasMoreElements();) {
					final Dataset dataset = e.nextElement();

					forward(dataset.input);
					error += back(dataset);
					count++;
				}

				error /= count;
			} while (error > ERR_LIMIT);

			if (loop % 100 == 0)
				sendNotification(loop + ": " + error);
		}

		sendNotification("done.");
	}

	// adam update helper
	private static final float adamUpdate(final float grad, final float m, final float v, final int t, final float lr) {
		final float mHat = m / (1f - (float) Math.pow(beta1, t));
		final float vHat = v / (1f - (float) Math.pow(beta2, t));

		return lr * mHat / ((float) Math.sqrt(vHat) + epsilon);
	}

	public float back(final Dataset data) {
		float error = 0;
		t++; // Adam step

		// Output layer (CROSS-ENTROPY + SIGMOID)
		for (int i = 0; i < outputSize; i++) {
			final float predicted = outputLayer[i];
			final float target = data.output[i];
			// cross-entropy loss
			error -= target * Math.log(predicted + 1e-8f) + (1 - target) * Math.log(1 - predicted + 1e-8f);
			// gradient for sigmoid + cross-entropy
			deltaOutput[i] = predicted - target;
		}

		// Hidden layer
		for (int i = 0; i < hiddenSize; i++) {
			float sum = 0f;

			for (int j = 0; j < outputSize; j++)
				sum += deltaOutput[j] * weightsHiddenOutput[i][j];

			deltaHidden[i] = sum * lreluDerivative(hiddenLayerPreActivation[i]);
		}

		// gradients for weights and biases (output layer)
		for (int i = 0; i < hiddenSize; i++)
			for (int j = 0; j < outputSize; j++)
				gradWho[i][j] = deltaOutput[j] * hiddenLayer[i];

		// gradients for weights and biases (hidden layer)
		for (int i = 0; i < inputSize; i++)
			for (int j = 0; j < hiddenSize; j++)
				gradWih[i][j] = deltaHidden[j] * data.input[i];

		// adam update for weightsHiddenOutput and biasOutput
		for (int i = 0; i < hiddenSize; i++)
			for (int j = 0; j < outputSize; j++) {
				final float b = gradWho[i][j];

				mWho[i][j] = beta1 * mWho[i][j] + (1 - beta1) * b;
				vWho[i][j] = beta2 * vWho[i][j] + (1 - beta2) * b * b;

				weightsHiddenOutput[i][j] -= adamUpdate(b, mWho[i][j], vWho[i][j], t, learningRate);
			}

		for (int i = 0; i < outputSize; i++) {
			final float b = deltaOutput[i];

			mBo[i] = beta1 * mBo[i] + (1 - beta1) * b;
			vBo[i] = beta2 * vBo[i] + (1 - beta2) * b * b;

			biasOutput[i] -= adamUpdate(b, mBo[i], vBo[i], t, learningRate);
		}

		// adam update for weightsInputHidden and biasHidden
		for (int i = 0; i < inputSize; i++)
			for (int j = 0; j < hiddenSize; j++) {
				final float b = gradWih[i][j];

				mWih[i][j] = beta1 * mWih[i][j] + (1 - beta1) * b;
				vWih[i][j] = beta2 * vWih[i][j] + (1 - beta2) * b * b;

				weightsInputHidden[i][j] -= adamUpdate(b, mWih[i][j], vWih[i][j], t, learningRate);
			}

		for (int i = 0; i < hiddenSize; i++) {
			final float b = deltaHidden[i];

			mBh[i] = beta1 * mBh[i] + (1 - beta1) * b;
			vBh[i] = beta2 * vBh[i] + (1 - beta2) * b * b;

			biasHidden[i] -= adamUpdate(b, mBh[i], vBh[i], t, learningRate);
		}

		return error / outputLayer.length;
	}

	@Override
	public float[] getResult() {
		return outputLayer;
	}

	protected void sendNotification(final String msg) {
		for (final NetworkProgressListener listener : listeners)
			listener.notifyProgress(msg);
	}

	@Override
	public void addProgressListener(final NetworkProgressListener listener) {
		listeners.add(listener);
	}

	private static final float lrelu(final float x) {
		return x < 0 ? 0.01f * x : x;
	}

	private static float lreluDerivative(final float x) {
		return x < 0 ? 0.01f : 1.0f;
	}

	private static float sigmoid(final float x) {
		return 1.0f / (1.0f + (float) Math.exp(-x));
	}
	
	private void saveBestModel() {
        for (int i = 0; i < inputSize; i++)
            for (int j = 0; j < hiddenSize; j++)
                bestWeightsInputHidden[i][j] = weightsInputHidden[i][j];

        for (int i = 0; i < hiddenSize; i++)
            for (int j = 0; j < outputSize; j++)
                bestWeightsHiddenOutput[i][j] = weightsHiddenOutput[i][j];

        for (int i = 0; i < hiddenSize; i++)
            bestBiasHidden[i] = biasHidden[i];

        for (int i = 0; i < outputSize; i++)
            bestBiasOutput[i] = biasOutput[i];
    }

    private void restoreBestModel() {
        for (int i = 0; i < inputSize; i++)
            for (int j = 0; j < hiddenSize; j++)
                weightsInputHidden[i][j] = bestWeightsInputHidden[i][j];

        for (int i = 0; i < hiddenSize; i++)
            for (int j = 0; j < outputSize; j++)
                weightsHiddenOutput[i][j] = bestWeightsHiddenOutput[i][j];

        for (int i = 0; i < hiddenSize; i++)
            biasHidden[i] = bestBiasHidden[i];

        for (int i = 0; i < outputSize; i++)
            biasOutput[i] = bestBiasOutput[i];
    }

    public float validate(final Vector<Dataset> validationSet) {
        float totalError = 0;
        int count = 0;
        
        for (final Dataset data : validationSet) {
            forward(data.input);
            
            float err = 0;
            for (int i = 0; i < outputSize; i++) {
                float delta = outputLayer[i] - data.output[i];
                err += delta * delta;
            }
            
            totalError += err / outputSize;
            count++;
        }
        
        return totalError / count;
    }

	@Override
	public float trainWithValidation(final Vector<Dataset> trainSet, final Vector<Dataset> validationSet, int validationFrequency,
			int patience) {
		log.info("Learning with validation...");

		float bestError = Float.MAX_VALUE;
		float bestAccuracy = 0;

		int epochsWithoutImprovement = 0;

		for (int loop = 0; loop < EPOCHS; loop++) {
			Collections.shuffle(trainSet);
			float trainError;

			do {
				trainError = 0;
				int count = 0;

				for (final Enumeration<Dataset> e = trainSet.elements(); e.hasMoreElements();) {
					final Dataset dataset = e.nextElement();

					forward(dataset.input);
					trainError += back(dataset);
					count++;
				}

				trainError /= count;
			} while (trainError > ERR_LIMIT);

			if (loop % validationFrequency == 0) {
				final float validationError = validate(validationSet);

				sendNotification(loop + ": te = " + trainError + ", ve = " + validationError);
				if (validationError < bestError) {
					bestError = validationError;

					epochsWithoutImprovement = 0;
					saveBestModel();
				} else {
					epochsWithoutImprovement++;
					if (patience > 0 && epochsWithoutImprovement >= patience) {
						log.info("early stopping at epoch " + loop);
						sendNotification(
								"early stopping at epoch " + loop + " (best validation error: " + bestError + ")");

						restoreBestModel();
						break;
					}
				}
			} else if (loop % 100 == 0)
				sendNotification(loop + ": " + trainError);
		}

		sendNotification("Training completed. Best validation error: " + bestError);
		return bestAccuracy;
	}

	@Override
	public void batchTrain(Vector<Dataset> samples, int batchSize) {
		throw new RuntimeException("Not implemented");
	}
}
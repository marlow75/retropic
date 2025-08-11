package pl.dido.image.utils.neural;

import java.io.*;
import java.util.*;
import java.util.logging.Logger;

public class FastClassifier implements Network {
    public static Logger log = Logger.getLogger(FastClassifier.class.getCanonicalName());

    private float EPOCHS = 1_000;
    private float ERR_LIMIT = 0.005f;
    private float learningRate = 0.001f;

    private int inputSize;
    private int hiddenSize;
    private int outputSize;

    private float[][] weightsInputHidden;
    private float[][] weightsHiddenOutput;

    private float[] biasHidden;
    private float[] biasOutput;

    private float[] hiddenLayer;
    private float[] hiddenLayerPreActivation;

    private float[] outputLayerPreActivation;
    private float[] outputLayer;

    private float[] error;

    // temporary data for back propagation
    private float[] deltaOutput;
    private float[] deltaHidden;

    private ArrayList<NetworkProgressListener> listeners = new ArrayList<NetworkProgressListener>();

    private float[] avgDeltaOutput;
    private float[] avgDeltaHidden;
    private float[] avgInput;

    // adam hyperparameters
    private static final float beta1 = 0.9f;
    private static final float beta2 = 0.999f;
    private static final float epsilon = 1e-9f;

    // adam moments for weights and biases
    private float[][] mWih, vWih;
    private float[][] mWho, vWho;
    private float[] mBh, vBh;
    private float[] mBo, vBo;

    private int t = 0; // adam step counter

    // early stopping - przechowywanie najlepszego modelu
    private float[][] bestWeightsInputHidden;
    private float[][] bestWeightsHiddenOutput;
    
    private float[] bestBiasHidden;
    private float[] bestBiasOutput;
    
    public FastClassifier(final int inputSize, final int hiddenSize, final int outputSize, final float learingRate, final float errorLimit) {
    	initialize(inputSize, hiddenSize, outputSize);
    	
    	this.ERR_LIMIT = errorLimit;
    	this.learningRate = learingRate;
    }

    public FastClassifier(final int inputSize, final int hiddenSize, final int outputSize) {
    	initialize(inputSize, hiddenSize, outputSize);
    }
    
    public void initialize(final int inputSize, final int hiddenSize, final int outputSize) {
        final Random rand = new Random(System.currentTimeMillis());

        this.inputSize = inputSize;
        this.hiddenSize = hiddenSize;
        this.outputSize = outputSize;

        hiddenLayer = new float[hiddenSize];
        hiddenLayerPreActivation = new float[hiddenSize];

        outputLayerPreActivation = new float[outputSize];
        outputLayer = new float[outputSize];

        error = new float[outputSize];

        avgDeltaOutput = new float[outputSize];
        avgDeltaHidden = new float[hiddenSize];
        avgInput = new float[inputSize];

        deltaOutput = new float[outputSize];
        deltaHidden = new float[hiddenSize];

        weightsInputHidden = new float[inputSize][hiddenSize];
        weightsHiddenOutput = new float[hiddenSize][outputSize];

        biasHidden = new float[hiddenSize];
        biasOutput = new float[outputSize];

        // early stopping - inicjalizacja
        bestWeightsInputHidden = new float[inputSize][hiddenSize];
        bestWeightsHiddenOutput = new float[hiddenSize][outputSize];
        bestBiasHidden = new float[hiddenSize];
        bestBiasOutput = new float[outputSize];

        final float nAvg = (inputSize + outputSize) / 2.0f;
        final float variance = 1.0f / nAvg;
        final float standardDeviation = (float) Math.sqrt(variance);

        // bias weight initialization
        for (int i = 0; i < inputSize; i++)
            for (int j = 0; j < hiddenSize; j++)
                weightsInputHidden[i][j] = (float) rand.nextGaussian() * standardDeviation;

        for (int i = 0; i < hiddenSize; i++)
            for (int j = 0; j < outputSize; j++)
                weightsHiddenOutput[i][j] = (float) rand.nextGaussian() * standardDeviation;

        for (int i = 0; i < hiddenSize; i++)
            biasHidden[i] = 0;

        for (int i = 0; i < outputSize; i++)
            biasOutput[i] = 0;

        // adam moments initialization
        mWih = new float[inputSize][hiddenSize];
        vWih = new float[inputSize][hiddenSize];

        mWho = new float[hiddenSize][outputSize];
        vWho = new float[hiddenSize][outputSize];

        mBh = new float[hiddenSize];
        vBh = new float[hiddenSize];

        mBo = new float[outputSize];
        vBo = new float[outputSize];
    }

    private void softmax() {
        float max = Float.NEGATIVE_INFINITY;
        final int len = outputSize;

        // get max value
        for (final float v : outputLayerPreActivation)
            if (v > max)
                max = v;

        float sum = 0.0f;
        for (int i = 0; i < len; i++) {
            outputLayer[i] = (float) Math.exp(outputLayerPreActivation[i] - max); // normalizacja
            sum += outputLayer[i];
        }

        for (int i = 0; i < len; i++)
            outputLayer[i] /= sum;
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
        }

        softmax();
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

    public float validate(Vector<Dataset> validationSet) {
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

    public float calculateAccuracy(Vector<Dataset> validationSet) {
        int correct = 0;
        
        for (Dataset data : validationSet) {
            forward(data.input);
            
            int predicted = getPredictedClass();
            int actual = getTargetClass(data.output);
            
            if (predicted == actual)
                correct++;
        }
        
        return (float) correct / validationSet.size();
    }

    public int getPredictedClass() {
        float maxValue = -Float.MAX_VALUE;
        int predictedClass = -1;
        
        for (int i = 0; i < outputSize; i++) {
            if (outputLayer[i] > maxValue) {
                maxValue = outputLayer[i];
                predictedClass = i;
            }
        }
        return predictedClass;
    }

    private int getTargetClass(float[] output) {
        
    	for (int i = 0; i < output.length; i++)
            if (output[i] > 0.5f)
                return i;
        
    	return -1;
    }

    public float trainWithValidation(Vector<Dataset> trainSet, Vector<Dataset> validationSet,
                                   int validationFrequency, int patience) {
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
                final float accuracy = calculateAccuracy(validationSet);

                sendNotification(loop + ": te = " + trainError +
                        ", ve = " + validationError +
                        ", accuracy = " + accuracy);

                // early stopping
                if (validationError < bestError) {
                    bestError = validationError;
                    bestAccuracy = accuracy;
                    
                    epochsWithoutImprovement = 0;
                    saveBestModel(); 
                } else {
                    epochsWithoutImprovement++;
                    if (patience > 0 && epochsWithoutImprovement >= patience) {
                        log.info("early stopping at epoch " + loop);
                        sendNotification("early stopping at epoch " + loop +
                                " (best validation error: " + bestError + ")");
                        
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

    public void train(final Vector<Dataset> samples) {
        log.info("Learning...");

        for (int loop = 0; loop < EPOCHS; loop++) {
            Collections.shuffle(samples);
            float error;

            do {
                error = 0;
                int count = 0;

                for (final Enumeration<Dataset> e = samples.elements(); e.hasMoreElements();) {
                    final Dataset dataset = e.nextElement();

                    forward(dataset.input);
                    error += back(dataset);
                    count++;
                }

                error /= count;
            } while (error > ERR_LIMIT);

            if (loop % 1000 == 0)
                sendNotification(loop + ": " + error);
        }

        sendNotification("done.");
    }

    public void batchTrain(final Vector<Dataset> samples, final int batchSize) {
        log.info("Learning...");

        for (int loop = 0; loop < EPOCHS; loop++) {
            Collections.shuffle(samples);
            float error;

            do {
                error = 0;
                int count = 0;

                for (final Enumeration<Dataset> e = samples.elements(); e.hasMoreElements();) {
                    int batchCount = 0;

                    Arrays.fill(avgDeltaOutput, 0);
                    Arrays.fill(avgDeltaHidden, 0);

                    Arrays.fill(avgInput, 0);
                    Arrays.fill(this.error, 0);

                    for (int k = 0; k < batchSize; k++) {
                        if (!e.hasMoreElements())
                            break;

                        final Dataset dataset = e.nextElement();
                        forward(dataset.input);

                        for (int i = 0; i < inputSize; i++)
                            avgInput[i] += dataset.input[i];

                        calcGradient(dataset.output);
                        batchCount++;
                    }

                    count += batchCount;
                    error += getMeanError(batchCount);

                    updateWeightsBiases(batchCount);
                }

                error /= count;
            } while (error > ERR_LIMIT);

            if (loop % 1000 == 0)
                sendNotification(loop + ": " + error);
        }

        sendNotification("done.");
    }

    protected float getMeanError(final int batchSize) {
        float e = 0;
        for (int i = 0; i < error.length; i++)
            e += error[i] / batchSize;

        return e / outputLayer.length;
    }

    protected void calcGradient(final float[] target) {
        for (int i = 0; i < outputSize; i++) {
            final float delta = outputLayer[i] - target[i];
            error[i] += delta * delta;

            deltaOutput[i] = delta;
            avgDeltaOutput[i] += delta;
        }

        for (int i = 0; i < hiddenSize; i++) {
            float sum = 0f;
            for (int j = 0; j < outputSize; j++)
                sum += deltaOutput[j] * weightsHiddenOutput[i][j];

            deltaHidden[i] = sum * lreluDerivative(hiddenLayerPreActivation[i]);
            avgDeltaHidden[i] += deltaHidden[i];
        }
    }

    protected void updateWeightsBiases(final int batchSize) {
        for (int i = 0; i < outputSize; i++)
            avgDeltaOutput[i] /= batchSize;

        for (int i = 0; i < hiddenSize; i++)
            avgDeltaHidden[i] /= batchSize;

        for (int i = 0; i < inputSize; i++)
            avgInput[i] /= batchSize;

        // update weights and biases (output layer)
        for (int i = 0; i < hiddenSize; i++)
            for (int j = 0; j < outputSize; j++)
                weightsHiddenOutput[i][j] -= learningRate * avgDeltaOutput[j] * hiddenLayer[i];

        for (int i = 0; i < outputSize; i++)
            biasOutput[i] -= learningRate * avgDeltaOutput[i];

        // update weights and biases (hidden layer)
        for (int i = 0; i < inputSize; i++)
            for (int j = 0; j < hiddenSize; j++)
                weightsInputHidden[i][j] -= learningRate * avgDeltaHidden[j] * avgInput[i];

        for (int i = 0; i < hiddenSize; i++)
            biasHidden[i] -= learningRate * avgDeltaHidden[i];
    }

    private static float adamUpdate(float grad, float m, float v, int t, float lr) {
        float mHat = m / (1f - (float)Math.pow(beta1, t));
        float vHat = v / (1f - (float)Math.pow(beta2, t));
        return lr * mHat / ((float)Math.sqrt(vHat) + epsilon);
    }

    public float back(final Dataset data) {
        float error = 0;
        t++; // adam step

        // Output layer
        for (int i = 0; i < outputSize; i++) {
            final float delta = outputLayer[i] - data.output[i];
            error += delta * delta;
            deltaOutput[i] = delta;
        }

        // hidden layer
        for (int i = 0; i < hiddenSize; i++) {
            float sum = 0f;
            for (int j = 0; j < outputSize; j++)
                sum += deltaOutput[j] * weightsHiddenOutput[i][j];
            deltaHidden[i] = sum * lreluDerivative(hiddenLayerPreActivation[i]);
        }

        // adam update for weightsHiddenOutput and biasOutput
        for (int i = 0; i < hiddenSize; i++)
            for (int j = 0; j < outputSize; j++) {
                float grad = deltaOutput[j] * hiddenLayer[i];
                mWho[i][j] = beta1 * mWho[i][j] + (1 - beta1) * grad;
                vWho[i][j] = beta2 * vWho[i][j] + (1 - beta2) * grad * grad;
                weightsHiddenOutput[i][j] -= adamUpdate(grad, mWho[i][j], vWho[i][j], t, learningRate);
            }

        for (int i = 0; i < outputSize; i++) {
            float grad = deltaOutput[i];
            mBo[i] = beta1 * mBo[i] + (1 - beta1) * grad;
            vBo[i] = beta2 * vBo[i] + (1 - beta2) * grad * grad;
            biasOutput[i] -= adamUpdate(grad, mBo[i], vBo[i], t, learningRate);
        }

        // adam update for weightsInputHidden and biasHidden
        for (int i = 0; i < inputSize; i++)
            for (int j = 0; j < hiddenSize; j++) {
                float grad = deltaHidden[j] * data.input[i];
                mWih[i][j] = beta1 * mWih[i][j] + (1 - beta1) * grad;
                vWih[i][j] = beta2 * vWih[i][j] + (1 - beta2) * grad * grad;
                weightsInputHidden[i][j] -= adamUpdate(grad, mWih[i][j], vWih[i][j], t, learningRate);
            }

        for (int i = 0; i < hiddenSize; i++) {
            float grad = deltaHidden[i];
            mBh[i] = beta1 * mBh[i] + (1 - beta1) * grad;
            vBh[i] = beta2 * vBh[i] + (1 - beta2) * grad * grad;
            biasHidden[i] -= adamUpdate(grad, mBh[i], vBh[i], t, learningRate);
        }

        return error / outputLayer.length;
    }

    @Override
    public void addProgressListener(final NetworkProgressListener listener) {
        listeners.add(listener);
    }

    private static final float lrelu(final float x) {
        return x < 0 ? (0.01f * x) : x;
    }

    private static float lreluDerivative(final float x) {
        return x < 0 ? 0.01f : 1.0f;
    }

    @Override
    public float[] getResult() {
        return outputLayer;
    }

    protected void sendNotification(final String msg) {
        for (final NetworkProgressListener listener : listeners)
            listener.notifyProgress(msg);
    }
}
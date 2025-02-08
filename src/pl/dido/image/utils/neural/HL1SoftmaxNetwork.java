package pl.dido.image.utils.neural;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Vector;

import at.fhtw.ai.nn.NeuralNetwork;
import at.fhtw.ai.nn.Neuron;
import at.fhtw.ai.nn.Synapse;
import at.fhtw.ai.nn.activation.layer.Softmax;
import at.fhtw.ai.nn.activation.rectifier.Rectifier;
import at.fhtw.ai.nn.initialize.XavierInitializer;
import at.fhtw.ai.nn.learning.BackPropagation;
import at.fhtw.ai.nn.loss.CrossEntropy;
import at.fhtw.ai.nn.utils.NeuralNetworkBuilder;
import pl.dido.image.utils.ProgressListener;

public class HL1SoftmaxNetwork implements Network {

	protected NeuralNetwork neuralNetwork;
	protected BackPropagation backPropagation;
	
	protected ArrayList<ProgressListener> listeners;
	protected int inputSize, hiddenSize, outputSize;

	public HL1SoftmaxNetwork(final int inputSize, final int hiddenSize, final int outputSize) {
		this.inputSize = inputSize;
		this.hiddenSize = hiddenSize;
		this.outputSize = outputSize;
		
		neuralNetwork = new NeuralNetworkBuilder().inputLayer("Input Layer", inputSize, new Rectifier())
				.hiddenLayer("Hidden Layer", hiddenSize, new Rectifier()).outputLayer("Output Layer", outputSize, new Softmax())
				.initializer(new XavierInitializer()).build();

		backPropagation = new BackPropagation();
		backPropagation.setLossFunction(new CrossEntropy());
		backPropagation.setLearningRate(0.2);
		backPropagation.setMomentum(0.9);
		backPropagation.setMeanSquareError(0.005);
		backPropagation.setNeuralNetwork(neuralNetwork);
		
		listeners = new ArrayList<ProgressListener>();
	}

	@Override
	public void forward(final Dataset data) {
		for (int k = 0; k < inputSize; k++)
			neuralNetwork.getInputLayer().getNeurons().get(k).value = data.getInput(k);

		neuralNetwork.fireOutput();
	}

	@Override
	public void load(final InputStream inputStream) throws IOException {
		// load network 
		final DataInputStream dis = new DataInputStream(inputStream);
		for (final Neuron neuron : neuralNetwork.getNeurons()) {
			neuron.setValue(dis.readFloat());
			neuron.getBias().setValue(dis.readFloat());
			neuron.getBias().setWeight(dis.readFloat());
		}

		for (final Synapse synapse : neuralNetwork.getSynapses())
			synapse.setWeight(dis.readFloat());

		dis.close();
	}

	@Override
	public void save(final OutputStream outputStream) throws IOException {
		// save network
		final DataOutputStream dos = new DataOutputStream(outputStream);
		for (final Neuron neuron : neuralNetwork.getNeurons()) {
			dos.writeFloat((float) neuron.getValue());
			dos.writeFloat((float) neuron.getBias().getValue());
			dos.writeFloat((float) neuron.getBias().getWeight());
		}

		for (final Synapse synapse : neuralNetwork.getSynapses())
			dos.writeFloat((float) synapse.getWeight());

		dos.close();
	}

	@Override
	public float[] getResult() {
		final float[] result = new float[outputSize];

		for (int k = 0; k < outputSize; k++)
			result[k] = (float) neuralNetwork.getOutputLayer().getNeurons().get(k).getValue();
		
		return result;
	}

	@Override
	public void train(final Vector<Dataset> samples) {
		int index = 0;

		final ArrayList<Double> t = new ArrayList<Double>();
		do {
			for (final Dataset ds : samples) {
				for (int i = 0; i < inputSize; i++)
					neuralNetwork.getInputLayer().getNeurons().get(i).value = ds.getInput(i);

				t.clear();
				backPropagation.getDesiredOutputValues().clear();

				for (int i = 0; i < outputSize; i++)
					t.add((double) ds.getOutput(i));

				backPropagation.getDesiredOutputValues().addAll(t);
				do {
					backPropagation.learn();
				} while (backPropagation.networkError() > backPropagation.getMeanSquareError());
			}

			if (index % 100 == 0)
				notifyListeners();

		} while (index++ < 1000);
	}

	@Override
	public void addProgressListener(final ProgressListener listener) {
		if (listeners == null)
			listeners = new ArrayList<ProgressListener>();
		
		listeners.add(listener);
	}
	
	protected void notifyListeners() {
		for (final ProgressListener listener: listeners) {
			listener.notifyProgress();
		}
	}
}
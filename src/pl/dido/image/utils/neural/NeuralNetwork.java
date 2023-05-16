package pl.dido.image.utils.neural;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.Vector;
import java.util.logging.Logger;

public class NeuralNetwork implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static Logger log = Logger.getLogger(NeuralNetwork.class.getCanonicalName());

	private int IN = 8 * 8; // input layer
	private int HID = 8 * 8; // hidden layer
	
	private int OUT = 256; // out
	private float ERR_LIMIT = 0.1f;

	private float ALPHA = 0.5f;
	private float BETA = 0.3f;

	private float EPOCHS = 100_000;
	private float[] I = new float[IN];

	private float[] H = new float[HID];
	private float[] O = new float[OUT];

	private float[][] W = new float[HID][IN];
	private float[][] V = new float[OUT][HID];

	private float[] theta = new float[HID];
	private float[] gamma = new float[OUT];

	private float[] delta = new float[OUT];
	private float[] sigma = new float[HID];

	private float[] T = new float[OUT];
	
	public void initWeight(float[][] w) {
		for (int i = 0; i < w.length; i++)
			for (int j = 0; j < w[i].length; j++) {
				// [-0.1, 0.1]
				w[i][j] = ((float)Math.random() - 0.5f) * 2f;
			}
	}

	public void showWeight(float[][] w) {
		for (int i = 0; i < w.length; i++) {
			for (int j = 0; j < w[i].length; j++)
				System.out.print(w[i][j] + " ");
			
			System.out.println();
		}
	}

	public void initThreshold(float[] threshold) {
		for (int i = 0; i < threshold.length; i++) {
			// [-0.1, 0.1]
			threshold[i] = ((float)Math.random() - 0.5f) * 2.f;
		}
	}

	private final static float sigmoid(final float x) {
		return (float) (1 / (1 + Math.exp(-0.25 * x)));
	}

	public void forward(final Dataset d) {
		for (int i = 0; i < IN; i++)			
			I[i] = d.getInput(i);

		for (int j = 0; j < HID; j++) {
			float temp = 0.0f;
			final float w[] = W[j];
			
			for (int i = 0; i < IN; i++)
				temp += w[i] * I[i];
			
			temp += theta[j];
			H[j] = sigmoid(temp);
		}

		for (int k = 0; k < OUT; k++) {
			float temp = 0.0f;
			final float v[] = V[k];
			
			for (int j = 0; j < HID; j++)
				temp += v[j] * H[j];
			
			temp += gamma[k];
			O[k] = sigmoid(temp);
		}
	}

	public float back(final Dataset data) {
		float error = 0.0f;

		for (int k = 0; k < OUT; k++) {
			T[k] = data.getSample(k);
			error += Math.abs(T[k] - O[k]);
			
			delta[k] = (T[k] - O[k]) * O[k] * (1 - O[k]);
		}

		for (int j = 0; j < HID; j++) {
			float temp = 0.0f;

			for (int k = 0; k < OUT; k++)
				temp += delta[k] * V[k][j];
			
			sigma[j] = temp * H[j] * (1 - H[j]);
		}
		
		for (int k = 0; k < OUT; k++) {
			final float v[] = V[k];
			final float d = delta[k];
			
			for (int j = 0; j < HID; j++)
				v[j] += ALPHA * d * H[j];
			
			gamma[k] += BETA * d;
		}

		for (int j = 0; j < HID; j++) {
			final float w[] = W[j];
			final float s = sigma[j];
			
			for (int i = 0; i < IN; i++)
				w[i] += ALPHA * s * I[i];
			
			theta[j] += BETA * s;
		}

		return error;
	}

	public void learn(final Vector<Dataset> samples) {
		log.info("Learing...");

		for (int loop = 0; loop < EPOCHS; loop++) {
			float error = 0.f;
			
			for (final Enumeration<Dataset> e = samples.elements(); e.hasMoreElements();) {
				final Dataset dataset = e.nextElement();
				
				forward(dataset);
				error += back(dataset);
			}

			if (loop % 100 == 0)
				log.info(loop + ": " + error);

			if (error < ERR_LIMIT)
				break;
		}
		
		log.info("done.");
	}

	public float[] getResult() {
		return this.O;
	}

	public NeuralNetwork() {
		initWeight(W);
		//showWeight(W);

		initWeight(V);
		initThreshold(theta);
		initThreshold(gamma);
	}

	public void save(final String fileName) throws IOException {
		final FileOutputStream fos = new FileOutputStream(fileName);
	    final DataOutputStream dos = new DataOutputStream(fos);
	    
	    for (int i = 0; i < W.length; i++)
	    	for (int j = 0; j < W.length; j++)
	    		dos.writeFloat(W[i][j]);

	    for (int i = 0; i < V.length; i++)
	    	for (int j = 0; j < W.length; j++)
	    		dos.writeFloat(V[i][j]);
	    
	    for (int i = 0; i < theta.length; i++)
	    	dos.writeFloat(theta[i]);

	    for (int i = 0; i < gamma.length; i++)
	    	dos.writeFloat(gamma[i]);
	    
	    for (int i = 0; i < delta.length; i++)
	    	dos.writeFloat(delta[i]);

	    for (int i = 0; i < sigma.length; i++)
	    	dos.writeFloat(sigma[i]);
	    
	    dos.close();		
	}
	
	public void load(final InputStream inputStream) throws IOException {
	    final DataInputStream dos = new DataInputStream(inputStream);
	    
	    for (int i = 0; i < W.length; i++)
	    	for (int j = 0; j < W.length; j++)
	    		W[i][j] = dos.readFloat();

	    for (int i = 0; i < V.length; i++)
	    	for (int j = 0; j < W.length; j++)
	    		V[i][j] = dos.readFloat();
	    
	    for (int i = 0; i < theta.length; i++)
	    	theta[i] = dos.readFloat();

	    for (int i = 0; i < gamma.length; i++)
	    	gamma[i] = dos.readFloat();
	    
	    for (int i = 0; i < delta.length; i++)
	    	delta[i] = dos.readFloat();

	    for (int i = 0; i < sigma.length; i++)
	    	sigma[i] = dos.readFloat();
	    
	    dos.close();		
	}
}
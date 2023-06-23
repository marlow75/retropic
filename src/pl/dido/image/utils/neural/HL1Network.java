package pl.dido.image.utils.neural;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Vector;
import java.util.logging.Logger;

public class HL1Network implements Network, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public static Logger log = Logger.getLogger(HL1Network.class.getCanonicalName());

	public int IN = 8 * 8; // input layer
	public int HID = 2 * 8 * 8; // hidden layer
	
	public int OUT = 256; // out
	public float ERR_LIMIT = 0.1f;
	
	// adding momentum
	public float momentum = 0.5f;

	public float ALPHA = 0.05f;
	public float BETA = 0.05f;

	public float EPOCHS = 150_000;
	public float[] I;

	public float[] H;
	public float[] O;

	public float[][] W;
	public float[][] V;

	public float[][] deltaW;
	public float[][] deltaV;

	public float[] hiddenBias;
	public float[] outputBias;

	public float[] gradientOutput;
	public float[] gradientHidden;
	
	//public float[] T;

	public HL1Network(final int in, final int hid, final int out) {
		IN = in; HID = hid; OUT = out;
		
		I = new float[IN];

		H = new float[HID];
		O = new float[OUT];
		
		W = new float[HID][IN];
		V = new float[OUT][HID];

		deltaW = new float[HID][IN];
		deltaV = new float[OUT][HID];

		hiddenBias = new float[HID];
		outputBias = new float[OUT];

		gradientOutput = new float[OUT];
		gradientHidden = new float[HID];
		
		initWeight(W);
		initWeight(V);
		
		initThreshold(hiddenBias);
		initThreshold(outputBias);
	}
	
	public void initWeight(float[][] w) {
		for (int i = 0; i < w.length; i++)
			for (int j = 0; j < w[i].length; j++) {
				// [-0.1, 0.1]
				w[i][j] = ((float)Math.random() - 0.5f) * 2f;
			}
	}

	public void initThreshold(float[] threshold) {
		for (int i = 0; i < threshold.length; i++) {
			// [-0.1, 0.1]
			threshold[i] = ((float)Math.random() - 0.5f) * 2.f;
		}
	}

	protected float activation(final float x) {
		return (float) (1 / (1 + Math.exp(-0.2 * x)));
	}

	protected float derivative(final float x) {
		return x * (1 - x);
	}
	
	public void forward(final Dataset d) {
		for (int i = 0; i < IN; i++)			
			I[i] = d.getInput(i);

		for (int j = 0; j < HID; j++) {
			float sum = 0f;
			final float w[] = W[j];
			
			for (int i = 0; i < IN; i++)
				sum += w[i] * I[i];
			
			H[j] = activation(sum + hiddenBias[j]);
		}

		for (int k = 0; k < OUT; k++) {
			float sum = 0f;
			final float v[] = V[k];
			
			for (int j = 0; j < HID; j++)
				sum += v[j] * H[j];
			
			O[k] = activation(sum + outputBias[k]);
		}
	}

	public float back(final Dataset data) {
		float error = 0f, sum;
		float o, d, t, g;

		for (int k = 0; k < OUT; k++) {
			t = data.getOutput(k);

			o = O[k];
			d = t - o;

			error += Math.abs(d);
			gradientOutput[k] = d * derivative(o);
		}

		for (int j = 0; j < HID; j++) {
			sum = 0f;

			for (int k = 0; k < OUT; k++)
				sum += gradientOutput[k] * V[k][j];

			gradientHidden[j] = sum * derivative(H[j]);
		}

		for (int k = 0; k < OUT; k++) {
			final float v[] = V[k];
			d = gradientOutput[k];

			for (int j = 0; j < HID; j++) {
				g = ALPHA * d * H[j];
				v[j] += g + momentum * deltaV[k][j];
				deltaV[k][j] = g;
			}

			outputBias[k] += BETA * d;
		}

		for (int j = 0; j < HID; j++) {
			final float w[] = W[j];
			d = gradientHidden[j];

			for (int i = 0; i < IN; i++) {
				g = ALPHA * d * I[i];
				w[i] += g + momentum * deltaW[j][i];
				deltaW[j][i] = g;
			}

			hiddenBias[j] += BETA * d;
		}

		return error;
	}
	
	public void batchLearn(final Vector<Vector<Dataset>> batches) {
		log.info("Learing...");

		for (int loop = 0; loop < EPOCHS; loop++) {
			float error = 0f;
			
			for (final Vector<Dataset> batch: batches) {
				Collections.shuffle(batch);
				final float output[] = new float[OUT];
				
				for (final Enumeration<Dataset> e = batch.elements(); e.hasMoreElements();) {
					final Dataset dataset = e.nextElement();
	
					for (int i = 0; i < IN; i++)
						output[i] += dataset.getOutput(i);
						
					forward(dataset);					
				}

				error += back(new Dataset(null, output));				
			}
			
			if (loop % 100 == 0)
				log.info(loop + ": " + error);

			if (error < ERR_LIMIT)
				break;
		}

		log.info("done.");
	}
	
	public void learn(final Vector<Dataset> samples) {
		log.info("Learning...");
		int count = 0;
		
		for (int loop = 0; loop < EPOCHS; loop++) {
			float error = 0f;
		
			count = 0;
			Collections.shuffle(samples);
			for (final Enumeration<Dataset> e = samples.elements(); e.hasMoreElements();) {
				final Dataset dataset = e.nextElement();

				forward(dataset);
				error += back(dataset);
				count++;
			}

			if (loop % 100 == 0) {
				error /= count;
				log.info(loop + ": " + error);
			}

			if (error < ERR_LIMIT)
				break;
		}

		log.info("done.");
	}
	
	public float[] getResult() {
		return this.O;
	}
	
	protected void loadNetwork(final DataInputStream dos) throws IOException {
		for (int i = 0; i < W.length; i++)
	    	for (int j = 0; j < I.length; j++)
	    		W[i][j] = dos.readFloat();

	    for (int i = 0; i < V.length; i++)
	    	for (int j = 0; j < W.length; j++)
	    		V[i][j] = dos.readFloat();
	    
	    for (int i = 0; i < hiddenBias.length; i++)
	    	hiddenBias[i] = dos.readFloat();

	    for (int i = 0; i < outputBias.length; i++)
	    	outputBias[i] = dos.readFloat();
	    
	    for (int i = 0; i < gradientOutput.length; i++)
	    	gradientOutput[i] = dos.readFloat();

	    for (int i = 0; i < gradientHidden.length; i++)
	    	gradientHidden[i] = dos.readFloat();
	}
	
	public void load(final InputStream inputStream) throws IOException {
	    final DataInputStream dos = new DataInputStream(inputStream);
	    loadNetwork(dos);
	    
	    dos.close();		
	}
	
	protected void saveNetwork(final DataOutputStream dos) throws IOException {
	    for (int i = 0; i < W.length; i++)
	    	for (int j = 0; j < I.length; j++)
	    		dos.writeFloat(W[i][j]);

	    for (int i = 0; i < V.length; i++)
	    	for (int j = 0; j < W.length; j++)
	    		dos.writeFloat(V[i][j]);
	    
	    for (int i = 0; i < hiddenBias.length; i++)
	    	dos.writeFloat(hiddenBias[i]);

	    for (int i = 0; i < outputBias.length; i++)
	    	dos.writeFloat(outputBias[i]);
	    
	    for (int i = 0; i < gradientOutput.length; i++)
	    	dos.writeFloat(gradientOutput[i]);

	    for (int i = 0; i < gradientHidden.length; i++)
	    	dos.writeFloat(gradientHidden[i]);
	}

	@Override
	public void save(final OutputStream outputStream) throws IOException {
	    final DataOutputStream dos = new DataOutputStream(outputStream);
	    saveNetwork(dos);
	    
	    dos.close();
	}
}
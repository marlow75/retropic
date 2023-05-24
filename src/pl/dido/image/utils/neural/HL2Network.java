package pl.dido.image.utils.neural;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Vector;
import java.util.logging.Logger;

public class HL2Network implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static Logger log = Logger.getLogger(HL2Network.class.getCanonicalName());

	private int IN = 8 * 8; // input layer
	private int HID = 8 * 8; // hidden layer
	
	private int OUT = 256; // out
	private float ERR_LIMIT = 0.1f;

	private float ALPHA = 0.3f;
	private float BETA = 0.3f;

	private float EPOCHS = 100_000;

	// activation
	private float[] I = new float[IN];

	private float[] H1 = new float[HID];
	private float[] H2 = new float[HID];
	
	private float[] O = new float[OUT];

	// weights
	private float[][] WH1 = new float[HID][IN];
	private float[][] WH2 = new float[HID][HID];
	
	private float[][] WO = new float[OUT][HID];

	private float[] H1B = new float[HID];
	private float[] H2B = new float[HID];
	private float[] OB = new float[OUT];

	private float[] deltaOutput = new float[OUT];
	private float[] deltaHidden1 = new float[HID];
	private float[] deltaHidden2 = new float[HID];

	// samples
	private float[] T = new float[OUT];
	
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

	private final static float sigmoid(final float x) {
		return (float) (1 / (1 + Math.exp(-0.25 * x)));
	}
	
	private final static float dsigmoid(final float x) {
		return x * (1 - x);
	}

	public void forward(final Dataset d) {
		for (int i = 0; i < IN; i++)			
			I[i] = d.getInput(i);

		for (int j = 0; j < HID; j++) {
			float sum = 0.0f;
			final float w[] = WH1[j];
			
			for (int i = 0; i < IN; i++)
				sum += w[i] * I[i];
			
			H1[j] = sigmoid(sum + H1B[j]);
		}
		
		for (int j = 0; j < HID; j++) {
			float sum = 0.0f;
			final float w[] = WH2[j];
			
			for (int i = 0; i < HID; i++)
				sum += w[i] * H1[i];
			
			H2[j] = sigmoid(sum + H2B[j]);
		}

		for (int k = 0; k < OUT; k++) {
			float sum = 0.0f;
			final float v[] = WO[k];
			
			for (int j = 0; j < HID; j++)
				sum += v[j] * H2[j];
			
			O[k] = sigmoid(sum + OB[k]);
		}
	}

	public float back(final Dataset data) {
		float error = 0.0f;

		for (int k = 0; k < OUT; k++) {
			T[k] = data.getSample(k);
			
			final float o = O[k];
			final float d = T[k] - o;
			
			error += Math.abs(d);
			deltaOutput[k] = d * o * (1 - o);
		}

		// second layer
		for (int j = 0; j < HID; j++) {
			float sum = 0.0f;

			for (int k = 0; k < OUT; k++)
				sum += deltaOutput[k] * WO[k][j];
			
			deltaHidden2[j] = sum * dsigmoid(H2[j]);
		}
		
		for (int k = 0; k < OUT; k++) {
			final float v[] = WO[k];
			final float d = deltaOutput[k];
			
			for (int j = 0; j < HID; j++)
				v[j] += ALPHA * d * H2[j];
			
			OB[k] += BETA * d;
		}

		for (int j = 0; j < HID; j++) {
			final float w[] = WH2[j];
			final float d = deltaHidden2[j];
			
			for (int i = 0; i < HID; i++)
				w[i] += ALPHA * d * H1[i];
			
			H2B[j] += BETA * d;
		}
		
		// first layer
		for (int j = 0; j < HID; j++) {
			float sum = 0.0f;

			for (int k = 0; k < HID; k++)
				sum += deltaHidden2[k] * WH2[k][j];
			
			deltaHidden1[j] = sum * dsigmoid(H1[j]);
		}
		
		for (int k = 0; k < HID; k++) {
			final float v[] = WH1[k];
			final float d = deltaHidden2[k];
			
			for (int j = 0; j < HID; j++)
				v[j] += ALPHA * d * H1[j];
			
			H2B[k] += BETA * d;
		}

		for (int j = 0; j < HID; j++) {
			final float w[] = WH1[j];
			final float d = deltaHidden1[j];
			
			for (int i = 0; i < IN; i++)
				w[i] += ALPHA * d * I[i];
			
			H1B[j] += BETA * d;
		}

		return error;
	}

	public void learn(final Vector<Dataset> samples) {
		log.info("Learing...");

		for (int loop = 0; loop < EPOCHS; loop++) {
			float error = 0f;
			
			Collections.shuffle(samples);
			
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

	public HL2Network() {
		initWeight(WH1);
		initWeight(WH2);

		initWeight(WO);		
		initThreshold(H1B);
		
		initThreshold(H2B);
		initThreshold(OB);
	}

	public void save(final String fileName) throws IOException {
		final FileOutputStream fos = new FileOutputStream(fileName);
	    final DataOutputStream dos = new DataOutputStream(fos);
	    
	    for (int i = 0; i < HID; i++)
	    	for (int j = 0; j < IN; j++)
	    		dos.writeFloat(WH1[i][j]);
	    
	    for (int i = 0; i < HID; i++)
	    	for (int j = 0; j < HID; j++)
	    		dos.writeFloat(WH2[i][j]);

	    for (int i = 0; i < OUT; i++)
	    	for (int j = 0; j < HID; j++)
	    		dos.writeFloat(WO[i][j]);
	    
	    for (int i = 0; i < H1B.length; i++)
	    	dos.writeFloat(H1B[i]);

	    for (int i = 0; i < H2B.length; i++)
	    	dos.writeFloat(H2B[i]);
	    
	    for (int i = 0; i < OB.length; i++)
	    	dos.writeFloat(OB[i]);
	    
	    dos.close();		
	}
	
	public void load(final InputStream inputStream) throws IOException {
	    final DataInputStream dos = new DataInputStream(inputStream);

	    for (int i = 0; i < HID; i++)
	    	for (int j = 0; j < IN; j++)
	    		WH1[i][j] = dos.readFloat();
	    
	    for (int i = 0; i < HID; i++)
	    	for (int j = 0; j < HID; j++)
	    		WH2[i][j] = dos.readFloat();

	    for (int i = 0; i < OUT; i++)
	    	for (int j = 0; j < HID; j++)
	    		WO[i][j] = dos.readFloat();
	    
	    for (int i = 0; i < H1B.length; i++)
	    	H1B[i] = dos.readFloat();

	    for (int i = 0; i < H2B.length; i++)
	    	H2B[i] = dos.readFloat();
	    
	    for (int i = 0; i < OB.length; i++)
	    	OB[i] = dos.readFloat();

	    dos.close();
	}
}
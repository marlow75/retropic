package pl.dido.image.utils.neural;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class HL2Network extends HL1Network {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private float[] H2;

	// weights
	private float[][] W2;
	private float[] hiddenBias2;
	
	private float[][] deltaW2;
	private float[] gradientHidden2;
	
	@Override
	public void forward(final Dataset d) {
		float sum;
		
		for (int i = 0; i < IN; i++)			
			I[i] = d.getInput(i);

		for (int j = 0; j < HID; j++) {
			sum = 0f;
			final float w[] = W[j];
			
			for (int i = 0; i < IN; i++)
				sum += w[i] * I[i];
			
			H[j] = activation(sum + H[j]);
		}
		
		for (int j = 0; j < HID; j++) {
			sum = 0f;
			final float w[] = W2[j];
			
			for (int i = 0; i < HID; i++)
				sum += w[i] * H[i];
			
			H2[j] = activation(sum + hiddenBias2[j]);
		}

		for (int k = 0; k < OUT; k++) {
			sum = 0f;
			final float v[] = V[k];
			
			for (int j = 0; j < HID; j++)
				sum += v[j] * H2[j];
			
			O[k] = activation(sum + outputBias[k]);
		}
	}

	@Override
	public float back(final Dataset data) {
		float error = 0f, sum = 0, t, d, o, g;

		for (int k = 0; k < OUT; k++) {
			t = data.getOutput(k);
			
			o = O[k];
			d = t - o;
			
			error += Math.abs(d);
			gradientOutput[k] = d * derivative(o);
		}

		// second layer
		for (int j = 0; j < HID; j++) {
			sum = 0f;
			for (int k = 0; k < OUT; k++)
				sum += gradientOutput[k] * V[k][j];
			
			gradientHidden2[j] = sum * derivative(H2[j]);
		}
		
		// biases2
		for (int k = 0; k < OUT; k++) {
			final float v[] = V[k];
			d = gradientOutput[k];
			
			for (int j = 0; j < HID; j++) {
				g = ALPHA * d * H2[j];
				v[j] += g + momentum * deltaV[k][j];
				deltaV[k][j] = g;
			}
			
			outputBias[k] += BETA * d;
		}

		for (int j = 0; j < HID; j++) {
			final float w[] = W2[j];
			d = gradientHidden2[j];
			
			for (int i = 0; i < HID; i++) {
				g = ALPHA * d * H[i];
				w[i] += g + momentum * deltaW2[j][i];
				deltaW2[j][i] = g;
			}
			
			hiddenBias2[j] += BETA * d;
		}
		
		// first layer
		for (int j = 0; j < HID; j++) {
			sum = 0f;
			
			for (int k = 0; k < HID; k++)
				sum += gradientHidden2[k] * W2[k][j];
			
			gradientHidden[j] = sum * derivative(H[j]);
		}
		
		for (int j = 0; j < HID; j++) {
			final float w[] = W[j];
			d = gradientHidden[j];
			
			for (int i = 0; i < IN; i++) {
				g = ALPHA * d * I[i];
				w[i] += g + momentum * deltaW[j][i];
			}
			
			hiddenBias[j] += BETA * d;
		}

		return error;
	}

	public HL2Network(final int in, final int hid, final int out) {
		super(in, hid, out);
		
		H2 = new float[HID];
		W2 = new float[HID][HID];
		
		deltaW2 = new float[HID][HID];
		hiddenBias2 = new float[HID];
		gradientHidden2 = new float[HID];
		
		initWeight(W2);
		initThreshold(hiddenBias2);
	}

	@Override
	public void save(final OutputStream outputStream) throws IOException {
	    final DataOutputStream dos = new DataOutputStream(outputStream);
	    
	    for (int i = 0; i < W.length; i++)
	    	for (int j = 0; j < I.length; j++)
	    		dos.writeFloat(W[i][j]);

	    for (int i = 0; i < V.length; i++)
	    	for (int j = 0; j < W.length; j++)
	    		dos.writeFloat(V[i][j]);
	    
	    for (int i = 0; i < W2.length; i++)
	    	for (int j = 0; j < W2.length; j++)
	    		dos.writeFloat(W2[i][j]);
	    
	    for (int i = 0; i < hiddenBias.length; i++)
	    	dos.writeFloat(hiddenBias[i]);
	    
	    for (int i = 0; i < hiddenBias2.length; i++)
	    	dos.writeFloat(hiddenBias2[i]);

	    for (int i = 0; i < outputBias.length; i++)
	    	dos.writeFloat(outputBias[i]);
	    	    
	    dos.close();		
	}
	
	@Override
	public void load(final InputStream inputStream) throws IOException {
	    final DataInputStream dos = new DataInputStream(inputStream);

	    for (int i = 0; i < W.length; i++)
	    	for (int j = 0; j < I.length; j++)
	    		W[i][j] = dos.readFloat();

	    for (int i = 0; i < V.length; i++)
	    	for (int j = 0; j < W.length; j++)
	    		V[i][j] = dos.readFloat();
	    
	    for (int i = 0; i < W2.length; i++)
	    	for (int j = 0; j < W2.length; j++)
	    		W2[i][j] = dos.readFloat();
	    
	    for (int i = 0; i < hiddenBias.length; i++)
	    	hiddenBias[i] = dos.readFloat();
	    
	    for (int i = 0; i < hiddenBias2.length; i++)
	    	hiddenBias2[i] = dos.readFloat();

	    for (int i = 0; i < outputBias.length; i++)
	    	outputBias[i] = dos.readFloat();

	    dos.close();
	}
}
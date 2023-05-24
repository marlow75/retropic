package pl.dido.image.utils.neural;

import java.io.IOException;
import java.util.Vector;

import pl.dido.image.utils.Utils;

public class Trainer {
	
	protected static final String PETSCII_CHARSET = "petscii.bin";

	public static final void main(final String args[]) throws IOException {
		final HL2Network neural = new HL2Network();
		
		final Vector<Dataset> samples = NNUtils.loadData(Utils.getResourceAsStream(PETSCII_CHARSET));
		neural.learn(samples);
		neural.save("petscii.network");
	}
}

package pl.dido.image.utils.neural;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Vector;

public interface Network {

	void forward(float data[]);
	
	void load(InputStream inputStream) throws IOException;
	void save(OutputStream outputStream) throws IOException;
	
	float[] getResult();
	void train(Vector<Dataset> samples);
	
	void batchTrain(Vector<Dataset> samples, int batchSize);
	float trainWithValidation(Vector<Dataset> trainSet, Vector<Dataset> validationSet, int validationFrequency, int patience);
	
	void addProgressListener(NetworkProgressListener listener);
}

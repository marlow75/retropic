package pl.dido.image.utils.neural;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Vector;

import pl.dido.image.utils.ProgressListener;

public interface Network {

	void forward(Dataset data);
	
	void load(InputStream inputStream) throws IOException;
	void save(OutputStream outputStream) throws IOException;
	
	float[] getResult();
	void train(Vector<Dataset> samples);
	
	void addProgressListener(ProgressListener listener);
}

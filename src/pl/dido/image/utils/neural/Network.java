package pl.dido.image.utils.neural;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface Network {

	void forward(Dataset data);
	float back(Dataset data);
	
	void load(InputStream inputStream) throws IOException;
	void save(OutputStream outputStream) throws IOException;
	
	float[] getResult();
}

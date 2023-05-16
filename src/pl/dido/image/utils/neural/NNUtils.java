package pl.dido.image.utils.neural;

import java.io.IOException;
import java.io.*;
import java.util.Vector;

public class NNUtils {

	public static Vector<Dataset> loadData(final InputStream inputStream) {
		int byteRead = -1;

		try {
			final Vector<Dataset> result = new Vector<Dataset>();
			
			for (int i = 0; i < 256; i++) { // first 256 characters
				final float[] answer = new float[256];
				final float[] input = new float[8 * 8];

				for (int j = 0; j < answer.length; j++)
					answer[j] = i == j ? 1 : 0;

				for (int j = 0; j < 8; j++) {
					byteRead = inputStream.read();
					if (byteRead == -1)
						break;
					
					input[j * 8] = (byteRead & 128) >> 7;
					input[j * 8 + 1] = (byteRead & 64) >> 6;
					input[j * 8 + 2] = (byteRead & 32) >> 5;
					input[j * 8 + 3] = (byteRead & 16) >> 4;
					input[j * 8 + 4] = (byteRead & 8) >> 3;
					input[j * 8 + 5] = (byteRead & 4) >> 2;
					input[j * 8 + 6] = (byteRead & 2) >> 1;
					input[j * 8 + 7] = (byteRead & 1);
				}
				
				result.add(new Dataset(input, answer));
			}

			return result;
		} catch (IOException ex) {
			return null;
		}
	}
}
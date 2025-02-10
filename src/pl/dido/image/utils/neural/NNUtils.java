package pl.dido.image.utils.neural;

import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;

import at.fhtw.ai.nn.utils.Dataset;

public class NNUtils {

	public static Vector<Dataset> loadData(final InputStream inputStream) {
		int byteRead = -1;

		try {
			final Vector<Dataset> result = new Vector<Dataset>();

			for (int i = 0; i < 256; i++) { // first 256 characters
				final float[] answer = new float[256];
				final float[] input = new float[8 * 8];

				answer[i] = 1f;

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

	public static Vector<Vector<Dataset>> loadBatchData(final InputStream inputStream, final int batchSize) {
		int byteRead = -1;

		try {
			final Vector<Vector<Dataset>> batches = new Vector<Vector<Dataset>>();
			Vector<Dataset> batch = new Vector<Dataset>();
			int counter = 0;
			
			for (int i = 0; i < 256; i++) { // first 256 characters
				final float[] answer = new float[256];
				final float[] input = new float[8 * 8];

				answer[i] = 1f;

				for (int j = 0; j < 8; j++) {
					byteRead = inputStream.read();
					if (byteRead == -1)
						return batches;

					input[j * 8] = (byteRead & 128) >> 7;
					input[j * 8 + 1] = (byteRead & 64) >> 6;
					input[j * 8 + 2] = (byteRead & 32) >> 5;
					input[j * 8 + 3] = (byteRead & 16) >> 4;
					input[j * 8 + 4] = (byteRead & 8) >> 3;
					input[j * 8 + 5] = (byteRead & 4) >> 2;
					input[j * 8 + 6] = (byteRead & 2) >> 1;
					input[j * 8 + 7] = (byteRead & 1);
				}

				batch.add(new Dataset(input, answer));
				if (counter++ == batchSize - 1) {
					batches.add(batch);
					counter = 0;
					
					batch = new Vector<Dataset>();
				}
			}
			
			return batches;
		} catch (IOException ex) {
			return null;
		}
	}
}
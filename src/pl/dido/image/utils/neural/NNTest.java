package pl.dido.image.utils.neural;

import java.io.IOException;

import pl.dido.image.utils.Utils;

public class NNTest {

	public static final void main(final String args[]) {
		final String PETSCII_NETWORK = "petscii.network";
		final String PETSCII_CHARSET = "petscii.bin";
		
		final HL2Network neural = new HL2Network();
		final byte charset[];

		try {
			charset = Utils.loadCharset(Utils.getResourceAsStream(PETSCII_CHARSET));
			neural.load(Utils.getResourceAsStream(PETSCII_NETWORK));
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}

		final float input[] = new float[64];
		for (int i = 0; i < 256; i++) {
			// get similar character

			for (int j = 0; j < 8; j++) {
				final int byteRead = charset[i * 8 + j];

				input[j * 8] = (byteRead & 128) >> 7;
				input[j * 8 + 1] = (byteRead & 64) >> 6;
				input[j * 8 + 2] = (byteRead & 32) >> 5;
				input[j * 8 + 3] = (byteRead & 16) >> 4;
				input[j * 8 + 4] = (byteRead & 8) >> 3;
				input[j * 8 + 5] = (byteRead & 4) >> 2;
				input[j * 8 + 6] = (byteRead & 2) >> 1;
				input[j * 8 + 7] = (byteRead & 1);
			}
			
			neural.forward(new Dataset(input));
			final float[] result = neural.getResult();

			int k = -1;
			float value = 0f;

			// get index of character in charset
			for (int j = 0; j < 256; j++)
				if (result[j] > value) {
					k = j;
					value = result[j];
				}

			if (k != i)
				System.out.println(i + ":" + k);
		}
	}
}

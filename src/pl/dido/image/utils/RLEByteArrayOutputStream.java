package pl.dido.image.utils;

import java.io.ByteArrayOutputStream;

public class RLEByteArrayOutputStream extends ByteArrayOutputStream {

	public RLEByteArrayOutputStream(final int size) {
		super(size);
	}

	@Override
	public byte[] toByteArray() {
		return RLECompression.compress(buf, 128);
	}
}

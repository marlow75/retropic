package pl.dido.image.utils;

import java.io.BufferedOutputStream;
import java.io.OutputStream;

public class ChecksumOutputStream extends BufferedOutputStream {

	private int checksum = 0;
	
	public ChecksumOutputStream(final OutputStream out) {
		super(out);
	}
	
	public ChecksumOutputStream(final OutputStream out, final int size) {
		super(out, size);
	}

	public synchronized void write(final int data) throws java.io.IOException {
		super.write(data);
		checksum += data;
	}	
	
	public int getChecksum() {
		return checksum;
	}
}

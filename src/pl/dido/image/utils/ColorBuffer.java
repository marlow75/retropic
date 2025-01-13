package pl.dido.image.utils;

import java.util.Arrays;

public class ColorBuffer {
	protected int cursor;

	protected int size;
	protected int buffer[];

	public ColorBuffer(final int size) {
		this.size = size;
		buffer = new int[size];

		clear();
	}

	public void write(final int data) {
		cursor = ++cursor % size;
		buffer[cursor] = data;
	}

	public void write(int i, final int data) {
		i = (cursor + i) % size;
		if (i < 0)
			i += size;

		buffer[i] = data;
	}

	public int read(int i) {
		i = (cursor + i) % size;
		if (i < 0)
			i += size;

		return buffer[i];
	}

	public void clear() {
		Arrays.fill(buffer, -1);
		cursor = -1;
	}
	
	public int getCursor() {
		return cursor;
	}

	public int[] getBuffer() {
		return Arrays.copyOf(buffer, cursor + 1);
	}
}

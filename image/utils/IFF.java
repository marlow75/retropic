package pl.dido.image.utils;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class IFF {

	public static final byte[] bigEndianDWORD(final int data) {
		final byte p[] = new byte[4];

		p[0] = (byte) ((data & 0xff000000) >> 24);
		p[1] = (byte) ((data & 0xff0000) >> 16);
		p[2] = (byte) ((data & 0xff00) >> 8);
		p[3] = (byte) (data & 0xff);

		return p;
	}

	public static final byte[] bigEndianWORD(final int data) {
		final byte p[] = new byte[2];

		p[0] = (byte) ((data & 0xff00) >> 8);
		p[1] = (byte) (data & 0xff);

		return p;
	}

	public static final byte[] chunk(final String name, final byte[]... data) throws IOException {
		int size = 0;

		for (final byte[] p : data)
			size += p.length;
		
		final boolean padding = (size % 2 == 1);
		final ByteArrayOutputStream mem = new ByteArrayOutputStream(padding ? size + 1: size);

		mem.write(name.getBytes());
		mem.write(bigEndianDWORD(size));

		for (final byte[] p : data)
			mem.write(p);

		if (padding)
			mem.write(0);
		
		return mem.toByteArray();
	}

	public static final byte[] getBitmap(final int width, final int height, final int bitplanes[][], final boolean compressed) throws IOException {		
		final int planes = bitplanes[0].length;
		final int size = width >> 4;
		
		final ByteArrayOutputStream mem = new ByteArrayOutputStream((width * height / 8) * planes); // bitplanes
		
		if (compressed) {			
			for (int y = 0; y < height; y++)
				for (int plane = 0; plane < planes; plane++) {
					final RLEByteArrayOutputStream out = new RLEByteArrayOutputStream(40);
					
					for (int x = 0; x < size; x++) {
						final int a = size * y + x;
						final int d = bitplanes[a][plane]; // WORD

						out.write(bigEndianWORD(d));
					}
					
					final byte bytes[] = out.toByteArray();
					mem.write(bytes);
				}
		} else { // without compression
			for (int y = 0; y < height; y++)
				for (int plane = 0; plane < planes; plane++)
					for (int x = 0; x < size; x++) {
						final int a = size * y + x;
						final int d = bitplanes[a][plane]; // WORD 2xbytes

						mem.write(bigEndianWORD(d));
					}
		}
		
		return mem.toByteArray();
	}

	public static final byte[] getILBMHD(final int width, final int height, final int aspectX, 
			final int aspectY, final int bitplanes, final boolean compressed) throws IOException {
		final ByteArrayOutputStream mem = new ByteArrayOutputStream(28);

		mem.write(bigEndianWORD(width));
		mem.write(bigEndianWORD(height));
		mem.write(bigEndianWORD(0)); // position on screen X,Y
		mem.write(bigEndianWORD(0));

		mem.write(bitplanes); // hamcode

		mem.write(0); // mask
		mem.write(compressed ? 1 : 0); // compress 0 = none, 1 byte run
		mem.write(0); // padding
		mem.write(bigEndianWORD(0)); // transparent background color
		mem.write(aspectX); // aspect X
		mem.write(aspectY); // aspect Y
		mem.write(bigEndianWORD(width)); // page width
		mem.write(bigEndianWORD(height)); // page height

		return mem.toByteArray();
	}

	public static final byte[] getILBMFormat(final byte[]... chunk) throws IOException {
		final ByteArrayOutputStream mem = new ByteArrayOutputStream(64 * 1024);

		mem.write("FORM".getBytes());

		int size = 0;
		for (final byte[] p : chunk)
			size += p.length;

		mem.write(bigEndianDWORD(size + 4)); // includes ILBM
		mem.write("ILBM".getBytes()); // planar data

		for (final byte[] p : chunk)
			mem.write(p);

		return mem.toByteArray();
	}

	public static final byte[] getCMAP(final int palette[][], final int pixelMode) throws IOException {
		final int size = palette.length;
		final ByteArrayOutputStream mem = new ByteArrayOutputStream(size * 3);

		for (int i = 0; i < size; i++) {
			final int color[] = palette[i];

			switch (pixelMode) {
			case BufferedImage.TYPE_3BYTE_BGR:
				mem.write(color[2]);
				mem.write(color[1]);
				mem.write(color[0]);
				break;
			case BufferedImage.TYPE_INT_RGB:
				mem.write(color[0]);
				mem.write(color[1]);
				mem.write(color[2]);
				break;
			}
		}

		return mem.toByteArray();
	}
}
package pl.dido.image.utils;

public class ColorCache {
	// two way associative cache
	private final int way0[]; // cache 0 way
	private final int way1[]; // cache 1 way

	private final int tag0[]; // hashcode for 0 way
	private final int tag1[]; // hashcode for 1 way

	private final byte lru[]; // lru
	private final int size = 0x1ff; // 512 entries

	private int index;
	private int hash;

	public ColorCache() {
		way0 = new int[size + 1];
		way1 = new int[size + 1];

		tag0 = new int[size + 1];
		tag1 = new int[size + 1];

		lru = new byte[size + 1];
	}

	public final int put(final int r, final int g, final int b, final int data) {
		index = ((b & 7) << 6) | ((r & 7) << 3) | (g & 7);
		hash = (r << 16) | (g << 8) | b; 
				
		if (lru[index] < 0) {
			// < 0
			this.tag1[index] = hash;
			this.way1[index] = data;
		} else {
			// > 0
			this.tag0[index] = hash;
			this.way0[index] = data;
		}
		
		return data;
	}

	public final int get(final int r, final int g, final int b) {
		index = ((b & 7) << 6) | ((r & 7) << 3) | (g & 7);		
		hash = (r << 16) | (g << 8) | b; 
		
		if (tag0[index] == hash) {
			lru[index]--;
			return way0[index];
		} else if (tag1[index] == hash) {
			lru[index]++;
			return way1[index];
		} else
			return -1;
	}
}
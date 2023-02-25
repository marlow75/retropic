package pl.dido.image.utils;

public class RLECompression {

	public static enum MODE {
		NONE, SEQUENCE, VARIETY
	};

	public static final byte[] compress(final byte input[], final int size) {
 		int len = input.length;

		final byte[] compressed = new byte[size];
		final byte var[] = new byte[size]; // variety buffer

		int c = 0;
		int k = 0;

		while (c < len) {
			byte d = input[c];
			int j = c + 1;
			
			byte err = 0;
			byte q = 0;
			byte p = 0;
			
			// check if variety
			while (j < len) {
				p = input[j];
				if (d == p) {// equals
					if (q == 0)
						break;
					else
					if (err > 0) {// literal error 
						q -= 2;
						break;
					}
					err++;
				} else 
					err = 0;

				if (q == 0)
					var[q++] = d;
				
				var[q++] = p;
				d = p;
				
				j++;
			}

			if (q > 0) { // variety?
				compressed[k++] = (byte) (q - 1);
				
				for (int l = 0; l < q; l++)
					compressed[k++] = var[l];
				
				c += q;
			} else
			if (j == len) {
				// write short variety
				compressed[k++] = 0;
				compressed[k++] = d;
				
				break;
			}
			
			if (c == len)
				break;
			
			// not a variety so assume sequence
			d = input[c];
			j = c + 1;
			
			while (j < len && d == input[j])
				j++;

			if (j > c + 1) { // write sequence
				compressed[k++] = (byte) (-1 * (j - c - 1));
				compressed[k++] = (byte) d;
				c = j;
			} else
				c += 1;
		}

		final byte result[] = new byte[k];
		System.arraycopy(compressed, 0, result, 0, k);
		
		return result;
	}
	
	public static final byte[] decompress(final byte input[], final int size) {
		final byte[] uncompressed = new byte[size];
		MODE mode = MODE.NONE;
		
		int counter = 0;
		int k = 0, i = 0;		
		byte data = 0;
		
		while (i < input.length) {
			if (counter > 0) {
				counter--;
				
				switch (mode) {
				case SEQUENCE:
					uncompressed[k++] = data;
					continue;
				case VARIETY:
					uncompressed[k++] = input[i++];
					continue;
				default:
					// nothing
				}				
			}
			
			byte d = input[i++];
			
			if (0 <= d && d <= 127) {
				mode = MODE.VARIETY;
				counter = d + 1;
			} else
			if (-1 >= d && d >= -127) {
				mode = MODE.SEQUENCE;
				counter = -1 * d + 1;
				data = input[i++];
			}
		}
		
		while (counter > 0) {
			counter--;
			
			switch (mode) {
			case SEQUENCE:
				uncompressed[k++] = data;
				continue;			
			default:
				// nothing
			}				
		}
		
		final byte result[] = new byte[k];
		System.arraycopy(uncompressed, 0, result, 0, k);

		return result;
	}
}
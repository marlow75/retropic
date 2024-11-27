package pl.dido.image.utils.neural;

import pl.dido.image.utils.Gfx;

public class MCISOMPalette extends SOMPalette {
	
	protected byte rgb[];

	public MCISOMPalette(final float rate, final float radius, final int epoch, final byte rgb[]) {
		super(2, 2, rate, radius, epoch);
		this.rgb = rgb;
	}

	@Override
	protected void matrixInit() {
		matrix = new int[width][height][3];

		int r = 0, g = 0, b = 0, b1 = 0, b2 = 0, b3 = 0, b4 = 0;
		float l1 = Float.MAX_VALUE, l2 = Float.MAX_VALUE, l3 = Float.MAX_VALUE, l4 = Float.MAX_VALUE;
		
		for (int i = 0; i < rgb.length; i += 3) {
			r = rgb[i] & 0xff;
			g = rgb[i + 1] & 0xff;
			b = rgb[i + 2] & 0xff;
			
			float p = Gfx.euclideanDistance(r, g, b, 0, 0, 0);
			if (p < l1) {
				b1 = i;
				l1 = p;
			}
			
			p = Gfx.euclideanDistance(r, g, b, 255, 255, 255);
			if (p < l2) {
				b2 = i;
				l2 = p;
			}
			
			p = Gfx.euclideanDistance(r, g, b, 255, 128, 255);
			if (p < l3) {
				b3 = i;
				l3 = p;
			}
			
			p = Gfx.euclideanDistance(r, g, b, 128, 255, 128);
			if (p < l4) {
				b4 = i;
				l4 = p;
			}
		}
				
		matrix[0][0][0] = rgb[b1] & 0xff;
		matrix[0][0][1] = rgb[b1 + 1] & 0xff;
		matrix[0][0][2] = rgb[b1 + 2] & 0xff;
		
		matrix[0][1][0] = rgb[b2] & 0xff;
		matrix[0][1][1] = rgb[b2 + 1] & 0xff;
		matrix[0][1][2] = rgb[b2 + 2] & 0xff;
		
		matrix[1][1][0] = rgb[b3] & 0xff;
		matrix[1][1][1] = rgb[b3 + 1] & 0xff;
		matrix[1][1][2] = rgb[b3 + 2] & 0xff;
		
		matrix[1][0][0] = rgb[b4] & 0xff;
		matrix[1][0][1] = rgb[b4 + 1] & 0xff;
		matrix[1][0][2] = rgb[b4 + 2] & 0xff;
	}
}
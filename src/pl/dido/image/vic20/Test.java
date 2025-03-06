package pl.dido.image.vic20;

public class Test {

	protected final static float diceSimilarity(final float[] a, final float[] b) {
		float tp = 0f, fn = 0, fp = 0f;

		for (int color = 0; color < 4; color++)
			for (int i = 0; i < 32; i++) {
				tp += (Math.round(a[i]) == color) && (Math.round(b[i]) == color) ? 1f : 0f;
				fp += (Math.round(a[i]) != color) && (Math.round(b[i]) == color) ? 1f : 0f;
				fn += (Math.round(a[i]) == color) && (Math.round(b[i]) != color) ? 1f : 0f;
			}

		return 2f * tp / (2f * tp + fp + fn);
	}

	public static void main(String[] args) {
		float[] m1 = new float[] { 
				2f, 2f, 2f, 2f, 
				2f, 2f, 2f, 2f, 
				2f, 2f, 2f, 2f, 
				2f, 2f, 2f, 2f, 
				2f, 2f, 2f, 2f, 
				2f, 2f, 2f, 2f, 
				2f, 2f, 2f, 2f, 
				2f, 2f, 2f, 2f };
		
		float[] m2 = new float[] { 
				0f, 0f, 0f, 0f, 
				2f, 0f, 2f, 2f, 
				0f, 0f, 0f, 0f, 
				0f, 0f, 0f, 0f, 
				0f, 0f, 0f, 0f, 
				2f, 0f, 2f, 2f, 
				0f, 0f, 0f, 0f, 
				0f, 0f, 0f, 0f };
		
		float[] m3 = new float[] { 
				1f, 1f, 1f, 1f, 
				1f, 1f, 1f, 1f, 
				1f, 1f, 1f, 1f, 
				1f, 1f, 1f, 1f, 
				1f, 1f, 1f, 1f, 
				1f, 1f, 1f, 1f, 
				1f, 1f, 1f, 1f, 
				1f, 1f, 1f, 1f };
		
		System.out.println(diceSimilarity(m1, m2));
		System.out.println(diceSimilarity(m1, m3));
	}
}

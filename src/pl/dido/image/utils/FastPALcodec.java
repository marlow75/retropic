package pl.dido.image.utils;

public class FastPALcodec {
	private static float luminance[];
	private static float chrominance[];

	private final static float SCANLINE_TIME = 0.000064f; // microseconds
	private final static float FRAME_TIME = 0.020f; 

	private final static float LINE_SYNC = 0.0000047f;
	private final static float BACK_PORCH = 0.0000057f;
	private final static float FRONT_PORCH = 0.000001618f;
	
	private final static float SUBCARRIER_FREQ = 4.43361875e6f;
	private final static int FRAME_SIZE = 720 * 610;

	private static float dotX;
	private static float dotY;

	private static int width;
	private static float dotClock;

	public static int data[];
	public static int src[];
	
	private static float sin[];
	private static float cos[];
	
	private final static float oscilator0(final float t) {
		return (float) Math.sin(2 * Math.PI * SUBCARRIER_FREQ * t);
	}

	private final static float oscilator90(final float t) {
		return (float) Math.cos(2 * Math.PI * SUBCARRIER_FREQ * t);
	}

	private static final float getLuminance(final float r, final float g, final float b) {
		return 0.299f * r + 0.587f * g + 0.114f * b;
	}

	public static final void init(final int width, final int height) {
		dotX = width / 720f;
		dotY = height / 610f;

		FastPALcodec.width = width;
		
		luminance = new float[FRAME_SIZE];
		chrominance = new float[FRAME_SIZE];

		dotClock = (SCANLINE_TIME - (FRONT_PORCH + LINE_SYNC + BACK_PORCH)) / 720f;
		prepareSinCosTables();
	}

	private static void prepareSinCosTables() {
		sin = new float[720 * 610];
		cos = new float[720 * 610];
		
		float t = FRAME_TIME + 5 * SCANLINE_TIME; // skip 5 scan line
		int index = 0;

		for (int y0 = 0; y0 < 610; y0 += 2) {			
			t += LINE_SYNC + BACK_PORCH;

			for (int x0 = 0; x0 < 720; x0++) {
				sin[index] = oscilator0(t);
				cos[index] = oscilator90(t);

				t += dotClock;
				index++;
			}
			
			t += FRONT_PORCH;
		}

		t += 7 * SCANLINE_TIME;

		for (int y0 = 1; y0 < 610; y0 += 2) {
			t += LINE_SYNC + BACK_PORCH;

			for (int x0 = 0; x0 < 720; x0++) {
				sin[index] = oscilator0(t);
				cos[index] = oscilator90(t);

				t += dotClock;
				index++;
			}
			
			t += FRONT_PORCH;
		}
	}

	public static void encodeYC() {
		final int len = src.length;
		int index = 0;

		for (int y0 = 0; y0 < 610; y0 += 2) {
			final int a = (int) (y0 * dotY) * width;
			final int even = -1 * (2 * (y0 % 2) - 1);

			for (int x0 = 0; x0 < 720; x0++) {
				final int p = a + (int) (x0 * dotX);
				final int d = src[p];

				final float r;
				final float g;
				final float b;

				if (p < len - 3) {
					r = ((d >> 16) & 0xff) / 255f;
					g = ((d >> 8) & 0xff) / 255f;
					b = (d & 0xff) / 255f;
				} else {
					r = 0f;
					g = 0f;
					b = 0f;
				}

				// get luminance component
				final float y = getLuminance(r, g, b);
				luminance[index] = y;

				final float u = 0.492f * (b - y);
				final float v = 0.877f * (r - y);

				// get chrominance component
				// even = PAL switching
				chrominance[index] = u * sin[index] + even * v * cos[index];
				index++;
			}
		}

		for (int y0 = 1; y0 < 610; y0 += 2) {
			final int a = (int) (y0 * dotY) * width;
			final int even = -1 * (2 * (y0 % 2) - 1);

			for (int x0 = 0; x0 < 720; x0++) {
				final int p = a + (int) (x0 * dotX);
				final int d = src[p];

				final float r;
				final float g;
				final float b;

				if (p < len - 3) {
					r = ((d >> 16) & 0xff) / 255f;
					g = ((d >> 8) & 0xff) / 255f;
					b = (d & 0xff) / 255f;
				} else {
					r = 0f;
					g = 0f;
					b = 0f;
				}

				// get luminance component
				final float y = getLuminance(r, g, b);
				luminance[index] = y;

				final float u = 0.492f * (b - y);
				final float v = 0.877f * (r - y);

				// get chrominance component
				// even = PAL switching
				chrominance[index] = u * sin[index] + even * v * cos[index];

				index++;
			}
		}
	}

	public static void decodeYC() {
		int index = 0;

		for (int y0 = 0; y0 < 610; y0 += 2) {
			final int even = -1 * (2 * (y0 % 2) - 1);			
			final int a1 = y0 * 720;

			for (int x0 = 0; x0 < 720; x0++) {
				final float c = 2 * chrominance[index];

				final float v = even * c * cos[index];
				final float u = c * sin[index];

				final float y = luminance[index];

				final float b = u / 0.492f + y;
				final float r = v / 0.877f + y;

				final float g = (y - 0.299f * r - 0.114f * b) / 0.587f;
				final int a2 = a1 + x0;

				data[a2]  = ((int) (saturate(r) * 255)) << 16;
				data[a2] |= ((int) (saturate(g) * 255)) << 8;
				data[a2] |= ((int) (saturate(b) * 255));

				index++;
			}
		}

		for (int y0 = 1; y0 < 610; y0 += 2) {
			final int even = -1 * (2 * (y0 % 2) - 1);
			final int a = y0 * 720;

			for (int x0 = 0; x0 < 720; x0++) {
				final float c = 2 * chrominance[index];

				final float v = even * c * cos[index];
				final float u = c * sin[index];

				final float y = luminance[index];

				final float b = u / 0.492f + y;
				final float r = v / 0.877f + y;

				final float g = (y - 0.299f * r - 0.114f * b) / 0.587f;
				final int p = a + x0;

				data[p]  = (int)(saturate(r) * 255) << 16;
				data[p] |= (int)(saturate(g) * 255) << 8;
				data[p] |= (int)(saturate(b) * 255);

				index++;
			}
		}
	}

	private final static float saturate(final float data) {
		return data < 0 ? 0 : data > 1 ? 1 : data;
	}
}
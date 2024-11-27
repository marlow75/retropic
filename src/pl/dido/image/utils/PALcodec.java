package pl.dido.image.utils;

public class PALcodec {
	private static float luminance[];
	private static float chrominance[];
	
	private final static float SCANLINE_TIME = 0.0000640f; // in micro seconds
	private final static float FRAME_TIME    = 0.020f;
	
	private final static float LINE_SYNC     = 0.0000047f;
	private final static float BACK_PORCH    = 0.0000057f;
	private final static float FRONT_PORCH   = 0.00000165f;
	
	private final static float SUBCARRIER_FREQ = 4.43361875e6f;

	public final static int WIDTH = 720;
	public final static int HEIGHT = 610;
	private final static int FRAME_SIZE = WIDTH * HEIGHT;

	private final static float scale(final float data) {
		return data / 255;
	}

	private final static float saturate(final float data) {
		return data < 0 ? 0 : data > 1 ? 1 : data;
	}
	
	private final static float oscilator0(final float a, final float t) {
		return a * (float) Math.sin(2 * Math.PI * SUBCARRIER_FREQ * t);
	}

	private final static float oscilator90(final float a, final float t) {
		return a * (float) Math.cos(2 * Math.PI * SUBCARRIER_FREQ * t);
	}

	public static final float getLuminance(final float r, final float g, final float b) {
		return 0.299f * r + 0.587f * g + 0.114f * b;
	}

	public static void encodeYC(final int width, final int height, final byte data[]) {
		float t = FRAME_TIME + 5 * SCANLINE_TIME; // skip 5 scan line
		final float dotClock = (SCANLINE_TIME - (FRONT_PORCH + LINE_SYNC + BACK_PORCH)) / WIDTH;

		final float dotX = (width * 1f) / WIDTH;
		final float dotY = (height * 1f) / HEIGHT;

		final int len = data.length;

		luminance = new float[FRAME_SIZE];
		chrominance = new float[FRAME_SIZE];

		int index = 0;

		for (int y0 = 0; y0 < HEIGHT; y0 += 2) {
			final int a = (int) (y0 * dotY) * width * 3;
			final int even = -1 * (2 * (y0 % 2) - 1);
			t += LINE_SYNC + BACK_PORCH;

			for (int x0 = 0; x0 < WIDTH; x0++) {
				final int p = a + (int) (x0 * dotX) * 3;
				final float b, g, r;

				if (p < len - 3) {
					b = scale(data[p + 0] & 0xff);
					g = scale(data[p + 1] & 0xff);
					r = scale(data[p + 2] & 0xff);
				} else {
					b = 0f;
					g = 0f;
					r = 0f;
				}

				// get luminance component
				final float y = getLuminance(r, g, b);
				luminance[index] = y;

				final float u = 0.492f * (b - y);
				final float v = 0.877f * (r - y);

				// get chrominance component
				final float c = oscilator0(u, t) + even * oscilator90(v, t);
				chrominance[index] = c;

				t += dotClock;
				index++;
			}
			
			t += FRONT_PORCH;
		}
		
		t += 7 * SCANLINE_TIME;
		
		for (int y0 = 1; y0 < HEIGHT; y0 += 2) {
			final int a = (int) (y0 * dotY) * width * 3;
			final int even = -1 * (2 * (y0 % 2) - 1);
			t += LINE_SYNC + BACK_PORCH;

			for (int x0 = 0; x0 < WIDTH; x0++) {
				final int p = a + (int) (x0 * dotX) * 3;
				final float b, g, r;

				if (p < len - 3) {
					b = scale(data[p + 0] & 0xff);
					g = scale(data[p + 1] & 0xff);
					r = scale(data[p + 2] & 0xff);
				} else {
					b = 0;
					g = 0;
					r = 0;
				}

				// get luminance component
				final float y = getLuminance(r, g, b);
				luminance[index] = y;

				final float u = 0.492f * (b - y);
				final float v = 0.877f * (r - y);

				// get chrominance component
				final float c = oscilator0(u, t) + even * oscilator90(v, t);
				chrominance[index] = c;

				t += dotClock;
				index++;
			}
			
			t += FRONT_PORCH; 
		}
	}

	public static void decodeYC(final byte data[]) {
		float t = FRAME_TIME + 5 * SCANLINE_TIME; // skip 5 scan lines
		final float dotClock = (SCANLINE_TIME - (FRONT_PORCH + LINE_SYNC + BACK_PORCH)) / WIDTH;

		int index = 0;
		final int len = data.length;

		for (int y0 = 0; y0 < HEIGHT; y0 += 2) {
			final int even = -1 * (2 * (y0 % 2) - 1);
			final int a = y0 * WIDTH * 3;
			t += LINE_SYNC + BACK_PORCH;
			
			for (int x0 = 0; x0 < WIDTH; x0++) {
				final int p = a + x0 * 3;
				if (p < len - 3) {
					final float c = 2f * chrominance[index];

					final float v = even * oscilator90(c, t);
					final float u = oscilator0(c, t);

					final float y = luminance[index];

					final float b = u / 0.492f + y;
					final float r = v / 0.877f + y;

					final float g = (y - 0.299f * r - 0.114f * b) / 0.587f;

					data[p + 0] = (byte) (saturate(b) * 255);
					data[p + 1] = (byte) (saturate(g) * 255);
					data[p + 2] = (byte) (saturate(r) * 255);
				}

				t += dotClock;
				index++;
			}
			
			t += FRONT_PORCH;
		}
		
		t += 7 * SCANLINE_TIME;
		
		for (int y0 = 1; y0 < HEIGHT; y0 += 2) {
			final int even = -1 * (2 * (y0 % 2) - 1);
			final int a = y0 * WIDTH * 3;
			
			t += LINE_SYNC + BACK_PORCH;
			
			for (int x0 = 0; x0 < WIDTH; x0++) {
				final int p = a + x0 * 3;
				if (p < len - 3) {
					final float c = 2f * chrominance[index];

					final float v = even * oscilator90(c, t);
					final float u = oscilator0(c, t);

					final float y = luminance[index];

					final float b = u / 0.492f + y;
					final float r = v / 0.877f + y;

					final float g = (y - 0.299f * r - 0.114f * b) / 0.587f;

					data[p + 0] = (byte) (saturate(b) * 255);
					data[p + 1] = (byte) (saturate(g) * 255);
					data[p + 2] = (byte) (saturate(r) * 255);
				}

				t += dotClock;
				index++;
			}
			
			t += FRONT_PORCH;
		}
	}
}
package pl.dido.image.utils;

import java.util.Random;

public class PALcodec {

	private static float luminance[];
	private static float chrominance[];

	private final static float SCANLINE_TIME = 0.0000640f; // in seconds (approx)

	private final static float LINE_SYNC = 0.0000047f;
	private final static float BACK_PORCH = 0.0000057f;
	private final static float FRONT_PORCH = 0.000001651f;

	private final static float SUBCARRIER_FREQ = 4.43361875e6f;

	public final static int WIDTH = 720;
	public final static int HEIGHT = 576;
	private final static int FRAME_SIZE = WIDTH * HEIGHT;

	// --- CRT effect parameters (enabled by default) ---
	private static final boolean CRT_SCANLINES = true;
	private static final float CRT_SCANLINE_STRENGTH = 0.6f; // 0..1

	private static final boolean CRT_SHADOW_MASK = true;
	private static final float CRT_SHADOW_MASK_STRENGTH = 0.12f; // 0..1

	private static final boolean CRT_VIGNETTE = true;
	private static final float CRT_VIGNETTE_STRENGTH = 0.25f; // 0..1

	private static final boolean CRT_BLOOM = true;
	private static final float CRT_BLOOM_INTENSITY = 0.45f;
	private static final float CRT_BLOOM_SIGMA = 2.0f;
	private static final float CRT_BLOOM_THRESHOLD = 0.7f;

	private static final boolean CRT_CHROM_AB = true;
	private static final float CRT_CHROM_AB_OFFSET = 0.8f; // pixels

	private static final boolean CRT_NOISE = true;
	private static final float CRT_NOISE_AMOUNT = 0.01f; // additive noise

	private static final Random random = new Random(12345L);

	// Gamma LUT and saturation boost
	private static final float CRT_GAMMA_POWER = 1.2f; // same as used previously
	private static final float SATURATION_BOOST = 1.8f; // previous hsv[1] *= 1.8f
	private static final int[] GAMMA_LUT = new int[256];

	static {
		buildGammaLUT(CRT_GAMMA_POWER);
	}

	private static void buildGammaLUT(final float gammaPower) {
		final float inv = 1.0f / gammaPower; // because earlier gamma(component, power) = pow(component, 1/power)
		
		for (int i = 0; i < 256; i++) {
			final float v = i / 255f;
			final int mapped = Gfx.saturate((int) Math.round(Math.pow(v, inv) * 255.0));
			
			GAMMA_LUT[i] = mapped;
		}
	}

	private final static float scale(final float component) {
		return component / 255f;
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

	public static void encodeYC(final int width, final int height, final byte data[], final boolean bw) {
		float t = 5 * SCANLINE_TIME; // skip initial lines
		final float dotClock = (SCANLINE_TIME - (LINE_SYNC + BACK_PORCH + FRONT_PORCH)) / WIDTH;

		final float dotX = (width * 1f) / WIDTH;
		final float dotY = (height * 1f) / HEIGHT;

		final int len = data.length;

		luminance = new float[FRAME_SIZE];
		chrominance = new float[FRAME_SIZE];

		int index = 0;

		// iterate lines in natural order
		for (int y0 = 0; y0 < HEIGHT; y0++) {
			final int a = (int) (y0 * dotY) * width * 3;
			float t0 = t + LINE_SYNC + BACK_PORCH;
			final boolean oddLine = (y0 % 2) == 1;

			for (int x0 = 0; x0 < WIDTH; x0++) {
				final int p = a + (int) (x0 * dotX) * 3;
				float b, g, r;
				
				if (p <= len - 3) {
					b = scale(data[p] & 0xff);
					g = scale(data[p + 1] & 0xff);
					r = scale(data[p + 2] & 0xff);
				} else
					b = g = r = 0f;

				final float y = getLuminance(r, g, b);
				luminance[index] = y;

				if (!bw) {
					final float u = 0.493f * (b - y);
					final float v = 0.877f * (r - y);

					// modulate chroma onto subcarrier; invert 90° term on odd lines (PAL)
					final float c = oscilator0(u, t0) + (oddLine ? -oscilator90(v, t0) : oscilator90(v, t0));
					chrominance[index] = c;
				} else
					chrominance[index] = 0f;

				t0 += dotClock;
				index++;
			}

			t += SCANLINE_TIME;
		}
	}

	public static void decodeYC(final byte data[], final boolean bw) {

		// intermediate float RGB buffers
		final int N = FRAME_SIZE;

		final float[] rArr = new float[N];
		final float[] gArr = new float[N];
		final float[] bArr = new float[N];

		float t = 5 * SCANLINE_TIME;
		final float dotClock = (SCANLINE_TIME - (LINE_SYNC + BACK_PORCH + FRONT_PORCH)) / WIDTH;

		int index = 0;

		// reconstruct into float buffers line by line
		for (int y0 = 0; y0 < HEIGHT; y0++) {
			float t0 = t + LINE_SYNC + BACK_PORCH;

			final boolean oddLine = (y0 % 2) == 1;
			float oldLuminance = 1f; // predictor as before

			for (int x0 = 0; x0 < WIDTH; x0++) {
				final float c = chrominance[index];

				final float u_comp = oscilator0(c, t0);
				final float v_comp = oddLine ? -oscilator90(c, t0) : oscilator90(c, t0);

				final float y = (luminance[index] + oldLuminance) / 2f;
				oldLuminance = y;

				if (bw) {
					final float gray = saturate(y);

					rArr[index] = gray;
					gArr[index] = gray;
					bArr[index] = gray;
				} else {

					final float b = u_comp / 0.493f + y;
					final float r = v_comp / 0.877f + y;
					final float g = (y - 0.299f * r - 0.114f * b) / 0.587f;

					rArr[index] = r;
					gArr[index] = g;
					bArr[index] = b;
				}

				t0 += dotClock;
				index++;
			}

			t += SCANLINE_TIME;
		}

		// --- Apply CRT effects ---
		if (CRT_SCANLINES)
			applyScanlines(rArr, gArr, bArr, CRT_SCANLINE_STRENGTH);
		if (CRT_SHADOW_MASK)
			applyShadowMask(rArr, gArr, bArr, CRT_SHADOW_MASK_STRENGTH);
		if (CRT_VIGNETTE)
			applyVignette(rArr, gArr, bArr, CRT_VIGNETTE_STRENGTH);
		if (CRT_BLOOM)
			applyBloom(rArr, gArr, bArr, CRT_BLOOM_INTENSITY, CRT_BLOOM_SIGMA, CRT_BLOOM_THRESHOLD);
		if (CRT_CHROM_AB)
			applyChromaticAberration(rArr, gArr, bArr, CRT_CHROM_AB_OFFSET);
		if (CRT_NOISE)
			applyNoise(rArr, gArr, bArr, CRT_NOISE_AMOUNT, random);

		applyBrightness(rArr, gArr, bArr, 1.5f);

		// --- convert back to byte[] B,G,R order (optimized: LUT + RGB-space saturation boost) ---
		for (int y = 0; y < HEIGHT; y++) {
			final int row = y * WIDTH;
			
			for (int x = 0; x < WIDTH; x++) {
				final int i = row + x;

				// saturate float channels to [0,1]
				float rF = saturate(rArr[i]);
				float gF = saturate(gArr[i]);
				float bF = saturate(bArr[i]);

				// approximate saturation boost in RGB space:
				final float L = 0.299f * rF + 0.587f * gF + 0.114f * bF;
				rF = L + (rF - L) * SATURATION_BOOST;
				gF = L + (gF - L) * SATURATION_BOOST;
				bF = L + (bF - L) * SATURATION_BOOST;

				// clamp again after boost
				rF = saturate(rF);
				gF = saturate(gF);
				bF = saturate(bF);

				// map through gamma LUT
				final int rByte = Gfx.saturate((int) (rF * 255f + 0.5f));
				final int gByte = Gfx.saturate((int) (gF * 255f + 0.5f));
				final int bByte = Gfx.saturate((int) (bF * 255f + 0.5f));

				final int rInt = GAMMA_LUT[rByte];
				final int gInt = GAMMA_LUT[gByte];
				final int bInt = GAMMA_LUT[bByte];

				final int p = y * WIDTH * 3 + x * 3;
				// B,G,R order
				data[p] = (byte) (bInt & 0xff);
				data[p + 1] = (byte) (gInt & 0xff);
				data[p + 2] = (byte) (rInt & 0xff);
			}
		}
	}

	// ------------------ CRT effect helpers ------------------
	private static final void applyScanlines(final float[] r, final float[] g, final float[] b, final float strength) {
		for (int y = 0; y < HEIGHT; y++) {
			final float factor = 1.0f - strength * (y % 2 == 0 ? 0.18f : 0.55f);
			final int base = y * WIDTH;

			for (int x = 0; x < WIDTH; x++) {
				final int i = base + x;

				r[i] *= factor;
				g[i] *= factor;
				b[i] *= factor;
			}
		}
	}

	private static final void applyVignette(final float[] r, final float[] g, final float[] b, final float strength) {
		final float cx = (WIDTH - 1) * 0.5f;
		final float cy = (HEIGHT - 1) * 0.5f;

		final float invCx = 1f / cx;
		final float invCy = 1f / cy;

		for (int y = 0; y < HEIGHT; y++) {
			final int base = y * WIDTH;

			for (int x = 0; x < WIDTH; x++) {
				final int i = base + x;

				final float dx = (x - cx) * invCx;
				final float dy = (y - cy) * invCy;

				final float d = (float) Math.sqrt(dx * dx + dy * dy);
				final float vign = 1f - strength * smoothstep(0.35f, 1.0f, d);

				r[i] *= vign;
				g[i] *= vign;
				b[i] *= vign;
			}
		}
	}

	private static final float smoothstep(final float edge0, final float edge1, final float x) {
		final float t = (x - edge0) / (edge1 - edge0);

		if (t <= 0)
			return 0f;

		if (t >= 1)
			return 1f;

		return t * t * (3f - 2f * t);
	}

	private static final void applyShadowMask(final float[] r, final float[] g, final float[] b, final float strength) {
		// simple triad mask repeating horizontally (R G B)
		for (int y = 0; y < HEIGHT; y++) {
			final int base = y * WIDTH;

			for (int x = 0; x < WIDTH; x++) {
				final int i = base + x;
				final int mod = x % 3;

				final float mR = (mod == 0) ? 1f : 0.86f;
				final float mG = (mod == 1) ? 1f : 0.86f;
				final float mB = (mod == 2) ? 1f : 0.86f;

				r[i] = r[i] * (1f - strength) + r[i] * mR * strength;
				g[i] = g[i] * (1f - strength) + g[i] * mG * strength;
				b[i] = b[i] * (1f - strength) + b[i] * mB * strength;
			}
		}
	}

	private static final void applyChromaticAberration(final float[] r, final float[] g, final float[] b, final float offsetPx) {
		final float[] rCopy = r.clone();
		final float[] bCopy = b.clone();

		final int ox = Math.round(offsetPx);

		for (int y = 0; y < HEIGHT; y++) {
			final int row = y * WIDTH;

			for (int x = 0; x < WIDTH; x++) {
				final int i = row + x;
				final int rx = Gfx.saturate(x + ox, 0, WIDTH - 1);
				final int bx = Gfx.saturate(x - ox, 0, WIDTH - 1);

				r[i] = rCopy[row + rx];
				b[i] = bCopy[row + bx];
				// g remains unchanged
			}
		}
	}

	private static final void applyNoise(final float[] r, final float[] g, final float[] b, final float amount, final Random rnd) {
		if (amount <= 0f)
			return;

		final int N = r.length;
		for (int i = 0; i < N; i++) {
			final float n = (rnd.nextFloat() * 2f - 1f) * amount;

			r[i] = saturate(r[i] + n);
			g[i] = saturate(g[i] + n);
			b[i] = saturate(b[i] + n);
		}
	}

	private static final void applyBloom(final float[] r, final float[] g, final float[] b, final float intensity, final float sigma, final float threshold) {
		if (intensity <= 0f)
			return;

		final int N = r.length;
		final float[] lum = new float[N];

		for (int i = 0; i < N; i++) {
			final float L = 0.2126f * r[i] + 0.7152f * g[i] + 0.0722f * b[i];
			lum[i] = Math.max(0f, L - threshold);
		}

		final float[] tmp = new float[N];
		final float[] kernel = makeGaussianKernel(sigma);
		separableBlur(lum, tmp, kernel);

		for (int i = 0; i < N; i++) {
			final float bloom = lum[i] * intensity;

			r[i] = saturate(r[i] + bloom);
			g[i] = saturate(g[i] + bloom);
			b[i] = saturate(b[i] + bloom);
		}
	}

	private static final float[] makeGaussianKernel(final float sigma) {
		final int radius = Math.max(1, (int) Math.ceil(3f * sigma));
		final int size = radius * 2 + 1;

		final float[] kernel = new float[size];
		float sum = 0f;

		final float denom = 2f * sigma * sigma;
		for (int i = -radius; i <= radius; i++) {
			final float v = (float) Math.exp(-(i * i) / denom);
			kernel[i + radius] = v;
			sum += v;
		}

		for (int i = 0; i < size; i++)
			kernel[i] /= sum;

		return kernel;
	}

	private static final void separableBlur(final float[] src, float[] tmp, float[] kernel) {
		final int radius = kernel.length / 2;

		// horizontal pass
		for (int y = 0; y < HEIGHT; y++) {
			final int row = y * WIDTH;

			for (int x = 0; x < WIDTH; x++) {
				float s = 0f;

				for (int k = -radius; k <= radius; k++) {
					int xx = x + k;

					if (xx < 0)
						xx = 0;
					else
					if (xx >= WIDTH)
						xx = WIDTH - 1;

					s += src[row + xx] * kernel[k + radius];
				}

				tmp[row + x] = s;
			}
		}

		// vertical pass
		for (int x = 0; x < WIDTH; x++)
			for (int y = 0; y < HEIGHT; y++) {
				float s = 0f;
				for (int k = -radius; k <= radius; k++) {
					int yy = y + k;
					if (yy < 0)
						yy = 0;
					else
					if (yy >= HEIGHT)
						yy = HEIGHT - 1;

					s += tmp[yy * WIDTH + x] * kernel[k + radius];
				}

				src[y * WIDTH + x] = s;
			}
	}

	private static final void applyBrightness(final float[] r, final float[] g, final float[] b, final float factor) {
		if (factor == 1f)
			return;

		final int N = r.length;
		for (int i = 0; i < N; i++) {
			r[i] = saturate(r[i] * factor);
			g[i] = saturate(g[i] * factor);
			b[i] = saturate(b[i] * factor);
		}
	}
}
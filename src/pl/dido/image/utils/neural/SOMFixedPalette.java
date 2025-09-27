package pl.dido.image.utils.neural;

import java.util.concurrent.ThreadLocalRandom;

import pl.dido.image.utils.Gfx;

public class SOMFixedPalette {

	protected float matrix[][][]; // [height][width][3]
	protected int width, height;

	protected float rate = 0.4f; // domyœlne
	protected float radius = 1.5f;

	protected int epoch = 20;
	protected float scale;

	protected int skip; // subsampling (1 = wszystkie próbki)

	private void initialize(final int width, final int height, final float rate, final float radius, final int epoch,
			final int bits, final int skip) {

		this.width = width;
		this.height = height;

		this.rate = rate;
		this.radius = radius;
		this.epoch = epoch;

		this.scale = 255f / ((1 << bits) - 1);
		this.skip = Math.max(1, skip); // 1 = bez pomijania
	}

	public SOMFixedPalette(final int width, final int height, final int bits) {
		initialize(width, height, rate, radius, epoch, bits, 1);
	}

	public SOMFixedPalette(final int width, final int height, final float rate, final float radius, final int epoch,
			final int bits) {
		initialize(width, height, rate, radius, epoch, bits, 1);
	}

	public SOMFixedPalette(final int width, final int height, final int bits, final int skip) {
		initialize(width, height, rate, radius, epoch, bits, skip);
	}

	public SOMFixedPalette(final int width, final int height, final float rate, final float radius, final int epoch,
			final int bits, final int skip) {
		initialize(width, height, rate, radius, epoch, bits, skip);
	}

	/**
	 * Inicjalizacja macierzy wag — jeœli dostêpne samples to losujemy wagi z próbek
	 * wejœciowych (szybsza konwergencja), w przeciwnym razie czysty los.
	 */
	protected void matrixInit(final byte samples[]) {
		matrix = new float[height][width][3];
		final ThreadLocalRandom rnd = ThreadLocalRandom.current();
		final int sampleCount = (samples == null) ? 0 : (samples.length / 3);

		for (int y = 0; y < height; y++) {
			final float line[][] = matrix[y];
			for (int x = 0; x < width; x++) {
				final float pixel[] = line[x];
				if (sampleCount > 0) {
					int si = rnd.nextInt(sampleCount) * 3;
					pixel[0] = (samples[si] & 0xff) / scale;
					pixel[1] = (samples[si + 1] & 0xff) / scale;
					pixel[2] = (samples[si + 2] & 0xff) / scale;
				} else {
					pixel[0] = rnd.nextFloat() * 255f / scale;
					pixel[1] = rnd.nextFloat() * 255f / scale;
					pixel[2] = rnd.nextFloat() * 255f / scale;
				}
			}
		}
	}

	/**
	 * Trenuje sieæ na dostarczonych próbkach (rgb jako r,g,b,r,g,b,...). Nie
	 * modyfikuje pól instancji rate/radius/epoch. Zwraca paletê jako tablicê
	 * [width*height][3].
	 */
	public int[][] train(final byte rgb[]) {
		// inicjalizacja wag (próbkami jeœli dostêpne)
		matrixInit(rgb);

		final int epochs = Math.max(1, this.epoch);
		final float initialRate = this.rate;

		final float initialRadius = this.radius;
		final float minRadius = 0.5f; // zabezpieczenie przed radius == 0
		final int sampleCount = (rgb == null) ? 0 : (rgb.length / 3);

		// zbuduj listê indeksów próbek (z uwzglêdnieniem skip)
		int[] order;
		if (sampleCount == 0)
			order = new int[] { 0 };
		else {
			// zbieramy indeksy, bierzemy co 'skip'-ty sample
			final int approx = (sampleCount + (skip - 1)) / skip;
			order = new int[approx];

			int pos = 0;
			for (int i = 0; i < sampleCount; i += skip)
				order[pos++] = i;

			if (pos < order.length) {
				int[] tmp = new int[pos];
				System.arraycopy(order, 0, tmp, 0, pos);
				order = tmp;
			}
		}

		final ThreadLocalRandom rnd = ThreadLocalRandom.current();

		// g³ówna pêtla epok
		for (int e = 0; e < epochs; e++) {
			// shuffle order (Fisher-Yates)
			if (order.length > 1) {
				for (int i = order.length - 1; i > 0; i--) {
					final int j = rnd.nextInt(i + 1);
					final int tmp = order[i];

					order[i] = order[j];
					order[j] = tmp;
				}
			}

			// wyk³adnicze t³umienie (mo¿na zmieniæ)
			final float t = (float) e / (float) epochs;
			final float currentRate = initialRate * (float) Math.exp(-t); // maleje wyk³adniczo
			final float currentRadius = Math.max(minRadius, initialRadius * (float) Math.exp(-t));
			final float radius = currentRadius * currentRadius;

			// dla ka¿dej próbki (w permutowanej kolejnoœci)
			for (int idx = 0; idx < order.length; idx++) {
				final int sampleIndex = (sampleCount > 0) ? order[idx] * 3 : 0;

				final float r = (sampleCount > 0) ? ((rgb[sampleIndex] & 0xff) / scale) : (rnd.nextInt(256) / scale);
				final float g = (sampleCount > 0) ? ((rgb[sampleIndex + 1] & 0xff) / scale)
						: (rnd.nextInt(256) / scale);
				final float b = (sampleCount > 0) ? ((rgb[sampleIndex + 2] & 0xff) / scale)
						: (rnd.nextInt(256) / scale);

				final Position best = getBMU(r, g, b);
				learn(best, r, g, b, currentRate, radius);
			}
		}

		return getPalette();
	}

	/**
	 * Zwraca tablicê palety [width*height][3] z poprawnym zaokr¹gleniem i clampem.
	 */
	protected int[][] getPalette() {
		final int result[][] = new int[width * height][3];

		for (int y = 0; y < height; y++) {
			final float line[][] = matrix[y];
			final int base = y * width;

			for (int x = 0; x < width; x++) {
				final float row1[] = line[x];
				final int i = base + x;

				int rr = Math.round(row1[0] * scale);
				int gg = Math.round(row1[1] * scale);
				int bb = Math.round(row1[2] * scale);

				result[i][0] = Gfx.saturate(rr);
				result[i][1] = Gfx.saturate(gg);
				result[i][2] = Gfx.saturate(bb);
			}
		}
		return result;
	}

	protected void learn(final Position best, final float r, final float g, final float b, final float currentRate,
			final float radius) {
		final int bx = best.x;
		final int by = best.y;

		for (int y = 0; y < height; y++) {
			final float line[][] = matrix[y];

			for (int x = 0; x < width; x++) {
				final float dx = bx - x;
				final float dy = by - y;
				final float d2 = dx * dx + dy * dy;

				final float influence = (float) Math.exp(-d2 / (2f * radius));
				final float n = currentRate * influence;
				final float row[] = line[x];

				row[0] += n * (r - row[0]);
				row[1] += n * (g - row[1]);
				row[2] += n * (b - row[2]);
			}
		}
	}

	/**
	 * ZnajdŸ BMU (best matching unit) u¿ywaj¹c odleg³oœci kwadratowej w przestrzeni
	 * kolorów. Porównujemy d^2 bez sqrt dla wydajnoœci.
	 */
	protected Position getBMU(final float red, final float green, final float blue) {
		int bx = 0, by = 0;
		float min = Float.MAX_VALUE;

		for (int y = 0; y < height; y++) {
			final float line[][] = matrix[y];
			for (int x = 0; x < width; x++) {
				final float row[] = line[x];

				final float dr = row[0] - red;
				final float dg = row[1] - green;
				final float db = row[2] - blue;

				final float d2 = dr * dr + dg * dg + db * db;
				if (d2 < min) {
					min = d2;
					bx = x;
					by = y;
				}
			}
		}

		return new Position(bx, by);
	}
}
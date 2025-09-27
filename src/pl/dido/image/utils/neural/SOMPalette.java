package pl.dido.image.utils.neural;

import java.util.concurrent.ThreadLocalRandom;

import pl.dido.image.utils.Gfx;

/**
 * Poprawiona wersja SOMPalette: - matrix jako float[][][] - nie modyfikuje pól
 * instancji epoch/rate/radius podczas trenowania - shuffle próbek w ka¿dej
 * epoce - u¿ycie odleg³oœci kwadratowej (bez sqrt) - inicjalizacja wag próbkami
 * z wejœcia gdy dostêpne
 */
public class SOMPalette {

	protected float matrix[][][]; // [height][width][3] - float dla precyzji
	protected int width, height;

	protected float rate = 0.6f; // defaults
	protected float radius = 2f;
	protected int epoch = 10;

	public SOMPalette(final int width, final int height) {
		this.width = width;
		this.height = height;
	}

	public SOMPalette(final int width, final int height, final float rate, final float radius, int epoch) {
		this.width = width;
		this.height = height;
		this.rate = rate;
		this.radius = radius;
		this.epoch = epoch;
	}

	/**
	 * Inicjalizacja losuj¹c kolory z danych wejœciowych (jeœli dostêpne) — szybsza
	 * konwergencja.
	 */
	protected void matrixInitFromSamples(final byte samples[]) {
		matrix = new float[height][width][3];

		final ThreadLocalRandom rnd = ThreadLocalRandom.current();
		final int sampleCount = (samples == null) ? 0 : samples.length / 3;

		for (int y = 0; y < height; y++) {
			final float line[][] = matrix[y];

			for (int x = 0; x < width; x++) {
				final float rgb[] = line[x];

				if (sampleCount > 0) {
					final int idx = rnd.nextInt(sampleCount) * 3;

					rgb[0] = (samples[idx] & 0xff);
					rgb[1] = (samples[idx + 1] & 0xff);
					rgb[2] = (samples[idx + 2] & 0xff);
				} else {
					rgb[0] = rnd.nextFloat() * 255f;
					rgb[1] = rnd.nextFloat() * 255f;
					rgb[2] = rnd.nextFloat() * 255f;
				}
			}
		}
	}

	/**
	 * Trenuje sieæ na dostarczonych próbkach (rgb jako bytów: r,g,b,r,g,b,...).
	 * Zwraca paletê jako tablicê [width*height][3].
	 */
	public int[][] train(final byte rgb[]) {
		// Inicjalizacja: staraj siê inicjalizowaæ z próbek jeœli s¹ dostêpne
		matrixInitFromSamples(rgb);

		final int epochs = Math.max(1, this.epoch);
		final float initialRate = this.rate;
		final float initialRadius = this.radius;

		final int sampleCount = (rgb == null) ? 0 : rgb.length / 3;

		// przygotuj tablicê indeksów próbek do shuffle
		final int[] order = new int[Math.max(1, sampleCount)];
		for (int i = 0; i < order.length; i++)
			order[i] = i;

		final ThreadLocalRandom rnd = ThreadLocalRandom.current();
		// g³ówna pêtla epok
		for (int e = 0; e < epochs; e++) {
			// shuffle próbek jeœli s¹
			if (sampleCount > 1) {
				// Fisher-Yates
				for (int i = order.length - 1; i > 0; i--) {
					final int j = rnd.nextInt(i + 1);
					final int tmp = order[i];

					order[i] = order[j];
					order[j] = tmp;
				}
			}

			// wyk³adnicze t³umienie (mo¿na dobraæ wspó³czynnik, tu proste exp(-t/epochs))
			final float t = (float) e / (float) epochs;
			final float currentRate = initialRate * (float) Math.exp(-t);
			
			final float currentRadius = Math.max(0.5f, initialRadius * (float) Math.exp(-t)); // radius nieco trzymamy
			final float radius = currentRadius * currentRadius;

			// dla ka¿dej próbki (w permutowanej kolejnoœci)
			final int limit = (sampleCount > 0) ? sampleCount : 1;
			for (int si = 0; si < limit; si++) {
				int sampleIndex = (sampleCount > 0) ? order[si] * 3 : 0;

				final int red = (sampleCount > 0) ? (rgb[sampleIndex] & 0xff)
						: ThreadLocalRandom.current().nextInt(256);
				final int green = (sampleCount > 0) ? (rgb[sampleIndex + 1] & 0xff)
						: ThreadLocalRandom.current().nextInt(256);
				final int blue = (sampleCount > 0) ? (rgb[sampleIndex + 2] & 0xff)
						: ThreadLocalRandom.current().nextInt(256);

				// znajdŸ BMU
				final Position best = getBMU(red, green, blue);

				// zaktualizuj wagi wokó³ BMU
				learn(best, red, green, blue, currentRate, radius);
			}
		}

		// przygotuj wynik (int[][]) - kopiuj i clampuj do [0,255]
		final int result[][] = new int[width * height][3];
		for (int y = 0; y < height; y++) {
			final float line[][] = matrix[y];
			
			for (int x = 0; x < width; x++) {
				final float f0 = line[x][0];
				final float f1 = line[x][1];
				final float f2 = line[x][2];

				final int ix = y * width + x;
				
				result[ix][0] = Gfx.saturate(Math.round(f0));
				result[ix][1] = Gfx.saturate(Math.round(f1));
				result[ix][2] = Gfx.saturate(Math.round(f2));
			}
		}

		return result;
	}

	protected void learn(final Position best, final int r, final int g, final int b, final float currentRate,
			final float radius2) {
		final int bx = best.x;
		final int by = best.y;

		for (int y = 0; y < height; y++) {
			final float line[][] = matrix[y];
			for (int x = 0; x < width; x++) {
				final float dx = bx - x;
				final float dy = by - y;
				final float d2 = dx * dx + dy * dy;

				// wp³yw gaussowski z odleg³oœci kwadratowej
				final float influence = (float) Math.exp(-d2 / (2f * radius2));
				final float n = currentRate * influence;

				final float[] rgb = line[x];
				rgb[0] += n * (r - rgb[0]);
				rgb[1] += n * (g - rgb[1]);
				rgb[2] += n * (b - rgb[2]);
			}
		}
	}

	/**
	 * ZnajdŸ BMU (Best Matching Unit) u¿ywaj¹c odleg³oœci kwadratowej w przestrzeni
	 * kolorów.
	 */
	protected Position getBMU(final int red, final int green, final int blue) {
		int bx = 0, by = 0;
		float min = Float.MAX_VALUE;

		for (int y = 0; y < height; y++) {
			final float line[][] = matrix[y];
			for (int x = 0; x < width; x++) {
				final float rgb[] = line[x];

				final float dr = rgb[0] - red;
				final float dg = rgb[1] - green;
				final float db = rgb[2] - blue;

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
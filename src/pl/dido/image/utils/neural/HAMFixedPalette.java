package pl.dido.image.utils.neural;

public class HAMFixedPalette extends SOMFixedPalette {

    public HAMFixedPalette(final int width, final int height, final int bits) {
        super(width, height, bits);
    }

    @Override
    public int[][] train(final byte rgb[]) {
        matrixInit(rgb);

        final int epochs = Math.max(1, this.epoch);

        final float delta_rate = rate / (float) epochs;
        final float delta_radius = radius / (float) epochs;

        final int len = rgb.length;
        float or = 0f, og = 0f, ob = 0f, currentRate = rate, currentRadius = radius;
        float a = 0f;
        
        for (int e = 0; e < epochs; e++) {
            for (int i = 0; i < len; i += 3) {

                // pickup sample (normalizacja zgodnie ze scale)
                final float r = ((rgb[i] & 0xff) / scale);
                final float g = ((rgb[i + 1] & 0xff) / scale);
                final float b = ((rgb[i + 2] & 0xff) / scale);

                // obliczamy odleg³oœæ kwadratow¹ (bez sqrt)
                final float dr = r - or;
                final float dg = g - og;
                final float db = b - ob;
                final float d2 = dr * dr + dg * dg + db * db;

                a = ((d2 + a) / 2f) * 1.35f;

                if (d2 > a)
                    learn(getBMU(r, g, b), r, g, b, currentRate, currentRadius);

                or = r;
                og = g;
                ob = b;
            }

            currentRate -= delta_rate;
            currentRadius -= delta_radius;
        }

        return getPalette();
    }
}
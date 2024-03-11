package pl.dido.image.utils;

import java.awt.image.BufferedImage;

public class C64PaletteCalculator {

	public static int[] angles, lumas;
	
	private static final float sector = 360f / 16f;
	private static final float origin = sector / 2f;
	
	private static final float radian = (float) (Math.PI) / 180f;
	private static final float screen = 1f / 5f;
	
	private static final float source = 2.8f; // PAL
	private static final float target = 2.2f; // sRGB

	static {
		angles = new int[16];
		
		angles[0x4] = 2; // Purple
		angles[0x2] = angles[0xa] = 4; // Red
		angles[0x8] = 5; // Orange
		angles[0x9] = 6; // Brown
		angles[0x7] = 7; // Yellow
		angles[0x5] = angles[0xd] = 2 + 8; // Green
		angles[0x3] = 4 + 8; // Cyan
		angles[0x6] = angles[0xe] = 7 + 8;

		// most common
		lumas = new int[16];

		lumas[0x6] = lumas[0x9] = 8;  // Blue, Brown
		lumas[0xb] = lumas[0x2] = 10; // Dk.Grey, Red
		lumas[0x4] = lumas[0x8] = 12; // Purple, Orange
		lumas[0xc] = lumas[0xe] = 15; // Md.Grey, Lt.Blue
		lumas[0x5] = lumas[0xa] = 16; // Green, Lt.Red
		lumas[0xf] = lumas[0x3] = 20; // Lt.Grey, Cyan
		lumas[0x7] = lumas[0xd] = 24; // Yellow, Lt.Green
		lumas[0x1] = 32;
	}

	public static float[] compose(float luma, float angle, float brightness, float contrast, float saturation) {
		// normalize
		brightness -= 50f;
		contrast /= 100f;
		saturation *= 1f - screen;

		// construct
		final float components[] = new float[3]; // monochrome (chroma switched off)

		if (angle != 0) {
			angle = (origin + angle * sector) * radian;

			components[1] = (float) Math.cos(angle) * saturation;
			components[2] = (float) Math.sin(angle) * saturation;
		}

		components[0] = 8 * luma + brightness;

		for (int i = 0; i < 3; i++)
			components[i] *= contrast + screen;

		return components;
	}

	public static int[] convert(final float components[]) {
		// matrix transformation
		final int[] color = new int[3];

		color[0] = (int) (components[0] + 1.140f * components[2]);
		color[1] = (int) (components[0] - 0.396f * components[1] - 0.581f * components[2]);
		color[2] = (int) (components[0] + 2.029f * components[1]);

		// gamma correction		
		for (int i = 0; i < 3; i++) {
			float c = Math.max(Math.min(color[i], 255), 0);

			c = (float) (Math.pow(255, 1 - source) * Math.pow(c, source));
			c = (float) (Math.pow(255, 1 - 1 / target) * Math.pow(c, 1 / target));
			
			color[i] = Math.round(c);
		}

		return color;
	}
	
	public static int[][] getCalculatedPalette(final int pixelType) {
		final int palette[][] = new int[16][3];
		
		for (int i = 0; i < 16; i++) {
			final int color[] = convert(compose(lumas[i], angles[i], 50, 100, 50));
			
			switch (pixelType) {
			case BufferedImage.TYPE_3BYTE_BGR:
				palette[i][2] = color[0];
				palette[i][1] = color[1];
				palette[i][0] = color[2];
				break;
			case BufferedImage.TYPE_INT_RGB:
				palette[i][0] = color[0];
				palette[i][1] = color[1];
				palette[i][2] = color[2];
				break;
			default:
				throw new RuntimeException("Unsupported pixel format !!!");
			}
		}
		
		return palette;
	}
}

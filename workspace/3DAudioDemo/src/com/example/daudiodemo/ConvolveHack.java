package com.example.daudiodemo;

import java.util.Arrays;

public class ConvolveHack {

	static final int hack_time_samples = 150;
	static final int time_samples = 128;

	public static float[] convolve(float[] x, float[] y) {
		float[] out = new float[hack_time_samples];

		// length of out - 128 - N
		int skipFront = 128;
		// length of out - 128 (previously 254)
		// May need to make this not be -1;
		int skipEnd = hack_time_samples + time_samples * 2 - time_samples - 1;

		// for all of y
		for (int i = 0; i < y.length; i++) {
			// for all of x
			for (int j = 0; j < x.length; j++) {
				if (!((i + j) < skipFront) && !((i + j) > skipEnd)) {
					out[i + j - skipFront] = out[i + j - skipFront] + x[j]* y[i];

				}
			}
		}

		return out;
	}

}

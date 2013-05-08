package com.example.audio;

import java.util.Arrays;

public class Audio3D {
	// The azimuth and elevation variables. We keep track of the old az and elev
	// to see if we need to crossfade
	static double az;
	static double elev = 0;
	static double oldAz = 0;
	static double oldElev = 0;

	// The audio data
	static HACKED_SAMPLES newOut = new HACKED_SAMPLES();
	static HACKED_SAMPLES oldOut = new HACKED_SAMPLES();
	float[] finalOut = new float[(HACKED_SAMPLES.sample_size + 4410)* 2]; 

	// cross fading stuff
	static boolean cfFlag;
	static float[] rampUp = new float[HACKED_SAMPLES.sample_size];
	static float[] rampDn = new float[HACKED_SAMPLES.sample_size];

	// The class that loads the audio data and picks the appropriate data from
	// an array
	public GetIRF getIrf = new GetIRF();

	/**
	 * Constructor
	 * 
	 * @param azimuth
	 *            - the initial azimuth that you want the sound to be played at
	 * @param elevation
	 *            - the initial elevation that you want the sound to be played
	 *            at
	 */
	Audio3D(int azimuth, int elevation) {
		Audio3D.az = azimuth;
		Audio3D.elev = elevation;
		Audio3D.oldAz = azimuth;
		Audio3D.oldElev = elevation;

		// Create the ramps
		for (int i = 0; i < HACKED_SAMPLES.sample_size; i++) {
			rampUp[i] = (float) (i / (float) (HACKED_SAMPLES.sample_size - 1));
			rampDn[i] = 1 - rampUp[i];
		}
	}

	public float[] runAudio3D() {

		// Get the old audio data if we need to crossfade
//		if (cfFlag) {
//			loadOldIRFs();
//		}

		// load the new audio data
		loadIRFs();

//		if (cfFlag) {
//			crossfade();
//		}

		// interleave audio samples so that we hear the left and right channels
		// note: even values are left, odd values are right
		for (int i = 0; i < newOut.left.length; i++) {
			// This flips the data if we are on the left side.
			if (getIrf.cur_flip_flag) {
				finalOut[i * 2] = newOut.right[i];
				finalOut[i * 2 + 1] = newOut.left[i];
			} else {
				finalOut[i * 2] = newOut.left[i];
				finalOut[i * 2 + 1] = newOut.right[i];
			}
		}

		return finalOut;
	}

	/**
	 * Loads the audio data
	 */
	void loadIRFs() {
		newOut = getIrf.get_irf(elev, az);
	}

	/**
	 * Loads the old audio data
	 */
	void loadOldIRFs() {
		oldOut = getIrf.get_irf(oldElev, oldAz);
	}

	/**
	 * Ramps up the new data and down the old data so we get a smooth
	 * transition.
	 */
	void crossfade() {
		for (int i = 0; i < HACKED_SAMPLES.sample_size; i++) {
			newOut.left[i] = newOut.left[i] * rampUp[i] + oldOut.left[i]
					* rampDn[i];
			newOut.right[i] = newOut.right[i] * rampUp[i] + oldOut.right[i]
					* rampDn[i];
		}
		return;
	}

	/**
	 * Changes the azimuth and elevations the audio is played at
	 * 
	 * @param newaz
	 * @param newelev
	 */
	void updateLocation(double newaz, double newelev) {
		// see if we need to crossfade
		if (newaz != az || newelev != elev)
			cfFlag = true;
		else
			cfFlag = false;

		oldAz = az;
		oldElev = elev;
		az = newaz;
		elev = newelev;
	}
}

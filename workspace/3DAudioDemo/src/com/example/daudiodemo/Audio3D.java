package com.example.daudiodemo;

import java.util.Arrays;

public class Audio3D {
	// Constants

	// Public members
	static double az;
	static double elev = 0;
	static double oldAz = 0;
	static double oldElev = 0;

	static HACKED_SAMPLES newOut = new HACKED_SAMPLES();
	static HACKED_SAMPLES oldOut = new HACKED_SAMPLES();
	float[] finalOut = new float[HACKED_SAMPLES.sample_size * 2];

	// cross fading stuff
	static boolean cfFlag;
	static float[] rampUp = new float[HACKED_SAMPLES.sample_size];
	static float[] rampDn = new float[HACKED_SAMPLES.sample_size];

	public GetIRF getIrf = new GetIRF();

	Audio3D(int azimuth, int elevation) {
		Audio3D.az = azimuth;
		Audio3D.elev = elevation;
		Audio3D.oldAz = azimuth;
		Audio3D.oldElev = elevation;

		// Create the ramps
		for (int i = 0; i < HACKED_SAMPLES.sample_size; i++) { 
			rampUp[i] = (float) (i / (float)(HACKED_SAMPLES.sample_size - 1));
			rampDn[i] = 1 - rampUp[i];
		}
		
		//System.out.println(Arrays.toString(rampDn));
	}

	public float[] runAudio3D() {

		// ////////////
		// Get current IRFs //
		// ///////////		
		if(cfFlag) {
			loadOldIRFs();
		}
		
		loadIRFs();
		
		if(cfFlag) {
			crossfade();
		}

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
		// return newOut.left;
	}

	/*************************************************
	 * New function
	 **************************************************/
	void loadIRFs() {
		newOut = getIrf.get_irf(elev, az);
	}

	void loadOldIRFs() {
		oldOut = getIrf.get_irf(oldElev, oldAz);
	}
	
	// Might have trouble with how I am changing the sides 
	void crossfade() {
		for (int i = 0; i < HACKED_SAMPLES.sample_size; i++) {
			newOut.left[i] = newOut.left[i] * rampUp[i] + oldOut.left[i] * rampDn[i];
			newOut.right[i] = newOut.right[i] * rampUp[i] + oldOut.right[i] * rampDn[i];
		}
		return;
	}

	void updateLocation(double newaz, double newelev) {
		// see if we need to crossfade
		if (newaz != az || newelev != elev) cfFlag = true;
		else cfFlag = false;
			
		oldAz = az;
		oldElev = elev;
		az = newaz;
		elev = newelev;
	}
}
package com.example.daudiodemo;
import java.util.Arrays;


public class Audio3D {	
	// Constants

	//number of time samples
	static final int time_samples = 128;
	static final int hacked_time_samples = 150;

	// Public members
	static double az;
	static double dist;
	static double oldAz = 0;
	static double elev = 0;
	static double oldElev = 0;

	static HACKED_SAMPLES oldOut = new HACKED_SAMPLES();
	static HACKED_SAMPLES newOut = new HACKED_SAMPLES();
	
	float[] finalOut = new float[hacked_time_samples*2];

	static boolean cfFlag; // Are we cross-fading or what?
	static float[] rampUp = new float[hacked_time_samples];
	static float[] rampDn = new float[hacked_time_samples];

	public GetIRF getIrf = new GetIRF();

	Audio3D(int azimuth, int elevation){
		Audio3D.az = azimuth;
		Audio3D.oldAz = azimuth;
		Audio3D.elev = elevation;
		Audio3D.oldElev = elevation;
	}

	public float[] runAudio3D() {
		// ///////////////////////////
		// Checks for cross-fading //
		// ///////////////////////////
		if ((oldAz != az) || (oldElev != elev))
			cfFlag = true;
		else
			cfFlag = false;

		// ////////////
		// Get current IRFs //
		// ////////////
		loadIRFs();

		// /////////////
		// Crossfade //
		// /////////////
		if (cfFlag) {
			createOldConvolveAndCrossfade();
		}

		// Carries over the old azimuth and elevation for cross-fading
		// purposes
		oldAz = az;
		oldElev = elev;
		oldOut = newOut;

		// interwave
		// Interleave left and right channels for stereo output
		for (int i = 0; i < newOut.left.length; i++) {
			finalOut[i] = newOut.left[i];
			finalOut[i + 1] = newOut.right[i];
		}

		return finalOut;
	}

	//////////////////////////////
	// Generic helper functions //
	//////////////////////////////
	/*****************************************************
	 * Changed this function
	 *****************************************************/
	void init() {
		// Read in 3D audio data from file
		GetIRF getIrf = new GetIRF();
		getIrf.read_irfs();

		for (int i = 0; i < hacked_time_samples; i++) { // Initialize ramps
			rampUp[i] = (float) (i/(hacked_time_samples-1));
			rampDn[i] = 1 - rampUp[i];
		}
	}

	/*************************************************
	 * New function
	 **************************************************/
	void loadIRFs(){
		newOut = getIrf.get_irf(elev, az);
	}


	/**
	 * If we have had a change in our irf buffer then we need to compute what the output signal
	 * would be with the old irf buffer and then crossfade between the two outputs.
	 */
	void createOldConvolveAndCrossfade() {
		newOut.left = crossfade(newOut.left, oldOut.left);
		newOut.right = crossfade(newOut.right, oldOut.right);

	}

	float[] crossfade(float[] newIn, float[] oldIn){
		float[] out = new float[hacked_time_samples];
		for (int i = 0; i < hacked_time_samples; i++) {
			out[i] = newIn[i]*rampUp[i] + oldIn[i]*rampDn[i];
		}
		return out;
	}

	void updateLocation(double newaz, double newelev, double newdist) {
		// Saves old az and elev- this may not be necessary, as it is done in runAudio3D
		oldAz = az;
		oldElev = elev;
		dist = newdist;
		az = newaz;
		elev = newelev;
	}
}

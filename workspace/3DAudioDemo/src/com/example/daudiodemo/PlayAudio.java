package com.example.daudiodemo;

import android.os.AsyncTask;
import android.util.FloatMath;

public class PlayAudio extends AsyncTask <Void, Void, Void> {
	final int sample_size = 150;
	
	@Override
	protected Void doInBackground(Void... args0) {
		AndroidAudioDevice device = new AndroidAudioDevice();
		Audio3D audioModder = new Audio3D(0, 5);
		audioModder.init();
		float[] finalOut;
		double az = 0;

		while (DemoGraphics.playAudio) {
			az = az + 0.1;
			// Update position
			audioModder.updateLocation(az, 0.0, 5.0);

			// Futz the input sound
			finalOut = audioModder.runAudio3D();

			device.writeSamples(finalOut);
		}
        
        return null;
	}

	protected void onProgressUpdate() {}

	protected void onPostExecute() {}
}
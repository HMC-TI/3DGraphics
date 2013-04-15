package com.example.daudiodemo;

import android.os.AsyncTask;
import java.util.Arrays;
import android.util.FloatMath;

public class PlayAudio extends AsyncTask <Void, Void, Void> {
	final int sample_size = 150;
	
	@Override
	protected Void doInBackground(Void... args0) {
		System.out.println("Started Audio");
		AndroidAudioDevice device = new AndroidAudioDevice();
		
		float[] finalOut;
		double az = 0;

		while (DemoGraphics.playAudio) {
			az = az + 0.1;
			// Update position
			SensorHubService.audio.updateLocation(SensorHubService.az, SensorHubService.elev);

			// Futz the input sound
			finalOut = SensorHubService.audio.runAudio3D();

			device.writeSamples(finalOut);
		}
        
        return null;
	}

	protected void onProgressUpdate() {}

	protected void onPostExecute() {}
}
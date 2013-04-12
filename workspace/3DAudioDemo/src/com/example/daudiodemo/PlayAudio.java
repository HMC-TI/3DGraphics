package com.example.daudiodemo;

import android.os.AsyncTask;
import android.util.FloatMath;

public class PlayAudio extends AsyncTask <Void, Void, Void> {

	@Override
	protected Void doInBackground(Void... args0) {
		final float frequency = 440;
        float increment = (float)(2*Math.PI) * frequency / 44100; // angular increment for each sample
        float angle = 0;
        AndroidAudioDevice device = new AndroidAudioDevice( );
        float samples[] = new float[128];
        
        
        
        while(RazorExample.continueMusic)
        {
           for( int i = 0; i < samples.length; i++ )
           {
              samples[i] = (float)FloatMath.sin(angle);
              angle += increment;
           }
           
           device.writeSamples( samples );
        }
        
        return null;
	}

	protected void onProgressUpdate() {}

	protected void onPostExecute() {}
}
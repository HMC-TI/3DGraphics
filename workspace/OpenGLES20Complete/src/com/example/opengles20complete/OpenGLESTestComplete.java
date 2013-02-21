package com.example.opengles20complete;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;


public class OpenGLESTestComplete extends Activity {

	/** Hold a reference to our GLSurfaceView */
	private GLSurfaceView mGLSurfaceView;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_glestest_complete);
        
        super.onCreate(savedInstanceState);
        
        mGLSurfaceView = new GLSurfaceView(this);
     
        // Check if the system supports OpenGL ES 2.0.
        final ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        final ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
        final boolean supportsEs2 = configurationInfo.reqGlEsVersion >= 0x20000;
     
        if (supportsEs2)
        {
            // Request an OpenGL ES 2.0 compatible context.
            mGLSurfaceView.setEGLContextClientVersion(2);
     
            // Set the renderer to our demo renderer, defined below.
            mGLSurfaceView.setRenderer(new LessonOneRenderer());
        }
        else
        {
            // This is where you could create an OpenGL ES 1.x compatible
            // renderer if you wanted to support both ES 1 and ES 2.
            return;
        }
     
        setContentView(mGLSurfaceView);
        
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_open_glestest_complete, menu);
        return true;
    }
}

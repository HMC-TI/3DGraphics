package com.example.opengles20basic;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.app.Activity;
//import android.view.Menu;

public class OpenGLES20 extends Activity {

	private GLSurfaceView mGLView;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       // setContentView(R.layout.activity_open_glbasic_test);
        mGLView = new MyGLSurfaceView(this);
        setContentView(mGLView);
   
    }

    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_open_glbasic_test, menu);
        return true;
    }*/
}

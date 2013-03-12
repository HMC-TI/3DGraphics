package com.example.daudiodemo;

import java.io.IOException;

import android.app.Activity;
import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import de.tuberlin.qu.razorahrs.RazorAHRS;
import de.tuberlin.qu.razorahrs.RazorListener;

public class DemoGraphics extends Activity 
{
	/** Hold a reference to our GLSurfaceView */
	private GLSurfaceView mGLSurfaceView;
	protected static final String TAG = "RazorExampleActivity";

	private BluetoothAdapter bluetoothAdapter;
	public static BluetoothDevice razorDevice;
	private RazorAHRS razor;
	
	private TextView yawTextView;
	private TextView pitchTextView;
	private TextView rollTextView;
	private TextView declinationTextView;
	
	public static float roll = 0;
	public static float pitch = 0;
	public static float yaw = 0;
	public static float initRoll;
	public static float initPitch;
	public static float initYaw;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate");
		
		// Create new razor instance and set listener
		razor = new RazorAHRS(RazorExample.razorDevice, new RazorListener() {
			public void onConnectAttempt(int attempt, int maxAttempts) {
				Toast.makeText(DemoGraphics.this, "Connect attempt " + attempt + " of " + maxAttempts + "...", Toast.LENGTH_SHORT).show();
			}
			
			public void onConnectOk() {
				Toast.makeText(DemoGraphics.this, "Connected!", Toast.LENGTH_LONG).show();
			}
			
			public void onConnectFail(Exception e) {
    			Toast.makeText(DemoGraphics.this, "Connecting failed: " + e.getMessage() + ".", Toast.LENGTH_LONG).show();
			}
			
			/**************************************************************
			 * This is the function that we want to use to get the roll, pitch and yaw
			 * 
			 * This update method is called in the RazorAHRS profile, which is what
			 * connects with the bluetooth 
			 **************************************************************/
			public void onAnglesUpdate(float yaw, float pitch, float roll) {
				//Calibration.rollTextViewCal.setText(String.format("%.1f", roll));
				DemoGraphics.roll = (int) roll;
				DemoGraphics.pitch = (int) pitch;
				DemoGraphics.yaw = (int) yaw;
				
				//rollTextViewCal.setText(String.format("%s", Calibration.roll));
						
			}

			public void onIOExceptionAndDisconnect(IOException e) {
    			Toast.makeText(DemoGraphics.this, "Disconnected, an error occured: " + e.getMessage() + ".", Toast.LENGTH_LONG).show();
			}
		});
		
		// Connect asynchronously
		razor.asyncConnect(5);
				
		
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
			mGLSurfaceView.setRenderer(new DemoRenderer());
		} 
		else 
		{
			// This is where you could create an OpenGL ES 1.x compatible
			// renderer if you wanted to support both ES 1 and ES 2.
			return;
		}

		setContentView(mGLSurfaceView);
		
		for (int i=0; i < 5; i++)
		{
			Toast.makeText(getApplicationContext(), " " + yawTextView, Toast.LENGTH_LONG).show();
		}
		

	}

	@Override
	protected void onResume() 
	{
		// The activity must call the GL surface view's onResume() on activity onResume().
		super.onResume();
		mGLSurfaceView.onResume();
	}

	@Override
	protected void onPause() 
	{
		// The activity must call the GL surface view's onPause() on activity onPause().
		super.onPause();
		mGLSurfaceView.onPause();
	}	
}
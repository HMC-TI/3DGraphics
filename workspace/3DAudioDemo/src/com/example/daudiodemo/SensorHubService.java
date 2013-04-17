package com.example.daudiodemo;

import java.io.IOException;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import de.tuberlin.qu.razorahrs.RazorAHRS;
import de.tuberlin.qu.razorahrs.RazorListener;
//import android.*;
//import de.tuberlin.qu.RazorExample;

public class SensorHubService extends Service {

	protected static final String TAG = "SensorHubService";

	// public BluetoothAdapter bluetoothAdapter;
	// public static BluetoothDevice razorDevice;
	public RazorAHRS razor;
	// public RadioGroup deviceListRadioGroup;
	
	// These are the values we want to use for the 3D audio. Note that they are calculated in DemoRenderer
	public static float az =0;
	public static float elev = 0;

	public static float roll = 0;
	public static float pitch = 0;
	public static float yaw = 0;

	public static TextView initial;
	public static boolean initConnected = false;
	
	public static Audio3D audio = new Audio3D(0,0);

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(TAG, "onCreate");

		//Toast.makeText(SensorHubService.this, "Sensor Hub Service Created ...", Toast.LENGTH_SHORT).show();

		// Create new razor instance and set listener
		razor = new RazorAHRS(RazorExample.razorDevice, new RazorListener() {

			public void onConnectAttempt(int attempt, int maxAttempts) {
				Toast.makeText(
						SensorHubService.this,
						"Connect attempt " + attempt + " of " + maxAttempts
								+ "...", Toast.LENGTH_SHORT).show();
			}

			public void onConnectOk() {
				Toast.makeText(SensorHubService.this, "Connected!",
						Toast.LENGTH_SHORT).show();
				RazorExample.setButtonStateConnected();
			}

			public void onConnectFail(Exception e) {
				Toast.makeText(SensorHubService.this,
						"Connecting failed: " + e.getMessage() + ".",
						Toast.LENGTH_SHORT).show();
			}

			/**************************************************************
			 * This is the function that we want to use to get the roll, pitch
			 * and yaw
			 * 
			 * This update method is called in the RazorAHRS profile, which is
			 * what connects with the Bluetooth
			 **************************************************************/
			public void onAnglesUpdate(float yaw, float pitch, float roll) {
				SensorHubService.roll = (float) roll;
				SensorHubService.pitch = (float) pitch;
				SensorHubService.yaw = (float) yaw;

			}

			public void onIOExceptionAndDisconnect(IOException e) {
				Toast.makeText(
						SensorHubService.this,
						"Disconnected, an error occured: " + e.getMessage()+ ".", Toast.LENGTH_SHORT).show();
				
				RazorExample.setButtonStateDisconnected();
			}

		});

		// Connect asynchronously
		razor.asyncConnect(5); // 5 connect attempts
		
		
		// now init the 3D audio
		audio.init();

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		this.razor.asyncDisconnect();
		//Toast.makeText(SensorHubService.this, "Disconnected", Toast.LENGTH_SHORT).show();
	}
}


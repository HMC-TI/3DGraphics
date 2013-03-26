package com.example.daudiodemo;

import java.io.IOException;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import de.tuberlin.qu.razorahrs.RazorAHRS;
import de.tuberlin.qu.razorahrs.RazorListener;
//import android.*;
//import de.tuberlin.qu.RazorExample;

public class SensorHubService  extends Service{
	
	protected static final String TAG = "SensorHubService";
	
	public BluetoothAdapter bluetoothAdapter;
	public static BluetoothDevice razorDevice;
	public RazorAHRS razor;
	public RadioGroup deviceListRadioGroup;
	
	public static float roll = 0;
	public static float pitch = 0;
	public static float yaw = 0;
	public static float caliRoll = 0;
	public static float caliPitch = 0;
	public static float caliYaw = 0;
	public static float initRoll;
	public static float initPitch;
	public static float initYaw;
	
	
	public static TextView initial;
	public static boolean initConnected = false;
	
	
	@Override
    public IBinder onBind(Intent arg0) {
          return null;
    }
	
    @Override
    public void onCreate() {
          super.onCreate();
          Log.d(TAG, "onCreate");
          
          Toast.makeText(SensorHubService.this,"Sensor Hub Service Created ...", Toast.LENGTH_LONG).show();
    	


		// Create new razor instance and set listener
		razor = new RazorAHRS(RazorExample.razorDevice, new RazorListener() {
			
			public void onConnectAttempt(int attempt, int maxAttempts) {
				System.out.println("Test");
				Toast.makeText(SensorHubService.this, "Connect attempt " + attempt + " of " + maxAttempts + "...", Toast.LENGTH_SHORT).show();
			}
			
			public void onConnectOk() {
				Toast.makeText(SensorHubService.this, "Connected!", Toast.LENGTH_LONG).show();
			}
			
			public void onConnectFail(Exception e) {
				Toast.makeText(SensorHubService.this, "Connecting failed: " + e.getMessage() + ".", Toast.LENGTH_LONG).show();
			}
			
			/**************************************************************
			 * This is the function that we want to use to get the roll, pitch and yaw
			 * 
			 * This update method is called in the RazorAHRS profile, which is what
			 * connects with the bluetooth 
			 **************************************************************/
			public void onAnglesUpdate(float yaw, float pitch, float roll) {
				//Calibration.rollTextViewCal.setText(String.format("%.1f", roll));
				SensorHubService.roll = (int) roll;
				SensorHubService.pitch = (int) pitch;
				SensorHubService.yaw = (int) yaw;
				
				// ----> Still need to deal with wrap around possibly
				SensorHubService.caliRoll=(int) SensorHubService.roll - SensorHubService.initRoll;
				SensorHubService.caliPitch=(int) SensorHubService.pitch - SensorHubService.initPitch;
				SensorHubService.caliYaw=(int) SensorHubService.yaw - SensorHubService.initYaw;
						
			}
	
			public void onIOExceptionAndDisconnect(IOException e) {
				Toast.makeText(SensorHubService.this, "Disconnected, an error occured: " + e.getMessage() + ".", Toast.LENGTH_LONG).show();
			}
		
	});

    
    RazorExample.connectButton.setEnabled(false);
    RazorExample.connectButton.setText("Connected");
    
    RazorExample.cancelButton.setEnabled(true);
    RazorExample.cancelButton.setText("Cancel");
    
    RazorExample.zeroButton.setEnabled(true);
    
    }
}



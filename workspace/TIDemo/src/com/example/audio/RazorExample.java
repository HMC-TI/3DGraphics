package com.example.audio;

/******************************************************************************************
 * Android Java Interface for Razor AHRS v1.4.1
 * 9 Degree of Measurement Attitude and Heading Reference System
 * for Sparkfun "9DOF Razor IMU" and "9DOF Sensor Stick"
 *
 * Released under GNU GPL (General Public License) v3.0
 * Copyright (C) 2011-2012 Quality & Usability Lab, Deutsche Telekom Laboratories, TU Berlin
 * Written by Peter Bartz (peter-bartz@gmx.de)
 *
 * Infos, updates, bug reports and feedback:
 *     http://dev.qu.tu-berlin.de/projects/sf-razor-9dof-ahrs
 ******************************************************************************************/
import java.util.Set;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import de.tuberlin.qu.razorahrs.RazorAHRS;

public class RazorExample extends Activity {

	protected static final String TAG = "RazorExample";

	public BluetoothAdapter bluetoothAdapter;
	public static BluetoothDevice razorDevice;
	public RazorAHRS razor;
	public BluetoothDevice rb;

	public static float roll = 0;
	public static float pitch = 0;
	public static float yaw = 0;
	
	// Don't need these?
	//public static float initRoll;
	//public static float initPitch;
	//public static float initYaw;

	//Don't need these?
	//public static TextView initial;
	//public static boolean initConnected = false;

	private RadioGroup deviceListRadioGroup;
	
	// Buttons
	public static Button beginButton;
	public static Button connectButton;
	public static Button cancelButton;

	public static boolean continueMusic = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate");

		// Make the orientation landscape due to hardware setup
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

		// Set view
		setContentView(R.layout.main);

		// Find views
		beginButton = (Button) findViewById(R.id.begin_button);
		connectButton = (Button) findViewById(R.id.connect_button);
		cancelButton = (Button) findViewById(R.id.cancel_button);
		deviceListRadioGroup = (RadioGroup) findViewById(R.id.devices_radiogroup);

		// Get Bluetooth adapter
		bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (bluetoothAdapter == null) { // Ooops
			// Show message
			errorText("Your device does not seem to have Bluetooth, sorry.");
			return;
		}

		// Check whether Bluetooth is enabled
		if (!bluetoothAdapter.isEnabled()) {
			errorText("Bluetooth not enabled. Please enable and try again!");
			return;
		}

		// Get list of paired devices
		Set<BluetoothDevice> pairedDevices = bluetoothAdapter
				.getBondedDevices();

		// Add devices to radio group
		for (BluetoothDevice device : pairedDevices) {
			RadioButton rb = new RadioButton(this);
			rb.setText(" " + device.getName());
			rb.setTag(device);
			deviceListRadioGroup.addView(rb);
		}

		// Check if any paired devices found
		if (pairedDevices.size() == 0) {
			errorText("No paired Bluetooth devices found. Please go to Bluetooth Settings and pair the Razor AHRS.");
		}

		else {
			((RadioButton) deviceListRadioGroup.getChildAt(0)).setChecked(true);
			setButtonStateDisconnected();
		}

		beginButton.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				// Move on to next screen
				Intent instructIntent = new Intent(RazorExample.this,
						InstructScreen.class);

				startActivity(instructIntent);

			};

		});

		// Connect button click handler
		connectButton.setOnClickListener(new View.OnClickListener() {

			public void onClick(View view) {
				setButtonStateConnecting();

				// Get selected Bluetooth device
				RadioButton rb = (RadioButton) findViewById(deviceListRadioGroup
						.getCheckedRadioButtonId());
				if (rb == null) {
					Toast.makeText(RazorExample.this,
							"You have select a device first.",
							Toast.LENGTH_LONG).show();
					return;
				}
				razorDevice = (BluetoothDevice) rb.getTag();

				// Start service
				startService(new Intent(RazorExample.this,
						SensorHubService.class));

			}
		});

		// Cancel button click handler
		cancelButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				// razor.asyncDisconnect(); // Also cancels pending connect
				stopService(new Intent(RazorExample.this,
						SensorHubService.class));
				setButtonStateDisconnected();
				Toast.makeText(RazorExample.this,
						"Sensor Hub Service Stopped ...", Toast.LENGTH_LONG)
						.show();
			}
		});

	}

	private void errorText(String text) {
		TextView tv = new TextView(this);
		tv.setText(text);
		deviceListRadioGroup.addView(tv);
	}

	public static void setButtonStateDisconnected() {
		// Enable connect button
		connectButton.setEnabled(true);
		connectButton.setText("Connect");

		// Disable cancel button and begin button
		cancelButton.setEnabled(false);
		beginButton.setEnabled(false);
	}

	public static void setButtonStateConnecting() {
		// Disable connect button and set text
		connectButton.setEnabled(false);
		connectButton.setText("Connecting...");

		// Enable cancel button
		cancelButton.setEnabled(true);
		beginButton.setEnabled(false);
	}

	public static void setButtonStateConnected() {
		// Disable connect button and set text
		connectButton.setEnabled(false);
		connectButton.setText("Connected");

		// Enable cancel button and begin button
		cancelButton.setEnabled(true);
		beginButton.setEnabled(true);

	}

	@Override
	protected void onStop() {
		super.onStop();
		Log.d(TAG, "onStop");

		if (razor != null)
			razor.asyncDisconnect();
	}
	
	@Override
	protected void onDestroy(){
		super.onDestroy();
		Log.d(TAG, "onDestroy");
		// Stop sensor hub service
		stopService(new Intent(RazorExample.this,
				SensorHubService.class));
	}

}
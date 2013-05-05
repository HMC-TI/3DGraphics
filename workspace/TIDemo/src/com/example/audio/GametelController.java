package com.example.audio;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.provider.Settings;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.widget.ImageView;
import android.widget.TextView;

public class GametelController extends Activity {
	private static final String GAMETEL_NAME = "gametel";
	private static final String GAMETEL_PACKAGE = "com.fructel.gametel";

	private TextView mStatus;

	private ImageView[] mButtons = new ImageView[12];
	private BroadcastReceiver mReceiver;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		/* Get all button indicators */
		// mButtons[GametelKey.NAVIGATION_UP.ordinal()] =
		// (ImageView)findViewById(R.id.ivUp);
		// mButtons[GametelKey.NAVIGATION_LEFT.ordinal()] =
		// (ImageView)findViewById(R.id.ivLeft);
		// mButtons[GametelKey.NAVIGATION_RIGHT.ordinal()]
		// =(ImageView)findViewById(R.id.ivRight);
		// mButtons[GametelKey.NAVIGATION_DOWN.ordinal()] =
		// (ImageView)findViewById(R.id.ivDown);
		// mButtons[GametelKey.ACTION_UP.ordinal()] =
		// (ImageView)findViewById(R.id.ivA);
		// mButtons[GametelKey.ACTION_RIGHT.ordinal()] =
		// (ImageView)findViewById(R.id.ivB);
		// mButtons[GametelKey.ACTION_DOWN.ordinal()] =
		// (ImageView)findViewById(R.id.ivC);
		// mButtons[GametelKey.ACTION_LEFT.ordinal()] =
		// (ImageView)findViewById(R.id.ivD);
		// mButtons[GametelKey.START.ordinal()] =
		// (ImageView)findViewById(R.id.ivStart);
		// mButtons[GametelKey.SELECT.ordinal()] =
		// (ImageView)findViewById(R.id.ivSelect);
		// mButtons[GametelKey.TRIGGER_LEFT.ordinal()] =
		// (ImageView)findViewById(R.id.ivLTrig);
		// mButtons[GametelKey.TRIGGER_RIGHT.ordinal()] =
		// (ImageView)findViewById(R.id.ivRTrig);

		/* ... and the status text */
		// mStatus = (TextView)findViewById(R.id.tvStatus);

		/* This receiver is used to detect Gametel connect/disconnect */
		mReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				updateGametelStatus();
			}
		};
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mReceiver = null;
	}

	@Override
	protected void onResume() {
		/*
		 * Register a listener to detect when Gametel devices
		 * connects/disconnects
		 */
		IntentFilter filter = new IntentFilter();
		/* For devices in RFCOMM mode (which uses the InputMethod) */
		filter.addAction(Intent.ACTION_INPUT_METHOD_CHANGED);
		/* For devices in HID mode */
		filter.addAction(Intent.ACTION_CONFIGURATION_CHANGED);
		registerReceiver(mReceiver, filter);

		/* Check if there are any Gametel devices connected */
		updateGametelStatus();

		super.onResume();
	}

	@Override
	protected void onPause() {
		unregisterReceiver(mReceiver);
		super.onPause();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (!handleKeyEvent(keyCode, event))
			return super.onKeyDown(keyCode, event);
		return true;
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (!handleKeyEvent(keyCode, event))
			return super.onKeyDown(keyCode, event);
		return true;
	}

	/* Help function to set a GUI button indicator */
	private void setButtonIndicator(GametelKey key, boolean pressed) {
		// int resId = pressed ? R.drawable.indicator_on :
		// R.drawable.indicator_off;
		// mButtons[key.ordinal()].setImageResource(resId);
	}

	/* Help function to parse the Gametel key */
	private boolean handleKeyEvent(int keyCode, KeyEvent event) {
		boolean pressed = event.getAction() == KeyEvent.ACTION_DOWN;

		switch (keyCode) {

		/* Upper navigation button */
		case KeyEvent.KEYCODE_DPAD_UP:
			setButtonIndicator(GametelKey.NAVIGATION_UP, pressed);
			break;

		/* Right navigation button */
		case KeyEvent.KEYCODE_DPAD_RIGHT:
			setButtonIndicator(GametelKey.NAVIGATION_RIGHT, pressed);
			break;

		/* Lower navigation button */
		case KeyEvent.KEYCODE_DPAD_DOWN:
			setButtonIndicator(GametelKey.NAVIGATION_DOWN, pressed);
			break;

		/* Left navigation button */
		case KeyEvent.KEYCODE_DPAD_LEFT:
			setButtonIndicator(GametelKey.NAVIGATION_LEFT, pressed);
			break;

		/* Start button */
		case KeyEvent.KEYCODE_BUTTON_START:
			setButtonIndicator(GametelKey.START, pressed);
			break;

		/* Select button */
		case KeyEvent.KEYCODE_BUTTON_SELECT:
			setButtonIndicator(GametelKey.SELECT, pressed);
			break;

		/* Left trigger button */
		case KeyEvent.KEYCODE_BUTTON_L1:
			setButtonIndicator(GametelKey.TRIGGER_LEFT, pressed);
			break;

		/* Right trigger button */
		case KeyEvent.KEYCODE_BUTTON_R1:
			setButtonIndicator(GametelKey.TRIGGER_RIGHT, pressed);
			break;

		/* Upper action button */
		case KeyEvent.KEYCODE_BUTTON_Y:
			setButtonIndicator(GametelKey.ACTION_UP, pressed);
			break;

		/*
		 * Right action button - can either be BACK+ALT or BUTTON_C depending on
		 * device mode
		 */
		case KeyEvent.KEYCODE_BACK:
			if (event.isAltPressed()) {
				setButtonIndicator(GametelKey.ACTION_RIGHT, pressed);
			} else {
				/* This is the real "Back" button - Exit app */
				finish();
			}
			break;
		case KeyEvent.KEYCODE_BUTTON_C:
			setButtonIndicator(GametelKey.ACTION_RIGHT, pressed);
			break;

		/*
		 * Lower action button - can either be DPAD_CENTER or BUTTON_Z depending
		 * on device mode
		 */
		case KeyEvent.KEYCODE_DPAD_CENTER:
		case KeyEvent.KEYCODE_BUTTON_Z:
			setButtonIndicator(GametelKey.ACTION_DOWN, pressed);
			break;

		/* Left action button */
		case KeyEvent.KEYCODE_BUTTON_X:
			setButtonIndicator(GametelKey.ACTION_LEFT, pressed);
			break;

		default:
			return false;
		}
		return true;
	}

	/*
	 * Function to check the current Gametel status. The status field will say
	 * 'connected' if either: - A Gametel device in HID Gamepad mode is
	 * connected - The current InputMethod is set to 'Gametel'
	 */
	private void updateGametelStatus() {
		boolean gametelAvailable = false;

		/* Check if there are any Gametels connected as HID gamepad */
		if (isHIDGametelConnected())
			gametelAvailable = true;

		/* Check if the Gametel InputMethod is active */
		if (isGametelIMEActive())
			gametelAvailable = true;

		mStatus.setText("Gametel "
				+ (gametelAvailable ? "connected" : "not connected"));
	}

	/* Function that checks if the Gametel InputMethod is currently active */
	private boolean isGametelIMEActive() {
		String activeIme = Settings.Secure.getString(getContentResolver(),
				Settings.Secure.DEFAULT_INPUT_METHOD);
		return activeIme.startsWith(GAMETEL_PACKAGE);
	}

	/*
	 * Function that checks if there are any Gametels in HID gamepad mode
	 * currently connected
	 */
	private boolean isHIDGametelConnected() {

		int ids[] = InputDevice.getDeviceIds();

		for (int i = 0; i < ids.length; i++) {
			InputDevice dev = InputDevice.getDevice(ids[i]);
			if (dev.getName().toLowerCase().contains(GAMETEL_NAME))
				return true;
		}

		return false;
	}

}
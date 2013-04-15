package com.example.daudiodemo;

import java.util.Random;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.KeyEvent;

public class DemoGraphics extends Activity {
	
	
	public static boolean startQuit;
	
	// When the audio should be played
	public static boolean playAudio = true;
	
	private BroadcastReceiver mReceiver;
	/** Hold a reference to our GLSurfaceView */
	private MyGLSurfaceView mGLSurfaceView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Make the orientation landscape due to hardware setup
		// setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

		mGLSurfaceView = new MyGLSurfaceView(this);
		mReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
			}
		};
		setContentView(mGLSurfaceView);
		
		//PlayAudio sound = new PlayAudio();
		//sound.execute();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mReceiver = null;
		
	}

	@Override
	protected void onResume() {
		// The activity must call the GL surface view's onResume() on activity
		// onResume().
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

		super.onResume();
		mGLSurfaceView.onResume();
	}

	@Override
	protected void onPause() {
		unregisterReceiver(mReceiver);
		// The activity must call the GL surface view's onPause() on activity
		// onPause().
		super.onPause();
		mGLSurfaceView.onPause();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (!mGLSurfaceView.handleKeyEvent(keyCode, event))
			return super.onKeyDown(keyCode, event);
		return true;
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (!mGLSurfaceView.handleKeyEvent(keyCode, event))
			return super.onKeyDown(keyCode, event);
		return true;
	}
}

class MyGLSurfaceView extends GLSurfaceView {

	private final DemoRenderer mRenderer;

	public MyGLSurfaceView(Context context) {
		super(context);

		// Create an OpenGL ES 2.0 context.
		setEGLContextClientVersion(2);

		// Set the Renderer for drawing on the GLSurfaceView
		mRenderer = new DemoRenderer(context);
		setRenderer(mRenderer);
	}

	/* Help function to parse the Gametel key */
	public boolean handleKeyEvent(int keyCode, KeyEvent event) {
		boolean pressed = event.getAction() == KeyEvent.ACTION_DOWN;

		switch (keyCode) {

		/* Upper navigation button */
		case KeyEvent.KEYCODE_DPAD_UP:
			if (pressed) {
				mRenderer.upRotate = true;
			} else {
				mRenderer.upRotate = false;
			}
			break;

		/* Right navigation button */
		case KeyEvent.KEYCODE_DPAD_RIGHT:
			if (pressed) {
				mRenderer.rightRotate = true;
			} else {
				mRenderer.rightRotate = false;
			}
			break;

		/* Lower navigation button */
		case KeyEvent.KEYCODE_DPAD_DOWN:
			if (pressed) {
				mRenderer.downRotate = true;
			} else {
				mRenderer.downRotate = false;
			}
			break;

		/* Left navigation button */
		case KeyEvent.KEYCODE_DPAD_LEFT:
			if (pressed) {
				mRenderer.leftRotate = true;
			} else {
				mRenderer.leftRotate = false;
			}
			break;

		/* Start button */
		case KeyEvent.KEYCODE_BUTTON_START:
			break;

		/* Select button */
		case KeyEvent.KEYCODE_BUTTON_SELECT:
			break;

		/* Left trigger button */
		case KeyEvent.KEYCODE_BUTTON_L1:
			if (pressed) {
				if (mRenderer.hasBeenFound()) {
					Random rand = new Random();
					mRenderer.pyrX = rand.nextFloat() * 8 - 4.0f;
					mRenderer.pyrY = rand.nextFloat() * 8 - 4.0f;
					mRenderer.pyrZ = rand.nextFloat() * (-8) - 2.0f;
				}
			}
			break;

		/* Right trigger button */
		case KeyEvent.KEYCODE_BUTTON_R1:
			break;

		/* Upper action button */
		case KeyEvent.KEYCODE_BUTTON_Y:
			break;

		/*
		 * Right action button - can either be BACK+ALT or BUTTON_C depending on
		 * device mode
		 */
		case KeyEvent.KEYCODE_BACK:
			break;
		case KeyEvent.KEYCODE_BUTTON_C:
			break;

		/*
		 * Lower action button - can either be DPAD_CENTER or BUTTON_Z depending
		 * on device mode
		 */
		case KeyEvent.KEYCODE_DPAD_CENTER:
			break;
		case KeyEvent.KEYCODE_BUTTON_Z:
			/*if (pressed) {
				if (mRenderer.hasBeenFound()) {
					Random rand = new Random();
					mRenderer.pyrX = rand.nextFloat() * 8 - 4.0f;
					mRenderer.pyrY = rand.nextFloat() * 8 - 4.0f;
					mRenderer.pyrZ = rand.nextFloat() * (-8) - 2.0f;
				}
			}*/
			break;

		/* Left action button */
		case KeyEvent.KEYCODE_BUTTON_X:
			break;

		default:
			return false;
		}
		return true;
	}
	
}
package com.example.audio;

import java.util.Random;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.Toast;

public class DemoGraphics extends Activity {

	public static boolean startQuit;

	// When the audio should be played
	public static boolean playAudio = true;

	public static AndroidAudioDevice device = new AndroidAudioDevice();

	// For the Gametel controller, a Bluetooth device
	private BroadcastReceiver mReceiver;
	
	// Our custom GLSurfaceView, defined below
	private MyGLSurfaceView mGLSurfaceView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mGLSurfaceView = new MyGLSurfaceView(this);
		mReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
			}
		};
		setContentView(mGLSurfaceView);
		// Begin audio
		PlayAudio sound = new PlayAudio();
		sound.execute();
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
		// For devices in RFCOMM mode (which uses the InputMethod)
		filter.addAction(Intent.ACTION_INPUT_METHOD_CHANGED);
		// For devices in HID mode
		filter.addAction(Intent.ACTION_CONFIGURATION_CHANGED);
		registerReceiver(mReceiver, filter);

		// This will restart the score any time the graphics are paused by a
		// screen change
		MyGLSurfaceView.score = 0;
		
		// The activity must call the GL surface view's onResume() on activity
		// onResume().
		mGLSurfaceView.onResume();
		super.onResume();
	}

	@Override
	protected void onPause() {
		unregisterReceiver(mReceiver);
		// The activity must call the GL surface view's onPause() on activity
		// onPause().
		mGLSurfaceView.onPause();
		super.onPause();
	}

	/**
	 * For handling button presses on the Gametel controller
	 */
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

	// Make our own custom renderer using OpenGL ES
	private final DemoRenderer mRenderer;

	// This will be used to keep score of the number of diamonds collected
	public static int score = 0;

	public MyGLSurfaceView(Context context) {
		super(context);

		// Create an OpenGL ES 2.0 context.
		setEGLContextClientVersion(2);

		// Set the Renderer for drawing on the GLSurfaceView
		mRenderer = new DemoRenderer(context);
		setRenderer(mRenderer);
	}

	/**
	 *  Help function to parse the Gametel key. Buttons can be set to game
	 *  functionality at the programmer's discretion.
	 */
	public boolean handleKeyEvent(int keyCode, KeyEvent event) {
		boolean pressed = event.getAction() == KeyEvent.ACTION_DOWN;

		switch (keyCode) {

		/* Upper navigation button */
		case KeyEvent.KEYCODE_DPAD_UP:
			// For debugging: if need to change view without sensor hub, use DPad on
			// Gametel Controller
			/*
			if (pressed) {
			mRenderer.upRotate = true;
			} else {
			mRenderer.upRotate = false;
			}
			*/
			break;

		/* Right navigation button */
		case KeyEvent.KEYCODE_DPAD_RIGHT:
			// For debugging: if need to change view without sensor hub, use DPad on
			// Gametel Controller
			/*
			if (pressed) {
			mRenderer.rightRotate = true;
			} else {
			mRenderer.rightRotate = false;
			}
			*/
			break;

		/* Lower navigation button */
		case KeyEvent.KEYCODE_DPAD_DOWN:
			// For debugging: if need to change view without sensor hub, use DPad on
			// Gametel Controller
			/*
			if (pressed) {
				mRenderer.downRotate = true;
			} else {
				mRenderer.downRotate = false;
			}
			*/
			break;

		/* Left navigation button */
		case KeyEvent.KEYCODE_DPAD_LEFT:
			// For debugging: if need to change view without sensor hub, use DPad on
			// Gametel Controller
			/*
			if (pressed) {
				mRenderer.leftRotate = true;
			} else {
				mRenderer.leftRotate = false;
			}
			*/
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
				// Demo "cheat" button: move the diamond to a new location without
				// centering diamond requirement
				Random rand = new Random();
				mRenderer.pyrX = rand.nextFloat() * 8 - 4.0f;
				mRenderer.pyrY = rand.nextFloat() * 8 - 4.0f;
				mRenderer.pyrZ = rand.nextFloat() * (-8) - 2.0f;
			}
			break;

		/* Right trigger button */
		case KeyEvent.KEYCODE_BUTTON_R1:
			if (pressed) {
				// If audio is on, mute it
				if (DemoGraphics.playAudio == true) {
					DemoGraphics.playAudio = false;
					Toast.makeText(this.getContext(), "Mute",
							Toast.LENGTH_SHORT).show();
				}
				// If audio is muted, turn it back on
				else if (DemoGraphics.playAudio == false) {
					DemoGraphics.playAudio = true;
					PlayAudio sound = new PlayAudio();
					sound.execute();
				}
			}
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
			break;

		/* Left action button */
		case KeyEvent.KEYCODE_BUTTON_X:
			if (pressed) {
				if (mRenderer.hasBeenFound()) {
					// If diamond is in view, centered, and the player has pressed
					// the button, move the diamond to a new location
					Random rand = new Random();
					mRenderer.pyrX = rand.nextFloat() * 8 - 4.0f;
					mRenderer.pyrY = rand.nextFloat() * 8 - 4.0f;
					mRenderer.pyrZ = rand.nextFloat() * (-8) - 2.0f;

					// Add a point for every found diamond and display score to
					// player
					score += 1;
					Toast.makeText(this.getContext(), String.valueOf(score),
							Toast.LENGTH_SHORT).show();
					
					/*
					 * For changing levels: if the player has reached a certain
					 * score, change levels (cube texture) by updating the level
					 * booleans here and inform the player what level they have
					 * achieved
					 */

					// Level 2
					if (score == 2) {
						mRenderer.levelTwo = true;
						Toast.makeText(this.getContext(), "Level 2",
								Toast.LENGTH_LONG).show();

					}
					// Level 3
					else if (score == 4) {
						mRenderer.levelThree = true;
						Toast.makeText(this.getContext(), "Level 3",
								Toast.LENGTH_LONG).show();

					}
				}
			}
			break;

		default:
			return false;
		}
		return true;
	}
}
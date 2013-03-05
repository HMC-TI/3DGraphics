package com.example.gameplay;

import com.example.gameplay.Soundsource;
import android.util.Log;
import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.BitmapFactory;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import java.util.Random;

public class MainGamePanel extends SurfaceView implements
	SurfaceHolder.Callback {
	
	private static final String TAG = MainGamePanel.class.getSimpleName();
	
	private MainThread thread;
	private Soundsource source;
	
	public MainGamePanel(Context context) {
		super(context);
		// adding the callback (this) to the surface holder to intercept events
		getHolder().addCallback(this);
		
		// create sound source and load bitmap
		source = new Soundsource(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher), 50, 50);
		
		//create the game loop thread
		thread = new MainThread(getHolder(), this);
		
		// make the GamePanel focusable so it can handle events
		setFocusable(true);
	}
	 
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
	}
	 
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		thread.setRunning(true);
		thread.start();
	}
	 
	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.d(TAG, "Surface is being destroyed");
		boolean retry = true;
		while (retry) {
			try {
				thread.join();
				retry = false;
			} catch (InterruptedException e) {
				// try again shutting down the thread
			}
		}
		Log.d(TAG, "Thread was shut down cleanly");
	}
	 
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		Random rnd = new Random();
		int randomx;
		int randomy;
		randomx = rnd.nextInt(getWidth());
		randomy = rnd.nextInt(getHeight());
		
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			if (event.getY() > getHeight() - 50) {
				thread.setRunning(false);
				((Activity)getContext()).finish();
			} else {
				source.handleActionDown((int)event.getX(), (int)event.getY());
				if (source.isTouched()) {
					source.randomLocation(randomx, randomy);
				}
				Log.d(TAG, "Coords: x=" + event.getX() + ",y=" + event.getY());
			}
		} /*if (event.getAction() == MotionEvent.ACTION_MOVE) {
			// the gestures
			if (source.isTouched()) {
				// the source was picked up and is being dragged
				source.setX((int)event.getX());
				source.setY((int)event.getY());
				Log.d(TAG, "Dragging...");
			}
		}*/ if (event.getAction() == MotionEvent.ACTION_UP) {
			// touch was released
			if (source.isTouched()) {
				source.setTouched(false);
			}
		}
		return true;
	}
	 
	protected void render(Canvas canvas) {
		// fills the canvas with black
		canvas.drawColor(Color.BLACK);
		source.draw(canvas);
	}
	
	/**
	 * This is the game update method. It iterates through all the objects
	 * and calls their update method if they have one or calls specific
	 * engine's update method.
	 */
	public void update() {
		// check collision with right wall if heading right
		if (source.getSpeed().getxDirection() == Speed.DIRECTION_RIGHT
				&& source.getX() + source.getBitmap().getWidth() / 2 >= getWidth()) {
			source.getSpeed().toggleXDirection();
		}
		// check collision with left wall if heading left
		if (source.getSpeed().getxDirection() == Speed.DIRECTION_LEFT
				&& source.getX() - source.getBitmap().getWidth() / 2 <= 0) {
			source.getSpeed().toggleXDirection();
		}
		// check collision with bottom wall if heading down
		if (source.getSpeed().getyDirection() == Speed.DIRECTION_DOWN
				&& source.getY() + source.getBitmap().getHeight() / 2 >= getHeight()) {
			source.getSpeed().toggleYDirection();
		}
		// check collision with top wall if heading up
		if (source.getSpeed().getyDirection() == Speed.DIRECTION_UP
				&& source.getY() - source.getBitmap().getHeight() / 2 <= 0) {
			source.getSpeed().toggleYDirection();
		}
		// Update the lone source
		source.update();
	}
}

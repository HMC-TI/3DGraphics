package com.example.daudiodemo;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

public class IntroScreen extends Activity {

	public static Button beginButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		setContentView(R.layout.activity_intro_screen);

		beginButton = (Button) findViewById(R.id.begin_button);
		beginButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {

				Intent calibrateIntent = new Intent(IntroScreen.this,
						RazorExample.class);

				startActivity(calibrateIntent);

			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_intro_screen, menu);
		return true;
	}

}

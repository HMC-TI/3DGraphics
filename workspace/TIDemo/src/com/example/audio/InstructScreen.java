package com.example.audio;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

public class InstructScreen extends Activity {

	public static Button playButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		setContentView(R.layout.activity_instruct_screen);

		playButton = (Button) findViewById(R.id.play_button);
		playButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {

				Intent graphicsIntent = new Intent(InstructScreen.this,
						DemoGraphics.class);

				startActivity(graphicsIntent);
			}
		});

		SensorHubService.create3DAudio();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_instruct_screen, menu);
		return true;
	}

}

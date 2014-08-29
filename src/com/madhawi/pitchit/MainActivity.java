package com.madhawi.pitchit;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

@SuppressLint("NewApi")
public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//Show the welcome view 
		setContentView(R.layout.activity_main);
		
		//Wait for 2 seconds
		Thread timer = new Thread() {
			public void run() {
				try {
					sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				finally {
					Intent menuIntent= new Intent(MainActivity.this,Selection.class);
					startActivity(menuIntent);
				}
			}
		};
		timer.start();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		//finish the view
		finish();
	}
}
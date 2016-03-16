package com.aawesh.taximeter;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;

import com.aawesh.taximeter.R;

public class SplashScreen extends Activity {
	public static final int delayTime = 1000;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
		    // Here activity is brought to front, not created,
		    // so finishing this will get you to the last viewed activity
		    finish();
		    return;
		  }
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.splash);
		
		Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			
			@Override
			public void run() {
				finish();
		
					Intent intent = new Intent(SplashScreen.this,MainActivity.class);
					startActivity(intent);
			}
		}, delayTime);
	}
}

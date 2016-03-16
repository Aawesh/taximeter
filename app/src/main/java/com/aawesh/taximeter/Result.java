package com.aawesh.taximeter;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.aawesh.taximeter.R;

import java.text.DecimalFormat;

public class Result extends Activity {
	double distance;
	double charge;
	boolean isDay;
	DecimalFormat df = new DecimalFormat("#.##");
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.result);
		ImageView background = (ImageView) findViewById(R.id.background);
		AlphaAnimation anim = new AlphaAnimation(0.07f, 0.07f);
		anim.setDuration(50);
		anim.setFillEnabled(true);
		anim.setFillAfter(true);
		background.startAnimation(anim);
		TextView dist_info = (TextView)findViewById(R.id.dist);
		TextView charge_info = (TextView)findViewById(R.id.charge);
		TextView result_distance= (TextView) findViewById(R.id.dist1);
		TextView result_charge= (TextView) findViewById(R.id.charge1);
		TextView minimumCharge = (TextView) findViewById(R.id.minimum_charge);
		TextView interactive_message = (TextView)findViewById(R.id.interactive_message);
		ImageView statusImage = (ImageView)findViewById(R.id.status);
		ImageButton back_button = (ImageButton)findViewById(R.id.back_button);
		
		TextView rateField = (TextView) findViewById(R.id.rate);
		Bundle bundle = getIntent().getExtras();
		distance = (Double)bundle.getDouble("distance");
		charge = (Double)bundle.getDouble("charge");
		isDay = (Boolean)bundle.getBoolean("dayOrNight");

		back_button.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				
				finish();
			}
		});
		
		if(distance != 0){
			result_distance.setText(df.format(distance));
			result_charge.setText(Math.ceil(charge)+"");//taking ceiling function
			if(isDay == true){
				statusImage.setBackgroundResource(R.drawable.sun);
				minimumCharge.setText("  *Rs "+ MainActivity.dayMinimumCharge+" minimum charge has been added");
				rateField.setText("Rate:       Rs. "+ MainActivity.dayRate+"/km");
				interactive_message.setText("Route Summary");
			}
			else{
				statusImage.setBackgroundResource(R.drawable.night);
				minimumCharge.setText("  *Rs "+ MainActivity.nightMinimumCharge+" minimum charge has been added");
				rateField.setText("Rate:       Rs. "+ MainActivity.nightRate+"/km");
				interactive_message.setText("Route Summary");
			}
		}
		else{
			if(isDay == true){
				statusImage.setBackgroundResource(R.drawable.sun);
			}
			else{
				statusImage.setBackgroundResource(R.drawable.night);
			}
			result_distance.setVisibility(View.GONE);
			result_charge.setVisibility(View.GONE);
			interactive_message.setText("You did not travel");
			dist_info.setText("Distance(Km):  0.00");
			charge_info.setText("Charge(Rs):       0.00");
			minimumCharge.setText("");
			rateField.setText("");
		}	
	}
}

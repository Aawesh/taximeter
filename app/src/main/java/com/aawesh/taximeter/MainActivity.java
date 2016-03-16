package com.aawesh.taximeter;

/**
 *
 * @author Aawesh
 */

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.aawesh.taximeter.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.text.DecimalFormat;
import java.util.Calendar;

public class MainActivity extends FragmentActivity implements  LocationListener {
	double totalDistance = 0;
	double totalCharge = 0;
	DecimalFormat df = new DecimalFormat("#.##");
	private static final LatLng NEPAL = new LatLng(27.9389, 84.9408); //GeoLocation of Nepal
	double minimumCharge;
	final static double dayMinimumCharge = 14;
	final static double nightMinimumCharge = 21;
	double rate;
	final static double dayRate = 37;
	final static double nightRate=55.5;
	boolean flag = true;
	boolean chargeFlag = true;
	boolean destinationFlag = true;
	public CameraPosition cameraPosition;
	private GoogleMap map;
	private LatLng initialPosition;
	private LatLng currentPosition;
	PolylineOptions line = new PolylineOptions();
	boolean switchValue = false;
	LocationManager manager = null;
	boolean myLocationFlag = false;
	TextView disp_charge, disp_distance,default_distance,default_charge;
	ImageButton switchButton,stopButton = null;
	LocationListener listener;
	ImageView background = null;
	View view = null; 
	RelativeLayout.LayoutParams layoutParams = null;
	//TODO add vars
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		switchButton = (ImageButton) findViewById(R.id.switchButton);
		if( GPSenabled() ){
			updateButton("finding_location");
			switchButton.setEnabled(false);
		}
		map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.themap)).getMap();
		//map = ((MapFragment) getFragmentManager().findFragmentById(R.id.themap)).getMap();
		if (map == null) {
			map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.themap)).getMap();
		}
		else {
				// state: TRUE = running | FALSE = not running
			SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
		    Boolean  state = sharedPreferences.getBoolean("state", false);	    
		    if ( !state ){
		    	listener = this;
		    	manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
				manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 2, this);
				
				startApp();
				SharedPreferences savePreferences = getPreferences(MODE_PRIVATE);
			    SharedPreferences.Editor editor = savePreferences.edit();
			    editor.putBoolean("state", true);
			    editor.commit();
			}
		}
	}
	
	private void startApp(){
		map.setMyLocationEnabled(true);//updates the camera as the map moves
		UiSettings ui = map.getUiSettings();
		ui.setMyLocationButtonEnabled(true);
		ui.setCompassEnabled(true);
		//ui.setZoomControlsEnabled(true);
		ui.setAllGesturesEnabled(true);
		map.setOnMyLocationButtonClickListener(new OnMyLocationButtonClickListener() {
			
			@Override
			public boolean onMyLocationButtonClick(){
				if(!myLocationFlag){
					Toast.makeText(getBaseContext(), "Data connection unavailable", Toast.LENGTH_SHORT).show();
				}
				return false;
			}
		});
		
		//Setting the initial map view of Nepal .Since this application is targeted for Nepal for now
		cameraPosition = new CameraPosition.Builder().target(NEPAL) //.bearing(90)//sets the orientation of the camera to east
				.zoom(7) //.tilt(30)//tilts 30 degree
				.build();
		map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

		default_distance = (TextView) findViewById(R.id.distance);
		default_charge = (TextView)findViewById(R.id.charge);
		disp_distance = (TextView) findViewById(R.id.meterReading);
		disp_charge = (TextView) findViewById(R.id.chargeReading);
		stopButton = (ImageButton)findViewById(R.id.stopButton);
		background= (ImageView)findViewById(R.id.background);
		///////////////////////////////////////////////////////////////////////////////

		final boolean dayOrNightFlag = findDayOrNight();//true for day and false for night
		if (dayOrNightFlag == true) {
			//that means day charge
			minimumCharge = dayMinimumCharge;
			rate = dayRate;
		} else {
			minimumCharge = nightMinimumCharge;
			rate = nightRate;
		}
	
		//if switch is on start working.If switch is off
		switchButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (!GPSenabled()){
					promptToEnableGPS();
				}
				else{
				
				//user is switching to ON button
				//put the code for route summary
				//this block is encountered when the user want to stop at the destination
				switchValue = true;
				
				// Get the layout params of the fragment holder
				view = findViewById(R.id.themap);
				layoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();

				// Now you have the layout params, do what you need to do
				//layoutParams.removeRule(RelativeLayout.ABOVE);
				layoutParams.addRule(RelativeLayout.ABOVE, R.id.background);
				
				disp_distance.setVisibility(View.VISIBLE);
				disp_charge.setVisibility(View.VISIBLE);
				default_distance.setVisibility(View.VISIBLE);
				default_charge.setVisibility(View.VISIBLE);
				stopButton.setVisibility(View.VISIBLE);
				background.setVisibility(View.VISIBLE);
				switchButton.setVisibility(View.GONE);
			}
		}
	});		
		
		
		stopButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				//here will be the code for setting switchValue
				//if isChecked is true this block will be invoked
				//stop the working of the program
				AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
				builder.setTitle(R.string.exit_confirmation);
				builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						manager.removeUpdates(listener);
						switchValue = false;
						stopButton.setVisibility(View.GONE);						
						//Toast.makeText(getBaseContext(),"OFF (Perform the final thing)",Toast.LENGTH_SHORT).show();
						if(currentPosition!=null  & destinationFlag){
							map.addMarker(new MarkerOptions()
							.position(currentPosition).title("Destination Point")
							.snippet("you stopped here")
//										.icon(BitmapDescriptorFactory.fromResource(R.drawable.pushpin)));
							.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
							destinationFlag = false;
						}
						Intent intent = new Intent(MainActivity.this,Result.class);
						intent.putExtra("distance",totalDistance);
						intent.putExtra("charge", totalCharge);
						intent.putExtra("dayOrNight",dayOrNightFlag);
						startActivity(intent);
					}
				})
				.setNegativeButton(R.string.cancel,new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						dialog.cancel();
						//setChecked(true);
					}
				});
				AlertDialog dialog = builder.create();
				dialog.show();
				dialog.setCanceledOnTouchOutside(false);
				
			}
		});
		
	}
	
	
	@Override
	public void onLocationChanged(Location location) {
		if(location != null){
			if(!myLocationFlag){
				switchButton.setEnabled(true);
				updateButton("lets_go");
				Toast.makeText(getBaseContext(), "Location set by GPS", Toast.LENGTH_SHORT).show();
				myLocationFlag = true;
			}
			else{
				double latitude = location.getLatitude();
				double longitude = location.getLongitude();
				currentPosition = new LatLng(latitude, longitude);
				// flag true = no points selected ! flag false = two points selected
				if(flag && switchValue){
					cameraPosition = new CameraPosition.Builder().target(currentPosition).zoom(15).build();
					map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
					
					map.addMarker(new MarkerOptions()
					.position(currentPosition).title("Start Point")
					.snippet("you started moving from here")
					.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
					line.add(currentPosition);
					flag = false;
				}
				else if(!flag && switchValue ){
					cameraPosition = new CameraPosition.Builder()
					.target(currentPosition).zoom(17).build();
					map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
					line.add(currentPosition).width(5).color(Color.BLUE);
					map.addPolyline(line);
					double currentDistance = calculateDistance(initialPosition,currentPosition);
					totalDistance += currentDistance;
					
					if(chargeFlag){
						totalCharge += (currentDistance*rate)+minimumCharge;
						chargeFlag = false;
					}
					else{
						totalCharge += (currentDistance*rate);
					}
					
					disp_distance.setText(df.format(totalDistance));
					disp_charge.setText(df.format(totalCharge));
					
				}
				/***********************************************/
				//flag = false;
				initialPosition = currentPosition;
			}
		}	
		
	}

	
	// takes the text in the button, eg: "finding_location", "enable_gps" etc. and changes the ImageResource
	public void updateButton(String btn_img){
		ImageButton targetImage = (ImageButton) findViewById(R.id.switchButton);
		if( btn_img.equalsIgnoreCase("enable_gps") )	targetImage.setBackgroundResource(R.drawable.turn_on_gps_xml);
		else if( btn_img.equalsIgnoreCase("lets_go") )	targetImage.setBackgroundResource(R.drawable.lets_go_xml);
		else if( btn_img.equalsIgnoreCase("finding_location") )	targetImage.setBackgroundResource(R.drawable.finding_location_pressed);
		//else if( btn_img.equalsIgnoreCase("stop_meter") )	targetImage.setText("Stop Meter");
		//else if( btn_img.equalsIgnoreCase("view_history") )	targetImage.setText("View History");
	}
	
	// Uses locationmanager to get GPS_PROVIDER enabled/not enabled
	private boolean GPSenabled(){
		LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
        boolean enabled = service.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!enabled) {
          return false;
        }
        else{
        	return true;
        }
	}
	
	public void promptToEnableGPS(){
		Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(intent);
	}
	// Returns (isDay) : True if day, False if night
	private boolean findDayOrNight() {
		// TODO Auto-generated method stub
		String AMorPM = "";
		int hour;
		int minute;
		boolean isDay = false; //true for day and false for night
		Calendar calendar = Calendar.getInstance();
		if (calendar.get(Calendar.AM_PM) == Calendar.PM) {
			AMorPM = "PM";
			System.out.println("PM");
		} else {
			AMorPM = "AM";
			System.out.println("AM");
		}
		hour = calendar.get(Calendar.HOUR);
		minute = calendar.get(Calendar.MINUTE);
		

		if (hour == 8 && minute == 50 && AMorPM == "PM") {
			isDay = false;
		} else if (hour >= 9 && hour <= 11 && AMorPM == "PM") {
			isDay = false;
		} else if (hour == 0 && AMorPM == "AM") {
			isDay = false;
		} else if (hour == 5 && minute == 50 && AMorPM == "AM") {
			isDay = true;
		} else if (hour >= 1 && hour <= 6 && AMorPM == "AM") {
			isDay = false;
		} else {
			isDay = true;
		}
		return isDay;
	}

	// Opens an alertdialog showing the info about the app and developers
	private void showAbout() {
		AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
		builder.setMessage(R.string.dialog_message)
		.setTitle(R.string.dialog_title)
		.setPositiveButton(R.string.back, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dialog.cancel();
				
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
		
	}

	// Save the shared preferences when the app stops
	@Override
	protected void onStop(){
		super.onStop();
		SharedPreferences savePreferences = getPreferences(MODE_PRIVATE);
	    SharedPreferences.Editor editor = savePreferences.edit();
	    editor.putBoolean("state", false);
	    editor.commit();
	}
	
	// show confirmation dialog [Rate this app: negative_button_act] [Quit : positive_button_act]
	@Override
	public void onBackPressed() {
		AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
		builder.setMessage(R.string.exit_message)
		.setTitle(R.string.exit_header);
		builder.setPositiveButton(R.string.quit, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				finish();
			}
		}).setNegativeButton(R.string.rating, new DialogInterface.OnClickListener() {

			@Override
			// Open the Google Play store for rating our app
			public void onClick(DialogInterface dialog, int which) {
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setData(Uri.parse("market://details?id=com.aawesh.taximeter"));
				startActivity(intent);
			}
		});
		AlertDialog dialog = builder.create();
		dialog.show();
	}

	// Takes start and end points, then returns the distance between them
	private double calculateDistance(LatLng start,LatLng end) {
		double startLat = start.latitude;
	    double endLat = end.latitude;
	    double startLong = start.longitude;
	    double endLong = end.longitude;
	    double dLat = Math.toRadians(endLat-startLat);
	    double dLon = Math.toRadians(endLong-startLong);
	    double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
	    Math.cos(Math.toRadians(startLat)) * Math.cos(Math.toRadians(endLat)) *
	    Math.sin(dLon/2) * Math.sin(dLon/2);
	    double c = 2 * Math.asin(Math.sqrt(a));
	    //radius of the earth is 6378.1 km so in meter it is 6378100.
		return 6378.1 * c;
	}
	
	// shows toast notification that GPS is off, then changes the button to "Enable GPS"
	@Override
	public void onProviderDisabled(String arg0) {
		//Toast.makeText(getBaseContext(), "GPS is turned off. Turn it on for better experience.", Toast.LENGTH_SHORT).show();
		switchButton.setEnabled(true);
		updateButton("enable_gps");
		myLocationFlag = false;
		//isChecked = false;
		
	}
	//shows toast notification that GPS is on, then changes the button to "Finding Location"
	@Override
	public void onProviderEnabled(String arg0) {
		Toast.makeText(getBaseContext(), "GPS is turned on.", Toast.LENGTH_SHORT).show();
		updateButton("finding_location");
		switchButton.setEnabled(false);
	}

	// no idea
	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		// TODO learn about it!
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
	
	// Two menu items: Rating and About
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item){
		//TODO add more items
		switch(item.getItemId()){
			case R.id.popup_menu_about:
				showAbout();
	             break;
			case R.id.popup_menu_rating:
				//do rating
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setData(Uri.parse("market://details?id=com.aawesh.taximeter"));
				startActivity(intent);
				break;
		}
		return true;
	}
	
}
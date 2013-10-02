package com.jman.trackrate;

import java.util.List;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.CancelableCallback;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.jman.trackrate.tracksendpoint.Tracksendpoint;
import com.jman.trackrate.tracksendpoint.model.GeoPt;

/**
 * This class contains the map view. Here the application tracks the devices location if called from the MainMenuActivity
 * or shows the track route if called from the TrackDetialsActivity
 */
@SuppressLint("ShowToast")
public class TrackMapActivity extends Activity {

	private int MIN_LOC_TIME = 0;
	private int MIN_LOC_DIST = 3;
	private static final String TAG = "TrackMapActivity";
	final static int DEFAULT_INTENT = 0;
	final static int REQUEST_CODE_PHOTO = 353;
	final static int PREF_ZOOM_LEVEL = 18;

	static final String PREFERENCE = "PREFERENCE";
	String FIRST_RUN_TRACK = "firstruntrack";


	private GoogleMap myMap;

	private Float lat;
	private Float lon;
	double latit;
	double longit;
	private PolylineOptions trackPolyOpt;
	Polyline trackPolyl;
	private LocationListener trackLocLis;
	private LocationManager trackLocMan;
	private Location loc;
	private String provider;
	String encodedImage;
	MapButtonFragment fragBtnRef;
	boolean isPaused = false;
	Bitmap trackImage;
	LatLng startpoint;
	int passedVal;
	ImageView ivPhoto;
	TrackRateApp trackapp;

	Tracksendpoint endpoint;

	FrameLayout mainFrame;
	View helpView;
	Boolean firstRun;

	/**
	 * This method creates the activity
	 */
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		mainFrame = new FrameLayout(this);
		mainFrame.addView(LayoutInflater.from(this).inflate(R.layout.activity_track_map, null));
		setContentView(mainFrame);

		trackapp = (TrackRateApp)getApplication();

		//if a previous activity has stored values to the geopt array, clear it
		if(trackapp.getGeoPtArray().size()>0){
			trackapp.clearGeoPtArray();
		}
		ivPhoto = (ImageView) findViewById(R.id.ivPhotoImage);

		Intent startTrackIntent = getIntent();
		//This is where the previous activity, MainMenuActivty or TrackDetails activity passes a value to this activity
		passedVal = startTrackIntent.getIntExtra(MainMenuActivity.OPEN_MAP, DEFAULT_INTENT);
		//check if activity has run before
		checkFirstRun();
		//if passed value is from the MainMenuActivity show the MapButton fragment
		if(passedVal == 1){
			if(findViewById(R.id.fragmentContainer) != null){
				if(savedInstanceState != null){
					return;
				}

				//The MapButtonFrament is loaded if the passed value is 1
				MapButtonFragment trackingBtnFrag = new MapButtonFragment();
				getFragmentManager().beginTransaction().add(R.id.fragmentContainer, trackingBtnFrag).commit();


			}


			showMap();



			showCurrentPosition();


			//Zoom camera to the location of the device			
			myMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(loc.getLatitude(), loc.getLongitude()), PREF_ZOOM_LEVEL), new CancelableCallback(){

				//If the view does not zoom properly display a message
				@Override
				public void onCancel() {
					Toast.makeText(getBaseContext(), "Camera did not zoom properly, please manually zoom in", Toast.LENGTH_LONG)
					.show();
					beginTracking();
				}

				@Override
				public void onFinish() {
					Log.i(TAG, "camera zoomed in properly");
					beginTracking();

				}

			});





			// Or else if the passed value is from the TrackDetails activity show the DetailsButtonFragment
		}else{
			//Remove the photo thumnail from the view
			ivPhoto.setVisibility(View.GONE);
			DetailsButtonFragment detailsBtnFrag = new DetailsButtonFragment();
			if(findViewById(R.id.fragmentContainer) != null){
				if(savedInstanceState != null){
					return;
				}

				detailsBtnFrag = new DetailsButtonFragment();
				getFragmentManager().beginTransaction().add(R.id.fragmentContainer, detailsBtnFrag).commit();


			}

			showMap();

			//Set this method parameter to false so that it will draw the appropriate line on the map
			drawLineOnMap(false);

			//Get the track route that was saved in the TrackRateApp class
			List<GeoPt> trackArray = trackapp.getSavedTrack().getRoute();



			if(trackArray != null){
				//Show the start point and end point of the track on the map
				startpoint = new LatLng(trackArray.get(0).getLatitude(), trackArray.get(0).getLongitude());
				LatLng endpoint = new LatLng(trackArray.get(trackArray.size()-1).getLatitude(), trackArray.get(trackArray.size()-1).getLongitude());

				myMap.animateCamera(CameraUpdateFactory.newLatLngZoom(startpoint, PREF_ZOOM_LEVEL));

				myMap.addMarker(new MarkerOptions().position(startpoint).icon(BitmapDescriptorFactory.fromResource(R.drawable.walk)));
				myMap.addMarker(new MarkerOptions().position(endpoint).icon(BitmapDescriptorFactory.fromResource(R.drawable.finish)));
			}else{
				Toast.makeText(this, "Track has no coordinates. No track can be shown", Toast.LENGTH_SHORT);
			}

		}





	}

	/**
	 * This method checks if activity has been run before
	 */
	private void checkFirstRun(){
		//If activity was called from TrackDetailsActivity set the string so
		//that the help overlay is different than if called from MainMenuActivity 
		if(passedVal == 2){
			FIRST_RUN_TRACK = "firstrunmap";
		}
		firstRun = getSharedPreferences(PREFERENCE, MODE_PRIVATE).getBoolean(FIRST_RUN_TRACK, true);
		getSharedPreferences(PREFERENCE, MODE_PRIVATE).edit().putBoolean(FIRST_RUN_TRACK, false).commit();
		if(firstRun){
			showFirstRunOverLay();
		}
	}

	/**
	 * This method shows help overlay if activity is run for first time
	 */
	private void showFirstRunOverLay(){
		helpView = LayoutInflater.from(getBaseContext()).inflate(R.layout.help_overylay_layout, null);
		ImageView helpImage = (ImageView)helpView.findViewById(R.id.ivHelp);
		Drawable img = null;
		if(passedVal == 1){
			img = getResources().getDrawable(R.drawable.track_screen_help);
		}else{
			img = getResources().getDrawable(R.drawable.track_map_help);
		}

		helpImage.setImageDrawable(img);
		mainFrame.addView(helpView);	

	}

	/**
	 * This method removes the help overlay
	 */
	public void removeHelp(View v){
		mainFrame.removeView(helpView);
	}



	/**
	 * This method shows the map fragment and turns on the device location indicator
	 */
	private void showMap(){
		myMap = ((MapFragment)getFragmentManager().findFragmentById(R.id.map)).getMap();
		myMap.setMyLocationEnabled(true);
		myMap.getMyLocation();

	}

	/**
	 * This method start the FullScreenActivity class if the photo thumbnail is pressed
	 * 
	 * @param v the view that was clicked
	 */
	public void showFullImage(View v){
		Intent fullImageIntent = new Intent(this, FullScreenActivity.class);
		fullImageIntent.putExtra("FullImage", trackImage);
		startActivity(fullImageIntent);


	}


	/**
	 * This method starts the camera and stores the bitmap photo
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(resultCode != RESULT_CANCELED){
			if(requestCode == REQUEST_CODE_PHOTO){
				trackImage = (Bitmap) data.getExtras().get("data");
				ivPhoto.setImageBitmap(trackImage);	
				if(isPaused){
					Toast.makeText(this, "Tracking paused", Toast.LENGTH_LONG).show();
				}
				
			}
		}
	}

	/**
	 * This method creates an alert dialog and displays it when the stop track
	 * button is pressed
	 * 
	 * @param v the view that was clicked
	 */
	public void stopTracking(View v){
		final AlertDialog.Builder stopTrackDialog = new AlertDialog.Builder(this);
		stopTrackDialog.setMessage("Are You Sure you want to Stop Tracking?");

		stopTrackDialog.setNegativeButton(R.string.cancelMsg, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});

		stopTrackDialog.setPositiveButton("Stop + Save", new DialogInterface.OnClickListener() {						
			@Override
			public void onClick(DialogInterface dialog, int which) {
				//Here I check if a route has been tracked, and a photo has been taken
				if(checkIfDetailsCorrect()){
					//Stop tracking
					removeLocationLister();
					Intent saveTrackIntent = new Intent(getApplicationContext(), SaveTrackActivity.class);
					//Here i pass the bitmap image to the SaveTrackActivity
					saveTrackIntent.putExtra("BitMapImage", trackImage);
					startActivity(saveTrackIntent);
					dialog.dismiss();
					finish();
				}
			}
		});



		AlertDialog alertDialog = stopTrackDialog.create();
		alertDialog.show();


	}

	/**
	 * This method is called when the pause button is pressed
	 * 
	 * @param v the view that is clicked
	 */
	public void pauseTracking(View v){		
		if(!isPaused){
			//Remove tracking
			removeLocationLister();
			isPaused = true;
			setPauseButton();
			Toast.makeText(this, "Tracking paused", Toast.LENGTH_LONG).show();
		}else{
			beginTracking();
			isPaused = false;
			setPauseButton();
			
		}
	}

	/**
	 * This method changes the text on the pause button
	 * depending on the pause state (true or false)
	 */
	private void setPauseButton(){
		fragBtnRef = (MapButtonFragment)getFragmentManager().findFragmentById(R.id.fragmentContainer);
		if(!isPaused){
			fragBtnRef.setPauseText("pause");
		}else{
			fragBtnRef.setPauseText("resume");
		}
	}

	/**
	 * This method stops the activity from tracking the device
	 * and drawing the polyline on the map
	 */
	private void removeLocationLister(){
		trackLocMan.removeUpdates(trackLocLis);
		trackLocLis = null;
		isPaused = true;
	}

	/**
	 * This method is called when the camera button is pressed
	 * 
	 * @param v the view that is clicked
	 */
	public void takePhoto(View v){
		//Here I start the camera and get the reulting photo
		Intent photoIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
		startActivityForResult(photoIntent, REQUEST_CODE_PHOTO);
		setPauseButton();

	}

	/**
	 * This method is called when the navigate button is pressed
	 * 
	 * @param v the view that is clicked
	 */
	public void getDirections(View v){
		//Here I create an alert just in case the button was pressed accidentally
		final AlertDialog.Builder stopTrackDialog = new AlertDialog.Builder(this);
		stopTrackDialog.setMessage("Are You Sure you want to navigate to Track?");

		stopTrackDialog.setNegativeButton(R.string.cancelMsg, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});

		stopTrackDialog.setPositiveButton("Navigate", new DialogInterface.OnClickListener() {						
			@Override
			public void onClick(DialogInterface dialog, int which) {
				//Here I start the navigation activity
				Intent navigationIntent = new Intent
						(android.content.Intent.ACTION_VIEW, Uri.parse("google.navigation:q="+startpoint.latitude+","+startpoint.longitude));
				startActivity(navigationIntent);				
				dialog.dismiss();
			}
		});

		AlertDialog alertDialog = stopTrackDialog.create();
		alertDialog.show();

	}

	/**
	 * This method gets the last known position attained by the
	 * device.
	 */
	private void showCurrentPosition(){
		trackLocMan = (LocationManager)getSystemService(Context.LOCATION_SERVICE);	
		Criteria criteria = new Criteria();
		provider = trackLocMan.getBestProvider(criteria, false);
		loc = trackLocMan.getLastKnownLocation(provider);

	}


	/**
	 * This method starts tracking the device's location
	 */
	private void beginTracking(){
		trackLocLis = new TrackLocationListener();				
		trackLocMan.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_LOC_TIME, MIN_LOC_DIST, trackLocLis);		
	}


	/**
	 * This method draws a polyline on the map. It draws a red polyline if tracking
	 * and it draws a green polyline if showing the track on the map
	 * 
	 * @param forLocationChange true if tracking, false if just showing route on map
	 */
	private void drawLineOnMap(Boolean forLocationChange){

		trackPolyOpt = new PolylineOptions();
		List<GeoPt> drawPts = null;
		if(forLocationChange){
			drawPts = trackapp.getGeoPtArray();
			trackPolyOpt.color(Color.RED);
		}else{
			drawPts = trackapp.getSavedTrack().getRoute();
			trackPolyOpt.color(Color.GREEN);
		}


		//Here I draw the polyline on the map from the stored coordinates
		if(drawPts.size() > 1){
			for(GeoPt g : drawPts){
				trackPolyOpt.add(new LatLng(g.getLatitude(), g.getLongitude()));
			}
		}

		trackPolyl = myMap.addPolyline(trackPolyOpt);
	}


	/**
	 * This method is called when the activity is paused
	 */
	@Override
	protected void onPause() {
		//Here I stop tracking when paused if the activity was called from the MainMenuActivty
		if(passedVal == 1){
			if(trackLocLis != null){
				removeLocationLister();
				setPauseButton();
			}
		}
		super.onPause();
	}

	/**
	 * This method is called when the activity is resumed
	 */
	@Override
	protected void onResume() {
		showMap();
		showCurrentPosition();
		//Here I start tracking when resumed if the activity was called from the MainMenuActivty
		if(passedVal == 1){
			if(!isPaused){
				beginTracking();
				setPauseButton();
			}else{
				setPauseButton();
			}
		}
		super.onResume();
	}

	/**
	 * This method checks if a route has been tracked and a photo has been taken
	 * 
	 * @return true is route and photo taken, false if one or both of them has not
	 */
	private boolean checkIfDetailsCorrect(){	
		if(trackapp.getGeoPtArray().size() == 0){
			Toast.makeText(this, "No route tracked. You must track a route before stopping", Toast.LENGTH_LONG).show();
			return false;
		}else if(trackImage == null){
			Toast.makeText(this, "No photo taken. You must take a photo of your route highlight before stopping", Toast.LENGTH_LONG).show();
			return false;
		}else{
			return true;
		}	

	}

	/**
	 * This method stores the latitude and longitude coordinates if set to true
	 * 
	 * @param isTrackingOn true if tracking not paused, false if tracking paused
	 */
	private void TrackingOn(){
		if(!isPaused){
			GeoPt geopt = new GeoPt();
			geopt.setLatitude(lat);
			geopt.setLongitude(lon);
			TrackRateApp trackapp = (TrackRateApp)getApplication();
			//Here I store the coordinates in the TrackRateApp class
			trackapp.setGeopt(geopt);


			drawLineOnMap(true);
		}
	}


	/**
	 * This inner class stores the latitude and longitude coordinates
	 * when the location of the device is changed. It also centres the screen to the new location.
	 *
	 */
	public class TrackLocationListener implements LocationListener{	
		@Override
		public void onLocationChanged(Location loc) {
			String txt = "My Location is Latitude = "+loc.getLatitude()+" Longtitute = "+loc.getLongitude();
			lat = (float) loc.getLatitude();
			lon = (float) loc.getLongitude();

			//Here I centre the diplay to the new location of the device
			myMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(lat, lon)));
			TrackingOn();
			Log.i(TAG, txt);			
		}

		@Override
		public void onProviderDisabled(String arg0) {

		}

		@Override
		public void onProviderEnabled(String arg0) {

		}

		@Override
		public void onStatusChanged(String arg0, int arg1, Bundle arg2) {

		}
	}




}

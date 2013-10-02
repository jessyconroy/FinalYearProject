package com.jman.trackrate;

import java.io.ByteArrayOutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.jman.trackrate.tracksendpoint.Tracksendpoint;
import com.jman.trackrate.tracksendpoint.model.GeoPt;
import com.jman.trackrate.tracksendpoint.model.Tracks;

/**
 * This class enables the application to upload a track to GAE's datastore
 * 
 * @author Jessy
 *
 */
public class SaveTrackActivity extends Activity {

	static String TAG = "SaveTrackActivity";
	static int METERS_IN_KMS = 1000;
	static final String PREFERENCE = "PREFERENCE";
	String FIRST_RUN_SAVE = "firstrunsave";

	Tracksendpoint endpoint;
	TrackRateApp trackapp;
	private EditText etTrackName;
	private EditText etTrackDes;
	private TextView etDisTrav;
	private ImageView ivTrackPic;
	Bitmap trackImage;
	double kmTravelled;

	int numBackEndTasks;

	String encodedImage;
	
	FrameLayout mainFrame;
	View helpView;
	Boolean firstRun;

	/**
	 * This method creates the activity
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mainFrame = new FrameLayout(this);
		mainFrame.addView(LayoutInflater.from(this).inflate(R.layout.activity_save_track, null));
		setContentView(mainFrame);
		
		
		trackapp = (TrackRateApp)getApplication();
		initialize();
		checkFirstRun();
		Intent saveTrackIntent = getIntent();
		//Here I get the passed bitmap image from the calling TrackMapActivity
		trackImage = (Bitmap)saveTrackIntent.getParcelableExtra("BitMapImage");
		calculateDistance();
		
		ivTrackPic.setImageBitmap(trackImage);
		
		ivTrackPic.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				//If the user clicks on the image then I open the FullScreenActivtiy so that the image can be shown in full screen
				Intent fullImageIntent = new Intent(getApplicationContext(), FullScreenActivity.class);
				fullImageIntent.putExtra("FullImage", trackImage);
				startActivity(fullImageIntent);
				
			}
			
		});
		etDisTrav.setText(String.valueOf(kmTravelled) + "Km");

	}
	
	/**
	 * This method check if activity has been run before
	 */
	private void checkFirstRun(){
		firstRun = getSharedPreferences(PREFERENCE, MODE_PRIVATE).getBoolean(FIRST_RUN_SAVE, true);
		getSharedPreferences(PREFERENCE, MODE_PRIVATE).edit().putBoolean(FIRST_RUN_SAVE, false).commit();
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
		Drawable img = getResources().getDrawable(R.drawable.save_track_help);		
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
	 * This method initializes all the views in the activity
	 */	
	private void initialize() {		
		etTrackName = (EditText) findViewById(R.id.etTrackName);
		etTrackDes = (EditText) findViewById(R.id.edTrackDescr);
		etDisTrav = (TextView) findViewById(R.id.etDisTravel);
		ivTrackPic = (ImageView) findViewById(R.id.trackImage);
		
		
	}

	/**
	 * This method check if all details are entered correctly and then calls the addTrack method
	 * 
	 * @param v the view that is clicked
	 */
	public void saveTrackToCloud(View v){		
		
		
		//if all the details are entered correctly
		if(checkIfDetailsCorrect()){

			//get the built trackrate endpoint			
			endpoint = trackapp.getEndpoint();
			//get a new instance of tracks
			Tracks tracks = new Tracks();
			
			//convert the image into a blob string
			ByteArrayOutputStream baos = new ByteArrayOutputStream();  
			trackImage.compress(Bitmap.CompressFormat.PNG, 100 , baos);    
			byte[] b = baos.toByteArray(); 
			encodedImage = Base64.encodeToString(b, Base64.DEFAULT);
			Log.d(TAG, "GeopointArray size is: " + trackapp.getGeoPtArray().size());

			//Insert all the details into the track instance
			tracks.setTrackName(String.valueOf(etTrackName.getText()));
			tracks.setTrackDescription(String.valueOf(etTrackDes.getText()));
			tracks.setTrackImage(encodedImage);
			tracks.setRoute(trackapp.getGeoPtArray());
			tracks.setDistanceTravelled(kmTravelled);
			//add track to cloud
			addTracks(tracks);
		}else{
			return;
		}
		

	}
	
	/**
	 * This method checks if a track name and the track description has been entered 
	 * 
	 * @return true if details have been entered. returns false if details have not been entered
	 */
	private boolean checkIfDetailsCorrect(){
		String chkTName = etTrackName.getText().toString();
		String chkTDes = etTrackDes.getText().toString();
		if(chkTName.trim().equals("")){
			Toast.makeText(this, "A track title must be entered before saving", Toast.LENGTH_LONG).show();
			return false;
		}else if(chkTDes.trim().equals("")){
			Toast.makeText(this, "A track description must be entered before saving", Toast.LENGTH_LONG).show();
			return false;
		}else if(kmTravelled == 0.00){
			Toast.makeText(this, "You didn't travel anywhere. Distance is 0", Toast.LENGTH_LONG).show();
			return false;
		}else{
			return true;
		}		
	}

	/**
	 * This method calls the AsyncAddTrack class
	 * 
	 * @param tracks the track to be added to the cloud
	 */
	public void addTracks(Tracks tracks) {		
		new AsyncAddTrack(this).execute(tracks);
	}

	/**
	 * This method calculate the distance travelled
	 */
	public void calculateDistance(){
		//get the geopt coordintates stored in TrackRateApp
		ArrayList<GeoPt> route = trackapp.getGeoPtArray();
		Log.i(TAG, "Route size is :" + route.size());
		//This list holds the results from the loop
		List<Float> results = new ArrayList<Float>();
		for(int i = 0; i<route.size(); i++){
			if((i+1)<route.size()){
				results.add(getDistance(route.get(i), route.get(i+1)));
			}
		}
		float disanceTravelled = 0;
		//This loop adds all the results together
		for(int j = 0; j<results.size(); j++){
			disanceTravelled = disanceTravelled + results.get(j);
		}
		//Here I set the amount of decimal points behind the zero
		DecimalFormat decfor = new DecimalFormat("#.##");
		kmTravelled = Double.valueOf((decfor.format(disanceTravelled/METERS_IN_KMS)));
		Toast.makeText(this, "Distance travelled is: " + kmTravelled + "km", Toast.LENGTH_LONG).show();
	}
	
	/**
	 * This method get the distance travelled between two points
	 * 
	 * @param startPt the start point coordinate
	 * @param endPt the end point coordinate
	 * @return returns the result calculation
	 */
	public float getDistance(GeoPt startPt, GeoPt endPt){
		float[] result = new float[1];
		//Here I use androids distance between method
		Location.distanceBetween(startPt.getLatitude(), startPt.getLongitude(), endPt.getLatitude(), endPt.getLongitude(), result);
		//The result is stored in the first position of the array 
		return result[0];		
	}

}

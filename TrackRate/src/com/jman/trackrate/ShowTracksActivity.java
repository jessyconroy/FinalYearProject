package com.jman.trackrate;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.jman.trackrate.tracksendpoint.Tracksendpoint;
import com.jman.trackrate.tracksendpoint.model.Tracks;

/**
 * This class show the tracks list
 * 
 * @author Jessy
 *
 */
public class ShowTracksActivity extends Activity {
	
	private static final String TAG = "ShowTracksActivity";
	
	private ListView listView;
	Tracksendpoint endpoint;
	TrackAdapter trackAdapter;
	TrackRateApp trackapp;
	int numBackEndTasks;
	Boolean showAllTracks;
	Boolean isMyTackList;

	/**
	 * This method creates the activity
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_show_tracks);
		trackapp = (TrackRateApp)getApplication();
		//Here I get the built trackendpoint
		endpoint = trackapp.getEndpoint();
		
		
		Intent allTrackIntent = getIntent();
		//Here I get the pass boolean value from the calling activity. If true this class shows the Top Rated tracks
		//If false this class show the users tracks
		showAllTracks = allTrackIntent.getBooleanExtra("ShowAllTracks" , true);
		
		listView = (ListView) findViewById(R.id.lvShowTracks);
		TextView tvListHeader = (TextView)findViewById(R.id.tvListHeader);
		if(showAllTracks){
			tvListHeader.setText("Top Rated Tracks");
		}else{
			tvListHeader.setText("Your Tracks");
		}
		
	
	    trackAdapter = trackapp.getTrackAdapter(this);
	    trackAdapter.setTracks(null);
	    //Here I call the get tracks method
		getTracks(showAllTracks);		
	    listView.setAdapter(trackAdapter);
	    
	    listView.setOnItemClickListener(new trackListClickListener());
	}

	/**
	 * This method call the AsyncListTracks class which searches the cloud for the desired tracks
	 * 
	 * @param showAllTracks the boolean value of whether to show Top rated or user tracks
	 */
	public void getTracks(Boolean showAllTracks){
		new AsyncListTracks(this, showAllTracks).execute();
		Log.i("LocationTrack", "Request Search");
	}
	
	/**
	 * This inner class is a list click listener. If a track is clicked then the TrackDetailsActivity is called
	 * 
	 * @author Jessy
	 *
	 */
	private class trackListClickListener implements OnItemClickListener{
		@Override
		public void onItemClick(AdapterView<?> Adpater, View view, int position,
				long arg3) {
			//Here I get the user name from the shared preferences
			SharedPreferences settings = getSharedPreferences(MainMenuActivity.TAG, 0);
			String accountName = settings.getString(MainMenuActivity.PREF_ACCOUNT_NAME, null);
			//Here I get the position of track track on the list which was clicked
			Tracks track = trackAdapter.getItem(position);
			Log.d(TAG, "AccountName is: " + accountName + " Track email address is: " + track.getUser().getEmail());
			//If the emails address from the shared preferences is equal to the email address of the track
			if(accountName.equalsIgnoreCase(track.getUser().getEmail())){
				isMyTackList = true;
			}else{
				isMyTackList = false;
			}
			//Here I save the clicked track into the TrackRateApp class
			trackapp.setSavedTrack(track);
			Intent listIntent = new Intent(getApplication(), TrackDetailsActivity.class);
			//Here I pass the boolean value to the TrackDetasilActivity
			listIntent.putExtra("isRateNotAvailable", isMyTackList);
			
			
			startActivity(listIntent);
			finish();

		}
		
	}

}

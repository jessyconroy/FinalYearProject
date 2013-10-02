package com.jman.trackrate;

import java.io.IOException;

import com.jman.trackrate.tracksendpoint.Tracksendpoint;
import com.jman.trackrate.tracksendpoint.model.Tracks;

import android.content.Intent;
import android.os.AsyncTask;
import android.view.View;
import android.widget.Toast;

/**
 * This is a class where the application can rate tracks from GAE's datastores asynchronously from the main thread
 * 
 * @author Jessy
 *
 */
public class AsyncRateTrack extends AsyncTask<Tracks, Void, Boolean>{



	final TrackDetailsActivity activity;
	final Tracksendpoint endpoint;
	private final View progressBar;
	private Boolean isRatePossible;

	public AsyncRateTrack(TrackDetailsActivity activity){
		this.activity = activity;
		this.endpoint = activity.endpoint;
		progressBar = activity.findViewById(R.id.title_refresh_progress);
	}

	/**
	 * Before execution set the progress bar to visible on the activity 
	 */
	@Override
	protected void onPreExecute() {
		activity.numBackEndTasks++;
		progressBar.setVisibility(View.VISIBLE);
	}

	/**
	 * This method performs the HTTP PUT to the datastore
	 */
	@Override
	protected Boolean doInBackground(Tracks... track) {
		try {
			Tracks result = endpoint.tracks().setRating(track[0]).execute();
			//null is returned if the user has already rated the track
			if(result.getDateUpload().equals(null)){
				isRatePossible = false;
			}else{
				isRatePossible = true;
			}
			return true;			
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * After execution set the progress bar to invisible on the activity.
	 * If the above method was successful start the ShowTracksActivity 
	 */
	@Override
	protected void onPostExecute(Boolean success) {
		if (0 == --activity.numBackEndTasks) {
			progressBar.setVisibility(View.GONE);
		}if(success){
			if(isRatePossible){
				Toast.makeText(activity, "Track Successfully rated",Toast.LENGTH_LONG).show();
				activity.startActivity(new Intent(activity, ShowTracksActivity.class));
				activity.finish();
			}else{
				Toast.makeText(activity, "You have rated already. You can only rate once per track",Toast.LENGTH_LONG).show();
			}
		}else{
			Toast.makeText(activity, "Track NOT rated, something went wrong. Please try again",Toast.LENGTH_LONG).show();
		}

	}
}
package com.jman.trackrate;

import java.io.IOException;

import com.jman.trackrate.tracksendpoint.Tracksendpoint;
import com.jman.trackrate.tracksendpoint.model.Tracks;

import android.content.Intent;
import android.os.AsyncTask;
import android.view.View;
import android.widget.Toast;


/**
 * This is a class where the application can delete a track from GAE's datastores asynchronously from the main thread
 * 
 * @author Jessy
 *
 */
public class AsyncDeleteTrack extends AsyncTask<Tracks, Void, Boolean>{



	final TrackDetailsActivity activity;
	final Tracksendpoint endpoint;
	private final View progressBar;

	public AsyncDeleteTrack(TrackDetailsActivity activity){
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
	 * This method performs the HTTP DELETE to the datastore
	 */
	@Override
	protected Boolean doInBackground(Tracks... track) {
		try {
			endpoint.tracks().delete(track[0].getId()).execute();
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
			activity.startActivity(new Intent(activity, ShowTracksActivity.class));
			activity.finish();
			Toast.makeText(activity, "Track Successfully deleted",Toast.LENGTH_LONG).show();
		}else{
			Toast.makeText(activity, "Track NOT deleted, Please try again",Toast.LENGTH_LONG).show();
		}

	}
}
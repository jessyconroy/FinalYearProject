package com.jman.trackrate;

import java.io.IOException;

import android.content.Intent;
import android.os.AsyncTask;
import android.view.View;
import android.widget.Toast;

import com.jman.trackrate.tracksendpoint.Tracksendpoint;
import com.jman.trackrate.tracksendpoint.model.Tracks;


/**
 * This is a class where the application can add tracks to the GAE's datastores asynchronously from the main thread
 * 
 * @author Jessy
 *
 */
public class AsyncAddTrack extends AsyncTask<Tracks, Void, Tracks>{



	final SaveTrackActivity activity;
	final Tracksendpoint endpoint;
	private final View progressBar;

	public AsyncAddTrack(SaveTrackActivity activity){
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
	protected Tracks doInBackground(Tracks... track) {
		Tracks result =  null;
		try {
			result = endpoint.tracks().insert(track[0]).execute();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	@Override
	protected void onProgressUpdate(Void... values) {
		// TODO Auto-generated method stub
		super.onProgressUpdate(values);
	}

	/**
	 * After execution set the progress bar to invisible on the activity.
	 * If the above method was successful start the MainMenuActivity 
	 */
	@Override
	protected void onPostExecute(Tracks result) {
		if (0 == --activity.numBackEndTasks) {
			progressBar.setVisibility(View.GONE);
		}if(result !=null){
			Toast.makeText(activity, "Track Successfully saved",Toast.LENGTH_LONG).show();
			activity.startActivity(new Intent(activity, MainMenuActivity.class));
			activity.trackapp.clearGeoPtArray();
			activity.finish();
		}else{
			Toast.makeText(activity, "Track did not Successfully save, Please try again",Toast.LENGTH_LONG).show();
		}

	}
}
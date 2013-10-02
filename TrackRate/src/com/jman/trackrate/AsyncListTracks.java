package com.jman.trackrate;

import java.io.IOException;
import java.util.List;

import com.jman.trackrate.tracksendpoint.Tracksendpoint;
import com.jman.trackrate.tracksendpoint.model.Tracks;

import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.Toast;


/**
 * This is a class where the application can list tracks from GAE's datastores asynchronously from the main thread
 * 
 * @author Jessy
 *
 */
public class AsyncListTracks extends AsyncTask<Void, Void, Boolean>{

	final ShowTracksActivity activity;
	final Tracksendpoint endpoint;
	final TrackAdapter adapter;
	private final View progressBar;
	List<Tracks> tracks;
	private Boolean showAllTracks;

	public AsyncListTracks(ShowTracksActivity activity, Boolean showAllTracks){
		this.activity = activity;
		this.endpoint = activity.endpoint;
		this.adapter = activity.trackAdapter;
		progressBar = activity.findViewById(R.id.title_refresh_progress);
		this.showAllTracks = showAllTracks;
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
	 * This method performs the HTTP GET to the datastore
	 */
	@Override
	protected Boolean doInBackground(Void... ignored) {
		String limit = "100";
		try {
			Log.i("LocationTrack", "Perfroming Search");
			tracks = endpoint.tracks().list().setLimit(limit).setListAllTracks(showAllTracks).execute().getItems();
			return true;

		} catch (IOException e) {
			e.printStackTrace();
		}


		return false;
	}

	/**
	 * After execution set the progress bar to invisible on the activity.
	 * If the above method was successful the TrackAdpater is updated with the returned list of tracks.
	 */
	@Override
	protected void onPostExecute(Boolean success) {
		if (0 == --activity.numBackEndTasks) {
			progressBar.setVisibility(View.GONE);
		}	
		if (success) {
			Log.i("LocationTrack", "Success");
			if(tracks != null){
			adapter.setTracks(tracks);
			activity.trackAdapter.notifyDataSetChanged();
			}else{
				Toast.makeText(activity, "You do not have any saved tracks",Toast.LENGTH_LONG).show();
			}
			
		}

	}

}

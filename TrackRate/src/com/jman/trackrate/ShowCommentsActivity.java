package com.jman.trackrate;


import com.jman.trackrate.tracksendpoint.model.Tracks;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

/**
 * This class shows the comments list
 * 
 * @author Jessy
 *
 */
@SuppressLint("ShowToast")
public class ShowCommentsActivity extends Activity {
	
private static final String TAG = "ShowCommentsActivity";
	
	private ListView listView;
	CommentAdapter adapter;

	/**
	 * This method creates the activity
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_show_comments);
		
		TrackRateApp trackapp = (TrackRateApp)getApplication();
		//Here I get the saved track from the TrackRateApp class
		Tracks track = trackapp.getSavedTrack();
		//Here I set the comments into the CommentAdapter class
		adapter = trackapp.getCommentAdapter(getApplicationContext());
		adapter.setComments(track.getCommentset());
		Log.i(TAG, "Size of CommentSet is: " + adapter.getCount());
		
		adapter.notifyDataSetChanged();
		listView = (ListView) findViewById(R.id.lvShowComments);
		listView.setEmptyView(findViewById(R.id.tvEmptyList));
		
	    listView.setAdapter(adapter);
	    
	 
	    
	}


}

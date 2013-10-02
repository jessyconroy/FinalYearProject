package com.jman.trackrate;

import java.util.ArrayList;

import android.app.Application;
import android.content.Context;

import com.jman.trackrate.tracksendpoint.Tracksendpoint;
import com.jman.trackrate.tracksendpoint.model.GeoPt;
import com.jman.trackrate.tracksendpoint.model.Tracks;

/**
 * This class is used to store values which remain consistent throughout the
 * lifecycle of the of the application. There values are cleared when a new application
 * lifecycle is run 
 * 
 * @author Jessy
 *
 */
public class TrackRateApp extends Application {

	private Tracksendpoint endpoint;
	private TrackAdapter trackAdapter;
	private CommentAdapter CommentAdapter;
	private Tracks savedTrack = new Tracks();


	private ArrayList<GeoPt> geoPtArray  = new ArrayList<GeoPt>();
	

	@SuppressWarnings("unused")
	private GeoPt geopt;

	public TrackRateApp() {

	}

	/**
	 * This method gets the TrackAdapter and creates a new instance if not already created
	 * 
	 * @param context of the activity
	 * @return the TrackAdapter
	 */
	public TrackAdapter getTrackAdapter(Context context) {
		if (trackAdapter == null) {
			trackAdapter = new TrackAdapter(context);
		}
		return trackAdapter;
	}
	
	/**
	 * This method gets the CommentsAdapter and creates a new instance if not already created
	 * 
	 * @param context of the activity
	 * @return the CommentsAdapter
	 */
	public CommentAdapter getCommentAdapter(Context context) {
		if (CommentAdapter == null) {
			CommentAdapter = new CommentAdapter(context);
		}
		return CommentAdapter;
	}

	/**
	 * This method puts the coordinates into a geopoint arraylist
	 * 
	 * @param geopt the data type that holds the latitude and longitude coordinates
	 */
	public void setGeopt(GeoPt geopt) {
		this.geopt = geopt;
		geoPtArray.add(geopt);
	}

	/**
	 * This method gets the arraylist of coordinates
	 * 
	 * @return the geopoint arraylist
	 */
	public ArrayList<GeoPt> getGeoPtArray() {		
		return geoPtArray;
	}
	
	/**
	 * This method clears the arraylist of coordinates
	 */
	public void clearGeoPtArray() {		
		geoPtArray.clear();
	}

	/**
	 * This method stores the endpoint which was built in the MainMenuActivty
	 * 
	 * @param endpoint the built TracksEndpoint
	 */
	public void setEndpoint(Tracksendpoint endpoint){
		this.endpoint = endpoint;
	}

	/**
	 * This method gets the built endpoint
	 * 
	 * @return the built endpoint
	 */
	public Tracksendpoint getEndpoint(){
		return endpoint;

	}

	/**
	 * This method gets the stored track
	 * 
	 * @return the saved track
	 */
	public Tracks getSavedTrack() {
		return savedTrack;
	}

	/**
	 * This method stores the track
	 * 
	 * @param savedTrack the track to be saved
	 */
	public void setSavedTrack(Tracks savedTrack) {
		this.savedTrack = savedTrack;
	}



}

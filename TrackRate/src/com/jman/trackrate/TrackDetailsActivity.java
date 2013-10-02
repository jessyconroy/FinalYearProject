package com.jman.trackrate;

import java.util.ArrayList;
import java.util.List;

import com.jman.trackrate.tracksendpoint.Tracksendpoint;
import com.jman.trackrate.tracksendpoint.model.Comments;
import com.jman.trackrate.tracksendpoint.model.Tracks;

import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RatingBar.OnRatingBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

/**
 * This class displays the selected track details from the the list view
 * 
 * @author Jessy
 *
 */
@SuppressLint({ "DefaultLocale", "ShowToast" })
public class TrackDetailsActivity extends Activity {


	private static final String TAG = "TrackDetailsActivity";
	static final String PREFERENCE = "PREFERENCE";
	static final String FIRST_RUN_DETAILS = "firstrundetails";

	Tracksendpoint endpoint;
	private TrackRateApp trackapp;
	int numBackEndTasks;
	TextView tvUserName;
	TextView tvTrackName;
	TextView tvTrackDescription;
	TextView tvDisTrav;
	ImageView ivTrackImage;
	Button btnRate;
	RatingBar trackRatingIndicator;
	TrackAdapter adapter;
	Tracks track;
	String userName;
	String trackName;
	String trackDescription;
	String trackImageString;
	Boolean isRateNotAvailable;
	int trackRating;
	int temprate;
	EditText etComment;

	FrameLayout mainFrame;
	View helpView;
	Boolean firstRun;




	/**
	 * This method creates the activity 
	 * 
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mainFrame = new FrameLayout(this);
		mainFrame.addView(LayoutInflater.from(this).inflate(R.layout.activity_track_details, null));
		setContentView(mainFrame);

		initialize();
		
		checkFirstRun();
		
		Intent listIntent = getIntent();
		//Here I get the passed boolean value from the calling activity
		isRateNotAvailable = listIntent.getBooleanExtra("isRateNotAvailable" ,false);
		
		//Here I decode the blob string image into a bitmap
		byte[] decodedString = track.decodeTrackImage();
		final Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length); 
		ivTrackImage.setImageBitmap(decodedByte);
		
		//Here I insert values into each view
		tvUserName.setText(track.getUser().getNickname());
		tvTrackName.setText(track.getTrackName().trim().toUpperCase());
		tvTrackDescription.setText(track.getTrackDescription().trim());
		trackRatingIndicator.setRating(track.getTrackRating());
		tvDisTrav.setText(String.valueOf(track.getDistanceTravelled()) + "Km");

		//If the passed boolean value is true then change the rate button to delete button
		//This is done so that the track owner cannot rate on his/her own track
		if(isRateNotAvailable){
			btnRate.setText("Delete Track");
		}

		//Here if the image is clicked it will start the FullScreenActivity and display the passed
		//image on full screen
		ivTrackImage.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				Intent fullImageIntent = new Intent(getApplicationContext(), FullScreenActivity.class);
				fullImageIntent.putExtra("FullImage", decodedByte);
				startActivity(fullImageIntent);

			}

		});

	}


	/**
	 * This method checks if activity has been run before
	 */
	private void checkFirstRun(){
		firstRun = getSharedPreferences(PREFERENCE, MODE_PRIVATE).getBoolean(FIRST_RUN_DETAILS, true);
		getSharedPreferences(PREFERENCE, MODE_PRIVATE).edit().putBoolean(FIRST_RUN_DETAILS, false).commit();
		if(firstRun){
			showFirstRunOverLay();
		}
	}

	/**
	 * This method shows the help overlay if activity is run for first time
	 */
	private void showFirstRunOverLay(){

		helpView = LayoutInflater.from(getBaseContext()).inflate(R.layout.help_overylay_layout, null);
		ImageView helpImage = (ImageView)helpView.findViewById(R.id.ivHelp);
		Drawable img = getResources().getDrawable(R.drawable.track_details_help);
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
	 * This method initializes all the views on the activity and gets an
	 * instance to the built trackendpoint
	 */
	private void initialize(){
		trackapp = (TrackRateApp)getApplication();
		endpoint = trackapp.getEndpoint();
		track = trackapp.getSavedTrack();
		ivTrackImage = (ImageView)findViewById(R.id.ivTrackDetailsImage);
		tvUserName = (TextView)findViewById(R.id.tvTrackDetailsUserName);
		tvTrackName = (TextView)findViewById(R.id.tvTrackDetailsName);
		tvDisTrav = (TextView)findViewById(R.id.tvTrackDetailsLength);
		tvTrackDescription = (TextView)findViewById(R.id.tvTrackDetailsDescrib);
		btnRate = (Button)findViewById(R.id.btnRateTrack);
		trackRatingIndicator = (RatingBar)findViewById(R.id.tvTrackDetailsRating);
	}

	/**
	 * This method starts the TrackMapActivty and shows a map view of the track route
	 * 
	 * @param v the view that has been clicked
	 */
	public void showTrackOnMap(View v){
		Intent startTrackIntent = new Intent(this, TrackMapActivity.class);
		//Here I pass the value 2 to the activity so that it won't start tracking the device
		//but rather show the route on the map and load the DetailsButtonFragment
		startTrackIntent.putExtra(MainMenuActivity.OPEN_MAP, 2);
		startActivity(startTrackIntent);
	}

	/**
	 * This method starts the rate track alert or delete track dialog
	 * depending on whether the user owns the viewed track or not
	 * 
	 * @param v the view the is clicked
	 */
	public void rateOrDeleteTrack(View v){

		if(btnRate.getText().equals("Delete Track")){

			final Dialog deleteDialog = new Dialog(this, R.style.cust_dialog);
			deleteDialog.setContentView(R.layout.delete_dialog_layout);
			deleteDialog.setTitle("Are you sure you want to delete track?");


			Button dialogButtonDelete = (Button) deleteDialog.findViewById(R.id.dialogDelete);
			dialogButtonDelete.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					//Here I call the delete track method
					deleteTrack(track);
					deleteDialog.dismiss();

				}
			});

			Button dialogButtonCancel = (Button) deleteDialog.findViewById(R.id.dialogCancelDelete);
			dialogButtonCancel.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					deleteDialog.dismiss();
				}
			});

			deleteDialog.show();		

		}else{
			final Dialog ratingDialog = new Dialog(this, R.style.cust_dialog);
			ratingDialog.setContentView(R.layout.rating_dialog_layout);
			ratingDialog.setTitle("What's this tracks rate?");

			etComment = (EditText) ratingDialog.findViewById(R.id.etComment);

			RatingBar trackRating = (RatingBar) ratingDialog.findViewById(R.id.dialogRatingBar);
			trackRating.setOnRatingBarChangeListener(new OnRatingBarChangeListener() { 

				//Here I get the value of the rating input by the user
				public void onRatingChanged(RatingBar ratingBar, float rating,  boolean fromUser) {

					temprate = (int) rating;
					Log.d(TAG, "Selected rating is: " + temprate);
				} 
			});

			Button dialogButtonRate = (Button) ratingDialog.findViewById(R.id.dialogSaveRating);
			dialogButtonRate.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					//Here I check if a comment was input
					if(checkDetailsCorrect()){
						//Here I create a new instance of comment so that I can store the input values
						//to it
						Comments comment = new Comments();
						comment.setComment(String.valueOf(etComment.getText()));
						comment.setCommentRating(temprate);

						//Here I create a list of comments so that I can insert it into the track
						List<Comments> listCmt = new ArrayList<Comments>();
						listCmt.add(comment);
						track.setCommentset(listCmt);
						//Here I call the update track method which tries to a rate to the track
						//depending whether the user has rated the track already
						updateTrack(track);
						ratingDialog.dismiss();
					}else{
						Toast.makeText(getBaseContext(), "You must comment on the track before you save", Toast.LENGTH_LONG).show();
					}

				}
			});

			Button dialogButtonCancel = (Button) ratingDialog.findViewById(R.id.dialogCancel);
			dialogButtonCancel.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					ratingDialog.dismiss();
				}
			});

			ratingDialog.show();
		}
	}

	/**
	 * This method check if the user has input a comment
	 * 
	 * @return true if comment inputted or false if not
	 */
	private boolean checkDetailsCorrect(){
		String chkComment = etComment.getText().toString();
		if(chkComment.trim().equals("")){
			return false;
		}else{
			return true;
		}

	}

	/**
	 * This method is called if the user presses the comment button on the activity
	 * 
	 * @param v the view that is clicked
	 */
	public void showComments(View v){
		//Here I start the ShowCommentsActivity
		startActivity(new Intent(this, ShowCommentsActivity.class));
	}

	/**
	 * This method calls the AsyncRateTrack class which asynchronously rates the track
	 * on the cloud
	 * 
	 * @param tracks the track to be rated
	 */
	public void updateTrack(Tracks tracks) {
		new AsyncRateTrack(this).execute(tracks);
	}

	/**
	 * This method calls the AsyncDeleteTrack class which asynchronously deletes the track
	 * on the cloud
	 * 
	 * @param tracks the track to be deleted
	 */
	public void deleteTrack(Tracks tracks) {
		new AsyncDeleteTrack(this).execute(tracks);
	}


}

package com.jman.trackrate;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.gson.GsonFactory;
import com.jman.trackrate.tracksendpoint.Tracksendpoint;
import com.jman.trackrate.tracksendpoint.model.GeoPt;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * This class is holds the buttons and functions of the MainMenuActivity.
 * Here I use the OAuth 2.0 login and I get the current position of the device.
 * 
 * @author Jessy
 *
 */
public class MainMenuActivity extends Activity {

	static final String TAG = "MainMenuActivity";
	static final String PREF_ACCOUNT_NAME = "accountName";
	static final String PREF_AUTH_TOKEN = "authToken";
	static final String OPEN_MAP = "openMap";
	static final int REQUEST_GOOGLE_PLAY_SERVICES = 0;
	static final int REQUEST_ACCOUNT_PICKER = 1;
	static final String PREFERENCE = "PREFERENCE";
	static final String FIRST_RUN_MAIN = "firstrunmain";

	private static final Level LOGGING_LEVEL = Level.ALL;





	//Preference object where the app stores the name of the preferred user.	 
	SharedPreferences settings;
	String accountName;


	//Credentials object that maintains tokens to send to the backend.
	GoogleAccountCredential credential;

	static boolean signedIn = false;


	//Service object that manages requests to the backend.
	Tracksendpoint endpoint;



	LocationManager trackLocMan;
	LocationListener trackLocLis;
	Location myCurrentLocation;
	Float myCurrentlat;
	Float myCurrentlon;
	GeoPt geopt;
	ImageButton btnShowAllTks;
	ImageButton btnShowMyTks;
	ImageButton btnStartTks;
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
		mainFrame.addView(LayoutInflater.from(this).inflate(R.layout.activity_main_menu, null));
		setContentView(mainFrame);

		initialize();

		settings = getSharedPreferences(TAG, 0);

		//Here I set the google credentials to the Audeince ID which is the the web client ID. This is used for GAE authentication
		credential = GoogleAccountCredential.usingAudience(this, AudienceID.AUDIENCE);
		setAccountName(settings.getString(PREF_ACCOUNT_NAME, null));

		//Here I build the endpoint so it can be used in the asynchronous tasks
		Tracksendpoint.Builder builder = new Tracksendpoint.Builder(
				AndroidHttp.newCompatibleTransport(), new GsonFactory(),
				credential);
		endpoint = builder.build();


		TrackRateApp trackapp = (TrackRateApp)getApplication();
		//Here I save the endpoint in the TrackRateApp class
		trackapp.setEndpoint(endpoint);

		//Here I check if the user has signed in or not
		if (credential.getSelectedAccountName() != null) {
			onSignIn();
		}else{
			setBtnsInvisible();
		}

		Logger.getLogger("com.google.api.client").setLevel(LOGGING_LEVEL);




		trackLocMan = (LocationManager)getSystemService(LOCATION_SERVICE);
		//check if GPS on
		boolean isGPSOn = trackLocMan.isProviderEnabled(LocationManager.GPS_PROVIDER);
		switchOnGps(isGPSOn);
		getLocation();

	}

	/**
	 * This method check if activity has been run before
	 */
	private void checkFirstRun(){
		firstRun = getSharedPreferences(PREFERENCE, MODE_PRIVATE).getBoolean(FIRST_RUN_MAIN, true);
		getSharedPreferences(PREFERENCE, MODE_PRIVATE).edit().putBoolean(FIRST_RUN_MAIN, false).commit();
		if(firstRun){
			showFirstRunOverLay();
		}
	}

	/**
	 * This method shthe ows help overlay if activity is run for first time
	 */
	private void showFirstRunOverLay(){

		helpView = LayoutInflater.from(getBaseContext()).inflate(R.layout.help_overylay_layout, null);
		ImageView helpImage = (ImageView)helpView.findViewById(R.id.ivHelp);
		Drawable img = getResources().getDrawable(R.drawable.main_screen_help);
		helpImage.setImageDrawable(img);
		mainFrame.addView(helpView);	

	}

	/**
	 * This method resoved the help overlay
	 */
	public void removeHelp(View v){
		mainFrame.removeView(helpView);
	}

	/**
	 * This method is called when the user logs out of the application
	 * 
	 * @param v the view that was clicked
	 */
	public void logOut(View v) {
		if (!signedIn) {
			chooseAccount();
		} else {
			forgetAccount();
			setSignInEnablement(true);
			setAccountLabel("(not signed in)");
		}
	}

	/**
	 * This method is called if the user has not logged into the application.
	 * It starts the choose account activity and obtains the resulting account name
	 * 
	 */
	private void chooseAccount() {
		startActivityForResult(credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
	}

	/**
	 * This method is called to set the retrieved account name into the applications shared preference.
	 * This stops the user from having to login every time the application starts.
	 * 
	 * @param accountName the account name being saved into the shared preferences
	 */
	private void setAccountName(String accountName) {
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(PREF_ACCOUNT_NAME, accountName);
		editor.commit();
		credential.setSelectedAccountName(accountName);
		this.accountName = accountName;
	}

	/**
	 * This method is called when the user has signed into the application
	 */
	private void onSignIn() {
		signedIn = true;
		setSignInEnablement(false);
		setAccountLabel(this.accountName);
		setBtnsInvisible();		
	}

	/**
	 * This method is called when the user has logged out of the application.
	 * The user OAuth 2.0 authorisation token is removed shared preferences. 
	 */
	private void forgetAccount() {
		signedIn = false;
		SharedPreferences.Editor editor2 = settings.edit();
		editor2.remove(PREF_AUTH_TOKEN);
		editor2.commit();
		setBtnsInvisible();
	}



	/**
	 * This method hides the menu buttons if the user has not logged in or
	 * displays the buttons if the user is logged in
	 */
	private void setBtnsInvisible(){
		if(signedIn){
			btnShowAllTks.setVisibility(View.VISIBLE);
			btnShowMyTks.setVisibility(View.VISIBLE);
			btnStartTks.setVisibility(View.VISIBLE);
		}else{
			btnShowAllTks.setVisibility(View.INVISIBLE);
			btnShowMyTks.setVisibility(View.INVISIBLE);
			btnStartTks.setVisibility(View.INVISIBLE);
		}
	}




	/**
	 * This method sets the text of the login button depending on whether the
	 * user if login in or not
	 * 
	 * @param state true if logged out, false if loggin in.
	 */
	private void setSignInEnablement(boolean state) {
		Button button = (Button) findViewById(R.id.btnLogOut);
		if (state) {
			button.setText("Sign In");
		} else {
			button.setText("Sign Out");
		}
	}

	/**
	 * This method sets the label to the email address of the user who has logged in
	 * 
	 * @param label the email address of the user
	 */
	private void setAccountLabel(String label) {
		TextView userLabel = (TextView) findViewById(R.id.tvUserName);
		userLabel.setText(label);
	}

	/**
	 * This method initializes all the views in the activity
	 */	
	private void initialize(){
		btnShowAllTks = (ImageButton) findViewById(R.id.btnShowAllTracks);
		btnShowMyTks = (ImageButton) findViewById(R.id.btnShowMyTracks);
		btnStartTks = (ImageButton) findViewById(R.id.btnStartNewTrack);
	}

	/**
	 * This method is called when the show top rated tracks button in pressed
	 * 
	 * @param v the view that is being clicked
	 */
	public void showAllTracks(View v) {
		Intent allTracksIntnent = new Intent(this, ShowTracksActivity.class);
		allTracksIntnent.putExtra("ShowAllTracks", true);
		startActivity(allTracksIntnent);
		stopGettingLocaiton();
	}

	/**
	 * This method start the ShowTracksActivity when the show my tracks button is pressed
	 * 
	 * @param v the view that has been clicked
	 */
	public void showMyTracks(View v) {
		Intent myTracksIntnent = new Intent(this, ShowTracksActivity.class);
		myTracksIntnent.putExtra("ShowAllTracks", false);
		startActivity(myTracksIntnent);
		stopGettingLocaiton();
	}


	/**
	 * This method starts the TrackMapActivity when the start tracking button is pressed
	 * 
	 * @param v the view that has been clicked
	 */
	public void startTracking(View v) {
		Intent startTrackIntent = new Intent(this, TrackMapActivity.class);
		//Here I pass the value 1 to the activity so that it will start tracking the device
		// and load the MapButtonFragment
		startTrackIntent.putExtra(OPEN_MAP, 1);
		startActivity(startTrackIntent);
		stopGettingLocaiton();
	}

	/**
	 * This method stops getting the device's location
	 */
	private void stopGettingLocaiton(){
		if(trackLocLis != null){
			trackLocMan.removeUpdates(trackLocLis);
			trackLocLis = null;
		}
	}


	/**
	 * This method is called if GPS is not activated on the device
	 * 
	 * @param isGPSOn true if GPS is on, false if GPS is not activated on device
	 */
	private void switchOnGps(Boolean isGPSOn){
		if (!isGPSOn) {
			//display alert dialog to switch on gps
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(R.string.diaglog_GPSMessage);
			builder.setPositiveButton(R.string.dialog_GPSSettings, new DialogInterface.OnClickListener() {						
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
					startActivity(intent);
					dialog.dismiss();
				}
			});
			builder.setNegativeButton(R.string.cancelMsg, new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					Toast.makeText(getBaseContext(), R.string.dialog_CancelMsg,Toast.LENGTH_LONG).show();
				}
			});

			AlertDialog alertDialog = builder.create();
			alertDialog.show();
		}
	}

	/**
	 * This method is called when the device location has changed
	 */
	private void getLocation(){
		trackLocLis = new LocationListener(){

			@Override
			public void onLocationChanged(Location location) {
				updateLocation(location);

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


		};
		//Get device's location just a single time
		trackLocMan.requestSingleUpdate(LocationManager.GPS_PROVIDER, trackLocLis, null);
	}

	/**
	 * This method prints a log for current location
	 * 
	 * @param location the location of the device
	 */
	private void updateLocation(Location location){
		myCurrentLocation = location;
		String txt = "My Location is Latitude = "+ myCurrentLocation.getLatitude()+" Longtitute = "+ myCurrentLocation.getLongitude();	
		Log.i(TAG, txt);	
	}


	/**
	 * This method is called when the activity is paused.
	 * The method stops getting the devices location
	 */
	@Override
	protected void onPause() {
		stopGettingLocaiton();
		super.onPause();
	}

	/**
	 * This method is called when the activity is resumed
	 * It resumes getting the device's location and check if google play services is installed in the device
	 */
	@Override
	protected void onResume() {
		super.onResume();
		getLocation();
		checkGooglePlayServicesAvailable();
	}

	/**
	 * This method starts the account picker where the user can select a google account
	 * to use. The activity stores the returned result in the SharedPreferences
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == REQUEST_ACCOUNT_PICKER){
			if (data != null && data.getExtras() != null) {
				String accountName = data.getExtras().getString(AccountManager.KEY_ACCOUNT_NAME);
				if (accountName != null) {
					setAccountName(accountName);
					SharedPreferences.Editor editor = settings.edit();
					editor.putString(PREF_ACCOUNT_NAME, accountName);
					editor.commit();
					onSignIn();
					checkFirstRun();
				}
			}
		}
	}

	/**
	 * This method checks that Google Play services APK is installed and up to date.
	 */
	private boolean checkGooglePlayServicesAvailable() {
		final int connectionStatusCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
		if (GooglePlayServicesUtil.isUserRecoverableError(connectionStatusCode)) {
			showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
			return false;
		}
		return true;
	}

	/**
	 * This method is called if the device does not have Google Play Services installed.
	 */
	void showGooglePlayServicesAvailabilityErrorDialog(final int connectionStatusCode) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Dialog dialog = GooglePlayServicesUtil.getErrorDialog(
						connectionStatusCode, MainMenuActivity.this, REQUEST_GOOGLE_PLAY_SERVICES);
				dialog.show();
			}
		});
	}


	/**
	 * This method shows the about menu
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the ment.
		getMenuInflater().inflate(R.menu.main_menu, menu);	
		return true;
	}	


	/**
	 * This method displays an alert dialog when the about menu button is pressed
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		AlertDialog.Builder alertbox = new AlertDialog.Builder(this);
		alertbox.setTitle("About TrackRate");


		alertbox.setMessage("Track your route, take a photo of your tracks " +
				"highlight and upload. Its that simple! " +
				"Don't forget to rate other users tracks." +
				"Whats your trackRate?");


		alertbox.setNeutralButton("Ok", new DialogInterface.OnClickListener() {


			public void onClick(DialogInterface arg0, int arg1) {


			}
		});


		alertbox.show();
		return true;

	}







}

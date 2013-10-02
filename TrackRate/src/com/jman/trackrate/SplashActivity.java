package com.jman.trackrate;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

/**
 * This class acts as a splash screen for the application
 * 
 * @author Jessy
 *
 */
public class SplashActivity extends Activity {

	private final int SPLASH_DISPLAY_TIME = 1500;
	Handler myHandler = new Handler();

	/**
	 * This method creates the activity
	 */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        TextView txtTop = (TextView)findViewById(R.id.tvTextTopSplash);
        TextView txtBottom = (TextView)findViewById(R.id.tvTextButtomSplash);
        //Here I insert a custom typeface to the text
        Typeface typefc = Typeface.createFromAsset(getAssets(), "fonts/bauhaus.ttf");
        txtTop.setTypeface(typefc);
        txtBottom.setTypeface(typefc);
		
        //Here I display the spash screen for 1 second
        myHandler.postDelayed(splashTime, SPLASH_DISPLAY_TIME);
        
        
    }
    
    /**
     * This method runs a runnable for one second and then starts the MainMenuActivity
     */
    private Runnable splashTime = new Runnable(){
    	public void run(){
    		Intent mainIntent = new Intent(SplashActivity.this, MainMenuActivity.class);
    		SplashActivity.this.startActivity(mainIntent);
    		SplashActivity.this.finish();   		
    	}
    };

}

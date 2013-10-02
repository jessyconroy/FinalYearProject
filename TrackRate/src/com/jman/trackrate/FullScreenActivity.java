package com.jman.trackrate;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;

/**
 * This class displays the selected image in full screen
 * 
 * @author Jessy
 *
 */
public class FullScreenActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_full_screen);
		
		Bitmap trackImage;
		//Here I get the passed image from the calling activity
		Intent getImageIntent = getIntent();
		trackImage = (Bitmap)getImageIntent.getParcelableExtra("FullImage");
		
		ImageView ivFullImage = (ImageView)findViewById(R.id.fullImageView);
		ivFullImage.setImageBitmap(trackImage);
	}


}

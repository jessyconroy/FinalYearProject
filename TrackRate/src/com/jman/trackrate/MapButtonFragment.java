package com.jman.trackrate;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * This class is a fragment that is used in the TrackMapActivity.
 * This class is called when the MainMenuActivity starts the TrackMapActivity
 * 
 * @author Jessy
 *
 */
public class MapButtonFragment extends Fragment {
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, 
        Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.tracking_buttons_view, container, false);
        return view;
    }


	
	
	/**
	 * This method changes the pause buttons text
	 * 
	 * @param txt the text to be inserted to the pasue button
	 */
	public void setPauseText(String txt){
		Button btnPaused = (Button)getView().findViewById(R.id.btnPause);
		btnPaused.setText(txt);
	}
}

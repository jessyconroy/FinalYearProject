package com.jman.trackrate;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * This class is a fragment that is used in the TrackMapActivity.
 * This class is called when the TrackDetailsActivity starts the TrackMapActivity
 * 
 * @author Jessy
 *
 */
public class DetailsButtonFragment extends Fragment {
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, 
        Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.details_buttons_view, container, false);
        
        return view;
    }

}

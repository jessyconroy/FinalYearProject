package com.jman.trackrate;

import java.util.ArrayList;
import java.util.List;

import com.jman.trackrate.tracksendpoint.model.Tracks;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

/**
 * This class is where I set up the custom tracks list
 * 
 * @author Jessy
 *
 */
@SuppressLint("DefaultLocale")
public class TrackAdapter extends BaseAdapter{

	final static class ViewHolder {
		ImageView image;
		TextView title;
		TextView usernme;		
		TextView ratecount;
		RatingBar rating;
		TextView trackLength;
	}

	private final List<Tracks> trackitems = new ArrayList<Tracks>();
	private final LayoutInflater inflater;

	TrackAdapter(Context context) {
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	/**
	 * This method adds the tracks to the adapter trackitems list
	 * 
	 * @param comments the comments begin added
	 */
	void setTracks(List<Tracks> tracks) {
		synchronized (this.trackitems) {
			trackitems.clear();
			if (tracks != null) {
				trackitems.addAll(tracks);		      
			}
		}
	}

	/**
	 * This method returns the size of the trackitems list
	 */
	@Override
	public int getCount() {
		synchronized (trackitems) {
			return trackitems.size();
		}
	}

	/**
	 * This method gets a track from the trackitems list in relation to its position
	 */
	@Override
	public Tracks getItem(int position) {
		synchronized (trackitems) {
			return trackitems.get(position);
		}
	}

	/**
	 * This method returns the items position in the list
	 */
	@Override
	public long getItemId(int position) {
		return position;
	}

	/**
	 * This method sets up the contents of the list
	 * 
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.list_view_row, null);
			holder = new ViewHolder();
			holder.image = (ImageView) convertView.findViewById(R.id.ivListTrackImage);
			holder.title = (TextView) convertView.findViewById(R.id.tvListTrackName);			
			holder.usernme = (TextView) convertView.findViewById(R.id.tvListUserName);
			holder.ratecount = (TextView) convertView.findViewById(R.id.tvListRateCount);
			holder.rating = (RatingBar) convertView.findViewById(R.id.listRatingBar);
			holder.trackLength  = (TextView) convertView.findViewById(R.id.tvListTrackLength);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		Tracks track = getItem(position);

		

		if(track.getTrackImage() != null || track.getTrackName() != null || track.getTrackDescription() !=null){
			holder.title.setText(track.getTrackName().trim().toUpperCase());
			holder.usernme.setText(track.getUser().getNickname());
			holder.ratecount.setText(String.valueOf(track.getRatingCount()));
			holder.rating.setRating(track.getTrackRating());
			holder.trackLength.setText(String.valueOf(track.getDistanceTravelled()) + "Km");
			byte[] decodedString = track.decodeTrackImage();
			Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
			holder.image.setImageBitmap(decodedByte);
		}
		return convertView;
	}

}

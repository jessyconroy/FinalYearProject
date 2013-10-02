package com.jman.trackrate;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RatingBar;
import android.widget.TextView;

import com.jman.trackrate.tracksendpoint.model.Comments;



/**
 * This class is where a set up the custom comment list
 * 
 * @author Jessy
 *
 */
public class CommentAdapter extends BaseAdapter{

	final static class ViewHolder {
		TextView usernme;		
		TextView dateUpload;
		RatingBar rating;
		TextView comment;
	}

	private final List<Comments> commentitems = new ArrayList<Comments>();
	private final LayoutInflater inflater;

	CommentAdapter(Context context) {
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	/**
	 * This method adds the comments to the adapter commentitems list
	 * 
	 * @param comments the comments begin added
	 */
	void setComments(List<Comments> comments) {
		synchronized (this.commentitems) {
			commentitems.clear();
			if (comments != null) {
				commentitems.addAll(comments);		      
			}
		}
	}

	/**
	 * This method returns the size of the commentitems list
	 */
	@Override
	public int getCount() {
		synchronized (commentitems) {
			return commentitems.size();
		}
	}

	/**
	 * This method gets a comment from the commentitems list in relation to its position
	 */
	@Override
	public Comments getItem(int position) {
		synchronized (commentitems) {
			return commentitems.get(position);
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
			convertView = inflater.inflate(R.layout.comment_view_row, null);
			holder = new ViewHolder();			
			holder.usernme = (TextView) convertView.findViewById(R.id.tvCommentListUserName);
			holder.rating = (RatingBar) convertView.findViewById(R.id.commentListRatingBar);
			holder.dateUpload  = (TextView) convertView.findViewById(R.id.tvCommentListDate);
			holder.comment  = (TextView) convertView.findViewById(R.id.tvCommentListCommentTxt);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		Comments comments = getItem(position);

		

		if(comments.getComment() != null || comments.getCommentRating() != null){
			String retrievedDate = String.valueOf(comments.getDateOfComment());
			//Here I a set the date to be user readable
			String splitDate = retrievedDate.split("T")[0];
			holder.dateUpload.setText(String.valueOf(splitDate));
			holder.usernme.setText(comments.getUser().getNickname());
			holder.comment.setText(comments.getComment()) ;
			holder.rating.setRating(comments.getCommentRating());
		}
		return convertView;
	}

}

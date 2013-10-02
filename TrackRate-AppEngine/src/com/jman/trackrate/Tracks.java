package com.jman.trackrate;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.jdo.annotations.Element;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Order;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.GeoPt;
import com.google.appengine.api.users.User;


/**
 * This is where I defined the types of data that need to be stored in GAE's Tracks datastore
 * 
 * @author Jessy
 *
 */
@PersistenceCapable(detachable = "true", identityType = IdentityType.APPLICATION)
public class Tracks {


	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Long id;

	@Persistent
	private User user;
	@Persistent
	private Date dateUpload;
	@Persistent
	private String trackName;
	@Persistent
	private String trackDescription;
	@Persistent
	ArrayList<GeoPt> route;
	@Persistent
	private double distanceTravelled;
	@Persistent
	private int trackRating;
	@Persistent
	private Blob trackImage;
	@Persistent
	private int ratingCount;
	//This a linked datastore which has a one-to-many relationship with the Tracks datastore
	@Persistent
	@Element(dependent = "true")
	@Order(extensions = @javax.jdo.annotations.Extension(vendorName = "datanucleus", key="list-ordering", value="dateOfComment desc"))
	private List<Comments> commentset;



	public Tracks(User user,  Date dateUpload, String trackName,
			String trackDescription, ArrayList<GeoPt> route, int trackRating, Blob trackImage, double distanceTravelled ){
		this.user = user;
		this.dateUpload = dateUpload;
		this.trackName = trackName;
		this.trackDescription = trackDescription;
		this.route = route;
		this.trackRating = trackRating;
		this.trackImage = trackImage;
		this.distanceTravelled = distanceTravelled;

	}


	public String getTrackName() {
		return trackName;
	}


	public void setTrackName(String trackName) {
		this.trackName = trackName;
	}


	public String getTrackDescription() {
		return trackDescription;
	}


	public void setTrackDescription(String trackDescription) {
		this.trackDescription = trackDescription;
	}


	/**
	 * Calculate the average Track Rating based on data stored in Comments class
	 * 
	 * @return the average track rating per track
	 */
	public int getTrackRating() {
		if(commentset != null){
			float tempNum = 0;
			for(int i = 0; i< commentset.size(); i++){
				int tempNum2 = commentset.get(i).getCommentRating();
				tempNum = tempNum + tempNum2;
			}
			trackRating = (int) Math.round(tempNum/commentset.size());
		}else{
			trackRating = 0;
		}
		return trackRating;
	}




	/**
	 * Get the amount of comments attributed with this track
	 * 
	 * @return the total amount of ratings
	 */
	public int getRatingCount(){
		if(commentset != null){
		ratingCount = commentset.size();
		}else{
			ratingCount = 0;
		}
		return ratingCount;

	}


	public Blob getTrackImage() {
		return trackImage;
	}


	public void setTrackImage(Blob trackImage) {
		this.trackImage = trackImage;
	}


	public ArrayList<GeoPt> getRoute() {
		return route;
	}

	public void setRoute(ArrayList<GeoPt> route) {
		this.route = route;
	}

	public Date getDateUpload() {
		return dateUpload;
	}

	public void setDateUpload(Date dateUpload) {
		this.dateUpload = dateUpload;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Long getId() {
		return id;
	}


	public double getDistanceTravelled() {
		return distanceTravelled;
	}


	public void setDistanceTravelled(double distanceTravelled) {
		this.distanceTravelled = distanceTravelled;
	}


	public List<Comments> getCommentset() {
		return commentset;
	}


	public void setCommentset(List<Comments> commentset) {
		this.commentset = commentset;
	}

}

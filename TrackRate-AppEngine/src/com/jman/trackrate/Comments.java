package com.jman.trackrate;

import java.util.Date;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.users.User;

/**
 * This is where I defined the types of data that need to be stored in GAE's Comments datastore.
 * This datastore is dependent on the Tracks datastore. It cannot exist without Tracks datastore.
 *  
 * @author Jessy
 *
 */
@PersistenceCapable(detachable = "true", identityType = IdentityType.APPLICATION)
public class Comments{

	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Key id;

	@Persistent
	private User user;
	@Persistent
	private Date dateOfComment;
	@Persistent
	private String comment;	
	@Persistent
	private int commentRating;
	
	public Comments(){

	}
	
	public Key getId() {
		return id;
	}
	
	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Date getDateOfComment() {
		return dateOfComment;
	}

	public void setDateOfComment(Date dateOfComment) {
		this.dateOfComment = dateOfComment;
	}	

	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}

	public int getCommentRating() {
		return commentRating;
	}

	public void setCommentRating(int commentRating) {
		this.commentRating = commentRating;
	}
}


package com.jman.trackrate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nullable;
import javax.inject.Named;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.Transaction;
import javax.jdo.annotations.Transactional;
import javax.persistence.EntityNotFoundException;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiMethod.HttpMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.NotFoundException;
import com.google.appengine.api.oauth.OAuthRequestException;
import com.google.appengine.api.users.User;


/**
 * This is where I created some Google App Engine API's to use on the TrackRate application.
 * These API's allows a user to manipulate the data stored in the datastore.
 * 
 * @author Jessy
 *
 */
@Api(name = "tracksendpoint",
namespace = @ApiNamespace(ownerDomain = "jman.com", ownerName = "jman.com", packagePath = "trackrate"),
clientIds = {Ids.WEB_CLIENT_ID, Ids.ANDROID_CLIENT_ID},
audiences = {Ids.ANDROID_AUDIENCE})
public class TracksEndpoint {

	private final Logger log = Logger.getLogger("TrackEndPoint");
	private static final String DEFAULT_LIMIT = "10";
	private static final Boolean LIST_ALL_TRACKS = true;


	/**
	 * This method list the tracks dependent of whether the user/process has requested to
	 * list all or just the users tracks.
	 * It uses HTTP GET method and paging support. 
	 * 
	 * @param limit the amount of track to be returned
	 * @param listAllTracks whether to list all tracks or just the users tracks
	 * @param user the person/process looking to perform operation
	 * @return a list of returned tracks
	 * @throws OAuthRequestException
	 * @throws IOException
	 */
	@ApiMethod(name="tracks.list")
	@SuppressWarnings({ "unchecked" })
	public List<Tracks> list(@Nullable @Named("limit") String limit, @Nullable @Named("listAllTracks") Boolean listAllTracks, User user) throws OAuthRequestException,
	IOException {
		PersistenceManager pm = getPersistenceManager();
		Query query = pm.newQuery(Tracks.class);		   
		query.setOrdering("trackRating desc, ratingCount desc");

		if (listAllTracks == null) {
			listAllTracks = LIST_ALL_TRACKS;
		}

		if(!listAllTracks){
			if (user != null) {
				query.setFilter("user == userParam");
				query.declareParameters("com.google.appengine.api.users.User userParam");
			} else {
				throw new OAuthRequestException("Invalid user.");
			}
		}else{
			if (user == null) {
				throw new OAuthRequestException("Invalid user.");
			}

		}

		if (limit == null) {
			limit = DEFAULT_LIMIT;
		}


		query.setRange(0, new Long(limit));

		return (List<Tracks>) pm.newQuery(query).execute(user);

	}


	/**
	 * This inserts a new track into App Engine datastore. If the track already
	 * exists in the datastore, an exception is thrown.
	 * It uses HTTP POST method.
	 *
	 * @param tracks the entity to be inserted.
	 * @param user the person/process looking to perform operation
	 * @return The inserted entity.
	 */
	@ApiMethod(name="tracks.insert")
	@Transactional
	public Tracks insertTracks(Tracks tracks, User user)throws
	OAuthRequestException, IOException {
		if (user != null) {
			List<Comments> comtList = new ArrayList<Comments>();


			tracks.setDateUpload(new Date());
			tracks.setUser(user);
			tracks.setCommentset(comtList);
			PersistenceManager mgr = getPersistenceManager();
			Transaction trns = mgr.currentTransaction();
			try{
				trns.begin();
				mgr.makePersistent(tracks);
				trns.commit();			
			}finally{
				if(trns.isActive()){
					trns.rollback();
				}
			}			
			mgr.close();
			return tracks;
		}else {
			throw new OAuthRequestException("Invalid user.");
		}
	}


	/**
	 * This method gives a track a rate if the user has
	 * not rated the track already.
	 * It uses HTTP PUT method.
	 * 
	 * @param dataTrack The track which holds the stored data
	 * @param user the person/process looking to perform operation
	 * @return The updated track
	 * @throws OAuthRequestException
	 * @throws NotFoundException
	 */
	@ApiMethod(name="tracks.setRating",
			path = "tracks/rating/",
			httpMethod = HttpMethod.PUT)
	@Transactional
	public Tracks setTrackRating(Tracks dataTrack, User user) throws OAuthRequestException, NotFoundException {

		log.setLevel(Level.INFO);
		Boolean hasEmail = false;
		Tracks updateTrack = null;
		if (user != null){
			PersistenceManager mgr = getPersistenceManager();
			try {				
				if (dataTrack == null) {
					throw new EntityNotFoundException("Track does not exist");
				}



				updateTrack = mgr.getObjectById(Tracks.class, dataTrack.getId());
				// loop through all the tracks comments. If a user's email is found then exit
				for(int i = 1; i<updateTrack.getCommentset().size(); i++){
					log.info("ID " + i + " email is: " + updateTrack.getCommentset().get(i).getUser().getEmail());
					if(updateTrack.getCommentset().get(i).getUser().getEmail().equalsIgnoreCase(user.getEmail())){						
						hasEmail = true;
						break;
					}
				}
				if(!hasEmail){
					log.info("Has email in IF statemtment is: " + hasEmail);

					//Create a new instance of comments
					Comments comment = new Comments();
					//Store the recieved data from the datatrack
					comment.setUser(user);
					comment.setComment(dataTrack.getCommentset().get(0).getComment());
					comment.setDateOfComment(new Date());
					comment.setCommentRating(dataTrack.getCommentset().get(0).getCommentRating());
					//Store the comments into the track
					updateTrack.getCommentset().add(comment);
					log.info("Username: " + user.getNickname() + " Tracks name: " + dataTrack.getTrackName() +
							" Comment: " + comment.getComment() + " Rating: " + comment.getCommentRating());
					Transaction trns = mgr.currentTransaction();
					mgr.setDetachAllOnCommit(true);
					try{						
						trns.begin();
						mgr.makePersistent(updateTrack);
						trns.commit();			
					}finally{
						if(trns.isActive()){
							trns.rollback();
						}
					}
				}else{
					log.info("Has email in ELSE statemtment is: " + hasEmail);
				}
			}finally {
				mgr.close();
			}

			return updateTrack;
		}else{			
			throw new OAuthRequestException("Invalid user.");
		}
	}

	/**
	 * This method removes the entity with primary key id.
	 * It uses HTTP DELETE method.
	 *
	 * @param id the primary key of the track to be deleted.
	 * @param user the person/process looking to perform operation
	 * @return The deleted track.
	 * @throws NotFoundException
	 * @throws OAuthRequestException
	 */
	@ApiMethod(name="tracks.delete")
	@Transactional
	public Tracks removeTracks(@Named("id") Long id, User user) throws
	OAuthRequestException, NotFoundException{
		if (user != null) {
			PersistenceManager mgr = getPersistenceManager();
			Transaction trns = mgr.currentTransaction();
			Tracks tracks = null;
			try {
				tracks = getTrackById(id);
				if (tracks == null || !tracks.getUser().getEmail().equalsIgnoreCase(user.getEmail())) {
					throw new NotFoundException("track not found");
				}
				try{						
					trns.begin();
					mgr.deletePersistent(mgr.getObjectById(tracks.getClass(), tracks.getId()));
					trns.commit();			
				}finally{
					if(trns.isActive()){
						trns.rollback();
					}
				}

			} finally {
				mgr.close();
			}
			return tracks;
		}else {
			throw new OAuthRequestException("Invalid user.");
		}
	}

	/**
	 * The method searches for a track based on the id
	 * 
	 * 
	 * @param id the primary key of the track to search for
	 * @return the found track
	 */
	private Tracks getTrackById(Long id) {
		PersistenceManager mgr = getPersistenceManager();
		Tracks track = null;
		try {
			if(id == null){
				return null;
			}
			track = mgr.getObjectById(Tracks.class, id);
		} catch (javax.jdo.JDOObjectNotFoundException ex) {
			return null;
		} finally {
			mgr.close();
		}
		return track;
	}

	private static PersistenceManager getPersistenceManager() {
		return PMF.get().getPersistenceManager();
	}


}
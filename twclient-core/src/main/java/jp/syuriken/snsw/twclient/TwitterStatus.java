package jp.syuriken.snsw.twclient;

import java.util.Date;

import twitter4j.Annotations;
import twitter4j.GeoLocation;
import twitter4j.HashtagEntity;
import twitter4j.MediaEntity;
import twitter4j.Place;
import twitter4j.RateLimitStatus;
import twitter4j.Status;
import twitter4j.URLEntity;
import twitter4j.User;
import twitter4j.UserMentionEntity;

/**
 * RT/favしたかどうかを格納できるTwitter拡張型
 * 
 * @author $Author: snsoftware $
 */
@SuppressWarnings({
	"serial",
	"deprecation"
})
public class TwitterStatus implements Status {
	
	private final Status originalStatus;
	
//	private Status retweetedStatus;
	
	private volatile boolean favorited;
	
	private volatile boolean retweetedByMe;
	
	
	/**
	 * インスタンスを生成する。
	 * 
	 * @param originalStatus オリジナルステータス
	 */
	public TwitterStatus(Status originalStatus) {
		this.originalStatus = originalStatus;
//		retweetedStatus = originalStatus.getRetweetedStatus();
		favorited = originalStatus.isFavorited();
		retweetedByMe = originalStatus.isRetweetedByMe();
	}
	
	@Override
	public int compareTo(Status o) {
		return originalStatus.compareTo(o);
	}
	
	@Override
	public int getAccessLevel() {
		return originalStatus.getAccessLevel();
	}
	
	@SuppressWarnings("deprecation")
	@Deprecated
	@Override
	public Annotations getAnnotations() {
		return originalStatus.getAnnotations();
	}
	
	@Override
	public long[] getContributors() {
		return originalStatus.getContributors();
	}
	
	@Override
	public Date getCreatedAt() {
		return originalStatus.getCreatedAt();
	}
	
	@Override
	public GeoLocation getGeoLocation() {
		return originalStatus.getGeoLocation();
	}
	
	@Override
	public HashtagEntity[] getHashtagEntities() {
		return originalStatus.getHashtagEntities();
	}
	
	@Override
	public long getId() {
		return originalStatus.getId();
	}
	
	@Override
	public String getInReplyToScreenName() {
		return originalStatus.getInReplyToScreenName();
	}
	
	@Override
	public long getInReplyToStatusId() {
		return originalStatus.getInReplyToStatusId();
	}
	
	@Override
	public long getInReplyToUserId() {
		return originalStatus.getInReplyToUserId();
	}
	
	@Override
	public MediaEntity[] getMediaEntities() {
		return originalStatus.getMediaEntities();
	}
	
	@Override
	public Place getPlace() {
		return originalStatus.getPlace();
	}
	
	@Override
	public RateLimitStatus getRateLimitStatus() {
		return originalStatus.getRateLimitStatus();
	}
	
	@Override
	public long getRetweetCount() {
		return originalStatus.getRetweetCount();
	}
	
	@Override
	public Status getRetweetedStatus() {
		return originalStatus.getRetweetedStatus();
	}
	
	@Override
	public String getSource() {
		return originalStatus.getSource();
	}
	
	@Override
	public String getText() {
		return originalStatus.getText();
	}
	
	@Override
	public URLEntity[] getURLEntities() {
		return originalStatus.getURLEntities();
	}
	
	@Override
	public User getUser() {
		return originalStatus.getUser();
	}
	
	@Override
	public UserMentionEntity[] getUserMentionEntities() {
		return originalStatus.getUserMentionEntities();
	}
	
	@Override
	public boolean isFavorited() {
		return favorited;
	}
	
	@Override
	public boolean isRetweet() {
		return originalStatus.isRetweet();
	}
	
	@Override
	public boolean isRetweetedByMe() {
		return retweetedByMe;
	}
	
	@Override
	public boolean isTruncated() {
		return originalStatus.isTruncated();
	}
	
	/**
	 * TODO snsoftware
	 * 
	 * @param favorited the favorited to set
	 */
	public void setFavorited(boolean favorited) {
		this.favorited = favorited;
	}
	
	/**
	 * TODO snsoftware
	 * 
	 * @param retweetedByMe the retweetedByMe to set
	 */
	public void setRetweetedByMe(boolean retweetedByMe) {
		this.retweetedByMe = retweetedByMe;
	}
	
	/**
	 * TODO snsoftware
	 * 
	 * @param retweetedStatus the retweetedStatus to set
	 */
	public void setRetweetedStatus(Status retweetedStatus) {
//		this.retweetedStatus = retweetedStatus;
	}
	
}

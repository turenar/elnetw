package jp.syuriken.snsw.twclient.filter;

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

@SuppressWarnings({
	"serial",
	"deprecation"
})
/*package*/class TestStatus implements Status {

	private final TestUser user;

	private final Status retweetedStatus;

	private final long inReplyToUserId;

	private final long id;


	public TestStatus(long id) {
		retweetedStatus = null;
		inReplyToUserId = -1;
		user = null;
		this.id = id;
	}

	public TestStatus(long createdUserId, Status retweetedStatus, long inReplyToUserId) {
		this.retweetedStatus = retweetedStatus;
		this.inReplyToUserId = inReplyToUserId;
		user = new TestUser(createdUserId);
		id = 0;
	}

	@Override
	public int compareTo(Status o) {
		return 0;
	}

	@Override
	public int getAccessLevel() {
		return 0;
	}

	@Deprecated
	@Override
	public Annotations getAnnotations() {
		return null;
	}

	@Override
	public long[] getContributors() {
		return null;
	}

	@Override
	public Date getCreatedAt() {
		return null;
	}

	@Override
	public GeoLocation getGeoLocation() {
		return null;
	}

	@Override
	public HashtagEntity[] getHashtagEntities() {
		return null;
	}

	@Override
	public long getId() {
		return id;
	}

	@Override
	public String getInReplyToScreenName() {
		return null;
	}

	@Override
	public long getInReplyToStatusId() {
		return 0;
	}

	@Override
	public long getInReplyToUserId() {
		return inReplyToUserId;
	}

	@Override
	public MediaEntity[] getMediaEntities() {
		return null;
	}

	@Override
	public Place getPlace() {
		return null;
	}

	@Override
	public RateLimitStatus getRateLimitStatus() {
		return null;
	}

	@Override
	public long getRetweetCount() {
		return 0;
	}

	@Override
	public Status getRetweetedStatus() {
		return retweetedStatus;
	}

	@Override
	public String getSource() {
		return null;
	}

	@Override
	public String getText() {
		return null;
	}

	@Override
	public URLEntity[] getURLEntities() {
		return null;
	}

	@Override
	public User getUser() {
		return user;
	}

	@Override
	public UserMentionEntity[] getUserMentionEntities() {
		return null;
	}

	@Override
	public boolean isFavorited() {
		return false;
	}

	@Override
	public boolean isRetweet() {
		return retweetedStatus != null;
	}

	@Override
	public boolean isRetweetedByMe() {
		return false;
	}

	@Override
	public boolean isTruncated() {
		return false;
	}
}

/*
 * The MIT License (MIT)
 * Copyright (c) 2011-2014 Turenai Project
 *
 * Permission is hereby granted, free of charge,
 *  to any person obtaining a copy of this software and associated documentation files (the "Software"),
 *  to deal in the Software without restriction, including without limitation the rights to
 *  use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 *  and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 *  in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 *  INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 *  IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 *  DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package jp.mydns.turenar.twclient.twitter;

import java.util.Date;

import javax.annotation.Nonnull;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jp.mydns.turenar.twclient.CacheManager;
import jp.mydns.turenar.twclient.ClientConfiguration;
import jp.mydns.turenar.twclient.Utility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.GeoLocation;
import twitter4j.HashtagEntity;
import twitter4j.JSONException;
import twitter4j.JSONObject;
import twitter4j.MediaEntity;
import twitter4j.Place;
import twitter4j.RateLimitStatus;
import twitter4j.Scopes;
import twitter4j.Status;
import twitter4j.SymbolEntity;
import twitter4j.TwitterObjectFactory;
import twitter4j.URLEntity;
import twitter4j.User;
import twitter4j.UserMentionEntity;

/**
 * RT/favしたかどうかを格納できるStatus拡張型
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class TwitterStatus implements Status {
	private static final long serialVersionUID = 6149133579765335552L;
	private static final Logger logger = LoggerFactory.getLogger(TwitterStatus.class);
	public static final ClientConfiguration configuration = ClientConfiguration.getInstance();

	/**
	 * get TwitterStatus from specified status.
	 *
	 * <ul>
	 * <li>First, return just status if status instanceof TwitterStatus.</li>
	 * <li>Second, check status is in cache. if status is cached, return that instance updated by status.</li>
	 * <li>Otherwise, make instance from status.</li>
	 * </ul>
	 *
	 * @param status status
	 * @return TwitterStatus instance
	 */
	public static TwitterStatus getInstance(Status status) {
		if (status instanceof TwitterStatus) {
			return (TwitterStatus) status;
		}

		CacheManager cacheManager = configuration.getCacheManager();
		TwitterStatus cachedStatus = cacheManager.getCachedStatus(status.getId());
		if (cachedStatus == null) {
			TwitterStatus twitterStatus = new TwitterStatus(status);
			cachedStatus = cacheManager.cacheStatus(twitterStatus);
			/* // update user cache
			cacheManager.cacheUser(twitterStatus.getUser());
			if (twitterStatus.isRetweet()) {
				cacheManager.cacheUser(twitterStatus.getRetweetedStatus().getUser());
			}*/
		} else {
			cachedStatus.update(status);
		}
		return cachedStatus;
	}

	private static JSONObject getJsonObject(Status originalStatus)
			throws AssertionError {
		String json = TwitterObjectFactory.getRawJSON(originalStatus);
		JSONObject jsonObject = null;
		if (json != null) {
			try {
				jsonObject = new JSONObject(json);
			} catch (JSONException e) {
				logger.error("Cannot parse json", e); // already parsed by Status*Impl
				throw new AssertionError(e);
			}
		}
		return jsonObject;
	}

	private static JSONObject getRetweetJSONObject(JSONObject jsonObject) {
		try {
			return jsonObject == null ? null : jsonObject.getJSONObject("retweeted_status");
		} catch (JSONException e) {
			logger.error("Although twitter.isRetweet() is true, json don't have retweet", e);
			return null;
		}
	}

	private final long[] contributors;
	private final Date createdAt;
	private final long id;
	private final long inReplyToStatusId;
	private final long inReplyToUserId;
	private final boolean isTruncated;
	private final TwitterStatus retweetedStatus;
	private final TwitterUser user;
	private/*final*/ GeoLocation geoLocation;
	private/*final*/ HashtagEntity[] hashtagEntities;
	private/*final*/ String inReplyToScreenName;
	private/*final*/ MediaEntity[] mediaEntities;
	private/*final*/ Place place;
	private/*final*/ String source;
	private/*final*/ String text;
	private/*final*/ URLEntity[] urlEntities;
	private/*final*/ UserMentionEntity[] userMentionEntities;
	private volatile boolean favorited;
	private volatile int retweetCount;
	private volatile boolean retweetedByMe;
	private boolean possiblySensitive;
	private long currentUserRetweetId;
	private boolean retweeted;
	private int favoriteCount;
	private String lang;
	private SymbolEntity[] symbolEntities;
	private Scopes scopes;
	private MediaEntity[] extendedMediaEntities;
	private String[] withheldInCountries;

	/**
	 * インスタンスを生成する。
	 *
	 * @param originalStatus オリジナルステータス
	 */
	public TwitterStatus(Status originalStatus) {
		this(originalStatus, getJsonObject(originalStatus), originalStatus.getUser());
	}

	/**
	 * インスタンスを生成する。
	 *
	 * @param originalStatus オリジナルステータス
	 * @param jsonObject     生JSON。取得できなかった場合にはnull。
	 */
	public TwitterStatus(Status originalStatus, JSONObject jsonObject) {
		this(originalStatus, jsonObject, originalStatus.getUser());
	}

	/**
	 * インスタンスを生成する。
	 *
	 * @param originalStatus オリジナルステータス
	 * @param jsonObject     生JSON。取得できなかった場合にはnull。
	 * @param user           getUser()
	 */
	public TwitterStatus(Status originalStatus, JSONObject jsonObject, User user) {
		favorited = originalStatus.isFavorited();
		retweetedByMe = originalStatus.isRetweetedByMe();
		urlEntities = originalStatus.getURLEntities();
		hashtagEntities = originalStatus.getHashtagEntities();
		mediaEntities = originalStatus.getMediaEntities();
		userMentionEntities = originalStatus.getUserMentionEntities();
		text = originalStatus.getText();
		createdAt = Utility.snowflakeIdToMilliSec(originalStatus.getCreatedAt(), originalStatus.getId());
		id = originalStatus.getId();
		source = originalStatus.getSource();
		isTruncated = originalStatus.isTruncated();
		inReplyToStatusId = originalStatus.getInReplyToStatusId();
		inReplyToUserId = originalStatus.getInReplyToUserId();
		favorited = originalStatus.isFavorited();
		inReplyToScreenName = originalStatus.getInReplyToScreenName();
		geoLocation = originalStatus.getGeoLocation();
		place = originalStatus.getPlace();
		retweetCount = originalStatus.getRetweetCount();
		retweetedByMe = originalStatus.isRetweetedByMe();
		contributors = originalStatus.getContributors();
		this.user = TwitterUser.getInstance(user);
		possiblySensitive = originalStatus.isPossiblySensitive();
		currentUserRetweetId = originalStatus.getCurrentUserRetweetId();
		retweeted = originalStatus.isRetweeted();
		favoriteCount = originalStatus.getFavoriteCount();
		lang = originalStatus.getLang();
		symbolEntities = originalStatus.getSymbolEntities();
		scopes = originalStatus.getScopes();
		extendedMediaEntities = originalStatus.getExtendedMediaEntities();
		withheldInCountries = originalStatus.getWithheldInCountries();

		Status retweetedStatus = originalStatus.getRetweetedStatus();
		if (!(originalStatus instanceof TwitterStatus)) {
			if (!(retweetedStatus == null || retweetedStatus instanceof TwitterStatus)) {
				CacheManager cacheManager = configuration.getCacheManager();
				TwitterStatus cachedStatus = cacheManager.getCachedStatus(retweetedStatus.getId());
				if (cachedStatus == null) {
					TwitterStatus status = new TwitterStatus(retweetedStatus, getRetweetJSONObject(jsonObject));
					cachedStatus = cacheManager.cacheStatus(status);
				}
				retweetedStatus = cachedStatus;
			}
		}
		this.retweetedStatus = (TwitterStatus) retweetedStatus;
	}

	@Override
	public int compareTo(@Nonnull Status b) {
		long thisId = id;
		long thatId = b.getId();
		if (thisId < thatId) {
			return -1;
		} else if (thisId > thatId) {
			return 1;
		} else {
			return 0;
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		} else if (this == obj) {
			return true;
		} else if (!(obj instanceof Status)) {
			return false;
		}
		return ((Status) obj).getId() == id;
	}

	/**
	 * -1を返します ((このクラスのインスタンスはキャッシュされるため一時的なデータは保存しない))
	 *
	 * @return -1
	 */
	@Override
	public int getAccessLevel() {
		return -1;
	}

	@Override
	@SuppressFBWarnings("EI_EXPOSE_REP")
	public long[] getContributors() {
		return contributors;
	}

	@Override
	public Date getCreatedAt() {
		return (Date) createdAt.clone();
	}

	@Override
	public long getCurrentUserRetweetId() {
		return currentUserRetweetId;
	}

	@Override
	@SuppressFBWarnings("EI_EXPOSE_REP")
	public MediaEntity[] getExtendedMediaEntities() {
		return extendedMediaEntities;
	}

	@Override
	public int getFavoriteCount() {
		return favoriteCount;
	}

	@Override
	public GeoLocation getGeoLocation() {
		return geoLocation;
	}

	@Override
	@SuppressFBWarnings("EI_EXPOSE_REP")
	public HashtagEntity[] getHashtagEntities() {
		return hashtagEntities;
	}

	@Override
	public long getId() {
		return id;
	}

	@Override
	public String getInReplyToScreenName() {
		return inReplyToScreenName;
	}

	@Override
	public long getInReplyToStatusId() {
		return inReplyToStatusId;
	}

	@Override
	public long getInReplyToUserId() {
		return inReplyToUserId;
	}

	@Override
	public String getLang() {
		return lang;
	}

	@Override
	@SuppressFBWarnings("EI_EXPOSE_REP")
	public MediaEntity[] getMediaEntities() {
		return mediaEntities;
	}

	@Override
	public Place getPlace() {
		return place;
	}

	/**
	 * nullを返します。(このクラスのインスタンスはキャッシュされるため一時的なデータは保存しない)
	 *
	 * @return null
	 */
	@Override
	public RateLimitStatus getRateLimitStatus() {
		return null;
	}

	@Override
	public int getRetweetCount() {
		return retweetCount;
	}

	@Override
	public TwitterStatus getRetweetedStatus() {
		return retweetedStatus;
	}

	@Override
	public Scopes getScopes() {
		return scopes;
	}

	@Override
	public String getSource() {
		return source;
	}

	@Override
	@SuppressFBWarnings("EI_EXPOSE_REP")
	public SymbolEntity[] getSymbolEntities() {
		return symbolEntities;
	}

	@Override
	public String getText() {
		return text;
	}

	@Override
	@SuppressFBWarnings("EI_EXPOSE_REP")
	public URLEntity[] getURLEntities() {
		return urlEntities;
	}

	@Override
	public TwitterUser getUser() {
		return user;
	}

	@Override
	@SuppressFBWarnings("EI_EXPOSE_REP")
	public UserMentionEntity[] getUserMentionEntities() {
		return userMentionEntities;
	}

	@Override
	@SuppressFBWarnings("EI_EXPOSE_REP")
	public String[] getWithheldInCountries() {
		return withheldInCountries;
	}

	@Override
	public int hashCode() {
		return (int) id;
	}

	@Override
	public boolean isFavorited() {
		return favorited;
	}

	@Override
	public boolean isPossiblySensitive() {
		return possiblySensitive;
	}

	@Override
	public boolean isRetweet() {
		return retweetedStatus != null;
	}

	@Override
	public boolean isRetweeted() {
		return retweeted;
	}

	@Override
	public boolean isRetweetedByMe() {
		return retweetedByMe;
	}

	@Override
	public boolean isTruncated() {
		return isTruncated;
	}

	/**
	 * ふぁぼられたかを設定する
	 *
	 * @param favorited ふぁぼられたかどうか
	 */
	public void setFavorited(boolean favorited) {
		this.favorited = favorited;
	}


	/**
	 * ユーザーがリツイートしたかどうかを設定する
	 *
	 * @param retweetedByMe リツイートされたかどうか
	 */
	public void setRetweetedByMe(boolean retweetedByMe) {
		this.retweetedByMe = retweetedByMe;
	}

	/**
	 * update this by status
	 *
	 * @param status new status object
	 * @return this
	 */
	public synchronized TwitterStatus update(Status status) {
		favorited = status.isFavorited();
		retweetedByMe = status.isRetweetedByMe();
		urlEntities = status.getURLEntities();
		hashtagEntities = status.getHashtagEntities();
		mediaEntities = status.getMediaEntities();
		userMentionEntities = status.getUserMentionEntities();
		text = status.getText();
		//createdAt = status.getCreatedAt();
		//id = status.getId();
		source = status.getSource();
		//isTruncated = status.isTruncated();
		//inReplyToStatusId = status.getInReplyToStatusId();
		//inReplyToUserId = status.getInReplyToUserId();
		favorited = status.isFavorited();
		inReplyToScreenName = status.getInReplyToScreenName();
		geoLocation = status.getGeoLocation();
		place = status.getPlace();
		retweetCount = status.getRetweetCount();
		retweetedByMe = status.isRetweetedByMe();
		//contributors = status.getContributors();
		//user = getInstance(status.getUser());
		possiblySensitive = status.isPossiblySensitive();
		currentUserRetweetId = status.getCurrentUserRetweetId();
		return this;
	}
}

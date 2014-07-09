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

package jp.syuriken.snsw.twclient.twitter;

import java.util.Date;

import javax.annotation.Nonnull;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jp.syuriken.snsw.twclient.CacheManager;
import jp.syuriken.snsw.twclient.ClientConfiguration;
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
public class TwitterStatus implements Status, TwitterExtendedObject {
	private static final long serialVersionUID = -188757917578787367L;
	private static final Logger logger = LoggerFactory.getLogger(TwitterStatus.class);
	public static final ClientConfiguration configuration = ClientConfiguration.getInstance();

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
	private final User user;
	private final String json;
	private/*final*/ GeoLocation geoLocation;
	private/*final*/ HashtagEntity[] hashtagEntities;
	private/*final*/ String inReplyToScreenName;
	private boolean loadedInitialization;
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
	private transient Scopes scopes;

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
		json = jsonObject == null ? null : jsonObject.toString();
		favorited = originalStatus.isFavorited();
		retweetedByMe = originalStatus.isRetweetedByMe();
		urlEntities = originalStatus.getURLEntities();
		hashtagEntities = originalStatus.getHashtagEntities();
		mediaEntities = originalStatus.getMediaEntities();
		userMentionEntities = originalStatus.getUserMentionEntities();
		text = originalStatus.getText();
		createdAt = originalStatus.getCreatedAt();
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

		Status retweetedStatus = originalStatus.getRetweetedStatus();
		if (!(originalStatus instanceof TwitterStatus)) {
			if (!(retweetedStatus == null || retweetedStatus instanceof TwitterStatus)) {
				CacheManager cacheManager = configuration.getCacheManager();
				TwitterStatus cachedStatus = cacheManager.getCachedStatus(retweetedStatus.getId());
				if (cachedStatus == null) {
					TwitterStatus status = new TwitterStatus(retweetedStatus, getRetweetJSONObject(jsonObject));
					cachedStatus = cacheManager.getCachedStatus(status);
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

	@SuppressFBWarnings("EI_EXPOSE_REP")
	@Override
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
	public int getFavoriteCount() {
		return favoriteCount;
	}

	@Override
	public GeoLocation getGeoLocation() {
		return geoLocation;
	}

	@SuppressFBWarnings("EI_EXPOSE_REP")
	@Override
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
	public String getJson() {
		return json;
	}

	@Override
	public String getLang() {
		return lang;
	}

	@SuppressFBWarnings("EI_EXPOSE_REP")
	@Override
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

	@SuppressFBWarnings("EI_EXPOSE_REP")
	@Override
	public URLEntity[] getURLEntities() {
		return urlEntities;
	}

	@Override
	public User getUser() {
		return user;
	}

	@SuppressFBWarnings("EI_EXPOSE_REP")
	@Override
	public UserMentionEntity[] getUserMentionEntities() {
		return userMentionEntities;
	}

	@Override
	public int hashCode() {
		return (int) id;
	}

	@Override
	public boolean isFavorited() {
		return favorited;
	}

	/**
	 * このステータスが起動時に読み込まれたものかどうかを調べる
	 *
	 * @return 起動時に読み込まれたならtrue
	 */
	public boolean isLoadedInitialization() {
		return loadedInitialization;
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
	 * このステータスは起動時に読み込まれたものです
	 *
	 * @param loadedInitialization 起動時に読み込まれたならtrue
	 */
	public void setLoadedInitialization(boolean loadedInitialization) {
		this.loadedInitialization = loadedInitialization;
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

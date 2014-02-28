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
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 *  INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 *  IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 *  DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package jp.syuriken.snsw.twclient;

import java.util.Date;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.GeoLocation;
import twitter4j.HashtagEntity;
import twitter4j.MediaEntity;
import twitter4j.Place;
import twitter4j.RateLimitStatus;
import twitter4j.Status;
import twitter4j.SymbolEntity;
import twitter4j.URLEntity;
import twitter4j.User;
import twitter4j.UserMentionEntity;
import twitter4j.internal.org.json.JSONException;
import twitter4j.internal.org.json.JSONObject;
import twitter4j.json.DataObjectFactory;

/**
 * RT/favしたかどうかを格納できるStatus拡張型
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class TwitterStatus implements Status, TwitterExtendedObject {

	private static final long serialVersionUID = -188757917578787367L;
	private static final Logger logger = LoggerFactory.getLogger(TwitterStatus.class);

	private static JSONObject getJsonObject(Status originalStatus)
			throws AssertionError {
		String json = DataObjectFactory.getRawJSON(originalStatus);
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

	private final long[] contributors;
	private final Date createdAt;
	private final long id;
	private final long inReplyToStatusId;
	private final long inReplyToUserId;
	private final boolean isTruncated;
	private final TwitterStatus retweetedStatus;
	private final User user;
	//	private final String json;
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
	private String isoLanguageCode;
	private SymbolEntity[] symbolEntities;


	/**
	 * インスタンスを生成する。
	 *
	 * @param originalStatus オリジナルステータス
	 */
	public TwitterStatus(Status originalStatus) {
		this(originalStatus, getJsonObject(originalStatus));
	}

	/**
	 * インスタンスを生成する。
	 *
	 * @param originalStatus オリジナルステータス
	 * @param jsonObject     生JSON。取得できなかった場合にはnull。
	 */
	public TwitterStatus(Status originalStatus, JSONObject jsonObject) {
		//json = jsonObject == null ? null : jsonObject.toString();
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
		user = getCachedUser(originalStatus.getUser());
		possiblySensitive = originalStatus.isPossiblySensitive();
		currentUserRetweetId = originalStatus.getCurrentUserRetweetId();
		retweeted = originalStatus.isRetweeted();
		favoriteCount = originalStatus.getFavoriteCount();
		isoLanguageCode = originalStatus.getIsoLanguageCode();
		symbolEntities = originalStatus.getSymbolEntities();

		Status retweetedStatus = originalStatus.getRetweetedStatus();
		if (originalStatus instanceof TwitterStatus) {
			// do nothing
		/*} else if (jsonObject == null) {
			retweetedStatus = new TwitterStatus(retweetedStatus);*/
		} else {
			if (retweetedStatus != null && retweetedStatus instanceof TwitterStatus == false) {
				CacheManager cacheManager = ClientConfiguration.getInstance().getCacheManager();
				Status cachedStatus = cacheManager.getCachedStatus(retweetedStatus.getId());
				if (cachedStatus == null || cachedStatus instanceof TwitterStatus == false) {
					Status status = new TwitterStatus(retweetedStatus);
					cachedStatus = cacheManager.cacheStatusIfAbsent(status);
					if (cachedStatus == null) {
						cachedStatus = status;
					}
				}
				retweetedStatus = cachedStatus;
			}
		}
		this.retweetedStatus = (TwitterStatus) retweetedStatus;
	}

	@Override
	public int compareTo(Status b) {
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
		} else if (obj instanceof Status == false) {
			return false;
		}
		return ((Status) obj).getId() == id;
	}

	/** -1を返します ((このクラスのインスタンスはキャッシュされるため一時的なデータは保存しない)) */
	@Override
	public int getAccessLevel() {
		return -1;
	}

	private User getCachedUser(User user) {
		if (user instanceof TwitterUser) {
			return user;
		}

		CacheManager cacheManager = ClientConfiguration.getInstance().getCacheManager();
		User cachedUser = cacheManager.getCachedUser(user.getId());
		if (cachedUser == null) {
			TwitterUser twitterUser = new TwitterUser(user);
			cachedUser = cacheManager.cacheUserIfAbsent(twitterUser);
			if (cachedUser == null) {
				cachedUser = twitterUser;
			}
		}
		return cachedUser;
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

	/*
	@Override
	public String getJson() {
		return json;
	}
	*/

	@Override
	public long getInReplyToUserId() {
		return inReplyToUserId;
	}

	@Override
	public String getIsoLanguageCode() {
		return isoLanguageCode;
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

	/** nullを返します。(このクラスのインスタンスはキャッシュされるため一時的なデータは保存しない) */
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
	public String getSource() {
		return source;
	}

	@Override
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

	public void update(Status status) {
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
		//user = getCachedUser(status.getUser());
		possiblySensitive = status.isPossiblySensitive();
		currentUserRetweetId = status.getCurrentUserRetweetId();
	}
}

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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

import javax.annotation.Nonnull;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jp.syuriken.snsw.twclient.CacheManager;
import jp.syuriken.snsw.twclient.ClientConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.RateLimitStatus;
import twitter4j.Status;
import twitter4j.URLEntity;
import twitter4j.User;
import twitter4j.internal.org.json.JSONException;
import twitter4j.internal.org.json.JSONObject;
import twitter4j.json.DataObjectFactory;

/**
 * プロフィールの更新ができるUser拡張型
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class TwitterUser implements User, TwitterExtendedObject {

	private static final long serialVersionUID = 1893110786616307437L;
	private static final Logger logger = LoggerFactory.getLogger(TwitterUser.class);

	private static JSONObject getJsonObject(User originalUser) throws AssertionError {
		String json = DataObjectFactory.getRawJSON(originalUser);
		JSONObject jsonObject = null;
		if (json != null) {
			try {
				jsonObject = new JSONObject(json);
			} catch (JSONException e) {
				logger.error("Cannot parse json", e); // already parsed by User*Impl
				throw new AssertionError(e);
			}
		}
		return jsonObject;
	}

	private final Date createdAt;
	private final long id;
	private final boolean isContributorsEnabled;
	private final boolean isFollowRequestSent;
	private final boolean isGeoEnabled;
	private final boolean isVerified;
	private final String lang;
	private final String profileBackgroundColor;
	private final boolean profileBackgroundTiled;
	private final String profileLinkColor;
	private final String profileSidebarBorderColor;
	private final String profileSidebarFillColor;
	private final String profileTextColor;
	private final boolean showAllInlineMedia;
	private final String timeZone;
	private final boolean translator;
	private final int utcOffset;
	private final String json;
	private boolean profileUseBackgroundImage;
	private String description;
	private int favouritesCount;
	private int followersCount;
	private int friendsCount;
	private boolean isProtected;
	private int listedCount;
	private String location;
	private String name;
	private String screenName;
	private Status status;
	private int statusesCount;
	private String url;
	private String profileBackgroundImageUrl;
	private String profileBackgroundImageUrlHttps;
	private String profileImageUrl;
	private String profileImageUrlHttps;
	private String profileBannerImageUrl;
	private URLEntity[] descriptionURLEntities;
	private URLEntity urlEntity;


	/**
	 * インスタンスを生成する。
	 *
	 * @param originalUser オリジナルユーザー
	 */
	public TwitterUser(User originalUser) {
		this(originalUser, getJsonObject(originalUser));
	}

	/**
	 * インスタンスを生成する。
	 *
	 * @param originalUser オリジナルユーザー
	 * @param jsonObject   生JSON。取得できなかった場合にはnull。
	 */
	public TwitterUser(User originalUser, JSONObject jsonObject) {
		createdAt = originalUser.getCreatedAt();
		id = originalUser.getId();
		isContributorsEnabled = originalUser.isContributorsEnabled();
		isFollowRequestSent = originalUser.isFollowRequestSent();
		isGeoEnabled = originalUser.isGeoEnabled();
		isVerified = originalUser.isVerified();
		lang = originalUser.getLang();
		url = originalUser.getURL();
		profileBackgroundColor = originalUser.getProfileBackgroundColor();
		profileBackgroundImageUrl = originalUser.getProfileBackgroundImageURL();
		profileBackgroundImageUrlHttps = originalUser.getProfileBackgroundImageUrlHttps();
		profileBackgroundTiled = originalUser.isProfileBackgroundTiled();
		profileImageUrl = originalUser.getProfileImageURL();
		profileImageUrlHttps = originalUser.getProfileImageURLHttps();
		profileLinkColor = originalUser.getProfileLinkColor();
		profileSidebarBorderColor = originalUser.getProfileSidebarBorderColor();
		profileSidebarFillColor = originalUser.getProfileSidebarFillColor();
		profileTextColor = originalUser.getProfileTextColor();
		profileUseBackgroundImage = originalUser.isProfileUseBackgroundImage();
		showAllInlineMedia = originalUser.isShowAllInlineMedia();
		timeZone = originalUser.getTimeZone();
		translator = originalUser.isTranslator();
		utcOffset = originalUser.getUtcOffset();
		description = originalUser.getDescription();
		favouritesCount = originalUser.getFavouritesCount();
		followersCount = originalUser.getFollowersCount();
		friendsCount = originalUser.getFriendsCount();
		isProtected = originalUser.isProtected();
		listedCount = originalUser.getListedCount();
		location = originalUser.getLocation();
		name = originalUser.getName();
		screenName = originalUser.getScreenName();
		statusesCount = originalUser.getStatusesCount();
		profileBannerImageUrl = originalUser.getProfileBannerURL();
		descriptionURLEntities = originalUser.getDescriptionURLEntities();
		urlEntity = originalUser.getURLEntity();

		json = jsonObject == null ? null : jsonObject.toString();

		Status status = originalUser.getStatus();
		JSONObject statusJsonObject;
		try {
			statusJsonObject = jsonObject == null ? null :
					(jsonObject.isNull("status") ? null : jsonObject.getJSONObject("status"));
		} catch (JSONException e) {
			logger.error("Cannot parse json", e);
			throw new RuntimeException(e);
		}
		if (!(status == null || status instanceof TwitterStatus)) {
			ClientConfiguration configuration = ClientConfiguration.getInstance();
			CacheManager cacheManager = configuration.getCacheManager();
			TwitterStatus cachedStatus = cacheManager.getCachedStatus(status.getId());
			if (cachedStatus == null) {
				TwitterStatus twitterStatus = new TwitterStatus(status, statusJsonObject, this);
				status = cacheManager.getCachedStatus(twitterStatus);
			}
		}
		this.status = status;
	}

	@Override
	public int compareTo(@Nonnull User b) {
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
		} else if (!(obj instanceof User)) {
			return false;
		}
		return ((User) obj).getId() == id;
	}

	/** -1を返す (このクラスのインスタンスはキャッシュされるため、一時的なデータは保存しない) */
	@Override
	public int getAccessLevel() {
		return -1;
	}

	@Override
	public String getBiggerProfileImageURL() {
		return toResizedURL(profileImageUrl, "_bigger");
	}

	@Override
	public String getBiggerProfileImageURLHttps() {
		return toResizedURL(profileImageUrlHttps, "_bigger");
	}

	@Override
	public Date getCreatedAt() {
		return (Date) createdAt.clone();
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	@SuppressFBWarnings("EI_EXPOSE_REP")
	public URLEntity[] getDescriptionURLEntities() {
		return descriptionURLEntities;
	}

	@Override
	public int getFavouritesCount() {
		return favouritesCount;
	}

	@Override
	public int getFollowersCount() {
		return followersCount;
	}

	@Override
	public int getFriendsCount() {
		return friendsCount;
	}

	@Override
	public long getId() {
		return id;
	}

	//@Override
	public String getJson() {
		return json;
	}

	@Override
	public String getLang() {
		return lang;
	}

	@Override
	public int getListedCount() {
		return listedCount;
	}

	@Override
	public String getLocation() {
		return location;
	}

	@Override
	public String getMiniProfileImageURL() {
		return toResizedURL(profileImageUrl, "_mini");
	}

	@Override
	public String getMiniProfileImageURLHttps() {
		return toResizedURL(profileImageUrlHttps, "_mini");
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getOriginalProfileImageURL() {
		return toResizedURL(profileImageUrl, "");
	}

	@Override
	public String getOriginalProfileImageURLHttps() {
		return toResizedURL(profileImageUrlHttps, "");
	}

	@Override
	public String getProfileBackgroundColor() {
		return profileBackgroundColor;
	}

	@SuppressFBWarnings("NM_CONFUSING")
	@Override
	public String getProfileBackgroundImageURL() {
		return profileBackgroundImageUrl;
	}

	@SuppressFBWarnings("NM_CONFUSING")
	@SuppressWarnings("deprecation")
	@Deprecated
	@Override
	public String getProfileBackgroundImageUrl() {
		return profileBackgroundImageUrl;
	}

	@Override
	public String getProfileBackgroundImageUrlHttps() {
		return profileBackgroundImageUrlHttps;
	}

	@Override
	public String getProfileBannerIPadRetinaURL() {
		return profileBannerImageUrl != null ? profileBannerImageUrl + "/ipad_retina" : null;
	}

	@Override
	public String getProfileBannerIPadURL() {
		return profileBannerImageUrl != null ? profileBannerImageUrl + "/ipad" : null;
	}

	@Override
	public String getProfileBannerMobileRetinaURL() {
		return profileBannerImageUrl != null ? profileBannerImageUrl + "/ipad_retina" : null;
	}

	@Override
	public String getProfileBannerMobileURL() {
		return profileBannerImageUrl != null ? profileBannerImageUrl + "/mobile" : null;
	}

	@Override
	public String getProfileBannerRetinaURL() {
		return profileBannerImageUrl != null ? profileBannerImageUrl + "/web_retina" : null;
	}

	@Override
	public String getProfileBannerURL() {
		return profileBannerImageUrl != null ? profileBannerImageUrl + "/web" : null;
	}

	@Override
	public String getProfileImageURL() {
		return profileImageUrl;
	}

	@Override
	public String getProfileImageURLHttps() {
		return profileImageUrlHttps;
	}

	@SuppressWarnings("deprecation")
	@Deprecated
	@Override
	public URL getProfileImageUrlHttps() {
		try {
			return new URL(profileImageUrlHttps);
		} catch (MalformedURLException e) {
			return null;
		}
	}

	@Override
	public String getProfileLinkColor() {
		return profileLinkColor;
	}

	@Override
	public String getProfileSidebarBorderColor() {
		return profileSidebarBorderColor;
	}

	@Override
	public String getProfileSidebarFillColor() {
		return profileSidebarFillColor;
	}

	@Override
	public String getProfileTextColor() {
		return profileTextColor;
	}

	/** nullを返す (このクラスのインスタンスはキャッシュされるため一時的なデータは保存しない) */
	@Override
	public RateLimitStatus getRateLimitStatus() {
		return null;
	}

	@Override
	public String getScreenName() {
		return screenName;
	}

	@Override
	public Status getStatus() {
		return status;
	}

	@Override
	public int getStatusesCount() {
		return statusesCount;
	}

	@Override
	public String getTimeZone() {
		return timeZone;
	}

	@Override
	public String getURL() {
		return url;
	}

	@Override
	public URLEntity getURLEntity() {
		return urlEntity;
	}

	@Override
	public int getUtcOffset() {
		return utcOffset;
	}

	@Override
	public int hashCode() {
		return (int) id;
	}

	@Override
	public boolean isContributorsEnabled() {
		return isContributorsEnabled;
	}

	@Override
	public boolean isFollowRequestSent() {
		return isFollowRequestSent;
	}

	@Override
	public boolean isGeoEnabled() {
		return isGeoEnabled;
	}

	@Override
	public boolean isProfileBackgroundTiled() {
		return profileBackgroundTiled;
	}

	@Override
	public boolean isProfileUseBackgroundImage() {
		return profileUseBackgroundImage;
	}

	@Override
	public boolean isProtected() {
		return isProtected;
	}

	@Override
	public boolean isShowAllInlineMedia() {
		return showAllInlineMedia;
	}

	@Override
	public boolean isTranslator() {
		return translator;
	}

	@Override
	public boolean isVerified() {
		return isVerified;
	}

	/**
	 * Original Code: twitter4j.internal.json.UserJSONImpl (v3.0.2)
	 *
	 * @param originalURL オリジナルURL
	 * @param sizeSuffix  サイズ指定子
	 * @return URL
	 */
	private String toResizedURL(String originalURL, String sizeSuffix) {
		if (null != originalURL) {
			int index = originalURL.lastIndexOf("_");
			int suffixIndex = originalURL.lastIndexOf(".");
			return originalURL.substring(0, index) + sizeSuffix + originalURL.substring(suffixIndex);
		}
		return null;
	}

	/**
	 * このユーザーのプロフィールを指定したユーザーのプロフィールでアップデートする
	 *
	 * @param user 新しい情報が含まれたユーザー
	 * @throws IllegalArgumentException このユーザーのIDと指定したユーザーのIDが一致しない
	 */
	public TwitterUser updateUser(User user) throws IllegalArgumentException {
		if (id != user.getId()) {
			throw new IllegalArgumentException("UserIDが一致しません");
		}

		description = user.getDescription();
		favouritesCount = user.getFavouritesCount();
		followersCount = user.getFollowersCount();
		friendsCount = user.getFriendsCount();
		listedCount = user.getListedCount();
		location = user.getLocation();
		name = user.getName();
		profileBackgroundImageUrl = user.getProfileBackgroundImageURL();
		profileBackgroundImageUrlHttps = user.getProfileBackgroundImageUrlHttps();
		profileImageUrl = user.getProfileImageURL();
		profileImageUrlHttps = user.getProfileImageURLHttps();
		isProtected = user.isProtected();
		screenName = user.getScreenName();
		status = user.getStatus();
		statusesCount = user.getStatusesCount();
		url = user.getURL();
		return this;
	}
}

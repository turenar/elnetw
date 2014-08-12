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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.JSONException;
import twitter4j.JSONObject;
import twitter4j.RateLimitStatus;
import twitter4j.Status;
import twitter4j.TwitterObjectFactory;
import twitter4j.URLEntity;
import twitter4j.User;

/**
 * プロフィールの更新ができるUser拡張型
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class TwitterUser implements User, TwitterExtendedObject {

	private static final long serialVersionUID = 1893110786616307437L;
	private static final Logger logger = LoggerFactory.getLogger(TwitterUser.class);

	public static TwitterUser getInstance(User user) {
		if (user instanceof TwitterUser) {
			return (TwitterUser) user;
		}

		CacheManager cacheManager = TwitterStatus.configuration.getCacheManager();
		TwitterUser cachedUser = cacheManager.getCachedUser(user.getId());
		if (cachedUser == null) {
			TwitterUser twitterUser = new TwitterUser(user);
			cachedUser = cacheManager.cacheUserIfAbsent(twitterUser);
			if (cachedUser == null) {
				cachedUser = twitterUser;
			}
		}
		return cachedUser;
	}

	private static JSONObject getJsonObject(User originalUser) throws AssertionError {
		String json = TwitterObjectFactory.getRawJSON(originalUser);
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
	private int statusesCount;
	private String url;
	private String profileBackgroundImageUrl;
	private String profileBackgroundImageUrlHttps;
	private String profileImageUrl;
	private String profileImageUrlHttps;
	private String profileBannerImageUrl;
	private URLEntity[] descriptionURLEntities;
	private URLEntity urlEntity;
	private boolean defaultProfile;
	private boolean defaultProfileImage;

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
		String profileBannerURL = originalUser.getProfileBannerURL(); // delete "/web"
		profileBannerImageUrl = profileBannerURL == null ? null : profileBannerURL.substring(0, profileBannerURL.length() - 4);
		descriptionURLEntities = originalUser.getDescriptionURLEntities();
		urlEntity = originalUser.getURLEntity();
		defaultProfile = originalUser.isDefaultProfile();
		defaultProfileImage = originalUser.isDefaultProfileImage();

		json = jsonObject == null ? null : jsonObject.toString();
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

	/**
	 * -1を返す (このクラスのインスタンスはキャッシュされるため、一時的なデータは保存しない)
	 *
	 * @return -1
	 */
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

	/**
	 * get json
	 *
	 * @return json string
	 */
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

	@Override
	public String getProfileBackgroundImageURL() {
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

	/**
	 * get large profile banner url
	 *
	 * @return url
	 */
	public String getProfileBannerLargeURL() {
		return profileBannerImageUrl != null ? profileBannerImageUrl + "/1500x500" : null;
	}

	/**
	 * get medium profile banner url
	 *
	 * @return url
	 */
	public String getProfileBannerMediumURL() {
		return profileBannerImageUrl != null ? profileBannerImageUrl + "/600x200" : null;
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

	/**
	 * get small profile banner url
	 *
	 * @return url
	 */
	public String getProfileBannerSmallURL() {
		return profileBannerImageUrl != null ? profileBannerImageUrl + "/300x100" : null;
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

	/**
	 * nullを返す (このクラスのインスタンスはキャッシュされるため一時的なデータは保存しない)
	 *
	 * @return null
	 */
	@Override
	public RateLimitStatus getRateLimitStatus() {
		return null;
	}

	@Override
	public String getScreenName() {
		return screenName;
	}

	/**
	 * this class returns always null. This is workaround Twitter#getUser().getStatus().getUser()==null
	 *
	 * @return null
	 */
	@Override
	public Status getStatus() {
		return null;
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
	public boolean isDefaultProfile() {
		return defaultProfile;
	}

	@Override
	public boolean isDefaultProfileImage() {
		return defaultProfileImage;
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

	@Override
	public String toString() {
		return "TwitterUser{"
				+ "id=" + id
				+ ", name='" + name + '\''
				+ ", screenName='" + screenName + '\''
				+ ", location='" + location + '\''
				+ ", description='" + description + '\''
				+ ", isContributorsEnabled=" + isContributorsEnabled
				+ ", profileImageUrl='" + profileImageUrl + '\''
				+ ", profileImageUrlHttps='" + profileImageUrlHttps + '\''
				+ ", url='" + url + '\''
				+ ", isProtected=" + isProtected
				+ ", followersCount=" + followersCount
				+ ", profileBackgroundColor='" + profileBackgroundColor + '\''
				+ ", profileTextColor='" + profileTextColor + '\''
				+ ", profileLinkColor='" + profileLinkColor + '\''
				+ ", profileSidebarFillColor='" + profileSidebarFillColor + '\''
				+ ", profileSidebarBorderColor='" + profileSidebarBorderColor + '\''
				+ ", profileUseBackgroundImage=" + profileUseBackgroundImage
				+ ", showAllInlineMedia=" + showAllInlineMedia
				+ ", friendsCount=" + friendsCount
				+ ", createdAt=" + createdAt
				+ ", favouritesCount=" + favouritesCount
				+ ", utcOffset=" + utcOffset
				+ ", timeZone='" + timeZone + '\''
				+ ", profileBackgroundImageUrl='" + profileBackgroundImageUrl + '\''
				+ ", profileBackgroundImageUrlHttps='" + profileBackgroundImageUrlHttps + '\''
				+ ", profileBackgroundTiled=" + profileBackgroundTiled
				+ ", lang='" + lang + '\''
				+ ", statusesCount=" + statusesCount
				+ ", isGeoEnabled=" + isGeoEnabled
				+ ", isVerified=" + isVerified
				+ ", translator=" + translator
				+ ", listedCount=" + listedCount
				+ ", isFollowRequestSent=" + isFollowRequestSent
				+ '}';
	}

	/**
	 * このユーザーのプロフィールを指定したユーザーのプロフィールでアップデートする
	 *
	 * @param user 新しい情報が含まれたユーザー
	 * @return this instance
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
		statusesCount = user.getStatusesCount();
		url = user.getURL();
		return this;
	}
}

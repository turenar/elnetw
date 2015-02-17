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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jp.mydns.turenar.twclient.storage.DirEntry;
import twitter4j.RateLimitStatus;
import twitter4j.Status;
import twitter4j.URLEntity;
import twitter4j.User;

import static jp.mydns.turenar.twclient.twitter.URLEntityImpl.getEntitiesFromDirEntry;

/**
 * プロフィールの更新ができるUser拡張型
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class TwitterUserImpl extends TwitterUser {

	private String[] withheldInCountries;

	@Override
	public String[] getWithheldInCountries() {
		return new String[0];
	}

	private static final long serialVersionUID = -1546636227286362016L;

	private final long createdAt;
	private final long id;
	private boolean isContributorsEnabled;
	private boolean isFollowRequestSent;
	private boolean isGeoEnabled;
	private boolean isVerified;
	private String lang;
	private String profileBackgroundColor;
	private boolean profileBackgroundTiled;
	private String profileLinkColor;
	private String profileSidebarBorderColor;
	private String profileSidebarFillColor;
	private String profileTextColor;
	private boolean showAllInlineMedia;
	private String timeZone;
	private boolean translator;
	private int utcOffset;
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
	private URLEntity[] descriptionURLEntities;
	private URLEntity urlEntity;
	private boolean defaultProfile;
	private boolean defaultProfileImage;
	private long timestamp;
	protected String profileBannerImageUrl;

	/**
	 * instance
	 *
	 * @param dirEntry data dir entry
	 */
	public TwitterUserImpl(DirEntry dirEntry) {
		isContributorsEnabled = dirEntry.readBool("contributors_enabled");
		createdAt = dirEntry.readLong("created_at");
		defaultProfile = dirEntry.readBool("default_profile");
		defaultProfileImage = dirEntry.readBool("default_profile_image");
		description = dirEntry.readString("description");
		favouritesCount = dirEntry.readInt("favourites_count");
		isFollowRequestSent = dirEntry.readBool("follow_request_sent");
		followersCount = dirEntry.readInt("followers_count");
		friendsCount = dirEntry.readInt("friends_count");
		isGeoEnabled = dirEntry.readBool("geo_enabled");
		id = dirEntry.readLong("id");
		translator = dirEntry.readBool("is_translator");
		lang = dirEntry.readString("lang");
		listedCount = dirEntry.readInt("listed_count");
		location = dirEntry.readString("location");
		name = dirEntry.readString("name");
		profileBackgroundColor = dirEntry.readString("profile_background_color");
		profileBackgroundImageUrl = dirEntry.readString("profile_background_image_url");
		profileBackgroundImageUrlHttps = dirEntry.readString("profile_background_image_url_https");
		profileBackgroundTiled = dirEntry.readBool("profile_background_tile");
		profileBannerImageUrl = dirEntry.readString("profile_banner_url");
		profileImageUrl = dirEntry.readString("profile_image_url");
		profileImageUrlHttps = dirEntry.readString("profile_image_url_https");
		profileLinkColor = dirEntry.readString("profile_link_color");
		profileSidebarBorderColor = dirEntry.readString("profile_sidebar_border_color");
		profileSidebarFillColor = dirEntry.readString("profile_sidebar_fill_color");
		profileTextColor = dirEntry.readString("profile_text_color");
		profileUseBackgroundImage = dirEntry.readBool("profile_use_background_image");
		isProtected = dirEntry.readBool("protected");
		screenName = dirEntry.readString("screen_name");
		showAllInlineMedia = dirEntry.readBool("show_all_inline_media");
		statusesCount = dirEntry.readInt("statuses_count");
		timeZone = dirEntry.readString("time_zone");
		url = dirEntry.readString("url");
		utcOffset = dirEntry.readInt("utc_offset");
		isVerified = dirEntry.readBool("verified");
		DirEntry entities = dirEntry.getDirEntry("entities");
		urlEntity = new URLEntityImpl(entities.getDirEntry("url"));
		descriptionURLEntities = getEntitiesFromDirEntry(entities.getDirEntry("description"));
		withheldInCountries = dirEntry.readStringArray("withheld_in_countries");

		timestamp = dirEntry.exists("timestamp") ? dirEntry.readLong("timestamp") : System.currentTimeMillis();
	}

	/**
	 * インスタンスを生成する。
	 *
	 * @param originalUser オリジナルユーザー
	 */
	public TwitterUserImpl(User originalUser) {
		createdAt = originalUser.getCreatedAt().getTime();
		id = originalUser.getId();
		update(originalUser);
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
		return new Date(createdAt);
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
	@Override
	public String getProfileBannerLargeURL() {
		return profileBannerImageUrl != null ? profileBannerImageUrl + "/1500x500" : null;
	}

	/**
	 * get medium profile banner url
	 *
	 * @return url
	 */
	@Override
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

	@Override
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
	public long getTimestamp() {
		return timestamp;
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

	@Override
	public void update(User originalUser) {
		if (originalUser.getId() != id) {
			throw new IllegalArgumentException("illegal user id: " + originalUser.getId());
		}
		if (originalUser instanceof TwitterUser && timestamp >= ((TwitterUser) originalUser).getTimestamp()) {
			return;
		}
		timestamp = originalUser instanceof TwitterUserImpl
				? ((TwitterUserImpl) originalUser).getTimestamp() : System.currentTimeMillis();

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
		withheldInCountries = originalUser.getWithheldInCountries();
	}

	@Override
	public void write(DirEntry dirEntry) {
		dirEntry.writeBool("contributors_enabled", isContributorsEnabled)
				.writeLong("created_at", createdAt)
				.writeBool("default_profile", defaultProfile)
				.writeBool("default_profile_image", defaultProfileImage)
				.writeString("description", description)
				.writeInt("favourites_count", favouritesCount)
				.writeBool("follow_request_sent", isFollowRequestSent)
				.writeInt("followers_count", followersCount)
				.writeInt("friends_count", friendsCount)
				.writeBool("geo_enabled", isGeoEnabled)
				.writeLong("id", id)
				.writeBool("is_translator", translator)
				.writeString("lang", lang)
				.writeInt("listed_count", listedCount)
				.writeString("location", location)
				.writeString("name", name)
				.writeString("profile_background_color", profileBackgroundColor)
				.writeString("profile_background_image_url", profileBackgroundImageUrl)
				.writeString("profile_background_image_url_https", profileBackgroundImageUrlHttps)
				.writeBool("profile_background_tile", profileBackgroundTiled)
				.writeString("profile_banner_url", profileBannerImageUrl)
				.writeString("profile_image_url", profileImageUrl)
				.writeString("profile_image_url_https", profileImageUrlHttps)
				.writeString("profile_link_color", profileLinkColor)
				.writeString("profile_sidebar_border_color", profileSidebarBorderColor)
				.writeString("profile_sidebar_fill_color", profileSidebarFillColor)
				.writeString("profile_text_color", profileTextColor)
				.writeBool("profile_use_background_image", profileUseBackgroundImage)
				.writeBool("protected", isProtected)
				.writeString("screen_name", screenName)
				.writeBool("show_all_inline_media", showAllInlineMedia)
				.writeInt("statuses_count", statusesCount)
				.writeString("time_zone", timeZone)
				.writeString("url", url)
				.writeInt("utc_offset", utcOffset)
				.writeBool("verified", isVerified)
				.writeStringArray("withheld_in_countries", withheldInCountries)
				.writeLong("timestamp", timestamp);
//				.writeString("notification", )
//				.writeString("following",  )
		dirEntry.mkdir("entities");
		URLEntityImpl.write(dirEntry.mkdir("entities/url"), urlEntity);
		URLEntityImpl.write(dirEntry.mkdir("entities/description"), descriptionURLEntities);
	}
}

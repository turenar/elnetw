package jp.syuriken.snsw.twclient;

import java.net.URL;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import twitter4j.RateLimitStatus;
import twitter4j.Status;
import twitter4j.User;
import twitter4j.internal.org.json.JSONException;
import twitter4j.internal.org.json.JSONObject;
import twitter4j.json.DataObjectFactory;

/**
 * プロフィールの更新ができるUser拡張型
 *
 * @author Turenar <snswinhaiku dot lo at gmail dot com>
 */
@SuppressWarnings("serial")
public class TwitterUser implements User, TwitterExtendedObject {

	private static final Logger logger = LoggerFactory.getLogger(TwitterUser.class);


	private static JSONObject getJsonObject(ClientConfiguration configuration, User originalUser) throws AssertionError {
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

	private boolean profileUseBackgroundImage;

	private final boolean showAllInlineMedia;

	private final String timeZone;

	private final boolean translator;

	private final int utcOffset;

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

	private URL url;

	private String profileBackgroundImageUrl;

	private String profileBackgroundImageUrlHttps;

	private URL profileImageUrl;

	private URL profileImageUrlHttps;

	private transient final int accessLevel;

	private transient final RateLimitStatus rateLimitStatus;

	private final String json;


	/**
	 * インスタンスを生成する。
	 * @param configuration 設定
	 * @param originalUser オリジナルユーザー
	 */
	public TwitterUser(ClientConfiguration configuration, User originalUser) {
		this(configuration, originalUser, getJsonObject(configuration, originalUser));
	}

	/**
	 * インスタンスを生成する。
	 * @param configuration 設定
	 * @param originalUser オリジナルユーザー
	 * @param jsonObject 生JSON。取得できなかった場合にはnull。
	 */
	public TwitterUser(ClientConfiguration configuration, User originalUser, JSONObject jsonObject) {
		createdAt = originalUser.getCreatedAt();
		id = originalUser.getId();
		isContributorsEnabled = originalUser.isContributorsEnabled();
		isFollowRequestSent = originalUser.isFollowRequestSent();
		isGeoEnabled = originalUser.isGeoEnabled();
		isVerified = originalUser.isVerified();
		lang = originalUser.getLang();
		url = originalUser.getURL();
		profileBackgroundColor = originalUser.getProfileBackgroundColor();
		profileBackgroundImageUrl = originalUser.getProfileBackgroundImageUrl();
		profileBackgroundImageUrlHttps = originalUser.getProfileBackgroundImageUrlHttps();
		profileBackgroundTiled = originalUser.isProfileBackgroundTiled();
		profileImageUrl = originalUser.getProfileImageURL();
		profileImageUrlHttps = originalUser.getProfileImageUrlHttps();
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
		accessLevel = originalUser.getAccessLevel();
		rateLimitStatus = originalUser.getRateLimitStatus();

		json = jsonObject == null ? null : jsonObject.toString();

		Status status = originalUser.getStatus();
		JSONObject statusJsonObject;
		try {
			statusJsonObject =
					jsonObject == null ? null : (jsonObject.isNull("status") ? null : jsonObject
						.getJSONObject("status"));
		} catch (JSONException e) {
			logger.error("Cannot parse json", e);
			throw new RuntimeException(e);
		}
		if (status != null && status instanceof TwitterStatus == false) {
			CacheManager cacheManager = configuration.getCacheManager();
			Status cachedStatus = cacheManager.getCachedStatus(status.getId());
			if (cachedStatus == null) {
				status = new TwitterStatus(configuration, status, statusJsonObject);
				cachedStatus = cacheManager.cacheStatusIfAbsent(status);
				if (cachedStatus == null) {
					cachedStatus = status;
				}
			}
		}
		this.status = status;
	}

	@Override
	public int compareTo(User b) {
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
		} else if (obj instanceof User == false) {
			return false;
		}
		return ((User) obj).getId() == id;
	}

	@Override
	public int getAccessLevel() {
		return accessLevel;
	}

	@Override
	public Date getCreatedAt() {
		return createdAt;
	}

	@Override
	public String getDescription() {
		return description;
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
	public String getName() {
		return name;
	}

	@Override
	public String getProfileBackgroundColor() {
		return profileBackgroundColor;
	}

	@Override
	public String getProfileBackgroundImageUrl() {
		return profileBackgroundImageUrl;
	}

	@Override
	public String getProfileBackgroundImageUrlHttps() {
		return profileBackgroundImageUrlHttps;
	}

	@Override
	public URL getProfileImageURL() {
		return profileImageUrl;
	}

	@Override
	public URL getProfileImageUrlHttps() {
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

	@Override
	public RateLimitStatus getRateLimitStatus() {
		return rateLimitStatus;
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
	public URL getURL() {
		return url;
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
	 * このユーザーのプロフィールを指定したユーザーのプロフィールでアップデートする
	 *
	 * @param user 新しい情報が含まれたユーザー
	 * @throws IllegalArgumentException このユーザーのIDと指定したユーザーのIDが一致しない
	 */
	public void updateUser(User user) throws IllegalArgumentException {
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
		profileBackgroundImageUrl = user.getProfileBackgroundImageUrl();
		profileBackgroundImageUrlHttps = user.getProfileBackgroundImageUrlHttps();
		profileImageUrl = user.getProfileImageURL();
		profileImageUrlHttps = user.getProfileImageUrlHttps();
		isProtected = user.isProtected();
		screenName = user.getScreenName();
		status = user.getStatus();
		statusesCount = user.getStatusesCount();
		url = user.getURL();
	}
}

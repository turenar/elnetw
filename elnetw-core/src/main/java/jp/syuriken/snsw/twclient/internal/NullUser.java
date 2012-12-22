package jp.syuriken.snsw.twclient.internal;

import java.net.URL;
import java.util.Date;

import twitter4j.RateLimitStatus;
import twitter4j.Status;
import twitter4j.User;

/**
 * 空のUser実装
 *
 * @author Turenar <snswinhaiku dot lo at gmail dot com>
 * @see NullStatus
 */
public class NullUser implements User {

	private static final long serialVersionUID = -709770915229434160L;

	/** 使いまわし用のインスタンス */
	public static final NullUser INSTANCE = new NullUser();


	@Override
	public int compareTo(User o) {
		return 0;
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof NullUser;
	}

	@Override
	public int getAccessLevel() {
		return -1;
	}

	@Override
	public String getBiggerProfileImageURL() {
		return null;
	}

	@Override
	public String getBiggerProfileImageURLHttps() {
		return null;
	}

	@Override
	public Date getCreatedAt() {
		return null;
	}

	@Override
	public String getDescription() {
		return null;
	}

	@Override
	public int getFavouritesCount() {
		return -1;
	}

	@Override
	public int getFollowersCount() {
		return -1;
	}

	@Override
	public int getFriendsCount() {
		return -1;
	}

	@Override
	public long getId() {
		return -1;
	}

	@Override
	public String getLang() {
		return null;
	}

	@Override
	public int getListedCount() {
		return -1;
	}

	@Override
	public String getLocation() {
		return null;
	}

	@Override
	public String getMiniProfileImageURL() {
		return null;
	}

	@Override
	public String getMiniProfileImageURLHttps() {
		return null;
	}

	@Override
	public String getName() {
		return null;
	}

	@Override
	public String getOriginalProfileImageURL() {
		return null;
	}

	@Override
	public String getOriginalProfileImageURLHttps() {
		return null;
	}

	@Override
	public String getProfileBackgroundColor() {
		return null;
	}

	@SuppressWarnings("deprecation")
	@Deprecated
	@Override
	public String getProfileBackgroundImageUrl() {
		return null;
	}

	@Override
	public String getProfileBackgroundImageURL() {
		return null;
	}

	@Override
	public String getProfileBackgroundImageUrlHttps() {
		return null;
	}

	@Override
	public String getProfileBannerIPadRetinaURL() {
		return null;
	}

	@Override
	public String getProfileBannerIPadURL() {
		return null;
	}

	@Override
	public String getProfileBannerMobileRetinaURL() {
		return null;
	}

	@Override
	public String getProfileBannerMobileURL() {
		return null;
	}

	@Override
	public String getProfileBannerRetinaURL() {
		return null;
	}

	@Override
	public String getProfileBannerURL() {
		return null;
	}

	@Override
	public String getProfileImageURL() {
		return null;
	}

	@SuppressWarnings("deprecation")
	@Deprecated
	@Override
	public URL getProfileImageUrlHttps() {
		return null;
	}

	@Override
	public String getProfileImageURLHttps() {
		return null;
	}

	@Override
	public String getProfileLinkColor() {
		return null;
	}

	@Override
	public String getProfileSidebarBorderColor() {
		return null;
	}

	@Override
	public String getProfileSidebarFillColor() {
		return null;
	}

	@Override
	public String getProfileTextColor() {
		return null;
	}

	@Override
	public RateLimitStatus getRateLimitStatus() {
		return null;
	}

	@Override
	public String getScreenName() {
		return null;
	}

	@Override
	public Status getStatus() {
		return null;
	}

	@Override
	public int getStatusesCount() {
		return -1;
	}

	@Override
	public String getTimeZone() {
		return null;
	}

	@Override
	public String getURL() {
		return null;
	}

	@Override
	public int getUtcOffset() {
		return -1;
	}

	@Override
	public int hashCode() {
		return 0;
	}

	@Override
	public boolean isContributorsEnabled() {
		return false;
	}

	@Override
	public boolean isFollowRequestSent() {
		return false;
	}

	@Override
	public boolean isGeoEnabled() {
		return false;
	}

	@Override
	public boolean isProfileBackgroundTiled() {
		return false;
	}

	@Override
	public boolean isProfileUseBackgroundImage() {
		return false;
	}

	@Override
	public boolean isProtected() {
		return false;
	}

	@Override
	public boolean isShowAllInlineMedia() {
		return false;
	}

	@Override
	public boolean isTranslator() {
		return false;
	}

	@Override
	public boolean isVerified() {
		return false;
	}

}

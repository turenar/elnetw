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

package jp.mydns.turenar.twclient.internal;

import java.util.Date;

import javax.annotation.Nonnull;

import twitter4j.RateLimitStatus;
import twitter4j.Status;
import twitter4j.URLEntity;
import twitter4j.User;

/**
 * 空のUser実装
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 * @see NullStatus
 */
public class NullUser implements User {
	@Override
	public String[] getWithheldInCountries() {
		return new String[0];
	}

	private static final long serialVersionUID = -709770915229434160L;

	/** 使いまわし用のインスタンス */
	public static final NullUser INSTANCE = new NullUser();
	private final long userId;

	/**
	 * instance. userId=-1L
	 */
	public NullUser() {
		this(-1L);
	}

	/**
	 * instance.
	 *
	 * @param userId userId: should be negative number because avoid conflict?
	 */
	public NullUser(long userId) {
		this.userId = userId;
	}

	@Override
	public int compareTo(@Nonnull User o) {
		return (int) (userId - o.getId());
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof NullUser && ((NullUser) obj).getId() == getId();
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
		return new Date();
	}

	@Override
	public String getDescription() {
		return null;
	}

	@Override
	public URLEntity[] getDescriptionURLEntities() {
		return new URLEntity[0];
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
		return userId;
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
	public URLEntity getURLEntity() {
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
	public boolean isDefaultProfile() {
		return false;
	}

	@Override
	public boolean isDefaultProfileImage() {
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

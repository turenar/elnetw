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

import jp.mydns.turenar.twclient.CacheManager;
import jp.mydns.turenar.twclient.ClientConfiguration;
import jp.mydns.turenar.twclient.storage.DirEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.RateLimitStatus;
import twitter4j.Status;
import twitter4j.URLEntity;
import twitter4j.User;

/**
 * DelayedUserImpl: block until cache manager gets user information
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class DelayedUserImpl extends TwitterUser {
	private static final Logger logger = LoggerFactory.getLogger(DelayedUserImpl.class);
	private static final long serialVersionUID = 2888425677038932000L;
	private final long userId;
	protected TwitterUser target;

	/**
	 * create instance
	 *
	 * @param userId user id
	 */
	public DelayedUserImpl(long userId) {
		this.userId = userId;
	}

	@Override
	public int getAccessLevel() {
		return getTarget().getAccessLevel();
	}

	@Override
	public String getBiggerProfileImageURL() {
		return getTarget().getBiggerProfileImageURL();
	}

	@Override
	public String getBiggerProfileImageURLHttps() {
		return getTarget().getBiggerProfileImageURLHttps();
	}

	@Override
	public Date getCreatedAt() {
		return getTarget().getCreatedAt();
	}

	@Override
	public String getDescription() {
		return getTarget().getDescription();
	}

	@Override
	public URLEntity[] getDescriptionURLEntities() {
		return getTarget().getDescriptionURLEntities();
	}

	@Override
	public int getFavouritesCount() {
		return getTarget().getFavouritesCount();
	}

	@Override
	public int getFollowersCount() {
		return getTarget().getFollowersCount();
	}

	@Override
	public int getFriendsCount() {
		return getTarget().getFriendsCount();
	}

	@Override
	public long getId() {
		return userId;
	}

	@Override
	public String getLang() {
		return getTarget().getLang();
	}

	@Override
	public int getListedCount() {
		return getTarget().getListedCount();
	}

	@Override
	public String getLocation() {
		return getTarget().getLocation();
	}

	@Override
	public String getMiniProfileImageURL() {
		return getTarget().getMiniProfileImageURL();
	}

	@Override
	public String getMiniProfileImageURLHttps() {
		return getTarget().getMiniProfileImageURLHttps();
	}

	@Override
	public String getName() {
		return getTarget().getName();
	}

	@Override
	public String getOriginalProfileImageURL() {
		return getTarget().getOriginalProfileImageURL();
	}

	@Override
	public String getOriginalProfileImageURLHttps() {
		return getTarget().getOriginalProfileImageURLHttps();
	}

	@Override
	public String getProfileBackgroundColor() {
		return getTarget().getProfileBackgroundColor();
	}

	@Override
	public String getProfileBackgroundImageURL() {
		return getTarget().getProfileBackgroundImageURL();
	}

	@Override
	public String getProfileBackgroundImageUrlHttps() {
		return getTarget().getProfileBackgroundImageUrlHttps();
	}

	@Override
	public String getProfileBannerIPadRetinaURL() {
		return getTarget().getProfileBannerIPadRetinaURL();
	}

	@Override
	public String getProfileBannerIPadURL() {
		return getTarget().getProfileBannerIPadURL();
	}

	@Override
	public String getProfileBannerLargeURL() {
		return getTarget().getProfileBannerLargeURL();
	}

	@Override
	public String getProfileBannerMediumURL() {
		return getTarget().getProfileBannerMediumURL();
	}

	@Override
	public String getProfileBannerMobileRetinaURL() {
		return getTarget().getProfileBannerMobileRetinaURL();
	}

	@Override
	public String getProfileBannerMobileURL() {
		return getTarget().getProfileBannerMobileURL();
	}

	@Override
	public String getProfileBannerRetinaURL() {
		return getTarget().getProfileBannerRetinaURL();
	}

	@Override
	public String getProfileBannerSmallURL() {
		return getTarget().getProfileBannerSmallURL();
	}

	@Override
	public String getProfileBannerURL() {
		return getTarget().getProfileBannerURL();
	}

	@Override
	public String getProfileImageURL() {
		return getTarget().getProfileImageURL();
	}

	@Override
	public String getProfileImageURLHttps() {
		return getTarget().getProfileImageURLHttps();
	}

	@Override
	public String getProfileLinkColor() {
		return getTarget().getProfileLinkColor();
	}

	@Override
	public String getProfileSidebarBorderColor() {
		return getTarget().getProfileSidebarBorderColor();
	}

	@Override
	public String getProfileSidebarFillColor() {
		return getTarget().getProfileSidebarFillColor();
	}

	@Override
	public String getProfileTextColor() {
		return getTarget().getProfileTextColor();
	}

	@Override
	public RateLimitStatus getRateLimitStatus() {
		return getTarget().getRateLimitStatus();
	}

	@Override
	public String getScreenName() {
		return getTarget().getScreenName();
	}

	@Override
	public Status getStatus() {
		return getTarget().getStatus();
	}

	@Override
	public int getStatusesCount() {
		return getTarget().getStatusesCount();
	}

	/**
	 * get real target user
	 *
	 * @return target user
	 */
	protected TwitterUser getTarget() {
		if (target == null) {
			ClientConfiguration configuration = ClientConfiguration.getInstance();
			CacheManager cacheManager = configuration.getCacheManager();
			if (!cacheManager.isCachedUser(userId)) {
				cacheManager.queueFetchingUser(userId);
				cacheManager.runUserFetcher(true);
				Object lock = cacheManager.getDelayingNotifierLock();
				synchronized (lock) {
					boolean interrupted = false;
					while (!cacheManager.isCachedUser(userId)) {
						try {
							logger.info("[DELAYED] waiting user cached...");
							lock.wait();
						} catch (InterruptedException e) {
							interrupted = true; // save interrupt status
						}
					}
					if (interrupted) {
						Thread.currentThread().interrupt();
					}
				}
			}
			target = cacheManager.getUser(userId);
		}
		if (target == null) {
			// TODO if cache manager cached error status, we throw that twitter exception (wrapped by RuntimeException)
			throw new RuntimeException("user fetching failed");
		}
		return target;
	}

	@Override
	public String getTimeZone() {
		return getTarget().getTimeZone();
	}

	@Override
	public long getTimestamp() {
		return getTarget().getTimestamp();
	}

	@Override
	public String getURL() {
		return getTarget().getURL();
	}

	@Override
	public URLEntity getURLEntity() {
		return getTarget().getURLEntity();
	}

	@Override
	public int getUtcOffset() {
		return getTarget().getUtcOffset();
	}

	@Override
	public boolean isContributorsEnabled() {
		return getTarget().isContributorsEnabled();
	}

	@Override
	public boolean isDefaultProfile() {
		return getTarget().isDefaultProfile();
	}

	@Override
	public boolean isDefaultProfileImage() {
		return getTarget().isDefaultProfileImage();
	}

	@Override
	public boolean isFollowRequestSent() {
		return getTarget().isFollowRequestSent();
	}

	@Override
	public boolean isGeoEnabled() {
		return getTarget().isGeoEnabled();
	}

	@Override
	public boolean isProfileBackgroundTiled() {
		return getTarget().isProfileBackgroundTiled();
	}

	@Override
	public boolean isProfileUseBackgroundImage() {
		return getTarget().isProfileUseBackgroundImage();
	}

	@Override
	public boolean isProtected() {
		return getTarget().isProtected();
	}

	@Override
	public boolean isShowAllInlineMedia() {
		return getTarget().isShowAllInlineMedia();
	}

	@Override
	public boolean isTranslator() {
		return getTarget().isTranslator();
	}

	@Override
	public boolean isVerified() {
		return getTarget().isVerified();
	}

	@Override
	public void update(User user) {
		getTarget().update(user);
	}

	@Override
	public void write(DirEntry dirEntry) {
		getTarget().write(dirEntry);
	}

}

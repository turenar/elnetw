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

import javax.annotation.Nonnull;

import jp.mydns.turenar.twclient.CacheManager;
import twitter4j.User;

/**
 * twitter user object
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public abstract class TwitterUser implements User, TwitterExtendedObject {

	/**
	 * get TwitterUser from specified user.
	 *
	 * <ul>
	 * <li>First, return just user if user instanceof TwitterUser.</li>
	 * <li>Second, check user is in cache. if user is cached, return that instance updated by user.</li>
	 * <li>Otherwise, make instance from user.</li>
	 * </ul>
	 *
	 * @param user user
	 * @return TwitterUser instance
	 */
	public static TwitterUser getInstance(User user) {
		if (user instanceof TwitterUser) {
			return (TwitterUser) user;
		}

		CacheManager cacheManager = TwitterStatus.configuration.getCacheManager();
		TwitterUser cachedUser = cacheManager.getCachedUser(user.getId());
		if (cachedUser == null) {
			TwitterUser twitterUser = new TwitterUserImpl(user);
			cachedUser = cacheManager.cacheUser(twitterUser);
			if (cachedUser == null) {
				cachedUser = twitterUser;
			}
		} else {
			cachedUser.update(user);
		}
		return cachedUser;
	}

	@Override
	public int compareTo(@Nonnull User b) {
		long thisId = getId();
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
		return ((User) obj).getId() == getId();
	}

	/**
	 * get large profile banner url
	 *
	 * @return url
	 */
	public abstract String getProfileBannerLargeURL();

	/**
	 * get medium profile banner url
	 *
	 * @return url
	 */
	public abstract String getProfileBannerMediumURL();

	/**
	 * get small profile banner url
	 *
	 * @return url
	 */
	public abstract String getProfileBannerSmallURL();

	/**
	 * last updated timestamp
	 *
	 * @return timestamp
	 */
	public abstract long getTimestamp();

	@Override
	public int hashCode() {
		return (int) getId();
	}

	/**
	 * update user information
	 *
	 * @param user originalUser
	 */
	public abstract void update(User user);
}

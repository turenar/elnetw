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

package jp.syuriken.snsw.twclient;

import java.util.Arrays;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import jp.syuriken.snsw.twclient.internal.ConcurrentSoftHashMap;
import jp.syuriken.snsw.twclient.internal.NullStatus;
import jp.syuriken.snsw.twclient.internal.NullUser;
import jp.syuriken.snsw.twclient.internal.TwitterRunnable;
import jp.syuriken.snsw.twclient.twitter.TwitterStatus;
import jp.syuriken.snsw.twclient.twitter.TwitterUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;

/**
 * StatusとUserをキャッシュするクラス。
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class CacheManager {

	/**
	 * Statusを取得するジョブ
	 *
	 * @author Turenar (snswinhaiku dot lo at gmail dot com)
	 */
	protected class StatusFetcher extends TwitterRunnable implements ParallelRunnable {

		private final Logger logger = LoggerFactory.getLogger(StatusFetcher.class);
		private long statusId;

		/**
		 * インスタンスを生成する。
		 *
		 * @param statusId ステータスID
		 */
		public StatusFetcher(long statusId) {
			this.statusId = statusId;
		}

		@Override
		protected void access() throws TwitterException {
			cacheStatus(new TwitterStatus(twitter.showStatus(statusId)));
		}

		@Override
		protected void onException(TwitterException ex) {
			if (ex.getStatusCode() == TwitterException.NOT_FOUND) {
				logger.info("not found: statusId={}", statusId);
				statusCacheMap.put(statusId, ERROR_STATUS);
			}
		}
	}

	/**
	 * ユーザーを取得するジョブ
	 *
	 * @author Turenar (snswinhaiku dot lo at gmail dot com)
	 */
	private class UserFetcher extends TwitterRunnable implements ParallelRunnable {

		private final Logger logger = LoggerFactory.getLogger(UserFetcher.class);
		private long[] userIds;

		/**
		 * インスタンスを生成する。
		 *
		 * @param userIds   ユーザーID
		 * @param intoQueue キューに追加するかどうか
		 */
		public UserFetcher(long[] userIds, boolean intoQueue) {
			super(intoQueue);
			Arrays.sort(userIds);
			this.userIds = userIds;
		}

		@Override
		protected void access() throws TwitterException {
			ResponseList<User> users = twitter.lookupUsers(userIds);
			for (User user : users) {
				cacheUser(new TwitterUser(user));
			}
			for (long userId : userIds) {
				if (!isCachedUser(userId)) {
					logger.info("not found: userId={}", userId);
					userCacheMap.put(userId, ERROR_USER);
				}
			}
		}
	}

	/** エラー時に格納するUser */
	protected static final User ERROR_USER = new NullUser();
	/** エラー時に格納するStatus */
	protected static final Status ERROR_STATUS = new NullStatus();
	/** リクエストごとの最大User要求数 */
	protected static final int MAX_USERS_PER_LOOKUP_REQUEST = 100;
	/** StatusをキャッシュするMap */
	protected final ConcurrentSoftHashMap<Long, Status> statusCacheMap;
	/** UserをキャッシュするMap */
	protected final ConcurrentSoftHashMap<Long, User> userCacheMap;
	/** Userのキャッシュ待ちキュー */
	protected final ConcurrentLinkedQueue<Long> userCacheQueue;
	/** Userのキャッシュ待ちキューの長さ */
	protected final AtomicInteger userCacheQueueLength;
	/** Twitterインスタンス */
	protected final Twitter twitter;
	/** 設定 */
	protected final ClientConfiguration configuration;

	/**
	 * インスタンスを生成する。
	 *
	 * @param configuration 設定
	 */
	public CacheManager(ClientConfiguration configuration) {
		this.configuration = configuration;
		ClientProperties properties = configuration.getConfigProperties();
		int concurrency = properties.getInteger("core.cache.data.concurrency");
		float loadFactor = properties.getFloat("core.cache.data.load_factor");
		int initialCapacity = properties.getInteger("core.cache.data.initial_capacity");

		statusCacheMap = new ConcurrentSoftHashMap<>(concurrency, loadFactor, initialCapacity);
		userCacheMap = new ConcurrentSoftHashMap<>(concurrency, loadFactor, initialCapacity);
		userCacheQueue = new ConcurrentLinkedQueue<>();
		userCacheQueueLength = new AtomicInteger();
		twitter = configuration.getTwitterForRead();
	}

	/**
	 * Statusをキャッシュする。
	 *
	 * @param status キャッシュするStatus。nullだとぬるぽ投げます。
	 */
	public void cacheStatus(TwitterStatus status) {
		if (status == null) {
			throw new NullPointerException();
		}
		statusCacheMap.put(status.getId(), status);
		if (status.isRetweeted()) {
			statusCacheMap.put(status.getRetweetedStatus().getId(), status);
		}
	}

	/**
	 * キャッシュされていない場合のみStatusをキャッシュする。
	 *
	 * @param status キャッシュするStatus。nullだとぬるぽ投げます。
	 * @return すでにキャッシュされていた場合、キャッシュされたStatus。キャッシュされていなかった場合null。
	 */
	public TwitterStatus cacheStatusIfAbsent(TwitterStatus status) {
		if (status == null) {
			throw new NullPointerException();
		}
		return extract(statusCacheMap.putIfAbsent(status.getId(), status));
	}

	/**
	 * Userをキャッシュする
	 *
	 * @param user キャッシュするUser。nullだとぬるぽ投げます。
	 */
	public void cacheUser(TwitterUser user) {
		if (user == null) {
			throw new NullPointerException();
		}
		userCacheMap.put(user.getId(), user);
	}

	/**
	 * キャッシュされていない場合のみUserをキャッシュする。
	 *
	 * @param user キャッシュするStatus。nullだとぬるぽ投げます。
	 * @return すでにキャッシュされていた場合、キャッシュされたStatus。キャッシュされていなかった場合null。
	 */
	public TwitterUser cacheUserIfAbsent(TwitterUser user) {
		if (user == null) {
			throw new NullPointerException();
		}
		return extract(userCacheMap.putIfAbsent(user.getId(), user));
	}

	private TwitterStatus extract(Status status) {
		if (status == ERROR_STATUS) {
			return null;
		} else {
			return (TwitterStatus) status;
		}
	}

	private TwitterUser extract(User user) {
		if (user == ERROR_USER) {
			return null;
		} else {
			return (TwitterUser) user;
		}
	}

	/**
	 * キャッシュ済みStatusを取得する。キャッシュされていなかったりStatusが存在しない(404)場合はnull。
	 * このメソッドはブロックしない。
	 *
	 * @param statusId Status ID
	 * @return Statusインスタンス。キャッシュされていなかったりStatusが存在しない(404)場合はnull。
	 */
	public TwitterStatus getCachedStatus(long statusId) {
		return extract(statusCacheMap.get(statusId));
	}

	/**
	 * キャッシュ済みStatusを取得する。キャッシュされていなかったりStatusが存在しない(404)場合は引数のTwitterStatusインスタンスを返す。
	 *
	 * @param status キャッシュ済みStatus
	 * @return TwitterStatusインスタンス
	 */
	public TwitterStatus getCachedStatus(TwitterStatus status) {
		TwitterStatus twitterStatus = cacheStatusIfAbsent(status);
		return twitterStatus == null ? status : twitterStatus.update(status);
	}

	/**
	 * キャッシュ済みUserを取得する。キャッシュされていなかったりUserが存在しない(404)場合はnull。
	 * このメソッドはブロックしない。
	 *
	 * @param userId User ID
	 * @return Userインスタンス。キャッシュされていなかったりUserが存在しない(404)場合はnull。
	 */
	public TwitterUser getCachedUser(long userId) {
		return extract(userCacheMap.get(userId));
	}

	/**
	 * キャッシュ済みUserを取得する。キャッシュされていなかったりUserが存在しない(404)場合は引数のTwitterUserインスタンスを返す。
	 *
	 * @param user キャッシュ済みUser
	 * @return TwitterUserインスタンス
	 */
	public TwitterUser getCachedUser(TwitterUser user) {
		TwitterUser twitterUser = cacheUserIfAbsent(user);
		return twitterUser == null ? user : twitterUser.updateUser(user);
	}

	/**
	 * Statusを取得する。なんらかの理由でStatusが取得できなかった場合はnull。
	 * このメソッドはブロックする可能性がある。
	 *
	 * @param statusId TwitterStatus ID
	 * @return Statusインスタンス。
	 */
	public TwitterStatus getStatus(long statusId) {
		Status status = statusCacheMap.get(statusId);
		if (status == null) {
			new StatusFetcher(statusId).run();
			status = statusCacheMap.get(statusId);
		}
		return extract(status);
	}

	/**
	 * キャッシュとして持っているステータスを取得するための {@link Collection} を取得する。
	 * これはキャッシュ格納に使用している {@link ConcurrentMap} が変更されても
	 * {@link ConcurrentModificationException} はスローされず、また取得する値も変わります。
	 *
	 * @return Collection
	 */
	public Collection<Status> getStatusSet() {
		return statusCacheMap.values();
	}

	/**
	 * Userを取得する。なんらかの理由でUserが取得できなかった場合はnull。
	 * このメソッドはブロックする可能性がある。
	 *
	 * @param userId User ID
	 * @return Userインスタンス。
	 */
	public TwitterUser getUser(long userId) {
		User user = userCacheMap.get(userId);
		if (user == null) {
			userCacheQueue.add(userId);
			int len = userCacheQueueLength.incrementAndGet();
			do {
				runUserFetcher(len, false);
			} while ((len = userCacheQueueLength.get()) > 0);
			user = userCacheMap.get(userId);
		}
		return extract(user);
	}

	/**
	 * キャッシュとして持っているユーザーを取得するための {@link Collection} を取得する。
	 * これはキャッシュ格納に使用している {@link ConcurrentMap} が変更されても
	 * {@link ConcurrentModificationException} はスローされず、また取得する値も変わります。
	 *
	 * @return Collection
	 */
	public Collection<User> getUserSet() {
		return userCacheMap.values();
	}

	/**
	 * IDがstatusIdなStatusがキャッシュ及びエラーキャッシュされているかどうかを調べる。
	 *
	 * <p>
	 * Statusが存在しない(404)場合もtrueを返すことに注意。エラーキャッシュではなくキャッシュされているかどうかのみを
	 * 調べるときは、 {@link #getCachedStatus(long)} != statusId を用いる。
	 * </p>
	 *
	 * @param statusId ステータスID
	 * @return キャッシュされているかどうか
	 */
	public boolean isCachedStatus(long statusId) {
		return statusCacheMap.containsKey(statusId);
	}

	/**
	 * IDがuserIdなUserがキャッシュ及びエラーキャッシュされているかどうかを調べる。
	 *
	 * <p>
	 * Userが存在しない(404)場合もtrueを返すことに注意。エラーキャッシュではなくキャッシュされているかどうかのみを
	 * 調べるときは、 {@link #getCachedUser(long)} != userId を用いる。
	 * </p>
	 *
	 * @param userId ステータスID
	 * @return キャッシュされているかどうか
	 */
	public boolean isCachedUser(long userId) {
		return userCacheMap.containsKey(userId);
	}

	/**
	 * Statusを遅延取得する。
	 *
	 * @param statusId Status ID
	 */
	public void queueFetchingStatus(long statusId) {
		configuration.addJob(new StatusFetcher(statusId));
	}

	/**
	 * Userを遅延取得する。
	 *
	 * @param userId User ID
	 */
	public void queueFetchingUser(long userId) {
		if (userCacheMap.containsKey(userId)) {
			return;
		}

		userCacheQueue.offer(userId);
		int len = userCacheQueueLength.incrementAndGet();
		if (len > MAX_USERS_PER_LOOKUP_REQUEST) {
			runUserFetcher(len, true);
		}
	}

	/**
	 * 指定したステータスのキャッシュを削除する
	 *
	 * @param statusId ステータスID
	 */
	public void removeCachedStatus(long statusId) {
		statusCacheMap.remove(statusId);
	}

	/**
	 * 指定したユーザーのキャッシュを削除する
	 *
	 * @param userId ユーザーID
	 */
	public void removeCachedUser(long userId) {
		userCacheMap.remove(userId);
	}

	/**
	 * UserFetcherを走らせる。
	 *
	 * @param expectedLength {@link #userCacheQueue}のこのメソッドが呼び出される前の長さ。
	 *                       同時更新していると思われる時にはreturn falseします。
	 * @param intoQueue      trueの場合ジョブキューに追加する。falseの場合UserFetcherが完了するまでブロックします。
	 * @return {@link #userCacheQueue} が更新されたと思われる場合false。 正常にキューできたときはtrue。
	 */
	protected boolean runUserFetcher(int expectedLength, boolean intoQueue) {
		int newLength = expectedLength - MAX_USERS_PER_LOOKUP_REQUEST;
		if (newLength < 0) {
			newLength = 0;
		}
		if (!userCacheQueueLength.compareAndSet(expectedLength, newLength)) {
			return false;
		}

		int len = expectedLength - newLength;
		long[] arr = new long[len];
		int i = 0;
		for (; i < len; i++) {
			Long userId = userCacheQueue.poll();
			if (userId == null) {
				break; // conflict?
			}
			arr[i] = userId;
		}
		if (i != len) {
			arr = Arrays.copyOf(arr, i);
		}

		UserFetcher userFetcher = new UserFetcher(arr, intoQueue);
		if (intoQueue) {
			configuration.addJob(userFetcher);
		} else {
			userFetcher.run();
		}
		return true;
	}
}

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

package jp.mydns.turenar.twclient;

import java.util.Arrays;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import jp.mydns.turenar.twclient.conf.ClientProperties;
import jp.mydns.turenar.twclient.internal.ConcurrentSoftHashMap;
import jp.mydns.turenar.twclient.internal.NullStatus;
import jp.mydns.turenar.twclient.internal.NullUser;
import jp.mydns.turenar.twclient.internal.TwitterRunnable;
import jp.mydns.turenar.twclient.storage.CacheStorage;
import jp.mydns.turenar.twclient.storage.DirEntry;
import jp.mydns.turenar.twclient.twitter.DelayedUserImpl;
import jp.mydns.turenar.twclient.twitter.TwitterStatus;
import jp.mydns.turenar.twclient.twitter.TwitterUser;
import jp.mydns.turenar.twclient.twitter.TwitterUserImpl;
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
				cacheUser(new TwitterUserImpl(user), false);
			}
			for (long userId : userIds) {
				if (!isCachedUser(userId)) {
					logger.info("not found: userId={}", userId);
					userCacheMap.put(userId, ERROR_USER);
				}
			}
			synchronized (delayingNotifierLock) {
				delayingNotifierLock.notifyAll();
			}
		}

	}

	/** エラー時に格納するUser */
	protected static final User ERROR_USER = new NullUser();
	/** エラー時に格納するStatus */
	protected static final Status ERROR_STATUS = new NullStatus();
	/** リクエストごとの最大User要求数 */
	protected static final int MAX_USERS_PER_LOOKUP_REQUEST = 100;

	private static TwitterStatus extract(Status status) {
		if (status == ERROR_STATUS) {
			return null;
		} else {
			return (TwitterStatus) status;
		}
	}

	private static TwitterUser extract(User user) {
		if (user == ERROR_USER) {
			return null;
		} else {
			return (TwitterUser) user;
		}
	}

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
	private final CacheStorage cacheStorage;
	/**
	 * lock for delayed user
	 */
	protected final Object delayingNotifierLock = new Object();

	/**
	 * インスタンスを生成する。
	 *
	 * @param configuration 設定
	 */
	public CacheManager(ClientConfiguration configuration) {
		this.configuration = configuration;
		cacheStorage = configuration.getCacheStorage();
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
	 * @return キャッシュされていたStatus。キャッシュされていなければ引数をそのまま返す。
	 */
	public TwitterStatus cacheStatus(TwitterStatus status) {
		if (status == null) {
			throw new NullPointerException();
		}
		TwitterStatus cachedStatus = extract(statusCacheMap.putIfAbsent(status.getId(), status));
		if (cachedStatus != null) {
			cachedStatus.update(status);
		}
		if (status.isRetweet()) {
			TwitterStatus retweetedStatus = status.getRetweetedStatus();
			TwitterStatus cachedRetweetedStatus = extract(statusCacheMap.putIfAbsent(retweetedStatus.getId(), status));
			if (cachedRetweetedStatus != null) {
				cachedRetweetedStatus.update(retweetedStatus);
			}
		}
		return cachedStatus == null ? status : cachedStatus;
	}

	/**
	 * Userをキャッシュする
	 *
	 * @param user キャッシュするUser。nullだとぬるぽ投げます。
	 * @return キャッシュされていたStatus。キャッシュされていなければ引数をそのまま返す。
	 */
	public TwitterUser cacheUser(TwitterUser user) {
		return cacheUser(user, true);
	}

	/**
	 * cache user
	 *
	 * @param user         user
	 * @param shouldNotify should notify delayingNotifierLock's holders
	 * @return cached user or user argument
	 */
	protected TwitterUser cacheUser(TwitterUser user, boolean shouldNotify) {
		if (user == null) {
			throw new NullPointerException();
		}
		TwitterUser cachedUser = extract(userCacheMap.putIfAbsent(user.getId(), user));
		if (cachedUser != null) {
			cachedUser.update(user);
			return cachedUser;
		} else {
			if (shouldNotify) {
				synchronized (delayingNotifierLock) {
					delayingNotifierLock.notifyAll();
				}
			}
			return user;
		}
	}

	/**
	 * flush to cache storage
	 */
	public void flush() {
		DirEntry dirEntry = cacheStorage.mkdir("/cache/user", true);
		getCachedUserStream()
				.map(CacheManager::extract)
				.filter(user -> user != null)
				.forEach(user -> {
					dirEntry.mkdir(Long.toHexString(user.getId() & 0xff));
					user.write(dirEntry.mkdir(getCachePath(user.getId())));
				});
	}

	private String getCachePath(long userId) {
		return "/cache/user/" + Long.toHexString(userId & 0xff) + "/" + userId;
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
	 * キャッシュとして持っているステータスを取得するための {@link Collection} を取得する。
	 * これはキャッシュ格納に使用している {@link ConcurrentMap} が変更されても
	 * {@link ConcurrentModificationException} はスローされず、また取得する値も変わります。
	 *
	 * @return Collection
	 */
	public Stream<Status> getCachedStatusStream() {
		return statusCacheMap.values().stream();
	}

	/**
	 * キャッシュ済みUserを取得する。キャッシュされていなかったりUserが存在しない(404)場合はnull。
	 * このメソッドはブロックしない。
	 *
	 * @param userId User ID
	 * @return Userインスタンス。キャッシュされていなかったりUserが存在しない(404)場合はnull。
	 */
	public TwitterUser getCachedUser(long userId) {
		TwitterUser user = extract(userCacheMap.get(userId));
		if (user == null) {
			String cachePath = getCachePath(userId);
			if (cacheStorage.exists(cachePath)) {
				user = cacheUser(new TwitterUserImpl(cacheStorage.getDirEntry(cachePath)));
			}
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
	public Stream<User> getCachedUserStream() {
		return userCacheMap.values().stream();
	}

	/**
	 * Userを遅延取得する。{@link #queueFetchingUser(long)}との違いは、この関数の呼び出し自体はブロックされないことです。
	 * ただし、UserインスタンスのuserId以外の取得はブロックされます。
	 *
	 * <p>Userに関しては、最大100までキューに入れるため、すぐに取得されるとは限りません。必要があるようなら、
	 * {@link #runUserFetcher(boolean)}を呼び出して、即フェッチさせてください。</p>
	 *
	 * @param userId User ID
	 * @return すでにキャッシュされているとき、エラーキャッシュの場合null、そうでなければ{@link TwitterUserImpl}インスタンス。
	 * キャッシュされていないとき{@link jp.mydns.turenar.twclient.twitter.DelayedUserImpl}
	 */
	public TwitterUser getDelayedUser(long userId) {
		if (userCacheMap.containsKey(userId)) {
			return getCachedUser(userId);
		}

		userCacheQueue.offer(userId);
		int len = userCacheQueueLength.incrementAndGet();
		if (len > MAX_USERS_PER_LOOKUP_REQUEST) {
			runUserFetcher(len, true);
		}
		return new DelayedUserImpl(userId);
	}

	/**
	 * delaying notifier lock: will be notified when new user cached.
	 *
	 * @return lock object. you should use this instance with synchronized block statement
	 */
	public Object getDelayingNotifierLock() {
		return delayingNotifierLock;
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
	 * Userを取得する。なんらかの理由でUserが取得できなかった場合はnull。
	 * このメソッドはブロックする可能性がある。
	 *
	 * @param userId User ID
	 * @return Userインスタンス。
	 */
	public TwitterUser getUser(long userId) {
		User user = getCachedUser(userId);
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
	 * <p>Userに関しては、最大100までキューに入れるため、すぐに取得されるとは限りません。必要があるようなら、
	 * {@link #runUserFetcher(boolean)}を呼び出して、即フェッチさせてください。</p>
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
	 * @param intoQueue trueの場合ジョブキューに追加する。falseの場合UserFetcherが完了するまでブロックします。
	 */
	public void runUserFetcher(boolean intoQueue) {
		while (true) {
			int len = userCacheQueueLength.get();
			if (len < 0) {
				break;
			} else {
				runUserFetcher(len, intoQueue);
			}

		}
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

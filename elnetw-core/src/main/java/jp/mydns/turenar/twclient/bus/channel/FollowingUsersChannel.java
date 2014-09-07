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

package jp.mydns.turenar.twclient.bus.channel;

import jp.mydns.turenar.lib.primitive.LongHashSet;
import jp.mydns.turenar.twclient.ClientMessageAdapter;
import jp.mydns.turenar.twclient.ClientMessageListener;
import jp.mydns.turenar.twclient.JobQueue;
import jp.mydns.turenar.twclient.ParallelRunnable;
import jp.mydns.turenar.twclient.bus.MessageBus;
import jp.mydns.turenar.twclient.bus.MessageChannel;
import jp.mydns.turenar.twclient.internal.TwitterRunnable;
import jp.mydns.turenar.twclient.twitter.TwitterUser;
import twitter4j.IDs;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;

/**
 * following users channel
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class FollowingUsersChannel extends TwitterRunnable implements MessageChannel, ParallelRunnable {
	/**
	 * following users checker
	 */
	public static class FollowingUsersQuery {
		private final FollowingUsersChannel channel;

		/**
		 * make instance
		 *
		 * @param channel parent channel
		 */
		protected FollowingUsersQuery(FollowingUsersChannel channel) {
			this.channel = channel;
		}

		/**
		 * get all followings
		 *
		 * @return followings
		 */
		public long[] getFollowings() {
			LongHashSet list = channel.followingList;
			return list.toArray();
		}

		/**
		 * check if the user is following userId
		 *
		 * @param userId user id
		 * @return is following?
		 */
		public boolean isFollowing(long userId) {
			return channel.followingList.contains(userId);
		}
	}

	private class FollowingUsersDispatcher extends ClientMessageAdapter {
		@Override
		public void onFriendList(long[] friendIds) {
			LongHashSet list = new LongHashSet();
			for (long friendId : friendIds) {
				list.add(friendId);
			}
			followingList = list;
		}
	}

	/**
	 * client message name when fetching blocking users is over
	 */
	public static final String FOLLOWING_USERS_FETCHED
			= "jp.mydns.turenar.twclient.bus.channel.FollowingUsersChannel followingFetched";
	/**
	 * dispatch to
	 */
	protected final ClientMessageListener listeners;
	/**
	 * message bus
	 */
	protected final MessageBus messageBus;
	/**
	 * account id (String)
	 */
	protected final String accountId;
	/**
	 * twitter instance: init in realConnect
	 */
	protected volatile Twitter twitter;
	/**
	 * actual user: init in connect from accountId
	 */
	protected volatile TwitterUser actualUser;
	/**
	 * following users list: operations must be synchronized
	 */
	protected volatile LongHashSet followingList = null;
	private FollowingUsersQuery query = new FollowingUsersQuery(this);

	/**
	 * インスタンスを生成する
	 *
	 * @param messageBus スケジューラー
	 * @param accountId  アカウントID (long)
	 * @param path       bus path
	 */
	public FollowingUsersChannel(MessageBus messageBus, String accountId, String path) {
		this.messageBus = messageBus;
		this.accountId = accountId;
		listeners = messageBus.getListeners(accountId, path);

		messageBus.establish(accountId, "stream/user", new FollowingUsersDispatcher());
	}

	@Override
	protected void access() throws TwitterException {
		long cursor = -1;
		LongHashSet newList = new LongHashSet(followingList == null ? 16 : followingList.size());
		do {
			IDs friendsIDs = twitter.getFriendsIDs(cursor);
			newList.addAll(friendsIDs.getIDs());
			cursor = friendsIDs.getNextCursor();
		} while (cursor != 0);
		followingList = newList;
		listeners.onClientMessage(FOLLOWING_USERS_FETCHED, query);
	}

	@Override
	public void connect() {
		initActualUser();
	}

	@Override
	public synchronized void disconnect() {
	}

	@Override
	public void establish(ClientMessageListener listener) {
		if (followingList != null) {
			synchronized (followingList) {
				query = new FollowingUsersQuery(this);
				listener.onClientMessage(FOLLOWING_USERS_FETCHED, query);
			}
		}
	}

	/**
	 * init actual user
	 */
	protected void initActualUser() {
		actualUser = configuration.getCacheManager().getUser(messageBus.getActualUser(accountId));
	}

	@Override
	public synchronized void realConnect() {
		twitter = new TwitterFactory(messageBus.getTwitterConfiguration(accountId)).getInstance();
		configuration.addJob(JobQueue.PRIORITY_HIGH, this);
	}
}

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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

import jp.mydns.turenar.twclient.ClientConfiguration;
import jp.mydns.turenar.twclient.ClientMessageAdapter;
import jp.mydns.turenar.twclient.ClientMessageListener;
import jp.mydns.turenar.twclient.JobQueue;
import jp.mydns.turenar.twclient.ParallelRunnable;
import jp.mydns.turenar.twclient.bus.MessageBus;
import jp.mydns.turenar.twclient.bus.MessageChannel;
import jp.mydns.turenar.twclient.twitter.TwitterUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.HttpResponseCode;
import twitter4j.PagableResponseList;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.UserList;

/**
 * ブロック中のユーザーを取得するDataFetcher。establish対応。
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class ListMembersChannel implements MessageChannel, ParallelRunnable {
	@FunctionalInterface
	private interface TwitterOperator<T> {
		T get(long cursor) throws TwitterException;
	}

	private class BlockingUserDispatcher extends ClientMessageAdapter {
		@Override
		public void onUserListMemberAddition(User addedMember, User listOwner, UserList list) {
			TwitterUser addedUser = TwitterUser.getInstance(addedMember);
			synchronized (ListMembersChannel.this) {
				members.add(addedUser);
			}
			listeners.onUserListMemberAddition(addedUser, listOwner, list);
		}

		@Override
		public void onUserListMemberDeletion(User deletedMember, User listOwner, UserList list) {
			TwitterUser deletedUser = TwitterUser.getInstance(deletedMember);
			synchronized (ListMembersChannel.this) {
				members.remove(deletedUser);
			}
			listeners.onUserListMemberDeletion(deletedUser, listOwner, list);
		}
	}


	/** client message name when fetching list members is over */
	public static final String MEMBERS_FETCH_FINISHED_ID
			= "jp.mydns.turenar.twclient.bus.channel.ListMembersChannel BlockingFetchFinished";
	/** client message name when fetching list members is started */
	public static final String MEMBERS_FETCH_STARTED_ID
			= "jp.mydns.turenar.twclient.bus.channel.ListMembersChannel BlockingFetchFinished";
	private static final int DEFAULT_LIFE = 3;
	private static final Logger logger = LoggerFactory.getLogger(ListMembersChannel.class);
	private final ClientConfiguration configuration;
	/**
	 * dispatch to
	 */
	private final ClientMessageListener listeners;
	/**
	 * message bus
	 */
	private final MessageBus messageBus;
	/**
	 * account id (String)
	 */
	private final String accountId;
	private final TwitterOperator<UserList> listFetcher;
	/**
	 * twitter instance: init in realConnect
	 */
	private volatile Twitter twitter;
	/**
	 * actual user: init in connect from accountId
	 */
	private volatile TwitterUser actualUser;
	/**
	 * list members
	 */
	private volatile List<TwitterUser> members;
	private volatile UserList userList;

	/**
	 * インスタンスを生成する
	 *
	 * @param messageBus スケジューラー
	 * @param accountId  アカウントID (long)
	 * @param path       bus path
	 */
	public ListMembersChannel(MessageBus messageBus, String accountId, String path, String listIdentifier) {
		this.messageBus = messageBus;
		this.accountId = accountId;
		listeners = messageBus.getListeners(accountId, path);

		configuration = ClientConfiguration.getInstance();
		messageBus.establish(accountId, "stream/user", new BlockingUserDispatcher());

		if (listIdentifier.startsWith(":")) {
			long listId = Long.parseLong(listIdentifier.substring(1));
			listFetcher = (cursor) -> twitter.showUserList(listId);
		} else {
			String slug;
			if (listIdentifier.startsWith("@")) {
				int indexOf = listIdentifier.indexOf('/');
				if (indexOf == -1) {
					throw new IllegalArgumentException(
							"specify listIdentifier as `:<listId>', `<listName>' or `@<listOwner>/<listName>'");
				}
				String listOwner = listIdentifier.substring(1, indexOf);
				slug = listIdentifier.substring(indexOf+1);
				listFetcher = (cursor) -> twitter.showUserList(listOwner, slug);
			} else {
				long listOwner = Long.parseLong(configuration.getAccountIdForRead());
				slug = listIdentifier;
				listFetcher = (cursor) -> twitter.showUserList(listOwner, slug);
			}
		}
	}

	private <T> T access(TwitterOperator<T> listFetcher, long cursor) throws TwitterException {
		int life = DEFAULT_LIFE;
		while (true) {
			try {
				return listFetcher.get(cursor);
			} catch (TwitterException e) {
				int statusCode = e.getStatusCode();
				// 500 == INTERNAL_SERVER_ERROR
				if (HttpResponseCode.INTERNAL_SERVER_ERROR <= statusCode) {
					if (--life <= 0) {
						logger.info("failed (over retry limit)", e);
					}
					continue; // with (prev) cursor
				} else if (statusCode == HttpResponseCode.NOT_FOUND) {
					logger.info("failed (Not Found)", e);
				} else {
					logger.warn("failed (status={})", statusCode, e);
				}					listeners.onException(e);
				throw e; // fail fetcher
			}
		}
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
		synchronized (this) {
			if (members != null) {
				listener.onClientMessage(MEMBERS_FETCH_STARTED_ID, null);
				for (User member : members) {
					listener.onUserListMemberAddition(member, userList.getUser(), userList);
				}
				listener.onClientMessage(MEMBERS_FETCH_FINISHED_ID, null);
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
		long interval = configuration.getConfigProperties().getTime("twitter.list_members.interval");
		configuration.getTimer().scheduleWithFixedDelay(() -> configuration.addJob(JobQueue.PRIORITY_HIGH, this),
				0, interval, TimeUnit.MILLISECONDS);
	}

	@Override
	public void run() {
		try {
			userList = access(listFetcher, 0);

			long cursor = -1;
			PagableResponseList<User> membersList;
			ArrayList<TwitterUser> members = new ArrayList<>(userList.getMemberCount());
			do {
				membersList = access((cur) -> twitter.getUserListMembers(userList.getId(), cur), cursor);
				membersList.stream()
						.map(TwitterUser::getInstance)
						.forEach(members::add);
				cursor = membersList.getNextCursor();
			} while (membersList.hasNext());

			synchronized (this) {
				List<TwitterUser> cachedMembers = this.members == null ? Collections.EMPTY_LIST : this.members;
				HashSet<TwitterUser> addedMembers = new HashSet<>(members);
				HashSet<TwitterUser> deletedMembers = new HashSet<>(cachedMembers);
				addedMembers.removeAll(cachedMembers);
				deletedMembers.removeAll(members);

				listeners.onClientMessage(MEMBERS_FETCH_STARTED_ID, null);
				addedMembers.forEach(user -> listeners.onUserListMemberAddition(user, userList.getUser(), userList));
				deletedMembers.forEach(user -> listeners.onUserListMemberDeletion(user, userList.getUser(), userList));
				this.members = members;
				listeners.onClientMessage(MEMBERS_FETCH_FINISHED_ID, null);
			}
		} catch (TwitterException ex) {
			// do nothing: already
		}
	}
}

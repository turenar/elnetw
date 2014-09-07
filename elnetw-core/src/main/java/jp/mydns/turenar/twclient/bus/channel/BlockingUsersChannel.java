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

import jp.mydns.turenar.twclient.ClientConfiguration;
import jp.mydns.turenar.twclient.ClientMessageAdapter;
import jp.mydns.turenar.twclient.ClientMessageListener;
import jp.mydns.turenar.twclient.JobQueue;
import jp.mydns.turenar.twclient.ParallelRunnable;
import jp.mydns.turenar.twclient.bus.MessageBus;
import jp.mydns.turenar.twclient.bus.MessageChannel;
import jp.mydns.turenar.twclient.internal.TwitterRunnable;
import jp.mydns.turenar.twclient.twitter.TwitterUser;
import twitter4j.PagableResponseList;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;

/**
 * ブロック中のユーザーを取得するDataFetcher。establish対応。
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class BlockingUsersChannel extends TwitterRunnable implements MessageChannel, ParallelRunnable {

	private class BlockingUserDispatcher extends ClientMessageAdapter {
		@Override
		public void onBlock(User source, User blockedUser) {

			synchronized (blockingUsers) {
				blockingUsers.add(TwitterUser.getInstance(blockedUser));
			}
			listeners.onBlock(source, blockedUser);
		}

		@Override
		public void onUnblock(User source, User unblockedUser) {
			synchronized (blockingUsers) {
				blockingUsers.remove(TwitterUser.getInstance(unblockedUser));
			}
			listeners.onUnblock(source, unblockedUser);
		}
	}


	/**
	 * client message name when fetching blocking users is over
	 */
	public static final String BLOCKING_FETCH_FINISHED_ID
			= "jp.mydns.turenar.twclient.bus.channel.BlockingUsersChannel BlockingFetchFinished";
	private final ClientConfiguration configuration;
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
	 * blocking users list: operations must be synchronized
	 */
	protected volatile ArrayList<TwitterUser> blockingUsers = null;

	/**
	 * インスタンスを生成する
	 *
	 * @param messageBus スケジューラー
	 * @param accountId  アカウントID (long)
	 * @param path       bus path
	 */
	public BlockingUsersChannel(MessageBus messageBus, String accountId, String path) {
		this.messageBus = messageBus;
		this.accountId = accountId;
		listeners = messageBus.getListeners(accountId, path);

		configuration = ClientConfiguration.getInstance();
		messageBus.establish(accountId, "stream/user", new BlockingUserDispatcher());
	}

	@Override
	protected void access() throws TwitterException {
		long cursor = -1;
		ArrayList<TwitterUser> blockingSet = new ArrayList<>();
		PagableResponseList<User> blocksIDs;
		do {
			blocksIDs = twitter.getBlocksList(cursor);
			for (User blockingUser : blocksIDs) {
				blockingSet.add(TwitterUser.getInstance(blockingUser));
				listeners.onBlock(actualUser, blockingUser);
			}
			cursor = blocksIDs.getNextCursor();
		} while (blocksIDs.hasNext());
		blockingUsers = blockingSet;
		listeners.onClientMessage(BLOCKING_FETCH_FINISHED_ID, null);
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
		if (blockingUsers != null) {
			synchronized (blockingUsers) {
				for (User blockingUser : blockingUsers) {
					listener.onBlock(actualUser, blockingUser);
				}
				listener.onClientMessage(BLOCKING_FETCH_FINISHED_ID, null);
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
	protected void onException(TwitterException ex) {
		listeners.onException(ex);
	}

	@Override
	public synchronized void realConnect() {
		twitter = new TwitterFactory(messageBus.getTwitterConfiguration(accountId)).getInstance();
		configuration.addJob(JobQueue.PRIORITY_HIGH, this);
	}
}

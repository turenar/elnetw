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

package jp.syuriken.snsw.twclient.bus;

import jp.syuriken.snsw.twclient.ClientConfiguration;
import jp.syuriken.snsw.twclient.ClientMessageListener;
import jp.syuriken.snsw.twclient.JobQueue;
import jp.syuriken.snsw.twclient.ParallelRunnable;
import jp.syuriken.snsw.twclient.internal.TwitterRunnable;
import jp.syuriken.snsw.twclient.twitter.TwitterUser;
import twitter4j.PagableResponseList;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;

/**
 * ブロック中のユーザーを取得するDataFetcher
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class BlockingUsersChannel extends TwitterRunnable implements MessageChannel, ParallelRunnable {

	private final ClientConfiguration configuration;
	private final ClientMessageListener listeners;
	private final MessageBus messageBus;
	private final String accountId;
	private volatile Twitter twitter;
	private TwitterUser actualUser;

	/**
	 * インスタンスを生成する
	 *
	 * @param messageBus スケジューラー
	 * @param accountId  アカウントID (long)
	 */
	public BlockingUsersChannel(MessageBus messageBus, String accountId, String path) {
		this.messageBus = messageBus;
		this.accountId = accountId;
		listeners = messageBus.getListeners(accountId, path);

		configuration = ClientConfiguration.getInstance();
	}

	@Override
	protected void access() throws TwitterException {
		long cursor = -1;
		PagableResponseList<User> blocksIDs;
		do {
			blocksIDs = twitter.getBlocksList(cursor);
			for (User blockingUser : blocksIDs) {
				listeners.onBlock(actualUser, blockingUser);
			}
			cursor = blocksIDs.getNextCursor();
		} while (blocksIDs.hasNext());
	}

	@Override
	public void connect() {
		actualUser = configuration.getCacheManager().getUser(messageBus.getActualUser(accountId));
	}

	@Override
	public synchronized void disconnect() {
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

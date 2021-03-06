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

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import jp.mydns.turenar.twclient.ClientConfiguration;
import jp.mydns.turenar.twclient.ClientMessageListener;
import jp.mydns.turenar.twclient.JobQueue;
import jp.mydns.turenar.twclient.ParallelRunnable;
import jp.mydns.turenar.twclient.bus.MessageBus;
import jp.mydns.turenar.twclient.bus.MessageChannel;
import jp.mydns.turenar.twclient.conf.ClientProperties;
import jp.mydns.turenar.twclient.internal.TwitterRunnable;
import twitter4j.DirectMessage;
import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;

/**
 * ダイレクトメッセージの取得をスケジュールしpeerに渡すDataFetcher。
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class DirectMessageChannel extends TwitterRunnable implements MessageChannel, ParallelRunnable {

	private final ClientConfiguration configuration;
	private final int intervalOfDirectMessage;
	private final ClientProperties configProperties;
	private final ClientMessageListener listeners;
	private final MessageBus messageBus;
	private final String accountId;
	private volatile ScheduledFuture<?> scheduledFuture;
	private volatile Twitter twitter;

	public DirectMessageChannel(MessageBus messageBus, String accountId) {
		this.messageBus = messageBus;
		this.accountId = accountId;
		listeners = messageBus.getListeners(accountId, "direct_messages");

		configuration = ClientConfiguration.getInstance();
		configProperties = configuration.getConfigProperties();
		intervalOfDirectMessage = configProperties.getInteger(
				ClientConfiguration.PROPERTY_INTERVAL_DIRECT_MESSAGES);
	}

	@Override
	protected void access() throws TwitterException {
		ResponseList<DirectMessage> directMessages = twitter.getDirectMessages(
				new Paging().count(configProperties.getInteger(ClientConfiguration.PROPERTY_PAGING_DIRECT_MESSAGES)));
		directMessages.forEach(listeners::onDirectMessage);
	}

	@Override
	public void connect() {
	}

	@Override
	public synchronized void disconnect() {
		if (scheduledFuture != null) {
			scheduledFuture.cancel(false);
			scheduledFuture = null;
		}
	}

	@Override
	public void establish(ClientMessageListener listener) {
	}

	@Override
	protected void onException(TwitterException ex) {
		listeners.onException(ex);
	}

	@Override
	public synchronized void realConnect() {
		if (scheduledFuture == null) {
			twitter = new TwitterFactory(
					messageBus.getTwitterConfiguration(accountId)).getInstance();

			scheduledFuture = configuration.getTimer().scheduleWithFixedDelay(
					() -> configuration.addJob(JobQueue.Priority.LOW, this),
					0, intervalOfDirectMessage, TimeUnit.SECONDS);
		}
	}
}

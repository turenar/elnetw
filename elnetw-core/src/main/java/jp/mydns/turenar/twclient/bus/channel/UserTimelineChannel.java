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
import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;

/**
 * users/timeline?{&lt;userId&gt;|@&lt;screenName&gt;}
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class UserTimelineChannel extends TwitterRunnable implements MessageChannel, ParallelRunnable {

	private final ClientConfiguration configuration;
	private final long interval;
	private final ClientProperties configProperties;
	private final ClientMessageListener listeners;
	private final MessageBus messageBus;
	private final String accountId;
	private String arg;
	private volatile ScheduledFuture<?> scheduledFuture;
	private volatile Twitter twitter;
	private volatile ResponseList<Status> lastTimeline;

	/**
	 * create instance
	 *
	 * @param messageBus bus
	 * @param accountId  account id
	 * @param path       channel path
	 * @param arg        argument
	 */
	public UserTimelineChannel(MessageBus messageBus, String accountId, String path, String arg) {
		if (arg == null || arg.isEmpty()) {
			throw new IllegalArgumentException(path + ": argument is required");
		}
		this.messageBus = messageBus;
		this.accountId = accountId;
		this.arg = arg;
		listeners = messageBus.getListeners(accountId, path);

		configuration = ClientConfiguration.getInstance();
		configProperties = configuration.getConfigProperties();
		interval = configProperties.getTime(
				ClientConfiguration.PROPERTY_INTERVAL_USER_TIMELINE, TimeUnit.SECONDS);
	}

	@Override
	protected void access() throws TwitterException {
		Paging paging = new Paging().count(configProperties.getInteger(ClientConfiguration.PROPERTY_PAGING_USER_TIMELINE));
		ResponseList<Status> timeline;
		if (arg.startsWith("@")) {
			timeline = twitter.getUserTimeline(arg.substring(1), paging);
		} else {
			timeline = twitter.getUserTimeline(Long.parseLong(arg), paging);
		}
		timeline.forEach(listeners::onStatus);
		lastTimeline = timeline;
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
		if (lastTimeline != null) {
			lastTimeline.forEach(listener::onStatus);
		}
	}

	@Override
	protected void onException(TwitterException ex) {
		listeners.onException(ex);
	}

	@Override
	public synchronized void realConnect() {
		if (scheduledFuture == null) {
			twitter = new TwitterFactory(messageBus.getTwitterConfiguration(accountId)).getInstance();

			scheduledFuture = configuration.getTimer().scheduleWithFixedDelay(
					() -> configuration.addJob(JobQueue.Priority.LOW, this),
					0, interval, TimeUnit.SECONDS);
		}
	}
}

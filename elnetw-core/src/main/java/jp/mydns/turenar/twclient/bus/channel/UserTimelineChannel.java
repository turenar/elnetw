package jp.mydns.turenar.twclient.bus.channel;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import jp.mydns.turenar.twclient.ClientConfiguration;
import jp.mydns.turenar.twclient.ClientMessageListener;
import jp.mydns.turenar.twclient.ClientProperties;
import jp.mydns.turenar.twclient.JobQueue;
import jp.mydns.turenar.twclient.bus.MessageBus;
import jp.mydns.turenar.twclient.bus.MessageChannel;
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
public class UserTimelineChannel extends TwitterRunnable implements MessageChannel {

	private final ClientConfiguration configuration;
	private final long intervalOfDirectMessage;
	private final ClientProperties configProperties;
	private final ClientMessageListener listeners;
	private final MessageBus messageBus;
	private final String accountId;
	private String arg;
	private volatile ScheduledFuture<?> scheduledFuture;
	private volatile Twitter twitter;
	private volatile ResponseList<Status> lastTimeline;

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
		intervalOfDirectMessage = configProperties.getTime(
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
		for (Status status : timeline) {
			listeners.onStatus(status);
		}
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
			for (Status status : lastTimeline) {
				listener.onStatus(status);
			}
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
					new Runnable() {
						@Override
						public void run() {
							configuration.addJob(JobQueue.Priority.LOW, UserTimelineChannel.this);
						}
					}, 0, intervalOfDirectMessage, TimeUnit.SECONDS);
		}
	}
}

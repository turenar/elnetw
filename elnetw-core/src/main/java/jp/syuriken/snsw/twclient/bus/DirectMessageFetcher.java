package jp.syuriken.snsw.twclient.bus;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import jp.syuriken.snsw.twclient.ClientConfiguration;
import jp.syuriken.snsw.twclient.ClientMessageListener;
import jp.syuriken.snsw.twclient.ClientProperties;
import jp.syuriken.snsw.twclient.JobQueue;
import jp.syuriken.snsw.twclient.internal.TwitterRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class DirectMessageFetcher extends TwitterRunnable implements MessageChannel {

	private static final Logger logger = LoggerFactory.getLogger(TimelineFetcher.class);
	private final ClientConfiguration configuration;
	private final int intervalOfDirectMessage;
	private final ClientProperties configProperties;
	private final ClientMessageListener listeners;
	private final MessageBus messageBus;
	private final String accountId;
	private volatile ScheduledFuture<?> scheduledFuture;
	private volatile Twitter twitter;

	public DirectMessageFetcher(MessageBus messageBus, String accountId) {
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
		for (DirectMessage dm : directMessages) {
			listeners.onDirectMessage(dm);
		}
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
	protected void onException(TwitterException ex) {
		listeners.onException(ex);
	}

	@Override
	public synchronized void realConnect() {
		if (scheduledFuture == null) {
			twitter = new TwitterFactory(
					messageBus.getTwitterConfiguration(accountId)).getInstance();

			scheduledFuture = configuration.getTimer().scheduleWithFixedDelay(new Runnable() {

				@Override
				public void run() {
					configuration.addJob(JobQueue.Priority.LOW, DirectMessageFetcher.this);
				}
			}, 0, intervalOfDirectMessage,
					TimeUnit.SECONDS);
		}
	}
}

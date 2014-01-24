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
import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;

/**
 * メンションを取得するDataFetcher
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class MentionsFetcher extends TwitterRunnable implements MessageChannel {

	private static final Logger logger = LoggerFactory.getLogger(MentionsFetcher.class);
	private final ClientConfiguration configuration;
	private final int intervalOfMentions;
	private final ClientProperties configProperties;
	private final ClientMessageListener listeners;
	private final MessageBus messageBus;
	private final String accountId;
	private volatile Twitter twitter;
	private volatile ScheduledFuture<?> scheduledFuture;

	/**
	 * インスタンスを生成する
	 *
	 * @param messageBus スケジューラー
	 * @param accountId  アカウントID (long)
	 */
	public MentionsFetcher(MessageBus messageBus, String accountId) {
		this.messageBus = messageBus;
		this.accountId = accountId;
		listeners = messageBus.getListeners(accountId, "statuses/mentions");

		configuration = ClientConfiguration.getInstance();
		configProperties = configuration.getConfigProperties();
		intervalOfMentions = configProperties.getInteger(ClientConfiguration.PROPERTY_INTERVAL_MENTIONS);
	}

	@Override
	protected synchronized void access() throws TwitterException {
		ResponseList<Status> mentions = twitter.getMentionsTimeline(
				new Paging().count(configProperties.getInteger(ClientConfiguration.PROPERTY_PAGING_MENTIONS)));
		for (Status status : mentions) {
			listeners.onStatus(status);
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
			twitter = new TwitterFactory(messageBus.getTwitterConfiguration(accountId)).getInstance();

			scheduledFuture = configuration.getTimer().scheduleWithFixedDelay(new Runnable() {

				@Override
				public void run() {
					configuration.addJob(JobQueue.Priority.LOW, MentionsFetcher.this);
				}
			}, 0, intervalOfMentions,
					TimeUnit.SECONDS);
		}
	}
}

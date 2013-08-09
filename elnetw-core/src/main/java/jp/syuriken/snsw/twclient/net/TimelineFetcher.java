package jp.syuriken.snsw.twclient.net;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import jp.syuriken.snsw.twclient.ClientConfiguration;
import jp.syuriken.snsw.twclient.ClientMessageListener;
import jp.syuriken.snsw.twclient.ClientProperties;
import jp.syuriken.snsw.twclient.JobQueue;
import jp.syuriken.snsw.twclient.internal.TwitterRunnable;
import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;

/**
 * home_timelineを取得するDataFetcher
 *
 * @Author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class TimelineFetcher extends TwitterRunnable implements DataFetcher {
	private final ClientConfiguration configuration;
	private final int intervalOfTimeline;
	private final ClientProperties configProperties;
	private final ClientMessageListener listeners;
	private final TwitterDataFetchScheduler twitterDataFetchScheduler;
	private final String accountId;
	private volatile Twitter twitter;
	private volatile ScheduledFuture<?> scheduledFuture;

	/**
	 * インスタンスを生成する
	 *
	 * @param twitterDataFetchScheduler スケジューラー
	 * @param accountId                 アカウントID (long)
	 */
	public TimelineFetcher(TwitterDataFetchScheduler twitterDataFetchScheduler, String accountId) {
		this.twitterDataFetchScheduler = twitterDataFetchScheduler;
		this.accountId = accountId;
		listeners = twitterDataFetchScheduler.getListeners(accountId, "statuses/timeline");

		configuration = ClientConfiguration.getInstance();
		configProperties = configuration.getConfigProperties();
		intervalOfTimeline = configProperties.getInteger(
				ClientConfiguration.PROPERTY_INTERVAL_TIMELINE);
	}

	@Override
	protected synchronized void access() throws TwitterException {
		ResponseList<Status> timeline = twitter.getHomeTimeline(
				new Paging().count(configProperties.getInteger(ClientConfiguration.PROPERTY_PAGING_TIMELINE)));
		for (Status status : timeline) {
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
			twitter = new TwitterFactory(twitterDataFetchScheduler.getTwitterConfiguration(accountId)).getInstance();

			scheduledFuture = configuration.getTimer().scheduleWithFixedDelay(new Runnable() {

				@Override
				public void run() {
					configuration.addJob(JobQueue.Priority.LOW, TimelineFetcher.this);
				}
			}, 0, intervalOfTimeline, TimeUnit.SECONDS);
		}
	}
}

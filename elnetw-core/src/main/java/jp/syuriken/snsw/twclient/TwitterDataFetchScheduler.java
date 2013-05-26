package jp.syuriken.snsw.twclient;

import java.util.TimerTask;

import jp.syuriken.snsw.twclient.internal.InitialMessage;
import jp.syuriken.snsw.twclient.internal.TwitterRunnable;
import twitter4j.DirectMessage;
import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;

/**
 * Twitterからの情報を取得するためのスケジューラ
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class TwitterDataFetchScheduler {

	private final class FirstDirectMessageFetcher extends TwitterRunnable implements ParallelRunnable {

		@Override
		protected void access() throws TwitterException {
			ResponseList<DirectMessage> directMessages;
			Paging paging =
					new Paging().count(configProperties
							.getInteger(ClientConfiguration.PROPERTY_PAGING_INITIAL_DIRECTMESSAGE));
			directMessages = twitterForRead.getDirectMessages(paging);
			for (DirectMessage directMessage : directMessages) {
				rootFilterService.onDirectMessage(new InitialMessage(directMessage));
			}
		}
	}

	private final class FirstMentionFetcher extends TwitterRunnable implements ParallelRunnable {

		@Override
		public void access() throws TwitterException {
			Paging paging =
					new Paging()
							.count(configProperties.getInteger(ClientConfiguration.PROPERTY_PAGING_INITIAL_MENTION));
			ResponseList<Status> mentions = twitterForRead.getMentionsTimeline(paging);
			for (Status status : mentions) {
				TwitterStatus twitterStatus = new TwitterStatus(status);
				twitterStatus.setLoadedInitialization(true);
				rootFilterService.onStatus(twitterStatus);
			}
		}
	}

	private final class FirstTimelineFetcher extends TwitterRunnable {

		@Override
		protected void access() throws TwitterException {
			ResponseList<Status> homeTimeline;
			Paging paging =
					new Paging().count(configProperties
							.getInteger(ClientConfiguration.PROPERTY_PAGING_INITIAL_TIMELINE));
			homeTimeline = twitterForRead.getHomeTimeline(paging);
			for (Status status : homeTimeline) {
				TwitterStatus twitterStatus = new TwitterStatus(status);
				twitterStatus.setLoadedInitialization(true);
				rootFilterService.onStatus(twitterStatus);
			}
		}
	}

	private final class HomeTimelineFetcher extends TwitterRunnable implements ParallelRunnable {

		@Override
		protected void access() throws TwitterException {
			Paging paging =
					new Paging().count(configProperties.getInteger(ClientConfiguration.PROPERTY_PAGING_TIMELINE));
			ResponseList<Status> timeline;
			timeline = twitterForRead.getHomeTimeline(paging);
			for (Status status : timeline) {
				rootFilterService.onStatus(status);
			}
		}
	}

	/*package*/final FilterService rootFilterService;

	/*package*/final ClientConfiguration configuration;

	/*package*/ Twitter twitterForRead;

	/*package*/ TwitterStream stream;

	/*package*/ ClientProperties configProperties;


	/**
	 * インスタンスを生成する。
	 *
	 * @param configuration 設定
	 */
	/*package*/TwitterDataFetchScheduler(final ClientConfiguration configuration) {
		this.configuration = configuration;
		configProperties = configuration.getConfigProperties();
		twitterForRead = configuration.getTwitterForRead();
		rootFilterService = configuration.getRootFilterService();

		scheduleFirstTimeline();
		scheduleFirstMentions();
		scheduleFirstDirectMessage();
		scheduleGettingTimeline();
		onChangeAccount(true);
		onChangeAccount(false);
	}

	/** お掃除する */
	public void cleanUp() {
		stream.shutdown();
	}

	/**
	 * アカウント変更通知
	 *
	 * @param forWrite 書き込み用アカウントが変更されたかどうか。
	 */
	public void onChangeAccount(boolean forWrite) {
		if (forWrite) {
			reloginForWrite(configuration.getAccountIdForWrite());
		} else {
			reloginForRead(configuration.getAccountIdForRead());
		}
	}

	private void reloginForRead(String accountId) {
		twitterForRead = configuration.getTwitterForRead();
		if (stream != null) {
			final TwitterStream oldStream = stream;
			new Thread(new Runnable() {

				@Override
				public void run() {
					oldStream.cleanUp();
				}
			}, "stream disconnector").start();
		}
		stream = new TwitterStreamFactory(configuration.getTwitterConfiguration(accountId)).getInstance();
		stream.addConnectionLifeCycleListener(rootFilterService);
		stream.addListener(rootFilterService);
		stream.user();
	}

	private void reloginForWrite(String accountId) {
		// do nothing
	}

	private void scheduleFirstDirectMessage() {
		configuration.addJob(new FirstDirectMessageFetcher());
	}

	private void scheduleFirstMentions() {
		configuration.addJob(new FirstMentionFetcher());
	}

	private void scheduleFirstTimeline() {
		configuration.addJob(new FirstTimelineFetcher());
	}

	private void scheduleGettingTimeline() {
		configuration.getTimer().schedule(new TimerTask() {

			@Override
			public void run() {
				configuration.addJob(new HomeTimelineFetcher());
			}
		}, configProperties.getInteger(ClientConfiguration.PROPERTY_INTERVAL_TIMELINE),
				configProperties.getInteger(ClientConfiguration.PROPERTY_INTERVAL_TIMELINE));
	}
}

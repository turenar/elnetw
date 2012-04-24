package jp.syuriken.snsw.twclient;

import java.util.TimerTask;

import jp.syuriken.snsw.twclient.ClientConfiguration.ConfigData;
import jp.syuriken.snsw.twclient.internal.TwitterRunnable;
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
 * @author $Author$
 */
public class TwitterDataFetchScheduler {
	
	private final ClientFrameApi frameApi;
	
	private final ConfigData configData;
	
	private Twitter twitterForRead;
	
	private final FilterService rootFilterService;
	
	private final ClientConfiguration configuration;
	
	private TwitterStream stream;
	
	
	/**
	 * インスタンスを生成する。
	 * 
	 * @param configuration 設定
	 */
	/*package*/TwitterDataFetchScheduler(final ClientConfiguration configuration) {
		this.configuration = configuration;
		frameApi = configuration.getFrameApi();
		configData = configuration.getConfigData();
		twitterForRead = configuration.getTwitterForRead();
		rootFilterService = configuration.getRootFilterService();
		
		scheduleFirstTimeline();
		scheduleFirstMentions();
		scheduleGettingTimeline();
		onChangeAccount(true);
		onChangeAccount(false);
	}
	
	/**
	 * お掃除する
	 */
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
	
	private void scheduleFirstMentions() {
		frameApi.addJob(new TwitterRunnable() {
			
			@Override
			public void access() throws TwitterException {
				try {
					Paging paging = configData.pagingOfGettingInitialMentions;
					ResponseList<Status> mentions = twitterForRead.getMentions(paging);
					for (Status status : mentions) {
						TwitterStatus twitterStatus = new TwitterStatus(status);
						twitterStatus.setLoadedInitialization(true);
						rootFilterService.onStatus(twitterStatus);
					}
				} finally {
					configuration.setInitializing(false);
				}
			}
			
			@Override
			protected ClientConfiguration getConfiguration() {
				return configuration;
			}
		});
	}
	
	private void scheduleFirstTimeline() {
		frameApi.addJob(new TwitterRunnable() {
			
			@Override
			protected void access() throws TwitterException {
				ResponseList<Status> homeTimeline;
				Paging paging = configData.pagingOfGettingInitialTimeline;
				homeTimeline = twitterForRead.getHomeTimeline(paging);
				for (Status status : homeTimeline) {
					TwitterStatus twitterStatus = new TwitterStatus(status);
					twitterStatus.setLoadedInitialization(true);
					rootFilterService.onStatus(twitterStatus);
				}
			}
			
			@Override
			protected ClientConfiguration getConfiguration() {
				return configuration;
			}
		});
	}
	
	private void scheduleGettingTimeline() {
		frameApi.getTimer().schedule(new TimerTask() {
			
			@Override
			public void run() {
				frameApi.addJob(new TwitterRunnable() {
					
					@Override
					protected void access() throws TwitterException {
						Paging paging = configData.pagingOfGettingTimeline;
						ResponseList<Status> timeline;
						timeline = twitterForRead.getHomeTimeline(paging);
						for (Status status : timeline) {
							rootFilterService.onStatus(status);
						}
					}
					
					@Override
					protected ClientConfiguration getConfiguration() {
						return configuration;
					}
				});
			}
		}, configData.intervalOfGetTimeline, configData.intervalOfGetTimeline);
	}
	
}

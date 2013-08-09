package jp.syuriken.snsw.twclient.net;

import jp.syuriken.snsw.twclient.ClientMessageListener;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;

/**
 * ストリームからデータを取得するDataFetcher
 *
 * @Author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class StreamFetcher implements DataFetcher {
	private final String accountId;
	private final ClientMessageListener listener;
	private final TwitterDataFetchScheduler twitterDataFetchScheduler;
	private volatile TwitterStream stream;

	public StreamFetcher(TwitterDataFetchScheduler twitterDataFetchScheduler, String accountId) {
		this.twitterDataFetchScheduler = twitterDataFetchScheduler;
		this.accountId = accountId;
		listener = twitterDataFetchScheduler.getListeners(accountId, "stream/user");
	}

	@Override
	public synchronized void connect() {
		if (stream == null) {
			stream = new TwitterStreamFactory(
					twitterDataFetchScheduler.getTwitterConfiguration(accountId)).getInstance();
			stream.addConnectionLifeCycleListener(listener);
			stream.addListener(listener);
			stream.user();
		}
	}

	@Override
	public synchronized void disconnect() {
		if (stream != null) {
			stream.shutdown();
			stream = null;
		}
	}

	@Override
	public void realConnect() {
		// #connect() works.
	}
}

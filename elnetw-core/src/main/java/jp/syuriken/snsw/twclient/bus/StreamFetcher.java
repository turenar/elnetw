package jp.syuriken.snsw.twclient.bus;

import jp.syuriken.snsw.twclient.ClientMessageListener;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;

/**
 * ストリームからデータを取得するDataFetcher
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class StreamFetcher implements MessageChannel {
	private final String accountId;
	private final ClientMessageListener listener;
	private final MessageBus messageBus;
	private volatile TwitterStream stream;

	public StreamFetcher(MessageBus messageBus, String accountId) {
		this.messageBus = messageBus;
		this.accountId = accountId;
		listener = messageBus.getListeners(accountId, "stream/user");
	}

	@Override
	public synchronized void connect() {
		if (stream == null) {
			stream = new TwitterStreamFactory(
					messageBus.getTwitterConfiguration(accountId)).getInstance();
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

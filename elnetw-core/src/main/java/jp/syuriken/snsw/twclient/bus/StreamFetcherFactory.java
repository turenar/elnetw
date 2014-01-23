package jp.syuriken.snsw.twclient.bus;

/**
 * {@link StreamFetcher}のファクトリークラス
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class StreamFetcherFactory implements MessageChannelFactory {
	@Override
	public MessageChannel getInstance(MessageBus messageBus, String accountId, String path) {
		return new StreamFetcher(messageBus, accountId);
	}
}

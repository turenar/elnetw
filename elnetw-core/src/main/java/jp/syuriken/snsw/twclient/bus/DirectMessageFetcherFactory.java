package jp.syuriken.snsw.twclient.bus;

/**
 * {@link DirectMessageFetcher}のためのファクトリークラス
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class DirectMessageFetcherFactory implements MessageChannelFactory {
	@Override
	public MessageChannel getInstance(MessageBus messageBus, String accountId, String path) {
		return new DirectMessageFetcher(messageBus, accountId);
	}
}

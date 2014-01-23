package jp.syuriken.snsw.twclient.bus;

/**
 * {@link TimelineFetcher}のファクトリークラス
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class TimelineFetcherFactory implements MessageChannelFactory {
	@Override
	public MessageChannel getInstance(MessageBus messageBus, String accountId, String path) {
		return new TimelineFetcher(messageBus, accountId);
	}
}

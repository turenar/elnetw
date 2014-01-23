package jp.syuriken.snsw.twclient.bus;

/**
 * {@link MentionsFetcher}のためのファクトリークラス
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class MentionsFetcherFactory implements MessageChannelFactory {
	@Override
	public MessageChannel getInstance(MessageBus messageBus, String accountId, String path) {
		return new MentionsFetcher(messageBus, accountId);
	}
}

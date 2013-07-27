package jp.syuriken.snsw.twclient.net;

/**
 * {@link DirectMessageFetcher}のためのファクトリークラス
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class DirectMessageFetcherFactory implements DataFetcherFactory {
	@Override
	public DataFetcher getInstance(TwitterDataFetchScheduler scheduler, String accountId, String path) {
		return new DirectMessageFetcher(scheduler, accountId);
	}
}

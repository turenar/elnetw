package jp.syuriken.snsw.twclient.net;

/**
 * {@link StreamFetcher}のファクトリークラス
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class StreamFetcherFactory implements DataFetcherFactory {
	@Override
	public DataFetcher getInstance(TwitterDataFetchScheduler scheduler, String accountId, String path) {
		return new StreamFetcher(scheduler, accountId);
	}
}

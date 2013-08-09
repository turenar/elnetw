package jp.syuriken.snsw.twclient.net;

/**
 * {@link TimelineFetcher}のファクトリークラス
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class TimelineFetcherFactory implements DataFetcherFactory {
	@Override
	public DataFetcher getInstance(TwitterDataFetchScheduler scheduler, String accountId, String path) {
		return new TimelineFetcher(scheduler, accountId);
	}
}

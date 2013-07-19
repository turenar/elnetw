package jp.syuriken.snsw.twclient.net;

/**
 * {@link MentionsFetcher}のためのファクトリークラス
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class MentionsFetcherFactory implements DataFetcherFactory {
	@Override
	public DataFetcher getInstance(TwitterDataFetchScheduler scheduler, String accountId, String path) {
		return new MentionsFetcher(scheduler, accountId);
	}
}

package jp.syuriken.snsw.twclient.net;

/**
 * {@link DataFetcher}を作成するファクトリークラス
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public interface DataFetcherFactory {
	/**
	 * インスタンスを生成する
	 *
	 * @param scheduler スケジューラー
	 * @param accountId アカウントID (long)
	 * @param path      "my/timeline"など
	 * @return DataFetcherインスタンス。nullを投げるくらいなら例外をくれ
	 */
	DataFetcher getInstance(TwitterDataFetchScheduler scheduler, String accountId, String path);
}

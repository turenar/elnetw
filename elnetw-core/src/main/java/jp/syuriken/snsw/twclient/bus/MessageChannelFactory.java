package jp.syuriken.snsw.twclient.bus;

/**
 * {@link MessageChannel}を作成するファクトリークラス
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public interface MessageChannelFactory {
	/**
	 * インスタンスを生成する
	 *
	 * @param messageBus スケジューラー
	 * @param accountId  アカウントID (long)
	 * @param path       "my/timeline"など
	 * @return DataFetcherインスタンス。nullを投げるくらいなら例外をくれ
	 */
	MessageChannel getInstance(MessageBus messageBus, String accountId, String path);
}

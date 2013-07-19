package jp.syuriken.snsw.twclient.net;

/**
 * Twitterからデータを取得して他のハンドラに渡すためのインターフェース
 *
 * @Author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public interface DataFetcher {
	/**
	 * このDataFetcherが使用されるため、コネクションの準備をしてください。
	 *
	 * <p>
	 * このメソッドが呼ばれるのはClientTabが復元される前なので、
	 * このメソッドでのハンドラに渡すようなデータを取得するためのREST APIの使用は避けるべき。
	 * </p>
	 *
	 * @see #realConnect()
	 */
	void connect();

	/** このDataFetcherが使用されなくなったので、もう通知する必要がなくなった */
	void disconnect();

	/**
	 * このDataFetcherが使用されるので、データの取得を開始してください。
	 *
	 * <p>
	 * このメソッドが呼ばれるのはClientTabが復元されたあとなので、
	 * このメソッドでREST APIを使用するのは一向に構わない。(ただしスレッドをブロックしないよう注意)
	 * </p>
	 */
	void realConnect();
}

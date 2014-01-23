package jp.syuriken.snsw.twclient.bus;

/**
 * Twitterからデータを取得して他のハンドラに渡すためのインターフェース
 *
 * @Author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public interface MessageChannel {
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

	/**
	 * このDataFetcherが使用されなくなったので、もう通知する必要がなくなった。
	 * <p>
	 * accountIdが
	 * {@link MessageBus#READER_ACCOUNT_ID} {@link MessageBus#WRITER_ACCOUNT_ID}で、
	 * 読み込み用/書き込み用アカウントが変更された時もこのメソッドが呼び出されます。
	 * この場合は続いて {@link #connect()} {@link #realConnect()}(アプリケーションの初期化が完了しているのみ)が呼び出されますが、
	 * 呼ばれた理由がアカウント変更であるかを取得できるメソッドは現在のところありません。
	 * </p>
	 */
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

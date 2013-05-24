package jp.syuriken.snsw.twclient.internal;

import jp.syuriken.snsw.twclient.ClientConfiguration;
import twitter4j.TwitterException;

/**
 * Twitterがダウンしてる時か過負荷のときに再試行するユーティリティクラス。
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public abstract class TwitterRunnable implements Runnable {

	private int life = 10;

	private boolean intoQueue;

	protected ClientConfiguration configuration;


	/**
	 * インスタンスを生成する。失敗時はジョブキューに追加する。
	 */
	public TwitterRunnable() {
		this(true);
	}

	/**
	 * インスタンスを生成する。
	 *
	 * @param intoQueue 失敗時にジョブキューに追加するかどうか
	 */
	public TwitterRunnable(boolean intoQueue) {
		this.intoQueue = intoQueue;
		configuration = ClientConfiguration.getInstance();
	}

	/**
	 * Twitterへのアクセス
	 * @throws TwitterException Twitterへのアクセス中に発生した例外
	 */
	protected abstract void access() throws TwitterException;

	/**
	 * 設定を取得する
	 * @return 設定
	 * @deprecated use {@link #configuration}
	 */
	@Deprecated
	protected ClientConfiguration getConfiguration(){
		return configuration;
	}

	/**
	 * 例外のハンドリング
	 *
	 * @param ex 例外
	 */
	protected void handleException(TwitterException ex) {
		configuration.getFrameApi().handleException(ex);
	}

	@Override
	public void run() {
		life--;
		try {
			access();
		} catch (TwitterException ex) {
			int statusCode = ex.getStatusCode();
			if ((502 <= statusCode && statusCode <= 504) && life >= 0) {
				// Twitter is down or overloaded
				if (intoQueue) {
					configuration.addJob(this);
				} else {
					run();
				}
			} else {
				handleException(ex);
			}
		}
	}
}

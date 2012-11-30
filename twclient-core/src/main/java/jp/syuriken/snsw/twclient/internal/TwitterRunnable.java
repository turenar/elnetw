package jp.syuriken.snsw.twclient.internal;

import jp.syuriken.snsw.twclient.ClientConfiguration;
import twitter4j.TwitterException;

/**
 * 503のときに再試行するユーティリティクラス。
 * 
 * @author $Author$
 */
public abstract class TwitterRunnable implements Runnable {
	
	private int life = 10;
	
	private boolean intoQueue;
	
	
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
	}
	
	/**
	 * Twitterへのアクセス
	 * @throws TwitterException Twitterへのアクセス中に発生した例外
	 */
	protected abstract void access() throws TwitterException;
	
	/**
	 * 設定を取得する
	 * @return 設定
	 */
	protected abstract ClientConfiguration getConfiguration();
	
	/**
	 * 例外のハンドリング
	 * 
	 * @param ex 例外
	 */
	protected void handleException(TwitterException ex) {
		getConfiguration().getFrameApi().handleException(ex);
	}
	
	@Override
	public void run() {
		life--;
		try {
			access();
		} catch (TwitterException ex) {
			if (ex.getStatusCode() == 503 && life >= 0) {
				if (intoQueue) {
					getConfiguration().getFrameApi().addJob(this);
				} else {
					run();
				}
			} else {
				handleException(ex);
			}
		}
	}
}

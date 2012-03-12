package jp.syuriken.snsw.twclient.internal;

import jp.syuriken.snsw.twclient.ClientConfiguration;
import jp.syuriken.snsw.twclient.ParallelRunnable;
import twitter4j.TwitterException;

/**
 * 503のときに再試行するユーティリティクラス。
 * 
 * @author $Author$
 */
public abstract class TwitterRunnable implements ParallelRunnable {
	
	private int life = 10;
	
	
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
	protected abstract void handleException(TwitterException ex);
	
	@Override
	public void run() {
		life--;
		try {
			access();
		} catch (TwitterException ex) {
			if (ex.getStatusCode() == 503 && life >= 0) {
				getConfiguration().getFrameApi().addJob(this);
			} else {
				handleException(ex);
			}
		}
	}
}

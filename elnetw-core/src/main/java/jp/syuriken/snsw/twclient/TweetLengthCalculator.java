package jp.syuriken.snsw.twclient;

/**
 * ツイートの長さを計算するインターフェース
 *
 * @author Turenar <snswinhaiku dot lo at gmail dot com>
 */
public interface TweetLengthCalculator {

	/** ツイートの最大長 */
	/*public*/static final int MAX_TWEET_LENGTH = 140;

	/** 警告を発するツイートの長さ */
	/*public*/static final int WARN_TWEET_LENGTH = 120;


	/**
	 * ツイートの長さを計算する
	 *
	 * @param original 投稿する文字列
	 */
	void calcTweetLength(String original);

	/**
	 * 短縮されたツイートのテキストを取得する
	 *
	 * @param original 投稿する文字列
	 * @return 短縮されたテキスト。短縮できない場合等はoriginalをそのまま返す
	 */
	String getShortenedText(String original);
}

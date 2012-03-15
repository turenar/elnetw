package jp.syuriken.snsw.twclient.internal;

import java.awt.Color;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jp.syuriken.snsw.twclient.TweetLengthCalculator;

/**
 * デフォルトのツイートの長さを計算するクラス。URL変換を行った上での長さを計算する。
 * 
 * @author $Author$
 */
public class DefaultTweetLengthCalculator implements TweetLengthCalculator {
	
	private final TweetLengthUpdater updater;
	
	/** URLパターン */
	public static Pattern urlPattern = Pattern.compile("https?://\\S+");
	
	
	/**
	 * ツイートの長さを取得する。URL変換を行う。
	 * 
	 * @param original オリジナルテキスト
	 * @return これぐらいの長さになりそうという長さ
	 */
	protected static int getTweetLength(String original) {
		int length = original.length();
		Matcher matcher = urlPattern.matcher(original);
		while (matcher.find()) {
			length = length - (matcher.end() - matcher.start()) + 20;
		}
		return length;
	}
	
	/**
	 * インスタンスを生成する。
	 * 
	 * @param updater 操作API
	 */
	public DefaultTweetLengthCalculator(TweetLengthUpdater updater) {
		this.updater = updater;
	}
	
	@Override
	public void calcTweetLength(String original) {
		int length = getTweetLength(original);
		Color color;
		if (length > MAX_TWEET_LENGTH) {
			color = Color.RED;
		} else if (length > WARN_TWEET_LENGTH) {
			color = Color.ORANGE;
		} else {
			color = Color.BLUE;
		}
		updater.updatePostLength(String.valueOf(length), color, null);
	}
	
	@Override
	public String getShortenedText(String original) {
		return original;
	}
	
}

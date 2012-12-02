package jp.syuriken.snsw.twclient.internal;

import java.awt.Color;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jp.syuriken.snsw.twclient.TweetLengthCalculator;

import com.twitter.Regex;

/**
 * デフォルトのツイートの長さを計算するクラス。URL変換を行った上での長さを計算する。
 * 
 * @author Turenar <snswinhaiku dot lo at gmail dot com>
 */
public class DefaultTweetLengthCalculator implements TweetLengthCalculator {
	
	private final TweetLengthUpdater updater;
	
	/** URLパターン */
	public static Pattern urlPattern = Regex.VALID_URL;
	
	
	/**
	 * ツイートの長さを取得する。URL変換を行う。
	 * 
	 * @param original オリジナルテキスト
	 * @return これぐらいの長さになりそうという長さ
	 */
	public static int getTweetLength(String original) {
		int length = original.length();
		Matcher matcher = urlPattern.matcher(original);
		while (matcher.find()) {
			int start = matcher.start(Regex.VALID_URL_GROUP_URL);
			int end = matcher.end(Regex.VALID_URL_GROUP_URL);
			int fat = end - start - 20;
			length -= fat;
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

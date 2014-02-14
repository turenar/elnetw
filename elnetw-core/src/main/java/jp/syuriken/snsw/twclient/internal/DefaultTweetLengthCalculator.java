/*
 * The MIT License (MIT)
 * Copyright (c) 2011-2014 Turenai Project
 *
 * Permission is hereby granted, free of charge,
 *  to any person obtaining a copy of this software and associated documentation files (the "Software"),
 *  to deal in the Software without restriction, including without limitation the rights to
 *  use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 *  and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 *  INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 *  IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 *  DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package jp.syuriken.snsw.twclient.internal;

import java.awt.Color;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.twitter.Regex;
import jp.syuriken.snsw.twclient.ClientConfiguration;
import jp.syuriken.snsw.twclient.TweetLengthCalculator;
import twitter4j.TwitterAPIConfiguration;

/**
 * デフォルトのツイートの長さを計算するクラス。URL変換を行った上での長さを計算する。
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class DefaultTweetLengthCalculator implements TweetLengthCalculator {

	/** URLパターン */
	public static final Pattern urlPattern = Regex.VALID_URL;
	private static TwitterAPIConfiguration apiConfiguration;

	/**
	 * テスト以外に使用してはならない。
	 * APIConfigurationの内部キャッシュを削除する。
	 */
	/*package*/
	static void clearApiConfiguration() {
		apiConfiguration = null;
	}

	/**
	 * ツイートの長さを取得する。URL変換を行う。
	 *
	 * @param original オリジナルテキスト
	 * @return これぐらいの長さになりそうという長さ
	 */
	public static int getTweetLength(String original) {
		if (apiConfiguration == null) {
			apiConfiguration = ClientConfiguration.getInstance().getMessageBus().getApiConfiguration();
		}

		final int shortURLLength = apiConfiguration == null ? DEFAULT_SHORT_URL_LENGTH
				: apiConfiguration.getShortURLLength();
		final int shortURLLengthHttps = apiConfiguration == null ? DEFAULT_SHORT_URL_LENGTH_HTTPS
				: apiConfiguration.getShortURLLengthHttps();

		int length = original.length();
		Matcher matcher = urlPattern.matcher(original);
		while (matcher.find()) {
			int start = matcher.start(Regex.VALID_URL_GROUP_URL);
			int end = matcher.end(Regex.VALID_URL_GROUP_URL);
			String protocol = matcher.group(Regex.VALID_URL_GROUP_PROTOCOL);
			// protocol can be null if not specified, and case insensitive
			int newUrlLength = "https://".equalsIgnoreCase(protocol) ? shortURLLengthHttps : shortURLLength;
			int fat = (end - start) - newUrlLength;
			length -= fat;
		}
		return length;
	}

	private final TweetLengthUpdater updater;

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

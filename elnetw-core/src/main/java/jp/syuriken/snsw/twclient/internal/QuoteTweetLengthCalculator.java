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
 * QTされた時用のツイートの長さを計算するクラス
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class QuoteTweetLengthCalculator implements TweetLengthCalculator {

	private static final Pattern qtPattern = Pattern.compile("[QR]T\\s?@[a-zA-Z0-9_]{1,20}:?+");
	private static final Pattern tokenPattern = Pattern.compile( //
			"(?:" + Regex.VALID_URL + "|" //
					+ Regex.VALID_HASHTAG + "|" //
					+ Regex.VALID_MENTION_OR_LIST + ")", Pattern.CASE_INSENSITIVE
	);
	private static final Pattern urlPattern = Regex.VALID_URL;
	private final TweetLengthUpdater updater;
	private TwitterAPIConfiguration apiConfiguration;
	private int shortURLLength = DEFAULT_SHORT_URL_LENGTH;
	private int shortURLLengthHttps = DEFAULT_SHORT_URL_LENGTH_HTTPS;


	/**
	 * インスタンスを生成する。
	 *
	 * @param api 操作用API
	 */
	public QuoteTweetLengthCalculator(TweetLengthUpdater api) {
		updater = api;
	}

	@Override
	public void calcTweetLength(String original) {
		initUrlLength();
		int length = original.length();
		boolean shortened = false; // 短縮するかどうか
		Matcher qtMatcher = qtPattern.matcher(original);
		if (qtMatcher.find()) { // "QT "が見つかる
			int qtIndex = qtMatcher.end();
			Matcher matcher = urlPattern.matcher(original);
			int fat = 0;
			int fatBeforeQT = 0;
			while (matcher.find()) { //calculate fat
				int start = matcher.start(Regex.VALID_URL_GROUP_URL);
				int end = matcher.end(Regex.VALID_URL_GROUP_URL);
				String protocol = matcher.group(Regex.VALID_URL_GROUP_PROTOCOL);
				// protocol can be null if not specified, and case insensitive
				int newUrlLength = "https://".equalsIgnoreCase(protocol) ? shortURLLengthHttps : shortURLLength;
				fat += (end - start) - newUrlLength;
				if (end < qtIndex) {
					fatBeforeQT = fat;
				}
			}
			if (length - fat > MAX_TWEET_LENGTH) { // 140文字を超えそう
				shortened = true;
				length = qtMatcher.end() - fatBeforeQT;
			} else { // 超えない
				length -= fat;
			}
		} else {
			length = DefaultTweetLengthCalculator.getTweetLength(original);
		}

		Color color;
		if (length > MAX_TWEET_LENGTH) {
			color = Color.RED;
		} else if (length > WARN_TWEET_LENGTH) {
			color = Color.ORANGE;
		} else {
			color = Color.BLUE;
		}
		if (shortened) {
			updater.updatePostLength(length + "+", color, "短縮されます (実際の投稿は" + getShortenedText(original).length()
					+ "文字です)");
		} else {
			updater.updatePostLength(String.valueOf(length), color, null);
		}
	}

	@Override
	public String getShortenedText(String original) {
		initUrlLength();
		final Matcher qtMatcher = qtPattern.matcher(original);
		if (original.length() <= MAX_TWEET_LENGTH || !qtMatcher.find()) {
			return original; // not short-able or not QT Pattern
		}
		int qtTokenStart = qtMatcher.start(); // "QT"が始まる場所
		int qtTokenEnd = qtMatcher.end(); // "QT"が終わる場所
		int userTextLength = DefaultTweetLengthCalculator.getTweetLength(original.substring(0, qtTokenStart));
		int qtLength = qtTokenEnd - qtTokenStart; // "QT @****:"の長さ

		Matcher tokenMatcher = tokenPattern.matcher(original.substring(qtTokenEnd));
		int remainLength = MAX_TWEET_LENGTH - userTextLength - qtLength; // 投稿できそうな残り長さ
		if (remainLength <= 0) { // "QT @****:"を残すと投稿でき無さそうなときは、"QT @****:"を消さないで、投稿を試してみる
			return original.substring(0, qtTokenEnd);
		}
		int fat = 0; // urlがt.coに変わった時に減る文字数
		int oldFat;
		while (tokenMatcher.find()) {
			oldFat = fat;
			int start = tokenMatcher.start();
			int end = tokenMatcher.end();
			// check url
			Matcher urlMatcher = urlPattern.matcher(tokenMatcher.group());
			if (urlMatcher.find()) {
				start += urlMatcher.start(Regex.VALID_URL_GROUP_URL); // skip preceeding char
				String protocol = urlMatcher.group(Regex.VALID_URL_GROUP_PROTOCOL);
				// protocol can be null if not specified, and case insensitive
				int newUrlLength = "https://".equalsIgnoreCase(protocol) ? shortURLLengthHttps : shortURLLength;
				fat += (end - start) - newUrlLength;
			} else { // skip preceeding char
				Matcher hashtagMatcher = Regex.VALID_HASHTAG.matcher(tokenMatcher.group());
				if (hashtagMatcher.find()) {
					start += hashtagMatcher.start(Regex.VALID_HASHTAG_GROUP_HASH);
				} else {
					Matcher mentionMatcher = Regex.VALID_MENTION_OR_LIST.matcher(tokenMatcher.group());
					if (mentionMatcher.find()) {
						start += mentionMatcher.start(Regex.VALID_MENTION_OR_LIST_GROUP_AT);
					} else {
						throw new AssertionError(); // tokenPattern is wrong??
					}
				}
			}

			if (start - oldFat > remainLength) { // 前回の切断できないトークンと今回のトークンの間に制限文字数がある
				return original.substring(0, qtTokenEnd + remainLength + oldFat); // そこで切断
			} else if (end - fat > remainLength) { // 今回のトークンの途中に制限文字数がある
				return original.substring(0, qtTokenEnd + start); // start includes fat
			}
		}
		return original.substring(0, qtTokenEnd + remainLength + fat); // 前回のトークン(または0)から最後の間に制限文字数がある
	}

	private void initUrlLength() {
		if (apiConfiguration == null) {
			apiConfiguration = ClientConfiguration.getInstance().getMessageBus().getApiConfiguration();
			if (apiConfiguration != null) {
				shortURLLength = apiConfiguration.getShortURLLength();
				shortURLLengthHttps = apiConfiguration.getShortURLLengthHttps();
			}
		}
	}
}

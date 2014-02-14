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

package jp.syuriken.snsw.twclient;

/**
 * ツイートの長さを計算するインターフェース
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public interface TweetLengthCalculator {

	/** http://t.co の長さ */
	/*public static final*/ int DEFAULT_SHORT_URL_LENGTH = 22;
	/** https://t.co の長さ */
	/*public static final*/ int DEFAULT_SHORT_URL_LENGTH_HTTPS = 23;
	/** ツイートの最大長 */
	/*public static final*/ int MAX_TWEET_LENGTH = 140;
	/** 警告を発するツイートの長さ */
	/*public static final*/ int WARN_TWEET_LENGTH = 120;

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

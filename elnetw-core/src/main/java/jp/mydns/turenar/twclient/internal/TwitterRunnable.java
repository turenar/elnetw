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
 * The above copyright notice and this permission notice shall be included
 *  in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 *  INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 *  IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 *  DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package jp.mydns.turenar.twclient.internal;

import jp.mydns.turenar.twclient.ClientConfiguration;
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


	/** インスタンスを生成する。失敗時はジョブキューに追加する。 */
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
	 *
	 * @throws TwitterException Twitterへのアクセス中に発生した例外
	 */
	protected abstract void access() throws TwitterException;

	/**
	 * 設定を取得する
	 *
	 * @return 設定
	 * @deprecated use {@link #configuration}
	 */
	@Deprecated
	protected ClientConfiguration getConfiguration() {
		return configuration;
	}

	/**
	 * 例外のハンドリング
	 *
	 * @param ex 例外
	 */
	protected void onException(TwitterException ex) {
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
				onException(ex);
			}
		}
	}
}

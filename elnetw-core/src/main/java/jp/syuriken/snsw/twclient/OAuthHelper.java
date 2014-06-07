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

package jp.syuriken.snsw.twclient;

import java.util.concurrent.CancellationException;

import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.RequestToken;

/**
 * OAuthでアクセストークンを取得するためのクラス。
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class OAuthHelper {

	private static final Logger logger = LoggerFactory.getLogger(OAuthHelper.class);
	private final ClientConfiguration configuration;

	/**
	 * インスタンスを生成する。
	 *
	 * @param configuration 設定
	 */
	public OAuthHelper(ClientConfiguration configuration) {
		this.configuration = configuration;
	}

	/**
	 * AccessTokenを取得するために、Twitterのoauthページを開き、PINコードを入力させる。
	 *
	 * @return アクセストークンが設定されたTwitterオブジェクト。キャンセルされたときはnullを返す。
	 * @throws TwitterException                           リクエストトークンが取得できない。メッセージダイアログは表示される。
	 * @throws java.util.concurrent.CancellationException user cancelled
	 */
	public Twitter show() throws CancellationException, TwitterException {
		String message = null;

		while (true) {
			Twitter twitter = new TwitterFactory(configuration.getTwitterConfigurationBuilder().build()).getInstance();
			RequestToken requestToken = null;

			try {
				requestToken = twitter.getOAuthRequestToken();
			} catch (TwitterException e) {
				logger.warn("Could not retrieve requestToken", e);
				JOptionPane.showMessageDialog(null,
						"リクエストトークンが取得できませんでした。しばらく経ってからお試しください。\n\n" + e.getLocalizedMessage(),
						ClientConfiguration.APPLICATION_NAME, JOptionPane.ERROR_MESSAGE);
				throw e;
			}

			String strURL = requestToken.getAuthorizationURL();

			try {
				configuration.getUtility().openBrowser(strURL);
			} catch (Exception e) {
				JOptionPane.showMessageDialog(null, "PIN codeを表示するためのブラウザを検索できませんでした。\n\n" + e.getLocalizedMessage(),
						"エラー", JOptionPane.ERROR_MESSAGE);
			}

			String viewMessage = "Please input PIN code";
			if (message != null) {
				viewMessage = viewMessage + "\n\n" + message;
				message = null;
			}
			String pin =
					JOptionPane.showInputDialog(null, viewMessage, "PIN CODE",
							JOptionPane.INFORMATION_MESSAGE);

			try {
				if (pin != null) {
					if (pin.length() > 0) {
						twitter.getOAuthAccessToken(requestToken, pin);
						return twitter;
					} else {
						message = "PIN codeを入力してください。";
					}
				} else {
					return null;
				}
			} catch (TwitterException te) {
				if (401 == te.getStatusCode()) {
					JOptionPane.showMessageDialog(null, "oAuthに失敗しました: PIN codeが正しくありません", "エラー",
							JOptionPane.ERROR_MESSAGE);
				} else {
					JOptionPane.showMessageDialog(null, "oAuthに失敗しました: " + te.getLocalizedMessage(), "エラー",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		}
	}
}

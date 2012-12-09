package jp.syuriken.snsw.twclient;

import java.util.concurrent.CancellationException;

import javax.swing.JOptionPane;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

/**
 * OAuthでアクセストークンを取得するためのクラス。
 *
 * @author Turenar <snswinhaiku dot lo at gmail dot com>
 */
public class OAuthFrame {

	private final ClientConfiguration configuration;


	/**
	 * インスタンスを生成する。
	 * @param configuration 設定
	 */
	public OAuthFrame(ClientConfiguration configuration) {
		this.configuration = configuration;
	}

	/**
	 * AccessTokenを取得するために、Twitterのoauthページを開き、PINコードを入力させる。
	 *
	 * @param twitter Twitter
	 * @return アクセストークン
	 */
	public AccessToken show(Twitter twitter) {
		RequestToken requestToken = null;

		try {
			requestToken = twitter.getOAuthRequestToken();
		} catch (TwitterException e) {
			e.printStackTrace();
		}

		AccessToken accessToken = null;

		while (null == accessToken) {
			String strURL = requestToken.getAuthorizationURL();

			try {
				configuration.getUtility().openBrowser(strURL);
			} catch (Exception e) {
				JOptionPane.showMessageDialog(null, "PIN codeを表示するためのブラウザが設定できませんでした。\n\n" + e.getLocalizedMessage(),
						"エラー", JOptionPane.ERROR_MESSAGE);
			}

			String pin =
					JOptionPane.showInputDialog(null, "Please input PIN code", "PIN CODE",
							JOptionPane.INFORMATION_MESSAGE);

			try {
				if (pin != null && pin.length() > 0) {
					accessToken = twitter.getOAuthAccessToken(requestToken, pin);
				} else {
					JOptionPane.showMessageDialog(null, "bye", "exit", JOptionPane.ERROR_MESSAGE);
					throw new CancellationException();
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
		return accessToken;
	}

}

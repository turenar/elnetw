package jp.syuriken.snsw.twclient.handler;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JMenuItem;

import com.twitter.Regex;
import jp.syuriken.snsw.twclient.ActionHandler;
import jp.syuriken.snsw.twclient.ClientFrameApi;
import jp.syuriken.snsw.twclient.StatusData;
import jp.syuriken.snsw.twclient.TweetLengthCalculator;
import jp.syuriken.snsw.twclient.internal.DefaultTweetLengthCalculator;
import jp.syuriken.snsw.twclient.internal.TweetLengthUpdater;
import twitter4j.Status;

/**
 * QTするためのアクションハンドラ
 *
 * @author Turenar <snswinhaiku dot lo at gmail dot com>
 */
public class QuoteTweetActionHandler implements ActionHandler {

	/**
	 * QTされた時用のツイートの長さを計算するクラス
	 *
	 * @author Turenar <snswinhaiku dot lo at gmail dot com>
	 */
	public static class QuoteTweetLengthCalculator implements TweetLengthCalculator {

		private final TweetLengthUpdater updater;

		private static Pattern qtPattern = Pattern.compile("[QR]T\\s?\\@[a-zA-Z01-9_]{1,20}\\:?+");

		private static Pattern tokenPattern = Pattern.compile( //
				"(?:" + Regex.VALID_URL + "|" //
						+ Regex.AUTO_LINK_HASHTAGS + "|" //
						+ Regex.AUTO_LINK_USERNAMES_OR_LISTS + ")", Pattern.CASE_INSENSITIVE);

		private static Pattern urlPattern = Regex.VALID_URL;


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
			int length = original.length();
			boolean shortened = false;
			Matcher qtMatcher = qtPattern.matcher(original);
			if (qtMatcher.find()) {
				int qtIndex = qtMatcher.end();
				Matcher matcher = urlPattern.matcher(original);
				int fat = 0;
				int fatBeforeQT = 0;
				while (matcher.find()) { //calculate fat
					int start = matcher.start(Regex.VALID_URL_GROUP_URL);
					int end = matcher.end(Regex.VALID_URL_GROUP_URL);
					fat += end - start - 20;
					if (end < qtIndex) {
						fatBeforeQT = fat;
					}
				}
				if (length - fat > MAX_TWEET_LENGTH) {
					shortened = true;
					length = qtMatcher.end() - fatBeforeQT;
				} else {
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
				updater.updatePostLength(length + "+", color, "短縮されます(実際の投稿は" + getShortenedText(original).length()
						+ "文字です)");
			} else {
				updater.updatePostLength(String.valueOf(length), color, null);
			}
		}

		@Override
		public String getShortenedText(String original) {
			final Matcher qtMatcher = qtPattern.matcher(original);
			if (original.length() <= MAX_TWEET_LENGTH || qtMatcher.find() == false) {
				return original; // not shortable or not QT Pattern
			}
			int lastTokenStart = qtMatcher.start();
			int lastFat = 0; // URL Fat (url.length() - 20)
			int fat = lastFat;
			final Matcher urlMatcher = urlPattern.matcher(original);
			while (urlMatcher.find()) { // handle URL before QT
				int start = urlMatcher.start(Regex.VALID_URL_GROUP_URL);
				int end = urlMatcher.end(Regex.VALID_URL_GROUP_URL);
				if (start - lastFat > lastTokenStart || end - fat > lastTokenStart) {
					break;
				}
				fat += end - start - 20;
			}

			final int qtEnd = qtMatcher.end();
			int offset = qtEnd; // Position's char includes
			if (offset - fat > MAX_TWEET_LENGTH) {
				return original; // avoid breaking QT
			}
			final Matcher tokenMatcher = tokenPattern.matcher(original);
			while (tokenMatcher.find(offset - 1)) { // preceeding char
				lastTokenStart = tokenMatcher.start() + 1; // NOT first char, Ignore Preceeding char
				offset = tokenMatcher.end();
				if (lastTokenStart - fat > MAX_TWEET_LENGTH) { // no breaking token
					return original.substring(0, MAX_TWEET_LENGTH + fat);
				}
				if (urlPattern.matcher(original.substring(lastTokenStart, offset)).find()) {
					fat += offset - lastTokenStart - 20;
				}
				if (offset - fat > MAX_TWEET_LENGTH) { // break token
					return original.substring(0, lastTokenStart); // lTS includes fat
				}
				// not over 140: continue
			}
			return original.substring(0, MAX_TWEET_LENGTH + fat); // without breaking token
		}

	}


	@Override
	public JMenuItem createJMenuItem(String commandName) {
		JMenuItem quoteMenuItem = new JMenuItem("引用(Q)", KeyEvent.VK_Q);
		return quoteMenuItem;
	}

	@Override
	public void handleAction(String actionName, StatusData statusData, ClientFrameApi api) {
		if (statusData.tag instanceof Status) {
			Status status = (Status) statusData.tag;
			api.setInReplyToStatus(status);
			api.setPostText(String.format(" QT @%s: %s", status.getUser().getScreenName(), status.getText()), 0, 0);
			api.focusPostBox();
			api.setTweetLengthCalculator(new QuoteTweetLengthCalculator(api));
		}
	}

	@Override
	public void popupMenuWillBecomeVisible(JMenuItem menuItem, StatusData statusData, ClientFrameApi api) {
		if ((statusData.isSystemNotify() == false) && (statusData.tag instanceof Status)) {
			menuItem.setEnabled(true);
		} else {
			menuItem.setEnabled(false);
		}
	}

}

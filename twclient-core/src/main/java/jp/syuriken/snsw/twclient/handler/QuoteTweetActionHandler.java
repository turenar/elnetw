package jp.syuriken.snsw.twclient.handler;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JMenuItem;

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
 * @author $Author$
 */
public class QuoteTweetActionHandler implements ActionHandler {
	
	/**
	 * QTされた時用のツイートの長さを計算するクラス
	 * 
	 * @author $Author$
	 */
	public static class QuoteTweetLengthCalculator implements TweetLengthCalculator {
		
		private final TweetLengthUpdater updater;
		
		private static Pattern qtPattern = Pattern.compile("[QR]T \\@[a-zA-Z01-9_]+\\:?");
		
		
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
			boolean shortened = original.length() > MAX_TWEET_LENGTH;
			int length = getShortenedText(original, true).length();
			Color color;
			if (length > MAX_TWEET_LENGTH) {
				color = Color.RED;
			} else if (length > WARN_TWEET_LENGTH) {
				color = Color.ORANGE;
			} else {
				color = Color.BLUE;
			}
			if (shortened) {
				updater.updatePostLength(length + "+", color, "短縮されます");
			} else {
				updater.updatePostLength(String.valueOf(length), color, null);
			}
		}
		
		@Override
		public String getShortenedText(String original) {
			return getShortenedText(original, false);
		}
		
		/*package*/String getShortenedText(String original, boolean minimumMatch) {
			int fat = 0;
			Matcher urlMatcher = DefaultTweetLengthCalculator.urlPattern.matcher(original);
			while (urlMatcher.find()) {
				fat += (urlMatcher.end() - urlMatcher.start()) - 20;
			}
			
			if (original.length() - fat <= MAX_TWEET_LENGTH) {
				return original;
			}
			Matcher matcher = qtPattern.matcher(original);
			if (matcher.find() == false) { // not QT
				return original;
			}
			if (matcher.end() - fat >= MAX_TWEET_LENGTH) { // [over13x] QT @...:
				return minimumMatch ? original.substring(0, matcher.end() + fat) : original;
			}
			return original.substring(0, (minimumMatch ? matcher.end() : MAX_TWEET_LENGTH) + fat);
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

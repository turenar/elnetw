package jp.syuriken.snsw.twclient.handler;

import java.awt.event.KeyEvent;
import java.text.MessageFormat;

import javax.swing.JMenuItem;

import jp.syuriken.snsw.twclient.ClientConfiguration;
import jp.syuriken.snsw.twclient.StatusData;
import jp.syuriken.snsw.twclient.internal.TwitterRunnable;
import jp.syuriken.snsw.twclient.gui.UserInfoFrameTab;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.User;

/**
 * ユーザー情報を表示するアクションハンドラ
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class UserInfoViewActionHandler extends StatusActionHandlerBase {

	/*package*/static final class UserFetcher extends TwitterRunnable {

		private final String userScreenName;
		private User user = null;


		protected UserFetcher(String userScreenName) {
			super(false);
			this.userScreenName = userScreenName;
		}

		@Override
		protected void access() throws TwitterException {
			user = configuration.getTwitterForRead().showUser(userScreenName);
		}

		protected User getUser() {
			run();
			return user;
		}
	}

	/**
	 * ユーザータイムラインfetcher
	 *
	 * @author Turenar (snswinhaiku dot lo at gmail dot com)
	 */
	private static final class UserTimelineFetcher extends TwitterRunnable {

		private final UserInfoFrameTab tab;
		private final long userId;


		private UserTimelineFetcher(UserInfoFrameTab tab, long userId) {
			this.tab = tab;
			this.userId = userId;
		}

		@Override
		protected void access() throws TwitterException {
			ResponseList<Status> timeline = configuration.getTwitterForRead().getUserTimeline(userId);
			for (Status status : timeline) {
				tab.getRenderer().onStatus(status);
			}
			for (Status status : configuration.getCacheManager().getStatusSet()) {
				if (status.getUser().getId() == userId) {
					tab.getRenderer().onStatus(status);
				}
			}
		}

		@Override
		protected void onException(TwitterException ex) {
			tab.getRenderer().onException(ex);
		}
	}
	private static Logger logger = LoggerFactory.getLogger(UserInfoViewActionHandler.class);

	@Override
	public JMenuItem createJMenuItem(IntentArguments arguments) {
		JMenuItem aboutMenuItem = new JMenuItem("ユーザーについて(A)...", KeyEvent.VK_A);
		return aboutMenuItem;
	}

	@Override
	public void handleAction(IntentArguments arguments) {
		User user = arguments.getExtraObj("user", User.class);
		if (user == null) {
			String screenName = arguments.getExtraObj("screenName", String.class);
			if (screenName == null) {
				StatusData statusData = arguments.getExtraObj(INTENT_ARG_NAME_SELECTING_POST_DATA, StatusData.class);
				if (statusData != null && statusData.tag instanceof Status) {
					Status status = (Status) statusData.tag;
					if (status.isRetweet()) {
						status = status.getRetweetedStatus();
					}
					user = status.getUser();
				} else {
					throw new IllegalArgumentException(
							"[userinfo AH] must call as userinfo!screenName=<screenName> or must statusData.tag is Status");
				}
			} else {
				user = new UserFetcher(screenName).getUser();
			}
		}

		ClientConfiguration configuration = ClientConfiguration.getInstance();
		final UserInfoFrameTab tab = new UserInfoFrameTab(user);
		final long userId = user.getId();
		configuration.addJob(new UserTimelineFetcher(tab, userId));
		configuration.addFrameTab(tab);
		configuration.focusFrameTab(tab);
	}

	@Override
	public void popupMenuWillBecomeVisible(JMenuItem menuItem, IntentArguments arguments) {
		Status status = getStatus(arguments);
		if (status != null) {
			if (status.isRetweet()) {
				status = status.getRetweetedStatus();
			}
			menuItem.setText(MessageFormat.format("@{0} ({1}) について(A)", status.getUser().getScreenName(), status
				.getUser().getName()));
			menuItem.setEnabled(true);
		} else {
			menuItem.setEnabled(false);
		}
	}
}

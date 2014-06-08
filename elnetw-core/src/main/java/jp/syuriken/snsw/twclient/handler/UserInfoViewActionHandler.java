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

package jp.syuriken.snsw.twclient.handler;

import java.awt.event.KeyEvent;
import java.text.MessageFormat;

import javax.swing.JMenuItem;

import jp.syuriken.snsw.twclient.ClientConfiguration;
import jp.syuriken.snsw.twclient.gui.UserInfoFrameTab;
import jp.syuriken.snsw.twclient.gui.render.RenderObject;
import jp.syuriken.snsw.twclient.internal.TwitterRunnable;
import jp.syuriken.snsw.twclient.twitter.TwitterUser;
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

	@Override
	public JMenuItem createJMenuItem(IntentArguments arguments) {
		return new JMenuItem("ユーザーについて(A)...", KeyEvent.VK_A);
	}

	@Override
	public void handleAction(IntentArguments arguments) {
		User user = arguments.getExtraObj("user", User.class);
		if (user == null) {
			String screenName = arguments.getExtraObj("screenName", String.class);
			if (screenName == null) {
				RenderObject renderObject = arguments.getExtraObj(INTENT_ARG_NAME_SELECTING_POST_DATA,
						RenderObject.class);
				if (renderObject != null && renderObject.getBasedObject() instanceof Status) {
					Status status = (Status) renderObject.getBasedObject();
					if (status.isRetweet()) {
						status = status.getRetweetedStatus();
					}
					user = status.getUser();
				} else {
					throw new IllegalArgumentException(
							"[userinfo AH] must call as userinfo!screenName=<screenName>"
									+ " or must renderObject.basedObject is Status");
				}
			} else {
				user = new UserFetcher(screenName).getUser();
			}
		}

		TwitterUser twitterUser = (user instanceof TwitterUser) ? (TwitterUser) user : new TwitterUser(user);
		final UserInfoFrameTab tab = new UserInfoFrameTab(twitterUser);
		final long userId = user.getId();
		ClientConfiguration configuration = ClientConfiguration.getInstance();
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

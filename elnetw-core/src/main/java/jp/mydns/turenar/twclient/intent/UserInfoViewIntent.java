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

package jp.mydns.turenar.twclient.intent;

import javax.swing.JMenuItem;

import jp.mydns.turenar.twclient.ClientConfiguration;
import jp.mydns.turenar.twclient.bus.MessageBus;
import jp.mydns.turenar.twclient.gui.render.RenderObject;
import jp.mydns.turenar.twclient.gui.tab.UserInfoFrameTab;
import jp.mydns.turenar.twclient.twitter.TwitterUser;
import twitter4j.Status;
import twitter4j.User;

import static jp.mydns.turenar.twclient.i18n.LocalizationResource.tr;

/**
 * ユーザー情報を表示するアクションハンドラ
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class UserInfoViewIntent extends AbstractIntent {

	@Override
	public void createJMenuItem(PopupMenuDispatcher dispatcher, IntentArguments arguments) {
		JMenuItem menuItem = new JMenuItem();
		Status status = getStatus(arguments);
		if (status != null) {
			if (status.isRetweet()) {
				status = status.getRetweetedStatus();
			}
			menuItem.setText(tr("About @%s (%s)...", status.getUser().getScreenName(), status.getUser().getName()));
			menuItem.setEnabled(true);
		} else {
			menuItem.setEnabled(false);
		}
		dispatcher.addMenu(menuItem, arguments);
	}

	@Override
	public void handleAction(IntentArguments arguments) {
		final UserInfoFrameTab tab;
		User user = arguments.getExtraObj("user", User.class);
		if (user != null) {
			tab = new UserInfoFrameTab(MessageBus.READER_ACCOUNT_ID, TwitterUser.getInstance(user));
		} else {
			String screenName = arguments.getExtraObj("screenName", String.class);
			if (screenName != null) {
				tab = new UserInfoFrameTab(MessageBus.READER_ACCOUNT_ID, screenName);
			} else {
				RenderObject renderObject = arguments.getExtraObj(INTENT_ARG_NAME_SELECTING_POST_DATA,
						RenderObject.class);
				if (renderObject != null && renderObject.getBasedObject() instanceof Status) {
					Status status = (Status) renderObject.getBasedObject();
					if (status.isRetweet()) {
						status = status.getRetweetedStatus();
					}
					tab = new UserInfoFrameTab(MessageBus.READER_ACCOUNT_ID, TwitterUser.getInstance(status.getUser()));
				} else {
					throw new IllegalArgumentException(
							"[userinfo AH] must call as userinfo!screenName=<screenName>"
									+ " or must renderObject.basedObject is Status");
				}
			}
		}

		ClientConfiguration configuration = ClientConfiguration.getInstance();
		configuration.addFrameTab(tab);
		configuration.focusFrameTab(tab);
	}

}

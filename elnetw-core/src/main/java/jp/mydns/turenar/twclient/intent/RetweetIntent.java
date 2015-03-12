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

import java.awt.event.KeyEvent;

import javax.swing.JMenuItem;

import jp.mydns.turenar.twclient.ClientConfiguration;
import jp.mydns.turenar.twclient.internal.TwitterRunnable;
import twitter4j.Status;
import twitter4j.TwitterException;

import static jp.mydns.turenar.twclient.i18n.LocalizationResource.tr;

/**
 * 公式リツイートするためのアクションハンドラ
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class RetweetIntent extends AbstractIntent {

	private static class RetweetTask extends TwitterRunnable {

		private final Status status;

		public RetweetTask(Status status) {
			this.status = status;
		}

		@Override
		public void access() throws TwitterException {
			configuration.getTwitterForWrite().retweetStatus(status.getId());
		}
	}

	private final ClientConfiguration configuration;

	/** create instance */
	public RetweetIntent() {
		configuration = ClientConfiguration.getInstance();
	}

	@Override
	public void createJMenuItem(PopupMenuDispatcher dispatcher, IntentArguments args) {
		JMenuItem retweetMenuItem = new JMenuItem(tr("Retweet"), KeyEvent.VK_T);
		Status status = getStatus(args);
		if (status == null) {
			retweetMenuItem.setVisible(false);
			retweetMenuItem.setEnabled(false);
		} else {
			retweetMenuItem.setEnabled(!status.getUser().isProtected());
			retweetMenuItem.setVisible(status.getUser().getId() != getLoginUserId());
		}
		dispatcher.addMenu(retweetMenuItem, args);
	}

	@Override
	public void handleAction(IntentArguments args) {
		Status status = getStatus(args);
		if (status == null) {
			throwIllegalArgument();
		}

		configuration.addJob(new RetweetTask(status));
	}
}

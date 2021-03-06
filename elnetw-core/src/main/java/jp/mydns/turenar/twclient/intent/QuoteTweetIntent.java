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

import jp.mydns.turenar.twclient.ClientFrameApi;
import jp.mydns.turenar.twclient.internal.QuoteTweetLengthCalculator;
import twitter4j.Status;

import static jp.mydns.turenar.twclient.i18n.LocalizationResource.tr;

/**
 * QTするためのアクションハンドラ
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class QuoteTweetIntent extends AbstractIntent {

	@Override
	public void createJMenuItem(PopupMenuDispatcher dispatcher, IntentArguments args) {
		JMenuItem menuItem = new JMenuItem(tr("Quoted tweet"), KeyEvent.VK_Q);
		menuItem.setEnabled(getStatus(args) != null);
		dispatcher.addMenu(menuItem, args);
	}

	@Override
	public void handleAction(IntentArguments arguments) {
		Status status = getStatus(arguments);
		if (status == null) {
			throwIllegalArgument();
		}

		ClientFrameApi api = configuration.getFrameApi();
		api.setInReplyToStatus(status);
		api.setPostText(String.format(" QT @%s: %s", status.getUser().getScreenName(), status.getText()), 0, 0);
		api.focusPostBox();
		api.setTweetLengthCalculator(new QuoteTweetLengthCalculator(api));
	}
}

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

import javax.swing.JMenuItem;

import jp.syuriken.snsw.twclient.ClientConfiguration;
import jp.syuriken.snsw.twclient.ClientFrameApi;
import twitter4j.Status;

/**
 * リプライするためのアクションハンドラ
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class ReplyActionHandler extends StatusActionHandlerBase {

	private final ClientConfiguration configuration;
	private final ClientFrameApi frameApi;

	public ReplyActionHandler() {
		configuration = ClientConfiguration.getInstance();
		frameApi = configuration.getFrameApi();
	}

	@Override
	public JMenuItem createJMenuItem(IntentArguments args) {
		JMenuItem replyMenuItem = new JMenuItem("Reply", KeyEvent.VK_R);
		return replyMenuItem;
	}

	@Override
	public void handleAction(IntentArguments args) {
		Status status = getStatus(args);
		if (status == null) {
			throwIllegalArgument();
		}

		String text;
		String screenName = status.getUser().getScreenName();

		Object appendFlagObj = args.getExtra("append");
		boolean appendFlag;
		if (appendFlagObj instanceof Boolean) {
			appendFlag = (Boolean) appendFlagObj;
		} else if (appendFlagObj != null) {
			appendFlag = true;
		} else {
			appendFlag = false;
		}
		if (appendFlag) {
			String postText = configuration.getFrameApi().getPostText();
			if (postText.trim().isEmpty()) {
				text = String.format(".@%s ", screenName);
			} else {
				text = String.format("%s@%s ", postText, screenName);
			}
			frameApi.setPostText(text);
		} else {
			text = String.format("@%s ", screenName);
			frameApi.focusPostBox();
		}
		frameApi.setPostText(text, text.length(), text.length());
		frameApi.setInReplyToStatus(status);
	}

	@Override
	public void popupMenuWillBecomeVisible(JMenuItem menuItem, IntentArguments arguments) {
		Status status = getStatus(arguments);
		menuItem.setEnabled(status != null);
	}
}

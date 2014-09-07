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

package jp.mydns.turenar.twclient.handler;

import javax.swing.JMenuItem;

import jp.mydns.turenar.twclient.ClientMessageListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ツイートをどうにかするアクションハンドラ
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class TweetActionHandler extends StatusActionHandlerBase {

	private static Logger logger = LoggerFactory.getLogger(TweetActionHandler.class);

	@Override
	public JMenuItem createJMenuItem(IntentArguments arguments) {
		return null;
	}

	@Override
	public void handleAction(IntentArguments arguments) {
		String actionName = arguments.getExtraObj("_arg", String.class);
		String messageName;
		switch (actionName) {
			case "copy":
				messageName = ClientMessageListener.REQUEST_COPY;
				break;
			case "copyurl":
				messageName = ClientMessageListener.REQUEST_COPY_URL;
				break;
			case "copyuserid":
				messageName = ClientMessageListener.REQUEST_COPY_USERID;
				break;
			case "browser_user":
				messageName = ClientMessageListener.REQUEST_BROWSER_USER_HOME;
				break;
			case "browser_status":
				messageName = ClientMessageListener.REQUEST_BROWSER_STATUS;
				break;
			case "browser_replyTo":
				messageName = ClientMessageListener.REQUEST_BROWSER_IN_REPLY_TO;
				break;
			case "openurls":
				messageName = ClientMessageListener.REQUEST_BROWSER_OPENURLS;
				break;
			default:
				logger.warn("{} is not action", actionName);
				return;
		}
		configuration.getFrameApi().getSelectingTab().getRenderer().onClientMessage(messageName, null);
	}

	@Override
	public void popupMenuWillBecomeVisible(JMenuItem menuItem, IntentArguments arguments) {
		// do nothing
	}
}

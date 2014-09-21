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

import java.net.URI;
import java.net.URISyntaxException;

import jp.mydns.turenar.twclient.ClientConfiguration;
import jp.mydns.turenar.twclient.bus.MessageBus;

/**
 * リストを閲覧するアクションハンドラ
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class ListIntent implements Intent {

	@Override
	public void createJMenuItem(PopupMenuDispatcher dispatcher, IntentArguments args) {
	}

	@Override
	public void handleAction(IntentArguments args) {
		Object user = args.getExtra("user");
		String userName;
		if (user == null) {
			throw new IllegalArgumentException("Specify extraArg `user`");
		} else if (user instanceof String) {
			userName = (String) user;
		} else {
			throw new IllegalArgumentException("extraArg `user` must be String");
		}
		Object listNameObj = args.getExtra("listName");
		String listName;
		if (listNameObj == null) {
			throw new IllegalArgumentException("Specify extraArg `listName`");
		} else if (listNameObj instanceof String) {
			listName = (String) listNameObj;
		} else {
			throw new IllegalArgumentException("extraArg `listName` must be String");
		}

		try {
			if (listName.startsWith("/")) {
				listName = listName.substring(1);
			}
			ClientConfiguration.getInstance().getUtility().openBrowser(
					new URI("https", "twitter.com", "/" + userName + "/lists/" + listName, null).toASCIIString());
		} catch (URISyntaxException e) {
			throw new AssertionError(e);
		} catch (Exception e) {
			ClientConfiguration.getInstance().getMessageBus().getListeners(MessageBus.READER_ACCOUNT_ID,
					"error").onException(e);
		}
	}
}
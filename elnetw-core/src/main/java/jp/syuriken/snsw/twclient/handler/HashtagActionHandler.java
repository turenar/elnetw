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

import javax.swing.JMenuItem;

import jp.syuriken.snsw.twclient.ActionHandler;
import jp.syuriken.snsw.twclient.ClientConfiguration;

/**
 * ハッシュタグを処理するアクションハンドラ
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class HashtagActionHandler implements ActionHandler {

	private final ClientConfiguration configuration;

	public HashtagActionHandler() {
		configuration = ClientConfiguration.getInstance();
	}

	@Override
	public JMenuItem createJMenuItem(IntentArguments arguments) {
		return null;
	}

	@Override
	public void handleAction(IntentArguments arguments) {
		String name = arguments.getExtraObj("name", String.class);
		if (name == null) {
			throw new IllegalArgumentException("actionName must be include hashtag: hashtag!name=<hashtag>");
		}

		IntentArguments query = arguments.clone().setIntentName("search").putExtra("query", "%23" + name);
		configuration.handleAction(query); //TODO
	}

	@Override
	public void popupMenuWillBecomeVisible(JMenuItem menuItem, IntentArguments arguments) {
	}
}

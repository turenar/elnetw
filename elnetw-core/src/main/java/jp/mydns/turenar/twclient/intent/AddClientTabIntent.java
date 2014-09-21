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
import jp.mydns.turenar.twclient.gui.AddClientTabConfirmFrame;
import jp.mydns.turenar.twclient.gui.tab.ClientTabFactory;

/**
 * action intent for adding client tab
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class AddClientTabIntent implements Intent {
	@Override
	public void createJMenuItem(PopupMenuDispatcher dispatcher, IntentArguments args) {
		String tabId = args.getExtraObj("tabId", String.class);
		if (tabId == null) {
			throw new IllegalArgumentException("tabId is not specified!");
		}
		ClientTabFactory factory = ClientConfiguration.getClientTabFactory(tabId);
		if (factory == null) {
			throw new IllegalArgumentException("tabId[" + tabId + "] is unknown!");
		}
		JMenuItem factoryItem = new JMenuItem(factory.getName());
		dispatcher.addMenu(factoryItem, new IntentArguments("tab_add").putExtra("tabId", tabId));
	}

	@Override
	public void handleAction(IntentArguments args) {
		new AddClientTabConfirmFrame(args.getExtraObj("tabId", String.class)).setVisible(true);
	}
}

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

import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import jp.mydns.turenar.twclient.ClientConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.Status;
import twitter4j.URLEntity;

/**
 * ツイートに含まれるURLを開くアクションハンドラ
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class UrlIntent extends StatusIntentBase {

	private static final Logger logger = LoggerFactory.getLogger(UrlIntent.class);
	private final ClientConfiguration configuration;

	public UrlIntent() {
		configuration = ClientConfiguration.getInstance();
	}

	@Override
	public JMenuItem createJMenuItem(IntentArguments arguments) {
		JMenu openUrlMenu = new JMenu("ツイートのURLをブラウザで開く");
		return openUrlMenu;
	}

	@Override
	public void handleAction(IntentArguments arguments) {
		String url = arguments.getExtraObj("url", String.class);
		if (url == null) {
			throw new IllegalArgumentException("arg `url' is not found");
		}
		try {
			configuration.getUtility().openBrowser(url);
		} catch (Exception e) {
			logger.warn("Failed open browser", e);
		}
	}

	@Override
	public void popupMenuWillBecomeVisible(JMenuItem menuItem, IntentArguments arguments) {
		if (menuItem instanceof JMenu == false) {
			throw new AssertionError("UrlActionHandler#pMWBV transfered menuItem which is not instanceof JMenu");
		}
		JMenu menu = (JMenu) menuItem;

		Status status = getStatus(arguments);
		if (status != null) {
			menu.removeAll();

			URLEntity[] urlEntities = status.getURLEntities();
			if (urlEntities == null || urlEntities.length == 0) {
				menu.setEnabled(false);
			} else {
				for (URLEntity entity : status.getURLEntities()) {
					JMenuItem urlMenu = new JMenuItem();
					if (entity.getDisplayURL() == null) {
						urlMenu.setText(entity.getURL());
					} else {
						urlMenu.setText(entity.getDisplayURL());
					}
					urlMenu.setActionCommand("url!" + entity.getURL());
					for (ActionListener listener : menu.getActionListeners()) {
						urlMenu.addActionListener(listener);
					}
					menu.add(urlMenu);
				}
				menu.setEnabled(true);
			}
		} else {
			menu.setEnabled(false);
		}
	}
}

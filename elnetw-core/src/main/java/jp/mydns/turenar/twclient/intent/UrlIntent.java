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

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import jp.mydns.turenar.twclient.ClientConfiguration;
import jp.mydns.turenar.twclient.internal.IntentActionListener;
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
	public void createJMenuItem(PopupMenuDispatcher dispatcher, IntentArguments args) {
		JMenu openUrlMenu = new JMenu("ツイートのURLをブラウザで開く");

		Status status = getStatus(args);
		if (status != null) {
			URLEntity[] urlEntities = status.getURLEntities();
			if (urlEntities == null || urlEntities.length == 0) {
				openUrlMenu.setEnabled(false);
			} else {
				for (URLEntity entity : status.getURLEntities()) {
					JMenuItem urlMenu = new JMenuItem();
					if (entity.getDisplayURL() == null) {
						urlMenu.setText(entity.getURL());
					} else {
						urlMenu.setText(entity.getDisplayURL());
					}
					urlMenu.addActionListener(new IntentActionListener("url").putExtra("url", entity.getURL()));
					openUrlMenu.add(urlMenu);
				}
				openUrlMenu.setEnabled(true);
			}
		} else {
			openUrlMenu.setEnabled(false);
		}
		dispatcher.addMenu(openUrlMenu, args);
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
}

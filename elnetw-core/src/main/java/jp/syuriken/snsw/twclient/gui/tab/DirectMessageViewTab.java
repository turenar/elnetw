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

package jp.syuriken.snsw.twclient.gui.tab;

import javax.swing.Icon;

import jp.syuriken.snsw.twclient.filter.IllegalSyntaxException;
import jp.syuriken.snsw.twclient.gui.render.RenderObject;
import jp.syuriken.snsw.twclient.gui.render.RenderTarget;
import twitter4j.DirectMessage;

/**
 * ダイレクトメッセージを表示するタブ
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class DirectMessageViewTab extends AbstractClientTab implements RenderTarget {

	private static final String TAB_ID = "directmessage";

	private DelegateRenderer renderer = new DelegateRenderer() {

		@Override
		public void onDirectMessage(DirectMessage directMessage) {
			actualRenderer.onDirectMessage(directMessage);
		}

		@Override
		public void onStatus(twitter4j.Status originalStatus) {
			// do nothing
		}
	};
	private boolean focusGained;
	private boolean isDirty;


	/**
	 * インスタンスを生成する。
	 *
	 * @throws IllegalSyntaxException クエリエラー
	 */
	public DirectMessageViewTab() throws IllegalSyntaxException {
		super();
		configuration.getMessageBus().establish("$reader", "direct_messages", getRenderer());
		configuration.getMessageBus().establish("$reader", "stream/user", getRenderer());
	}

	/**
	 * インスタンスを生成する。
	 *
	 * @param uniqId unique identifier
	 */
	public DirectMessageViewTab(String uniqId) {
		super(uniqId);
		configuration.getMessageBus().establish("$reader", "direct_messages", getRenderer());
		configuration.getMessageBus().establish("$reader", "stream/user", getRenderer());
	}

	@Override
	public void addStatus(RenderObject renderObject) {
		super.addStatus(renderObject);
		if (!(focusGained || isDirty)) {
			isDirty = true;
			runInDispatcherThread(new Runnable() {
				@Override
				public void run() {
					configuration.refreshTab(DirectMessageViewTab.this);
				}
			});
		}
	}

	@Override
	public void focusGained() {
		focusGained = true;
		isDirty = false;
		runInDispatcherThread(new Runnable() {
			@Override
			public void run() {
				configuration.refreshTab(DirectMessageViewTab.this);
			}
		});
	}

	@Override
	public void focusLost() {
		focusGained = false;
	}

	@Override
	public DelegateRenderer getDelegateRenderer() {
		return renderer;
	}

	@Override
	public Icon getIcon() {
		return null; // TODO
	}

	@Override
	public String getTabId() {
		return TAB_ID;
	}

	@Override
	public String getTitle() {
		return isDirty ? "DM*" : "DM";
	}

	@Override
	public String getToolTip() {
		return "DirectMessages";
	}
}
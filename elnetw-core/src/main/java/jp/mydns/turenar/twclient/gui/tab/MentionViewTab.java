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

package jp.mydns.turenar.twclient.gui.tab;

import javax.swing.Icon;

import jp.mydns.turenar.twclient.gui.render.RenderTarget;
import twitter4j.Status;

import static jp.mydns.turenar.twclient.i18n.LocalizationResource.tr;

/**
 * メンション表示用タブ
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class MentionViewTab extends AbstractClientTab implements RenderTarget {
	/**
	 * メンションタブ用レンダラ
	 *
	 * @author Turenar (snswinhaiku dot lo at gmail dot com)
	 */
	protected class MentionRenderer extends DelegateRenderer {


		@Override
		public void onStatus(Status originalStatus) {
			Status status;
			if (originalStatus.isRetweet()) {
				status = originalStatus.getRetweetedStatus();
			} else {
				status = originalStatus;
			}
			if (isMentioned(status.getUserMentionEntities())) {
				actualRenderer.onStatus(originalStatus);
			}
		}
	}

	private static final String TAB_ID = "mention";
	/** レンダラ */
	protected DelegateRenderer renderer = new MentionRenderer();

	/**
	 * インスタンスを生成する。
	 *
	 * @param accountId accountId
	 */
	public MentionViewTab(String accountId) {
		super(accountId);
		establishTweetPipe();
	}

	/**
	 * インスタンスを生成する。
	 *
	 * @param tabId  ignored
	 * @param uniqId unique identifier
	 */
	public MentionViewTab(String tabId, String uniqId) {
		super(tabId, uniqId);
		establishTweetPipe();
	}

	private void establishTweetPipe() {
		configuration.getMessageBus().establish(accountId, "statuses/mentions", getRenderer());
		configuration.getMessageBus().establish(accountId, "stream/user", getRenderer());
	}

	@Override
	public String getDefaultTitle() {
		return "Mention";
	}

	@Override
	public DelegateRenderer getDelegateRenderer() {
		return renderer;
	}

	@Override
	public Icon getIcon() {
		return null;
	}

	@Override
	public String getTabId() {
		return TAB_ID;
	}

	@Override
	public String getToolTip() {
		return tr("Mentioned tweets");
	}

	@Override
	protected String getTwitterUrl() {
		return "https://twitter.com/mentions";
	}
}

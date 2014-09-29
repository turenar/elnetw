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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.DirectMessage;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.User;

/**
 * タイムラインビュー
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class TimelineViewTab extends AbstractClientTab implements RenderTarget {
	/*package*/ static final Logger logger = LoggerFactory.getLogger(TimelineViewTab.class);
	private static final String TAB_ID = "timeline";
	private DelegateRenderer renderer = new DelegateRenderer() {
		@Override
		public void onChangeAccount(boolean forWrite) {
			actualRenderer.onChangeAccount(forWrite);
		}

		@Override
		public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
		}

		@Override
		public void onDirectMessage(DirectMessage directMessage) {
			actualRenderer.onDirectMessage(directMessage);
		}

		@Override
		public void onException(Exception ex) {
			actualRenderer.onException(ex);
		}

		@Override
		public void onFavorite(User source, User target, Status favoritedStatus) {
			actualRenderer.onFavorite(source, target, favoritedStatus);
		}

		@Override
		public void onFollow(User source, User followedUser) {
			actualRenderer.onFollow(source, followedUser);
		}

		@Override
		public void onStatus(Status status) {
			actualRenderer.onStatus(status);
		}

		@Override
		public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
			actualRenderer.onTrackLimitationNotice(numberOfLimitedStatuses);
		}

		@Override
		public void onUnfavorite(User source, User target, Status unfavoritedStatus) {
			actualRenderer.onUnfavorite(source, target, unfavoritedStatus);
		}
	};

	/**
	 * インスタンスを生成する。
	 *
	 * @param accountId account id
	 */
	public TimelineViewTab(String accountId) {
		super(accountId);
		configuration.getMessageBus().establish(accountId, "my/timeline", getRenderer());
		configuration.getMessageBus().establish(accountId, "error", getRenderer());
	}

	/**
	 * インスタンスを生成する。
	 *
	 * @param tabId  ignored
	 * @param uniqId unique identifier
	 */
	public TimelineViewTab(String tabId, String uniqId) {
		super(tabId, uniqId);
		configuration.getMessageBus().establish(accountId, "my/timeline", getRenderer());
		configuration.getMessageBus().establish(accountId, "error", getRenderer());
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
		return "Timeline";
	}

	@Override
	public String getToolTip() {
		return "HomeTimeline";
	}

	@Override
	protected String getTwitterUrl() {
		return "https://twitter.com";
	}
}

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

package jp.syuriken.snsw.twclient.filter.delayed;

import java.awt.Color;
import java.awt.EventQueue;

import jp.syuriken.snsw.lib.primitive.LongHashSet;
import jp.syuriken.snsw.twclient.ClientConfiguration;
import jp.syuriken.snsw.twclient.ClientMessageAdapter;
import jp.syuriken.snsw.twclient.bus.BlockingUsersChannel;
import jp.syuriken.snsw.twclient.bus.MessageBus;
import jp.syuriken.snsw.twclient.filter.AbstractMessageFilter;
import jp.syuriken.snsw.twclient.gui.render.MessageRenderBase;
import twitter4j.User;

/**
 * blocking user filter
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class BlockingUserFilter extends DelayedFilter {

	private static final String FILTER_BLOCKING_USER_WAIT_MESSAGE = "!filter/blockingUser/waitMessage";
	private final ClientConfiguration configuration;
	private boolean isGlobal;
	private LongHashSet longHashSet = new LongHashSet();

	/**
	 * instance
	 * @param isGlobal is this global filter?
	 */
	public BlockingUserFilter(boolean isGlobal) {
		this.isGlobal = isGlobal;
		configuration = ClientConfiguration.getInstance();
		init();
	}

	@Override
	public AbstractMessageFilter clone() throws CloneNotSupportedException {
		BlockingUserFilter clone = (BlockingUserFilter) super.clone();
		clone.isGlobal = false;
		clone.init();
		return clone;
	}

	@Override
	protected boolean filterUser(long userId) {
		return longHashSet.contains(userId);
	}

	/**
	 * for constructor and clone
	 */
	private void init() {
		if (!isGlobal) {
			configuration.getMessageBus().establish(MessageBus.READER_ACCOUNT_ID, "users/blocking", new ClientMessageAdapter() {
				@Override
				public void onBlock(User source, User blockedUser) {
					longHashSet.add(blockedUser.getId());
				}

				@Override
				public void onClientMessage(String name, Object arg) {
					switch (name) {
						case BlockingUsersChannel.BLOCKING_FETCH_FINISHED_ID:
							EventQueue.invokeLater(
									new Runnable() {
										@Override
										public void run() {
											child.onClientMessage(RENDER_DELETE_OBJECT, FILTER_BLOCKING_USER_WAIT_MESSAGE);
										}
									}
							);
							start();
					}
				}

				@Override
				public void onUnblock(User source, User unblockedUser) {
					longHashSet.add(unblockedUser.getId());
				}
			});
		}
	}

	@Override
	public void onClientMessage(String name, Object arg) {
		switch (name) { // CS-IGNORE
			case INIT_UI:
				MessageRenderBase renderBase = new MessageRenderBase(null)
						.setBackgroundColor(Color.GRAY)
						.setCreatedById("filter")
						.setCreatedById("!filter/blockingUser")
						.setCreatedByText("BlockingUserFilter")
						.setText("ブロック中のユーザーを取得中です。しばらくお待ちください。")
						.setUniqId(FILTER_BLOCKING_USER_WAIT_MESSAGE);
				child.onClientMessage(RENDER_SHOW_OBJECT, renderBase);
				break;
		}
		super.onClientMessage(name, arg);
	}
}

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

import jp.syuriken.snsw.lib.primitive.LongHashSet;
import jp.syuriken.snsw.twclient.ClientConfiguration;
import jp.syuriken.snsw.twclient.ClientMessageAdapter;
import jp.syuriken.snsw.twclient.bus.MessageBus;
import jp.syuriken.snsw.twclient.filter.AbstractMessageFilter;
import twitter4j.User;

/**
 * blocking user filter
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class BlockingUserFilter extends DelayedFilter {

	private final ClientConfiguration configuration;
	private LongHashSet longHashSet = new LongHashSet();

	/**
	 * instance
	 */
	public BlockingUserFilter() {
		configuration = ClientConfiguration.getInstance();
		init();
	}

	@Override
	public AbstractMessageFilter clone() throws CloneNotSupportedException {
		BlockingUserFilter clone = (BlockingUserFilter) super.clone();
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
		configuration.getMessageBus().establish(MessageBus.READER_ACCOUNT_ID, "users/blocking", new ClientMessageAdapter() {
			@Override
			public void onBlock(User source, User blockedUser) {
				start();
				longHashSet.add(blockedUser.getId());
			}

			@Override
			public void onUnblock(User source, User unblockedUser) {
				longHashSet.add(unblockedUser.getId());
			}
		});
	}
}

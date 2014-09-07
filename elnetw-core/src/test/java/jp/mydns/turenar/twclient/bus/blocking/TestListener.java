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

package jp.mydns.turenar.twclient.bus.blocking;

import java.util.LinkedList;

import jp.mydns.turenar.twclient.ClientMessageAdapter;
import jp.mydns.turenar.twclient.bus.channel.BlockingUsersChannel;
import twitter4j.User;

/**
 * listener impl for BlockingUsersChannelTest
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class TestListener extends ClientMessageAdapter {
	public static final String BLOCKING_FETCH_FINISHED_MSG = "clientMessage#BLOCKING_FETCH_FINISHED";
	private LinkedList<Object[]> calledLog = new LinkedList<>();

	private void call(Object... args) {
		calledLog.addLast(args);
	}

	@Override
	public void onBlock(User source, User blockedUser) {
		call("block", source, blockedUser);
	}

	@Override
	public void onClientMessage(String name, Object arg) {
		if (name.equals(BlockingUsersChannel.BLOCKING_FETCH_FINISHED_ID)) {
			call(BLOCKING_FETCH_FINISHED_MSG);
		}
	}

	@Override
	public void onUnblock(User source, User unblockedUser) {
		call("unblock", source, unblockedUser);
	}

	public Object[] popLog() {
		return calledLog.pollFirst();
	}
}

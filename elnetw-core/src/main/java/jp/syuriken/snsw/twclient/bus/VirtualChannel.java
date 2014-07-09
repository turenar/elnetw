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

package jp.syuriken.snsw.twclient.bus;

import jp.syuriken.snsw.twclient.ClientMessageListener;

/**
 * virtual channel: publish specified channel
 *
 * <p>establish messageには対応していない</p>
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class VirtualChannel implements MessageChannel {
	public VirtualChannel(MessageBus messageBus, String accountId, String to, String[] from) {
		ClientMessageListener listener = messageBus.getListeners(accountId, to);
		for (String s : from) {
			messageBus.establish(accountId, s, listener);
		}
	}

	@Override
	public synchronized void connect() {
	}

	@Override
	public synchronized void disconnect() {
	}

	@Override
	public void establish(ClientMessageListener listener) {
	}

	@Override
	public void realConnect() {
	}
}

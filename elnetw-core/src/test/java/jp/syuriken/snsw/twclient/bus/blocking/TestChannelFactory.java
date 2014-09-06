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

package jp.syuriken.snsw.twclient.bus.blocking;

import jp.syuriken.snsw.twclient.bus.MessageBus;
import jp.syuriken.snsw.twclient.bus.MessageChannel;
import jp.syuriken.snsw.twclient.bus.MessageChannelFactory;

/**
 * test channel factory
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class TestChannelFactory implements MessageChannelFactory {

	private TestChanel instance;

	public TestChannelFactory() {
	}

	public TestChanel getInstance() {
		return instance;
	}

	@Override
	public MessageChannel getInstance(MessageBus messageBus, String accountId, String path, String arg) {
		instance = new TestChanel(messageBus, accountId, path);
		return instance;
	}
}

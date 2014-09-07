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

import jp.mydns.turenar.twclient.ClientConfigurationTestImpl;
import jp.mydns.turenar.twclient.ClientMessageListener;
import jp.mydns.turenar.twclient.bus.MessageBus;
import jp.mydns.turenar.twclient.bus.TestMessageBus;
import jp.mydns.turenar.twclient.filter.FilterConstants;
import jp.mydns.turenar.twclient.internal.NullUser;
import jp.mydns.turenar.twclient.storage.CacheStorage;
import jp.mydns.turenar.twclient.twitter.TwitterUserImpl;
import org.junit.BeforeClass;
import org.junit.Test;
import twitter4j.User;

import static jp.mydns.turenar.twclient.bus.blocking.TestListener.BLOCKING_FETCH_FINISHED_MSG;
import static org.junit.Assert.*;

public class BlockingUsersChannelTest {

	@BeforeClass
	public static void setUp() throws Exception {
		FilterConstants.registerJson();
	}

	private User getUser(long userId) {
		return new TwitterUserImpl(new NullUser(userId));
	}

	@Test
	public void testBlockingWithDelayedEstablish() throws Exception {
		ClientConfigurationTestImpl configuration = new ClientConfigurationTestImpl();
		configuration.setCacheStorage(new CacheStorage());
		configuration.setGlobalInstance();
		try {
			MessageBus messageBus = new TestMessageBus();
			messageBus.addChannelFactory("users/blocking", new BlockingUsersChannelTestFactory());
			TestChannelFactory testChannelFactory = new TestChannelFactory();
			messageBus.addChannelFactory("stream/user", testChannelFactory);
			TestListener alice = new TestListener();
			messageBus.establish(MessageBus.READER_ACCOUNT_ID, "users/blocking", alice);
			assertNull(alice.popLog());

			messageBus.onInitialized();
			assertArrayEquals(new Object[]{"block", getUser(1L), getUser(2L)}, alice.popLog());
			assertArrayEquals(new Object[]{"block", getUser(1L), getUser(3L)}, alice.popLog());
			assertArrayEquals(new Object[]{BLOCKING_FETCH_FINISHED_MSG}, alice.popLog());
			assertNull(alice.popLog());

			TestListener betty = new TestListener();
			messageBus.establish(MessageBus.READER_ACCOUNT_ID, "users/blocking", betty);
			assertArrayEquals(new Object[]{"block", getUser(1L), getUser(2L)}, betty.popLog());
			assertArrayEquals(new Object[]{"block", getUser(1L), getUser(3L)}, betty.popLog());
			assertArrayEquals(new Object[]{BLOCKING_FETCH_FINISHED_MSG}, betty.popLog());
			assertNull(betty.popLog());

			ClientMessageListener publisher = testChannelFactory.getInstance().getListeners();
			publisher.onBlock(getUser(1L), getUser(4L));
			publisher.onUnblock(getUser(1L), getUser(2L));
			assertArrayEquals(new Object[]{"block", getUser(1L), getUser(4L)}, alice.popLog());
			assertArrayEquals(new Object[]{"unblock", getUser(1L), getUser(2L)}, alice.popLog());
			assertNull(alice.popLog());
			assertArrayEquals(new Object[]{"block", getUser(1L), getUser(4L)}, betty.popLog());
			assertArrayEquals(new Object[]{"unblock", getUser(1L), getUser(2L)}, betty.popLog());
			assertNull(betty.popLog());

			TestListener chris = new TestListener();
			messageBus.establish(MessageBus.READER_ACCOUNT_ID, "users/blocking", chris);
			assertArrayEquals(new Object[]{"block", getUser(1L), getUser(3L)}, chris.popLog());
			assertArrayEquals(new Object[]{"block", getUser(1L), getUser(4L)}, chris.popLog());
			assertArrayEquals(new Object[]{BLOCKING_FETCH_FINISHED_MSG}, chris.popLog());
			assertNull(chris.popLog());
		} finally {
			configuration.clearGlobalInstance();
		}
	}

	@Test
	public void testBlockingWithStream() throws Exception {
		ClientConfigurationTestImpl configuration = new ClientConfigurationTestImpl();
		configuration.setCacheStorage(new CacheStorage());
		configuration.setGlobalInstance();
		try {
			MessageBus messageBus = new TestMessageBus();
			messageBus.addChannelFactory("users/blocking", new BlockingUsersChannelTestFactory());
			TestChannelFactory factory = new TestChannelFactory();
			messageBus.addChannelFactory("stream/user", factory);
			TestListener alice = new TestListener();
			messageBus.establish(MessageBus.READER_ACCOUNT_ID, "users/blocking", alice);
			assertNull(alice.popLog());

			messageBus.onInitialized();
			assertArrayEquals(new Object[]{"block", getUser(1L), getUser(2L)}, alice.popLog());
			assertArrayEquals(new Object[]{"block", getUser(1L), getUser(3L)}, alice.popLog());
			assertArrayEquals(new Object[]{BLOCKING_FETCH_FINISHED_MSG}, alice.popLog());
			assertNull(alice.popLog());

			ClientMessageListener publisher = factory.getInstance().getListeners();
			publisher.onBlock(getUser(1L), getUser(4L));
			publisher.onUnblock(getUser(1L), getUser(2L));
			assertArrayEquals(new Object[]{"block", getUser(1L), getUser(4L)}, alice.popLog());
			assertArrayEquals(new Object[]{"unblock", getUser(1L), getUser(2L)}, alice.popLog());
			assertNull(alice.popLog());
		} finally {
			configuration.clearGlobalInstance();
		}
	}

	@Test
	public void testInit() throws Exception {
		ClientConfigurationTestImpl configuration = new ClientConfigurationTestImpl();
		configuration.setCacheStorage(new CacheStorage());
		configuration.setGlobalInstance();
		try {
			MessageBus messageBus = new TestMessageBus();
			messageBus.addChannelFactory("users/blocking", new BlockingUsersChannelTestFactory());
			messageBus.addChannelFactory("stream/user", new TestChannelFactory());
			TestListener alice = new TestListener();
			messageBus.establish(MessageBus.READER_ACCOUNT_ID, "users/blocking", alice);
			assertNull(alice.popLog());

			messageBus.onInitialized();
			assertArrayEquals(new Object[]{"block", getUser(1L), getUser(2L)}, alice.popLog());
			assertArrayEquals(new Object[]{"block", getUser(1L), getUser(3L)}, alice.popLog());
			assertArrayEquals(new Object[]{BLOCKING_FETCH_FINISHED_MSG}, alice.popLog());
			assertNull(alice.popLog());
		} finally {
			configuration.clearGlobalInstance();
		}
	}
}
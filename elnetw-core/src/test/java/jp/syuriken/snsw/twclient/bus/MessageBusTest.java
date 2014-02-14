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
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 *  INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 *  IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 *  DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package jp.syuriken.snsw.twclient.bus;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.TreeSet;

import jp.syuriken.snsw.twclient.ClientConfigurationTestImpl;
import jp.syuriken.snsw.twclient.ClientMessageAdapter;
import jp.syuriken.snsw.twclient.ClientMessageListener;
import org.junit.Test;
import twitter4j.conf.Configuration;

import static org.junit.Assert.*;

/**
 * Test for MessageBus
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class MessageBusTest {

	private static class GetInternalListenersMessageListener extends MessageAdapter {
		@Override
		public void onClientMessage(String name, Object arg) {
			value = name;
		}
	}

	private static class GetListeners extends TestFetcherAdapter {

		private static GetListeners INSTANCE;
		private final MessageBus messageBus;

		public GetListeners(MessageBus messageBus) {
			this.messageBus = messageBus;
			INSTANCE = this;
		}

		public ClientMessageListener getListeners(String path, boolean recursive) {
			return messageBus.getListeners("account", recursive, path);
		}
	}

	private static class MessageAdapter extends ClientMessageAdapter {
		protected String value;

		public MessageAdapter() {
			this(null);
		}

		public MessageAdapter(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}
	}

	private static class MyMessageBus extends MessageBus {
		@Override
		protected void init() {
		}
	}

	private static class OnChangeAccountTestFetcher extends TestFetcherAdapter {
		public static int connectionCalledCount;

		@Override
		public void connect() {
			connectionCalledCount++;
		}

		@Override
		public void disconnect() {
			connectionCalledCount++;
		}

		@Override
		public void realConnect() {
			connectionCalledCount++;
		}
	}

	private static class OnCleanUpTestFetcher extends TestFetcherAdapter {
		public static boolean isDisconnected;

		@Override
		public void disconnect() {
			isDisconnected = true;
		}
	}

	private static class OnInitializedTestFetcher extends TestFetcherAdapter {
		public static boolean isRealConnected;

		@Override
		public void realConnect() {
			isRealConnected = true;
		}
	}

	private static abstract class TestFetcherAdapter implements MessageChannel {
		@Override
		public void connect() {
		}

		@Override
		public void disconnect() {
		}

		@Override
		public void realConnect() {
		}
	}

	private static class TestFetcherFactory implements MessageChannelFactory {
		@Override
		public TestFetcherAdapter getInstance(MessageBus messageBus, String accountId, String path) {
			switch (path) {
				case "OnInitializedTest":
					return new OnInitializedTestFetcher();
				case "OnCleanUpTest":
					return new OnCleanUpTestFetcher();
				case "GetListeners":
					return new GetListeners(messageBus);
				case "OnChangeAccount":
					return new OnChangeAccountTestFetcher();
			}
			throw new AssertionError();
		}
	}

	public static final String READER_ACCOUNT = "ReaderAccount";
	public static final String WRITER_ACCOUNT = "WriterAccount";

	private void assertArrayItemsEquals(String[] actual, String... expected) {
		HashSet<String> hashSet = new HashSet<>();
		Collections.addAll(hashSet, actual);
		assertArrayItemsEquals(hashSet, expected);
	}

	private void assertArrayItemsEquals(Collection<String> actual, String... expected) {
		for (String expectedItem : expected) {
			if (!actual.remove(expectedItem)) {
				throw new AssertionError();
			}
		}
		assertTrue(actual.isEmpty());
	}

	private void assertConf(MessageBus messageBus, String accountId, String expected) {
		TwitterConfigurationImpl configuration = (TwitterConfigurationImpl) messageBus.getTwitterConfiguration(
				accountId);
		assertEquals(expected, configuration.getName());
	}

	@Test
	public void testCleanUp() throws Exception {
		MessageBus messageBus = new MyMessageBus();
		messageBus.addChannelFactory("OnCleanUpTest", new TestFetcherFactory());
		messageBus.establish("test", "OnCleanUpTest", new MessageAdapter());
		messageBus.cleanUp();
		assertTrue(OnCleanUpTestFetcher.isDisconnected);
	}

	@Test
	public void testGetListeners() throws Exception {
		MessageBus messageBus = new MyMessageBus();
		messageBus.addChannelFactory("GetListeners", new TestFetcherFactory());

		MessageAdapter allListener = new GetInternalListenersMessageListener();
		messageBus.establish("account", "all", allListener);

		MessageAdapter singleListener = new GetInternalListenersMessageListener();
		messageBus.establish("account", "GetListeners", singleListener);

		ClientMessageListener singleDispatcher = GetListeners.INSTANCE.getListeners("GetListeners", false);
		ClientMessageListener recursiveDispatcher = GetListeners.INSTANCE.getListeners("GetListeners", true);

		singleDispatcher.onClientMessage("isNotRecursive", null);
		assertEquals("isNotRecursive", singleListener.getValue());
		assertNull(allListener.getValue());

		recursiveDispatcher.onClientMessage("isRecursive", null);
		assertEquals("isRecursive", singleListener.getValue());
		assertEquals("isRecursive", allListener.getValue());
	}

	@Test
	public void testGetPath() throws Exception {
		MessageBus messageBus = new MyMessageBus();
		assertEquals("a:b", messageBus.getPath("a", "b"));
	}

	@Test
	public void testGetRecursivePaths() throws Exception {
		MessageBus messageBus = new MyMessageBus();
		assertArrayItemsEquals(messageBus.getRecursivePaths("a", "b"),
				"a:b", "a:all","$all:all","$all:b");
		assertArrayItemsEquals(messageBus.getRecursivePaths("a", "b/c"),
				"a:b/c", "a:b/all", "a:all","$all:all","$all:b/c");
		assertArrayItemsEquals(messageBus.getRecursivePaths("a", "b/c/d"),
				"a:b/c/d", "a:b/c/all", "a:b/all", "a:all","$all:b/c/d","$all:all");

		TreeSet<String> treeSet = new TreeSet<>();
		messageBus.getRecursivePaths(treeSet, "a", "b/c/d/e");
		assertArrayItemsEquals(treeSet,
				"a:b/c/d/e", "a:b/c/d/all", "a:b/c/all", "a:b/all", "a:all",
				"$all:b/c/d/e","$all:all");
	}

	@Test
	public void testGetTwitterConfiguration() throws Exception {
		ClientConfigurationTestImpl configuration = new ClientConfigurationTestImpl() {
			@Override
			public String getAccountIdForRead() {
				return READER_ACCOUNT;
			}

			@Override
			public String getAccountIdForWrite() {
				return WRITER_ACCOUNT;
			}

			@Override
			public Configuration getTwitterConfiguration(String accountId) {
				return new TwitterConfigurationImpl(accountId);
			}
		};
		configuration.setGlobalInstance();
		try {
			MessageBus messageBus = new MyMessageBus();
			assertConf(messageBus, MessageBus.READER_ACCOUNT_ID, READER_ACCOUNT);
			assertConf(messageBus, MessageBus.WRITER_ACCOUNT_ID, WRITER_ACCOUNT);
			assertConf(messageBus, "aiueo", "aiueo");
			assertConf(messageBus, "twitter", "twitter");
		} finally {
			configuration.clearGlobalInstance();
		}
	}

	@Test
	public void testOnChangeAccount() throws Exception {
		assertEquals(0, OnChangeAccountTestFetcher.connectionCalledCount);
		MessageBus messageBus = new MyMessageBus();
		messageBus.addChannelFactory("OnChangeAccount", new TestFetcherFactory());
		messageBus.establish(MessageBus.READER_ACCOUNT_ID,
				"OnChangeAccount", new MessageAdapter());
		assertEquals(1, OnChangeAccountTestFetcher.connectionCalledCount);
		messageBus.onChangeAccount(false);
		assertEquals(3, OnChangeAccountTestFetcher.connectionCalledCount);
		messageBus.onInitialized();
		assertEquals(4, OnChangeAccountTestFetcher.connectionCalledCount);
		messageBus.onChangeAccount(false);
		assertEquals(7, OnChangeAccountTestFetcher.connectionCalledCount);
	}

	@Test
	public void testOnInitialized() throws Exception {
		MessageBus messageBus = new MyMessageBus();
		messageBus.addChannelFactory("OnInitializedTest", new TestFetcherFactory());
		messageBus.establish("test", "OnInitializedTest", new MessageAdapter());
		messageBus.onInitialized();
		assertTrue(OnInitializedTestFetcher.isRealConnected);
	}
}

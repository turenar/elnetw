package jp.syuriken.snsw.twclient.net;

import java.util.Collection;
import java.util.HashSet;
import java.util.TreeSet;

import jp.syuriken.snsw.twclient.ClientConfigurationTestImpl;
import jp.syuriken.snsw.twclient.ClientMessageAdapter;
import jp.syuriken.snsw.twclient.ClientMessageListener;
import org.junit.Test;
import twitter4j.conf.Configuration;

import static org.junit.Assert.*;

/**
 * Test for TwitterDataFetchScheduler
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class TwitterDataFetchSchedulerTest {

	private static class GetInternalListenersMessageListener extends MessageAdapter {
		@Override
		public void onClientMessage(String name, Object arg) {
			value = name;
		}
	}

	private static class GetListeners extends TestFetcherAdapter {

		private static GetListeners INSTANCE;
		private final TwitterDataFetchScheduler scheduler;

		public GetListeners(TwitterDataFetchScheduler scheduler) {
			this.scheduler = scheduler;
			INSTANCE = this;
		}

		public ClientMessageListener getListeners(String path, boolean recursive) {
			return scheduler.getListeners("account", recursive, path);
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

	private static class MyTwitterDataFetchScheduler extends TwitterDataFetchScheduler {
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

	private static abstract class TestFetcherAdapter implements DataFetcher {
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

	private static class TestFetcherFactory implements DataFetcherFactory {
		@Override
		public TestFetcherAdapter getInstance(TwitterDataFetchScheduler scheduler, String accountId, String path) {
			switch (path) {
				case "OnInitializedTest":
					return new OnInitializedTestFetcher();
				case "OnCleanUpTest":
					return new OnCleanUpTestFetcher();
				case "GetListeners":
					return new GetListeners(scheduler);
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
		for (String item : actual) {
			hashSet.add(item);
		}
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

	private void assertConf(TwitterDataFetchScheduler fetchScheduler, String accountId, String expected) {
		TwitterConfigurationImpl configuration = (TwitterConfigurationImpl) fetchScheduler.getTwitterConfiguration(
				accountId);
		assertEquals(expected, configuration.getName());
	}

	@Test
	public void testCleanUp() throws Exception {
		TwitterDataFetchScheduler fetchScheduler = new MyTwitterDataFetchScheduler();
		fetchScheduler.addFetcherFactory("OnCleanUpTest", new TestFetcherFactory());
		fetchScheduler.establish("test", "OnCleanUpTest", new MessageAdapter());
		fetchScheduler.cleanUp();
		assertTrue(OnCleanUpTestFetcher.isDisconnected);
	}

	@Test
	public void testGetListeners() throws Exception {
		TwitterDataFetchScheduler fetchScheduler = new MyTwitterDataFetchScheduler();
		fetchScheduler.addFetcherFactory("GetListeners", new TestFetcherFactory());

		MessageAdapter allListener = new GetInternalListenersMessageListener();
		fetchScheduler.establish("account", "all", allListener);

		MessageAdapter singleListener = new GetInternalListenersMessageListener();
		fetchScheduler.establish("account", "GetListeners", singleListener);

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
		TwitterDataFetchScheduler fetchScheduler = new MyTwitterDataFetchScheduler();
		assertEquals("a:b", fetchScheduler.getPath("a", "b"));
	}

	@Test
	public void testGetRecursivePaths() throws Exception {
		TwitterDataFetchScheduler fetchScheduler = new MyTwitterDataFetchScheduler();
		assertArrayItemsEquals(fetchScheduler.getRecursivePaths("a", "b"), "a:b", "a:all");
		assertArrayItemsEquals(fetchScheduler.getRecursivePaths("a", "b/c"), "a:b/c", "a:b/all", "a:all");
		assertArrayItemsEquals(fetchScheduler.getRecursivePaths("a", "b/c/d"),
				"a:b/c/d", "a:b/c/all", "a:b/all", "a:all");

		TreeSet<String> treeSet = new TreeSet<>();
		fetchScheduler.getRecursivePaths(treeSet, "a", "b/c/d/e");
		assertArrayItemsEquals(treeSet,
				"a:b/c/d/e", "a:b/c/d/all", "a:b/c/all", "a:b/all", "a:all");
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
			TwitterDataFetchScheduler fetchScheduler = new MyTwitterDataFetchScheduler();
			assertConf(fetchScheduler, TwitterDataFetchScheduler.READER_ACCOUNT_ID, READER_ACCOUNT);
			assertConf(fetchScheduler, TwitterDataFetchScheduler.WRITER_ACCOUNT_ID, WRITER_ACCOUNT);
			assertConf(fetchScheduler, "aiueo", "aiueo");
			assertConf(fetchScheduler, "twitter", "twitter");
		} finally {
			configuration.clearGlobalInstance();
		}
	}

	@Test
	public void testOnChangeAccount() throws Exception {
		assertEquals(0, OnChangeAccountTestFetcher.connectionCalledCount);
		TwitterDataFetchScheduler fetchScheduler = new MyTwitterDataFetchScheduler();
		fetchScheduler.addFetcherFactory("OnChangeAccount", new TestFetcherFactory());
		fetchScheduler.establish(TwitterDataFetchScheduler.READER_ACCOUNT_ID,
				"OnChangeAccount", new MessageAdapter());
		assertEquals(1, OnChangeAccountTestFetcher.connectionCalledCount);
		fetchScheduler.onChangeAccount(false);
		assertEquals(3, OnChangeAccountTestFetcher.connectionCalledCount);
		fetchScheduler.onInitialized();
		assertEquals(4, OnChangeAccountTestFetcher.connectionCalledCount);
		fetchScheduler.onChangeAccount(false);
		assertEquals(7, OnChangeAccountTestFetcher.connectionCalledCount);
	}

	@Test
	public void testOnInitialized() throws Exception {
		TwitterDataFetchScheduler fetchScheduler = new MyTwitterDataFetchScheduler();
		fetchScheduler.addFetcherFactory("OnInitializedTest", new TestFetcherFactory());
		fetchScheduler.establish("test", "OnInitializedTest", new MessageAdapter());
		fetchScheduler.onInitialized();
		assertTrue(OnInitializedTestFetcher.isRealConnected);
	}
}

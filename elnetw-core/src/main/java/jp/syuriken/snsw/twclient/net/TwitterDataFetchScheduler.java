package jp.syuriken.snsw.twclient.net;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import jp.syuriken.snsw.twclient.ClientConfiguration;
import jp.syuriken.snsw.twclient.ClientMessageListener;
import jp.syuriken.snsw.twclient.ClientProperties;
import jp.syuriken.snsw.twclient.ParallelRunnable;
import jp.syuriken.snsw.twclient.internal.TwitterRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.Twitter;
import twitter4j.TwitterAPIConfiguration;
import twitter4j.TwitterException;
import twitter4j.conf.Configuration;

/**
 * Twitterからの情報を取得するためのスケジューラ
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class TwitterDataFetchScheduler {
	private static class NullIterable implements Iterable<ClientMessageListener> {
		@Override
		public Iterator<ClientMessageListener> iterator() {
			return NULL_ITERATOR;
		}
	}

	private static class NullIterator implements Iterator<ClientMessageListener> {
		@Override
		public boolean hasNext() {
			return false;
		}

		@Override
		public ClientMessageListener next() {
			return null;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

	private final class ApiConfigurationFetcher extends TwitterRunnable implements ParallelRunnable {

		@Override
		protected void access() throws TwitterException {
			apiConfiguration = twitterForRead.getAPIConfiguration();
		}
	}

	private static final Logger logger = LoggerFactory.getLogger(TwitterDataFetchScheduler.class);
	/*package*/ static final Iterator<ClientMessageListener> NULL_ITERATOR = new NullIterator();
	/*package*/ static final Iterable<ClientMessageListener> NULL_LISTENERS = new NullIterable();

	private static String getAppended(StringBuilder builder, String appendedString) {
		int oldLength = builder.length();
		String ret = builder.append(appendedString).toString();
		builder.setLength(oldLength);
		return ret;
	}

	/*package*/final ClientConfiguration configuration;
	protected HashMap<String, String[]> virtualPathMap = new HashMap<>();
	protected HashMap<String, DataFetcherFactory> fetcherMap = new HashMap<>();
	protected LinkedHashMap<String, DataFetcher> pathMap = new LinkedHashMap<>();
	protected HashMap<String, CopyOnWriteArrayList<ClientMessageListener>> pathListenerMap = new HashMap<>();
	/*package*/ Twitter twitterForRead;
	/*package*/ ClientProperties configProperties;
	/*package*/ TwitterAPIConfiguration apiConfiguration;
	private volatile boolean isInitialized;

	/** インスタンスを生成する。 */
	public TwitterDataFetchScheduler() {
		this.configuration = ClientConfiguration.getInstance();
		configProperties = configuration.getConfigProperties();
		twitterForRead = configuration.getTwitterForRead();
		init(); // for tests
	}

	/** お掃除する */
	public void cleanUp() {
		Collection<DataFetcher> entries = pathMap.values();
		for (DataFetcher entry : entries) {
			entry.disconnect();
		}
	}

	/**
	 * DataFetcherからデータを取得できるように登録する
	 *
	 * @param accountId    アカウントID (long|$reader|$writer)
	 * @param notifierName 通知名。"my/timeline"など
	 * @param listener     リスナ
	 * @return 登録できたかどうか。
	 */
	public synchronized boolean establish(String accountId, String notifierName, ClientMessageListener listener) {
		String path = getPath(accountId, notifierName);

		CopyOnWriteArrayList<ClientMessageListener> messageListeners = pathListenerMap.get(path);
		if (messageListeners == null) {
			messageListeners = new CopyOnWriteArrayList<>();
			pathListenerMap.put(path, messageListeners);
		}
		messageListeners.add(listener);

		String[] virtualPaths = virtualPathMap.get(notifierName);
		if (virtualPaths != null) {
			for (String virtualPath : virtualPaths) {
				establish(accountId, virtualPath, listener);
			}
		} else {
			DataFetcher dataFetcher = pathMap.get(path);
			if (dataFetcher == null) {
				DataFetcherFactory factory = fetcherMap.get(notifierName);
				if (factory == null) {
					if (!notifierName.endsWith("all")) {
						logger.warn("DataFetcher `{}' is not found.", notifierName);
					}
					return false;
				}
				dataFetcher = factory.getInstance(this, accountId, notifierName);
				pathMap.put(path, dataFetcher);
				dataFetcher.connect();
				if (isInitialized) {
					dataFetcher.realConnect();
				}
			}
		}
		return true;
	}

	/**
	 * 現在のREST API情報を取得する
	 *
	 * @return {@link TwitterAPIConfiguration}インスタンス。まだ取得されていない場合null。
	 */
	public TwitterAPIConfiguration getApiConfiguration() {
		return apiConfiguration;
	}

	/*package*/ Iterable<ClientMessageListener> getInternalListeners(String path) {
		CopyOnWriteArrayList<ClientMessageListener> listeners = pathListenerMap.get(path);
		return listeners == null ? NULL_LISTENERS : listeners;
	}

	/**
	 * DataFetcherがpeerに通知するためのリスナを取得する。返り値はキャッシュされるべき。
	 * peerが2つあるいはそれ以上あっても、返り値のインスタンスの内部でそれぞれのpeerに通知される
	 *
	 * @param accountId    アカウントID (peer)
	 * @param notifierName 通知Identifier (&quot;my/timeline&quot;など)
	 * @return peerに通知するためのリスナ。キャッシュされるべき。
	 */
	public ClientMessageListener getListeners(String accountId, String... notifierName) {
		return getListeners(accountId, true, notifierName);
	}

	/**
	 * DataFetcherがpeerに通知するためのリスナを取得する。返り値はキャッシュされるべき。
	 * peerが2つあるいはそれ以上あっても、返り値のインスタンスの内部でそれぞれのpeerに通知される
	 *
	 * @param accountId    アカウントID (peer)
	 * @param recursive    再帰的に通知する
	 * @param notifierName 通知Identifier (&quot;my/timeline&quot;など)
	 * @return peerに通知するためのリスナ。キャッシュされるべき。
	 */
	public ClientMessageListener getListeners(String accountId, boolean recursive, String... notifierName) {
		return new VirtualMultipleMessageDispatcher(this, recursive, accountId, notifierName);
	}

	/*package*/ String getPath(String accountId, String notifierName) {
		return accountId + ":" + notifierName;
	}

	public String[] getRecursivePaths(String accountId, String notifierName) {
		ArrayList<String> paths = new ArrayList<>();
		getRecursivePaths(paths, accountId, notifierName);
		return paths.toArray(new String[paths.size()]);
	}

	public void getRecursivePaths(Collection<String> paths, String accountId, String notifierName) {
		String[] notifierDirs = notifierName.split("/");
		StringBuilder builder = new StringBuilder(accountId).append(':');
		paths.add(getAppended(builder, "all"));

		final int max = notifierDirs.length - 1;
		for (int i = 0; i < max; i++) {
			if (i != 0) {
				builder.append('/');
			}
			builder.append(notifierDirs[i]);

			paths.add(getAppended(builder, "/all"));
		}
		paths.add(getPath(accountId, notifierName));
	}

	/**
	 * accountIdの{@link Configuration}を取得する。
	 *
	 * @param accountId アカウントID (long|$reader|$writer)
	 * @return Twitter Configuration
	 */
	public Configuration getTwitterConfiguration(String accountId) {
		if (accountId.equals("$reader")) {
			return configuration.getTwitterConfiguration(configuration.getAccountIdForRead());
		} else if (accountId.equals("$writer")) {
			return configuration.getTwitterConfiguration(configuration.getAccountIdForWrite());
		} else {
			return configuration.getTwitterConfiguration(accountId);
		}
	}

	protected void init() {
		virtualPathMap.put("my/timeline", new String[]{"stream/user", "statuses/timeline"});
		fetcherMap.put("stream/user", new StreamFetcherFactory());
		fetcherMap.put("statuses/timeline", new TimelineFetcherFactory());
		fetcherMap.put("statuses/mentions", new MentionsFetcherFactory());
		fetcherMap.put("direct_messages", new DirectMessageFetcherFactory());

		scheduleGettingTwitterApiConfiguration();
		onChangeAccount(true);
		onChangeAccount(false);
	}

	/**
	 * アカウント変更通知
	 *
	 * @param forWrite 書き込み用アカウントが変更されたかどうか。
	 */
	public void onChangeAccount(boolean forWrite) {
		if (forWrite) {
			reloginForWrite(configuration.getAccountIdForWrite());
		} else {
			reloginForRead(configuration.getAccountIdForRead());
		}
	}

	/** ClientTabの復元が完了し、DataFetcherの通知を受け取れるようになった */
	public synchronized void onInitialized() {
		isInitialized = true;
		for (DataFetcher fetcher : pathMap.values()) {
			fetcher.realConnect();
		}
	}

	private void reloginForRead(String accountId) {
		// TODO
	}

	private void reloginForWrite(String accountId) {
		// TODO
	}

	private void scheduleGettingTwitterApiConfiguration() {
		configuration.addJob(new ApiConfigurationFetcher());
	}
}

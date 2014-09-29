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

package jp.mydns.turenar.twclient.bus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;

import jp.mydns.turenar.twclient.ClientConfiguration;
import jp.mydns.turenar.twclient.ClientMessageListener;
import jp.mydns.turenar.twclient.ParallelRunnable;
import jp.mydns.turenar.twclient.bus.factory.NullMessageChannelFactory;
import jp.mydns.turenar.twclient.conf.ClientProperties;
import jp.mydns.turenar.twclient.gui.tab.TabRenderer;
import jp.mydns.turenar.twclient.internal.TwitterRunnable;
import jp.mydns.turenar.twclient.storage.CacheStorage;
import jp.mydns.turenar.twclient.storage.DirEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.MediaEntity;
import twitter4j.RateLimitStatus;
import twitter4j.Twitter;
import twitter4j.TwitterAPIConfiguration;
import twitter4j.TwitterException;
import twitter4j.conf.Configuration;

/**
 * Message Bus
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class MessageBus {
	private static class ApiConfigurationImpl implements TwitterAPIConfiguration {

		private static final long serialVersionUID = 3286804457928802269L;

		private int charactersReservedPerMedia;
		private int maxMediaPerUpload;
		private String[] nonUsernamePathArray;
		private int photoSizeLimit;
		private Map<Integer, MediaEntity.Size> photoSizes;
		private int shortUrlLength;
		private int shortUrlLengthHttps;

		public ApiConfigurationImpl(DirEntry cacheStorage) {
			charactersReservedPerMedia = cacheStorage.readInt("charactersReservedPerMedia");
			maxMediaPerUpload = cacheStorage.readInt("maxMediaPerUpload");
			List<String> nonUsernamePaths = cacheStorage.readStringList("nonUsernamePaths");
			nonUsernamePathArray = nonUsernamePaths.toArray(new String[nonUsernamePaths.size()]);

			photoSizeLimit = cacheStorage.readInt("photoSizeLimit");
			DirEntry photoSizeEntries = cacheStorage.getDirEntry("photoSizes");
			photoSizes = new HashMap<>();
			for (Iterator<String> traverse = photoSizeEntries.traverse(); traverse.hasNext(); ) {
				DirEntry photoSizeEntry = photoSizeEntries.getDirEntry(traverse.next());
				int type = photoSizeEntry.readInt("type");
				int width = photoSizeEntry.readInt("width");
				int height = photoSizeEntry.readInt("height");
				int resize = photoSizeEntry.readInt("resize");
				SizeImpl size = new SizeImpl(width, height, resize);
				photoSizes.put(type, size);
			}

			shortUrlLength = cacheStorage.readInt("shortURLLength");
			shortUrlLengthHttps = cacheStorage.readInt("shortURLLengthHttps");
		}

		@Override
		public int getAccessLevel() {
			return 0;
		}

		@Override
		public int getCharactersReservedPerMedia() {
			return charactersReservedPerMedia;
		}

		@Override
		public int getMaxMediaPerUpload() {
			return maxMediaPerUpload;
		}

		@Override
		public String[] getNonUsernamePaths() {
			return nonUsernamePathArray;
		}

		@Override
		public int getPhotoSizeLimit() {
			return photoSizeLimit;
		}

		@Override
		public Map<Integer, MediaEntity.Size> getPhotoSizes() {
			return null;
		}

		@Override
		public RateLimitStatus getRateLimitStatus() {
			return null;
		}

		@Override
		public int getShortURLLength() {
			return shortUrlLength;
		}

		@Override
		public int getShortURLLengthHttps() {
			return shortUrlLengthHttps;
		}

		public void update(TwitterAPIConfiguration apiConfiguration) {
			charactersReservedPerMedia = apiConfiguration.getCharactersReservedPerMedia();
			maxMediaPerUpload = apiConfiguration.getMaxMediaPerUpload();
			nonUsernamePathArray = apiConfiguration.getNonUsernamePaths();
			photoSizeLimit = apiConfiguration.getPhotoSizeLimit();
			photoSizes = apiConfiguration.getPhotoSizes();
			shortUrlLength = apiConfiguration.getShortURLLength();
			shortUrlLengthHttps = apiConfiguration.getShortURLLengthHttps();
		}
	}

	private static class SizeImpl implements MediaEntity.Size {

		private static final long serialVersionUID = -1371963865104040605L;
		private final int width;
		private final int height;
		private final int resize;

		public SizeImpl(int width, int height, int resize) {
			this.width = width;
			this.height = height;
			this.resize = resize;
		}

		@Override
		public int getHeight() {
			return height;
		}

		@Override
		public int getResize() {
			return resize;
		}

		@Override
		public int getWidth() {
			return width;
		}
	}

	private final class ApiConfigurationFetcher extends TwitterRunnable implements ParallelRunnable {

		@Override
		protected void access() throws TwitterException {
			TwitterAPIConfiguration conf = twitterForRead.getAPIConfiguration();
			if (apiConfiguration instanceof ApiConfigurationImpl) {
				((ApiConfigurationImpl) apiConfiguration).update(conf);
			} else {
				apiConfiguration = conf;
			}
			DirEntry storage = configuration.getCacheStorage().mkdir(CACHE_PATH_API_CONF, true);
			storage.writeInt("charactersReservedPerMedia", conf.getCharactersReservedPerMedia());
			storage.writeInt("maxMediaPerUpload", conf.getMaxMediaPerUpload());
			storage.writeList("nonUsernamePaths", (Object[]) conf.getNonUsernamePaths());
			storage.writeInt("photoSizeLimit", conf.getPhotoSizeLimit());

			storage.rmdir("photoSizes", true);
			DirEntry photoSizeEntries = storage.mkdir("photoSizes");
			Map<Integer, MediaEntity.Size> photoSizes = conf.getPhotoSizes();
			for (Map.Entry<Integer, MediaEntity.Size> entry : photoSizes.entrySet()) {
				DirEntry photoSizeEntry = photoSizeEntries.mkdir(entry.getKey().toString());
				MediaEntity.Size size = entry.getValue();
				photoSizeEntry.writeInt("type", entry.getKey());
				photoSizeEntry.writeInt("width", size.getWidth());
				photoSizeEntry.writeInt("height", size.getHeight());
				photoSizeEntry.writeInt("resize", size.getResize());
			}

			storage.writeInt("shortURLLength", conf.getShortURLLength());
			storage.writeInt("shortURLLengthHttps", conf.getShortURLLengthHttps());
			storage.writeLong(CACHE_PATH_API_CONF_MODIFIED, System.currentTimeMillis());
		}
	}

	private static final Logger logger = LoggerFactory.getLogger(MessageBus.class);
	/**
	 * virtual account id for reader
	 */
	public static final String READER_ACCOUNT_ID = "$reader";
	/**
	 * virtual account id for writer
	 */
	public static final String WRITER_ACCOUNT_ID = "$writer";
	/**
	 * virtual account id for all account
	 */
	public static final String ALL_ACCOUNT_ID = "$all";
	/**
	 * api configuration cache path
	 */
	private static final String CACHE_PATH_API_CONF = "/conf/twitter/api";
	/**
	 * the cached time of api configuration
	 */
	private static final String CACHE_PATH_API_CONF_MODIFIED = "/conf/twitter/api/modifiedTime";
	/**
	 * alive time for api configuration cache
	 */
	private static final int API_CONF_CACHE_TIME = 1000 * 60 * 60 * 24;

	private static String getAppended(StringBuilder builder, String appendedString) {
		int oldLength = builder.length();
		String ret = builder.append(appendedString).toString();
		builder.setLength(oldLength);
		return ret;
	}

	/**
	 * アカウントIDと通知Identifierから通知に使用するパスを生成する。
	 *
	 * @param accountId    アカウントID
	 * @param notifierName 通知Identifier
	 * @return パス
	 */
	public static String getPath(String accountId, String notifierName) {
		return accountId + ":" + notifierName;
	}

	/**
	 * 引数を取得する
	 *
	 * @param path パス
	 * @return 引数。ない場合はnull
	 */
	public static String getPathArg(String path) {
		int indexOf = path.indexOf('?');
		return indexOf < 0 ? null : path.substring(indexOf + 1);
	}

	/**
	 * 引数がない (?以降がカットされた) パスを取得する
	 *
	 * @param path パス
	 * @return 引数がないパス
	 */
	public static String getPathWithoutArg(String path) {
		int indexOf = path.indexOf('?');
		return indexOf < 0 ? path : path.substring(0, indexOf);
	}

	/**
	 * get recursive path from accountId and notifierName
	 *
	 * @param accountId    アカウントID
	 * @param notifierName notifier name
	 * @return パスの配列 (** / all..)
	 */
	public static String[] getRecursivePaths(String accountId, String notifierName) {
		TreeSet<String> paths = new TreeSet<>();
		getRecursivePaths(paths, accountId, notifierName);
		return paths.toArray(new String[paths.size()]);
	}

	/**
	 * get recursive path from accountId and notifierName, and add them into paths
	 *
	 * @param paths        path collection
	 * @param accountId    アカウントID
	 * @param notifierName notifier name
	 */
	public static void getRecursivePaths(Collection<String> paths, String accountId, String notifierName) {
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
		paths.add(getPath(ALL_ACCOUNT_ID, notifierName));
		paths.add(getPath(ALL_ACCOUNT_ID, "all"));
	}

	/*package*/final ClientConfiguration configuration;
	/** {String=notifierName, MessageChannelFactory=channelFactory} */
	protected HashMap<String, MessageChannelFactory> channelMap = new HashMap<>();
	/** {String=path, MessageChannel=channel} */
	protected LinkedHashMap<String, MessageChannel> pathMap = new LinkedHashMap<>();
	/** {K=path, V=listeners} */
	protected HashMap<String, ArrayList<ClientMessageListener>> pathListenerMap = new HashMap<>();
	/** {K=accountId, V=MessageChannels} */
	protected HashMap<String, ArrayList<MessageChannel>> userListenerMap = new HashMap<>();
	/*package*/ Twitter twitterForRead;
	/*package*/ ClientProperties configProperties;
	/*package*/ TwitterAPIConfiguration apiConfiguration;
	private volatile boolean isInitialized;
	private AtomicInteger modifiedCount = new AtomicInteger();

	/** インスタンスを生成する。 */
	public MessageBus() {
		this.configuration = ClientConfiguration.getInstance();
		configProperties = configuration.getConfigProperties();
		init(); // for tests
	}

	/**
	 * add channel factory
	 *
	 * @param notifierName notifier name
	 * @param factory      channel factory
	 * @return old channel factory
	 */
	public synchronized MessageChannelFactory addChannelFactory(String notifierName, MessageChannelFactory factory) {
		return channelMap.put(notifierName, factory);
	}

	/** お掃除する */
	public synchronized void cleanUp() {
		Collection<MessageChannel> entries = pathMap.values();
		for (MessageChannel entry : entries) {
			entry.disconnect();
		}
	}

	/**
	 * Channel からデータを取得するのをやめる
	 *
	 * @param accountId    アカウントID (long|$reader|$writer)
	 * @param notifierName 通知名。"my/timeline"など
	 * @param listener     リスナ
	 * @return 削除できたかどうか。
	 */
	public synchronized boolean dissolve(String accountId, String notifierName, ClientMessageListener listener) {
		String path = getPath(accountId, notifierName);

		ArrayList<ClientMessageListener> messageListeners = pathListenerMap.get(path);
		if (messageListeners != null && messageListeners.remove(listener)) {
			if (messageListeners.isEmpty()) {
				MessageChannel messageChannel = pathMap.remove(path);
				messageChannel.disconnect();
				ArrayList<MessageChannel> userListeners = userListenerMap.get(accountId);
				if (userListeners != null) {
					userListeners.remove(messageChannel);
				}
			}
			modifiedCount.incrementAndGet();
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Channel からデータを取得できるように登録する
	 *
	 * @param accountId    アカウントID (long|$reader|$writer)
	 * @param notifierName 通知名。"my/timeline"など
	 * @param listener     リスナ
	 * @return 登録できたかどうか。
	 */
	public synchronized boolean establish(String accountId, String notifierName, ClientMessageListener listener) {
		String path = getPath(accountId, notifierName);
		String pathArg = getPathArg(notifierName);

		ArrayList<ClientMessageListener> messageListeners = pathListenerMap.get(path);
		if (messageListeners == null) {
			messageListeners = new ArrayList<>();
			pathListenerMap.put(path, messageListeners);
		}
		messageListeners.add(listener);

		MessageChannel messageChannel = pathMap.get(path);
		boolean ret = true;
		// ALL_ACCOUNT_ID is virtual id, so we must not create any channel with that id.
		if (!accountId.equals(ALL_ACCOUNT_ID) && (messageChannel == null)) {
			MessageChannelFactory factory = channelMap.get(getPathWithoutArg(notifierName));
			if (factory == null) {
				if (!notifierName.endsWith("all")) {
					logger.warn("MessageChannel `{}' is not found.", notifierName);
					ret = false;
				}
				factory = NullMessageChannelFactory.INSTANCE;
			}
			messageChannel = factory.getInstance(this, accountId, notifierName, pathArg);
			pathMap.put(path, messageChannel);

			messageChannel.connect();
			if (isInitialized) {
				messageChannel.realConnect();
			}

			ArrayList<MessageChannel> userListeners = userListenerMap.get(accountId);
			if (userListeners == null) {
				userListeners = new ArrayList<>();
				userListenerMap.put(accountId, userListeners);
			}
			userListeners.add(messageChannel);
		}

		messageChannel.establish(listener);

		modifiedCount.incrementAndGet();
		return ret;
	}

	/**
	 * accountIdの実際のユーザーIDを取得する
	 *
	 * @param accountId アカウントID (long|$reader|$writer)
	 * @return Twitter User ID
	 */
	public long getActualUser(String accountId) {
		return Long.parseLong(getActualUserIdString(accountId));
	}

	/**
	 * accountIdの実際のユーザーIDを取得する
	 *
	 * @param accountId アカウントID (long|$reader|$writer)
	 * @return Twitter User ID
	 */
	public String getActualUserIdString(String accountId) {
		switch (accountId) {
			case READER_ACCOUNT_ID:
				return configuration.getAccountIdForRead();
			case WRITER_ACCOUNT_ID:
				return configuration.getAccountIdForWrite();
			case ALL_ACCOUNT_ID:
				throw new IllegalArgumentException(ALL_ACCOUNT_ID + " is not valid id");
			default:
				return accountId;
		}
	}

	/**
	 * 現在のREST API情報を取得する
	 *
	 * @return {@link TwitterAPIConfiguration}インスタンス。まだ取得されていない場合null。
	 */
	public TwitterAPIConfiguration getApiConfiguration() {
		return apiConfiguration;
	}

	/**
	 * get target endpoints
	 *
	 * you should use {@link #getListeners(String, String...)} or {@link #getListeners(String, boolean, String...)}
	 * because <code>getListeners</code> catch any Exceptions.
	 *
	 * @param paths endpoint path array
	 * @return actual listerners
	 */
	public synchronized ClientMessageListener[] getEndpoints(String[] paths) {
		ClientMessageListener[] listeners;
		HashSet<ClientMessageListener> listenersSet = new HashSet<>();
		for (String path : paths) {
			ArrayList<ClientMessageListener> list = pathListenerMap.get(path);
			if (list != null) {
				for (ClientMessageListener listener : list) {
					listenersSet.add(listener);
				}
			}
		}
		listeners = listenersSet.toArray(new ClientMessageListener[listenersSet.size()]);
		return listeners;
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
		return new VirtualMessagePublisher(this, recursive, accountId, notifierName);
	}

	/**
	 * get modified count. if this value is updated, you should re-retrieve listener cache
	 * @return modified count
	 */
	public int getModifiedCount() {
		return modifiedCount.get();
	}

	/**
	 * accountIdの{@link Configuration}を取得する。
	 *
	 * @param accountId アカウントID (long|$reader|$writer)
	 * @return Twitter Configuration
	 */
	public Configuration getTwitterConfiguration(String accountId) {
		return configuration.getTwitterConfiguration(getActualUserIdString(accountId));
	}

	/**
	 * initialize. this method is provided for test.
	 */
	protected void init() {
		twitterForRead = configuration.getTwitterForRead();
		scheduleGettingTwitterApiConfiguration();
	}

	/**
	 * アカウント変更通知
	 *
	 * @param forWrite 書き込み用アカウントが変更されたかどうか。
	 */
	public void onChangeAccount(boolean forWrite) {
		relogin(forWrite ? WRITER_ACCOUNT_ID : READER_ACCOUNT_ID, forWrite);
	}

	/** ClientTabの復元が完了し、DataFetcherの通知を受け取れるようになった */
	public synchronized void onInitialized() {
		isInitialized = true;
		for (MessageChannel channel : pathMap.values()) {
			channel.realConnect();
		}
	}

	private synchronized void relogin(String accountId, boolean forWrite) {
		modifiedCount.incrementAndGet();
		ArrayList<MessageChannel> clientMessageListeners = userListenerMap.get(accountId);
		if (clientMessageListeners == null) {
			return;
		}

		for (MessageChannel channel : clientMessageListeners) {
			channel.disconnect();
			channel.connect();
			if (isInitialized) {
				channel.realConnect();
			}
		}
		getListeners(accountId, "core").onClientMessage(
				forWrite ? TabRenderer.WRITER_ACCOUNT_CHANGED : TabRenderer.READER_ACCOUNT_CHANGED,
				accountId);
	}

	private void scheduleGettingTwitterApiConfiguration() {
		CacheStorage cacheStorage = configuration.getCacheStorage();
		if (cacheStorage.isDirEntry(CACHE_PATH_API_CONF)) {
			apiConfiguration = new ApiConfigurationImpl(cacheStorage.getDirEntry(CACHE_PATH_API_CONF));
			long modifiedTime = cacheStorage.readLong(CACHE_PATH_API_CONF_MODIFIED, -1L);
			if (modifiedTime + API_CONF_CACHE_TIME < System.currentTimeMillis()) {
				configuration.addJob(new ApiConfigurationFetcher());
			} else {
				logger.info("api configuration cache hit");
			}
		} else {
			configuration.addJob(new ApiConfigurationFetcher());
		}
	}
}

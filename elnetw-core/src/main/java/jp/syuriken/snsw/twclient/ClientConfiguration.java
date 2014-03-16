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

package jp.syuriken.snsw.twclient;

import java.awt.EventQueue;
import java.awt.TrayIcon;
import java.lang.reflect.Constructor;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import jp.syuriken.snsw.lib.parser.ArgParser;
import jp.syuriken.snsw.lib.parser.ParsedArguments;
import jp.syuriken.snsw.twclient.bus.MessageBus;
import jp.syuriken.snsw.twclient.config.ConfigFrameBuilder;
import jp.syuriken.snsw.twclient.filter.MessageFilter;
import jp.syuriken.snsw.twclient.gui.ClientTab;
import jp.syuriken.snsw.twclient.handler.IntentArguments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.UserMentionEntity;
import twitter4j.auth.AccessToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

/**
 * elnetw の情報などを格納するクラス。
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class ClientConfiguration {

	/** スクロール量のプロパティ名 */
	public static final String PROPERTY_LIST_SCROLL = "gui.list.scroll";
	/** フォーカスしたポストの色のプロパティ名 */
	public static final String PROPERTY_COLOR_FOCUS_LIST = "gui.color.list.focus";
	/**
	 * メンション判定の厳密な比較するかどうかのプロパティ名
	 *
	 * @deprecated you can use {@link #isMyAccount(long)}
	 */
	@Deprecated
	public static final String PROPERTY_ID_STRICT_MATCH = "core.id_strict_match";
	/** 情報の生存時間のプロパティ名 */
	public static final String PROPERTY_INFO_SURVIVE_TIME = "core.info.survive_time";
	/** UI更新間隔のプロパティ名 */
	public static final String PROPERTY_INTERVAL_POSTLIST_UPDATE = "gui.interval.list_update";
	/** アカウントリストのプロパティ名 */
	public static final String PROPERTY_ACCOUNT_LIST = "twitter.oauth.access_token.list";
	/** メンション取得の取得ステータス数のプロパティ名 */
	public static final String PROPERTY_PAGING_MENTIONS = "twitter.mention.count";
	/** メンション取得のページングのプロパティ名 */
	public static final String PROPERTY_INTERVAL_MENTIONS = "twitter.mention.interval";
	/** タイムライン取得の取得ステータス数のプロパティ名 */
	public static final String PROPERTY_PAGING_TIMELINE = "twitter.timeline.count";
	/** タイムライン取得間隔のプロパティ名 */
	public static final String PROPERTY_INTERVAL_TIMELINE = "twitter.timeline.interval";
	/** ダイレクトメッセージ取得の取得ステータス数のプロパティ名 */
	public static final String PROPERTY_PAGING_DIRECT_MESSAGES = "twitter.dm.count";
	/** ダイレクトメッセージ取得の取得間隔のプロパティ名 */
	public static final String PROPERTY_INTERVAL_DIRECT_MESSAGES = "twitter.dm.interval";
	/** 環境依存の改行コード */
	public static final String NEW_LINE = System.getProperty("line.separator");
	public static final Charset UTF8_CHARSET = Charset.forName("UTF-8");
	/** アプリケーション名 */
	public static final String APPLICATION_NAME = "elnetw";
	public static final String PROPERTY_GUI_FONT_DEFAULT = "gui.font.default";
	public static final String PROPERTY_GUI_FONT_UI = "gui.font.ui";
	private static ClientConfiguration INSTANCE;
	private static HashMap<String, Constructor<? extends ClientTab>> clientTabConstructorsMap =
			new HashMap<>();

	/**
	 * タブ復元に使用するコンストラクタ(ClientConfiguration, String)を取得する
	 *
	 * @param id タブID
	 * @return コンストラクタ。idに関連付けられたコンストラクタがない場合 <code>null</code>
	 */
	public static Constructor<? extends ClientTab> getClientTabConstructor(String id) {
		return clientTabConstructorsMap.get(id);
	}

	/**
	 * インスタンスを取得する。
	 *
	 * @return ClientConfiguration インスタンス
	 */
	public static synchronized ClientConfiguration getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new ClientConfiguration();
		}
		return INSTANCE;
	}

	/**
	 * タブ復元時に使用するコンストラクタを追加する。
	 * この関数は {@link #putClientTabConstructor(String, Constructor)} を内部で呼び出します。
	 * {@link ClientConfiguration} と {@link String} の2つの引数を持つコンストラクタがあるクラスである必要があります。
	 *
	 * @param id     タブ復元時に使用するID。タブクラスをFQCNで記述するといいでしょう。
	 * @param class1 タブ復元時にコンストラクタを呼ぶクラス
	 * @return 以前 id に関連付けられていたコンストラクタ
	 * @throws IllegalArgumentException 不正なコンストラクタ
	 * @see #putClientTabConstructor(String, Constructor)
	 */
	public static Constructor<? extends ClientTab> putClientTabConstructor(String id,
			Class<? extends ClientTab> class1) throws IllegalArgumentException {
		try {
			return putClientTabConstructor(id, class1.getConstructor(String.class));
		} catch (ReflectiveOperationException e) {
			throw new IllegalArgumentException("指定されたクラスはコンストラクタ(String)を持ちません", e);
		}
	}

	/**
	 * タブ復元時に使用するコンストラクタを追加する。
	 * コンストラクタは {@link String} ()の2つの引数を持つコンストラクタである必要があります。
	 *
	 * @param id          タブ復元時に使用するID。タブクラスをFQCNで記述するといいでしょう。
	 * @param constructor タブ復元時に呼ばれるコンストラクタ
	 * @return 以前 id に関連付けられていたコンストラクタ
	 */
	public static Constructor<? extends ClientTab> putClientTabConstructor(String id,
			Constructor<? extends ClientTab> constructor) {
		Class<?>[] parameterTypes = constructor.getParameterTypes();
		if (parameterTypes.length == 1 && parameterTypes[0].isAssignableFrom(String.class)) {
			return clientTabConstructorsMap.put(id, constructor);
		} else {
			throw new IllegalArgumentException(
					"ClientConfiguration#addClientTabConstructor: 渡されたコンストラクタは正しい型の引数を持ちません");
		}
	}

	/**
	 * テスト以外呼び出し禁止！
	 *
	 * @param conf インスタンス
	 */
	/*package*/
	static void setInstance(ClientConfiguration conf) {
		INSTANCE = conf;
	}

	private final List<ClientTab> tabsList = new ArrayList<>();
	private final Utility utility = new Utility(this);
	private final ReentrantReadWriteLock tabsListLock = new ReentrantReadWriteLock();
	private final transient JobQueue jobQueue = new JobQueue();
	private final Logger logger = LoggerFactory.getLogger(ClientConfiguration.class);
	private transient Hashtable<String, ActionHandler> actionHandlerTable = new Hashtable<>();
	/*package*/ ClientProperties configProperties;
	/*package*/ ClientProperties configDefaultProperties;
	/*package*/ ConcurrentHashMap<String, Twitter> cachedTwitterInstances = new ConcurrentHashMap<>();
	private volatile TrayIcon trayIcon;
	private TwitterClientFrame frameApi;
	private volatile boolean isInitializing = true;
	private ConfigFrameBuilder configBuilder;
	private volatile ImageCacher imageCacher;
	private volatile String accountIdForRead;
	private volatile String accountIdForWrite;
	private volatile MessageBus messageBus;
	private boolean portabledConfiguration;
	private volatile CacheManager cacheManager;
	private transient ScheduledExecutorService timer;
	private ClassLoader extraClassLoader;
	private CopyOnWriteArrayList<MessageFilter> messageFilters = new CopyOnWriteArrayList<>();
	private ParsedArguments parsedArguments;
	private ArgParser argParser;

	/** インスタンスを生成する。テスト以外この関数の直接の呼び出しは禁止。素直に {@link #getInstance()} */
	protected ClientConfiguration() {
	}

	/**
	 * アクションハンドラを追加する
	 *
	 * @param name    ハンドラ名
	 * @param handler ハンドラ
	 * @return 同名のハンドラが以前関連付けられていたらそのインスタンス、そうでない場合null
	 */
	public ActionHandler addActionHandler(String name, ActionHandler handler) {
		return actionHandlerTable.put(name, handler);
	}

	/**
	 * フィルタを追加する
	 *
	 * @param filter フィルター
	 */
	public void addFilter(MessageFilter filter) {
		messageFilters.add(filter);
	}

	/**
	 * 新しいタブを追加する。
	 *
	 * @param tab タブ
	 * @return 追加されたかどうか。
	 * @throws IllegalStateException {@link EventQueue#isDispatchThread()}がfalseを返す場合
	 */
	public boolean addFrameTab(final ClientTab tab) throws IllegalStateException {
		ensureRunningInDispatcherThread();

		if (tab == null) {
			return false;
		}
		boolean result;
		try {
			tabsListLock.writeLock().lock();
			result = tabsList.add(tab);
			frameApi.addTab(tab);
		} catch (RuntimeException e) {
			tabsList.remove(tab); //例外が発生したときはtabsListに追加しない
			throw e;
		} finally {
			tabsListLock.writeLock().unlock();
		}
		return result;
	}

	/**
	 * ジョブを追加する。 {@link ParallelRunnable}の場合は並列的に起動されます。
	 *
	 * @param priority 優先度
	 * @param job      ジョブ
	 */
	public void addJob(byte priority, Runnable job) {
		jobQueue.addJob(priority, job);
	}

	/**
	 * ジョブを追加する。 {@link ParallelRunnable}の場合は並列的に起動されます。
	 *
	 * @param job ジョブ
	 */
	public void addJob(Runnable job) {
		jobQueue.addJob(job);
	}

	private boolean checkValidAccountId(String accountId) {
		for (String account : getAccountList()) {
			if (account.equals(accountId)) {
				return true;
			}
		}
		return false;
	}

	private void ensureRunningInDispatcherThread() throws IllegalStateException {
		if (!(isInitializing || EventQueue.isDispatchThread())) {
			throw new IllegalStateException("Please run in EventDispatcherThread");
		}
	}

	/**
	 * 指定されたタブをフォーカスする。
	 *
	 * @param tab タブ
	 * @throws IllegalStateException {@link EventQueue#isDispatchThread()}がfalseを返す場合
	 */
	public void focusFrameTab(final ClientTab tab) throws IllegalStateException {
		ensureRunningInDispatcherThread();

		try {
			tabsListLock.readLock().lock();
			final int indexOf = tabsList.indexOf(tab);
			if (indexOf != -1) {
				frameApi.focusFrameTab(tab, indexOf);
			}
		} finally {
			tabsListLock.readLock().unlock();
		}
	}

	/**
	 * 読み込み用アカウントのIDを取得する。
	 *
	 * @return アカウントID (ユニーク)
	 */
	public synchronized String getAccountIdForRead() {
		return accountIdForRead;
	}

	/**
	 * 書き込み用アカウントのIDを取得する。
	 *
	 * @return アカウントID (ユニーク)
	 */
	public synchronized String getAccountIdForWrite() {
		return accountIdForWrite;
	}

	/**
	 * アカウントリストを取得する。リストがない場合長さ0の配列を返す。
	 *
	 * @return アカウントリスト。
	 */
	public String[] getAccountList() {
		return configProperties.getArray(PROPERTY_ACCOUNT_LIST);
	}

	/**
	 * アクションハンドラを取得する
	 *
	 * @param intent IntentArguments
	 * @return アクションハンドラ
	 */
	public ActionHandler getActionHandler(IntentArguments intent) {
		return actionHandlerTable.get(intent.getIntentName());
	}

	/**
	 * ArgParserインスタンスを取得する
	 * @return ArgParser
	 */
	public ArgParser getArgParser() {
		return argParser;
	}

	/**
	 * キャッシュマネージャを取得する。
	 *
	 * @return キャッシュマネージャ
	 */
	public synchronized CacheManager getCacheManager() {
		return cacheManager;
	}

	/**
	 * コンフィグビルダーを取得する。
	 *
	 * @return 設定ビルダー
	 */
	public synchronized ConfigFrameBuilder getConfigBuilder() {
		return configBuilder;
	}

	/**
	 * デフォルト設定を格納するプロパティを取得する。
	 *
	 * @return the configDefaultProperties
	 */
	public synchronized ClientProperties getConfigDefaultProperties() {
		return configDefaultProperties;
	}

	/**
	 * 現在のユーザー設定を格納するプロパティを取得する。
	 *
	 * @return the configProperties
	 */
	public synchronized ClientProperties getConfigProperties() {
		return configProperties;
	}

	/**
	 * 設定を格納するためのディレクトリを取得する。
	 *
	 * @return 設定を格納するディレクトリ
	 */
	public String getConfigRootDir() {
		return portabledConfiguration ? "." : System.getProperty("elnetw.home");
	}

	private String[] getConsumerPair() {
		String[] consumerPair;
		try {
			consumerPair
					= configProperties.getPrivateString("twitter.oauth.consumer_pair", "X4b:mZ\"p4").split(":");
		} catch (InvalidKeyException e) {
			throw new RuntimeException(e);
		}
		return consumerPair;
	}

	/**
	 * デフォルトで使用するアカウントのIDを取得する。
	 *
	 * @return アカウントのID (int)
	 */
	public String getDefaultAccountId() {
		String accountId = configProperties.getProperty("twitter.oauth.access_token.default");
		if (accountId == null || accountId.isEmpty()) {
			String[] accountList = getAccountList();
			if (accountList.length > 0) {
				accountId = accountList[0];
			} else {
				accountId = null;
			}
		}
		return accountId;
	}

	public synchronized ClassLoader getExtraClassLoader() {
		return extraClassLoader;
	}

	public MessageFilter[] getFilters() {
		return messageFilters.toArray(new MessageFilter[messageFilters.size()]);
	}

	/**
	 * FrameApiを取得する
	 *
	 * @return フレームAPI
	 */
	public ClientFrameApi getFrameApi() {
		return frameApi;
	}

	/**
	 * 指定されたインデックスのFrameTabを取得する。
	 *
	 * @param index インデックス
	 * @return FrameTab
	 */
	public ClientTab getFrameTab(int index) {
		ClientTab result = null;
		try {
			tabsListLock.readLock().lock();
			result = tabsList.get(index);
		} finally {
			tabsListLock.readLock().unlock();
		}
		return result;
	}

	/**
	 * 追加されているFrameTabの個数を数える
	 *
	 * @return 個数
	 */
	public int getFrameTabCount() {
		int size;
		try {
			tabsListLock.readLock().lock();
			size = tabsList.size();
		} finally {
			tabsListLock.readLock().unlock();
		}
		return size;
	}

	/*package*/List<ClientTab> getFrameTabs() {
		return tabsList;
	}

	/*package*/ReentrantReadWriteLock getFrameTabsLock() {
		return tabsListLock;
	}

	/**
	 * ImageCacherインスタンスを取得する。
	 *
	 * @return イメージキャッシャ
	 */
	public ImageCacher getImageCacher() {
		return imageCacher;
	}

	/**
	 * ジョブキューを取得する。
	 *
	 * @return ジョブキュー
	 */
	public JobQueue getJobQueue() {
		return jobQueue;
	}

	/**
	 * 情報取得のスケジューラを取得する。
	 *
	 * @return スケジューラ
	 */
	public synchronized MessageBus getMessageBus() {
		return messageBus;
	}

	/**
	 * 実行時引数から作られたParsedArgumentsを取得する
	 * @return ParsedArguments
	 */
	public ParsedArguments getParsedArguments() {
		return parsedArguments;
	}

	/**
	 * タイマーを取得する。
	 *
	 * @return タイマー
	 */
	public synchronized ScheduledExecutorService getTimer() {
		return timer;
	}

	/**
	 * TrayIconをかえす。nullの場合有り。
	 *
	 * @return トレイアイコン
	 */
	public synchronized TrayIcon getTrayIcon() {
		return trayIcon;
	}

	/**
	 * 指定したアカウントのTwitterインスタンスを取得する。
	 *
	 * @param accountId アカウントID
	 * @return Twitterインスタンス
	 */
	public Twitter getTwitter(String accountId) {
		Twitter twitter = cachedTwitterInstances.get(accountId);
		if (twitter == null) {
			twitter = new TwitterFactory(getTwitterConfiguration(accountId)).getInstance();
			Twitter twitter2 = cachedTwitterInstances.putIfAbsent(accountId, twitter);
			if (twitter2 != null) {
				return twitter2;
			}
		}
		return twitter;
	}

	/**
	 * デフォルトのアカウントのTwitterの {@link Configuration} インスタンスを取得する。
	 *
	 * @return Twitter Configuration
	 */
	public Configuration getTwitterConfiguration() {
		return getTwitterConfiguration(getDefaultAccountId());
	}

	/**
	 * 指定されたアカウントIDのTwitterの {@link Configuration} インスタンスを取得する。
	 *
	 * @param accountId アカウントID
	 * @return Twitter Configuration
	 */
	public Configuration getTwitterConfiguration(String accountId) {
		String[] accessToken;
		try {
			String accessTokenString = configProperties.getPrivateString("twitter.oauth.access_token." + accountId,
					"X4b:mZ\"p4");
			accessToken = accessTokenString.split(":");
		} catch (InvalidKeyException e) {
			throw new RuntimeException(e);
		}

		return getTwitterConfigurationBuilder() //
				.setOAuthAccessToken(accessToken[0]) //
				.setOAuthAccessTokenSecret(accessToken[1]) //
				.setOAuthConsumerKey(accessToken[2]) //
				.setOAuthConsumerSecret(accessToken[3]) //
				.build();
	}

	/**
	 * Twitterの {@link ConfigurationBuilder} インスタンスを取得する。
	 *
	 * @return Twitter ConfigurationBuilder
	 */
	public ConfigurationBuilder getTwitterConfigurationBuilder() {
		String[] consumerPair = getConsumerPair();
		String consumerKey = consumerPair[0];
		String consumerSecret = consumerPair[1];

		return new ConfigurationBuilder().setOAuthConsumerKey(consumerKey).setOAuthConsumerSecret(consumerSecret)
				.setUserStreamRepliesAllEnabled(configProperties.getBoolean("twitter.stream.replies_all"))
				.setJSONStoreEnabled(true).setClientVersion(VersionInfo.getUniqueVersion())
				.setClientURL(VersionInfo.getSupportUrl());
	}

	/**
	 * 読み込み用Twitterインスタンスを取得する。
	 *
	 * @return 読み込み用Twitterインスタンス
	 */
	public Twitter getTwitterForRead() {
		return getTwitter(getAccountIdForRead());
	}

	/**
	 * 書き込み用Twitterインスタンスを取得する。
	 *
	 * @return 読み込み用Twitterインスタンス。
	 */
	public Twitter getTwitterForWrite() {
		return getTwitter(getAccountIdForWrite());
	}

	/**
	 * Utilityインスタンスを取得する。
	 *
	 * @return インスタンス
	 */
	public Utility getUtility() {
		return utility;
	}

	public void handleAction(IntentArguments intentArguments) {
		ActionHandler actionHandler = getActionHandler(intentArguments);
		if (actionHandler != null) {
			try {
				actionHandler.handleAction(intentArguments);
				logger.trace("call {}", intentArguments);
			} catch (RuntimeException e) {
				logger.error("Uncaught exception", e);
			}
		} else {
			logger.warn("ActionHandler {} is not found", intentArguments.getIntentName());
		}
	}

	/**
	 * 指定したタブが選択されているかどうかを取得する
	 *
	 * @param tab タブ
	 * @return 選択されているかどうか
	 * @throws IllegalStateException {@link EventQueue#isDispatchThread()}がfalseを返す場合
	 */
	public boolean isFocusTab(ClientTab tab) throws IllegalStateException {
		ensureRunningInDispatcherThread();

		boolean result;
		try {
			tabsListLock.readLock().lock();
			int indexOf = tabsList.indexOf(tab);
			result = frameApi.isFocusTab(indexOf);
		} finally {
			tabsListLock.readLock().unlock();
		}
		return result;
	}

	/**
	 * 初期化中/初期TLロード中であるかどうかを返す。
	 *
	 * @return the isInitializing
	 */
	public boolean isInitializing() {
		return isInitializing;
	}

	/**
	 * IDが呼ばれたかどうかを判定する
	 *
	 * @param userMentionEntities エンティティ
	 * @return 呼ばれたかどうか
	 * @deprecated use {@link #isMentioned(long, twitter4j.UserMentionEntity[])}
	 */
	@Deprecated
	public boolean isMentioned(UserMentionEntity[] userMentionEntities) {
		if (userMentionEntities == null) {
			return false;
		}
		for (UserMentionEntity userMentionEntity : userMentionEntities) {
			if (configProperties.getBoolean(PROPERTY_ID_STRICT_MATCH)) {
				if (userMentionEntity.getId() == frameApi.getLoginUser().getId()) {
					return true;
				}
			} else {
				if (userMentionEntity.getScreenName().startsWith(frameApi.getLoginUser().getScreenName())) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * IDが呼ばれたかどうかを判定する
	 *
	 * @param accountId           ユーザーID (long)
	 * @param userMentionEntities エンティティ
	 * @return 呼ばれたかどうか
	 */
	public boolean isMentioned(long accountId, UserMentionEntity[] userMentionEntities) {
		if (userMentionEntities == null) {
			return false;
		}
		for (UserMentionEntity userMentionEntity : userMentionEntities) {
			if (configProperties.getBoolean(PROPERTY_ID_STRICT_MATCH)) {
				if (userMentionEntity.getId() == accountId) {
					return true;
				}
			} else {
				if (userMentionEntity.getScreenName().startsWith(cacheManager.getUser(accountId).getScreenName())) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * IDが呼ばれたかどうかを判定する
	 *
	 * @param accountId           アカウントID (long|$reader|$writer)
	 * @param userMentionEntities エンティティ
	 * @return 呼ばれたかどうか
	 */
	public boolean isMentioned(String accountId, UserMentionEntity[] userMentionEntities) {
		String userId;
		switch (accountId) {
			case MessageBus.READER_ACCOUNT_ID:
				userId = getAccountIdForRead();
				break;
			case MessageBus.WRITER_ACCOUNT_ID:
				userId = getAccountIdForWrite();
				break;
			default:
				userId = accountId;
				break;
		}
		return isMentioned(Long.parseLong(userId), userMentionEntities);
	}

	/**
	 * 自分のアカウントなのかを調べる
	 *
	 * @param id ユーザーユニークID ({@link User#getId()})
	 * @return 自分のアカウントかどうか
	 * @see #isMyAccount(String)
	 */
	public boolean isMyAccount(long id) {
		return isMyAccount(String.valueOf(id));
	}

	/**
	 * 自分のアカウントなのかを調べる
	 *
	 * @param accountId ユーザーユニークID
	 * @return 自分のアカウントかどうか
	 * @see #isMyAccount(long)
	 */
	public boolean isMyAccount(String accountId) {
		String[] accountList = getAccountList();
		for (String account : accountList) {
			if (accountId.equals(account)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * タブの表示を更新する。タブのタイトルを変更するときなどに使用してください。
	 *
	 * @param tab タブ
	 * @throws IllegalStateException {@link EventQueue#isDispatchThread()}がfalseを返す場合
	 */
	public void refreshTab(final ClientTab tab) throws IllegalStateException {
		ensureRunningInDispatcherThread();

		try {
			tabsListLock.readLock().lock();
			final int indexOf = tabsList.indexOf(tab);
			if (indexOf >= 0) {
				frameApi.refreshTab(indexOf, tab);
			}
		} finally {
			tabsListLock.readLock().unlock();
		}
	}

	/**
	 * タブを削除する
	 *
	 * @param tab タブ
	 * @return 削除されたかどうか。
	 * @throws IllegalStateException {@link EventQueue#isDispatchThread()}がfalseを返す場合
	 */
	public boolean removeFrameTab(final ClientTab tab) throws IllegalStateException {
		ensureRunningInDispatcherThread();

		final int indexOf;
		try {
			tabsListLock.writeLock().lock();
			indexOf = tabsList.indexOf(tab);
			if (indexOf != -1) {
				tabsList.remove(indexOf);
				frameApi.removeFrameTab(indexOf, tab);
			}
		} finally {
			tabsListLock.writeLock().unlock();
		}
		return indexOf != -1;
	}

	/**
	 * 読み込み用アカウントを設定する
	 *
	 * @param accountId アカウントID。ユニーク。
	 * @return 古い読み込み用アカウントID。
	 */
	public synchronized String setAccountIdForRead(String accountId) {
		if (!checkValidAccountId(accountId)) {
			throw new IllegalArgumentException("accountId is not in user's accounts: " + accountId);
		}
		String old = accountIdForRead;
		if (old == null || !old.equals(accountId)) {
			accountIdForRead = accountId;
			if (messageBus != null) {
				messageBus.onChangeAccount(false);
			}
		}
		return old;
	}

	/**
	 * 書き込み用アカウントを設定する。
	 *
	 * @param accountId アカウントID。ユニーク
	 * @return 古い書き込み用アカウントID。
	 */
	public synchronized String setAccountIdForWrite(String accountId) {
		if (!checkValidAccountId(accountId)) {
			throw new IllegalArgumentException("accountId is not in user's accounts: " + accountId);
		}
		String old = accountIdForWrite;
		if (old == null || !old.equals(accountId)) {
			accountIdForWrite = accountId;
			if (messageBus != null) {
				messageBus.onChangeAccount(true);
			}
		}
		return old;
	}

	/*package*/ void setArgParser(ArgParser argParser) {
		this.argParser = argParser;
	}

	/*package*/
	synchronized void setCacheManager(CacheManager cacheManager) {
		this.cacheManager = cacheManager;
	}

	/*package*/
	synchronized void setConfigBuilder(ConfigFrameBuilder configBuilder) {
		this.configBuilder = configBuilder;
	}

	/**
	 * デフォルト設定を格納するプロパティを設定する。
	 *
	 * @param configDefaultProperties the configDefaultProperties to set
	 */
	public synchronized void setConfigDefaultProperties(ClientProperties configDefaultProperties) {
		this.configDefaultProperties = configDefaultProperties;
	}

	/**
	 * 現在のユーザー設定を格納するプロパティを設定する。
	 *
	 * @param configProperties the configProperties to set
	 */
	public synchronized void setConfigProperties(ClientProperties configProperties) {
		this.configProperties = configProperties;
	}

	/*package*/
	synchronized void setExtraClassLoader(ClassLoader extraClassLoader) {
		this.extraClassLoader = extraClassLoader;
	}

	/**
	 * FrameApiを設定する
	 *
	 * @param frameApi フレームAPI
	 */
	/*package*/void setFrameApi(TwitterClientFrame frameApi) {
		this.frameApi = frameApi;
	}

	/*package*/void setImageCacher(ImageCacher imageCacher) {
		this.imageCacher = imageCacher;
	}

	/**
	 * 初期化中/初期TLロード中であるかを設定する
	 *
	 * @param isInitializing 初期化中かどうか。
	 */
	/*package*/void setInitializing(boolean isInitializing) {
		this.isInitializing = isInitializing;
	}

	/*package*/
	synchronized void setMessageBus(MessageBus messageBus) {
		this.messageBus = messageBus;
	}

	/*package*/ void setParsedArguments(ParsedArguments parsedArguments) {
		this.parsedArguments = parsedArguments;
	}

	/*package*/void setPortabledConfiguration(boolean portable) {
		portabledConfiguration = portable;
	}

	/*package*/
	synchronized void setTimer(ScheduledExecutorService timer) {
		this.timer = timer;
	}

	/**
	 * トレイアイコン
	 *
	 * @param trayIcon the trayIcon to set
	 */
	public synchronized void setTrayIcon(TrayIcon trayIcon) {
		this.trayIcon = trayIcon;
	}

	/**
	 * store User's access token.
	 *
	 * @param accessToken accessToken Instance
	 */
	public void storeAccessToken(AccessToken accessToken) {
		String[] consumerKeys = getConsumerPair();
		StringBuilder stringBuilder = new StringBuilder().append(accessToken.getToken()).append(':').append(
				accessToken.getTokenSecret()).append(':').append(consumerKeys[0]).append(':').append(consumerKeys[1]);
		try {
			configProperties.setPrivateString("twitter.oauth.access_token." + accessToken.getUserId(),
					stringBuilder.toString(), "X4b:mZ\"p4");
		} catch (InvalidKeyException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * OAuthトークンの取得を試みる。実行中のスレッドをブロックします。
	 *
	 * @return 取得を試して発生した例外。ない場合はnull
	 */
	public Exception tryGetOAuthToken() {
		Twitter twitter;
		try {
			twitter = new OAuthHelper(this).show();
		} catch (TwitterException e) {
			return e;
		}

		if (twitter == null) { // canceled
			return null;
		}

		//将来の参照用に accessToken を永続化する
		String userId;
		try {
			userId = String.valueOf(twitter.getId());
		} catch (TwitterException e1) {
			return e1;
		}
		synchronized (configProperties) {
			String[] accountList = getAccountList();
			boolean updateAccountList = true;
			for (String accountId : accountList) {
				if (accountId.equals(userId)) {
					updateAccountList = false;
					break;
				}
			}
			if (updateAccountList) {
				configProperties.setProperty("twitter.oauth.access_token.list", MessageFormat.format("{0} {1}",
						configProperties.getProperty("twitter.oauth.access_token.list"), userId));
			}
			try {
				storeAccessToken(twitter.getOAuthAccessToken());
			} catch (TwitterException e) {
				return e;
			}
			configProperties.store();
		}
		return null;
	}
}

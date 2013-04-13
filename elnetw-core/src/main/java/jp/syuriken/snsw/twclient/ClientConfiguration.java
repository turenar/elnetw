package jp.syuriken.snsw.twclient;

import java.awt.EventQueue;
import java.awt.TrayIcon;
import java.lang.reflect.Constructor;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import jp.syuriken.snsw.twclient.config.ConfigFrameBuilder;
import jp.syuriken.snsw.twclient.filter.MessageFilter;
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
 * @author Turenar <snswinhaiku dot lo at gmail dot com>
 */
public class ClientConfiguration {

	/** スクロール量のプロパティ名 */
	public static final String PROPERTY_LIST_SCROLL = "gui.list.scroll";

	/** フォーカスしたポストの色のプロパティ名 */
	public static final String PROPERTY_COLOR_FOCUS_LIST = "gui.color.list.focus";

	/** メンション判定の厳密な比較するかどうかのプロパティ名 */
	public static final String PROPERTY_ID_STRICT_MATCH = "core.id_strict_match";

	/** 情報の生存時間のプロパティ名 */
	public static final String PROPERTY_INFO_SURVIVE_TIME = "core.info.survive_time";

	/** UI更新間隔のプロパティ名 */
	public static final String PROPERTY_INTERVAL_POSTLIST_UPDATE = "gui.interval.list_update";

	/** ダイレクトメッセージ初期取得のページングのプロパティ名 */
	public static final String PROPERTY_PAGING_INITIAL_DIRECTMESSAGE = "twitter.page.initial_dm";

	/** アカウントリストのプロパティ名 */
	public static final String PROPERTY_ACCOUNT_LIST = "twitter.oauth.access_token.list";

	/** メンション初期取得のページングのプロパティ名 */
	public static final String PROPERTY_PAGING_INITIAL_MENTION = "twitter.page.initial_mention";

	/** タイムライン取得間隔のプロパティ名 */
	public static final String PROPERTY_INTERVAL_TIMELINE = "twitter.interval.timeline";

	/** タイムライン取得のページングの更新間隔 */
	public static final String PROPERTY_PAGING_TIMELINE = "twitter.page.timeline";

	/** タイムライン初期取得のページングのプロパティ名 */
	public static final String PROPERTY_PAGING_INITIAL_TIMELINE = "twitter.page.initial_timeline";

	/** 環境依存の改行コード */
	public static final String NEW_LINE = System.getProperty("line.separator");

	public static final Charset UTF8_CHARSET;

	static {
		UTF8_CHARSET = Charset.forName("UTF-8");
	}

	/** アプリケーション名 */
	public static final String APPLICATION_NAME = "elnetw";

	private static final String HOME_BASE_DIR = System.getProperty("user.home") + "/.elnetw";

	private static HashMap<String, Constructor<? extends ClientTab>> clientTabConstructorsMap =
			new HashMap<String, Constructor<? extends ClientTab>>();

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
	 * タブ復元時に使用するコンストラクタを追加する。
	 * この関数は {@link #putClientTabConstructor(String, Constructor)} を内部で呼び出します。
	 * {@link ClientConfiguration} と {@link String} の2つの引数を持つコンストラクタがあるクラスである必要があります。
	 *
	 * @param id     タブ復元時に使用するID。タブクラスをFQCNで記述するといいでしょう。
	 * @param class1 タブ復元時にコンストラクタを呼ぶクラス
	 * @return 以前 id に関連付けられていたコンストラクタ
	 * @see #putClientTabConstructor(String, Constructor)
	 */
	public static Constructor<? extends ClientTab> putClientTabConstructor(String id,
			Class<? extends ClientTab> class1) {
		try {
			return putClientTabConstructor(id, class1.getConstructor(ClientConfiguration.class, String.class));
		} catch (Exception e) {
			throw new IllegalArgumentException("指定されたクラスはコンストラクタ(ClientConfiguration, String)を持ちません", e);
		}
	}

	/**
	 * タブ復元時に使用するコンストラクタを追加する。
	 * コンストラクタは {@link ClientConfiguration} と {@link String} の2つの引数を持つコンストラクタである必要があります。
	 *
	 * @param id          タブ復元時に使用するID。タブクラスをFQCNで記述するといいでしょう。
	 * @param constructor タブ復元時に呼ばれるコンストラクタ
	 * @return 以前 id に関連付けられていたコンストラクタ
	 */
	public static Constructor<? extends ClientTab> putClientTabConstructor(String id,
			Constructor<? extends ClientTab> constructor) {
		Class<?>[] parameterTypes = constructor.getParameterTypes();
		if (parameterTypes.length == 2 && parameterTypes[0].isAssignableFrom(ClientConfiguration.class)
				&& parameterTypes[1].isAssignableFrom(String.class)) {
			return clientTabConstructorsMap.put(id, constructor);
		} else {
			throw new IllegalArgumentException(
					"ClientConfiguration#addClientTabConstructor: 渡されたコンストラクタは正しい型の引数を持ちません");
		}
	}

	private final List<ClientTab> tabsList = new ArrayList<ClientTab>();

	private final Utility utility = new Utility(this);

	private final ReentrantReadWriteLock tabsListLock = new ReentrantReadWriteLock();

	private final transient JobQueue jobQueue = new JobQueue();

	private transient Hashtable<String, ActionHandler> actionHandlerTable = new Hashtable<String, ActionHandler>();

	/*package*/ ClientProperties configProperties;

	/*package*/ ClientProperties configDefaultProperties;

	/*package*/ ConcurrentHashMap<String, Twitter> cachedTwitterInstances = new ConcurrentHashMap<String, Twitter>();

	private TrayIcon trayIcon;

	private boolean isShutdownPhase = false;

	private TwitterClientFrame frameApi;

	private boolean isInitializing = true;

	private ConfigFrameBuilder configBuilder;

	private volatile FilterService rootFilterService;

	private volatile ImageCacher imageCacher;

	private volatile String accountIdForRead;

	private volatile String accountIdForWrite;

	private TwitterDataFetchScheduler fetchScheduler;

	private boolean portabledConfiguration;

	private volatile CacheManager cacheManager;

	private List<String> args;

	private transient Timer timer;

	private ClassLoader extraClassLoader;

	/**
	 * インスタンスを生成する。
	 *
	 */
	protected ClientConfiguration() {
	}

	/**
	 * アクションハンドラを追加する
	 *
	 * @param name ハンドラ名
	 * @param handler ハンドラ
	 * @return 同名のハンドラが以前関連付けられていたらそのインスタンス、そうでない場合null
	 */
	public ActionHandler addActionHandler(String name, ActionHandler handler) {
		return actionHandlerTable.put(name, handler);
	}

	/**
	 * フィルタを追加する
	 *
	 * @param rootFilter フィルター
	 */
	public void addFilter(MessageFilter rootFilter) {
		getRootFilterService().addFilter(rootFilter);
	}

	/**
	 * 新しいタブを追加する。
	 *
	 * @throws IllegalStateException {@link EventQueue#isDispatchThread()}がfalseを返す場合
	 * @param tab タブ
	 * @return 追加されたかどうか。
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
	public void addJob(JobQueue.Priority priority, Runnable job) {
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
			if (Utility.equalString(account, accountId)) {
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
	 * @throws IllegalStateException {@link EventQueue#isDispatchThread()}がfalseを返す場合
	 * @param tab タブ
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
	public String getAccountIdForRead() {
		return accountIdForRead;
	}

	/**
	 * 書き込み用アカウントのIDを取得する。
	 *
	 * @return アカウントID (ユニーク)
	 */
	public String getAccountIdForWrite() {
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
	 * @param name アクション名。!を含んでいても可
	 * @return アクションハンドラ
	 */
	public ActionHandler getActionHandler(String name) {
		int indexOf = name.indexOf('!');
		String commandName = indexOf < 0 ? name : name.substring(0, indexOf);
		ActionHandler actionHandler = actionHandlerTable.get(commandName);
		return actionHandler;
	}

	/**
	 * キャッシュマネージャを取得する。
	 *
	 * @return キャッシュマネージャ
	 */
	public CacheManager getCacheManager() {
		return cacheManager;
	}

	/**
	 * コンフィグビルダーを取得する。
	 *
	 * @return 設定ビルダー
	 */
	public ConfigFrameBuilder getConfigBuilder() {
		return configBuilder;
	}

	/**
	 * デフォルト設定を格納するプロパティを取得する。
	 *
	 * @return the configDefaultProperties
	 */
	public ClientProperties getConfigDefaultProperties() {
		return configDefaultProperties;
	}

	/**
	 * 現在のユーザー設定を格納するプロパティを取得する。
	 *
	 * @return the configProperties
	 */
	public ClientProperties getConfigProperties() {
		return configProperties;
	}

	/**
	 * 設定を格納するためのディレクトリを取得する。
	 *
	 * @return 設定を格納するディレクトリ
	 */
	public String getConfigRootDir() {
		return portabledConfiguration ? "." : HOME_BASE_DIR;
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

	public ClassLoader getExtraClassLoader() {
		return extraClassLoader;
	}

	/**
	 * 情報取得のスケジューラを取得する。
	 *
	 * @return スケジューラ
	 */
	public TwitterDataFetchScheduler getFetchScheduler() {
		return fetchScheduler;
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
	 * @return ジョブキュー
	 */
	public JobQueue getJobQueue() {
		return jobQueue;
	}

	/**
	 * アプリケーション実行時に指定されたオプションの変更できないリストを取得する。
	 * なお、内容はGetoptによって並び替えられている
	 *
	 * @return unmodifiable List
	 */
	public List<String> getOpts() {
		return Collections.unmodifiableList(args);
	}

	/**
	 * すべての入力をフィルターするクラスを取得する。
	 *
	 * @return フィルター
	 */
	public FilterService getRootFilterService() {
		return rootFilterService;
	}

	/**
	 * タイマーを取得する。
	 *
	 * @return タイマー
	 * @Deprecated {@link ClientConfiguration#getTimer()}
	 */
	public Timer getTimer() {
		return timer;
	}

	/**
	 * TrayIconをかえす。nullの場合有り。
	 *
	 * @return トレイアイコン
	 */
	public TrayIcon getTrayIcon() {
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

	/**
	 * 指定したタブが選択されているかどうかを取得する
	 *
	 * @throws IllegalStateException {@link EventQueue#isDispatchThread()}がfalseを返す場合
	 * @param tab タブ
	 * @return 選択されているかどうか
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
	 */
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
			if (Utility.equalString(accountId, account)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * シャットダウンフェーズかどうかを取得する。
	 *
	 * @return シャットダウンフェーズかどうか
	 */
	public boolean isShutdownPhase() {
		return isShutdownPhase;
	}

	/**
	 * タブの表示を更新する。タブのタイトルを変更するときなどに使用してください。
	 *
	 * @throws IllegalStateException {@link EventQueue#isDispatchThread()}がfalseを返す場合
	 * @param tab タブ
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
	 * @throws IllegalStateException {@link EventQueue#isDispatchThread()}がfalseを返す場合
	 * @param tab タブ
	 * @return 削除されたかどうか。
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
	public String setAccountIdForRead(String accountId) {
		if (checkValidAccountId(accountId) == false) {
			throw new IllegalArgumentException("accountId is not in user's accounts: " + accountId);
		}
		String old = accountIdForRead;
		if (old == null || old.equals(accountId) == false) {
			accountIdForRead = accountId;
			if (rootFilterService != null) {
				rootFilterService.onChangeAccount(false);
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
	public String setAccountIdForWrite(String accountId) {
		if (checkValidAccountId(accountId) == false) {
			throw new IllegalArgumentException("accountId is not in user's accounts: " + accountId);
		}
		String old = accountIdForWrite;
		if (old == null || old.equals(accountId) == false) {
			accountIdForWrite = accountId;
			if (rootFilterService != null) {
				rootFilterService.onChangeAccount(true);
			}
		}
		return old;
	}

	/*package*/void setCacheManager(CacheManager cacheManager) {
		this.cacheManager = cacheManager;
	}

	/*package*/ void setConfigBuilder(ConfigFrameBuilder configBuilder) {
		this.configBuilder = configBuilder;
	}

	/**
	 * デフォルト設定を格納するプロパティを設定する。
	 *
	 * @param configDefaultProperties the configDefaultProperties to set
	 */
	public void setConfigDefaultProperties(ClientProperties configDefaultProperties) {
		this.configDefaultProperties = configDefaultProperties;
	}

	/**
	 * 現在のユーザー設定を格納するプロパティを設定する。
	 *
	 * @param configProperties the configProperties to set
	 */
	public void setConfigProperties(ClientProperties configProperties) {
		this.configProperties = configProperties;
	}

	/*package*/ void setExtraClassLoader(ClassLoader extraClassLoader) {
		this.extraClassLoader = extraClassLoader;
	}

	/*package*/void setFetchScheduler(TwitterDataFetchScheduler fetchScheduler) {
		this.fetchScheduler = fetchScheduler;
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

	/*package*/void setOpts(String[] args) {
		this.args = Arrays.asList(args);
	}

	/*package*/void setPortabledConfiguration(boolean portable) {
		portabledConfiguration = portable;
	}

	/*package*/ void setRootFilterService(FilterService service) {
		rootFilterService = service;
	}

	/**
	 * シャットダウンフェーズであるかどうかを設定する
	 *
	 * @param isShutdownPhase シャットダウンフェーズかどうか。
	 */
	public void setShutdownPhase(boolean isShutdownPhase) {
		this.isShutdownPhase = isShutdownPhase;
	}

	/*package*/ void setTimer(Timer timer) {
		this.timer = timer;
	}

	/**
	 * トレイアイコン
	 *
	 * @param trayIcon the trayIcon to set
	 */
	public void setTrayIcon(TrayIcon trayIcon) {
		this.trayIcon = trayIcon;
	}

	/**
	 * store User's access token.
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
			twitter = new OAuthFrame(this).show();
		} catch (TwitterException e) {
			return e;
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

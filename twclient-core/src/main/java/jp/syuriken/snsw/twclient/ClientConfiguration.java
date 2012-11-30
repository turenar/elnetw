package jp.syuriken.snsw.twclient;

import java.awt.Color;
import java.awt.TrayIcon;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.imageio.ImageIO;

import jp.syuriken.snsw.twclient.config.ConfigFrameBuilder;
import jp.syuriken.snsw.twclient.filter.MessageFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import twitter4j.Paging;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.UserMentionEntity;
import twitter4j.auth.AccessToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

/**
 * twclient の情報などを格納するクラス。
 * 
 * @author $Author$
 */
public class ClientConfiguration {
	
	/**
	 * よく使いそうな設定をキャッシュしておくクラス
	 * 
	 * @author $Author$
	 */
	public class ConfigData implements PropertyChangeListener {
		
		private static final String PROPERTY_PAGING_INITIAL_DIRECTMESSAGE = "twitter.page.initial_dm";
		
		private static final String PROPERTY_ACCOUNT_LIST = "twitter.oauth.access_token.list";
		
		private static final String PROPERTY_PAGING_INITIAL_MENTION = "twitter.page.initial_mention";
		
		private static final String PROPERTY_INTERVAL_TIMELINE = "twitter.interval.timeline";
		
		private static final String PROPERTY_PAGING_TIMELINE = "twitter.page.timeline";
		
		private static final String PROPERTY_PAGING_INITIAL_TIMELINE = "twitter.page.initial_timeline";
		
		private static final String PROPERTY_INTERVAL_POSTLIST_UPDATE = "gui.interval.list_update";
		
		private static final String PROPERTY_LIST_SCROLL = "gui.list.scroll";
		
		private static final String PROPERTY_COLOR_FOCUS_LIST = "gui.color.list.focus";
		
		private static final String PROPERTY_ID_STRICT_MATCH = "core.id_strict_match";
		
		private static final String PROPERTY_INFO_SURVIVE_TIME = "core.info.survive_time";
		
		/** UI更新間隔 */
		public int intervalOfPostListUpdate = configProperties.getInteger(PROPERTY_INTERVAL_POSTLIST_UPDATE);
		
		/** タイムライン取得間隔 */
		public int intervalOfGetTimeline = configProperties.getInteger(PROPERTY_INTERVAL_TIMELINE);
		
		/** フォーカスしたポストの色 */
		public Color colorOfFocusList = configProperties.getColor(PROPERTY_COLOR_FOCUS_LIST);
		
		/** タイムライン取得のページング */
		public Paging pagingOfGettingTimeline = new Paging().count(configProperties
			.getInteger(PROPERTY_PAGING_TIMELINE));
		
		/** タイムライン初期取得のページング */
		public Paging pagingOfGettingInitialTimeline = new Paging().count(configProperties
			.getInteger(PROPERTY_PAGING_INITIAL_TIMELINE));
		
		/** メンション判定の厳密な比較 */
		public boolean mentionIdStrictMatch = configProperties.getBoolean(PROPERTY_ID_STRICT_MATCH);
		
		/** スクロール量 */
		public int scrollAmount = configProperties.getInteger(PROPERTY_LIST_SCROLL);
		
		/** 情報の生存時間 */
		public int timeOfSurvivingInfo = configProperties.getInteger(PROPERTY_INFO_SURVIVE_TIME);
		
		/** メンション初期取得のページング */
		public Paging pagingOfGettingInitialMentions = new Paging().count(configProperties
			.getInteger(PROPERTY_PAGING_INITIAL_MENTION));
		
		/** アカウントリスト */
		public String[] accountList = initAccountList();
		
		/** ダイレクトメッセージ初期取得のページング */
		public Paging pagingOfGettingInitialDirectMessage = new Paging().count(configProperties
			.getInteger(PROPERTY_PAGING_INITIAL_DIRECTMESSAGE));
		
		
		/*package*/ConfigData() {
			configProperties.addPropertyChangedListener(this);
		}
		
		/**
		 * アカウントリストを取得する。リストがない場合長さ0の配列を返す。
		 * 
		 * @return アカウントリスト。
		 */
		private String[] initAccountList() {
			cachedTwitterInstances.clear();
			String accountListString = configProperties.getProperty(PROPERTY_ACCOUNT_LIST);
			if (accountListString == null) {
				return new String[] {};
			} else {
				return accountListString.split(" ");
			}
		}
		
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			String name = evt.getPropertyName();
			if (Utility.equalString(name, PROPERTY_INTERVAL_POSTLIST_UPDATE)) {
				intervalOfPostListUpdate = configProperties.getInteger(PROPERTY_INTERVAL_POSTLIST_UPDATE);
			} else if (Utility.equalString(name, PROPERTY_INTERVAL_TIMELINE)) {
				intervalOfGetTimeline = configProperties.getInteger(PROPERTY_INTERVAL_TIMELINE);
			} else if (Utility.equalString(name, PROPERTY_COLOR_FOCUS_LIST)) {
				colorOfFocusList = configProperties.getColor(PROPERTY_COLOR_FOCUS_LIST);
			} else if (Utility.equalString(name, PROPERTY_PAGING_TIMELINE)) {
				pagingOfGettingTimeline = new Paging().count(configProperties.getInteger(PROPERTY_PAGING_TIMELINE));
			} else if (Utility.equalString(name, PROPERTY_PAGING_INITIAL_TIMELINE)) {
				pagingOfGettingInitialTimeline =
						new Paging().count(configProperties.getInteger(PROPERTY_PAGING_INITIAL_TIMELINE));
			} else if (Utility.equalString(name, PROPERTY_ID_STRICT_MATCH)) {
				mentionIdStrictMatch = configProperties.getBoolean(PROPERTY_ID_STRICT_MATCH);
			} else if (Utility.equalString(name, PROPERTY_LIST_SCROLL)) {
				scrollAmount = configProperties.getInteger(PROPERTY_LIST_SCROLL);
			} else if (Utility.equalString(name, PROPERTY_INFO_SURVIVE_TIME)) {
				timeOfSurvivingInfo = configProperties.getInteger(PROPERTY_INFO_SURVIVE_TIME);
			} else if (Utility.equalString(name, PROPERTY_PAGING_INITIAL_MENTION)) {
				timeOfSurvivingInfo = configProperties.getInteger(PROPERTY_PAGING_INITIAL_MENTION);
			} else if (Utility.equalString(name, PROPERTY_ACCOUNT_LIST)) {
				accountList = initAccountList();
			} else if (Utility.equalString(name, PROPERTY_PAGING_INITIAL_DIRECTMESSAGE)) {
				pagingOfGettingInitialDirectMessage =
						new Paging().count(configProperties.getInteger(PROPERTY_PAGING_INITIAL_DIRECTMESSAGE));
			}
		}
	}
	
	
	private static HashMap<String, Constructor<? extends ClientTab>> clientTabConstructorsMap =
			new HashMap<String, Constructor<? extends ClientTab>>();
	
	private static final Logger logger = LoggerFactory.getLogger(ClientConfiguration.class);
	
	
	/**
	 * タブ復元に使用するコンストラクタ(ClientConfiguration, String)を取得する
	 * 
	 * @param id タブID
	 * @return コンストラクタ。idに関連付けられたコンストラクタがない場合 <code>null</code>
	 */
	public static Constructor<? extends ClientTab> getClientTabConstructor(String id) {
		return clientTabConstructorsMap.get(id);
	}
	
	private static void init(ClientConfiguration instance, boolean b) {
		synchronized (ClientConfiguration.class) {
			if (ClientConfiguration.instance == null) {
				ClientConfiguration.instance = instance;
			} else if (b) {
				logger.error("ClientConfigurationが複数インスタンス生成されようとしています。");
			}
		}
	}
	
	/**
	 * タブ復元時に使用するコンストラクタを追加する。
	 * この関数は {@link #putClientTabConstructor(String, Constructor)} を内部で呼び出します。
	 * {@link ClientConfiguration} と {@link String} の2つの引数を持つコンストラクタがあるクラスである必要があります。
	 * @param id タブ復元時に使用するID。タブクラスをFQCNで記述するといいでしょう。
	 * @param class1 タブ復元時にコンストラクタを呼ぶクラス
	 * @return 以前 id に関連付けられていたコンストラクタ
	 * @see #putClientTabConstructor(String, Constructor)
	 */
	public static Constructor<? extends ClientTab> putClientTabConstructor(String id, Class<? extends ClientTab> class1) {
		try {
			return putClientTabConstructor(id, class1.getConstructor(ClientConfiguration.class, String.class));
		} catch (Exception e) {
			throw new IllegalArgumentException("指定されたクラスはコンストラクタ(ClientConfiguration, String)を持ちません", e);
		}
	}
	
	/**
	 * タブ復元時に使用するコンストラクタを追加する。
	 * コンストラクタは {@link ClientConfiguration} と {@link String} の2つの引数を持つコンストラクタである必要があります。
	 * @param id タブ復元時に使用するID。タブクラスをFQCNで記述するといいでしょう。
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
			throw new IllegalArgumentException("ClientConfiguration#addClientTabConstructor: 渡されたコンストラクタは正しい型の引数を持ちません");
		}
	}
	
	
	private TrayIcon trayIcon;
	
	private ClientProperties configProperties;
	
	private ClientProperties configDefaultProperties;
	
	private boolean isShutdownPhase = false;
	
	private TwitterClientFrame frameApi;
	
	private final List<ClientTab> tabsList = new ArrayList<ClientTab>();
	
	private final Utility utility = new Utility(this);
	
	private boolean isInitializing = true;
	
	private ConfigFrameBuilder configBuilder = new ConfigFrameBuilder(this);
	
	private final ReentrantReadWriteLock tabsListLock = new ReentrantReadWriteLock();
	
	private volatile FilterService rootFilterService;
	
	private volatile ImageCacher imageCacher;
	
	private volatile ConfigData configData;
	
	private ConcurrentHashMap<String, Twitter> cachedTwitterInstances = new ConcurrentHashMap<String, Twitter>();
	
	private String accountIdForRead;
	
	private String accountIdForWrite;
	
	private TwitterDataFetchScheduler fetchScheduler;
	
	private boolean portabledConfiguration;
	
	private static final String HOME_BASE_DIR = System.getProperty("user.home") + "/.turetwcl";
	
	private volatile CacheManager cacheManager;
	
	private Object lockObject = new Object();
	
	private static ClientConfiguration instance;
	
	
	/**
	 * {@link ClientConfiguration} インスタンスを返す。一度もインスタンスが生成されていないときは <code>null</code>
	 * 
	 * @return インスタンス
	 */
	public static ClientConfiguration getInstance() {
		return instance;
	}
	
	/**
	 * インスタンスを生成する。
	 * 
	 */
	protected ClientConfiguration() {
		init(this, true);
		rootFilterService = new FilterService(this);
		try {
			trayIcon =
					new TrayIcon(ImageIO.read(getClass().getClassLoader().getResourceAsStream(
							"jp/syuriken/snsw/twclient/img/icon16.png")), TwitterClientFrame.APPLICATION_NAME);
		} catch (IOException e) {
			logger.error("icon ファイルの読み込みに失敗。");
			trayIcon = null;
		}
	}
	
	/**
	 * <b style="color:red;">テスト用</b>インスタンスを生成する。HeadlessExceptionを無視
	 * 
	 * @param register {@link ClientConfiguration#getInstance()}で取得できるようにするかどうか
	 */
	protected ClientConfiguration(boolean register) {
		init(this, register);
	}
	
	/**
	 * フィルタを追加する
	 * 
	 * @param rootFilter フィルター
	 */
	public void addFilter(MessageFilter rootFilter) {
		rootFilterService.addFilter(rootFilter);
	}
	
	/**
	 * 新しいタブを追加する。
	 * 
	 * <p><b>このメソッドはEventDispatcherThread内で動かしてください。</b></p>
	 * @param tab タブ
	 * @return 追加されたかどうか。
	 */
	public boolean addFrameTab(final ClientTab tab) {
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
	
	private boolean checkValidAccountId(String accountId) {
		for (String account : getAccountList()) {
			if (Utility.equalString(account, accountId)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 指定されたタブをフォーカスする。
	 * 
	 * <p><b>このメソッドはEventDispatcherThread内で動かしてください。</b></p>
	 * @param tab タブ
	 */
	public void focusFrameTab(final ClientTab tab) {
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
		if (accountIdForRead == null) {
			synchronized (lockObject) {
				if (accountIdForRead == null) {
					accountIdForRead = getDefaultAccountId();
				}
			}
		}
		return accountIdForRead;
	}
	
	/**
	 * 書き込み用アカウントのIDを取得する。
	 * 
	 * @return アカウントID (ユニーク)
	 */
	public String getAccountIdForWrite() {
		if (accountIdForWrite == null) {
			synchronized (lockObject) {
				if (accountIdForWrite == null) {
					accountIdForWrite = getAccountIdForRead();
				}
			}
		}
		return accountIdForWrite;
	}
	
	/**
	 * アカウントリストを取得する。リストがない場合長さ0の配列を返す。
	 * 
	 * @return アカウントリスト。
	 */
	public String[] getAccountList() {
		return getConfigData().initAccountList();
	}
	
	/**
	 * キャッシュマネージャを取得する。
	 *
	 * @return キャッシュマネージャ 
	 */
	public CacheManager getCacheManager() {
		if (cacheManager == null) {
			synchronized (lockObject) {
				if (cacheManager == null) {
					cacheManager = new CacheManager(this);
				}
			}
		}
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
	 * 設定のデータを格納するクラスを取得する
	 * 
	 * @return データクラス
	 */
	public ConfigData getConfigData() {
		if (configData == null) {
			synchronized (lockObject) {
				if (configData == null) {
					configData = new ConfigData();
				}
			}
		}
		return configData;
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
	
	/**
	 * デフォルトで使用するアカウントのIDを取得する。
	 * 
	 * @return アカウントのID (int)
	 */
	public String getDefaultAccountId() {
		String accountId = configProperties.getProperty("twitter.oauth.access_token.default");
		if (accountId == null) {
			String[] accountIds = getAccountList();
			if(accountIds != null){
				accountId = getAccountList()[0];
			}
		}
		return accountId;
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
	 * @return イメージキャッシャ
	 */
	public ImageCacher getImageCacher() {
		if (imageCacher == null) {
			synchronized (lockObject) {
				if (imageCacher == null) {
					imageCacher = new ImageCacher(this);
				}
			}
		}
		return imageCacher;
	}
	
	/**
	 * すべての入力をフィルターするクラスを取得する。
	 * 
	 * @return フィルター
	 */
	public FilterService getRootFilterService() {
		if (rootFilterService == null) {
			synchronized (lockObject) {
				if (rootFilterService == null) {
					rootFilterService = new FilterService(this);
				}
			}
		}
		return rootFilterService;
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
	* デフォルトのアカウントのTwitterの {@link Configuration} インスタンスを取得する。	 * 
	* @return Twitter Configuration
	*/
	public Configuration getTwitterConfiguration() {
		return getTwitterConfiguration(getDefaultAccountId());
	}
	
	/**
	 * 指定されたアカウントIDのTwitterの {@link Configuration} インスタンスを取得する。
	 * @param accountId アカウントID 
	 * @return Twitter Configuration
	 */
	public Configuration getTwitterConfiguration(String accountId) {
		String accessTokenString = configProperties.getProperty("twitter.oauth.access_token." + accountId);
		String accessTokenSecret =
				configProperties.getProperty(MessageFormat.format("twitter.oauth.access_token.{0}_secret", accountId));
		
		return getTwitterConfigurationBuilder() //
			.setOAuthAccessToken(accessTokenString) //
			.setOAuthAccessTokenSecret(accessTokenSecret) //
			.build();
	}
	
	/**
	 * Twitterの {@link ConfigurationBuilder} インスタンスを取得する。
	 * 
	 * @return Twitter ConfigurationBuilder
	 */
	public ConfigurationBuilder getTwitterConfigurationBuilder() {
		String consumerKey = configProperties.getProperty("twitter.oauth.consumer");
		String consumerSecret = configProperties.getProperty("twitter.oauth.consumer_secret");
		
		return new ConfigurationBuilder() //
			.setOAuthConsumerKey(consumerKey) //
			.setOAuthConsumerSecret(consumerSecret) //
			.setUserStreamRepliesAllEnabled(configProperties.getBoolean("twitter.stream.replies_all")) //
			.setJSONStoreEnabled(true);
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
	 * <p><b>このメソッドはEventDispatcherThread内で動かしてください。</b></p>
	 * @param tab タブ
	 * @return 選択されているかどうか
	 */
	public boolean isFocusTab(ClientTab tab) {
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
			if (getConfigData().mentionIdStrictMatch) {
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
	 * <p><b>このメソッドはEventDispatcherThread内で動かしてください。</b></p>
	 * @param tab タブ
	 */
	public void refreshTab(final ClientTab tab) {
		try {
			tabsListLock.readLock().lock();
			final int indexOf = tabsList.indexOf(tab);
			frameApi.refreshTab(indexOf, tab);
		} finally {
			tabsListLock.readLock().unlock();
		}
	}
	
	/**
	 * タブを削除する
	 * 
	 * <p><b>このメソッドはEventDispatcherThread内で動かしてください。</b></p>
	 * @param tab タブ
	 * @return 削除されたかどうか。
	 */
	public boolean removeFrameTab(final ClientTab tab) {
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
		String old = getAccountIdForWrite();
		if (old.equals(accountId) == false) {
			accountIdForRead = accountId;
			getRootFilterService().onChangeAccount(false);
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
		String old = getAccountIdForWrite();
		if (old.equals(accountId) == false) {
			accountIdForWrite = accountId;
			getRootFilterService().onChangeAccount(true);
		}
		return old;
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
	
	/**
	 * 初期化中/初期TLロード中であるかを設定する
	 * 
	 * @param isInitializing 初期化中かどうか。
	 */
	/*package*/void setInitializing(boolean isInitializing) {
		this.isInitializing = isInitializing;
	}
	
	/*package*/void setPortabledConfiguration(boolean portable) {
		portabledConfiguration = portable;
	}
	
	/**
	 * シャットダウンフェーズであるかどうかを設定する
	 * @param isShutdownPhase シャットダウンフェーズかどうか。
	 */
	public void setShutdownPhase(boolean isShutdownPhase) {
		this.isShutdownPhase = isShutdownPhase;
	}
	
	/**
	 * OAuthトークンの取得を試みる。実行中のスレッドをブロックします。
	 * 
	 * @return 取得を試して発生した例外。ない場合はnull
	 */
	public Exception tryGetOAuthToken() {
		Twitter twitter = new TwitterFactory(getTwitterConfigurationBuilder().build()).getInstance();
		AccessToken accessToken = new OAuthFrame(this).show(twitter);
		
		//将来の参照用に accessToken を永続化する
		String userId;
		try {
			userId = String.valueOf(twitter.verifyCredentials().getId());
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
			configProperties.setProperty("twitter.oauth.access_token." + userId, accessToken.getToken());
			configProperties.setProperty(MessageFormat.format("twitter.oauth.access_token.{0}_secret", userId),
					accessToken.getTokenSecret());
			configProperties.store();
		}
		return null;
	}
}

class TestCacher extends ImageCacher {
	
	public TestCacher(ClientConfiguration configuration) {
		super(configuration);
	}
	
}

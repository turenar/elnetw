package jp.syuriken.snsw.twclient;

import java.awt.TrayIcon;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import jp.syuriken.snsw.twclient.config.ConfigFrameBuilder;
import jp.syuriken.snsw.twclient.filter.MessageFilter;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
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

	private TrayIcon trayIcon;

	/*package*/ClientProperties configProperties;

	/*package*/ClientProperties configDefaultProperties;

	private boolean isShutdownPhase = false;

	private TwitterClientFrame frameApi;

	private final List<ClientTab> tabsList = new ArrayList<ClientTab>();

	private final Utility utility = new Utility(this);

	private boolean isInitializing = true;

	private ConfigFrameBuilder configBuilder = new ConfigFrameBuilder(this);

	private final ReentrantReadWriteLock tabsListLock = new ReentrantReadWriteLock();

	private volatile FilterService rootFilterService;

	private volatile ImageCacher imageCacher;

	/*package*/ConcurrentHashMap<String, Twitter> cachedTwitterInstances = new ConcurrentHashMap<String, Twitter>();

	private String accountIdForRead;

	private String accountIdForWrite;

	private TwitterDataFetchScheduler fetchScheduler;

	private boolean portabledConfiguration;

	private static final String HOME_BASE_DIR = System.getProperty("user.home") + "/.elnetw";

	/** 環境依存の改行コード */
	public static final String NEW_LINE = System.getProperty("line.separator");

	private volatile CacheManager cacheManager;

	private Object lockObject = new Object();

	private List<String> args;


	/**
	 * インスタンスを生成する。
	 *
	 */
	protected ClientConfiguration() {
	}

	/**
	 * <strong>テスト用</strong>インスタンスを生成する。HeadlessExceptionを無視
	 *
	 * @param isTestMethod テストメソッドですよ。悪用（？）禁止
	 */
	protected ClientConfiguration(boolean isTestMethod) {
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
		return configProperties.getArray(PROPERTY_ACCOUNT_LIST);
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
			accountId = getAccountList()[0];
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

	/*package*/void setOpts(String[] args) {
		this.args = Arrays.asList(args);
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
	 * トレイアイコン
	 *
	 * @param trayIcon the trayIcon to set
	 */
	public void setTrayIcon(TrayIcon trayIcon) {
		this.trayIcon = trayIcon;
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

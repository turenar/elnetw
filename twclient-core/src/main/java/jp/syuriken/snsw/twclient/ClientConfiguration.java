package jp.syuriken.snsw.twclient;

import java.awt.TrayIcon;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

/**
 * twclient の情報などを格納するクラス。
 * 
 * @author $Author$
 */
public class ClientConfiguration {
	
	private TrayIcon trayIcon;
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	private ClientProperties configProperties;
	
	private ClientProperties configDefaultProperties;
	
	private boolean isShutdownPhase = false;
	
	private TwitterClientFrame frameApi;
	
	private final List<ClientTab> tabsList = new ArrayList<ClientTab>();
	
	private final Utility utility = new Utility(this);
	
	private boolean isInitializing = true;

	private final ReentrantReadWriteLock tabsListLock = new ReentrantReadWriteLock();
	
	private final FilterService rootFilterService;
	
	private ImageCacher imageCacher;
	
	
	/*package*/ClientConfiguration() {
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
	 * 新しいタブを追加する。
	 * 
	 * @param tab タブ
	 * @return 追加されたかどうか。
	 */
	public boolean addFrameTab(ClientTab tab) {
		if (tab == null) {
			return false;
		}
		tabsListLock.writeLock().lock();
		boolean result = tabsList.add(tab);
		frameApi.addTab(tab);
		tabsListLock.writeLock().unlock();
		return result;
	}
	
	/**
	 * アカウントリストを取得する。リストがない場合長さ0の配列を返す。
	 * 
	 * @return アカウントリスト。
	 */
	public String[] getAccountList() {
		String list = configProperties.getProperty("twitter.oauth.access_token.list");
		if (list == null) {
			return new String[] {};
		} else {
			return list.split(" ");
		}
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
		tabsListLock.readLock().lock();
		ClientTab result = tabsList.get(index);
		tabsListLock.readLock().unlock();
		return result;
	}
	
	/**
	 * 追加されているFrameTabの個数を数える
	 * 
	 * @return 個数
	 */
	public int getFrameTabCount() {
		tabsListLock.readLock().lock();
		int size = tabsList.size();
		tabsListLock.readLock().unlock();
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
			imageCacher = new ImageCacher(this);
		}
		return imageCacher;
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
	 * TrayIconをかえす。nullの場合有り。
	 * 
	 * @return トレイアイコン
	 */
	public TrayIcon getTrayIcon() {
		return trayIcon;
	}
	
	/**
	* デフォルトのアカウントのTwitterの {@link Configuration} インスタンスを取得する。	 * 
	* @return Twitter Configuration
	*/
	public Configuration getTwitterConfiguration() {
		return getTwitterConfiguration(getDefaultAccountId());
	}
	
	/**	 * 指定されたアカウントIDのTwitterの {@link Configuration} インスタンスを取得する。
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
			.setUserStreamRepliesAllEnabled(configProperties.getBoolean("twitter.stream.replies_all"));
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
	 * 初期化中/初期TLロード中であるかどうかを返す。
	 * 
	 * @return the isInitializing
	 */
	public boolean isInitializing() {
		return isInitializing;
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
	 * タブを削除する
	 * 
	 * @param tab タブ
	 * @return 削除されたかどうか。
	 */
	public boolean removeFrameTab(ClientTab tab) {
		tabsListLock.writeLock().lock();
		int indexOf = tabsList.indexOf(tab);
		if (indexOf != -1) {
			tabsList.remove(indexOf);
			frameApi.removeFrameTab(indexOf, tab);
		}
		tabsListLock.writeLock().unlock();
		return indexOf != -1;
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

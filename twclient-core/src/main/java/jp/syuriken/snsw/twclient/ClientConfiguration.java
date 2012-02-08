package jp.syuriken.snsw.twclient;

import java.awt.TrayIcon;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import twitter4j.conf.Configuration;

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
	
	private Configuration twitterConfiguration;
	
	private boolean isShutdownPhase;
	
	private ClientFrameApi frameApi;
	
	private final Utility utility = new Utility(this);
	
	private boolean isInitializing = true;
	
	
	/*package*/ClientConfiguration() {
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
	 * FrameApiを取得する
	 * 
	 * @return フレームAPI
	 */
	public ClientFrameApi getFrameApi() {
		return frameApi;
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
	 * Twitterの設定を取得する。
	 * 
	 * @return {@link Configuration}インスタンス
	 */
	public Configuration getTwitterConfiguration() {
		return twitterConfiguration;
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
	/*package*/void setFrameApi(ClientFrameApi frameApi) {
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
	 * Twitter4JのConfigurationを設定する。
	 * 
	 * @param twitterConfiguration {@link Configuration}インスタンス
	 */
	public void setTwitterConfiguration(Configuration twitterConfiguration) {
		this.twitterConfiguration = twitterConfiguration;
	}
}

package jp.syuriken.snsw.twclient;

import twitter4j.conf.Configuration;

/**
 * twclient の情報などを格納するクラス。
 * 
 * @author $Author$
 */
public class ClientConfiguration {
	
	private ClientProperties configProperties;
	
	private ClientProperties configDefaultProperties;
	
	private Configuration twitterConfiguration;
	
	private boolean isShutdownPhase;
	
	private ClientFrameApi frameApi;
	
	
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
	 * Twitterの設定を取得する。
	 * 
	 * @return {@link Configuration}インスタンス
	 */
	public Configuration getTwitterConfiguration() {
		return twitterConfiguration;
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
	 * シャットダウンフェーズであるかどうかを設定する
	 * @param isShutdownPahse シャットダウンフェーズかどうか。
	 */
	public void setShutdownPhase(boolean isShutdownPahse) {
		this.isShutdownPhase = isShutdownPahse;
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

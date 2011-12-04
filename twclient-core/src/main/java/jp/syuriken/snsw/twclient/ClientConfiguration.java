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
	 * Twitterの設定を取得する。
	 * 
	 * @return {@link Configuration}インスタンス
	 */
	public Configuration getTwitterConfiguration() {
		return twitterConfiguration;
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
	 * Twitter4JのConfigurationを設定する。
	 * 
	 * @param twitterConfiguration {@link Configuration}インスタンス
	 */
	public void setTwitterConfiguration(Configuration twitterConfiguration) {
		this.twitterConfiguration = twitterConfiguration;
	}
}

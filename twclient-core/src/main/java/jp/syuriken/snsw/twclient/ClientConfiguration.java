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
	 * TODO snsoftware
	 * 
	 * @return the twitterConfiguration
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
	 * TODO snsoftware
	 * 
	 * @param twitterConfiguration
	 */
	public void setTwitterConfiguration(Configuration twitterConfiguration) {
		this.twitterConfiguration = twitterConfiguration;
	}
}

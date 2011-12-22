package jp.syuriken.snsw.twclient;

import java.text.MessageFormat;

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
	
	private ClientProperties configProperties;
	
	private ClientProperties configDefaultProperties;
	
	
	/**
	 * アカウントリストを取得する。
	 * 
	 * @return アカウントリスト。
	 */
	public String[] getAccountList() {
		return configProperties.getProperty("oauth.access_token.list").split(" ");
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
		String accountId = configProperties.getProperty("oauth.access_token.default");
		if (accountId == null) {
			accountId = getAccountList()[0];
		}
		return accountId;
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
	 * @param accountId アカウントID 
	 * @return Twitter Configuration
	 */
	public Configuration getTwitterConfiguration(String accountId) {
		String accessTokenString = configProperties.getProperty("oauth.access_token." + accountId);
		String accessTokenSecret =
				configProperties.getProperty(MessageFormat.format("oauth.access_token.{0}_secret", accountId));
		
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
		String consumerKey = configProperties.getProperty("oauth.consumer");
		String consumerSecret = configProperties.getProperty("oauth.consumer_secret");
		
		return new ConfigurationBuilder() //
			.setOAuthConsumerKey(consumerKey) //
			.setOAuthConsumerSecret(consumerSecret) //
			.setUserStreamRepliesAllEnabled(configProperties.getBoolean("twitter.stream.replies_all"));
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
	 * OAuthトークンの取得を試みる。実行中のスレッドをブロックします。
	 * 
	 * @return 取得を試して発生した例外。ない場合はnull
	 */
	public Exception tryGetOAuthToken() {
		Twitter twitter = new TwitterFactory(getTwitterConfigurationBuilder().build()).getInstance();
		AccessToken accessToken = new OAuthFrame().show(twitter);
		
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
				configProperties.setProperty("oauth.access_token.list", MessageFormat.format("{0} {1}",
						configProperties.getProperty("oauth.access_token.list"), userId));
			}
			configProperties.setProperty("oauth.access_token." + userId, accessToken.getToken());
			configProperties.setProperty(MessageFormat.format("oauth.access_token.{0}_secret", userId),
					accessToken.getTokenSecret());
			configProperties.store();
		}
		return null;
		
	}
	
}

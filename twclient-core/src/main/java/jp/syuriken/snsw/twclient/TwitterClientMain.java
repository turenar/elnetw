package jp.syuriken.snsw.twclient;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Properties;
import java.util.logging.Level;

import jp.syuriken.snsw.utils.Logger;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

/**
 * TODO snsoftware
 * 
 * @author $Author$
 */
public class TwitterClientMain {
	
	protected Properties configProperties;
	
	private Logger logger;
	
	
	/**
	 * インスタンスを生成する。
	 * 
	 * @param args
	 */
	public TwitterClientMain(String[] args) {
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * TODO snsoftware
	 * @return 
	 * 
	 */
	private Configuration initTwitterConfiguration() {
		String consumerKey = configProperties.getProperty("oauth.consumer");
		String consumerSecret = configProperties.getProperty("oauth.consumer_secret");
		String accessTokenString;
		String accessTokenSecret;
		
		if (configProperties.containsKey("oauth.access_token")) {
			accessTokenString = configProperties.getProperty("oauth.access_token");
			accessTokenSecret = configProperties.getProperty("oauth.access_token_secret");
		} else {
			Twitter twitter = new TwitterFactory().getInstance();
			twitter.setOAuthConsumer(consumerKey, consumerSecret);
			AccessToken accessToken = tryGetOAuthAccessToken(twitter);
			accessTokenString = accessToken.getToken();
			accessTokenSecret = accessToken.getTokenSecret();
		}
		return new ConfigurationBuilder().setOAuthConsumerKey(consumerKey).setOAuthConsumerSecret(consumerSecret)
			.setOAuthAccessToken(accessTokenString).setOAuthAccessTokenSecret(accessTokenSecret)
			.setUserStreamRepliesAllEnabled(false).build();
	}
	
	/**
	 * TODO snsoftware
	 * 
	 */
	public int run() {
		try {
			logger = Logger.getLogger("twclient.log", Level.ALL);
		} catch (IOException e) {
			System.err.println("ログ出力用に twclient.log が開けませんでした。");
			return 1;
		}
		Properties defaultConfig = new Properties();
		try {
			defaultConfig.load(getClass().getClassLoader().getResourceAsStream(
					"jp/syuriken/snsw/twclient/config.properties"));
		} catch (IOException e) {
			System.err.println("デフォルト設定が読み込めません。");
		}
		configProperties = new Properties(defaultConfig);
		if (new File("twclient.cfg").exists()) {
			try {
				configProperties.load(new FileReader("twclient.cfg"));
			} catch (FileNotFoundException e) {
				System.err.println("twclient.cfg: " + e.getLocalizedMessage());
				logger.log(Level.WARNING, e);
			} catch (IOException e) {
				System.err.println("twclient.cfg: " + e.getLocalizedMessage());
			}
		}
		final Configuration configuration = initTwitterConfiguration();
		java.awt.EventQueue.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
					
					@Override
					public void uncaughtException(Thread t, Throwable e) {
						e.printStackTrace();
					}
				});
				new TwitterClientFrame(configuration).start();
			}
		});
		try {
			logger.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return 0;
	}
	
	/**
	 * TODO snsoftware
	 * @param requestToken 
	 * @return 
	 * 
	 */
	private AccessToken tryGetOAuthAccessToken(Twitter twitter) {
		AccessToken accessToken = null;
		
		accessToken = new OAuthFrame().show(twitter, logger);
		//将来の参照用に accessToken を永続化する
		configProperties.setProperty("oauth.access_token", accessToken.getToken());
		configProperties.setProperty("oauth.access_token_secret", accessToken.getTokenSecret());
		try {
			configProperties.store(new BufferedWriter(new FileWriter("twclient.cfg")), "Auto generated.");
		} catch (IOException e) {
			// TODO 
			e.printStackTrace();
		}
		return accessToken;
	}
}

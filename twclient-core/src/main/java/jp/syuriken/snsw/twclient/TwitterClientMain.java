package jp.syuriken.snsw.twclient;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.text.MessageFormat;
import java.util.logging.Level;

import javax.swing.JOptionPane;

import jp.syuriken.snsw.utils.Logger;
import twitter4j.Twitter;
import twitter4j.TwitterException;
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
	
	/** TODO snsoftware */
	private static final File CONFIG_FILE = new File("twclient.cfg");
	
	protected ClientProperties configProperties;
	
	protected final ClientConfiguration configuration;
	
	private Logger logger;
	
	
	/**
	 * インスタンスを生成する。
	 * 
	 * @param args
	 */
	public TwitterClientMain(String[] args) {
		configuration = new ClientConfiguration();
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
		
		if (configProperties.containsKey("oauth.access_token.list")) {
			String account = configProperties.getProperty("oauth.access_token.list").split(" ")[0];
			accessTokenString = configProperties.getProperty("oauth.access_token." + account);
			accessTokenSecret =
					configProperties.getProperty(MessageFormat.format("oauth.access_token.{0}_secret", account));
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
		ClientProperties defaultConfig = new ClientProperties();
		try {
			defaultConfig.load(getClass().getClassLoader().getResourceAsStream(
					"jp/syuriken/snsw/twclient/config.properties"));
		} catch (IOException e) {
			System.err.println("デフォルト設定が読み込めません。");
		}
		configuration.setConfigDefaultProperties(defaultConfig);
		configProperties = new ClientProperties(defaultConfig);
		if (CONFIG_FILE.exists()) {
			try {
				configProperties.setStoreFile(CONFIG_FILE);
				configProperties.load(new FileReader(CONFIG_FILE));
			} catch (FileNotFoundException e) {
				System.err.println("twclient.cfg: " + e.getLocalizedMessage());
				logger.log(Level.WARNING, e);
			} catch (IOException e) {
				System.err.println("twclient.cfg: " + e.getLocalizedMessage());
			}
		}
		configuration.setConfigProperties(configProperties);
		configuration.setTwitterConfiguration(initTwitterConfiguration());
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
		String userId;
		try {
			userId = String.valueOf(twitter.verifyCredentials().getId());
		} catch (TwitterException e1) {
			JOptionPane.showMessageDialog(null, "ユーザー情報の取得に失敗しました。時間をおいて試してみて下さい: " + e1.getLocalizedMessage(), "エラー",
					JOptionPane.ERROR_MESSAGE);
			throw new RuntimeException(e1);
		}
		configProperties.setProperty("oauth.access_token.list", userId);
		configProperties.setProperty("oauth.access_token." + userId, accessToken.getToken());
		configProperties.setProperty(MessageFormat.format("oauth.access_token.{0}_secret", userId),
				accessToken.getTokenSecret());
		try {
			configProperties.store(new BufferedWriter(new FileWriter("twclient.cfg")), "Auto generated.");
		} catch (IOException e) {
			// TODO 
			e.printStackTrace();
		}
		return accessToken;
	}
}

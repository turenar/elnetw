package jp.syuriken.snsw.twclient;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.text.MessageFormat;

import javax.swing.JOptionPane;
import javax.swing.UIManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

/**
 * 実際に起動するランチャ
 * 
 * @author $Author$
 */
public class TwitterClientMain {
	
	/** 設定ファイル名 */
	private static final String CONFIG_FILE_NAME = "twclient.cfg";
	
	/** 設定ファイル */
	private static final File CONFIG_FILE = new File(CONFIG_FILE_NAME);
	
	/** 設定データ */
	protected ClientProperties configProperties;
	
	/** 設定 */
	protected final ClientConfiguration configuration;
	
	/** スレッドホルダ */
	protected Object threadHolder = new Object();
	
	private final Logger logger;
	
	
	/**
	 * インスタンスを生成する。
	 * 
	 * @param args コマンドラインオプション
	 */
	public TwitterClientMain(String[] args) {
		logger = LoggerFactory.getLogger(getClass());
		configuration = new ClientConfiguration();
		
		try {
			javax.swing.UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			logger.warn("LookAndFeelの設定に失敗", e);
		}
	}
	
	/**
	 * TwitterConfigurationを作成する。
	 * @return 認証済み Configuration インスタンス
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
			.setUserStreamRepliesAllEnabled(configProperties.getBoolean("twitter.stream.replies_all")).build();
	}
	
	/**
	 * 起動する。
	 * @return 終了値
	 * 
	 */
	public int run() {
		ClientProperties defaultConfig = new ClientProperties();
		try {
			defaultConfig.load(getClass().getClassLoader().getResourceAsStream(
					"jp/syuriken/snsw/twclient/config.properties"));
		} catch (IOException e) {
			logger.error("デフォルト設定が読み込めません", e);
		}
		configuration.setConfigDefaultProperties(defaultConfig);
		configProperties = new ClientProperties(defaultConfig);
		configProperties.setStoreFile(CONFIG_FILE);
		if (CONFIG_FILE.exists()) {
			logger.debug(CONFIG_FILE_NAME + " is found.");
			try {
				configProperties.load(new FileReader(CONFIG_FILE));
			} catch (IOException e) {
				logger.warn("設定ファイルの読み込み中にエラー", e);
			}
		}
		configuration.setConfigProperties(configProperties);
		configuration.setTwitterConfiguration(initTwitterConfiguration());
		final TwitterClientFrame frame = new TwitterClientFrame(configuration, threadHolder);
		Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
			
			@Override
			public void uncaughtException(Thread t, Throwable e) {
				logger.error("Uncaught Exception", e);
			}
		});
		java.awt.EventQueue.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				frame.start();
			}
		});
		
		synchronized (threadHolder) {
			while (configuration.isShutdownPhase() == false) {
				try {
					threadHolder.wait();
					break;
				} catch (InterruptedException e) {
					// do nothing
				}
			}
		}
		logger.info("Exiting twclient...");
		frame.cleanUp();
		
		return 0;
	}
	
	/**
	 * OAuthアクセストークンの取得を試す
	 * @param twitter Twitterインスタンス
	 * @return アクセストークン
	 */
	private AccessToken tryGetOAuthAccessToken(Twitter twitter) {
		AccessToken accessToken = null;
		
		accessToken = new OAuthFrame().show(twitter);
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
			configProperties.store(new BufferedWriter(new FileWriter(CONFIG_FILE_NAME)), "Auto generated.");
		} catch (IOException e) {
			// TODO 
			e.printStackTrace();
		}
		return accessToken;
	}
}

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
 * 実際に起動するランチャ
 * 
 * @author $Author$
 */
public class TwitterClientMain {
	
	/** 設定ファイル */
	private static final File CONFIG_FILE = new File("twclient.cfg");
	
	/** 設定データ */
	protected ClientProperties configProperties;
	
	/** 設定 */
	protected final ClientConfiguration configuration;
	
	/** スレッドホルダ */
	protected Object threadHolder = new Object();
	
	private Logger logger;
	
	
	/**
	 * インスタンスを生成する。
	 * 
	 * @param args コマンドラインオプション
	 */
	public TwitterClientMain(String[] args) {
		configuration = new ClientConfiguration();
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
		final TwitterClientFrame frame = new TwitterClientFrame(configuration, threadHolder);
		java.awt.EventQueue.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
					
					@Override
					public void uncaughtException(Thread t, Throwable e) {
						e.printStackTrace();
					}
				});
				frame.start();
			}
		});
		
		synchronized (threadHolder) {
			while (configuration.isShutdownPahse() == false) {
				try {
					threadHolder.wait(10000);
					break;
				} catch (InterruptedException e) {
					// do nothing
				}
			}
		}
		frame.cleanUp();
		
		try {
			logger.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
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
			configProperties.store(new BufferedWriter(new FileWriter("twclient.cfg")), "Auto generated.");
		} catch (IOException e) {
			// TODO 
			e.printStackTrace();
		}
		return accessToken;
	}
}

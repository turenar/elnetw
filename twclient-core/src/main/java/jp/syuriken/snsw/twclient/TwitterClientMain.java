package jp.syuriken.snsw.twclient;

import java.io.File;
import java.io.FileReader;
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

/**
 * 実際に起動するランチャ
 * 
 * @author $Author$
 */
public class TwitterClientMain {
	
	/** TODO snsoftware */
	private static final String CONFIG_FILE_NAME = "twclient.cfg";
	
	/** 設定ファイル */
	private static final File CONFIG_FILE = new File(CONFIG_FILE_NAME);
	
	/** 設定データ */
	protected ClientProperties configProperties;
	
	/** 設定 */
	protected final ClientConfiguration configuration;
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	
	/**
	 * インスタンスを生成する。
	 * 
	 * @param args コマンドラインオプション
	 */
	public TwitterClientMain(String[] args) {
		configuration = new ClientConfiguration();
		try {
			javax.swing.UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			logger.warn("LookAndFeelの設定に失敗", e);
		}
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
		tryGetOAuthAccessToken();
		final TwitterClientFrame frame = new TwitterClientFrame(configuration, new Object());
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
		
		return 0;
	}
	
	/**
	 * OAuthアクセストークンの取得を試す
	 * @return アクセストークン
	 */
	private boolean tryGetOAuthAccessToken() {
		if (configuration.getAccountList().length != 0) {
			return false;
		}
		Twitter twitter = new TwitterFactory(configuration.getTwitterConfigurationBuilder().build()).getInstance();
		AccessToken accessToken = new OAuthFrame().show(twitter);
		
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
		configProperties.setProperty("oauth.access_token.default", userId);
		configProperties.setProperty("oauth.access_token." + userId, accessToken.getToken());
		configProperties.setProperty(MessageFormat.format("oauth.access_token.{0}_secret", userId),
				accessToken.getTokenSecret());
		configProperties.store();
		return true;
	}
}

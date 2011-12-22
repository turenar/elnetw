package jp.syuriken.snsw.twclient;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
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
		
		tryGetOAuthAccessToken();
		
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
	 * OAuthアクセストークンの取得を試す
	 * @param twitter Twitterインスタンス
	 * @return アクセストークン
	 */
	private boolean tryGetOAuthAccessToken() {
		if (configuration.getAccountList() != null) {
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

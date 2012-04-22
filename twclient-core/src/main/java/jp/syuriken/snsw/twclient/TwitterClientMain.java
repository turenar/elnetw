package jp.syuriken.snsw.twclient;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.TimerTask;

import javax.swing.JOptionPane;
import javax.swing.UIManager;

import jp.syuriken.snsw.twclient.ClientConfiguration.ConfigData;
import jp.syuriken.snsw.twclient.filter.RootFilter;
import jp.syuriken.snsw.twclient.filter.UserFilter;
import jp.syuriken.snsw.twclient.internal.TwitterRunnable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
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
	
	/** 設定ファイル名 */
	private static final String CONFIG_FILE_NAME = "twclient.cfg";
	
	private static final String HOME_BASE_DIR = System.getProperty("user.home") + "/.turetwcl";
	
	/** 設定データ */
	protected ClientProperties configProperties;
	
	/** 設定 */
	protected final ClientConfiguration configuration;
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	/** スレッドホルダ */
	protected Object threadHolder = new Object();
	
	
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
		boolean portable = Boolean.getBoolean("config.portable");
		File homeBaseDirFile = new File(HOME_BASE_DIR);
		if (portable == false && homeBaseDirFile.exists() == false) {
			if (homeBaseDirFile.mkdirs()) {
				homeBaseDirFile.setReadable(false, false);
				homeBaseDirFile.setWritable(false, false);
				homeBaseDirFile.setExecutable(false, false);
				homeBaseDirFile.setReadable(true, true);
				homeBaseDirFile.setWritable(true, true);
				homeBaseDirFile.setExecutable(true, true);
			} else {
				logger.warn("ディレクトリの作成ができませんでした: {}", homeBaseDirFile.getPath());
			}
		}
		
		ClientProperties defaultConfig = new ClientProperties();
		try {
			defaultConfig.load(getClass().getClassLoader().getResourceAsStream(
					"jp/syuriken/snsw/twclient/config.properties"));
		} catch (IOException e) {
			logger.error("デフォルト設定が読み込めません", e);
		}
		configuration.setConfigDefaultProperties(defaultConfig);
		configProperties = new ClientProperties(defaultConfig);
		File configFile = new File(portable ? "." : HOME_BASE_DIR, CONFIG_FILE_NAME);
		configProperties.setStoreFile(configFile);
		if (configFile.exists()) {
			logger.debug(CONFIG_FILE_NAME + " is found.");
			try {
				configProperties.load(new FileReader(configFile));
			} catch (IOException e) {
				logger.warn("設定ファイルの読み込み中にエラー", e);
			}
		}
		configuration.setConfigProperties(configProperties);
		tryGetOAuthAccessToken();
		
		final TwitterClientFrame frame = new TwitterClientFrame(configuration, threadHolder);
		configuration.addFilter(new RootFilter(configuration));
		configuration.addFilter(new UserFilter(configuration));
		configuration.addFrameTab(new TimelineViewTab(configuration));
		configuration.addFrameTab(new MentionViewTab(configuration));
		scheduleTwitterDataLoader();
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
	
	private void scheduleTwitterDataLoader() {
		final ClientFrameApi frameApi = configuration.getFrameApi();
		final ConfigData configData = configuration.getConfigData();
		final Twitter twitterForRead = frameApi.getTwitterForRead();
		final FilterService rootFilterService = configuration.getRootFilterService();
		
		frameApi.addJob(new TwitterRunnable() {
			
			@Override
			protected void access() throws TwitterException {
				ResponseList<Status> homeTimeline;
				Paging paging = configData.pagingOfGettingInitialTimeline;
				homeTimeline = twitterForRead.getHomeTimeline(paging);
				for (Status status : homeTimeline) {
					TwitterStatus twitterStatus = new TwitterStatus(status);
					twitterStatus.setLoadedInitialization(true);
					rootFilterService.onStatus(twitterStatus);
				}
			}
			
			@Override
			protected ClientConfiguration getConfiguration() {
				return configuration;
			}
		});
		frameApi.addJob(new TwitterRunnable() {
			
			@Override
			public void access() throws TwitterException {
				try {
					Paging paging = configData.pagingOfGettingInitialMentions;
					ResponseList<Status> mentions = twitterForRead.getMentions(paging);
					for (Status status : mentions) {
						TwitterStatus twitterStatus = new TwitterStatus(status);
						twitterStatus.setLoadedInitialization(true);
						rootFilterService.onStatus(twitterStatus);
					}
				} finally {
					configuration.setInitializing(false);
				}
			}
			
			@Override
			protected ClientConfiguration getConfiguration() {
				return configuration;
			}
		});
		frameApi.getTimer().schedule(new TimerTask() {
			
			@Override
			public void run() {
				frameApi.addJob(new TwitterRunnable() {
					
					@Override
					protected void access() throws TwitterException {
						Paging paging = configData.pagingOfGettingTimeline;
						ResponseList<Status> timeline;
						timeline = twitterForRead.getHomeTimeline(paging);
						for (Status status : timeline) {
							rootFilterService.onStatus(status);
						}
					}
					
					@Override
					protected ClientConfiguration getConfiguration() {
						return configuration;
					}
				});
			}
		}, configData.intervalOfGetTimeline, configData.intervalOfGetTimeline);
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
		AccessToken accessToken = new OAuthFrame(configuration).show(twitter);
		
		//将来の参照用に accessToken を永続化する
		String userId;
		try {
			userId = String.valueOf(twitter.verifyCredentials().getId());
		} catch (TwitterException e1) {
			JOptionPane.showMessageDialog(null, "ユーザー情報の取得に失敗しました。時間をおいて試してみて下さい: " + e1.getLocalizedMessage(), "エラー",
					JOptionPane.ERROR_MESSAGE);
			throw new RuntimeException(e1);
		}
		configProperties.setProperty("twitter.oauth.access_token.list", userId);
		configProperties.setProperty("oauth.access_token.default", userId);
		configProperties.setProperty("twitter.oauth.access_token." + userId, accessToken.getToken());
		configProperties.setProperty(MessageFormat.format("twitter.oauth.access_token.{0}_secret", userId),
				accessToken.getTokenSecret());
		configProperties.store();
		return true;
	}
}

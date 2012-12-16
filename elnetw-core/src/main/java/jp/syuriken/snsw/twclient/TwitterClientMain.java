package jp.syuriken.snsw.twclient;

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;

import java.awt.GraphicsEnvironment;
import java.awt.TrayIcon;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.MessageFormat;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

import jp.syuriken.snsw.twclient.filter.IllegalSyntaxException;
import jp.syuriken.snsw.twclient.filter.RootFilter;
import jp.syuriken.snsw.twclient.filter.UserFilter;
import jp.syuriken.snsw.twclient.handler.UserInfoViewActionHandler.UserInfoFrameTab;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;

/**
 * 実際に起動するランチャ
 *
 * @author Turenar <snswinhaiku dot lo at gmail dot com>
 */
public class TwitterClientMain {

	/** 設定ファイル名 */
	private static final String CONFIG_FILE_NAME = "elnetw.cfg";

	/** 設定データ */
	protected ClientProperties configProperties;

	/** 設定 */
	protected final ClientConfiguration configuration;

	private Logger logger = LoggerFactory.getLogger(getClass());

	/** スレッドホルダ */
	protected Object threadHolder = new Object();

	private Getopt getopt;


	/**
	 * インスタンスを生成する。
	 *
	 * @param args コマンドラインオプション
	 */
	public TwitterClientMain(String[] args) {
		configuration = new ClientConfiguration();
		configuration.setOpts(args);
		LongOpt[] longOpts = new LongOpt[] {
			new LongOpt("debug", LongOpt.NO_ARGUMENT, null, 'd'),
		};
		getopt = new Getopt("elnetw", args, "d", longOpts);

		try {
			javax.swing.UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			logger.warn("LookAndFeelの設定に失敗", e);
		}
	}

	/**
	 * 環境チェック
	 *
	 */
	private void checkEnvironment() {
		if (Charset.isSupported("UTF-8") == false) {
			throw new AssertionError("UTF-8 エンコードがサポートされていないようです。UTF-8 エンコードがサポートされていない環境では"
					+ "このソフトを動かすことはできません。Java VMの開発元に問い合わせてみてください。");
		}
		if (GraphicsEnvironment.isHeadless()) {
			throw new AssertionError("お使いのJava VMないし環境ではGUI出力がサポートされていないようです。GUIモードにするか、Java VMにGUIサポートを組み込んでください");
		}
	}

	/**
	 * 起動する。
	 * @return 終了値
	 *
	 */
	public int run() {
		checkEnvironment();

		Getopt getopt = this.getopt;
		int c;
		boolean portable = Boolean.getBoolean("config.portable");
		boolean debugMode = false;
		while ((c = getopt.getopt()) != -1) {
			switch (c) {
				case 'd':
					portable = true;
					debugMode = true;
					break;
				default:
					break;
			}
		}

		if (debugMode) {
			URL resource = getClass().getResource("/logback-debug.xml");
			if (resource == null) {
				logger.error("resource /logback-debug.xml is not found");
			} else {
				LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
				try {
					JoranConfigurator configurator = new JoranConfigurator();
					configurator.setContext(context);
					context.reset();
					configurator.doConfigure(resource);
				} catch (JoranException je) {
					// StatusPrinter will handle this
				}
			}
		}

		configuration.setPortabledConfiguration(portable);
		File configRootDir = new File(configuration.getConfigRootDir());
		if (portable == false && configRootDir.exists() == false) {
			if (configRootDir.mkdirs()) {
				configRootDir.setReadable(false, false);
				configRootDir.setWritable(false, false);
				configRootDir.setExecutable(false, false);
				configRootDir.setReadable(true, true);
				configRootDir.setWritable(true, true);
				configRootDir.setExecutable(true, true);
			} else {
				logger.warn("ディレクトリの作成ができませんでした: {}", configRootDir.getPath());
			}
		}

		try {
			configuration.setTrayIcon(new TrayIcon(ImageIO.read(getClass().getClassLoader().getResourceAsStream(
					"jp/syuriken/snsw/twclient/img/icon16.png")), TwitterClientFrame.APPLICATION_NAME));
		} catch (IOException e) {
			logger.error("icon ファイルの読み込みに失敗。");
		}

		ClientProperties defaultConfig = new ClientProperties();
		try {
			InputStream stream = getClass().getResourceAsStream("config.properties");
			if (stream == null) {
				logger.error("リソース(default) config.properties を読み込めません");
			} else {
				InputStreamReader reader = new InputStreamReader(stream, "UTF-8");
				defaultConfig.load(reader);
			}
		} catch (IOException e) {
			logger.error("デフォルト設定が読み込めません", e);
		}
		configuration.setConfigDefaultProperties(defaultConfig);
		configProperties = new ClientProperties(defaultConfig);
		File configFile = new File(configuration.getConfigRootDir(), CONFIG_FILE_NAME);
		configProperties.setStoreFile(configFile);
		if (configFile.exists()) {
			logger.debug(CONFIG_FILE_NAME + " is found.");
			try {
				InputStreamReader reader = new InputStreamReader(new FileInputStream(configFile), "UTF-8");
				configProperties.load(reader);
			} catch (IOException e) {
				logger.warn("設定ファイルの読み込み中にエラー", e);
			}
		}
		configuration.setConfigProperties(configProperties);
		tryGetOAuthAccessToken();

		final TwitterClientFrame frame = new TwitterClientFrame(configuration, threadHolder);
		configuration.addFilter(new UserFilter(configuration));
		configuration.addFilter(new RootFilter(configuration));

		ClientConfiguration.putClientTabConstructor("timeline", TimelineViewTab.class);
		ClientConfiguration.putClientTabConstructor("mention", MentionViewTab.class);
		ClientConfiguration.putClientTabConstructor("directmessage", DirectMessageViewTab.class);
		ClientConfiguration.putClientTabConstructor("userinfo", UserInfoFrameTab.class);

		String tabsList = configProperties.getProperty("gui.tabs.list");
		if (tabsList == null) {
			try {
				configuration.addFrameTab(new TimelineViewTab(configuration));
				configuration.addFrameTab(new MentionViewTab(configuration));
				configuration.addFrameTab(new DirectMessageViewTab(configuration));
			} catch (IllegalSyntaxException e) {
				throw new AssertionError(e); // This can't happen: because no query
			}
		} else {
			String[] tabs = tabsList.split(" ");
			for (String tabIndetifier : tabs) {
				int separatorPosition = tabIndetifier.indexOf(':');
				String tabId = tabIndetifier.substring(0, separatorPosition);
				String uniqId = tabIndetifier.substring(separatorPosition + 1);
				Constructor<? extends ClientTab> tabConstructor = ClientConfiguration.getClientTabConstructor(tabId);
				if (tabConstructor == null) {
					logger.warn("タブが復元できません: tabId={}, uniqId={}", tabId, uniqId);
				} else {
					try {
						ClientTab tab =
								tabConstructor.newInstance(configuration,
										configProperties.getProperty("gui.tabs.data." + uniqId));
						configuration.addFrameTab(tab);
					} catch (IllegalArgumentException e) {
						logger.error("タブが復元できません: タブを初期化できません。tabId=" + tabId, e);
					} catch (InstantiationException e) {
						logger.error("タブが復元できません: タブを初期化できません。tabId=" + tabId, e);
					} catch (IllegalAccessException e) {
						logger.error("タブが復元できません: 正しくないアクセス指定子です。tabId=" + tabId, e);
					} catch (InvocationTargetException e) {
						logger.error("タブが復元できません: 初期化中にエラーが発生しました。tabId=" + tabId, e);
					}
				}
			}
		}

		TwitterDataFetchScheduler fetchScheduler = new TwitterDataFetchScheduler(configuration);
		configuration.setFetchScheduler(fetchScheduler);
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
		logger.info("Exiting elnetw...");
		frame.cleanUp();
		fetchScheduler.cleanUp();

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

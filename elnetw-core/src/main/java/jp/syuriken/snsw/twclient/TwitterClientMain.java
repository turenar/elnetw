package jp.syuriken.snsw.twclient;

import java.awt.TrayIcon;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.concurrent.CancellationException;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;
import jp.syuriken.snsw.twclient.filter.IllegalSyntaxException;
import jp.syuriken.snsw.twclient.filter.RootFilter;
import jp.syuriken.snsw.twclient.filter.UserFilter;
import jp.syuriken.snsw.twclient.handler.UserInfoViewActionHandler.UserInfoFrameTab;
import jp.syuriken.snsw.twclient.internal.NotifySendMessageNotifier;
import jp.syuriken.snsw.twclient.internal.TrayIconMessageNotifier;
import jp.syuriken.snsw.twclient.jni.LibnotifyMessageNotifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.auth.AccessToken;

/**
 * 実際に起動するランチャ
 *
 * @author Turenar <snswinhaiku dot lo at gmail dot com>
 */
public class TwitterClientMain {

	/** 設定ファイル名 */
	private static final String CONFIG_FILE_NAME = "elnetw.cfg";

	/** 設定 */
	protected final ClientConfiguration configuration;

	/** 設定データ */
	protected ClientProperties configProperties;

	/** スレッドホルダ */
	protected Object threadHolder = new Object();

	private Logger logger = LoggerFactory.getLogger(getClass());

	private Getopt getopt;

	private JobWorkerThread jobWorkerThread;

	/**
	 * インスタンスを生成する。
	 *
	 * @param args コマンドラインオプション
	 */
	public TwitterClientMain(String[] args, ClassLoader classLoader) {
		configuration = new ClientConfiguration();
		configuration.setExtraClassLoader(classLoader);
		configuration.setOpts(args);
		LongOpt[] longOpts = new LongOpt[]{
				new LongOpt("debug", LongOpt.NO_ARGUMENT, null, 'd'),
		};
		getopt = new Getopt("elnetw", args, "dL:D:", longOpts);

		try {
			javax.swing.UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			logger.warn("LookAndFeelの設定に失敗", e);
		}
	}

	/**
	 * 起動する。
	 *
	 * @return 終了値
	 */
	public int run() {
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
				case 'L':
				case 'D':
					break; // do nothing
				default:
					break;
			}
		}

		if (debugMode) {
			setDebugLogger();
		}

		configuration.setPortabledConfiguration(portable);
		File configRootDir = new File(configuration.getConfigRootDir());
		if (portable == false && configRootDir.exists() == false) {
			if (configRootDir.mkdirs()) {
				setConfigRootDirPermission(configRootDir);
			} else {
				logger.warn("ディレクトリの作成ができませんでした: {}", configRootDir.getPath());
			}
		}

		setTrayIcon();
		setDefaultConfigProperties();
		setConfigProperties();
		setMessageNotifiers();

		try {
			tryGetOAuthAccessToken();
		} catch (CancellationException e) {
			return 0; // user operation
		} catch (RuntimeException e) {
			e.printStackTrace();
			return 1;
		}

		startJobWorkerThread();

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
		configuration.setInitializing(false);
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
		configuration.getTimer().cancel();
		fetchScheduler.cleanUp();
		jobWorkerThread.cleanUp();

		return 0;
	}

	private void setConfigProperties() {
		configProperties = new ClientProperties(configuration.getConfigDefaultProperties());
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
	}

	@edu.umd.cs.findbugs.annotations.SuppressWarnings("RV_RETURN_VALUE_IGNORED_BAD_PRACTICE")
	private void setConfigRootDirPermission(File configRootDir) {
		configRootDir.setReadable(false, false);
		configRootDir.setWritable(false, false);
		configRootDir.setExecutable(false, false);
		configRootDir.setReadable(true, true);
		configRootDir.setWritable(true, true);
		configRootDir.setExecutable(true, true);
	}

	private void setDebugLogger() {
		URL resource = TwitterClientMain.class.getResource("/logback-debug.xml");
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

	private void setDefaultConfigProperties() {
		ClientProperties defaultConfig = new ClientProperties();
		try {
			InputStream stream = TwitterClientMain.class.getResourceAsStream("config.properties");
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
	}

	private void setMessageNotifiers() {
		Utility.addMessageNotifier(2000, LibnotifyMessageNotifier.class);
		Utility.addMessageNotifier(1000, NotifySendMessageNotifier.class);
		Utility.addMessageNotifier(0, TrayIconMessageNotifier.class);
	}

	private void setTrayIcon() {
		try {
			configuration.setTrayIcon(new TrayIcon(ImageIO.read(getClass().getClassLoader().getResourceAsStream(
					"jp/syuriken/snsw/twclient/img/icon16.png")), ClientConfiguration.APPLICATION_NAME));
		} catch (IOException e) {
			logger.error("icon ファイルの読み込みに失敗。");
		}
	}

	private void startJobWorkerThread() {
		jobWorkerThread = new JobWorkerThread(configuration.getJobQueue(), configuration);
		jobWorkerThread.start();
	}

	/**
	 * OAuthアクセストークンの取得を試す
	 *
	 * @return アクセストークン
	 */
	private boolean tryGetOAuthAccessToken() {
		if (configuration.getAccountList().length != 0) {
			return false;
		}

		Twitter twitter;
		AccessToken accessToken;
		do {
			try {
				twitter = new OAuthFrame(configuration).show();
				if (twitter == null) {
					int button = JOptionPane.showConfirmDialog(null, "終了しますか？", ClientConfiguration.APPLICATION_NAME,
							JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
					if (button == JOptionPane.YES_OPTION) {
						throw new CancellationException();
					}
				} else {
					accessToken = twitter.getOAuthAccessToken();
					break;
				}
			} catch (TwitterException e) {
				throw new RuntimeException(e);
			}
		} while (true);

		//将来の参照用に accessToken を永続化する
		String userId;
		try {
			userId = String.valueOf(twitter.getId());
		} catch (TwitterException e) {
			JOptionPane.showMessageDialog(null, "ユーザー情報の取得に失敗しました。時間をおいて試してみて下さい: " + e.getLocalizedMessage(), "エラー",
					JOptionPane.ERROR_MESSAGE);
			throw new RuntimeException(e);
		}
		configProperties.setProperty("twitter.oauth.access_token.list", userId);
		configProperties.setProperty("twitter.oauth.access_token.default", userId);
		configuration.storeAccessToken(accessToken);
		configProperties.store();
		return true;
	}
}

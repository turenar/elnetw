package jp.syuriken.snsw.twclient;

import java.awt.AWTException;
import java.awt.EventQueue;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;
import jp.syuriken.snsw.twclient.config.ActionButtonConfigType;
import jp.syuriken.snsw.twclient.config.BooleanConfigType;
import jp.syuriken.snsw.twclient.config.ConfigFrameBuilder;
import jp.syuriken.snsw.twclient.config.IntegerConfigType;
import jp.syuriken.snsw.twclient.filter.FilterCompiler;
import jp.syuriken.snsw.twclient.filter.FilterConfigurator;
import jp.syuriken.snsw.twclient.filter.FilterProperty;
import jp.syuriken.snsw.twclient.filter.IllegalSyntaxException;
import jp.syuriken.snsw.twclient.filter.UserFilter;
import jp.syuriken.snsw.twclient.filter.func.AndFilterFunction;
import jp.syuriken.snsw.twclient.filter.func.ExtractFilterFunction;
import jp.syuriken.snsw.twclient.filter.func.IfFilterFunction;
import jp.syuriken.snsw.twclient.filter.func.InRetweetFilterFunction;
import jp.syuriken.snsw.twclient.filter.func.NotFilterFunction;
import jp.syuriken.snsw.twclient.filter.func.OneOfFilterFunction;
import jp.syuriken.snsw.twclient.filter.func.OrFilterFunction;
import jp.syuriken.snsw.twclient.filter.prop.InListProperty;
import jp.syuriken.snsw.twclient.filter.prop.StandardBooleanProperties;
import jp.syuriken.snsw.twclient.filter.prop.StandardIntProperties;
import jp.syuriken.snsw.twclient.filter.prop.StandardStringProperties;
import jp.syuriken.snsw.twclient.gui.ClientTab;
import jp.syuriken.snsw.twclient.gui.DirectMessageViewTab;
import jp.syuriken.snsw.twclient.gui.MentionViewTab;
import jp.syuriken.snsw.twclient.gui.TimelineViewTab;
import jp.syuriken.snsw.twclient.gui.UserInfoFrameTab;
import jp.syuriken.snsw.twclient.handler.AccountVerifierActionHandler;
import jp.syuriken.snsw.twclient.handler.ClearPostBoxActionHandler;
import jp.syuriken.snsw.twclient.handler.FavoriteActionHandler;
import jp.syuriken.snsw.twclient.handler.HashtagActionHandler;
import jp.syuriken.snsw.twclient.handler.ListActionHandler;
import jp.syuriken.snsw.twclient.handler.MenuPropertyEditorActionHandler;
import jp.syuriken.snsw.twclient.handler.MenuQuitActionHandler;
import jp.syuriken.snsw.twclient.handler.MuteActionHandler;
import jp.syuriken.snsw.twclient.handler.OpenImageActionHandler;
import jp.syuriken.snsw.twclient.handler.PostActionHandler;
import jp.syuriken.snsw.twclient.handler.QuoteTweetActionHandler;
import jp.syuriken.snsw.twclient.handler.ReloginActionHandler;
import jp.syuriken.snsw.twclient.handler.RemoveTweetActionHandler;
import jp.syuriken.snsw.twclient.handler.ReplyActionHandler;
import jp.syuriken.snsw.twclient.handler.RetweetActionHandler;
import jp.syuriken.snsw.twclient.handler.SearchActionHandler;
import jp.syuriken.snsw.twclient.handler.TweetActionHandler;
import jp.syuriken.snsw.twclient.handler.UnofficialRetweetActionHandler;
import jp.syuriken.snsw.twclient.handler.UrlActionHandler;
import jp.syuriken.snsw.twclient.handler.UserInfoViewActionHandler;
import jp.syuriken.snsw.twclient.init.DynamicInitializeService;
import jp.syuriken.snsw.twclient.init.InitCondition;
import jp.syuriken.snsw.twclient.init.InitializeException;
import jp.syuriken.snsw.twclient.init.InitializeService;
import jp.syuriken.snsw.twclient.init.Initializer;
import jp.syuriken.snsw.twclient.init.InitializerInstance;
import jp.syuriken.snsw.twclient.internal.MenuConfiguratorActionHandler;
import jp.syuriken.snsw.twclient.internal.NotifySendMessageNotifier;
import jp.syuriken.snsw.twclient.internal.TrayIconMessageNotifier;
import jp.syuriken.snsw.twclient.jni.LibnotifyMessageNotifier;
import jp.syuriken.snsw.twclient.media.NullMediaProvider;
import jp.syuriken.snsw.twclient.media.RegexpMediaProvider;
import jp.syuriken.snsw.twclient.media.UrlProvider;
import jp.syuriken.snsw.twclient.net.DirectMessageFetcherFactory;
import jp.syuriken.snsw.twclient.net.MentionsFetcherFactory;
import jp.syuriken.snsw.twclient.net.StreamFetcherFactory;
import jp.syuriken.snsw.twclient.net.TimelineFetcherFactory;
import jp.syuriken.snsw.twclient.net.TwitterDataFetchScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.auth.AccessToken;

/**
 * 実際に起動するランチャ
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class TwitterClientMain {

	/** 設定ファイル名 */
	protected static final String CONFIG_FILE_NAME = "elnetw.cfg";
	public static final int JOBWORKER_JOIN_TIMEOUT = 32;
	@InitializerInstance
	private static volatile TwitterClientMain SINGLETON;

	public static synchronized TwitterClientMain getInstance(String[] args, ClassLoader classLoader) {
		if (SINGLETON != null) {
			throw new IllegalStateException("another instance always seems to be running");
		}
		SINGLETON = new TwitterClientMain(args, classLoader);
		return SINGLETON;
	}

	/** 終了する。 */
	public static synchronized void quit() {
		if (SINGLETON == null) {
			throw new IllegalStateException("no instance running!");
		}
		SINGLETON.isInterrupted = true;
		SINGLETON.MAIN_THREAD.interrupt();
	}

	/** 設定 */
	protected final ClientConfiguration configuration;
	/** for interruption */
	private final Thread MAIN_THREAD;
	/** 設定データ */
	protected ClientProperties configProperties;
	/** スレッドホルダ */
	protected Object threadHolder = new Object();
	private Logger logger = LoggerFactory.getLogger(getClass());
	protected Getopt getopt;
	protected JobWorkerThread jobWorkerThread;
	protected boolean debugMode;
	protected boolean portable;
	protected TwitterDataFetchScheduler fetchScheduler;
	protected TwitterClientFrame frame;
	private boolean isInterrupted;

	/**
	 * インスタンスを生成する。
	 *
	 * @param args コマンドラインオプション
	 */
	private TwitterClientMain(String[] args, ClassLoader classLoader) {
		MAIN_THREAD = Thread.currentThread();
		configuration = ClientConfiguration.getInstance();
		configuration.setExtraClassLoader(classLoader);
		configuration.setOpts(args);
		LongOpt[] longOpts = new LongOpt[] {
				new LongOpt("debug", LongOpt.NO_ARGUMENT, null, 'd'),
		};
		getopt = new Getopt("elnetw", args, "dL:D:", longOpts);

		try {
			javax.swing.UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			logger.warn("LookAndFeelの設定に失敗", e);
		}
	}

	@Initializer(name = "internal-addClientTabConstructor", phase = "init")
	public void addClientTabConstructor() {
		ClientConfiguration.putClientTabConstructor("timeline", TimelineViewTab.class);
		ClientConfiguration.putClientTabConstructor("mention", MentionViewTab.class);
		ClientConfiguration.putClientTabConstructor("directmessage", DirectMessageViewTab.class);
		ClientConfiguration.putClientTabConstructor("userinfo", UserInfoFrameTab.class);
	}

	@Initializer(name = "internal-addConfigurator-filter", dependencies = {"init-gui", "configBuilder"}, phase = "init")
	public void addConfiguratorOfFilter() {
		configuration.getConfigBuilder().getGroup("フィルタ")
				.addConfig("<ignore>", "フィルタの編集", "", new FilterConfigurator(configuration));
	}

	@Initializer(name = "actionHandler", phase = "init")
	public void initActionHandlerTable() {
		configuration.addActionHandler("reply", new ReplyActionHandler());
		configuration.addActionHandler("qt", new QuoteTweetActionHandler());
		configuration.addActionHandler("unofficial_rt", new UnofficialRetweetActionHandler());
		configuration.addActionHandler("rt", new RetweetActionHandler());
		configuration.addActionHandler("fav", new FavoriteActionHandler());
		configuration.addActionHandler("remove", new RemoveTweetActionHandler());
		configuration.addActionHandler("userinfo", new UserInfoViewActionHandler());
		configuration.addActionHandler("url", new UrlActionHandler());
		configuration.addActionHandler("clear", new ClearPostBoxActionHandler());
		configuration.addActionHandler("post", new PostActionHandler());
		configuration.addActionHandler("mute", new MuteActionHandler());
		configuration.addActionHandler("tweet", new TweetActionHandler());
		configuration.addActionHandler("list", new ListActionHandler());
		configuration.addActionHandler("hashtag", new HashtagActionHandler());
		configuration.addActionHandler("search", new SearchActionHandler());
		configuration.addActionHandler("openimg", new OpenImageActionHandler());
		configuration.addActionHandler("menu_quit", new MenuQuitActionHandler());
		configuration.addActionHandler("menu_propeditor", new MenuPropertyEditorActionHandler());
		configuration.addActionHandler("menu_account_verify", new AccountVerifierActionHandler());
		configuration.addActionHandler("menu_login_read", new ReloginActionHandler(false));
		configuration.addActionHandler("menu_login_write", new ReloginActionHandler(true));
		configuration.addActionHandler("menu_config", new MenuConfiguratorActionHandler());
	}

	@Initializer(name = "cacheManager", dependencies = {"config", "twitterAccountId"}, phase = "init")
	public void initCacheManager() {
		configuration.setCacheManager(new CacheManager(configuration));
	}

	@Initializer(name = "configBuilder", phase = "preinit")
	public void initConfigBuilder() {
		configuration.setConfigBuilder(new ConfigFrameBuilder(configuration));
	}

	@Initializer(name = "configurator", dependencies = {"init-gui", "configBuilder"}, phase = "init")
	public void initConfigurator() {
		ConfigFrameBuilder configBuilder = configuration.getConfigBuilder();
		configBuilder.getGroup("Twitter").getSubgroup("取得間隔 (秒)")
				.addConfig(ClientConfiguration.PROPERTY_INTERVAL_TIMELINE, "タイムライン", "秒数",
						new IntegerConfigType(60, 3600))
				.addConfig(ClientConfiguration.PROPERTY_INTERVAL_MENTIONS, "メンション(@通知)", "秒数",
						new IntegerConfigType(60, 3600))
				.addConfig(ClientConfiguration.PROPERTY_INTERVAL_DIRECT_MESSAGES, "ダイレクトメッセージ", "秒数",
						new IntegerConfigType(60, 10800))
				.getParentGroup().getSubgroup("取得数 (ツイート数)")
				.addConfig(ClientConfiguration.PROPERTY_PAGING_TIMELINE, "タイムライン", "ツイート",
						new IntegerConfigType(1, 200))
				.addConfig(ClientConfiguration.PROPERTY_PAGING_MENTIONS, "メンション(@通知)", "ツイート",
						new IntegerConfigType(1, 200))
				.addConfig(ClientConfiguration.PROPERTY_PAGING_DIRECT_MESSAGES, "ダイレクトメッセージ", "メッセージ",
						new IntegerConfigType(1, 200));
		configBuilder.getGroup("UI")
				.addConfig("gui.interval.list_update", "UI更新間隔 (ミリ秒)", "ミリ秒(ms)", new IntegerConfigType(100, 5000))
				.addConfig("gui.list.scroll", "スクロール量", null, new IntegerConfigType(1, 100));
		configBuilder
				.getGroup("core")
				.addConfig("core.info.survive_time", "一時的な情報を表示する時間 (ツイートの削除通知など)", "秒",
						new IntegerConfigType(1, 60, 1000))
				.addConfig("core.match.id_strict_match", "リプライ判定時のIDの厳格な一致", "チェックが入っていないときは先頭一致になります",
						new BooleanConfigType());
		configBuilder.getGroup("高度な設定").addConfig(null, "設定を直接編集する (動作保証対象外です)", null,
				new ActionButtonConfigType("プロパティーエディターを開く...", "menu_propeditor", frame));
	}

	@Initializer(name = "filter-functions", phase = "preinit")
	public void initFilterFunctions() {
		FilterCompiler.putFilterFunction("or", OrFilterFunction.getFactory());
		FilterCompiler.putFilterFunction("exactly_one_of", OneOfFilterFunction.getFactory());
		FilterCompiler.putFilterFunction("and", AndFilterFunction.getFactory());
		FilterCompiler.putFilterFunction("not", NotFilterFunction.getFactory());
		FilterCompiler.putFilterFunction("extract", ExtractFilterFunction.getFactory()); // for FilterEditFrame
		FilterCompiler.putFilterFunction("inrt", InRetweetFilterFunction.getFactory());
		FilterCompiler.putFilterFunction("if", IfFilterFunction.getFactory());
	}

	@Initializer(name = "filter-properties", phase = "preinit")
	public void initFilterProperties() {
		Constructor<? extends FilterProperty> properties;
		properties = StandardIntProperties.getFactory();
		FilterCompiler.putFilterProperty("userid", properties);
		FilterCompiler.putFilterProperty("in_reply_to_userid", properties);
		FilterCompiler.putFilterProperty("rtcount", properties);
		FilterCompiler.putFilterProperty("timediff", properties);
		properties = StandardBooleanProperties.getFactory();
		FilterCompiler.putFilterProperty("retweeted", properties);
		FilterCompiler.putFilterProperty("mine", properties);
		FilterCompiler.putFilterProperty("protected", properties);
		FilterCompiler.putFilterProperty("verified", properties);
		FilterCompiler.putFilterProperty("status", properties);
		FilterCompiler.putFilterProperty("dm", properties);
		properties = StandardStringProperties.getFactory();
		FilterCompiler.putFilterProperty("user", properties);
		FilterCompiler.putFilterProperty("text", properties);
		FilterCompiler.putFilterProperty("client", properties);

		FilterCompiler.putFilterProperty("in_list", InListProperty.getFactory());
	}

	@Initializer(name = "init-gui", dependencies = {"cacheManager", "twitterAccountId"}, phase = "init")
	public void initFrame() {
		frame = new TwitterClientFrame(configuration);
	}

	@Initializer(name = "imageCacher", dependencies = "config", phase = "init")
	public void initImageCacher() {
		configuration.setImageCacher(new ImageCacher(configuration));
	}

	/** ショートカットキーテーブルを初期化する。 */
	@Initializer(name = "shortcutKey", dependencies = "init-gui", phase = "init")
	public void initShortcutKey() {
		String parentConfigName = configProperties.getProperty("gui.shortcutkey.parent");
		Properties shortcutkeyProperties = new Properties();
		if (parentConfigName.trim().isEmpty() == false) {
			InputStream stream = null;
			try {
				stream = TwitterClientMain.class.getResourceAsStream(
						"shortcutkey/" + parentConfigName + ".properties");
				shortcutkeyProperties.load(stream);
			} catch (IOException e) {
				logger.error("ショートカットキーの読み込みに失敗", e);
			} finally {
				if (stream != null) {
					try {
						stream.close();
					} catch (IOException e) {
						logger.error("Failed closing resource", e);
					}
				}
			}
		}
		File file = new File(configuration.getConfigRootDir(), "shortcutkey.cfg");
		if (file.exists()) {
			InputStream inputStream = null;
			Reader reader = null;
			try {
				inputStream = new FileInputStream(file);
				reader = new InputStreamReader(inputStream, "UTF-8");
				shortcutkeyProperties.load(reader);
			} catch (FileNotFoundException e) {
				logger.error("ショートカットキーファイルのオープンに失敗", e);
			} catch (IOException e) {
				logger.error("ショートカットキーの読み込みに失敗", e);
			} finally {
				try {
					if (reader != null) {
						reader.close();
					}
				} catch (IOException e) {
					logger.warn("shortcutkey.cfgのクローズに失敗", e);
				}
				try {
					if (inputStream != null) {
						inputStream.close();
					}
				} catch (IOException e) {
					logger.warn("shortcutkey.cfgのクローズに失敗", e);
				}
			}
		}
		for (Object obj : shortcutkeyProperties.keySet()) {
			String key = (String) obj;
			frame.addShortcutKey(key, shortcutkeyProperties.getProperty(key));
		}
	}

	@Initializer(name = "urlProvider", phase = "init")
	public void initUrlProviders() {
		UrlProvider.addMediaProvider("\\.(jpe?g|png|gif)",
				new NullMediaProvider());
		UrlProvider.addMediaProvider("^http://twitpic\\.com/[a-zA-Z0-9]+",
				new RegexpMediaProvider("http://.*?\\.cloudfront\\.net/photos/(?:large|full)/[\\w.]+"));
	}

	@Initializer(name = "internal-init-fetchSched", dependencies = "recover-clientTabs", phase = "prestart")
	public void realConnectFetchSched() {
		fetchScheduler.onInitialized();
	}

	@Initializer(name = "recover-clientTabs", phase = "prestart")
	public void recoverClientTabs() {
		String tabsList = configProperties.getProperty("gui.tabs.list");
		if (tabsList == null) {
			try {
				configuration.addFrameTab(new TimelineViewTab());
				configuration.addFrameTab(new MentionViewTab());
				configuration.addFrameTab(new DirectMessageViewTab());
			} catch (IllegalSyntaxException e) {
				throw new AssertionError(e); // This can't happen: because no query
			}
		} else {
			String[] tabs = tabsList.split(" ");
			for (String tabIndetifier : tabs) {
				if (tabIndetifier.isEmpty()) {
					continue;
				}
				int separatorPosition = tabIndetifier.indexOf(':');
				String tabId = tabIndetifier.substring(0, separatorPosition);
				String uniqId = tabIndetifier.substring(separatorPosition + 1);
				Constructor<? extends ClientTab> tabConstructor = ClientConfiguration.getClientTabConstructor(tabId);
				if (tabConstructor == null) {
					logger.warn("タブが復元できません: tabId={}, uniqId={}", tabId, uniqId);
				} else {
					try {
						ClientTab tab = tabConstructor.newInstance(
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
	}

	/**
	 * 起動する。
	 *
	 * @return 終了値
	 */
	public int run() {
		Getopt getopt = this.getopt;
		int c;
		portable = Boolean.getBoolean("config.portable");
		debugMode = false;
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

		setDebugLogger();

		InitializeService initializeService = DynamicInitializeService.use(configuration);
		initializeService
				.registerPhase("earlyinit") //
				.registerPhase("preinit") //
				.registerPhase("init") //
				.registerPhase("postinit") //
				.registerPhase("prestart") //
				.registerPhase("start") //
				.register(TwitterClientMain.class);

		try {
			initializeService
					.enterPhase("earlyinit") //
					.enterPhase("preinit") //
					.enterPhase("init") //
					.enterPhase("postinit") //
					.enterPhase("prestart") //
					.enterPhase("start");
		} catch (InitializeException e) {
			logger.error("failed initialization", e);
			return e.getExitCode();
		}
		logger.info("Initialized");

		synchronized (threadHolder) {
			try {
				while (!isInterrupted) {
					threadHolder.wait();
				}
			} catch (InterruptedException e) {
				// interrupted shows TCM#quit() is called.
			}
		}

		try {
			initializeService.uninit();
		} catch (InitializeException e) {
			logger.error("failed quitting", e);
			return e.getExitCode();
		}

		return 0;
	}

	@Initializer(name = "twitterAccountId", dependencies = "config", phase = "earlyinit")
	public void setAccountId() {
		String defaultAccountId = configuration.getDefaultAccountId();
		configuration.setAccountIdForRead(defaultAccountId);
		configuration.setAccountIdForWrite(defaultAccountId);
	}

	@Initializer(name = "config", dependencies = "default-config", phase = "earlyinit")
	public void setConfigProperties(InitCondition cond) {
		if (cond.isInitializingPhase()) {
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

			String configVersion = configProperties.getProperty("cfg.version", "0");
			InitializeService.getService().provideInitializer("config-v" + configVersion, true);
		} else {
			configProperties.store();
		}
	}

	@edu.umd.cs.findbugs.annotations.SuppressWarnings("RV_RETURN_VALUE_IGNORED_BAD_PRACTICE")
	public void setConfigRootDirPermission(File configRootDir) {
		configRootDir.setReadable(false, false);
		configRootDir.setWritable(false, false);
		configRootDir.setExecutable(false, false);
		configRootDir.setReadable(true, true);
		configRootDir.setWritable(true, true);
		configRootDir.setExecutable(true, true);
	}

	private void setDebugLogger() {
		if (debugMode) {
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
	}

	@Initializer(name = "default-config", dependencies = "internal-portableConfig", phase = "earlyinit")
	public void setDefaultConfigProperties() {
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

	@Initializer(name = "set-globalfilter",
			dependencies = {"config", "filter-functions", "filter-properties"}, phase = "init")
	public void setDefaultFilter() {
		configuration.addFilter(new UserFilter(UserFilter.PROPERTY_KEY_FILTER_GLOBAL_QUERY));
	}

	@Initializer(name = "fetch-sched", dependencies = {"set-globalfilter", "cacheManager"}, phase = "init")
	public void setFetchScheduler(InitCondition cond) {
		if (cond.isInitializingPhase()) {
			fetchScheduler = new TwitterDataFetchScheduler();
			configuration.setFetchScheduler(fetchScheduler);
		} else {
			fetchScheduler.cleanUp();
		}
	}

	@Initializer(name = "set-fetchsched-notifier", dependencies = "fetch-sched", phase = "init")
	public void setFetcherFactory() {
		fetchScheduler.addVirtualNotifier("my/timeline", new String[] {"stream/user", "statuses/timeline"});
		fetchScheduler.addFetcherFactory("stream/user", new StreamFetcherFactory());
		fetchScheduler.addFetcherFactory("statuses/timeline", new TimelineFetcherFactory());
		fetchScheduler.addFetcherFactory("statuses/mentions", new MentionsFetcherFactory());
		fetchScheduler.addFetcherFactory("direct_messages", new DirectMessageFetcherFactory());
	}

	@Initializer(name = "finish-initPhase", phase = "start")
	public void setInitializePhaseFinished() {
		configuration.setInitializing(false);
		fetchScheduler.onInitialized();
	}

	@Initializer(name = "internal-messageNotifiers", phase = "prestart")
	public void setMessageNotifiersCandidate() {
		Utility.addMessageNotifier(2000, LibnotifyMessageNotifier.class);
		Utility.addMessageNotifier(1000, NotifySendMessageNotifier.class);
		Utility.addMessageNotifier(0, TrayIconMessageNotifier.class);
	}

	@Initializer(name = "internal-portableConfig", phase = "earlyinit")
	public void setPortabledConfigDir() {
		configuration.setPortabledConfiguration(portable);

		File configRootDir = new File(configuration.getConfigRootDir());
		if (portable == false && configRootDir.exists() == false) {
			if (configRootDir.mkdirs()) {
				setConfigRootDirPermission(configRootDir);
			} else {
				logger.warn("ディレクトリの作成ができませんでした: {}", configRootDir.getPath());
			}
		}
	}

	@Initializer(name = "timer", phase = "earlyinit")
	public void setTimer(InitCondition cond) {
		if (cond.isInitializingPhase()) {
			configuration.setTimer(Executors.newSingleThreadScheduledExecutor());
		} else {
			ScheduledExecutorService timer = configuration.getTimer();
			timer.shutdown();
			try {
				if (!timer.awaitTermination(5, TimeUnit.SECONDS)) {
					logger.error("Failed shutdown timer: timeout");
					timer.shutdownNow();
				}
			} catch (InterruptedException e) {
				timer.shutdownNow();
			}
		}
	}

	@Initializer(name = "show-trayicon", phase = "start")
	public void setTrayIcon(InitCondition cond) {
		if (cond.isInitializingPhase()) {
			if (SystemTray.isSupported()) {
				try {
					SystemTray.getSystemTray().add(configuration.getTrayIcon());
				} catch (AWTException e) {
					logger.warn("SystemTrayへの追加に失敗", e);
				}
			}
		} else {
			if (SystemTray.isSupported()) {
				SystemTray.getSystemTray().remove(configuration.getTrayIcon());
			}
		}
	}

	@Initializer(name = "tray", phase = "prestart")
	public void setTrayIcon() {
		try {
			configuration.setTrayIcon(new TrayIcon(ImageIO.read(getClass().getClassLoader().getResourceAsStream(
					"jp/syuriken/snsw/twclient/img/icon16.png")), ClientConfiguration.APPLICATION_NAME));
		} catch (IOException e) {
			logger.error("icon ファイルの読み込みに失敗。");
		}
	}

	@Initializer(name = "show-gui", phase = "start")
	public void showFrame(InitCondition cond) {
		if (cond.isInitializingPhase()) {
			java.awt.EventQueue.invokeLater(new Runnable() {

				@Override
				public void run() {
					frame.start();
				}
			});
		} else {
			logger.info("Exiting elnetw...");
			try {
				EventQueue.invokeAndWait(new Runnable() {
					@Override
					public void run() {
						frame.setVisible(false);
						frame.dispose();
					}
				});
			} catch (InterruptedException e) {
				logger.error("interrupted while closing frame", e);
			} catch (InvocationTargetException e) {
				logger.error("Caught error", e.getCause());
			}
		}
	}

	@Initializer(name = "jobqueue", dependencies = "config", phase = "preinit")
	public void startJobWorkerThread(InitCondition cond) {
		if (cond.isInitializingPhase()) {
			jobWorkerThread = new JobWorkerThread(configuration.getJobQueue());
			jobWorkerThread.start();
		} else {
			jobWorkerThread.cleanUp();
			while (true) {
				try {
					jobWorkerThread.join(JOBWORKER_JOIN_TIMEOUT);
					if (jobWorkerThread.isAlive()) {
						// ImageIO caught interrupt but not set INTERRUPTED-STATUS
						// If it seemed to be occurred, retry to shutdown jobWorker
						jobWorkerThread.cleanUp();
					} else {
						break;
					}
				} catch (InterruptedException e) {
					continue;
				}
			}
		}
	}

	/** OAuthアクセストークンの取得を試す */
	@Initializer(name = "accesstoken", dependencies = "config", phase = "earlyinit")
	public void tryGetOAuthAccessToken(InitCondition cond) {
		if (cond.isInitializingPhase() == false) {
			return;
		}

		if (configuration.getAccountList().length != 0) {
			return;
		}

		Twitter twitter;
		AccessToken accessToken;
		do {
			try {
				twitter = new OAuthHelper(configuration).show();
				if (twitter == null) {
					int button = JOptionPane.showConfirmDialog(null, "終了しますか？", ClientConfiguration.APPLICATION_NAME,
							JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
					if (button == JOptionPane.YES_OPTION) {
						cond.setFailStatus("OAuth failed: canceled", -1);
						return;
					}
				} else {
					accessToken = twitter.getOAuthAccessToken();
					break;
				}
			} catch (TwitterException e) {
				cond.setFailStatus("error", 1);
				return;
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
		return;
	}

	@Initializer(name = "config-v1", dependencies = "config-v0", phase = "earlyinit")
	public void updateConfigToV1() {
		logger.info("Updating config to v1");
		configProperties.setProperty("cfg.version", "1");
	}

	@Initializer(name = "config-v2", dependencies = "config-v1", phase = "earlyinit")
	public void updateConfigToV2() {
		logger.info("Updating config to v2");
		if (configProperties.containsKey(ClientConfiguration.PROPERTY_INTERVAL_TIMELINE)) {
			configProperties.setInteger(ClientConfiguration.PROPERTY_INTERVAL_TIMELINE,
					configProperties.getInteger(ClientConfiguration.PROPERTY_INTERVAL_TIMELINE) / 1000);
		}
		configProperties.setProperty("cfg.version", "2");
	}
}

/*
 * The MIT License (MIT)
 * Copyright (c) 2011-2014 Turenai Project
 *
 * Permission is hereby granted, free of charge,
 *  to any person obtaining a copy of this software and associated documentation files (the "Software"),
 *  to deal in the Software without restriction, including without limitation the rights to
 *  use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 *  and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 *  in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 *  INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 *  IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 *  DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

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
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.TreeSet;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;
import jp.syuriken.snsw.twclient.bus.DirectMessageFetcherFactory;
import jp.syuriken.snsw.twclient.bus.MentionsFetcherFactory;
import jp.syuriken.snsw.twclient.bus.MessageBus;
import jp.syuriken.snsw.twclient.bus.NullMessageChannelFactory;
import jp.syuriken.snsw.twclient.bus.StreamFetcherFactory;
import jp.syuriken.snsw.twclient.bus.TimelineFetcherFactory;
import jp.syuriken.snsw.twclient.config.ActionButtonConfigType;
import jp.syuriken.snsw.twclient.config.BooleanConfigType;
import jp.syuriken.snsw.twclient.config.ConfigFrameBuilder;
import jp.syuriken.snsw.twclient.config.ConsumerTokenConfigType;
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
import jp.syuriken.snsw.twclient.gui.render.simple.RenderObjectHandler;
import jp.syuriken.snsw.twclient.handler.AccountVerifierActionHandler;
import jp.syuriken.snsw.twclient.handler.ClearPostBoxActionHandler;
import jp.syuriken.snsw.twclient.handler.DoNothingActionHandler;
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
import jp.syuriken.snsw.twclient.media.NullMediaResolver;
import jp.syuriken.snsw.twclient.media.RegexpMediaResolver;
import jp.syuriken.snsw.twclient.media.UrlResolverManager;
import jp.syuriken.snsw.twclient.media.XpathMediaResolver;
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

	private static class LogFileVisitor extends SimpleFileVisitor<Path> {
		private final TreeSet<Path> logFileSet;
		private PathMatcher matcher;

		public LogFileVisitor(TreeSet<Path> logFileSet) {
			this.logFileSet = logFileSet;
			matcher = FileSystems.getDefault().getPathMatcher("glob:elnetw-*.log");
		}

		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
			if (matcher.matches(file.getFileName())) {
				logFileSet.add(file);
			}
			return FileVisitResult.CONTINUE;
		}
	}

	private class CacheCleanerVisitor extends SimpleFileVisitor<Path> {
		long cacheExpire = configuration.getConfigProperties().getLong("core.cache.icon.survive_time");

		@Override
		public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
			try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(dir)) {
				if (!directoryStream.iterator().hasNext()) {
					// Directory is empty
					Files.delete(dir);
					logger.debug("Delete empty dir: {}", dir);
				}
			} catch (IOException e) {
				logger.debug("Fail readdir: {}", dir, e);
			}
			return FileVisitResult.CONTINUE;
		}

		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
			long lastModified = attrs.lastModifiedTime().toMillis();
			if (lastModified + cacheExpire < System.currentTimeMillis()) {
				try {
					Files.delete(file);
					logger.debug("clean expired cache: {} (lastModified:{})",
							Utility.protectPrivacy(file.toString()), lastModified);
				} catch (IOException e) {
					logger.warn("Failed cleaning cache: {}",
							Utility.protectPrivacy(file.toString()), e);
				}
			}
			return FileVisitResult.CONTINUE;
		}
	}

	/** 設定ファイル名 */
	protected static final String CONFIG_FILE_NAME = "elnetw.cfg";
	public static final int JOBWORKER_JOIN_TIMEOUT = 32;
	public static final int LAUNCHER_ABI_VERSION = 1;
	@InitializerInstance
	private static volatile TwitterClientMain SINGLETON;

	public static synchronized TwitterClientMain getInstance(String[] args, ClassLoader classLoader) {
		if (SINGLETON != null) {
			throw new IllegalStateException("another instance always seems to be running");
		}
		SINGLETON = new TwitterClientMain(args, classLoader);
		return SINGLETON;
	}

	/**
	 * このクラスのABIバージョンを返す。method signatureの変更等でこの数値を上げる。
	 * ランチャーは対応していないABIバージョンが帰って来た時、エラー終了しなければならない。
	 *
	 * @return ABIバージョン
	 */
	@SuppressWarnings("UnusedDeclaration")
	/*package*/ static int getLauncherAbiVersion() {
		return LAUNCHER_ABI_VERSION;
	}

	private static HashMap<String, Object> getResultMap(int exitCode, boolean isCrash) {
		HashMap<String, Object> ret = new HashMap<>();
		ret.put("exitCode", exitCode);
		ret.put("isCrash", isCrash);
		return ret;
	}

	/** 終了する。 */
	public static synchronized void quit() {
		if (SINGLETON == null) {
			throw new IllegalStateException("no instance running!");
		}
		SINGLETON.isInterrupted = true;
		SINGLETON.MAIN_THREAD.interrupt();
	}

	private final ClassLoader classLoader;
	/** for interruption */
	private final Thread MAIN_THREAD;
	/** スレッドホルダ */
	protected final Object threadHolder = new Object();
	private String[] args;
	/** 設定 */
	protected ClientConfiguration configuration;
	/** 設定データ */
	protected ClientProperties configProperties;
	private Logger logger;
	protected Getopt getopt;
	protected JobQueue jobQueue;
	protected boolean debugMode;
	protected boolean portable;
	protected MessageBus messageBus;
	protected TwitterClientFrame frame;
	private boolean isInterrupted;

	/**
	 * インスタンスを生成する。
	 *
	 * @param args コマンドラインオプション
	 */
	private TwitterClientMain(String[] args, ClassLoader classLoader) {
		this.args = args;
		this.classLoader = classLoader;
		MAIN_THREAD = Thread.currentThread();
		LongOpt[] longOpts = new LongOpt[] {
				new LongOpt("debug", LongOpt.NO_ARGUMENT, null, 'd'),
		};
		getopt = new Getopt("elnetw", args, "dL:D:", longOpts);

		try {
			javax.swing.UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace(); // logger cannot use.
		}
	}

	@Initializer(name = "gui/tab/init-factory", phase = "init")
	public void addClientTabConstructor() {
		ClientConfiguration.putClientTabConstructor("timeline", TimelineViewTab.class);
		ClientConfiguration.putClientTabConstructor("mention", MentionViewTab.class);
		ClientConfiguration.putClientTabConstructor("directmessage", DirectMessageViewTab.class);
		ClientConfiguration.putClientTabConstructor("userinfo", UserInfoFrameTab.class);
	}

	@Initializer(name = "gui/config/filter", dependencies = {"gui/main", "gui/config/builder"}, phase = "init")
	public void addConfiguratorOfFilter() {
		configuration.getConfigBuilder().getGroup("フィルタ")
				.addConfig("<ignore>", "フィルタの編集", "", new FilterConfigurator(configuration));
	}

	@Initializer(name = "clean/log", phase = "poststart")
	public void cleanLogFile() {
		String appHome = System.getProperty("elnetw.home");
		String logDir = appHome + "/log";
		final TreeSet<Path> logFileSet = new TreeSet<>();
		try {
			Files.walkFileTree(new File(logDir).toPath(), new LogFileVisitor(logFileSet));
		} catch (IOException e) {
			logger.warn("fail traversing dir", e);
		}
		Iterator<Path> pathIterator = logFileSet.descendingIterator();
		for (int i = 0; i < 5; i++) {
			if (pathIterator.hasNext()) {
				pathIterator.next();
			} else {
				return;
			}
		}
		for (; pathIterator.hasNext(); ) {
			Path next = pathIterator.next();
			try {
				Files.delete(next);
			} catch (IOException e) {
				logger.warn("fail deleting file", e);
			}
		}
	}


	/**
	 * ディスクキャッシュから期限切れのユーザーアイコンを削除する。
	 */
	@Initializer(name = "clean/iconcache", dependencies = {"cache/image", "config"}, phase = "poststart")
	public void cleanOldUserIconCache() {
		Path userIconCacheDir = new File(System.getProperty("elnetw.cache.dir"), "user").toPath();
		try {
			Files.walkFileTree(userIconCacheDir, new CacheCleanerVisitor());
		} catch (IOException e) {
			e.printStackTrace();
		}
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
		configuration.addActionHandler("blackhole", new DoNothingActionHandler());
		configuration.addActionHandler("menu_quit", new MenuQuitActionHandler());
		configuration.addActionHandler("menu_propeditor", new MenuPropertyEditorActionHandler());
		configuration.addActionHandler("menu_account_verify", new AccountVerifierActionHandler());
		configuration.addActionHandler("menu_login_read", new ReloginActionHandler(false));
		configuration.addActionHandler("menu_login_write", new ReloginActionHandler(true));
		configuration.addActionHandler("menu_config", new MenuConfiguratorActionHandler());
		configuration.addActionHandler("<elnetw>.gui.render.simple.RenderObjectHandler", new RenderObjectHandler());
	}

	@Initializer(name = "cache", dependencies = {"config", "accountId"}, phase = "init")
	public void initCacheManager() {
		configuration.setCacheManager(new CacheManager(configuration));
	}

	@Initializer(name = "gui/config/builder", phase = "preinit")
	public void initConfigBuilder() {
		configuration.setConfigBuilder(new ConfigFrameBuilder(configuration));
	}

	@Initializer(name = "gui/config/core", dependencies = {"gui/main", "gui/config/builder"}, phase = "init")
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
		configBuilder.getGroup("core")
				.addConfig("core.info.survive_time", "一時的な情報を表示する時間 (ツイートの削除通知など)", "秒",
						new IntegerConfigType(1, 60, 1000))
				.addConfig("core.match.id_strict_match", "リプライ判定時のIDの厳格な一致", "チェックが入っていないときは先頭一致になります",
						new BooleanConfigType());
		configBuilder.getGroup("高度な設定")
				.addConfig(null, "コンシューマーキーの設定", null, new ConsumerTokenConfigType())
				.addConfig(null, "設定を直接編集する (動作保証対象外です)", null,
						new ActionButtonConfigType("プロパティーエディターを開く...", "menu_propeditor", frame));
	}

	@Initializer(name = "filter/func", phase = "preinit")
	public void initFilterFunctions() {
		FilterCompiler.putFilterFunction("or", OrFilterFunction.getFactory());
		FilterCompiler.putFilterFunction("exactly_one_of", OneOfFilterFunction.getFactory());
		FilterCompiler.putFilterFunction("and", AndFilterFunction.getFactory());
		FilterCompiler.putFilterFunction("not", NotFilterFunction.getFactory());
		FilterCompiler.putFilterFunction("extract", ExtractFilterFunction.getFactory()); // for FilterEditFrame
		FilterCompiler.putFilterFunction("inrt", InRetweetFilterFunction.getFactory());
		FilterCompiler.putFilterFunction("if", IfFilterFunction.getFactory());
	}

	@Initializer(name = "filter/prop", phase = "preinit")
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

	@Initializer(name = "gui/main", dependencies = {"cache", "accountId"}, phase = "init")
	public void initFrame() {
		frame = new TwitterClientFrame(configuration);
	}

	@Initializer(name = "cache/image", dependencies = "config", phase = "init")
	public void initImageCacher() {
		configuration.setImageCacher(new ImageCacher(configuration));
	}

	/** ショートカットキーテーブルを初期化する。 */
	@Initializer(name = "gui/shortcutKey", dependencies = "gui/main", phase = "init")
	public void initShortcutKey() {
		String parentConfigName = configProperties.getProperty("gui.shortcutkey.parent");
		Properties shortcutkeyProperties = new Properties();
		if (!parentConfigName.trim().isEmpty()) {
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
		UrlResolverManager.addMediaProvider("\\.(jpe?g|png|gif)", new NullMediaResolver(true));
		UrlResolverManager.addMediaProvider("^http://twitpic\\.com/[a-zA-Z0-9]+",
				new RegexpMediaResolver("http://.*?\\.cloudfront\\.net/photos/(?:large|full)/[\\w.]+"));
		UrlResolverManager.addMediaProvider("^http://p\\.twipple\\.jp/[a-zA-Z0-9]+",
				new XpathMediaResolver("id('post_image')/@src"));
	}

	@Initializer(name = "bus/init", dependencies = "gui/tab/restore", phase = "prestart")
	public void realConnectMessageBus() {
		messageBus.onInitialized();
	}

	@Initializer(name = "gui/tab/restore", dependencies = {"config", "bus", "filter/global", "gui/tab/init-factory"},
			phase = "prestart")
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
			for (String tabIdentifier : tabs) {
				if (tabIdentifier.isEmpty()) {
					continue;
				}
				int separatorPosition = tabIdentifier.indexOf(':');
				String tabId = tabIdentifier.substring(0, separatorPosition);
				String uniqId = tabIdentifier.substring(separatorPosition + 1);
				Constructor<? extends ClientTab> tabConstructor = ClientConfiguration.getClientTabConstructor(tabId);
				if (tabConstructor == null) {
					logger.warn("タブが復元できません: tabId={}, uniqId={}", tabId, uniqId);
				} else {
					try {
						ClientTab tab = tabConstructor.newInstance(
								configProperties.getProperty("gui.tabs.data." + uniqId));
						configuration.addFrameTab(tab);
					} catch (IllegalArgumentException | InstantiationException e) {
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
	public HashMap<String, Object> run() {
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
		setHomeProperty();
		setDebugLogger();
		configuration = ClientConfiguration.getInstance();
		configuration.setExtraClassLoader(classLoader);
		configuration.setOpts(args);

		logger.info("elnetw version {}", VersionInfo.getDescribedVersion());

		InitializeService initializeService = DynamicInitializeService.use(configuration);
		initializeService
				.registerPhase("earlyinit")
				.registerPhase("preinit")
				.registerPhase("init")
				.registerPhase("postinit")
				.registerPhase("prestart")
				.registerPhase("start")
				.registerPhase("poststart")
				.register(TwitterClientMain.class);

		try {
			initializeService
					.enterPhase("earlyinit")
					.enterPhase("preinit")
					.enterPhase("init")
					.enterPhase("postinit")
					.enterPhase("prestart")
					.enterPhase("start")
					.enterPhase("poststart");
		} catch (InitializeException e) {
			logger.error("failed initialization", e);
			return getResultMap(e.getExitCode(), true);
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
			return getResultMap(e.getExitCode(), true);
		}

		return getResultMap(0, false);
	}

	@Initializer(name = "accountId", dependencies = "config", phase = "earlyinit")
	public void setAccountId() {
		String defaultAccountId = configuration.getDefaultAccountId();
		configuration.setAccountIdForRead(defaultAccountId);
		configuration.setAccountIdForWrite(defaultAccountId);
	}

	@Initializer(name = "config", dependencies = "config/default", phase = "earlyinit")
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
			InitializeService.getService().provideInitializer("config/update/v" + configVersion, true);
		} else {
			configProperties.store();
		}
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
		logger = LoggerFactory.getLogger(getClass());
	}

	@Initializer(name = "config/default", dependencies = "internal/portableConfig", phase = "earlyinit")
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

	@Initializer(name = "internal-setDefaultExceptionHandler", phase = "earlyinit")
	public void setDefaultExceptionHandler() {
		Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(Thread t, Throwable e) {
				logger.error("Uncaught exception", e);
				quit();
			}
		});
	}

	@Initializer(name = "filter/global", dependencies = "config", phase = "init")
	public void setDefaultFilter() {
		configuration.addFilter(new UserFilter(UserFilter.PROPERTY_KEY_FILTER_GLOBAL_QUERY));
	}

	private void setHomeProperty() {
		String appHomeDir = System.getProperty("elnetw.home");
		String cacheDir = System.getProperty("elnetw.cache.dir");
		// do not use Utility: it initializes logger!
		if (System.getProperty("os.name").contains("Windows")) {
			appHomeDir = appHomeDir == null ? System.getenv("APPDATA") : appHomeDir;
			cacheDir = ((cacheDir == null) ? (System.getProperty("java.io.tmpdir") + "/elnetw/cache") : cacheDir);
		} else {
			appHomeDir = ((appHomeDir == null) ? (System.getProperty("user.home") + "/.elnetw") : appHomeDir);
			cacheDir = cacheDir == null ? System.getProperty("user.home") + "/.cache/elnetw" : cacheDir;
			Path cacheDirPath = new File(cacheDir).toPath();
			Path cacheLinkPath = new File(appHomeDir, "cache").toPath();
			if (!Files.exists(cacheLinkPath, LinkOption.NOFOLLOW_LINKS)) {
				try {
					Files.createSymbolicLink(cacheLinkPath, cacheDirPath);
				} catch (IOException e) {
					System.err.println(
							"[core] failed symbolic link from '" + appHomeDir + "'/cache to '" + cacheDir + "'");
				}
			}
		}
		tryMkdir(appHomeDir);
		tryMkdir(cacheDir);
		System.setProperty("elnetw.home", appHomeDir);
		System.setProperty("elnetw.cache.dir", cacheDir);
	}

	@Initializer(name = "internal/finish-initPhase", dependencies = "gui/main/show", phase = "start")
	public void setInitializePhaseFinished() {
		configuration.setInitializing(false);
		messageBus.onInitialized();
	}

	@Initializer(name = "bus", dependencies = {"jobqueue", "config"}, phase = "preinit")
	public void setMessageBus(InitCondition cond) {
		if (cond.isInitializingPhase()) {
			messageBus = new MessageBus();
			configuration.setMessageBus(messageBus);
		} else {
			messageBus.cleanUp();
		}
	}

	@Initializer(name = "bus/factory", dependencies = "bus", phase = "init")
	public void setMessageChannelFactory() {
		messageBus.addVirtualChannel("my/timeline", new String[] {"stream/user", "statuses/timeline"});
		messageBus.addChannelFactory("stream/user", new StreamFetcherFactory());
		messageBus.addChannelFactory("statuses/timeline", new TimelineFetcherFactory());
		messageBus.addChannelFactory("statuses/mentions", new MentionsFetcherFactory());
		messageBus.addChannelFactory("direct_messages", new DirectMessageFetcherFactory());
		messageBus.addChannelFactory("core", NullMessageChannelFactory.INSTANCE);
	}

	@Initializer(name = "internal/notifier", phase = "prestart")
	public void setMessageNotifiersCandidate() {
		Utility.addMessageNotifier(2000, LibnotifyMessageNotifier.class);
		Utility.addMessageNotifier(1000, NotifySendMessageNotifier.class);
		Utility.addMessageNotifier(0, TrayIconMessageNotifier.class);
	}

	@Initializer(name = "internal/portableConfig", phase = "earlyinit")
	public void setPortabledConfigDir() {
		configuration.setPortabledConfiguration(portable);
	}

	@Initializer(name = "timer", phase = "earlyinit")
	public void setTimer(InitCondition cond) {
		if (cond.isInitializingPhase()) {
			configuration.setTimer(new ScheduledThreadPoolExecutor(1, new ThreadFactory() {
				@Override
				public Thread newThread(@Nonnull Runnable r) {
					return new Thread(r, "timer");
				}
			}));
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

	@Initializer(name = "gui/tray/show", dependencies = "gui/tray", phase = "start")
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

	@Initializer(name = "gui/tray", phase = "prestart")
	public void setTrayIcon() {
		try {
			configuration.setTrayIcon(new TrayIcon(ImageIO.read(getClass().getClassLoader().getResourceAsStream(
					"jp/syuriken/snsw/twclient/img/icon16.png")), ClientConfiguration.APPLICATION_NAME));
		} catch (IOException e) {
			logger.error("icon ファイルの読み込みに失敗。");
		}
	}

	@Initializer(name = "gui/main/show", dependencies = {"gui/main", "gui/tab/restore"}, phase = "start")
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

	@Initializer(name = "jobqueue", dependencies = "config", phase = "earlyinit")
	public void startJobWorkerThread(InitCondition cond) {
		if (cond.isInitializingPhase()) {
			jobQueue = new JobQueue();
			configuration.setJobQueue(jobQueue);
		} else {
			jobQueue.shutdownWorkerThreads();
			while (true) {
				try {
					if (jobQueue.shutdownNow(JOBWORKER_JOIN_TIMEOUT)) {
						break;
					} else {
						// ImageIO caught interrupt but not set INTERRUPTED-STATUS
						// If it seemed to be occurred, retry to shutdown jobWorker
						jobQueue.shutdownWorkerThreads();
					}
				} catch (InterruptedException e) {
					// continue;
				}
			}
		}
	}

	/** OAuthアクセストークンの取得を試す */
	@Initializer(name = "accesstoken", dependencies = "config", phase = "earlyinit")
	public void tryGetOAuthAccessToken(InitCondition cond) {
		if (!cond.isInitializingPhase()) {
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
	}

	private void tryMkdir(String dir) {
		Path dirPath = new File(dir).toPath();
		if (!Files.isDirectory(dirPath)) {
			try {
				Files.createDirectories(dirPath);
			} catch (IOException e) {
				logger.warn("Failed mkdir", e);
			}
			try {
				Files.setPosixFilePermissions(dirPath, PosixFilePermissions.fromString("rwx------"));
			} catch (UnsupportedOperationException e) {
				// ignore
			} catch (IOException e) {
				logger.warn("Failed setting posix permission", e);
			}

		}
	}

	@Initializer(name = "config/update/v1", dependencies = "config/update/v0", phase = "earlyinit")
	public void updateConfigToV1() {
		logger.info("Updating config to v1");
		configProperties.setProperty("cfg.version", "1");
	}

	@Initializer(name = "config/update/v2", dependencies = "config/update/v1", phase = "earlyinit")
	public void updateConfigToV2() {
		logger.info("Updating config to v2");
		if (configProperties.containsKey(ClientConfiguration.PROPERTY_INTERVAL_TIMELINE)) {
			configProperties.setInteger(ClientConfiguration.PROPERTY_INTERVAL_TIMELINE,
					configProperties.getInteger(ClientConfiguration.PROPERTY_INTERVAL_TIMELINE) / 1000);
		}
		configProperties.setProperty("cfg.version", "2");
	}
}

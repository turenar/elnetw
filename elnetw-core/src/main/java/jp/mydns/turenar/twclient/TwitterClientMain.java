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

package jp.mydns.turenar.twclient;

import java.awt.AWTException;
import java.awt.EventQueue;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.TreeSet;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.UIManager;

import jp.mydns.turenar.lib.parser.ArgParser;
import jp.mydns.turenar.lib.parser.ArgumentType;
import jp.mydns.turenar.lib.parser.ParsedArguments;
import jp.mydns.turenar.twclient.bus.MessageBus;
import jp.mydns.turenar.twclient.bus.factory.BlockingUsersChannelFactory;
import jp.mydns.turenar.twclient.bus.factory.DirectMessageChannelFactory;
import jp.mydns.turenar.twclient.bus.factory.FilterStreamChannelFactory;
import jp.mydns.turenar.twclient.bus.factory.FollowingUsersChannelFactory;
import jp.mydns.turenar.twclient.bus.factory.ListMembersChannelFactory;
import jp.mydns.turenar.twclient.bus.factory.ListTimelineChannelFactory;
import jp.mydns.turenar.twclient.bus.factory.MentionsChannelFactory;
import jp.mydns.turenar.twclient.bus.factory.NullMessageChannelFactory;
import jp.mydns.turenar.twclient.bus.factory.SearchChannelFactory;
import jp.mydns.turenar.twclient.bus.factory.TimelineChannelFactory;
import jp.mydns.turenar.twclient.bus.factory.TwitterStreamChannelFactory;
import jp.mydns.turenar.twclient.bus.factory.UserTimelineChannelFactory;
import jp.mydns.turenar.twclient.bus.factory.VirtualChannelFactory;
import jp.mydns.turenar.twclient.cache.ImageCacher;
import jp.mydns.turenar.twclient.conf.ClientProperties;
import jp.mydns.turenar.twclient.filter.ExtendedMuteFilter;
import jp.mydns.turenar.twclient.filter.GlobalUserIdFilter;
import jp.mydns.turenar.twclient.filter.delayed.BlockingUserFilter;
import jp.mydns.turenar.twclient.filter.query.QueryCompiler;
import jp.mydns.turenar.twclient.filter.query.func.StandardFunctionFactory;
import jp.mydns.turenar.twclient.filter.query.prop.StandardPropertyFactory;
import jp.mydns.turenar.twclient.gui.ShortcutKeyManager;
import jp.mydns.turenar.twclient.gui.config.ActionButtonConfigType;
import jp.mydns.turenar.twclient.gui.config.BooleanConfigType;
import jp.mydns.turenar.twclient.gui.config.ComboBoxConfigType;
import jp.mydns.turenar.twclient.gui.config.ConfigFrameBuilder;
import jp.mydns.turenar.twclient.gui.config.ConsumerTokenConfigType;
import jp.mydns.turenar.twclient.gui.config.IntegerConfigType;
import jp.mydns.turenar.twclient.gui.config.QueryEditConfigType;
import jp.mydns.turenar.twclient.gui.tab.ClientTab;
import jp.mydns.turenar.twclient.gui.tab.ClientTabFactory;
import jp.mydns.turenar.twclient.gui.tab.DirectMessageViewTab;
import jp.mydns.turenar.twclient.gui.tab.MentionViewTab;
import jp.mydns.turenar.twclient.gui.tab.TimelineViewTab;
import jp.mydns.turenar.twclient.gui.tab.factory.DirectMessageViewTabFactory;
import jp.mydns.turenar.twclient.gui.tab.factory.ListTimelineTabFactory;
import jp.mydns.turenar.twclient.gui.tab.factory.MentionViewTabFactory;
import jp.mydns.turenar.twclient.gui.tab.factory.SearchTabFactory;
import jp.mydns.turenar.twclient.gui.tab.factory.TimelineViewTabFactory;
import jp.mydns.turenar.twclient.gui.tab.factory.UpdateProfileTabFactory;
import jp.mydns.turenar.twclient.gui.tab.factory.UserInfoViewTabFactory;
import jp.mydns.turenar.twclient.init.InitBefore;
import jp.mydns.turenar.twclient.init.InitCondition;
import jp.mydns.turenar.twclient.init.InitDepends;
import jp.mydns.turenar.twclient.init.InitProvide;
import jp.mydns.turenar.twclient.init.InitializeException;
import jp.mydns.turenar.twclient.init.InitializeService;
import jp.mydns.turenar.twclient.init.Initializer;
import jp.mydns.turenar.twclient.init.InitializerInstance;
import jp.mydns.turenar.twclient.init.SplashScreenCtrl;
import jp.mydns.turenar.twclient.init.tree.TreeInitializeService;
import jp.mydns.turenar.twclient.intent.AccountVerifierIntent;
import jp.mydns.turenar.twclient.intent.AddClientTabIntent;
import jp.mydns.turenar.twclient.intent.ClearPostBoxIntent;
import jp.mydns.turenar.twclient.intent.DisplayRequirementIntent;
import jp.mydns.turenar.twclient.intent.DoNothingIntent;
import jp.mydns.turenar.twclient.intent.FavoriteIntent;
import jp.mydns.turenar.twclient.intent.HashtagIntent;
import jp.mydns.turenar.twclient.intent.IntentArguments;
import jp.mydns.turenar.twclient.intent.ListIntent;
import jp.mydns.turenar.twclient.intent.MenuPropertyEditorIntent;
import jp.mydns.turenar.twclient.intent.MenuQuitIntent;
import jp.mydns.turenar.twclient.intent.MuteIntent;
import jp.mydns.turenar.twclient.intent.OpenImageIntent;
import jp.mydns.turenar.twclient.intent.PostIntent;
import jp.mydns.turenar.twclient.intent.QuoteTweetIntent;
import jp.mydns.turenar.twclient.intent.ReloginIntent;
import jp.mydns.turenar.twclient.intent.RemoveTweetIntent;
import jp.mydns.turenar.twclient.intent.ReplyIntent;
import jp.mydns.turenar.twclient.intent.RetweetIntent;
import jp.mydns.turenar.twclient.intent.SearchIntent;
import jp.mydns.turenar.twclient.intent.TweetIntent;
import jp.mydns.turenar.twclient.intent.UnofficialRetweetIntent;
import jp.mydns.turenar.twclient.intent.UpdateProfileIntent;
import jp.mydns.turenar.twclient.intent.UrlIntent;
import jp.mydns.turenar.twclient.intent.UserInfoViewIntent;
import jp.mydns.turenar.twclient.internal.AsyncAppender;
import jp.mydns.turenar.twclient.internal.DeadlockMonitor;
import jp.mydns.turenar.twclient.internal.LoggingConfigurator;
import jp.mydns.turenar.twclient.internal.MenuConfiguratorIntent;
import jp.mydns.turenar.twclient.internal.ShutdownHook;
import jp.mydns.turenar.twclient.jni.LibnotifyMessageNotifier;
import jp.mydns.turenar.twclient.media.NullMediaResolver;
import jp.mydns.turenar.twclient.media.RegexpMediaResolver;
import jp.mydns.turenar.twclient.media.UrlResolverManager;
import jp.mydns.turenar.twclient.media.XpathMediaResolver;
import jp.mydns.turenar.twclient.notifier.NotifyHandler;
import jp.mydns.turenar.twclient.notifier.NotifySendMessageNotifier;
import jp.mydns.turenar.twclient.notifier.TrayIconMessageNotifier;
import jp.mydns.turenar.twclient.storage.CacheStorage;
import jp.mydns.turenar.twclient.storage.DirEntry;
import jp.mydns.turenar.twclient.storage.StorageEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.auth.AccessToken;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.WRITE;
import static jp.mydns.turenar.twclient.ClientConfiguration.PROPERTY_ACCOUNT_LIST;
import static jp.mydns.turenar.twclient.i18n.LocalizationResource.tr;

/**
 * 実際に起動するランチャ
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public final class TwitterClientMain {

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
	private static final int JOBWORKER_JOIN_TIMEOUT = 32;
	/**
	 * abi version for communicating between launcher and TwitterClient Main
	 */
	public static final int LAUNCHER_ABI_VERSION = 1;
	private static final Pattern shortcutKeyPattern = Pattern.compile("(.+)\\.(press|release)\\((.+)\\)");
	@InitializerInstance
	private static volatile TwitterClientMain singleton;

	/**
	 * assert singleton. THIS METHOD IS FOR LAUNCHER, DEVELOPERS MUST NOT USE THIS METHOD.
	 *
	 * @param args        arguments
	 * @param classLoader class loader
	 * @return singleton instance
	 */
	public static synchronized TwitterClientMain getInstance(String[] args, ClassLoader classLoader) {
		if (singleton != null) {
			throw new IllegalStateException("another instance always seems to be running");
		}
		singleton = new TwitterClientMain(args, classLoader);
		return singleton;
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
		if (singleton == null) {
			throw new IllegalStateException("no instance running!");
		}
		singleton.isInterrupted = true;
		singleton.mainThread.interrupt();
	}

	private final ClassLoader classLoader;
	/** for interruption */
	private final Thread mainThread;
	/** スレッドホルダ */
	private final Object threadHolder = new Object();
	private final ArgParser parser;
	private final ParsedArguments parsedArguments;
	/** 設定 */
	private ClientConfiguration configuration;
	/** 設定データ */
	private ClientProperties configProperties;
	private Logger logger;
	private JobQueue jobQueue;
	private boolean portable;
	private MessageBus messageBus;
	private TwitterClientFrame frame;
	private boolean isInterrupted;
	private DeadlockMonitor deadlockMonitor;
	private CacheStorage cacheStorage;
	private FileLock configFileLock;
	private FileLock cacheFileLock;

	/**
	 * インスタンスを生成する。
	 *
	 * @param args コマンドラインオプション
	 */
	private TwitterClientMain(String[] args, ClassLoader classLoader) {
		this.classLoader = classLoader;
		mainThread = Thread.currentThread();
		parser = new ArgParser()
				.description(tr("Usage: %s [OPTION...]", System.getProperty("elnetw.launcher.bin", "elnetw")));
		parser.addOption("-d", "--debug").argType(ArgumentType.NO_ARGUMENT)
				.description(tr("enable function for maintainer. This enables also --portable, "
						+ "--color-log, --log-level=TRACE"));
		parser.addOption("-L", "--classpath").argType(ArgumentType.REQUIRED_ARGUMENT).multiple(true).argName("PATH")
				.description(tr("add java classpath to execute\nPATH: directory or jar archive"));
		parser.addOption("-D", "--define").argType(ArgumentType.REQUIRED_ARGUMENT).multiple(true).argName("NAME[=VALUE]")
				.description(tr("define system property"));
		parser.addOption("-h", "--help").argType(ArgumentType.NO_ARGUMENT).description(tr("print this help and exit"));
		parser.addOption("-V", "--version").argType(ArgumentType.NO_ARGUMENT).description(tr("print version and exit"));
		parser.addOption("--no-splash").argType(ArgumentType.NO_ARGUMENT)
				.description(tr("disable splash screen. this is also implicitly enabled by passing --help or --version "
						+ "[launcher specific]"));
		LoggingConfigurator.setOpts(parser);
		parsedArguments = parser.parse(args);

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace(); // logger cannot use.
		}
	}

	/**
	 * init client tab factories
	 */
	@Initializer(name = "gui/tab/factory/std", phase = "preinit")
	@InitProvide("gui/tab/factory")
	public void addClientTabConstructor() {
		ClientConfiguration.putClientTabFactory("timeline", new TimelineViewTabFactory());
		ClientConfiguration.putClientTabFactory("mention", new MentionViewTabFactory());
		ClientConfiguration.putClientTabFactory("directmessage", new DirectMessageViewTabFactory());
		ClientConfiguration.putClientTabFactory("userinfo", new UserInfoViewTabFactory());
		ClientConfiguration.putClientTabFactory("update_profile", new UpdateProfileTabFactory());
		ClientConfiguration.putClientTabFactory("list", new ListTimelineTabFactory());
		ClientConfiguration.putClientTabFactory("search", new SearchTabFactory());
	}

	/**
	 * set configurator of filter
	 */
	@Initializer(name = "gui/config/filter", phase = "init")
	@InitDepends({"gui/main", "gui/config/builder"})
	public void addConfiguratorOfFilter() {
		configuration.getConfigBuilder().getGroup(tr("Filter"))
				.addConfig(ClientConfiguration.PROPERTY_BLOCKING_USER_MUTE_ENABLED,
						tr("Mute blocking users"), tr("Checking makes reading initial timeline delayed"),
						new BooleanConfigType())
				.addConfig("<ignore>", "Edit filter", "", new QueryEditConfigType());
	}

	/**
	 * clean log files
	 */
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
	 * clean old user cache.
	 */
	@Initializer(name = "clean/cache/user", phase = "poststart")
	@InitDepends({"cache/db", "cache/twitter"})
	public void cleanOldUserCache() {
		if (!cacheStorage.isDirEntry("/cache/user")) {
			return;
		}

		long expireTime = configProperties.getTime("core.cache.user.expire", TimeUnit.MILLISECONDS);
		DirEntry cacheDirEntry = cacheStorage.getDirEntry("/cache/user");
		List<DirEntry> removeList = cacheDirEntry.walk(2, 2)
				.map(StorageEntry::asDirEntry)
				.filter(dir -> dir != null && dir.exists("timestamp"))
				.filter(dir -> dir.readLong("timestamp") + expireTime < System.currentTimeMillis())
				.collect(Collectors.toList());
		removeList.stream()
				.peek(dir -> logger.debug("clean expired user cache: {}", dir.getPath()))
				.forEach(dir -> cacheDirEntry.rmdir(dir.getPath(), true));
	}

	/**
	 * ディスクキャッシュから期限切れのユーザーアイコンを削除する。
	 */
	@Initializer(name = "clean/cache/icon", phase = "poststart")
	@InitDepends({"cache/image", "config"})
	public void cleanOldUserIconCache() {
		Path userIconCacheDir = new File(configuration.getCacheDir(), "user").toPath();
		try {
			Files.walkFileTree(userIconCacheDir, new CacheCleanerVisitor());
		} catch (IOException e) {
			logger.warn("error while cleaning image cache");
		}
	}

	/**
	 * convert property separated by space into list
	 *
	 * @param propName prop name
	 */
	protected void convertOldPropArrayToList(String propName) {
		if (configProperties.containsKey(propName)) {
			String[] oldArray = configProperties.getProperty(propName).split(" ");
			configProperties.remove(propName); // avoid invalid list
			List<String> list = configProperties.getList(propName);
			Collections.addAll(list, oldArray);
		}
	}

	/**
	 * init cache manager
	 *
	 * @param condition init condition
	 */
	@Initializer(name = "cache/twitter", phase = "init")
	@InitDepends({"config", "accountId"})
	public void initCacheManager(InitCondition condition) {
		if (condition.isInitializingPhase()) {
			configuration.setCacheManager(new CacheManager(configuration));
		} else {
			configuration.getCacheManager().flush();
		}
	}

	/**
	 * init cache storage
	 *
	 * @param condition init condition
	 */
	@Initializer(name = "cache/db", phase = "preinit")
	@InitDepends("config")
	public void initCacheStorage(InitCondition condition) {
		if (condition.isInitializingPhase()) {
			try {
				Path lockPath = Paths.get(configuration.getCacheDir(), "cache.db.lock");
				FileChannel channel = FileChannel.open(lockPath, EnumSet.of(CREATE, WRITE));
				cacheFileLock = channel.tryLock();
				if (cacheFileLock == null) {
					JOptionPane.showMessageDialog(null,
							tr("Detected another instance with the same cache directory running. Abort!\n\n"
									+ "Check if other instances are running."),
							ClientConfiguration.APPLICATION_NAME, JOptionPane.ERROR_MESSAGE);
					condition.setFailStatus("MultiInstanceRunning", 1);
					return;
				}
			} catch (IOException e) {
				logger.warn("Lock failed", e);
			}

			Path dbPath = Paths.get(configuration.getCacheDir(), "cache.db");
			cacheStorage = new CacheStorage(dbPath);
			configuration.setCacheStorage(cacheStorage);
		} else {
			try {
				cacheStorage.store();
			} catch (IOException e) {
				logger.error("Error occurred while storing database", e);
			}
			if (cacheFileLock != null) {
				try {
					cacheFileLock.release();
				} catch (IOException e) {
					logger.warn("Unlock failed", e);
				}
			}
		}
	}

	/**
	 * init conf builder
	 */
	@Initializer(name = "gui/config/builder", phase = "preinit")
	public void initConfigBuilder() {
		configuration.setConfigBuilder(new ConfigFrameBuilder(configuration));
	}

	/**
	 * init configurator
	 */
	@Initializer(name = "gui/config/core", phase = "init")
	@InitDepends({"gui/main", "gui/config/builder"})
	public void initConfigurator() {
		ConfigFrameBuilder configBuilder = configuration.getConfigBuilder();
		configBuilder.getGroup("Twitter").getSubgroup(tr("Fetch interval (sec.)"))
				.addConfig(ClientConfiguration.PROPERTY_INTERVAL_TIMELINE, tr("Timeline"), tr("seconds"),
						new IntegerConfigType(60, 3600))
				.addConfig(ClientConfiguration.PROPERTY_INTERVAL_MENTIONS, tr("Mention"), tr("seconds"),
						new IntegerConfigType(60, 3600))
				.addConfig(ClientConfiguration.PROPERTY_INTERVAL_DIRECT_MESSAGES, tr("Direct message"), tr("seconds"),
						new IntegerConfigType(60, 10800))
				.getParentGroup().getSubgroup(tr("Fetch count (tweets)"))
				.addConfig(ClientConfiguration.PROPERTY_PAGING_TIMELINE, tr("Timeline"), tr("tweets"),
						new IntegerConfigType(1, 200))
				.addConfig(ClientConfiguration.PROPERTY_PAGING_MENTIONS, tr("Mention"), tr("tweets"),
						new IntegerConfigType(1, 200))
				.addConfig(ClientConfiguration.PROPERTY_PAGING_DIRECT_MESSAGES, tr("Direct message"), tr("message"),
						new IntegerConfigType(1, 200));
		configBuilder.getGroup(tr("UI"))
				.addConfig("gui.interval.list_update", tr("UI update interval (msec.)"), tr("milliseconds"),
						new IntegerConfigType(100, 5000))
				.addConfig("gui.list.scroll", tr("scroll amounts"), null, new IntegerConfigType(1, 100))
				.addConfig("gui.date.type", tr("date format"), null, new ComboBoxConfigType()
						.addItem(tr("sec."), "sec")
						.addItem(tr("msec."), "ms")
						.addItem(tr("sec. + relative"), "sec+rel")
						.addItem(tr("msec. + relative"), "ms+rel")
						.addItem(tr("relative"), "rel"));
		configBuilder.getGroup("core")
				.addConfig("core.info.survive_time", tr("life time of temporary message"), tr("seconds"),
						new IntegerConfigType(1, 60, 1000))
				.addConfig("core.match.id_strict_match", tr("strict ID match in detecting reply"),
						tr("if not checked, detect reply as screen name top matching"),
						new BooleanConfigType())
				.addConfig("core.filter.retweet_only_once", tr("The same tweets including retweeted appears only once"),
						tr("default is off"), new BooleanConfigType());
		configBuilder.getGroup("画像")
				.addConfig("gui.image.follow_sensitive", "不適切かもしれない画像を表示するときに確認する",
						"Webの設定とは同期しません", new BooleanConfigType());
		configBuilder.getGroup(tr("Advanced"))
				.addConfig(null, tr("Consumer key"), null, new ConsumerTokenConfigType())
				.addConfig(null, tr("Edit directly (not supported if you edit)"), null,
						new ActionButtonConfigType(tr("Open property editor..."), "menu_propeditor", frame));
	}

	/**
	 * init filter functions
	 */
	@Initializer(name = "filter/query/func/std", phase = "preinit")
	@InitProvide("filter/query/func")
	public void initFilterFunctions() {
		QueryCompiler.putFilterFunction("and", StandardFunctionFactory.SINGLETON);
		QueryCompiler.putFilterFunction("extract", StandardFunctionFactory.SINGLETON);
		QueryCompiler.putFilterFunction("if", StandardFunctionFactory.SINGLETON);
		QueryCompiler.putFilterFunction("inrt", StandardFunctionFactory.SINGLETON);
		QueryCompiler.putFilterFunction("not", StandardFunctionFactory.SINGLETON);
		QueryCompiler.putFilterFunction("exactly_one_of", StandardFunctionFactory.SINGLETON);
		QueryCompiler.putFilterFunction("one_of", StandardFunctionFactory.SINGLETON);
		QueryCompiler.putFilterFunction("or", StandardFunctionFactory.SINGLETON);

	}

	/**
	 * init filter properties
	 */
	@Initializer(name = "filter/query/prop/std", phase = "preinit")
	@InitProvide("filter/query/prop")
	public void initFilterProperties() {
		QueryCompiler.putFilterProperty("userid", StandardPropertyFactory.SINGLETON);
		QueryCompiler.putFilterProperty("user_id", StandardPropertyFactory.SINGLETON);
		QueryCompiler.putFilterProperty("in_reply_to", StandardPropertyFactory.SINGLETON);
		QueryCompiler.putFilterProperty("in_reply_to_userid", StandardPropertyFactory.SINGLETON);
		QueryCompiler.putFilterProperty("inreplyto", StandardPropertyFactory.SINGLETON);
		QueryCompiler.putFilterProperty("send_to", StandardPropertyFactory.SINGLETON);
		QueryCompiler.putFilterProperty("sendto", StandardPropertyFactory.SINGLETON);
		QueryCompiler.putFilterProperty("rtcount", StandardPropertyFactory.SINGLETON);
		QueryCompiler.putFilterProperty("rt_count", StandardPropertyFactory.SINGLETON);
		QueryCompiler.putFilterProperty("timediff", StandardPropertyFactory.SINGLETON);
		QueryCompiler.putFilterProperty("retweeted", StandardPropertyFactory.SINGLETON);
		QueryCompiler.putFilterProperty("mine", StandardPropertyFactory.SINGLETON);
		QueryCompiler.putFilterProperty("protected", StandardPropertyFactory.SINGLETON);
		QueryCompiler.putFilterProperty("is_protected", StandardPropertyFactory.SINGLETON);
		QueryCompiler.putFilterProperty("isprotected", StandardPropertyFactory.SINGLETON);
		QueryCompiler.putFilterProperty("verified", StandardPropertyFactory.SINGLETON);
		QueryCompiler.putFilterProperty("is_verified", StandardPropertyFactory.SINGLETON);
		QueryCompiler.putFilterProperty("isverified", StandardPropertyFactory.SINGLETON);
		QueryCompiler.putFilterProperty("status", StandardPropertyFactory.SINGLETON);
		QueryCompiler.putFilterProperty("is_status", StandardPropertyFactory.SINGLETON);
		QueryCompiler.putFilterProperty("isstatus", StandardPropertyFactory.SINGLETON);
		QueryCompiler.putFilterProperty("dm", StandardPropertyFactory.SINGLETON);
		QueryCompiler.putFilterProperty("directmessage", StandardPropertyFactory.SINGLETON);
		QueryCompiler.putFilterProperty("direct_message", StandardPropertyFactory.SINGLETON);
		QueryCompiler.putFilterProperty("is_dm", StandardPropertyFactory.SINGLETON);
		QueryCompiler.putFilterProperty("isdm", StandardPropertyFactory.SINGLETON);
		QueryCompiler.putFilterProperty("is_directmessage", StandardPropertyFactory.SINGLETON);
		QueryCompiler.putFilterProperty("is_direct_message", StandardPropertyFactory.SINGLETON);
		QueryCompiler.putFilterProperty("isdirectmessage", StandardPropertyFactory.SINGLETON);
		QueryCompiler.putFilterProperty("user", StandardPropertyFactory.SINGLETON);
		QueryCompiler.putFilterProperty("author", StandardPropertyFactory.SINGLETON);
		QueryCompiler.putFilterProperty("screen_name", StandardPropertyFactory.SINGLETON);
		QueryCompiler.putFilterProperty("screenname", StandardPropertyFactory.SINGLETON);
		QueryCompiler.putFilterProperty("text", StandardPropertyFactory.SINGLETON);
		QueryCompiler.putFilterProperty("client", StandardPropertyFactory.SINGLETON);
		QueryCompiler.putFilterProperty("in_list", StandardPropertyFactory.SINGLETON);
		QueryCompiler.putFilterProperty("has_hashtag", StandardPropertyFactory.SINGLETON);
		QueryCompiler.putFilterProperty("hashashtag", StandardPropertyFactory.SINGLETON);
		QueryCompiler.putFilterProperty("has_url", StandardPropertyFactory.SINGLETON);
		QueryCompiler.putFilterProperty("hasurl", StandardPropertyFactory.SINGLETON);
		QueryCompiler.putFilterProperty("is_following", StandardPropertyFactory.SINGLETON);
		QueryCompiler.putFilterProperty("isfollowing", StandardPropertyFactory.SINGLETON);
		QueryCompiler.putFilterProperty("following", StandardPropertyFactory.SINGLETON);
	}

	/**
	 * init frame
	 */
	@Initializer(name = "gui/main", phase = "init")
	@InitDepends({"cache/twitter", "accountId", "gui/shortcutKey"})
	public void initFrame() {
		frame = new TwitterClientFrame(configuration);
	}

	/**
	 * init image cacher
	 */
	@Initializer(name = "cache/image", phase = "init")
	@InitDepends("config")
	public void initImageCacher() {
		configuration.setImageCacher(new ImageCacher());
	}

	/**
	 * init action handlers
	 */
	@Initializer(name = "intent", phase = "init")
	@InitDepends("gui/main")
	public void initIntentTable() {
		configuration.addIntent("reply", new ReplyIntent());
		configuration.addIntent("qt", new QuoteTweetIntent());
		configuration.addIntent("unofficial_rt", new UnofficialRetweetIntent());
		configuration.addIntent("rt", new RetweetIntent());
		configuration.addIntent("fav", new FavoriteIntent());
		configuration.addIntent("remove", new RemoveTweetIntent());
		configuration.addIntent("userinfo", new UserInfoViewIntent());
		configuration.addIntent("url", new UrlIntent());
		configuration.addIntent("clear", new ClearPostBoxIntent());
		configuration.addIntent("post", new PostIntent());
		configuration.addIntent("mute", new MuteIntent());
		configuration.addIntent("tweet", new TweetIntent());
		configuration.addIntent("list", new ListIntent());
		configuration.addIntent("hashtag", new HashtagIntent());
		configuration.addIntent("search", new SearchIntent());
		configuration.addIntent("openimg", new OpenImageIntent());
		configuration.addIntent("blackhole", new DoNothingIntent());
		configuration.addIntent("tab_add", new AddClientTabIntent());
		configuration.addIntent("update_profile", new UpdateProfileIntent());
		configuration.addIntent("menu_quit", new MenuQuitIntent());
		configuration.addIntent("menu_propeditor", new MenuPropertyEditorIntent());
		configuration.addIntent("menu_account_verify", new AccountVerifierIntent());
		configuration.addIntent("menu_login_read", new ReloginIntent(false));
		configuration.addIntent("menu_login_write", new ReloginIntent(true));
		configuration.addIntent("menu_config", new MenuConfiguratorIntent());
		configuration.addIntent("dispReq", new DisplayRequirementIntent());
	}

	/**
	 * initialize notify handler
	 */
	@Initializer(name = "notify", phase = "prestart")
	@InitDepends({"bus", "gui/notifier"})
	@InitBefore("bus/init")
	public void initNotifyHandler(InitCondition condition) {
		if (condition.isInitializingPhase()) {
			NotifyHandler.register();
		} else if (!condition.isFastUninit()) {
			NotifyHandler.serialize(cacheStorage);
		}
	}

	/** ショートカットキーテーブルを初期化する。 */
	@Initializer(name = "gui/shortcutKey", phase = "init")
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
		ShortcutKeyManager.setDefaultMap("all");
		for (Object obj : shortcutkeyProperties.keySet()) {
			String key = (String) obj;
			Matcher matcher = shortcutKeyPattern.matcher(key);
			if (!matcher.matches()) {
				logger.error("Illegal shortcutKey: {}; ignoring", key);
			}
			String component = matcher.group(1);
			String keyEventType = matcher.group(2);
			String strokeString = matcher.group(3);
			KeyStroke keyStroke = Utility.toKeyStroke(strokeString, keyEventType.equals("release"));
			IntentArguments intent = Utility.getIntentArguments(shortcutkeyProperties.getProperty(key));
			ShortcutKeyManager.addShortcutKey(component, keyStroke, intent);
		}
	}

	/**
	 * init url provider
	 */
	@Initializer(name = "urlProvider", phase = "init")
	public void initUrlProviders() {
		UrlResolverManager.addMediaProvider("\\.(jpe?g|png|gif)", new NullMediaResolver(true));
		UrlResolverManager.addMediaProvider("^https?://twitpic\\.com/[a-zA-Z0-9]+",
				new RegexpMediaResolver("https?://.*?\\.cloudfront\\.net/photos/(?:large|full)/[\\w.]+"));
		UrlResolverManager.addMediaProvider("^http://p\\.twipple\\.jp/[a-zA-Z0-9]+",
				new XpathMediaResolver("id('post_image')/@src"));
	}

	/**
	 * send initialized message to message bus
	 */
	@Initializer(name = "bus/init", phase = "prestart")
	@InitDepends({"gui/tab/restore", "bus/factory"})
	public void realConnectMessageBus() {
		messageBus.onInitialized();
	}

	/**
	 * queue log flusher by notifying property changed
	 */
	@Initializer(name = "log/flush", phase = "earlyinit")
	@InitDepends({"timer", "config/default", "config"})
	public void registerLogFlusher() {
		configProperties.firePropertyUpdated(AsyncAppender.PROPERTY_FLUSH_INTERVAL, null, null);
	}

	/**
	 * restore tabs
	 *
	 * @param condition init condition
	 */
	@Initializer(name = "gui/tab/restore", phase = "prestart")
	@InitDepends({"config", "bus/factory", "filter/global", "gui/tab/factory", "gui/main"})
	public void restoreClientTabs(InitCondition condition) {
		List<String> tabsList = configProperties.getList("gui.tabs.list");
		if (condition.isInitializingPhase()) {
			if (tabsList.size() == 0) {
				configuration.addFrameTab(new TimelineViewTab("$reader"));
				configuration.addFrameTab(new MentionViewTab("$reader"));
				configuration.addFrameTab(new DirectMessageViewTab("$reader"));
			} else {
				for (String tabIdentifier : tabsList) {
					if (tabIdentifier.isEmpty()) {
						continue;
					}
					int separatorPosition = tabIdentifier.indexOf(':');
					String tabId = tabIdentifier.substring(0, separatorPosition);
					String uniqId = tabIdentifier.substring(separatorPosition + 1);
					ClientTabFactory tabConstructor = ClientConfiguration.getClientTabFactory(tabId);
					if (tabConstructor == null) {
						logger.warn("タブが復元できません: tabId={}, uniqId={}", tabId, uniqId);
					} else {
						ClientTab tab = tabConstructor.getInstance(tabId, uniqId);
						configuration.addFrameTab(tab);
					}
				}
			}
		} else {
			tabsList.clear();
			for (ClientTab tab : configuration.getFrameTabs()) {
				String tabId = tab.getTabId();
				String uniqId = tab.getUniqId();
				tabsList.add(tabId + ':' + uniqId);
				tab.serialize();
			}
		}
	}

	/**
	 * 起動する。
	 *
	 * @return 終了値
	 */
	public HashMap<String, Object> run() {
		portable = Boolean.getBoolean("config.portable");
		if (parsedArguments.hasOpt("--help")) {
			System.out.println(parser.generateHelpString());
			return getResultMap(0, false);
		} else if (parsedArguments.hasOpt("--version")) {
			System.out.println(tr("elnetw: version %1$s [%2$s:%3$s]\n"
							+ "Twitter Client for hitobasira\n\n"
							+ "License: %4$s\n"
							+ "Copyright: %5$s\n"
							+ "Support: %6$s\n",
					VersionInfo.getUniqueVersion(), VersionInfo.getMajorVersion(), VersionInfo.getCodeName(),
					VersionInfo.getLicense(), VersionInfo.getCopyright(), VersionInfo.getSupportUrl()));
			System.out.println(tr("Java VM: %s (%s)", System.getProperty("java.vm.name"),
					System.getProperty("java.vm.version")));
			return getResultMap(0, false);
		}
		if (parsedArguments.hasOpt("--debug")) {
			portable = true;
		}
		setHomeProperty();

		SplashScreenCtrl.setString(tr("Preparing for logger"));
		LoggingConfigurator.configureLogger(parsedArguments);
		logger = LoggerFactory.getLogger(TwitterClientMain.class);

		for (Iterator<String> errorMessages = parsedArguments.getErrorMessageIterator(); errorMessages.hasNext(); ) {
			logger.warn("ArgParser: {}", errorMessages.next());
		}

		SplashScreenCtrl.setString(tr("Starting initializer..."));
		configuration = ClientConfiguration.getInstance();
		configuration.setExtraClassLoader(classLoader);
		configuration.setArgParser(parser);
		configuration.setParsedArguments(parsedArguments);

		logger.info("elnetw version {}", VersionInfo.getDescribedVersion());

		InitializeService initializeService = TreeInitializeService.use();

		SplashScreenCtrl.setString(tr("Registering initializer..."));
		Runtime.getRuntime().addShutdownHook(new ShutdownHook());
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
			try {
				initializeService.uninit();
			} catch (InitializeException e1) {
				logger.error("failed uninit", e1);
			}
			return getResultMap(e.getExitCode(), true);
		}
		if (!initializeService.isInitialized("gui/main/show")) {
			logger.error("failed initialization. MainWindow is not shown");
			return getResultMap(1, true);
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

	/**
	 * set account id
	 */
	@Initializer(name = "accountId", phase = "earlyinit")
	@InitDepends({"config", "accesstoken"})
	public void setAccountId() {
		String defaultAccountId = configuration.getDefaultAccountId();
		configuration.setAccountIdForRead(defaultAccountId);
		configuration.setAccountIdForWrite(defaultAccountId);
	}

	/**
	 * set app name for GNOME3. If we don't do this, we get jp-mydns-turenar-launcher-TwitterClientLauncher for title.
	 * see GitHub issue#76
	 */
	@Initializer(name = "appName", phase = "preinit")
	public void setAppName() {
		// for Gnome3
		final Toolkit toolkit = Toolkit.getDefaultToolkit();
		final Class<?> cls = toolkit.getClass();

		try {
			// When X11 toolkit is used then set the awtAppClassName field
			if (cls.getName().equals("sun.awt.X11.XToolkit")) {
				final Field field = cls.getDeclaredField("awtAppClassName");
				field.setAccessible(true);
				field.set(toolkit, ClientConfiguration.getTitleForMainFrame());
			}
		} catch (Exception e) {
			// we prefer successfully initialize
			logger.warn("failed to set application name", e);
		}
	}

	@Initializer(name = "bus/errhandler")
	@InitDepends("bus")
	@InitBefore("bus/init")
	public void setBusOnExceptionHandler() {
		messageBus.establish(MessageBus.ALL_ACCOUNT_ID, "all", new ClientMessageAdapter() {
			@Override
			public void onException(Exception ex) {
				logger.warn("Exception thrown", ex);
			}
		});
	}

	/**
	 * set conf properties
	 *
	 * @param condition init condition
	 */
	@Initializer(name = "config", phase = "earlyinit")
	@InitDepends({"config/default", "internal/portableConfig"})
	public void setConfigProperties(InitCondition condition) {
		if (condition.isInitializingPhase()) {
			configProperties = configuration.getConfigProperties();

			try {
				Path lockPath = Paths.get(configuration.getConfigRootDir(), CONFIG_FILE_NAME + ".lock");
				FileChannel channel = FileChannel.open(lockPath, EnumSet.of(CREATE, WRITE));
				configFileLock = channel.tryLock();
				if (configFileLock == null) {
					JOptionPane.showMessageDialog(null, tr("Detected another instance with the same config running."
									+ " Abort!\n\nCheck if other instances are running."),
							ClientConfiguration.APPLICATION_NAME, JOptionPane.ERROR_MESSAGE);
					condition.setFailStatus("MultiInstanceRunning", 1);
					return;
				}
			} catch (IOException e) {
				logger.warn("Lock failed", e);
			}

			Path configPath = Paths.get(configuration.getConfigRootDir(), CONFIG_FILE_NAME);
			configProperties.setStorePath(configPath);
			if (Files.exists(configPath)) {
				logger.debug(CONFIG_FILE_NAME + " is found.");
				try (BufferedReader reader = Files.newBufferedReader(configPath, ClientConfiguration.UTF8_CHARSET)) {
					configProperties.load(reader);
				} catch (IOException e) {
					logger.warn("設定ファイルの読み込み中にエラー", e);
				}
			}
		} else {
			configProperties.store();
			if (configFileLock != null) {
				try {
					configFileLock.release();
				} catch (IOException e) {
					logger.warn("Unlock failed", e);
				}
			}
		}
	}

	/**
	 * set default conf properties
	 */
	@Initializer(name = "config/default", phase = "earlyinit")
	public void setDefaultConfigProperties() {
		ClientProperties defaultConfig = configuration.getConfigDefaultProperties();
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
	}

	/**
	 * set default exception handler
	 */
	@Initializer(name = "internal/exceptionHandler", phase = "earlyinit")
	public void setDefaultExceptionHandler() {
		Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
			logger.error("Uncaught exception", e);
			quit();
		});
	}

	/**
	 * set default global filter
	 */
	@Initializer(name = "filter/global/std/default", phase = "init")
	@InitProvide("filter/global")
	@InitDepends("config")
	public void setDefaultFilter() {
		configuration.addFilter(new GlobalUserIdFilter());
		configuration.addFilter(new ExtendedMuteFilter());
	}

	/**
	 * set default global filter
	 */
	@Initializer(name = "filter/global/std/bus", phase = "init")
	@InitProvide("filter/global")
	@InitDepends({"config", "bus/factory"})
	public void setDefaultFilterWithBus() {
		configuration.addFilter(new BlockingUserFilter(true));
	}

	/**
	 * set elnetw home and cache directory properties
	 */
	private void setHomeProperty() {
		String appHomeDir = System.getProperty("elnetw.home");
		String cacheDir = System.getProperty("elnetw.cache.dir");
		// do not use Utility: it initializes logger!
		if (System.getProperty("os.name").contains("Windows")) {
			appHomeDir = appHomeDir == null ? System.getenv("APPDATA") + "/elnetw" : appHomeDir;
			cacheDir = cacheDir == null ? System.getProperty("java.io.tmpdir") + "/elnetw/cache" : cacheDir;
		} else {
			appHomeDir = appHomeDir == null ? System.getProperty("user.home") + "/.elnetw" : appHomeDir;
			cacheDir = cacheDir == null ? System.getProperty("user.home") + "/.cache/elnetw" : cacheDir;
		}
		tryMkdir(appHomeDir);
		tryMkdir(cacheDir);
		System.setProperty("elnetw.home", appHomeDir);
		System.setProperty("elnetw.cache.dir", cacheDir);
	}

	/**
	 * notify initialize phase finished
	 */
	@Initializer(name = "internal/finish-initPhase", phase = "start")
	@InitDepends("gui/main/show")
	public void setInitializePhaseFinished() {
		configuration.setInitializing(false);
	}

	/**
	 * init message bus
	 *
	 * @param condition init condition
	 */
	@Initializer(name = "bus", phase = "preinit")
	@InitDepends({"queue", "config", "cache/db"})
	public void setMessageBus(InitCondition condition) {
		if (condition.isInitializingPhase()) {
			messageBus = new MessageBus();
			configuration.setMessageBus(messageBus);
		} else if (!condition.isFastUninit()) {
			messageBus.cleanUp();
		}
	}

	/**
	 * init message bus channel factories
	 */
	@Initializer(name = "bus/factory/std", phase = "init")
	@InitDepends({"bus", "cache/twitter"})
	@InitProvide("bus/factory")
	public void setMessageChannelFactory() {
		messageBus.addChannelFactory("my/timeline", new VirtualChannelFactory("stream/user", "statuses/timeline"));
		messageBus.addChannelFactory("stream/user", new TwitterStreamChannelFactory());
		messageBus.addChannelFactory("stream/filter", new FilterStreamChannelFactory());
		messageBus.addChannelFactory("statuses/timeline", new TimelineChannelFactory());
		messageBus.addChannelFactory("statuses/mentions", new MentionsChannelFactory());
		messageBus.addChannelFactory("direct_messages", new DirectMessageChannelFactory());
		messageBus.addChannelFactory("users/blocking", new BlockingUsersChannelFactory());
		messageBus.addChannelFactory("users/following", new FollowingUsersChannelFactory());
		messageBus.addChannelFactory("statuses/user_timeline", new UserTimelineChannelFactory());
		messageBus.addChannelFactory("lists/tweets", new ListTimelineChannelFactory());
		messageBus.addChannelFactory("lists/members", new ListMembersChannelFactory());
		messageBus.addChannelFactory("search", new SearchChannelFactory());
		messageBus.addChannelFactory("core", NullMessageChannelFactory.INSTANCE);
		messageBus.addChannelFactory("error", NullMessageChannelFactory.INSTANCE);
	}

	/**
	 * set message notifiers candidates
	 */
	@Initializer(name = "gui/notifier/std", phase = "init")
	@InitProvide("gui/notifier")
	public void setMessageNotifiersCandidate() {
		Utility.addMessageNotifier(2000, LibnotifyMessageNotifier::getInstance);
		Utility.addMessageNotifier(1000, NotifySendMessageNotifier::getInstance);
		Utility.addMessageNotifier(0, TrayIconMessageNotifier::getInstance);
	}

	/**
	 * set portable configuration property
	 */
	@Initializer(name = "internal/portableConfig", phase = "earlyinit")
	public void setPortabledConfigDir() {
		configuration.setPortabledConfiguration(portable);
	}

	/**
	 * init timer
	 *
	 * @param condition init condition
	 */
	@Initializer(name = "timer", phase = "earlyinit")
	public void setTimer(InitCondition condition) {
		if (condition.isInitializingPhase()) {
			configuration.setTimer(new ScheduledThreadPoolExecutor(1, r -> {
				return new Thread(r, "timer");
			}));
		} else {
			ScheduledExecutorService timer = configuration.getTimer();
			timer.shutdown();
			if (!condition.isFastUninit()) {
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
	}

	/**
	 * show tray icon
	 *
	 * @param condition init condition
	 */
	@Initializer(name = "gui/tray/show", phase = "start")
	@InitDepends({"gui/tray", "gui/main/show"})
	public void setTrayIcon(InitCondition condition) {
		if (condition.isInitializingPhase()) {
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

	/**
	 * init tray icon
	 */
	@Initializer(name = "gui/tray", phase = "prestart")
	public void setTrayIcon() {
		try {
			configuration.setTrayIcon(new TrayIcon(ImageIO.read(getClass().getClassLoader().getResourceAsStream(
					"jp/mydns/turenar/twclient/img/icon16.png")), ClientConfiguration.APPLICATION_NAME));
		} catch (IOException e) {
			logger.error("icon ファイルの読み込みに失敗。");
		}
	}

	/**
	 * show main window
	 *
	 * @param condition init condition
	 */
	@Initializer(name = "gui/main/show", phase = "start")
	@InitDepends("bus/start")
	public void showFrame(InitCondition condition) {
		if (condition.isInitializingPhase()) {
			java.awt.EventQueue.invokeLater(frame::start);
		} else {
			logger.info("Exiting elnetw...");
			try {
				EventQueue.invokeAndWait(() -> {
					frame.setVisible(false);
					frame.dispose();
				});
			} catch (InterruptedException e) {
				logger.error("interrupted while closing frame", e);
			} catch (InvocationTargetException e) {
				logger.error("Caught error", e.getCause());
			}
		}
	}

	/**
	 * start deadlock monitor
	 *
	 * @param condition init condition
	 */
	@Initializer(name = "queue/monitor/deadlock", phase = "earlyinit")
	@InitDepends({"timer", "queue"})
	public void startDeadlockMonitor(InitCondition condition) {
		if (condition.isInitializingPhase()) {
			deadlockMonitor = new DeadlockMonitor();
		} else if (condition.isFastUninit()) {
			deadlockMonitor.cancel();
		}
	}

	/**
	 * start job worker thread
	 *
	 * @param condition init condition
	 */
	@Initializer(name = "queue", phase = "earlyinit")
	@InitDepends("config")
	public void startJobWorkerThread(InitCondition condition) {
		if (condition.isInitializingPhase()) {
			jobQueue = configuration.getJobQueue();
			jobQueue.startWorker();
			InitializeService.getService().setJobQueue(jobQueue);
		} else {
			jobQueue.shutdown();
			while (!condition.isFastUninit()) {
				try {
					if (jobQueue.shutdownNow(JOBWORKER_JOIN_TIMEOUT)) {
						break;
					} else {
						// ImageIO caught interrupt but not set INTERRUPTED-STATUS
						// If it seemed to be occurred, retry to shutdown jobWorker
						jobQueue.shutdown();
					}
				} catch (InterruptedException e) {
					// continue;
				}
			}
		}
	}

	/**
	 * start message bus
	 */
	@Initializer(name = "bus/start", phase = "start")
	@InitDepends({"bus/init", "gui/main"})
	public void startMessageBus() {
		messageBus.onInitialized();
	}

	/**
	 * OAuthアクセストークンの取得を試す
	 *
	 * @param condition init condition
	 */
	@Initializer(name = "accesstoken", phase = "earlyinit")
	@InitDepends("config/update")
	public void tryGetOAuthAccessToken(InitCondition condition) {
		if (!condition.isInitializingPhase()) {
			return;
		}

		if (configuration.getAccountList().size() != 0) {
			return;
		}

		Twitter twitter;
		AccessToken accessToken;
		do {
			try {
				twitter = new OAuthHelper(configuration).show();
				if (twitter == null) {
					int button = JOptionPane.showConfirmDialog(null, tr("Exit?"), ClientConfiguration.APPLICATION_NAME,
							JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
					if (button == JOptionPane.YES_OPTION) {
						condition.setFailStatus("OAuth failed: canceled", -1);
						return;
					}
				} else {
					accessToken = twitter.getOAuthAccessToken();
					break;
				}
			} catch (TwitterException e) {
				condition.setFailStatus("error", 1);
				return;
			}
		} while (true);

		//将来の参照用に accessToken を永続化する
		String userId;
		try {
			userId = String.valueOf(twitter.getId());
		} catch (TwitterException e) {
			JOptionPane.showMessageDialog(null,
					tr("Failed to fetch user information. Try again later: %s", e.getLocalizedMessage()), tr("Error"),
					JOptionPane.ERROR_MESSAGE);
			throw new RuntimeException(e);
		}
		List<String> accountList = configProperties.getList(PROPERTY_ACCOUNT_LIST);
		accountList.add(userId);
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

	/**
	 * update configuration
	 *
	 * @param condition init condition
	 */
	@Initializer(name = "config/update", phase = "earlyinit")
	@InitDepends("config")
	@SuppressWarnings("fallthrough")
	public void updateConfig(InitCondition condition) {
		if (condition.isInitializingPhase()) {
			int version = Integer.parseInt(configProperties.getProperty("cfg.version", "0"));
			switch (version) {
				case 0:
					logger.info("Updating config to v1");
					//configProperties.setProperty("cfg.version", "1");
				case 1: // fall-through
					logger.info("Updating config to v2");
					if (configProperties.containsKey(ClientConfiguration.PROPERTY_INTERVAL_TIMELINE)) {
						configProperties.setInteger(ClientConfiguration.PROPERTY_INTERVAL_TIMELINE,
								configProperties.getInteger(ClientConfiguration.PROPERTY_INTERVAL_TIMELINE) / 1000);
					}
				case 2: // fall-through
					logger.info("Updating config to v3");
					configProperties.removePrefixed("gui.tabs");
					configProperties.setProperty("cfg.version", "3");
				case 3: // fall-through
					logger.info("Updating config to v4");
					convertOldPropArrayToList(PROPERTY_ACCOUNT_LIST);
					convertOldPropArrayToList("gui.tabs.list");
					configProperties.setProperty("cfg.version", "4");
				case 4: // fall-through
					logger.info("Updating config to v5");
				{
					List<String> toConvertKeys = configProperties.keySet()
							.stream()
							.filter(key -> key.startsWith("core.filter._tabs"))
							.collect(Collectors.toList());
					for (String key : toConvertKeys) {
						String query = configProperties.getProperty(key);
						String uniqId = key.substring("core.filter._tabs.".length());
						configProperties.setProperty("gui.tabs.data." + uniqId + ".filter.query", query);
						configProperties.remove(key);
					}
				}
				configProperties.setProperty("cfg.version", "5");
				case 5: // fall-through
					logger.info("Updating config to v6");
					convertOldPropArrayToList("core.filter.user.ids");
					configProperties.setProperty("cfg.version", "6");
				case 6: // latest
					break;
				default:
					int i = JOptionPane.showConfirmDialog(null,
							tr("You are running older elnetw than the version of config.\n"
									+ "It may cause unexpected bug or crash.\n"
									+ "Exit?"),
							ClientConfiguration.APPLICATION_NAME, JOptionPane.YES_NO_OPTION,
							JOptionPane.WARNING_MESSAGE);
					if (i == JOptionPane.YES_OPTION) {
						condition.setFailStatus("old version elnetw", 2);
					}
			}
		}
	}

	/**
	 * workaround for SSLv3 Poodle.
	 */
	@Initializer(name = "workaround/poodle", phase = "preinit")
	public void workaroundForPoodle() {
		System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2");
		System.setProperty("jdk.tls.client.protocols", "TLSv1,TLSv1.1,TLSv1.2");
	}
}

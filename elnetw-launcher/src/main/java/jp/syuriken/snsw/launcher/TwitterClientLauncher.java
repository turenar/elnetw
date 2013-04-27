package jp.syuriken.snsw.launcher;

import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.FilenameFilter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * TwitterClient のためのランチャ
 *
 * @author Turenar <snswinhaiku dot lo at gmail dot com>
 */
public class TwitterClientLauncher {

	private static FilenameFilter jarFilter = new FilenameFilter() {

		@Override
		public boolean accept(File dir, String name) {
			return name.endsWith(".jar");
		}
	};

	private static void addURL(Map<String, ClasspathEntry> urlMap, File file) {
		if (file.exists() == false) {
			System.err.println("[launcher] Specified classpath is not found: " + file.getPath());
			return;
		}

		if (file.isDirectory()) { // directory traversal
			File[] files = file.listFiles(jarFilter);
			for (File childFile : files) {
				addURL(urlMap, childFile);
			}
		} else {
			ClasspathEntry classpathEntry = new ClasspathEntry(file);
			if (urlMap.containsKey(classpathEntry.getLibraryName())) {
				urlMap.get(classpathEntry.getLibraryName()).update(classpathEntry);
			} else {
				urlMap.put(classpathEntry.getLibraryName(), classpathEntry);
			}
		}
	}

	/** 環境チェック */
	private static void checkEnvironment() {
		if (Charset.isSupported("UTF-8") == false) {
			throw new AssertionError("UTF-8 エンコードがサポートされていないようです。UTF-8 エンコードがサポートされていない環境では"
					+ "このソフトを動かすことはできません。Java VMの開発元に問い合わせてみてください。");
		}
		if (GraphicsEnvironment.isHeadless()) {
			throw new AssertionError("お使いのJava VMないし環境ではGUI出力がサポートされていないようです。GUIモードにするか、Java VMにGUIサポートを組み込んでください");
		}
	}

	/**
	 * Launch
	 *
	 * @param args アプリケーション引数
	 */
	public static void main(String[] args) {
		checkEnvironment();
		System.exit(new TwitterClientLauncher(args).run());
	}

	private final String[] args;

	private ArrayList<String> classpath = new ArrayList<String>();

	public TwitterClientLauncher(String[] args) {
		for (String arg : args) {
			if (arg.startsWith("-D")) {
				int indexOf = arg.indexOf('=');
				if (indexOf == -1) {
					System.clearProperty(arg.substring(2));
				} else {
					System.getProperties().setProperty(arg.substring(2, indexOf), arg.substring(indexOf + 1));
				}
			} else if (arg.startsWith("-L")) {
				classpath.add(arg.substring(2));
			}
		}
		this.args = args;
	}

	private URLClassLoader prepareClassLoader() {
		HashMap<String, ClasspathEntry> libMap = new HashMap<String, ClasspathEntry>();

		// Handle -L option
		for (String classpathEntry : classpath) {
			addURL(libMap, new File(classpathEntry));
		}

		// handle ~/.elnetw/lib
		addURL(libMap, new File(System.getProperty("user.home"), ".elnetw/lib"));

		ArrayList<URL> urlList = new ArrayList<URL>();
		for (ClasspathEntry entry : libMap.values()) {
			urlList.add(entry.getLibraryUrl());
		}
		URL[] urls = urlList.toArray(new URL[urlList.size()]);

		System.out.print("[launcher] classpath=");
		System.out.println(Arrays.toString(urls));

		URLClassLoader classLoader = new URLClassLoader(urls, TwitterClientLauncher.class.getClassLoader());
		return classLoader;
	}

	public int run() {
		URLClassLoader classLoader = prepareClassLoader();
		Class<?> clazz;
		try {
			clazz = Class.forName("jp.syuriken.snsw.twclient.TwitterClientMain", false, classLoader);
			Method getInstance = clazz.getMethod("getInstance", String[].class, ClassLoader.class);
			Object instance = getInstance.invoke(null, args, classLoader);
			Method method = clazz.getMethod("run");
			Integer retCode = (Integer) method.invoke(instance);
			return retCode;
		} catch (ClassNotFoundException e) {
			System.err.println("[launcher] 起動に必要なクラスの準備に失敗しました。");
			e.printStackTrace();
			return 16;
		} catch (InvocationTargetException e) {
			e.printStackTrace();
			return 1;
			// } catch (SecurityException e) {
			// } catch (NoSuchMethodException e) {
			// } catch (IllegalArgumentException e) {
			// } catch (InstantiationException e) {
			// } catch (IllegalAccessException e) {
		} catch (Exception e) {
			System.err.println("[launcher] 起動することができませんでした。");
			e.printStackTrace();
			return 16;
		}
	}
}

package jp.syuriken.snsw.launcher;

import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.FilenameFilter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

	private static void addURL(List<URL> urlList, File file, boolean onlyIncludeJarFile) {
		if (file.exists() == false) {
			System.err.println("[launcher] Specified classpath is not found: " + file.getPath());
			return;
		}

		if (onlyIncludeJarFile) {
			if (file.isDirectory()) { // directory traversal
				File[] files = file.listFiles(jarFilter);
				for (File childFile : files) {
					addURL(urlList, childFile, onlyIncludeJarFile);
				}
			} else {
				try {
					URL url = file.toURI().toURL();
					urlList.add(url);
				} catch (MalformedURLException e) {
					System.err.println("[launcher] Failed convert to URL");
					e.printStackTrace();
				}
			}
		} else {
			if (file.isDirectory()) {
				file = new File(file.getPath() + "/");
			}
			try {
				urlList.add(file.toURI().toURL());
			} catch (MalformedURLException e) {
				System.err.println("[launcher] Failed convert to URL");
				e.printStackTrace();
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
		ArrayList<URL> libList = new ArrayList<URL>();

		// Handle -L option
		for (String classpathEntry : classpath) {
			addURL(libList, new File(classpathEntry), true);
		}

		// handle ~/.elnetw/lib
		addURL(libList, new File(System.getProperty("user.home"), ".elnetw/lib"), true);

		URL[] urls = libList.toArray(new URL[libList.size()]);

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

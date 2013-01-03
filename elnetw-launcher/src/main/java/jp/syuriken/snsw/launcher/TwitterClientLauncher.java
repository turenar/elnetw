package jp.syuriken.snsw.launcher;

import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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

	private static void addURL(List<URL> urlList, Map<String, URL> fileMap, File file) {
		if (file.isDirectory() == false) {
			return;
		}
		try {
			file = file.getCanonicalFile();
		} catch (IOException e) {
			System.err.println("[launcher] Failed getting full-path:");
			e.printStackTrace();
		}
		if (fileMap.containsKey(file.getPath())) {
			System.out.println("skipped");
			return; //同じディレクトリを検索したときはスキップする
		}
		fileMap.put(file.getPath(), null); //フルパス=path.separatorが含まれているので衝突は考えない
		File[] files = file.listFiles(jarFilter);
		for (File childFile : files) {
			if (childFile.isFile()) {
				String jarName = childFile.getName();
				URL url = fileMap.get(jarName);
				if (url != null) {
					urlList.remove(url);
				}
				try {
					url = childFile.toURI().toURL();
					fileMap.put(jarName, url);
					urlList.add(url);
				} catch (MalformedURLException e) {
					System.err.println("[launcher] Failed convert to URL");
					e.printStackTrace();
				}
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
		String[] classpath = System.getProperty("java.class.path").split(System.getProperty("path.separator"));
		boolean skipCurrentDir = false;

		ArrayList<URL> libList = new ArrayList<URL>();
		HashMap<String, URL> fileMap = new HashMap<String, URL>();
		addURL(libList, fileMap, new File(System.getProperty("user.home"), ".elnetw/lib"));

		// Handle -L option
		for (String classpathEntry : classpath) {
			addURL(libList, fileMap, new File(classpathEntry));
		}

		URL[] urls = libList.toArray(new URL[libList.size()]);
		return new URLClassLoader(urls, TwitterClientLauncher.class.getClassLoader());
	}

	public int run() {
		URLClassLoader classLoader = prepareClassLoader();
		Class<?> clazz;
		try {
			clazz = Class.forName("jp.syuriken.snsw.twclient.TwitterClientMain", false, classLoader);
			Constructor<?> constructor = clazz.getConstructor(String[].class);
			Object instance = constructor.newInstance((Object) args);
			Method method = clazz.getMethod("run");
			Integer retCode = (Integer) method.invoke(instance);
			return retCode;
		} catch (ClassNotFoundException e) {
			System.err.println("[launcher] 起動に必要なクラスの準備に失敗しました。");
			e.printStackTrace();
			System.err.println("[launcher] ClassLoaderの検索先は次のとおりです。");
			System.err.println("[launcher] " + Arrays.toString(classLoader.getURLs()));
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

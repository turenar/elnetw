package jp.syuriken.snsw.launcher;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * TwitterClient のためのランチャ
 * 
 * @author $Author$
 */
public class TwitterClientLauncher {

	private static FilenameFilter jarFilter = new FilenameFilter() {

		@Override
		public boolean accept(File dir, String name) {
			return name.endsWith(".jar");
		}
	};

	private static void addURL(ArrayList<URL> urlList, File file) {
		if (file.isDirectory() == false) {
			return;
		}
		try {
			file = file.getCanonicalFile();
		} catch (IOException e) {
			System.err.println("[launcher] Failed getting full-path:");
			e.printStackTrace();
		}
		File[] files = file.listFiles(jarFilter);
		for (File childFile : files) {
			if (childFile.isFile()) {
				try {
					urlList.add(childFile.toURI().toURL());
				} catch (MalformedURLException e) {
					System.err.println("[launcher] Failed convert to URL");
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Launch
	 * 
	 * @param args
	 *            アプリケーション引数
	 */
	public static void main(String[] args) {
		URLClassLoader classLoader = prepareClassLoader();
		Class<?> clazz;
		try {
			clazz = Class.forName(
					"jp.syuriken.snsw.twclient.TwitterClientMain", false,
					classLoader);
			// TwitterClientMain twitterClientMain = new
			// TwitterClientMain(args);
			Constructor<?> constructor = clazz.getConstructor(String[].class);
			Object instance = constructor.newInstance((Object) args);
			// twitterClientMain.run();
			Method method = clazz.getMethod("run");
			method.invoke(instance);
		} catch (ClassNotFoundException e) {
			System.err.println("[launcher] 起動に必要なクラスの準備に失敗しました。");
			e.printStackTrace();
			System.err.println("[launcher] ClassLoaderの検索先は次のとおりです。");
			System.err.println("[launcher] "
					+ Arrays.toString(classLoader.getURLs()));
			System.exit(1);
			return;
		} catch (InvocationTargetException e) {
			e.printStackTrace();
			System.exit(16);
			// } catch (SecurityException e) {
			// } catch (NoSuchMethodException e) {
			// } catch (IllegalArgumentException e) {
			// } catch (InstantiationException e) {
			// } catch (IllegalAccessException e) {
		} catch (Exception e) {
			System.err.println("[launcher] 起動することができませんでした。");
			e.printStackTrace();
			System.exit(1);
			return;
		}
	}

	/**
	 * TODO snsoftware
	 * 
	 * @return
	 * 
	 */
	private static URLClassLoader prepareClassLoader() {
		String[] classpath = System.getProperty("java.class.path").split(
				System.getProperty("path.separator"));
		File baseDir;

		if (classpath.length == 1) { // run with "-jar" Option
			baseDir = new File(classpath[0]).getParentFile();
			if (baseDir == null) {
				baseDir = new File(".");
			}
		} else {
			baseDir = new File(".");
		}
		ArrayList<URL> libList = new ArrayList<URL>();
		addURL(libList, new File(baseDir, "lib"));
		addURL(libList,
				new File(System.getProperty("user.home", ".turetwcl/lib")));
		addURL(libList, new File("lib"));

		URL[] urls = libList.toArray(new URL[libList.size()]);
		return new URLClassLoader(urls,
				TwitterClientLauncher.class.getClassLoader());
	}
}

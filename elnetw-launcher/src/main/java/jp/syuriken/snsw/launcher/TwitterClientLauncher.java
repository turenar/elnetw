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
	
	/**
	 * Launch
	 * 
	 * @param args アプリケーション引数
	 */
	public static void main(String[] args) {
		URLClassLoader classLoader = prepareClassLoader();
		Class<?> clazz;
		try {
			clazz = Class.forName("jp.syuriken.snsw.twclient.TwitterClientMain", false, classLoader);
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
			System.err.println("[launcher] " + Arrays.toString(classLoader.getURLs()));
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
	
	private static URLClassLoader prepareClassLoader() {
		String[] classpath = System.getProperty("java.class.path").split(System.getProperty("path.separator"));
		File baseDir = null;
		boolean skipCurrentDir = false;
		
		if (classpath.length == 1) { // run with "-jar" Option
			try {
				baseDir = new File(classpath[0]).getCanonicalFile().getParentFile();
			} catch (IOException e) {
				// do nothing
			}
		}
		if (baseDir == null) {
			baseDir = new File(".");
			skipCurrentDir = true;
		}
		ArrayList<URL> libList = new ArrayList<URL>();
		HashMap<String, URL> fileMap = new HashMap<String, URL>();
		if (Boolean.getBoolean("config.portable") == false) {
			addURL(libList, fileMap, new File(baseDir, "../lib"));
		}
		addURL(libList, fileMap, new File(System.getProperty("user.home"), ".elnetw/lib"));
		if (skipCurrentDir == false) {
			addURL(libList, fileMap, new File("lib"));
		}
		
		URL[] urls = libList.toArray(new URL[libList.size()]);
		return new URLClassLoader(urls, TwitterClientLauncher.class.getClassLoader());
	}
}

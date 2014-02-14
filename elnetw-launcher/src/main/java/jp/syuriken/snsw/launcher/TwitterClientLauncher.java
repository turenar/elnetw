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
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 *  INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 *  IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 *  DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

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
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class TwitterClientLauncher {

	public static final String DEFAULT_LAUNCHING_CLASS = "jp.syuriken.snsw.twclient.TwitterClientMain";
	private static FilenameFilter jarFilter = new FilenameFilter() {

		@Override
		public boolean accept(File dir, String name) {
			return name.endsWith(".jar");
		}
	};

	private static void addURL(Map<String, ClasspathEntry> urlMap, File file) {
		if (!file.exists()) {
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

	@SuppressWarnings("unchecked")
	private static <T> T castTo(Object object) {
		return (T) object;
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
	private ArrayList<String> classpath = new ArrayList<>();

	@edu.umd.cs.findbugs.annotations.SuppressWarnings("EI_EXPOSE_REP2")
	private TwitterClientLauncher(String[] args) {
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

	private int getAbiVersion(Class<?> clazz) {
		Method abiVersionMethod;
		try {
			abiVersionMethod = clazz.getDeclaredMethod("getLauncherAbiVersion");
		} catch (NoSuchMethodException e) {
			return 0;
		}
		abiVersionMethod.setAccessible(true);
		try {
			return (Integer) abiVersionMethod.invoke(null);
		} catch (IllegalAccessException e) {
			System.err.println("[launcher] Access denied");
			e.printStackTrace();
			return -1;
		} catch (InvocationTargetException e) {
			System.err.println("[launcher] Failed getting MainClass ABI Version");
			e.printStackTrace();
			return -1;
		}
	}

	public Class<?> getMainClass(URLClassLoader classLoader) {
		String launchClass = System.getProperty("elnetw.launch.class", DEFAULT_LAUNCHING_CLASS);
		try {
			return Class.forName(launchClass, false, classLoader);
		} catch (ClassNotFoundException e) {
			System.err.println("[launcher] 起動に必要なクラスの準備に失敗しました。");
			e.printStackTrace();
			return null;
		}
	}

	private Object getMainClassInstance(Class<?> clazz, URLClassLoader classLoader) {
		Method getInstance;
		try {
			getInstance = clazz.getMethod("getInstance", String[].class, ClassLoader.class);
		} catch (NoSuchMethodException e) {
			System.err.println("[launcher] Failed instantiation main class");
			e.printStackTrace();
			return null;
		}

		try {
			return getInstance.invoke(null, args, classLoader);
		} catch (IllegalAccessException e) {
			System.err.println("[launcher] Access denied");
			e.printStackTrace();
			return null;
		} catch (InvocationTargetException e) {
			System.err.println("[launcher] Failed getting MainClass instance");
			e.printStackTrace();
			return null;
		}
	}

	private int invokeMainClass0(Class<?> clazz, URLClassLoader classLoader) {
		Object instance = getMainClassInstance(clazz, classLoader);
		if (instance == null) {
			return 1;
		}
		try {
			Method method = clazz.getMethod("run");
			return (Integer) method.invoke(instance);
		} catch (InvocationTargetException e) {
			e.printStackTrace();
			return 16;
		} catch (ReflectiveOperationException e) {
			System.err.println("[launcher] 起動することができませんでした。");
			e.printStackTrace();
			return 16;
		}
	}

	private int invokeMainClass1(Class<?> clazz, URLClassLoader classLoader) {
		Object instance = getMainClassInstance(clazz, classLoader);
		if (instance == null) {
			return 1;
		}
		try {
			Method method = clazz.getMethod("run");
			HashMap<String, Object> ret = castTo(method.invoke(instance));
			return (Integer) ret.get("exitCode");
		} catch (ReflectiveOperationException e) {
			System.err.println("[launcher] 起動することができませんでした。");
			e.printStackTrace();
			return 1;
		}
	}

	private URLClassLoader prepareClassLoader() {
		HashMap<String, ClasspathEntry> libMap = new HashMap<>();

		// Handle -L option
		for (String classpathEntry : classpath) {
			addURL(libMap, new File(classpathEntry));
		}

		ArrayList<URL> urlList = new ArrayList<>();
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
		Class<?> clazz = getMainClass(classLoader);
		if (clazz == null) {
			return 1;
		}

		// check ABI version
		int abiVersion = getAbiVersion(clazz);
		switch (abiVersion) {
			case -1:
				return 1;
			case 0:
				return invokeMainClass0(clazz, classLoader);
			case 1:
				return invokeMainClass1(clazz, classLoader);
			default:
				System.err.println("[launcher] Out-dated launcher!");
				return 1;
		}
	}
}

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

package jp.mydns.turenar.launcher;

import java.awt.GraphicsEnvironment;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import jp.mydns.turenar.lib.parser.ArgParser;
import jp.mydns.turenar.lib.parser.ArgumentType;
import jp.mydns.turenar.lib.parser.OptionInfo;
import jp.mydns.turenar.lib.parser.ParsedArguments;

/**
 * TwitterClient のためのランチャ
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class TwitterClientLauncher {

	public static final String DEFAULT_LAUNCHING_CLASS = "jp.mydns.turenar.twclient.TwitterClientMain";

	private static void addURL(Map<String, ClasspathEntry> urlMap, File file) {
		if (!file.exists()) {
			System.err.println("[launcher] Specified classpath is not found: " + file.getPath());
			return;
		}

		if (file.isDirectory()) { // directory traversal
			File[] files = file.listFiles((dir, name) -> name.endsWith(".jar"));
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
		if (!Charset.isSupported("UTF-8")) {
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
	private final boolean quietFlag;
	private ArrayList<String> classpath = new ArrayList<>();

	private TwitterClientLauncher(String[] args) {
		ArgParser parser = new ArgParser().setIgnoreUnknownOption(true);
		parser.addOption("-q", "--quiet").argType(ArgumentType.NO_ARGUMENT).group("quiet");
		parser.addOption("-L", "--classpath").argType(ArgumentType.REQUIRED_ARGUMENT).multiple(true);
		parser.addOption("-D", "--define").argType(ArgumentType.REQUIRED_ARGUMENT).multiple(true);
		parser.addOption("-h", "--help").argType(ArgumentType.NO_ARGUMENT).group("quiet");
		parser.addOption("-V", "--version").argType(ArgumentType.NO_ARGUMENT).group("quiet");


		ParsedArguments parsedArguments = parser.parse(args);
		quietFlag = parsedArguments.hasOptGroup("quiet");
		for (OptionInfo info : parsedArguments.getOptGroup("--define", true)) {
			String arg = info.getArg();
			if (arg == null || arg.isEmpty()) {
				System.err.println("missing argument for -D");
			} else {
				int indexOf = arg.indexOf('=');
				if (indexOf == -1) {
					System.clearProperty(arg);
				} else {
					System.getProperties().setProperty(arg.substring(0, indexOf), arg.substring(indexOf + 1));
				}
			}
		}
		for (OptionInfo info : parsedArguments.getOptGroup("--classpath", true)) {
			String arg = info.getArg();
			if (arg == null || arg.isEmpty()) {
				System.err.println("missing arugment for -L");
			} else {
				classpath.add(arg);
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

	private Class<?> getMainClass(URLClassLoader classLoader) {
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

		if (!quietFlag) {
			System.out.print("[launcher] classpath=");
			System.out.println(Arrays.toString(urls));
		}

		return new URLClassLoader(urls, TwitterClientLauncher.class.getClassLoader());
	}

	private int run() {
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

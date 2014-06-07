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

package jp.syuriken.snsw.twclient.jni;

import java.lang.reflect.InvocationTargetException;

import jp.syuriken.snsw.twclient.ClientConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Java-Gnome用のユーティリティ。Java-Gnomeがクラスパスになくても使用可
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class JavaGnome {
	private static final Logger logger = LoggerFactory.getLogger(JavaGnome.class);
	private static JavaGnome singleton;

	public static synchronized JavaGnome getInstance() {
		if (singleton == null) {
			singleton = new JavaGnome();
		}
		return singleton;
	}

	private final Class<?> versionClass;

	/**
	 * インスタンスの生成
	 */
	private JavaGnome() {
		ClassLoader extraClassLoader = ClientConfiguration.getInstance().getExtraClassLoader();

		Class<?> versionClass1;
		try {
			versionClass1 = Class.forName("org.freedesktop.bindings.Version", true, extraClassLoader);
		} catch (ClassNotFoundException e) {
			versionClass1 = null;
			// not found
		}
		versionClass = versionClass1;
	}

	/**
	 * get java-gnome api version
	 *
	 * @return api version
	 */
	public String getApiVersion() {
		if (versionClass == null) {
			return null;
		}
		try {
			return (String) versionClass.getMethod("getAPI").invoke(null);
		} catch (IllegalAccessException e) {
			logger.warn("#getApiVersion", e);
			return null;
		} catch (InvocationTargetException e) {
			logger.warn("#getApiVersion", e.getCause());
			return null;
		} catch (NoSuchMethodException e) {
			return null;
		}
	}

	/**
	 * get java-gnome version
	 *
	 * @return java-gnome version
	 */
	public String getVersion() {
		if (versionClass == null) {
			return null;
		}
		try {
			return (String) versionClass.getMethod("getVersion").invoke(null);
		} catch (IllegalAccessException e) {
			logger.warn("#getVersion", e);
			return null;
		} catch (InvocationTargetException e) {
			logger.warn("#getVersion", e.getCause());
			return null;
		} catch (NoSuchMethodException e) {
			return null;
		}
	}

	/**
	 * get if function using java-gnome should be disabled
	 *
	 * @return should be disabled
	 */
	public boolean isDisabled() {
		return Boolean.getBoolean("elnetw.java-gnome.disable");
	}

	/**
	 * get if java-gnome is loaded
	 *
	 * @return if java-gnome is loaded
	 */
	public boolean isFound() {
		return versionClass != null;
	}
}

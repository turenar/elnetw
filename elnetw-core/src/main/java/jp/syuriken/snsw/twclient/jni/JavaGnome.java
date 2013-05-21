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

	public static JavaGnome getInstance(ClientConfiguration configuration) {
		return new JavaGnome(configuration);
	}

	private final ClassLoader extraClassLoader;

	private final Class<?> versionClass;

	public JavaGnome(ClientConfiguration configuration) {
		extraClassLoader = configuration.getExtraClassLoader();

		Class<?> versionClass1;
		try {
			versionClass1 = Class.forName("org.freedesktop.bindings.Version", true, extraClassLoader);
		} catch (ClassNotFoundException e) {
			versionClass1 = null;
			// not found
		}
		versionClass = versionClass1;
	}

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

	public boolean isFound() {
		return versionClass != null;
	}


}

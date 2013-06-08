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

	private static JavaGnome INSTANCE;

	public static synchronized JavaGnome getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new JavaGnome();
		}
		return INSTANCE;
	}

	private final ClassLoader extraClassLoader;

	private final Class<?> versionClass;

	public JavaGnome() {
		extraClassLoader = ClientConfiguration.getInstance().getExtraClassLoader();

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

	public boolean isDisabled() {
		return Boolean.getBoolean("elnetw.java-gnome.disable");
	}

	public boolean isFound() {
		return versionClass != null;
	}
}

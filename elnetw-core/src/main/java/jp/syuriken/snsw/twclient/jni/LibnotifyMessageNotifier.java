package jp.syuriken.snsw.twclient.jni;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import jp.syuriken.snsw.twclient.ClientConfiguration;
import jp.syuriken.snsw.twclient.MessageNotifier;
import jp.syuriken.snsw.twclient.Utility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Notify System for Libnotify (with Java-GNOME)
 *
 * <p>This notifier will run only with packaged elnetw...</p>
 */
public class LibnotifyMessageNotifier implements MessageNotifier {
	private static final Logger logger = LoggerFactory.getLogger(LibnotifyMessageNotifier.class);

	public static final boolean checkUsable(ClientConfiguration configuration) {
		JavaGnome javaGnome = JavaGnome.getInstance();
		if (javaGnome.isDisabled()) {
			logger.info("Skip java-gnome notify system");
			return false;
		} else if (Utility.getOstype() == Utility.OSType.OTHER) {
			ClassLoader extraClassLoader = configuration.getExtraClassLoader();
			if (!javaGnome.isFound()) {
				return false;
			}

			try {
				logger.info("detected java-gnome. version:{}", javaGnome.getVersion());
				Class<?> gtkClass = Class.forName("org.gnome.gtk.Gtk", true, extraClassLoader);
				Boolean isGtkInitialized = (Boolean) gtkClass.getMethod("isInitialized").invoke(null);
				if (!isGtkInitialized) { // if(!Gtk.isInitialized){
					// Gtk.init(new String[]{});
					gtkClass.getMethod("init", String[].class).invoke(null, (Object) new String[]{});
				}
				Class<?> notifyClass = Class.forName("org.gnome.notify.Notify", true, extraClassLoader);
				Boolean isNotifyInitialized = (Boolean) notifyClass.getMethod("isInitialized").invoke(null);
				if (!isNotifyInitialized) { // if(!Notify.isInitialized){
					// Notify.init(ClientConfiguration.APPLICATION_NAME);
					notifyClass.getMethod("init", String.class).invoke(null, ClientConfiguration.APPLICATION_NAME);
				}

				// Object serverCapabilities = Notify.getServerCapabilities();
				Object serverCapabilities = notifyClass.getMethod("getServerCapabilities").invoke(null);
				logger.info("connected notification server. caps:{}", serverCapabilities);
				return true;
			} catch (ClassNotFoundException e) {
				logger.trace("java-gnome is partial found...", e);
			} catch (InvocationTargetException e) {
				logger.warn("#checkUsable", e.getCause());
			} catch (NoSuchMethodException e) {
				logger.warn("#checkUsable", e);
			} catch (IllegalAccessException e) {
				logger.warn("#checkUsable", e);
			}
		}
		return false;
	}

	private final Class<?> notificationClass;

	public LibnotifyMessageNotifier(ClientConfiguration configuration) {
		try {
			notificationClass = Class.forName("org.gnome.notify.Notification", true,
					configuration.getExtraClassLoader());
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void sendNotify(String summary, String text, File imageFile) throws IOException {
		// new Notification(summary, text, imageFile == null ? null : imageFile.getCanonicalPath())
		// .show();
		try {
			Constructor<?> constructor = notificationClass.getConstructor(String.class, String.class, String.class);
			String imageFilePath = imageFile == null ? null : imageFile.getCanonicalPath();
			Object notification = constructor.newInstance(summary, text, imageFilePath);

			notificationClass.getMethod("show").invoke(notification);
		} catch (InstantiationException e) {
			logger.error("#sendNotify", e);
		} catch (IllegalAccessException e) {
			logger.error("#sendNotify", e);
		} catch (InvocationTargetException e) {
			logger.error("#sendNotify", e.getCause());
		} catch (NoSuchMethodException e) {
			logger.error("#sendNotify", e);
		}
	}
}

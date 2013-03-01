package jp.syuriken.snsw.twclient.jni
		;

import java.io.File;
import java.io.IOException;

import jp.syuriken.snsw.twclient.ClientConfiguration;
import jp.syuriken.snsw.twclient.MessageNotifier;
import jp.syuriken.snsw.twclient.Utility;
import org.gnome.gtk.Gtk;
import org.gnome.notify.Notification;
import org.gnome.notify.Notify;
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
		if (Utility.getOstype() == Utility.OSType.OTHER) {
			if (configuration.getExtraClassLoader() != LibnotifyMessageNotifier.class.getClassLoader()) {
				logger.warn("MainClassLoader is different from ExtraClassLoader. " +
						"It will occur unexpected class-loading error");
			}

			try {
				Class<?> aClass = Class.forName("org.gnome.notify.Notify", true, configuration.getExtraClassLoader());
				logger.info("detected java-gnome. version:{}", org.freedesktop.bindings.Version.getVersion());
				if (Gtk.isInitialized() == false) {
					Gtk.init(new String[]{});
				}
				if (Notify.isInitialized() == false) {
					Notify.init(ClientConfiguration.APPLICATION_NAME);
				}
				logger.info("connected notification server. caps:{}", (Object) Notify.getServerCapabilities());
				return true;
			} catch (ClassNotFoundException e) {
				logger.trace("java-gnome is not found", e);
			}
		}
		return false;
	}

	public LibnotifyMessageNotifier(ClientConfiguration configuration) {
	}

	@Override
	public void sendNotify(String summary, String text, File imageFile) throws IOException {
		new Notification(summary, text, imageFile == null ? null : imageFile.getCanonicalPath()).show();
	}
}

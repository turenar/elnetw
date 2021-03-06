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

package jp.mydns.turenar.twclient.jni;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import jp.mydns.turenar.twclient.ClientConfiguration;
import jp.mydns.turenar.twclient.Utility;
import jp.mydns.turenar.twclient.notifier.MessageNotifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Notify System for Libnotify (with Java-GNOME)
 *
 * <p>This notifier will run only with packaged elnetw...</p>
 */
public class LibnotifyMessageNotifier implements MessageNotifier {
	private static final Logger logger = LoggerFactory.getLogger(LibnotifyMessageNotifier.class);

	/**
	 * check is usable notifier for this environment and get notifier instance
	 *
	 * @return if usable, return valid instance. if not, return null
	 */
	public static LibnotifyMessageNotifier getInstance() {
		JavaGnome javaGnome = JavaGnome.getInstance();
		if (javaGnome.isDisabled()) {
			logger.info("Skip java-gnome notify system");
			return null;
		} else if (Utility.getOstype() == Utility.OSType.OTHER) {
			ClassLoader extraClassLoader = ClientConfiguration.getInstance().getExtraClassLoader();
			if (!javaGnome.isFound()) {
				return null;
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
				return new LibnotifyMessageNotifier();
			} catch (ClassNotFoundException e) {
				logger.trace("java-gnome is partial found...", e);
			} catch (InvocationTargetException e) {
				logger.warn("#checkUsable", e.getCause());
			} catch (ReflectiveOperationException e) {
				logger.warn("#checkUsable", e);
			}
		}
		return null;
	}

	private final Class<?> notificationClass;

	/**
	 * インスタンスの生成
	 */
	private LibnotifyMessageNotifier() {
		try {
			notificationClass = Class.forName("org.gnome.notify.Notification", true,
					ClientConfiguration.getInstance().getExtraClassLoader());
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
		} catch (InstantiationException | IllegalAccessException | NoSuchMethodException e) {
			logger.error("#sendNotify", e);
		} catch (InvocationTargetException e) {
			logger.error("#sendNotify", e.getCause());
		}
	}
}

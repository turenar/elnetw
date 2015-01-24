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

package jp.mydns.turenar.twclient;

import java.awt.Color;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.PixelGrabber;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.ListIterator;

import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import jp.mydns.turenar.lib.primitive.LongHashSet;
import jp.mydns.turenar.twclient.intent.IntentArguments;
import jp.mydns.turenar.twclient.notifier.MessageNotifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.Status;
import twitter4j.UserMentionEntity;

/**
 * ユーティリティクラス。
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class Utility {

	private static class KVEntry {

		final String key;
		final String value;

		public KVEntry(String key, String value) {
			this.key = key;
			this.value = value;
		}
	}

	private static class MessageNotifierEntry {
		protected int priority;
		protected Class<? extends MessageNotifier> messageNotifierClass;

		protected MessageNotifierEntry(int priority, Class<? extends MessageNotifier> messageNotifierClass) {
			this.priority = priority;
			this.messageNotifierClass = messageNotifierClass;
		}
	}

	/**
	 * OSの種別を判断する。
	 *
	 * @author Turenar (snswinhaiku dot lo at gmail dot com)
	 */
	public enum OSType {
		/** Windows環境 */
		WINDOWS,
		/** MacOS環境 */
		MAC,
		/** その他 (*nixなど) */
		OTHER
	}

	/** 秒→ミリセカンド */
	public static final long SEC2MS = 1000;
	/** 分→ミリセカンド */
	public static final long MINUTE2MS = SEC2MS * 60;
	/** 時→ミリセカンド */
	public static final long HOUR2MS = MINUTE2MS * 60;
	/** 日→ミリセカンド */
	public static final long DAY2MS = HOUR2MS * 24;


	private static final Logger logger = LoggerFactory.getLogger(Utility.class);
	private static final LinkedList<MessageNotifierEntry> messageNotifiers = new LinkedList<>();
	/*package*/ static final String[] BROWSER_CANDIDATES = new String[]{
			"xdg-open",
			"firefox",
			"iron",
			"chromium",
			"chrome",
			"opera",
			"konqueror",
			"epiphany",
			"mozilla"
	};
	/**
	 * epoch offset for snowflake
	 */
	public static final long SNOWFLAKE_EPOCH_OFFSET = 1288834974657L;
	/**
	 * date bit shift for snowflake
	 */
	public static final int SNOWFLAKE_DATE_BITSHIFT = 22;
	private static volatile OSType ostype;
	private static KVEntry[] privacyEntries;

	/**
	 * Register MessageNotifier.
	 *
	 * <p>Elnetw select notifier which has higher priority and is usable.</p>
	 *
	 * @param priority             higher priority will be selected.
	 * @param messageNotifierClass Class object.
	 *                             messageNotifierClass must implement static method 'checkUsable(ClientConfiguration)'
	 *                             and constructor '&lt;init&gt;(ClientConfiguration)'
	 */
	public static void addMessageNotifier(int priority, Class<? extends MessageNotifier> messageNotifierClass) {
		try {
			messageNotifierClass.getMethod("checkUsable");
		} catch (NoSuchMethodException e) {
			throw new IllegalArgumentException(
					"messageNotifierClass must implement static method 'checkUsable()'", e);
		}
		try {
			messageNotifierClass.getConstructor();
		} catch (NoSuchMethodException e) {
			throw new IllegalArgumentException("messageNotifierClass must implement <init>()", e);
		}
		synchronized (messageNotifiers) {
			ListIterator<MessageNotifierEntry> listIterator = messageNotifiers.listIterator();
			while (true) {
				if (listIterator.hasNext()) {
					MessageNotifierEntry entry = listIterator.next();
					int entryPriority = entry.priority;
					// First element is the highest priority MessageNotifier.
					// If MessageNotifier which has same priority is already registered,
					//  <messageNotifier> will be put after it.
					if (entryPriority < priority) {
						listIterator.previous();
						listIterator.add(new MessageNotifierEntry(priority, messageNotifierClass));
						break;
					} else if (entryPriority == priority) {
						priority--;
					}
				} else {
					listIterator.add(new MessageNotifierEntry(priority, messageNotifierClass));
					break;
				}
			}
		}
	}

	/**
	 * sourceのalpha値を使用して色のアルファブレンドを行う。返されるalpha値はtargetを継承します。
	 *
	 * @param target 下の色
	 * @param source 上の色
	 * @return 合成済みColor
	 */

	public static Color blendColor(Color target, Color source) {
		double alpha = (double) source.getAlpha() / 255;
		int newr = (int) ((target.getRed() * (1.0 - alpha)) + (source.getRed() * alpha));
		int newg = (int) ((target.getGreen() * (1.0 - alpha)) + (source.getGreen() * alpha));
		int newb = (int) ((target.getBlue() * (1.0 - alpha)) + (source.getBlue() * alpha));
		return new Color(newr, newg, newb, target.getAlpha());
	}

	/**
	 * create buffered image from image
	 *
	 * @param image   original image
	 * @param tracker image tracker
	 * @return buffered image
	 * @throws InterruptedException interrupted
	 */
	public static BufferedImage createBufferedImage(Image image, MediaTracker tracker) throws InterruptedException {
		if (image instanceof BufferedImage) {
			return (BufferedImage) image;
		}

		tracker.addImage(image, 0);
		tracker.waitForAll();

		PixelGrabber pixelGrabber = new PixelGrabber(image, 0, 0, -1, -1, false);
		pixelGrabber.grabPixels();
		ColorModel cm = pixelGrabber.getColorModel();

		final int w = pixelGrabber.getWidth();
		final int h = pixelGrabber.getHeight();
		WritableRaster raster = cm.createCompatibleWritableRaster(w, h);
		BufferedImage renderedImage = new BufferedImage(cm, raster, cm.isAlphaPremultiplied(),
				new Hashtable<String, Object>());
		renderedImage.getRaster().setDataElements(0, 0, w, h, pixelGrabber.getPixels());
		return renderedImage;
	}

	/** OSを確定する */
	private static void detectOS() {
		String osName = System.getProperty("os.name");
		if (osName.startsWith("Mac OS")) {
			ostype = OSType.MAC;
		} else if (osName.startsWith("Windows")) {
			ostype = OSType.WINDOWS;
		} else {
			ostype = OSType.OTHER;
		}
	}

	private static char getCharFromLongName(String longName) {
		switch (longName) {
			case "%caret":
				return '^';
			case "%plus":
				return '+';
			case "%at":
				return '@';
			case "%equal":
				return '=';
			case "%colon":
				return ',';
			case "%space":
				return ' ';
			case "%bracketstart":
				return '[';
			case "%bracketend":
				return ']';
			case "%return":
				return KeyEvent.VK_ENTER;
			case "%up":
				return KeyEvent.VK_UP;
			case "%down":
				return KeyEvent.VK_DOWN;
			case "%right":
				return KeyEvent.VK_RIGHT;
			case "%left":
				return KeyEvent.VK_LEFT;
			case "%tab":
				return KeyEvent.VK_TAB;
			case "%pagedown":
				return KeyEvent.VK_PAGE_DOWN;
			case "%pageup":
				return KeyEvent.VK_PAGE_UP;
			case "%home":
				return KeyEvent.VK_HOME;
			case "%end":
				return KeyEvent.VK_END;
			case "%F1":
				return KeyEvent.VK_F1;
			case "%F2":
				return KeyEvent.VK_F2;
			case "%F3":
				return KeyEvent.VK_F3;
			case "%F4":
				return KeyEvent.VK_F4;
			case "%F5":
				return KeyEvent.VK_F5;
			case "%F6":
				return KeyEvent.VK_F6;
			case "%F7":
				return KeyEvent.VK_F7;
			case "%F8":
				return KeyEvent.VK_F8;
			case "%F9":
				return KeyEvent.VK_F9;
			case "%F10":
				return KeyEvent.VK_F10;
			case "%F11":
				return KeyEvent.VK_F11;
			case "%F12":
				return KeyEvent.VK_F12;
			default:
				if (longName.length() == 1) {
					return longName.charAt(0);
				} else {
					throw new IllegalArgumentException("Not supported long name: " + longName);
				}
		}
	}

	/**
	 * get intent argument from action command
	 *
	 * @param actionCommand {name} [! {arg} [=value] ; [ {arg} [=value] [; ...]]]
	 * @return intent argument
	 */
	public static IntentArguments getIntentArguments(String actionCommand) {
		int argSeparatorIndex = actionCommand.indexOf('!');
		IntentArguments intentArguments = new IntentArguments(
				argSeparatorIndex < 0 ? actionCommand : actionCommand.substring(0, argSeparatorIndex));


		String argsString = actionCommand.substring(argSeparatorIndex + 1);
		if (!argsString.isEmpty()) {
			String[] args = (argSeparatorIndex < 0 ? "" : argsString).split(";");
			for (String arg : args) {
				int kvSeparatorIndex = arg.indexOf('=');
				String name = kvSeparatorIndex < 0 ? "_arg" : arg.substring(0, kvSeparatorIndex);
				String value = kvSeparatorIndex < 0 ? arg : arg.substring(kvSeparatorIndex + 1);
				intentArguments.putExtra(name, value);
			}
		}
		return intentArguments;
	}


	static {
		privacyEntries = new KVEntry[]{
				new KVEntry(System.getProperty("elnetw.home"), "{DATA}/"),
				new KVEntry(System.getProperty("user.dir"), "{USER}/"),
				new KVEntry(System.getProperty("java.io.tmpdir"), "{TEMP}/"),
				new KVEntry(System.getProperty("user.home"), "{HOME}/"),
		};
	}

	/**
	 * OS種別を取得する
	 *
	 * @return OSの種類
	 */
	public static OSType getOstype() {
		if (ostype == null) {
			detectOS();
		}
		return ostype;
	}

	/**
	 * 与えられたステータスが普通のホームタイムラインに表示されるかどうかを返す
	 *
	 * @param status    ステータス
	 * @param following フォロー中のユーザー
	 * @return 普通のホームタイムライン
	 */
	public static boolean mayAppearInTimeline(Status status, LongHashSet following) {
		if (following.contains(status.getUser().getId())) {
			long inReplyToUserId = status.getInReplyToUserId();
			if (inReplyToUserId == -1) {
				return true; // not reply
			} else if (following.contains(inReplyToUserId)) {
				return true; // target is following
			} else {
				UserMentionEntity[] mentionEntities = status.getUserMentionEntities();
				for (UserMentionEntity entity : mentionEntities) {
					if (entity.getStart() == 0) {
						return false; // "@target text..."
					}
				}
				return true; // ".@target text..."
			}
		}
		return false;
	}

	/**
	 * ディレクトリの文字列置換えを行う。ホームディレクトリ等を隠す。
	 *
	 * @param string ディレクトリパス
	 * @return ディレクトリパス
	 */
	public static String protectPrivacy(String string) {
		return protectPrivacy(new StringBuilder(string)).toString();
	}

	/**
	 * ディレクトリの文字列置き換えを行う。ホームディレクトリ等を隠す
	 *
	 * @param builder ディレクトリパス。変更されます。
	 * @return builder自身。
	 */
	public static StringBuilder protectPrivacy(StringBuilder builder) {
		for (KVEntry entry : privacyEntries) {
			String before = entry.key;
			String after = entry.value;
			int offset = 0;
			int end;
			while (offset < builder.length()) {
				offset = builder.indexOf(before);
				if (offset == -1) {
					break;
				}
				end = offset + before.length();
				builder.replace(offset, end, after);
			}
		}
		return builder;
	}

	/**
	 * tear snowflake id into epoch time
	 *
	 * @param createdAtDate created at
	 * @param snowflakeId   snowflake id
	 * @return snowflake
	 */
	public static Date snowflakeIdToMilliSec(Date createdAtDate, long snowflakeId) {
		if (createdAtDate.getTime() < SNOWFLAKE_EPOCH_OFFSET) {
			return createdAtDate;
		} else {
			long date = (snowflakeId >> SNOWFLAKE_DATE_BITSHIFT) + SNOWFLAKE_EPOCH_OFFSET;
			return new Date(date);
		}
	}

	/**
	 * convert stroke string to key stroke
	 *
	 * @param strokeString stroke
	 * @param onKeyRelease released event?
	 * @return key stroke
	 */
	public static KeyStroke toKeyStroke(String strokeString, boolean onKeyRelease) {
		int index = 0;
		char modifierChar = strokeString.charAt(index);
		int modifiers = 0;
		if (modifierChar == '^') {
			modifiers |= KeyEvent.CTRL_DOWN_MASK;
			modifierChar = strokeString.charAt(++index);
		}
		if (modifierChar == '@') {
			modifiers |= KeyEvent.ALT_DOWN_MASK;
			modifierChar = strokeString.charAt(++index);
		}
		if (modifierChar == '+') {
			modifiers |= KeyEvent.SHIFT_DOWN_MASK;
			++index;
		}
		String keyLongName = strokeString.substring(index);
		char keyChar = getCharFromLongName(keyLongName);
		if (keyChar >= 'a' && keyChar <= 'z') {
			keyChar += 'A' - 'a'; // make Capital Character
		}
		return KeyStroke.getKeyStroke(keyChar, modifiers, onKeyRelease);
	}

	/**
	 * unchecked cast. IT IS DANGEROUS!!!
	 *
	 * @param value object
	 * @param <T>   type to cast
	 * @return casted object
	 */
	@SuppressWarnings("unchecked")
	public static <T> T uncheckedCast(Object value) {
		return (T) value;
	}

	private final ClientConfiguration configuration;
	private String detectedBrowser = null;
	/** 通知を送信するクラス */
	public volatile MessageNotifier notifySender = null;

	/**
	 * インスタンスを生成する。
	 *
	 * @param configuration 設定
	 */
	public Utility(ClientConfiguration configuration) {
		this.configuration = configuration;
	}

	/**
	 * インストールされているブラウザを確定する。
	 *
	 * @return ブラウザコマンド
	 */
	protected String detectBrowser() {
		if (detectedBrowser != null) {
			return detectedBrowser;
		}

		String detectedBrowser = configuration.getConfigProperties().getProperty("gui.browser.cmd");

		if (detectedBrowser == null) {
			for (String browser : BROWSER_CANDIDATES) {
				try {
					if (Runtime.getRuntime().exec(new String[]{
							"which",
							browser
					}).waitFor() == 0) {
						detectedBrowser = browser;
						break;
					}
				} catch (InterruptedException | IOException e2) {
					// do nothing
				}
			}
		}

		if (detectedBrowser == null) {
			detectedBrowser =
					JOptionPane.showInputDialog(null, "Please input path-to-browser.", "elnetw",
							JOptionPane.INFORMATION_MESSAGE);
			configuration.getConfigProperties().setProperty("gui.browser.cmd", detectedBrowser);
		}
		this.detectedBrowser = detectedBrowser;
		return detectedBrowser;
	}

	/** 通知を送信するクラスを設定する */
	private void detectNotifier() {
		if (notifySender == null) {
			synchronized (messageNotifiers) {
				for (MessageNotifierEntry entry : messageNotifiers) {
					Class<? extends MessageNotifier> messageNotifierClass = entry.messageNotifierClass;
					try {
						Method checkUsableMethod = messageNotifierClass.getMethod("checkUsable",
								ClientConfiguration.class);
						boolean usability = (Boolean) checkUsableMethod.invoke(null, configuration);
						if (usability) {
							Constructor<? extends MessageNotifier> constructor = messageNotifierClass.getConstructor(
									ClientConfiguration.class
							);
							MessageNotifier messageNotifier = constructor.newInstance(configuration);
							notifySender = messageNotifier;
							logger.info("use {} as MessageNotifier", messageNotifier);
							break;
						}
					} catch (NoSuchMethodException | InvocationTargetException
							| IllegalAccessException | InstantiationException e) {
						logger.warn("#detectNotifier", e);
					}
				}
			}
		}
	}

	/**
	 * browserを表示する。
	 *
	 * @param url 開くURL
	 * @throws java.awt.HeadlessException             GUIを使用できない
	 * @throws java.lang.ReflectiveOperationException 関数のinvokeに失敗 (Mac OS)
	 * @throws IllegalArgumentException               正しくない引数
	 * @throws IOException                            IOエラーが発生
	 * @throws SecurityException                      セキュリティ例外
	 */
	public void openBrowser(String url) throws IOException, ReflectiveOperationException {
		detectOS();
		switch (ostype) {
			case WINDOWS:
				Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url.trim());
				break;
			case MAC:
				Class<?> fileMgr = Class.forName("com.apple.eio.FileManager");

				Method openURL = fileMgr.getDeclaredMethod("openURL", String.class);
				openURL.invoke(null, url.trim());
				break;
			case OTHER:
				String browser = detectBrowser();
				Runtime.getRuntime().exec(new String[]{
						browser,
						url.trim()
				});
				break;
			default:
				break;
		}
	}

	/**
	 * 通知を送信する
	 *
	 * @param summary 概要
	 * @param text    テキスト
	 */
	public void sendNotify(String summary, String text) {
		sendNotify(summary, text, null);
	}

	/**
	 * 通知を送信する
	 *
	 * @param summary   概要
	 * @param text      テキスト
	 * @param imageFile アイコン
	 */
	public void sendNotify(String summary, String text, File imageFile) {
		if (configuration.isInitializing()) {
			return;
		}
		detectNotifier();
		if (notifySender != null) {
			try {
				notifySender.sendNotify(summary, text, imageFile);
			} catch (IOException e) {
				logger.warn("通知を送信できませんでした", e);
			}
		} else {
			logger.warn("有効なNotifierが見つかりません");
		}
	}
}

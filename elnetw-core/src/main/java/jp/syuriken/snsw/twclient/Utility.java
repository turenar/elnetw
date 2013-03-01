package jp.syuriken.snsw.twclient;

import java.awt.Color;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.ListIterator;

import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ユーティリティクラス。
 *
 * @author Turenar <snswinhaiku dot lo at gmail dot com>
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
	 * @author Turenar <snswinhaiku dot lo at gmail dot com>
	 */
	public enum OSType {
		/** Windows環境 */
		WINDOWS,
		/** MacOS環境 */
		MAC,
		/** その他 (*nixなど) */
		OTHER;
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

	private static final LinkedList<MessageNotifierEntry> messageNotifiers = new LinkedList<MessageNotifierEntry>();

	private static volatile OSType ostype;

	private static HashMap<Integer, String> keyMap = new HashMap<Integer, String>();

	private static KVEntry[] privacyEntries;

	static {
		keyMap.put(KeyEvent.VK_ENTER, "%return");
		keyMap.put(KeyEvent.VK_UP, "%up");
		keyMap.put(KeyEvent.VK_DOWN, "%down");
		keyMap.put(KeyEvent.VK_RIGHT, "%right");
		keyMap.put(KeyEvent.VK_LEFT, "%left");
		keyMap.put(KeyEvent.VK_CIRCUMFLEX, "%caret");
		keyMap.put(KeyEvent.VK_PLUS, "%plus");
		keyMap.put(KeyEvent.VK_AT, "%at");
		keyMap.put(KeyEvent.VK_TAB, "%tab");
		keyMap.put(KeyEvent.VK_EQUALS, "%equal");
		keyMap.put(KeyEvent.VK_COLON, "%colon");
		keyMap.put(KeyEvent.VK_SPACE, "%space");
		keyMap.put(KeyEvent.VK_PAGE_DOWN, "%pagedown");
		keyMap.put(KeyEvent.VK_PAGE_UP, "%pageup");
		keyMap.put(KeyEvent.VK_OPEN_BRACKET, "%bracketstart");
		keyMap.put(KeyEvent.VK_CLOSE_BRACKET, "%bracketend");
		keyMap.put(KeyEvent.VK_F1, "%F1");
		keyMap.put(KeyEvent.VK_F2, "%F2");
		keyMap.put(KeyEvent.VK_F3, "%F3");
		keyMap.put(KeyEvent.VK_F4, "%F4");
		keyMap.put(KeyEvent.VK_F5, "%F5");
		keyMap.put(KeyEvent.VK_F6, "%F6");
		keyMap.put(KeyEvent.VK_F7, "%F7");
		keyMap.put(KeyEvent.VK_F8, "%F8");
		keyMap.put(KeyEvent.VK_F9, "%F9");
		keyMap.put(KeyEvent.VK_F10, "%F10");
		keyMap.put(KeyEvent.VK_F11, "%F11");
		keyMap.put(KeyEvent.VK_F12, "%F12");

		privacyEntries = new KVEntry[] {
			new KVEntry(System.getProperty("user.dir"), "{USER}/"),
			new KVEntry(System.getProperty("java.io.tmpdir"), "{TEMP}/"),
			new KVEntry(System.getProperty("user.home"), "{HOME}/"),
		};
	}

	/** DateFormatを管理する */
	private static ThreadLocal<SimpleDateFormat> dateFormat = new ThreadLocal<SimpleDateFormat>() {

		@Override
		protected SimpleDateFormat initialValue() {
			return new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		}
	};

	/**
	 * Register MessageNotifier.
	 *
	 * <p>Elnetw select notifier which has higher priority and is usable.</p>
	 *
	 * <p>TODO: use Annotation</p>
	 * @param priority higher priority will be selected.
	 * @param messageNotifierClass Class object.
	 *                             messageNotifierClass must implement static method 'checkUsable(ClientConfiguration)'
	 *                             and constructor '&lt;init&gt;(ClientConfiguration)'
	 */
	public static void addMessageNotifier(int priority, Class<? extends MessageNotifier> messageNotifierClass) {
		try {
			messageNotifierClass.getMethod("checkUsable", ClientConfiguration.class);
		} catch (NoSuchMethodException e) {
			throw new IllegalArgumentException(
					"messageNotifierClass must implement static method 'checkUsable(ClientConfiguration'", e);
		}
		try {
			messageNotifierClass.getConstructor(
					ClientConfiguration.class);
		} catch (NoSuchMethodException e) {
			throw new IllegalArgumentException("messageNotifierClass must implement <init>(ClientConfiguration)", e);
		}
		synchronized (messageNotifiers) {
			ListIterator<MessageNotifierEntry> listIterator = messageNotifiers.listIterator();
			while (true) {
				if (listIterator.hasNext()) {
					MessageNotifierEntry entry = listIterator.next();
					int entryPriority = entry.priority;
					// First element is the highest priority MessageNotifier.
					// If MessageNotifier which has same priority is already regeistered,
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
	 * @param target 下の色
	 * @param source 上の色
	 * @return 合成済みColor
	 */

	public static Color blendColor(Color target, Color source) {
		double alpha = (double) source.getAlpha() / 255;
		int newr = (int) ((target.getRed() * (1.0 - alpha)) + (source.getRed() * alpha));
		int newg = (int) ((target.getGreen() * (1.0 - alpha)) + (source.getGreen() * alpha));
		int newb = (int) ((target.getBlue() * (1.0 - alpha)) + (source.getBlue() * alpha));
		Color color = new Color(newr, newg, newb, target.getAlpha());
		return color;
	}

	/**
	 * OSを確定する
	 */
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

	/**
	 * 文字列が等しいかどうかを調べる。単にequalsとするよりも速くなるかもしれないぐらいの程度
	 *
	 * @param a 文字列A
	 * @param b 文字列B
	 * @return 等しいかどうか
	 */
	public static boolean equalString(String a, String b) {
		if (a == null && b == null) {
			return true;
		}
		if (a == null || b == null) {
			return false;
		}
		if (a.hashCode() != b.hashCode()) {
			return false;
		}
		return a.equals(b);
	}

	/**
	 * yyyy/MM/dd HH:mm:ssな {@link SimpleDateFormat} を取得する。
	 *
	 * このメソッドはマルチスレッド対応ですが、このメソッドで返されるインスタンスは<strong>スレッドローカルなので
	 * 複数スレッドで使いまわさないでください</strong>。
	 *
	 * @return 日付フォーマッタ
	 */
	public static SimpleDateFormat getDateFormat() {
		return dateFormat.get();
	}

	/**
	 * 日時をあらわす文字列を短い形式を含めて取得する。
	 *
	 * <p>
	 * このメソッドで返す文字列は&quot;(\d{2}[smh])?(&lt;small&gt;)? \(yyyy/MM/dd HH:mm:ss\)(&lt;/small&gt;)?&quot;です
	 * </p>
	 * @param date 日時
	 * @param html HTMLタグを使用するかどうか。trueの場合、<strong>返す文字列には&lt;html&gt;がつきます</strong>。
	 * @return 日時を表す文字列
	 */
	public static String getDateString(Date date, boolean html) {
		long timeDiff = System.currentTimeMillis() - date.getTime();
		String dateFormatted = dateFormat.get().format(date);
		if (timeDiff < 0 || timeDiff > DAY2MS) {
			// date is fortune or older than 24hours
			return dateFormatted;
		} else {
			StringBuilder stringBuilder = new StringBuilder();
			if (html) {
				stringBuilder.append("<html>");
			}

			if (timeDiff < MINUTE2MS) {
				stringBuilder.append(timeDiff / SEC2MS).append("秒前");
			} else if (timeDiff < HOUR2MS) {
				stringBuilder.append(timeDiff / MINUTE2MS).append("分前");
			} else {
				stringBuilder.append(timeDiff / HOUR2MS).append("時間前");
			}

			stringBuilder.append(" (").append(dateFormatted).append(')');
			return stringBuilder.toString();
		}
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
	 * 単にobjを配列として返すだけの
	 *
	 * @param obj オブジェクト
	 * @return 配列
	 */
	public static Object[] toArray(Object... obj) {
		return obj;
	}

	/**
	 * キーをキー文字列に変換する
	 *
	 * @param code キー
	 * @param modifiers キー修飾。 {@link InputEvent#CTRL_DOWN_MASK}等
	 * @param isReleased keyReleased等のイベントでコールされたかどうか
	 * @return キー文字列
	 */
	public static String toKeyString(int code, int modifiers, boolean isReleased) {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(isReleased ? "release(" : "press(");
		if ((modifiers & InputEvent.CTRL_DOWN_MASK) != 0) {
			stringBuilder.append('^');
		}
		if ((modifiers & InputEvent.ALT_DOWN_MASK) != 0) {
			stringBuilder.append('@');
		}
		if ((modifiers & InputEvent.SHIFT_DOWN_MASK) != 0) {
			stringBuilder.append('+');
		}
		String key = keyMap.get(code);
		stringBuilder.append(key != null ? key : (char) code);
		stringBuilder.append(')');
		return stringBuilder.toString();
	}

	/**
	 * キーをキー文字列に変換する
	 *
	 * @param e キーイベント
	 * @return キー文字列
	 */
	public static String toKeyString(KeyEvent e) {
		if (e.getID() == KeyEvent.KEY_TYPED) {
			throw new IllegalArgumentException("KeyEvent.getID() must not be KEY_TYPED");
		}
		return toKeyString(e.getKeyCode(), e.getModifiersEx(), e.getID() == KeyEvent.KEY_RELEASED);
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
		String[] browsers = {
			"xdg-open",
			"firefox",
			"chrome",
			"opera",
			"konqueror",
			"epiphany",
			"mozilla",
		};
		String detectedBrowser = null;

		for (String browser : browsers) {
			try {
				if (Runtime.getRuntime().exec(new String[] {
					"which",
					browser
				}).waitFor() == 0) {
					detectedBrowser = browser;
					break;
				}
			} catch (InterruptedException e2) {
				// do nothing
			} catch (IOException e) {
				// do nothing
			}
		}

		if (detectedBrowser == null) {
			detectedBrowser =
					JOptionPane.showInputDialog(null, "Please input path-to-browser.", "elnetw",
							JOptionPane.INFORMATION_MESSAGE);
		}
		return detectedBrowser;
	}

	/**
	 * 通知を送信するクラスを設定する
	 */
	private void detectNotifier() {
		if (notifySender == null) {
			synchronized (messageNotifiers) {
				for (MessageNotifierEntry entry : messageNotifiers) {
					Class<? extends MessageNotifier> messageNotifierClass = entry.messageNotifierClass;
					try {
						Method checkUsableMethod = messageNotifierClass.getMethod("checkUsable", ClientConfiguration.class);
						boolean usability = (Boolean) checkUsableMethod.invoke(null, configuration);
						if (usability) {
							Constructor<? extends MessageNotifier> constructor = messageNotifierClass.getConstructor(
									ClientConfiguration.class
							);
							MessageNotifier messageNotifier = constructor.newInstance(configuration);
							notifySender = messageNotifier;
							logger.info("use {} as MessageNotifier", notifySender);
							break;
						}
					} catch (NoSuchMethodException e) {
						logger.warn("#detectNotifier", e);
					} catch (InvocationTargetException e) {
						logger.warn("#detectNotifier", e);
					} catch (IllegalAccessException e) {
						logger.warn("#detectNotifier", e);
					} catch (InstantiationException e) {
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
	 * @return
	 * 		<dl>
	 * 		<dt>HeadlessException</dt><dd>GUIを使用できない</dd>
	 * 		<dt>InvocationTargetException</dt><dd>関数のinvokeに失敗 (Mac OS)</dd>
	 * 		<dt>IllegalAccessException</dt><dd>アクセスに失敗</dd>
	 * 		<dt>IllegalArgumentException</dt><dd>正しくない引数</dd>
	 * 		<dt>IOException</dt><dd>IOエラーが発生</dd>
	 * 		<dt>NoSuchMethodException</dt><dd>関数のinvokeに失敗 (Mac OS)</dd>
	 * 		<dt>SecurityException</dt><dd>セキュリティ例外</dd>
	 * 		<dt>ClassNotFoundException</dt><dd>クラスのinvokeに失敗 (Mac OS)</dd>
	 * 		</dl>
	 */
	public Throwable openBrowser(String url) {
		detectOS();
		try {
			switch (ostype) {
				case WINDOWS:
					Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url.trim());
					break;
				case MAC:
					Class<?> fileMgr = null;
					fileMgr = Class.forName("com.apple.eio.FileManager");

					Method openURL = fileMgr.getDeclaredMethod("openURL", new Class[] {
						String.class
					});
					openURL.invoke(null, new Object[] {
						url.trim()
					});
					break;
				case OTHER:
					String browser = detectBrowser();
					Runtime.getRuntime().exec(new String[] {
						browser,
						url.trim()
					});
					break;
				default:
					break;
			}
			return null;
		} catch (Throwable ex) {
			logger.warn("Failed opening browser", ex);
			return ex;
		}
	}

	/**
	 * 通知を送信する
	 * @param summary 概要
	 * @param text テキスト
	 */
	public void sendNotify(String summary, String text) {
		sendNotify(summary, text, null);
	}

	/**
	 * 通知を送信する
	 * @param summary 概要
	 * @param text テキスト
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

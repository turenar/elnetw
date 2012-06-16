package jp.syuriken.snsw.twclient;

import java.awt.Color;
import java.awt.HeadlessException;
import java.awt.TrayIcon;
import java.awt.TrayIcon.MessageType;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.TimerTask;

import javax.swing.JOptionPane;

import jp.syuriken.snsw.twclient.JobQueue.Priority;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ユーティリティクラス。
 * 
 * @author $Author$
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
	
	/**
	 * notify-sendを使用して通知を送信するクラス。
	 * 
	 * @author $Author$
	 */
	public static class LibnotifySender implements NotifySender {
		
		@Override
		public void sendNotify(String summary, String text, File imageFile) throws IOException {
			if (imageFile == null) {
				Runtime.getRuntime().exec(new String[] {
					"notify-send",
					summary,
					text
				});
			} else {
				Runtime.getRuntime().exec(new String[] {
					"notify-send",
					"-i",
					imageFile.getPath(),
					summary,
					text
				});
			}
		}
		
	}
	
	/**
	 * 通知が送信されるクラスのインターフェース
	 * 
	 * @author $Author$
	 */
	protected interface NotifySender {
		
		/**
		 * 通知を送信する
		 * @param summary 概要
		 * @param text テキスト
		 * @param imageFile アイコン。ない場合はnull
		 * @throws IOException 外部プロセスの起動に失敗
		 */
		public void sendNotify(String summary, String text, File imageFile) throws IOException;
	}
	
	/**
	 * OSの種別を判断する。
	 * 
	 * @author $Author$
	 */
	public enum OSType {
		/** Windows環境 */
		WINDOWS,
		/** MacOS環境 */
		MAC,
		/** その他 (*nixなど) */
		OTHER;
	}
	
	/**
	 * TrayIconを使用して通知する。
	 * 
	 * @author $Author$
	 */
	public static class TrayIconNotifySender implements NotifySender, ParallelRunnable {
		
		private TrayIcon trayIcon;
		
		private LinkedList<Object[]> queue = new LinkedList<Object[]>();
		
		private final ClientConfiguration configuration;
		
		private long lastNotified;
		
		
		/**
		 * インスタンスを生成する。
		 * 
		 * @param configuration 設定
		 */
		public TrayIconNotifySender(ClientConfiguration configuration) {
			this.configuration = configuration;
			trayIcon = configuration.getTrayIcon();
		}
		
		@Override
		public void run() {
			synchronized (queue) {
				long tempTime = lastNotified + 5000; //TODO 5000 from configure
				if (tempTime > System.currentTimeMillis()) {
					
					configuration.getFrameApi().getTimer().schedule(new TimerTask() {
						
						@Override
						public void run() {
							TrayIconNotifySender.this.run();
						}
					}, tempTime - System.currentTimeMillis());
					return;
				}
				Object[] arr = queue.poll();
				if (arr == null) {
					return;
				}
				String summary = (String) arr[0];
				String text = (String) arr[1];
				trayIcon.displayMessage(summary, text, MessageType.INFO);
				lastNotified = System.currentTimeMillis();
				if (queue.size() > 0) {
					configuration.getFrameApi().addJob(Priority.LOW, this);
				}
			}
		}
		
		@Override
		public void sendNotify(String summary, String text, File imageFile) {
			synchronized (queue) {
				queue.add(new Object[] {
					summary,
					text
				/*,imageFile*/});
				if (queue.size() == 1) {
					configuration.getFrameApi().addJob(Priority.LOW, this);
				}
			}
		}
	}
	
	
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
	
	/*package*/static final ThreadLocal<StringBuilder> stringBuilderThreadLocal = new ThreadLocal<StringBuilder>() {
		
		@Override
		protected StringBuilder initialValue() {
			return new StringBuilder();
		}
	};
	
	
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
		StringBuilder stringBuilder = stringBuilderThreadLocal.get();
		stringBuilder.setLength(0);
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
	
	private Logger logger = LoggerFactory.getLogger(Utility.class);
	
	private String detectedBrowser = null;
	
	/** 通知を送信するクラス */
	public volatile NotifySender notifySender = null;
	
	
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
					JOptionPane.showInputDialog(null, "Please input path-to-browser.", "twclient",
							JOptionPane.INFORMATION_MESSAGE);
		}
		return detectedBrowser;
	}
	
	/**
	 * 通知を送信するクラスを設定する
	 */
	private void detectNotifier() {
		if (notifySender == null) {
			if (getOstype() == OSType.OTHER) {
				try {
					if (Runtime.getRuntime().exec(new String[] {
						"which",
						"notify-send"
					}).waitFor() == 0) {
						notifySender = new LibnotifySender();
					}
				} catch (InterruptedException e) {
					// do nothing
				} catch (IOException e) {
					logger.warn("#detectNotifier: whichの呼び出しに失敗");
				}
			}
			if (notifySender == null) {
				notifySender = new TrayIconNotifySender(configuration);
			}
		}
	}
	
	/**
	 * browserを表示する。
	 * 
	 * @param url 開くURL
	 * @throws HeadlessException GUIを使用できない
	 * @throws InvocationTargetException 関数のinvokeに失敗 (Mac OS)
	 * @throws IllegalAccessException アクセスに失敗
	 * @throws IllegalArgumentException 正しくない引数
	 * @throws IOException IOエラーが発生
	 * @throws NoSuchMethodException 関数のinvokeに失敗 (Mac OS)
	 * @throws SecurityException セキュリティ例外
	 * @throws ClassNotFoundException クラスのinvokeに失敗 (Mac OS)
	 */
	public void openBrowser(String url) throws HeadlessException, IllegalArgumentException, IllegalAccessException,
			InvocationTargetException, IOException, SecurityException, NoSuchMethodException, ClassNotFoundException {
		detectOS();
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
			default:
				break;
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

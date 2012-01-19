package jp.syuriken.snsw.twclient;

import java.awt.Color;
import java.awt.HeadlessException;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ユーティリティクラス。
 * 
 * @author $Author$
 */
public class Utility {
	
	/**
	 * TODO snsoftware
	 * 
	 * @author $Author$
	 */
	public static class IllegalKeyStringException extends RuntimeException {
		
		/**
		 * インスタンスを生成する。
		 * 
		 * @param key
		 */
		public IllegalKeyStringException(String key) {
			super("正しくないキー: " + key);
		}
		
	}
	
	/**
	 * notify-sendを使用して通知を送信するクラス。
	 * 
	 * @author $Author$
	 */
	protected static class LibnotifySender implements NotifySender {
		
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
	
	
	private static Logger logger = LoggerFactory.getLogger(Utility.class);
	
	private static String detectedBrowser = null;
	
	private static volatile OSType ostype;
	
	/** 通知を送信するクラス */
	public static volatile NotifySender notifySender = null;
	
	private static HashMap<Integer, String> keyMap = new HashMap<Integer, String>();
	
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
	 * インストールされているブラウザを確定する。
	 * 
	 * @return ブラウザコマンド
	 */
	protected static String detectBrowser() {
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
	private static void detectNotifier() {
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
					e.printStackTrace(); //TODO
				} catch (IOException e) {
					e.printStackTrace(); //TODO
				}
			}
		}
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
	
	public static boolean equalString(String a, String b) {
		if (a == b) {
			return true;
		}
		if (a == null || b == null) {
			return false;
		}
		if (a.hashCode() == b.hashCode()) {
			return true;
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
	public static void openBrowser(String url) throws HeadlessException, IllegalArgumentException,
			IllegalAccessException, InvocationTargetException, IOException, SecurityException, NoSuchMethodException,
			ClassNotFoundException {
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
	public static void sendNotify(String summary, String text) {
		sendNotify(summary, text, null);
	}
	
	/**
	 * 通知を送信する
	 * @param summary 概要
	 * @param text テキスト
	 * @param imageFile アイコン
	 */
	public static void sendNotify(String summary, String text, File imageFile) {
		detectNotifier();
		if (notifySender != null) {
			try {
				notifySender.sendNotify(summary, text, imageFile);
			} catch (IOException e) {
				logger.warn("通知を送信できませんでした", e);
			}
		}
	}
	
	/*package*/static Object[] toArray(Object... obj) {
		return obj;
	}
	
	/**
	 * TODO snsoftware
	 * 
	 * @param key
	 * @return
	 */
	public static String toKeyString(int code, int modifiers) throws IllegalKeyStringException {
		StringBuilder stringBuilder = stringBuilderThreadLocal.get();
		stringBuilder.setLength(0);
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
		return stringBuilder.toString();
	}
	
	private Utility() {
	}
}

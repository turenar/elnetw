package jp.syuriken.snsw.twclient;

import java.awt.HeadlessException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.swing.JOptionPane;

/**
 * ユーティリティクラス。
 * 
 * @author $Author$
 */
public class Utility {
	
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
	
	
	private static String detectedBrowser = null;
	
	private static OSType ostype;
	
	
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
	 * OS種別を取得する
	 * 
	 * @return OSの種類
	 */
	public static OSType getOstype() {
		return ostype;
	}
	
	/**
	 * 16進ダンプする
	 * 
	 * @param str 文字列
	 * @return 16進ダンプ
	 */
	protected static String hexDump(String str) {
		StringBuilder stringBuilder = new StringBuilder();
		for (int i = 0; i < str.length(); i++) {
			stringBuilder.append(jp.syuriken.snsw.utils.Utility.fixStringLength(Integer.toHexString(str.charAt(i)), 4));
		}
		return stringBuilder.toString();
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
		try {
			if (Runtime.getRuntime().exec(new String[] {
				"which",
				"notify-send"
			}).waitFor() == 0) {
				Runtime.getRuntime().exec(new String[] {
					"notify-send",
					summary,
					text
				});
			}
		} catch (InterruptedException e) {
			e.printStackTrace(); //TODO
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private Utility() {
	}
	
}

package jp.syuriken.snsw.twclient;

import java.awt.HeadlessException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.swing.JOptionPane;

/**
 * TODO snsoftware
 * 
 * @author $Author$
 */
public class Utility {
	
	/**
	 * TODO snsoftware
	 * 
	 * @author $Author$
	 */
	private enum OSType {
		WINDOWS, MAC, OTHER;
	}
	
	
	private static String detectedBrowser = null;
	
	private static OSType ostype;
	
	
	protected static String detectBrowser() {
		if (detectedBrowser != null) {
			return detectedBrowser;
		}
		String[] browsers = {
			"gnome-open",
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
					JOptionPane.showInputDialog(null, "Please input path-to-browser.", "TWclient",
							JOptionPane.INFORMATION_MESSAGE);
		}
		return detectedBrowser;
	}
	
	/**
	 * TODO snsoftware
	 * 
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
	
	protected static String hexDump(String str) {
		StringBuilder stringBuilder = new StringBuilder();
		for (int i = 0; i < str.length(); i++) {
			stringBuilder.append(jp.syuriken.snsw.utils.Utility.fixStringLength(Integer.toHexString(str.charAt(i)), 4));
		}
		return stringBuilder.toString();
	}
	
	/**
	 * TODO snsoftware
	 * 
	 * @param time
	 * @return
	 */
	public static String long2str(long arg) {
		long temp = arg;
		char[] bytes = new char[4];
		for (int i = 3; i > 0; i--) {
			bytes[i] = (char) (temp & 0xffff);
			temp >>= 8;
		}
		return new String(bytes);
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
	
	private Utility() {
	}
	
}

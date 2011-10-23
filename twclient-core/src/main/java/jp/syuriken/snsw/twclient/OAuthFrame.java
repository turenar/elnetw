package jp.syuriken.snsw.twclient;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.CancellationException;
import java.util.logging.Level;

import javax.swing.JOptionPane;

import jp.syuriken.snsw.utils.Logger;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

/**
 * TODO snsoftware
 * 
 * @author $Author$
 */
public class OAuthFrame {
	
	public AccessToken show(Twitter twitter, Logger logger) {
		RequestToken requestToken = null;
		
		try {
			requestToken = twitter.getOAuthRequestToken();
		} catch (TwitterException e) {
			e.printStackTrace();
		}
		
		AccessToken accessToken = null;
		String osName = System.getProperty("os.name");
		
		while (null == accessToken) {
			String strURL = requestToken.getAuthorizationURL();
			
			if (osName.startsWith("Mac OS")) {
				Class<?> fileMgr = null;
				try {
					fileMgr = Class.forName("com.apple.eio.FileManager");
					
					Method openURL = fileMgr.getDeclaredMethod("openURL", new Class[] {
						String.class
					});
					openURL.invoke(null, new Object[] {
						strURL.trim()
					});
				} catch (SecurityException e) {
					logger.log(Level.SEVERE, e);
				} catch (IllegalArgumentException e) {
					logger.log(Level.SEVERE, e);
				} catch (ClassNotFoundException e) {
					logger.log(Level.SEVERE, e);
				} catch (NoSuchMethodException e) {
					logger.log(Level.SEVERE, e);
				} catch (IllegalAccessException e) {
					logger.log(Level.SEVERE, e);
				} catch (InvocationTargetException e) {
					logger.log(Level.SEVERE, e);
				}
				
			} else if (osName.startsWith("Windows")) {
				try {
					Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + strURL.trim());
				} catch (IOException e) {
					logger.log(Level.SEVERE, e);
				}
			} else {
				try {
					Runtime.getRuntime().exec(new String[] {
						"gnome-open",
						strURL.trim()
					});
				} catch (Exception e) {
					try {
						String[] browsers = {
							"firefox",
							"chrome",
							"opera",
							"konqueror",
							"epiphany",
							"mozilla",
						};
						String browser = null;
						
						for (int count = 0; count < browsers.length && browser == null; count++) {
							try {
								if (Runtime.getRuntime().exec(new String[] {
									"which",
									browsers[count]
								}).waitFor() == 0) {
									browser = browsers[count];
								}
							} catch (InterruptedException e2) {
								logger.log(Level.SEVERE, e2);
							}
						}
						
						if (browser == null) {
							JOptionPane.showInputDialog(null, "Please input path-to-browser.", "TWclient",
									JOptionPane.INFORMATION_MESSAGE);
						} else {
							Runtime.getRuntime().exec(new String[] {
								browser,
								strURL.trim()
							});
						}
					} catch (IOException e2) {
						logger.log(Level.SEVERE, e2);
					}
				}
			}
			
			String pin =
					JOptionPane.showInputDialog(null, "Please input PIN code", "PIN CODE",
							JOptionPane.INFORMATION_MESSAGE);
			
			try {
				if (pin != null && pin.length() > 0) {
					accessToken = twitter.getOAuthAccessToken(requestToken, pin);
				} else {
					JOptionPane.showMessageDialog(null, "bye", "exit", JOptionPane.ERROR_MESSAGE);
					throw new CancellationException();
				}
			} catch (TwitterException te) {
				if (401 == te.getStatusCode()) {
					JOptionPane.showMessageDialog(null, "oAuthに失敗しました: PIN codeが正しくありません", "エラー",
							JOptionPane.ERROR_MESSAGE);
				} else {
					JOptionPane.showMessageDialog(null, "oAuthに失敗しました: " + te.getLocalizedMessage(), "エラー",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		}
		return accessToken;
	}
}

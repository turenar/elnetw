package jp.syuriken.snsw.launcher;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import jp.syuriken.snsw.twclient.TwitterClientMain;

/**
 * TwitterClient のためのランチャ
 * 
 * @author $Author$
 */
public class TwitterClientLauncher {
	
	/**
	 * Launch
	 * 
	 * @param args アプリケーション引数
	 */
	public static void main(String[] args) {
		try {
			javax.swing.UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
		
		TwitterClientMain twitterClientMain = new TwitterClientMain(args);
		twitterClientMain.run();
	}
}

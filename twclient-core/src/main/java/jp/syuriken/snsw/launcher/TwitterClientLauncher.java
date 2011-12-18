package jp.syuriken.snsw.launcher;

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
		TwitterClientMain twitterClientMain = new TwitterClientMain(args);
		twitterClientMain.run();
	}
}

package jp.syuriken.snsw.twclient;

import twitter4j.ConnectionLifeCycleListener;

/**
 * TODO snsoftware
 * 
 * @author $Author$
 */
public class ClientConnectionLifeCycleListner implements ConnectionLifeCycleListener {
	
	private final TwitterClientFrame twitterClientFrame;
	
	
	/**
	 * インスタンスを生成する。
	 * 
	 * @param twitterClientFrame
	 */
	public ClientConnectionLifeCycleListner(TwitterClientFrame twitterClientFrame) {
		this.twitterClientFrame = twitterClientFrame;
	}
	
	@Override
	public void onCleanUp() {
		twitterClientFrame.setStatusBar("Cleaning up stream...");
	}
	
	@Override
	public void onConnect() {
		twitterClientFrame.setStatusBar("Connected stream.");
	}
	
	@Override
	public void onDisconnect() {
		twitterClientFrame.setStatusBar("Disconnected stream.");
	}
	
}

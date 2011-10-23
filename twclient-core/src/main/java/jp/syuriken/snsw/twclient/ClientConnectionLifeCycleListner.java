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
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onConnect() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onDisconnect() {
		// TODO Auto-generated method stub
		
	}
	
}

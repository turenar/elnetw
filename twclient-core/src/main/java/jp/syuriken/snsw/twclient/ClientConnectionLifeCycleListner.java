package jp.syuriken.snsw.twclient;

import java.awt.Color;
import java.util.Date;

import javax.swing.JLabel;

import twitter4j.ConnectionLifeCycleListener;

/**
 * ストリームコネクションフック。
 * 
 * @author $Author$
 */
public class ClientConnectionLifeCycleListner implements ConnectionLifeCycleListener {
	
	private final TwitterClientFrame twitterClientFrame;
	
	
	/**
	 * インスタンスを生成する。
	 * 
	 * @param twitterClientFrame API呼び出し
	 */
	public ClientConnectionLifeCycleListner(TwitterClientFrame twitterClientFrame) {
		this.twitterClientFrame = twitterClientFrame;
	}
	
	@Override
	public void onCleanUp() {
		StatusData statusData = new StatusData(this, new Date());
		statusData.backgroundColor = Color.LIGHT_GRAY;
		statusData.foregroundColor = Color.WHITE;
		statusData.image = new JLabel();
		statusData.sentBy = new JLabel();
		statusData.sentBy.setName("!sys.stream.cleanup");
		statusData.data = new JLabel("Cleaning up stream...");
		twitterClientFrame.addStatus(statusData);
	}
	
	@Override
	public void onConnect() {
	}
	
	@Override
	public void onDisconnect() {
		StatusData statusData = new StatusData(this, new Date());
		statusData.backgroundColor = Color.LIGHT_GRAY;
		statusData.foregroundColor = Color.BLACK;
		statusData.image = new JLabel();
		statusData.sentBy = new JLabel();
		statusData.sentBy.setName("!sys.stream.disconnect");
		statusData.data = new JLabel("Disconnected stream...");
		twitterClientFrame.addStatus(statusData, 3000);
	}
	
}

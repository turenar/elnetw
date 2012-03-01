package jp.syuriken.snsw.twclient;

import java.awt.Color;
import java.util.Date;

import javax.swing.Icon;
import javax.swing.JLabel;

import twitter4j.TwitterException;

/**
 * タイムラインビュー
 * 
 * @author $Author$
 */
public class TimelineViewTab extends DefaultClientTab {
	
	private DefaultRenderer renderer = new DefaultRenderer() {
		
		@Override
		public void onException(Exception ex) {
			StatusData statusData = new StatusData(ex, new Date());
			statusData.backgroundColor = Color.BLACK;
			statusData.foregroundColor = Color.RED;
			statusData.image = new JLabel();
			statusData.sentBy = new JLabel("!ERROR!");
			statusData.sentBy.setName("!ex." + ex.getClass().getName());
			String exString;
			if (ex instanceof TwitterException) {
				TwitterException twex = (TwitterException) ex;
				exString = twex.getStatusCode() + ": " + twex.getErrorMessage();
			} else {
				exString = ex.toString();
				if (exString.length() > 256) {
					exString = new StringBuilder().append(exString, 0, 254).append("..").toString();
				}
			}
			statusData.data = new JLabel(exString);
			addStatus(statusData);
		}
	};
	
	private boolean focusGained;
	
	private boolean isDirty;
	
	
	/**
	 * インスタンスを生成する。
	 * 
	 * @param configuration 設定
	 */
	protected TimelineViewTab(ClientConfiguration configuration) {
		super(configuration);
	}
	
	@Override
	public StatusPanel addStatus(StatusData statusData) {
		if (focusGained == false && isDirty == false) {
			isDirty = true;
			configuration.refreshTab(this);
		}
		return super.addStatus(statusData);
	}
	
	@Override
	public void focusGained() {
		focusGained = true;
		isDirty = false;
		configuration.refreshTab(this);
	}
	
	@Override
	public void focusLost() {
		focusGained = false;
	}
	
	@Override
	public Icon getIcon() {
		return null; // TODO
	}
	
	@Override
	public DefaultRenderer getRenderer() {
		return renderer;
	}
	
	@Override
	public String getTitle() {
		return isDirty ? "Timeline*" : "Timeline";
	}
	
	@Override
	public String getToolTip() {
		return "HomeTimeline";
	}
}

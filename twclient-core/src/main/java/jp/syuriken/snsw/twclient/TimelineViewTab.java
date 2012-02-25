package jp.syuriken.snsw.twclient;

import javax.swing.Icon;

/**
 * タイムラインビュー
 * 
 * @author $Author$
 */
public class TimelineViewTab extends DefaultClientTab {
	
	private DefaultRenderer renderer = new DefaultRenderer();
	
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

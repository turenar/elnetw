package jp.syuriken.snsw.twclient;

import javax.swing.Icon;

import twitter4j.Status;

/**
 * メンション表示用タブ
 * 
 * @author $Author$
 */
public class MentionViewTab extends DefaultClientTab {
	
	/**
	 * メンションタブ用レンダラ
	 * 
	 * @author $Author$
	 */
	protected class MentionRenderer extends ClientMessageAdapter implements TabRenderer {
		
		@Override
		public void onStatus(Status originalStatus) {
			Status status;
			if (originalStatus.isRetweet()) {
				status = originalStatus.getRetweetedStatus();
			} else {
				status = originalStatus;
			}
			if (isMentioned(status.getUserMentionEntities())) {
				addStatus(originalStatus);
			}
		}
	}
	
	
	/** レンダラ */
	protected TabRenderer renderer = new MentionRenderer();
	
	private boolean focusGained;
	
	private boolean isDirty;
	
	
	/**
	 * インスタンスを生成する。
	 * 
	 * @param configuration 設定
	 */
	public MentionViewTab(ClientConfiguration configuration) {
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
		return null;
	}
	
	@Override
	public TabRenderer getRenderer() {
		return renderer;
	}
	
	@Override
	public String getTitle() {
		return isDirty ? "Mention*" : "Mention";
	}
	
	@Override
	public String getToolTip() {
		return "@関連";
	}
}

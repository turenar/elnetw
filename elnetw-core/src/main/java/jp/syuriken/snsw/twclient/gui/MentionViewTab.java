package jp.syuriken.snsw.twclient.gui;

import java.awt.EventQueue;

import javax.swing.Icon;

import jp.syuriken.snsw.twclient.filter.IllegalSyntaxException;
import jp.syuriken.snsw.twclient.gui.render.RenderObject;
import jp.syuriken.snsw.twclient.gui.render.RenderTarget;
import twitter4j.Status;
import twitter4j.internal.org.json.JSONException;
import twitter4j.internal.org.json.JSONObject;

/**
 * メンション表示用タブ
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class MentionViewTab extends DefaultClientTab implements RenderTarget {

	/**
	 * メンションタブ用レンダラ
	 *
	 * @author Turenar (snswinhaiku dot lo at gmail dot com)
	 */
	protected class MentionRenderer extends DelegateRenderer {


		@Override
		public void onStatus(Status originalStatus) {
			Status status;
			if (originalStatus.isRetweet()) {
				status = originalStatus.getRetweetedStatus();
			} else {
				status = originalStatus;
			}
			if (isMentioned(status.getUserMentionEntities())) {
				actualRenderer.onStatus(originalStatus);
			}
		}
	}

	private static final String TAB_ID = "mention";
	/** レンダラ */
	protected DelegateRenderer renderer = new MentionRenderer();
	private volatile boolean focusGained;
	private volatile boolean isDirty;


	/**
	 * インスタンスを生成する。
	 *
	 * @throws IllegalSyntaxException クエリエラー
	 */
	public MentionViewTab() throws IllegalSyntaxException {
		super();
		establishTweetPipe();
	}

	/**
	 * インスタンスを生成する。
	 *
	 * @param data 保存されたデータ
	 * @throws JSONException          JSON例外
	 * @throws IllegalSyntaxException クエリエラー
	 */
	public MentionViewTab(String data) throws JSONException, IllegalSyntaxException {
		super(data);
		establishTweetPipe();
	}

	@Override
	public void addStatus(RenderObject renderObject) {
		super.addStatus(renderObject);
		if (!(focusGained || isDirty)) {
			isDirty = true;
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					configuration.refreshTab(MentionViewTab.this);
				}
			});
		}
	}

	private void establishTweetPipe() {
		configuration.getMessageBus().establish(accountId, "statuses/mentions", getRenderer());
		configuration.getMessageBus().establish(accountId, "stream/user", getRenderer());
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
	public DelegateRenderer getDelegateRenderer() {
		return renderer;
	}

	@Override
	public Icon getIcon() {
		return null;
	}

	@Override
	protected Object getSerializedExtendedData() {
		return JSONObject.NULL;
	}

	@Override
	public String getTabId() {
		return TAB_ID;
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

package jp.syuriken.snsw.twclient.gui;

import java.awt.EventQueue;

import javax.swing.Icon;

import jp.syuriken.snsw.twclient.gui.render.RenderObject;
import jp.syuriken.snsw.twclient.gui.render.RenderTarget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.DirectMessage;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.User;
import twitter4j.internal.org.json.JSONException;
import twitter4j.internal.org.json.JSONObject;

/**
 * タイムラインビュー
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class TimelineViewTab extends DefaultClientTab implements RenderTarget {

	/*package*/ static final Logger logger = LoggerFactory.getLogger(TimelineViewTab.class);
	private static final String TAB_ID = "timeline";
	private DelegateRenderer renderer = new DelegateRenderer() {
		@Override
		public void onStatus(Status status) {
			actualRenderer.onStatus(status);
		}

		@Override
		public void onChangeAccount(boolean forWrite) {
			actualRenderer.onChangeAccount(forWrite);
		}

		@Override
		public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
		}

		@Override
		public void onDirectMessage(DirectMessage directMessage) {
			actualRenderer.onDirectMessage(directMessage);
		}

		@Override
		public void onException(Exception ex) {
			actualRenderer.onException(ex);
		}

		@Override
		public void onFavorite(User source, User target, Status favoritedStatus) {
			actualRenderer.onFavorite(source, target, favoritedStatus);
		}

		@Override
		public void onFollow(User source, User followedUser) {
			actualRenderer.onFollow(source, followedUser);
		}

		@Override
		public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
			actualRenderer.onTrackLimitationNotice(numberOfLimitedStatuses);
		}

		@Override
		public void onUnfavorite(User source, User target, Status unfavoritedStatus) {
			actualRenderer.onUnfavorite(source, target, unfavoritedStatus);
		}
	};
	private volatile boolean focusGained;
	private volatile boolean isDirty;

	/** インスタンスを生成する。 */
	public TimelineViewTab() {
		super();
		configuration.getMessageBus().establish(accountId, "my/timeline", getRenderer());
	}

	/**
	 * インスタンスを生成する。
	 *
	 * @param data 保存されたデータ
	 * @throws JSONException JSON例外
	 */
	public TimelineViewTab(String data) throws JSONException {
		super(data);
		configuration.getMessageBus().establish(accountId, "my/timeline", getRenderer());
	}

	@Override
	public void addStatus(RenderObject renderObject) {
		super.addStatus(renderObject);
		if (!(focusGained || isDirty)) {
			isDirty = true;
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					configuration.refreshTab(TimelineViewTab.this);
				}
			});
		}
	}

	@Override
	public void focusGained() {
		super.focusGained();
		focusGained = true;
		isDirty = false;
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				configuration.refreshTab(TimelineViewTab.this);
			}
		});
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
		return null; // TODO
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
		return isDirty ? "Timeline*" : "Timeline";
	}

	@Override
	public String getToolTip() {
		return "HomeTimeline";
	}
}

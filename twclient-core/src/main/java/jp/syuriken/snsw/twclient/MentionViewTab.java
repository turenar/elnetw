package jp.syuriken.snsw.twclient;

import javax.swing.Icon;

import jp.syuriken.snsw.twclient.filter.IllegalSyntaxException;
import twitter4j.DirectMessage;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.User;
import twitter4j.UserList;
import twitter4j.internal.org.json.JSONException;
import twitter4j.internal.org.json.JSONObject;

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
	protected class MentionRenderer extends DefaultRenderer {
		
		@Override
		public void onBlock(User source, User blockedUser) {
			// do nothing
		}
		
		@Override
		public void onChangeAccount(boolean forWrite) {
			// do nothing
		}
		
		@Override
		public void onCleanUp() {
			// do nothing
		}
		
		@Override
		public void onConnect() {
			// do nothing
		}
		
		@Override
		public void onDeletionNotice(long directMessageId, long userId) {
			// do nothing
		}
		
		@Override
		public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
			// do nothing
		}
		
		@Override
		public void onDirectMessage(DirectMessage directMessage) {
			// do nothing
		}
		
		@Override
		public void onDisconnect() {
			// do nothing
		}
		
		@Override
		public void onException(Exception ex) {
			// do nothing
		}
		
		@Override
		public void onFavorite(User source, User target, Status favoritedStatus) {
			// do nothing
		}
		
		@Override
		public void onFollow(User source, User followedUser) {
			// do nothing
		}
		
		@Override
		public void onFriendList(long[] friendIds) {
			// do nothing
		}
		
		@Override
		public void onRetweet(User source, User target, Status retweetedStatus) {
			// do nothing
		}
		
		@Override
		public void onScrubGeo(long userId, long upToStatusId) {
			// do nothing
		}
		
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
		
		@Override
		public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
			// do nothing
		}
		
		@Override
		public void onUnblock(User source, User unblockedUser) {
			// do nothing
		}
		
		@Override
		public void onUnfavorite(User source, User target, Status unfavoritedStatus) {
			// do nothing
		}
		
		@Override
		public void onUserListCreation(User listOwner, UserList list) {
			// do nothing
		}
		
		@Override
		public void onUserListDeletion(User listOwner, UserList list) {
			// do nothing
		}
		
		@Override
		public void onUserListMemberAddition(User addedMember, User listOwner, UserList list) {
			// do nothing
		}
		
		@Override
		public void onUserListMemberDeletion(User deletedMember, User listOwner, UserList list) {
			// do nothing
		}
		
		@Override
		public void onUserListSubscription(User subscriber, User listOwner, UserList list) {
			// do nothing
		}
		
		@Override
		public void onUserListUnsubscription(User subscriber, User listOwner, UserList list) {
			// do nothing
		}
		
		@Override
		public void onUserListUpdate(User listOwner, UserList list) {
			// do nothing
		}
		
		@Override
		public void onUserProfileUpdate(User updatedUser) {
			// do nothing
		}
	}
	
	
	private static final String TAB_ID = "mention";
	
	/** レンダラ */
	protected TabRenderer renderer = new MentionRenderer();
	
	private boolean focusGained;
	
	private boolean isDirty;
	
	
	/**
	 * インスタンスを生成する。
	 * 
	 * @param configuration 設定
	 * @throws IllegalSyntaxException クエリエラー
	 */
	public MentionViewTab(ClientConfiguration configuration) throws IllegalSyntaxException {
		super(configuration);
	}
	
	/**
	 * インスタンスを生成する。
	 * 
	 * @param configuration 設定
	 * @param data 保存されたデータ
	 * @throws JSONException JSON例外
	 * @throws IllegalSyntaxException クエリエラー
	 */
	public MentionViewTab(ClientConfiguration configuration, String data) throws JSONException, IllegalSyntaxException {
		super(configuration, data);
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
	public TabRenderer getActualRenderer() {
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

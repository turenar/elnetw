package jp.syuriken.snsw.twclient;

import twitter4j.DirectMessage;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.User;
import twitter4j.UserList;

/**
 * {@link ClientMessageListener} のアダプター・クラス
 * 
 * @author $Author$
 */
public abstract class ClientMessageAdapter implements ClientMessageListener {
	
	@Override
	public void onBlock(User source, User blockedUser) {
	}
	
	@Override
	public void onChangeAccount(boolean forWrite) {
	}
	
	@Override
	public void onCleanUp() {
	}
	
	@Override
	public void onClientMessage(String name, Object arg) {
	}
	
	@Override
	public void onConnect() {
	}
	
	@Override
	public void onDeletionNotice(long directMessageId, long userId) {
	}
	
	@Override
	public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
	}
	
	@Override
	public void onDirectMessage(DirectMessage directMessage) {
	}
	
	@Override
	public void onDisconnect() {
	}
	
	@Override
	public void onException(Exception ex) {
	}
	
	@Override
	public void onFavorite(User source, User target, Status favoritedStatus) {
	}
	
	@Override
	public void onFollow(User source, User followedUser) {
	}
	
	@Override
	public void onFriendList(long[] friendIds) {
	}
	
	@Override
	public void onRetweet(User source, User target, Status retweetedStatus) {
	}
	
	@Override
	public void onScrubGeo(long userId, long upToStatusId) {
	}
	
	@Override
	public void onStallWarning(StallWarning arg0) {
	}
	
	@Override
	public void onStatus(Status status) {
	}
	
	@Override
	public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
	}
	
	@Override
	public void onUnblock(User source, User unblockedUser) {
	}
	
	@Override
	public void onUnfavorite(User source, User target, Status unfavoritedStatus) {
	}
	
	@Override
	public void onUserListCreation(User listOwner, UserList list) {
	}
	
	@Override
	public void onUserListDeletion(User listOwner, UserList list) {
	}
	
	@Override
	public void onUserListMemberAddition(User addedMember, User listOwner, UserList list) {
	}
	
	@Override
	public void onUserListMemberDeletion(User deletedMember, User listOwner, UserList list) {
	}
	
	@Override
	public void onUserListSubscription(User subscriber, User listOwner, UserList list) {
	}
	
	@Override
	public void onUserListUnsubscription(User subscriber, User listOwner, UserList list) {
	}
	
	@Override
	public void onUserListUpdate(User listOwner, UserList list) {
	}
	
	@Override
	public void onUserProfileUpdate(User updatedUser) {
	}
	
}

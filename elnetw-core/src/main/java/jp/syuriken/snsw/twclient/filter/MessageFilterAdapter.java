package jp.syuriken.snsw.twclient.filter;

import twitter4j.DirectMessage;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.User;
import twitter4j.UserList;

/**
 * {@link MessageFilter}のためのアダプタークラス
 *
 * @author Turenar <snswinhaiku dot lo at gmail dot com>
 */
public abstract class MessageFilterAdapter implements MessageFilter {

	@Override
	public boolean onChangeAccount(boolean forWrite) {
		return false;
	}

	@Override
	public boolean onClientMessage(String name, Object arg) {
		return false;
	}

	@Override
	public boolean onDeletionNotice(long directMessageId, long userId) {
		return false;
	}

	@Override
	public StatusDeletionNotice onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
		return statusDeletionNotice;
	}

	@Override
	public DirectMessage onDirectMessage(DirectMessage message) {
		return message;
	}

	@Override
	public boolean onException(Exception obj) {
		return false;
	}

	@Override
	public boolean onFavorite(User source, User target, Status favoritedStatus) {
		return false;
	}

	@Override
	public boolean onFollow(User source, User followedUser) {
		return false;
	}

	@Override
	public long[] onFriendList(long[] arr) {
		return arr;
	}

	@Override
	public boolean onRetweet(User source, User target, Status retweetedStatus) {
		return false;
	}

	@Override
	public boolean onScrubGeo(long userId, long upToStatusId) {
		return false;
	}

	@Override
	public boolean onStallWarning(StallWarning warning) {
		return false;
	}

	@Override
	public Status onStatus(Status status) {
		return status;
	}

	@Override
	public boolean onStreamCleanUp() {
		return false;
	}

	@Override
	public boolean onStreamConnect() {
		return false;
	}

	@Override
	public boolean onStreamDisconnect() {
		return false;
	}

	@Override
	public boolean onTrackLimitationNotice(int numberOfLimitedStatuses) {
		return false;
	}

	@Override
	public boolean onUnblock(User source, User unblockedUser) {
		return false;
	}

	@Override
	public boolean onUnfavorite(User source, User target, Status unfavoritedStatus) {
		return false;
	}

	@Override
	public boolean onUserListCreation(User listOwner, UserList list) {
		return false;
	}

	@Override
	public boolean onUserListDeletion(User listOwner, UserList list) {
		return false;
	}

	@Override
	public boolean onUserListMemberAddition(User addedMember, User listOwner, UserList list) {
		return false;
	}

	@Override
	public boolean onUserListMemberDeletion(User deletedMember, User listOwner, UserList list) {
		return false;
	}

	@Override
	public boolean onUserListSubscription(User subscriber, User listOwner, UserList list) {
		return false;
	}

	@Override
	public boolean onUserListUnsubscription(User subscriber, User listOwner, UserList list) {
		return false;
	}

	@Override
	public boolean onUserListUpdate(User listOwner, UserList list) {
		return false;
	}

	@Override
	public boolean onUserProfileUpdate(User updatedUser) {
		return false;
	}

}

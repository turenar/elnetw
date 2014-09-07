/*
 * The MIT License (MIT)
 * Copyright (c) 2011-2014 Turenai Project
 *
 * Permission is hereby granted, free of charge,
 *  to any person obtaining a copy of this software and associated documentation files (the "Software"),
 *  to deal in the Software without restriction, including without limitation the rights to
 *  use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 *  and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 *  in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 *  INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 *  IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 *  DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package jp.mydns.turenar.twclient.filter;

import twitter4j.DirectMessage;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.User;
import twitter4j.UserList;
import twitter4j.UserMentionEntity;

/**
 * ユーザー設定によりフィルタを行うフィルタクラス
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public abstract class AbstractMessageFilter extends MessageFilterAdapter {

	/**
	 * create instance
	 */
	public AbstractMessageFilter() {
		this(null);
	}

	/**
	 * create instance
	 *
	 * @param child child message dispatcher
	 */
	public AbstractMessageFilter(MessageFilter child) {
		super(child);
	}

	@Override
	public AbstractMessageFilter clone() throws CloneNotSupportedException {
		AbstractMessageFilter clone = (AbstractMessageFilter) super.clone();
		if (child != null) {
			clone.child = child.clone();
		}
		return clone;
	}

	/**
	 * filter status
	 *
	 * @param status status instance
	 * @return whether status should be filtered out
	 */
	protected boolean filterStatus(Status status) {
		boolean filtered;
		filtered = filterUser(status.getUser());
		if (!filtered && status.isRetweet()) {
			filtered = filterStatus(status.getRetweetedStatus());
		}
		if (!filtered && status.getInReplyToUserId() != -1) {
			filtered = filterUser(status.getInReplyToUserId());
		}
		UserMentionEntity[] userMentionEntities = status.getUserMentionEntities();
		if (!filtered && userMentionEntities != null) {
			final int length = userMentionEntities.length;
			for (int i = 0; !filtered && i < length; i++) {
				filtered = filterUser(userMentionEntities[i].getId());
			}
		}

		return filtered;
	}

	/**
	 * filter user
	 *
	 * @param userId user id
	 * @return whether userId should be filtered out
	 */
	protected abstract boolean filterUser(long userId);

	/**
	 * filter user
	 *
	 * @param user user instance
	 * @return whether user should be filtered out
	 */
	protected boolean filterUser(User user) {
		return filterUser(user.getId());
	}

	@Override
	public void onBlock(User source, User blockedUser) {
		if (!(filterUser(source) || filterUser(blockedUser))) {
			child.onBlock(source, blockedUser);
		}
	}

	@Override
	public void onChangeAccount(boolean forWrite) {
		child.onChangeAccount(forWrite);
	}

	@Override
	public void onCleanUp() {
		child.onCleanUp();
	}

	@Override
	public void onClientMessage(String name, Object arg) {
		child.onClientMessage(name, arg);
	}

	@Override
	public void onConnect() {
		child.onConnect();
	}

	@Override
	public void onDeletionNotice(long directMessageId, long userId) {
		if (!(filterUser(userId))) {
			child.onDeletionNotice(directMessageId, userId);
		}
	}

	@Override
	public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
		if (!(filterUser(statusDeletionNotice.getUserId()))) {
			child.onDeletionNotice(statusDeletionNotice);
		}
	}

	@Override
	public void onDirectMessage(DirectMessage message) {
		if (!(filterUser(message.getSenderId()) || filterUser(message.getRecipientId()))) {
			child.onDirectMessage(message);
		}
	}

	@Override
	public void onDisconnect() {
		child.onDisconnect();
	}

	@Override
	public void onException(Exception ex) {
		child.onException(ex);
	}

	@Override
	public void onFavorite(User source, User target, Status favoritedStatus) {
		if (!(filterUser(source) || filterUser(target) || filterStatus(favoritedStatus))) {
			child.onFavorite(source, target, favoritedStatus);
		}
	}

	@Override
	public void onFollow(User source, User followedUser) {
		if (!(filterUser(source) || filterUser(followedUser))) {
			child.onFollow(source, followedUser);
		}
	}

	@Override
	public void onFriendList(long[] friendIds) {
		child.onFriendList(friendIds);
	}

	@Override
	public void onScrubGeo(long userId, long upToStatusId) {
		if (!(filterUser(userId))) {
			child.onScrubGeo(userId, upToStatusId);
		}
	}

	@Override
	public void onStallWarning(StallWarning stallWarning) {
		child.onStallWarning(stallWarning);
	}

	@Override
	public void onStatus(Status status) {
		if (!filterStatus(status)) {
			child.onStatus(status);
		}
	}

	@Override
	public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
		child.onTrackLimitationNotice(numberOfLimitedStatuses);
	}

	@Override
	public void onUnblock(User source, User unblockedUser) {
		if (!(filterUser(source) || filterUser(unblockedUser))) {
			child.onUnblock(source, unblockedUser);
		}
	}

	@Override
	public void onUnfavorite(User source, User target, Status unfavoritedStatus) {
		if (!(filterUser(source) || filterUser(target) || filterStatus(unfavoritedStatus))) {
			child.onUnfavorite(source, target, unfavoritedStatus);
		}
	}

	@Override
	public void onUnfollow(User source, User unfollowedUser) {
		child.onUnfollow(source, unfollowedUser);
	}

	@Override
	public void onUserListCreation(User listOwner, UserList list) {
		if (!filterUser(listOwner)) {
			child.onUserListCreation(listOwner, list);
		}
	}

	@Override
	public void onUserListDeletion(User listOwner, UserList list) {
		if (!filterUser(listOwner)) {
			child.onUserListDeletion(listOwner, list);
		}
	}

	@Override
	public void onUserListMemberAddition(User addedMember, User listOwner, UserList list) {
		if (!(filterUser(addedMember) || filterUser(listOwner))) {
			child.onUserListMemberAddition(addedMember, listOwner, list);
		}
	}

	@Override
	public void onUserListMemberDeletion(User deletedMember, User listOwner, UserList list) {
		if (!(filterUser(deletedMember) || filterUser(listOwner))) {
			child.onUserListMemberDeletion(deletedMember, listOwner, list);
		}
	}

	@Override
	public void onUserListSubscription(User subscriber, User listOwner, UserList list) {
		if (!(filterUser(subscriber) || filterUser(listOwner))) {
			child.onUserListSubscription(subscriber, listOwner, list);
		}
	}

	@Override
	public void onUserListUnsubscription(User subscriber, User listOwner, UserList list) {
		if (!(filterUser(subscriber) || filterUser(listOwner))) {
			child.onUserListUnsubscription(subscriber, listOwner, list);
		}
	}

	@Override
	public void onUserListUpdate(User listOwner, UserList list) {
		if (!(filterUser(listOwner))) {
			child.onUserListUpdate(listOwner, list);
		}
	}

	@Override
	public void onUserProfileUpdate(User updatedUser) {
		if (!(filterUser(updatedUser))) {
			child.onUserProfileUpdate(updatedUser);
		}
	}

	@Override
	public String toString() {
		return getClass().getName() + "{child=" + child + "}";
	}
}

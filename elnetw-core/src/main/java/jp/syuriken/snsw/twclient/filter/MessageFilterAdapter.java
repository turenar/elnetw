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
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public abstract class MessageFilterAdapter implements MessageFilter {
	@Override
	public boolean onUnfollow(User source, User unfollowedUser) {
		return false;
	}

	@Override
	public boolean onBlock(User source, User blockedUser) {
		return false;
	}

	@Override
	public boolean onChangeAccount(boolean forWrite) {
		return false;
	}

	@Override
	public boolean onCleanUp() {
		return false;
	}

	@Override
	public boolean onClientMessage(String name, Object arg) {
		return false;
	}

	@Override
	public boolean onConnect() {
		return false;
	}

	@Override
	public boolean onDeletionNotice(long directMessageId, long userId) {
		return false;
	}

	@Override
	public boolean onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
		return false;
	}

	@Override
	public DirectMessage onDirectMessage(DirectMessage message) {
		return message;
	}

	@Override
	public boolean onDisconnect() {
		return false;
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

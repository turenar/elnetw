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
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
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
	public void onScrubGeo(long userId, long upToStatusId) {
	}

	@Override
	public void onStallWarning(StallWarning stallWarning) {
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
	public void onUnfollow(User source, User unfollowedUser) {
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

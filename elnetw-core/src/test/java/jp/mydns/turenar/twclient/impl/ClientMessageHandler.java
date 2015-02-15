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

package jp.mydns.turenar.twclient.impl;

import java.util.LinkedList;

import jp.mydns.turenar.twclient.filter.MessageFilter;
import twitter4j.DirectMessage;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.User;
import twitter4j.UserList;

import static org.junit.Assert.*;

/**
 * Created with IntelliJ IDEA.
 * Date: 9/22/14
 * Time: 3:07 AM
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class ClientMessageHandler implements MessageFilter {
	protected LinkedList<String> calledList = new LinkedList<>();

	@Override
	public void addChild(MessageFilter filter) throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	@Override
	public MessageFilter clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

	@Override
	public MessageFilter getChild() throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void onBlock(User source, User blockedUser) {
		calledList.add("onBlock");
	}

	@Override
	public void onChangeAccount(boolean forWrite) {
		calledList.add("onChangeAccount");
	}

	@Override
	public void onCleanUp() {
		calledList.add("onCleanUp");
	}

	@Override
	public void onClientMessage(String name, Object arg) {
		calledList.add("onClientMessage");
	}

	@Override
	public void onConnect() {
		calledList.add("onConnect");
	}

	@Override
	public void onDeletionNotice(long directMessageId, long userId) {
		calledList.add("onDeletionNotice");
	}

	@Override
	public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
		calledList.add("onDeletionNotice");
	}

	@Override
	public void onDirectMessage(DirectMessage directMessage) {
		calledList.add("onDirectMessage");
	}

	@Override
	public void onDisconnect() {
		calledList.add("onDisconnect");
	}

	@Override
	public void onException(Exception ex) {
		calledList.add("onException");
	}

	@Override
	public void onFavorite(User source, User target, Status favoritedStatus) {
		calledList.add("onFavorite");
	}

	@Override
	public void onFollow(User source, User followedUser) {
		calledList.add("onFollow");
	}

	@Override
	public void onFriendList(long[] friendIds) {
		calledList.add("onFriendList");
	}

	@Override
	public void onScrubGeo(long userId, long upToStatusId) {
		calledList.add("onScrubGeo");
	}

	@Override
	public void onStallWarning(StallWarning warning) {
		calledList.add("onStallWarning");
	}

	@Override
	public void onStatus(Status status) {
		calledList.add("onStatus");
	}

	@Override
	public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
		calledList.add("onTrackLimitationNotice");
	}

	@Override
	public void onUnblock(User source, User unblockedUser) {
		calledList.add("onUnblock");
	}

	@Override
	public void onUnfavorite(User source, User target, Status unfavoritedStatus) {
		calledList.add("onUnfavorite");
	}

	@Override
	public void onUnfollow(User source, User unfollowedUser) {
		calledList.add("onUnfollow");
	}

	@Override
	public void onUserDeletion(long deletedUser) {
		calledList.add("onUserDeletion");
	}

	@Override
	public void onUserListCreation(User listOwner, UserList list) {
		calledList.add("onUserListCreation");
	}

	@Override
	public void onUserListDeletion(User listOwner, UserList list) {
		calledList.add("onUserListDeletion");
	}

	@Override
	public void onUserListMemberAddition(User addedMember, User listOwner, UserList list) {
		calledList.add("onUserListMemberAddition");
	}

	@Override
	public void onUserListMemberDeletion(User deletedMember, User listOwner, UserList list) {
		calledList.add("onUserListMemberDeletion");
	}

	@Override
	public void onUserListSubscription(User subscriber, User listOwner, UserList list) {
		calledList.add("onUserListSubscription");
	}

	@Override
	public void onUserListUnsubscription(User subscriber, User listOwner, UserList list) {
		calledList.add("onUserListUnsubscription");
	}

	@Override
	public void onUserListUpdate(User listOwner, UserList list) {
		calledList.add("onUserListUpdate");
	}

	@Override
	public void onUserProfileUpdate(User updatedUser) {
		calledList.add("onUserProfileUpdate");
	}

	@Override
	public void onUserSuspension(long suspendedUser) {
		calledList.add("onUserSuspension");
	}

	@Override
	public void setChild(MessageFilter child) throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	public void testCalled(String message) {
		assertEquals(message, calledList.poll());
	}

	public void testNotCalled() {
		assertTrue(calledList.isEmpty());
	}
}

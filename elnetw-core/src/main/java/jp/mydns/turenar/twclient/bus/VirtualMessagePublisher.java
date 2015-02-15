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

package jp.mydns.turenar.twclient.bus;

import java.util.Arrays;
import java.util.TreeSet;
import java.util.function.Consumer;

import jp.mydns.turenar.twclient.ClientMessageListener;
import jp.mydns.turenar.twclient.twitter.TwitterStatus;
import jp.mydns.turenar.twclient.twitter.TwitterUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.ConnectionLifeCycleListener;
import twitter4j.DirectMessage;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.User;
import twitter4j.UserList;

/**
 * 複数のpeerに通知できるようにしたMessageDispatcher
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
/*package*/
class VirtualMessagePublisher implements ClientMessageListener {
	@FunctionalInterface
	/*package*/interface ExceptionHandler<T> extends Consumer<T> {
		@Override
		default void accept(T t) {
			try {
				consume(t);
			} catch (RuntimeException ex) {
				logger.warn("uncaught exception", ex);
			}
		}

		void consume(T t);
	}

	private static final Logger logger = LoggerFactory.getLogger(VirtualMessagePublisher.class);

	private static <T> Consumer<T> handleEx(ExceptionHandler<T> handler) {
		return handler;
	}

	private final String[] paths;
	private final MessageBus messageBus;
	private volatile int modifiedCount;
	private volatile ClientMessageListener[] cachedListeners;

	public VirtualMessagePublisher(MessageBus messageBus, boolean recursive, String accountId,
			String[] notifierNames) {
		this.messageBus = messageBus;
		// 重複をなくすためArrayListではない。が、TreeSetのほうが結果的に重いかも
		TreeSet<String> paths = new TreeSet<>();
		for (String notifierName : notifierNames) {
			if (recursive) {
				MessageBus.getRecursivePaths(paths, accountId, notifierName);
			} else {
				paths.add(MessageBus.getPath(accountId, notifierName));
			}
		}
		this.paths = paths.toArray(new String[paths.size()]);
	}

	private ClientMessageListener[] getListeners() {
		ClientMessageListener[] listeners = cachedListeners;
		if (modifiedCount != messageBus.getModifiedCount()) {
			synchronized (this) {
				int newModifiedCount = messageBus.getModifiedCount();
				if (modifiedCount != newModifiedCount) {
					logger.debug("Update notifier cache");
					cachedListeners = listeners = messageBus.getEndpoints(paths);
					modifiedCount = newModifiedCount;
				}
			}
		}
		return listeners;
	}

	private Status getStatus(Status status, ClientMessageListener[] listeners) {
		if (listeners.length == 0) {
			return status;
		} else {
			return TwitterStatus.getInstance(status);
		}
	}

	private User getUser(User user, ClientMessageListener[] listeners) {
		if (listeners.length == 0) {
			return user;
		} else {
			return TwitterUser.getInstance(user);
		}
	}

	@Override
	public void onBlock(User source, User blockedUser) {
		ClientMessageListener[] listeners = getListeners();
		final User sourceFinal = getUser(source, listeners);
		final User blockedUserFinal = getUser(blockedUser, listeners);
		Arrays.stream(listeners).forEach(handleEx(
				listener -> listener.onBlock(sourceFinal, blockedUserFinal)
		));
	}

	@Override
	public void onChangeAccount(boolean forWrite) {
		ClientMessageListener[] listeners = getListeners();
		Arrays.stream(listeners).forEach(handleEx(
				listener -> listener.onChangeAccount(forWrite)
		));
	}

	@Override
	public void onCleanUp() {
		ClientMessageListener[] listeners = getListeners();
		Arrays.stream(listeners).forEach(handleEx(ConnectionLifeCycleListener::onCleanUp));
	}

	@Override
	public void onClientMessage(String mesName, Object arg) {
		ClientMessageListener[] listeners = getListeners();
		Arrays.stream(listeners).forEach(handleEx(
				listener -> listener.onClientMessage(mesName, arg)
		));
	}

	@Override
	public void onConnect() {
		ClientMessageListener[] listeners = getListeners();
		Arrays.stream(listeners).forEach(handleEx(ConnectionLifeCycleListener::onConnect));
	}

	@Override
	public void onDeletionNotice(long directMessageId, long userId) {
		ClientMessageListener[] listeners = getListeners();
		Arrays.stream(listeners).forEach(handleEx(
				listener -> listener.onDeletionNotice(directMessageId, userId)
		));
	}

	@Override
	public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
		ClientMessageListener[] listeners = getListeners();
		Arrays.stream(listeners).forEach(handleEx(
				listener -> listener.onDeletionNotice(statusDeletionNotice)
		));
	}

	@Override
	public void onDirectMessage(DirectMessage directMessage) {
		ClientMessageListener[] listeners = getListeners();
		Arrays.stream(listeners).forEach(handleEx(
				listener -> listener.onDirectMessage(directMessage)
		));
	}

	@Override
	public void onDisconnect() {
		ClientMessageListener[] listeners = getListeners();
		Arrays.stream(listeners).forEach(handleEx(ConnectionLifeCycleListener::onDisconnect));
	}

	@Override
	public void onException(Exception ex) {
		ClientMessageListener[] listeners = getListeners();
		for (ClientMessageListener listener : listeners) {
			try {
				listener.onException(ex);
			} catch (RuntimeException re) {
				logger.warn("uncaught exception", re);
			}
		}
	}

	@Override
	public void onFavorite(User source, User target, Status favoritedStatus) {
		ClientMessageListener[] listeners = getListeners();
		final User sourceFinal = getUser(source, listeners);
		final User targetFinal = getUser(target, listeners);
		final Status favoritedStatusFinal = getStatus(favoritedStatus, listeners);
		Arrays.stream(listeners).forEach(handleEx(
				listener -> listener.onFavorite(sourceFinal, targetFinal, favoritedStatusFinal)
		));
	}

	@Override
	public void onFollow(User source, User followedUser) {
		ClientMessageListener[] listeners = getListeners();
		final User sourceFinal = getUser(source, listeners);
		final User followedUserFinal = getUser(followedUser, listeners);
		Arrays.stream(listeners).forEach(handleEx(
				listener -> listener.onFollow(sourceFinal, followedUserFinal)
		));
	}

	@Override
	public void onFriendList(long[] friendIds) {
		ClientMessageListener[] listeners = getListeners();
		Arrays.stream(listeners).forEach(handleEx(
				listener -> listener.onFriendList(friendIds)
		));
	}

	@Override
	public void onScrubGeo(long userId, long upToStatusId) {
		ClientMessageListener[] listeners = getListeners();
		Arrays.stream(listeners).forEach(handleEx(
				listener -> listener.onScrubGeo(userId, upToStatusId)
		));
	}

	@Override
	public void onStallWarning(StallWarning warning) {
		ClientMessageListener[] listeners = getListeners();
		Arrays.stream(listeners).forEach(handleEx(
				listener -> listener.onStallWarning(warning)
		));
	}

	@Override
	public void onStatus(Status status) {
		ClientMessageListener[] listeners = getListeners();
		final Status statusFinal = getStatus(status, listeners);

		Arrays.stream(listeners).forEach(handleEx(
				listener -> listener.onStatus(statusFinal)
		));
	}

	@Override
	public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
		ClientMessageListener[] listeners = getListeners();
		Arrays.stream(listeners).forEach(handleEx(
				listener -> listener.onTrackLimitationNotice(numberOfLimitedStatuses)
		));
	}

	@Override
	public void onUnblock(User source, User unblockedUser) {
		ClientMessageListener[] listeners = getListeners();
		final User sourceFinal = getUser(source, listeners);
		final User unblockedUserFinal = getUser(unblockedUser, listeners);

		Arrays.stream(listeners).forEach(handleEx(
				listener -> listener.onUnblock(sourceFinal, unblockedUserFinal)
		));
	}

	@Override
	public void onUnfavorite(User source, User target, Status unfavoritedStatus) {
		ClientMessageListener[] listeners = getListeners();
		final User sourceFinal = getUser(source, listeners);
		final User targetFinal = getUser(target, listeners);
		final Status unfavoritedStatusFinal = getStatus(unfavoritedStatus, listeners);

		Arrays.stream(listeners).forEach(handleEx(
				listener -> listener.onUnfavorite(sourceFinal, targetFinal, unfavoritedStatusFinal)
		));
	}

	@Override
	public void onUnfollow(User source, User unfollowedUser) {
		ClientMessageListener[] listeners = getListeners();
		final User sourceFinal = getUser(source, listeners);
		final User unfollowedUserFinal = getUser(unfollowedUser, listeners);
		Arrays.stream(listeners).forEach(handleEx(
				listener -> listener.onFollow(sourceFinal, unfollowedUserFinal)
		));
	}

	@Override
	public void onUserDeletion(long deletedUser) {
		ClientMessageListener[] listeners = getListeners();

		Arrays.stream(listeners).forEach(handleEx(
				listener -> listener.onUserDeletion(deletedUser)
		));
	}

	@Override
	public void onUserListCreation(User listOwner, UserList list) {
		ClientMessageListener[] listeners = getListeners();
		final User listOwnerFinal = getUser(listOwner, listeners);

		Arrays.stream(listeners).forEach(handleEx(
				listener -> listener.onUserListCreation(listOwnerFinal, list)
		));
	}

	@Override
	public void onUserListDeletion(User listOwner, UserList list) {
		ClientMessageListener[] listeners = getListeners();
		final User listOwnerFinal = getUser(listOwner, listeners);

		Arrays.stream(listeners).forEach(handleEx(
				listener -> listener.onUserListDeletion(listOwnerFinal, list)
		));
	}

	@Override
	public void onUserListMemberAddition(User addedMember, User listOwner, UserList list) {
		ClientMessageListener[] listeners = getListeners();
		final User addedMemberFinal = getUser(addedMember, listeners);
		final User listOwnerFinal = getUser(listOwner, listeners);


		Arrays.stream(listeners).forEach(handleEx(
				listener -> listener.onUserListMemberAddition(addedMemberFinal, listOwnerFinal, list)
		));
	}

	@Override
	public void onUserListMemberDeletion(User deletedMember, User listOwner, UserList list) {
		ClientMessageListener[] listeners = getListeners();
		final User deletedMemberFinal = getUser(deletedMember, listeners);
		final User listOwnerFinal = getUser(listOwner, listeners);

		Arrays.stream(listeners).forEach(handleEx(
				listener -> listener.onUserListMemberDeletion(deletedMemberFinal, listOwnerFinal, list)
		));
	}

	@Override
	public void onUserListSubscription(User subscriber, User listOwner, UserList list) {
		ClientMessageListener[] listeners = getListeners();
		final User subscriberFinal = getUser(subscriber, listeners);
		final User listOwnerFinal = getUser(listOwner, listeners);

		Arrays.stream(listeners).forEach(handleEx(
				listener -> listener.onUserListSubscription(subscriberFinal, listOwnerFinal, list)
		));
	}

	@Override
	public void onUserListUnsubscription(User subscriber, User listOwner, UserList list) {
		ClientMessageListener[] listeners = getListeners();
		final User subscriberFinal = getUser(subscriber, listeners);
		final User listOwnerFinal = getUser(listOwner, listeners);


		Arrays.stream(listeners).forEach(handleEx(
				listener -> listener.onUserListUnsubscription(subscriberFinal, listOwnerFinal, list)
		));
	}

	@Override
	public void onUserListUpdate(User listOwner, UserList list) {
		ClientMessageListener[] listeners = getListeners();
		final User listOwnerFinal = getUser(listOwner, listeners);

		Arrays.stream(listeners).forEach(handleEx(
				listener -> listener.onUserListUpdate(listOwnerFinal, list)
		));
	}

	@Override
	public void onUserProfileUpdate(User updatedUser) {
		ClientMessageListener[] listeners = getListeners();
		final User updatedUserFinal = getUser(updatedUser, listeners);

		Arrays.stream(listeners).forEach(handleEx(
				listener -> listener.onUserProfileUpdate(updatedUserFinal)
		));
	}

	@Override
	public void onUserSuspension(long suspendedUser) {
		ClientMessageListener[] listeners = getListeners();

		Arrays.stream(listeners).forEach(handleEx(
				listener -> listener.onUserSuspension(suspendedUser)
		));
	}
}

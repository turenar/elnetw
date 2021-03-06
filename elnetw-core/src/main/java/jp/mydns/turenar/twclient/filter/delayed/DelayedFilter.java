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

package jp.mydns.turenar.twclient.filter.delayed;

import java.util.concurrent.ConcurrentLinkedQueue;

import jp.mydns.turenar.twclient.filter.AbstractMessageFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.DirectMessage;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.User;
import twitter4j.UserList;

/**
 * Delayed Message Filter.
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public abstract class DelayedFilter extends AbstractMessageFilter {
	private static Logger logger = LoggerFactory.getLogger(DelayedFilter.class);
	/**
	 * delayed message queue
	 */
	protected ConcurrentLinkedQueue<Runnable> filteringQueue = new ConcurrentLinkedQueue<>();
	/**
	 * is not to delay?
	 */
	protected volatile boolean isStarted;

	@Override
	public AbstractMessageFilter clone() throws CloneNotSupportedException {
		DelayedFilter clone = (DelayedFilter) super.clone();
		clone.filteringQueue = new ConcurrentLinkedQueue<>(filteringQueue);
		return clone;
	}

	@Override
	public void onBlock(User source, User blockedUser) {
		if (isStarted) {
			super.onBlock(source, blockedUser);
		} else {
			filteringQueue.add(() -> onBlock(source, blockedUser));
		}
	}

	@Override
	public void onDeletionNotice(long directMessageId, long userId) {
		if (isStarted) {
			super.onDeletionNotice(directMessageId, userId);
		} else {
			filteringQueue.add(() -> onDeletionNotice(directMessageId, userId));
		}
	}

	@Override
	public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
		if (isStarted) {
			super.onDeletionNotice(statusDeletionNotice);
		} else {
			filteringQueue.add(() -> onDeletionNotice(statusDeletionNotice));
		}
	}

	@Override
	public void onDirectMessage(DirectMessage message) {
		if (isStarted) {
			super.onDirectMessage(message);
		} else {
			filteringQueue.add(() -> onDirectMessage(message));
		}
	}

	@Override
	public void onFavorite(User source, User target, Status favoritedStatus) {
		if (isStarted) {
			super.onFavorite(source, target, favoritedStatus);
		} else {
			filteringQueue.add(() -> onFavorite(source, target, favoritedStatus));
		}
	}

	@Override
	public void onFollow(User source, User followedUser) {
		if (isStarted) {
			super.onFollow(source, followedUser);
		} else {
			filteringQueue.add(() -> onFollow(source, followedUser));
		}
	}

	@Override
	public void onScrubGeo(long userId, long upToStatusId) {
		// scrub geo should be handled in any case.
		super.onScrubGeo(userId, upToStatusId);
	}

	@Override
	public void onStatus(Status status) {
		if (isStarted) {
			super.onStatus(status);
		} else {
			filteringQueue.add(() -> onStatus(status));
		}
	}

	@Override
	public void onUnblock(User source, User unblockedUser) {
		if (isStarted) {
			super.onUnblock(source, unblockedUser);
		} else {
			filteringQueue.add(() -> onUnblock(source, unblockedUser));
		}
	}

	@Override
	public void onUnfavorite(User source, User target, Status unfavoritedStatus) {
		if (isStarted) {
			super.onUnfavorite(source, target, unfavoritedStatus);
		} else {
			filteringQueue.add(() -> onUnfavorite(source, target, unfavoritedStatus));
		}
	}

	@Override
	public void onUnfollow(User source, User unfollowedUser) {
		if (isStarted) {
			super.onUnfollow(source, unfollowedUser);
		} else {
			filteringQueue.add(() -> onUnfollow(source, unfollowedUser));
		}
	}

	@Override
	public void onUserListCreation(User listOwner, UserList list) {
		if (isStarted) {
			super.onUserListCreation(listOwner, list);
		} else {
			filteringQueue.add(() -> onUserListCreation(listOwner, list));
		}
	}

	@Override
	public void onUserListDeletion(User listOwner, UserList list) {
		if (isStarted) {
			super.onUserListDeletion(listOwner, list);
		} else {
			filteringQueue.add(() -> onUserListDeletion(listOwner, list));
		}
	}

	@Override
	public void onUserListMemberAddition(User addedMember, User listOwner, UserList list) {
		if (isStarted) {
			super.onUserListMemberAddition(addedMember, listOwner, list);
		} else {
			filteringQueue.add(() -> onUserListMemberAddition(addedMember, listOwner, list));
		}
	}

	@Override
	public void onUserListMemberDeletion(User deletedMember, User listOwner, UserList list) {
		if (isStarted) {
			super.onUserListMemberDeletion(deletedMember, listOwner, list);
		} else {
			filteringQueue.add(() -> onUserListMemberDeletion(deletedMember, listOwner, list));
		}
	}

	@Override
	public void onUserListSubscription(User subscriber, User listOwner, UserList list) {
		if (isStarted) {
			super.onUserListSubscription(subscriber, listOwner, list);
		} else {
			filteringQueue.add(() -> onUserListSubscription(subscriber, listOwner, list));
		}
	}

	@Override
	public void onUserListUnsubscription(User subscriber, User listOwner, UserList list) {
		if (isStarted) {
			super.onUserListUnsubscription(subscriber, listOwner, list);
		} else {
			filteringQueue.add(() -> onUserListUnsubscription(subscriber, listOwner, list));
		}
	}

	@Override
	public void onUserListUpdate(User listOwner, UserList list) {
		if (isStarted) {
			super.onUserListUpdate(listOwner, list);
		} else {
			filteringQueue.add(() -> onUserListUpdate(listOwner, list));
		}
	}

	@Override
	public void onUserProfileUpdate(User updatedUser) {
		if (isStarted) {
			super.onUserProfileUpdate(updatedUser);
		} else {
			filteringQueue.add(() -> onUserProfileUpdate(updatedUser));
		}
	}

	/**
	 * start message delaying
	 */
	protected synchronized void startDelay() {
		logger.info("start message delaying");
		isStarted = false;
	}

	/**
	 * stop message delaying
	 */
	protected synchronized void stopDelay() {
		logger.info("stop message delaying");
		isStarted = true;
		while (!filteringQueue.isEmpty()) {
			filteringQueue.poll().run();
		}
	}
}

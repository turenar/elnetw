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

package jp.syuriken.snsw.twclient.filter.delayed;

import java.util.concurrent.ConcurrentLinkedQueue;

import jp.syuriken.snsw.twclient.filter.AbstractMessageFilter;
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
			filteringQueue.add(new DelayedOnBlock(this, source, blockedUser));
		}
	}

	@Override
	public void onDeletionNotice(long directMessageId, long userId) {
		if (isStarted) {
			super.onDeletionNotice(directMessageId, userId);
		} else {
			filteringQueue.add(new DelayedOnDirectMessageDeletionNotice(this, directMessageId, userId));
		}
	}

	@Override
	public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
		if (isStarted) {
			super.onDeletionNotice(statusDeletionNotice);
		} else {
			filteringQueue.add(new DelayedOnStatusDeletionNotice(this, statusDeletionNotice));
		}
	}

	@Override
	public void onDirectMessage(DirectMessage message) {
		if (isStarted) {
			super.onDirectMessage(message);
		} else {
			filteringQueue.add(new DelayedOnDirectMessage(this, message));
		}
	}

	@Override
	public void onFavorite(User source, User target, Status favoritedStatus) {
		if (isStarted) {
			super.onFavorite(source, target, favoritedStatus);
		} else {
			filteringQueue.add(new DelayedOnFavorite(this, source, target, favoritedStatus));
		}
	}

	@Override
	public void onFollow(User source, User followedUser) {
		if (isStarted) {
			super.onFollow(source, followedUser);
		} else {
			filteringQueue.add(new DelayedOnFollow(this, source, followedUser));
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
			filteringQueue.add(new DelayedOnStatus(this, status));
		}
	}

	@Override
	public void onUnblock(User source, User unblockedUser) {
		if (isStarted) {
			super.onUnblock(source, unblockedUser);
		} else {
			filteringQueue.add(new DelayedOnUnblock(this, source, unblockedUser));
		}
	}

	@Override
	public void onUnfavorite(User source, User target, Status unfavoritedStatus) {
		if (isStarted) {
			super.onUnfavorite(source, target, unfavoritedStatus);
		} else {
			filteringQueue.add(new DelayedOnUnfavorite(this, source, target, unfavoritedStatus));
		}
	}

	@Override
	public void onUnfollow(User source, User unfollowedUser) {
		if (isStarted) {
			super.onUnfollow(source, unfollowedUser);
		} else {
			filteringQueue.add(new DelayedOnUnfollow(this, source, unfollowedUser));
		}
	}

	@Override
	public void onUserListCreation(User listOwner, UserList list) {
		if (isStarted) {
			super.onUserListCreation(listOwner, list);
		} else {
			filteringQueue.add(new DelayedOnUserListCreation(this, listOwner, list));
		}
	}

	@Override
	public void onUserListDeletion(User listOwner, UserList list) {
		if (isStarted) {
			super.onUserListDeletion(listOwner, list);
		} else {
			filteringQueue.add(new DelayedOnUserListDeletion(this, listOwner, list));
		}
	}

	@Override
	public void onUserListMemberAddition(User addedMember, User listOwner, UserList list) {
		if (isStarted) {
			super.onUserListMemberAddition(addedMember, listOwner, list);
		} else {
			filteringQueue.add(new DelayedOnUserListMemberAddition(this, addedMember, listOwner, list));
		}
	}

	@Override
	public void onUserListMemberDeletion(User deletedMember, User listOwner, UserList list) {
		if (isStarted) {
			super.onUserListMemberDeletion(deletedMember, listOwner, list);
		} else {
			filteringQueue.add(new DelayedOnUserListMemberDeletion(this, deletedMember, listOwner, list));
		}
	}

	@Override
	public void onUserListSubscription(User subscriber, User listOwner, UserList list) {
		if (isStarted) {
			super.onUserListSubscription(subscriber, listOwner, list);
		} else {
			filteringQueue.add(new DelayedOnUserListSubscription(this, subscriber, listOwner, list));
		}
	}

	@Override
	public void onUserListUnsubscription(User subscriber, User listOwner, UserList list) {
		if (isStarted) {
			super.onUserListUnsubscription(subscriber, listOwner, list);
		} else {
			filteringQueue.add(new DelayedOnUserListUnsubscription(this, subscriber, listOwner, list));
		}
	}

	@Override
	public void onUserListUpdate(User listOwner, UserList list) {
		if (isStarted) {
			super.onUserListUpdate(listOwner, list);
		} else {
			filteringQueue.add(new DelayedOnUserListUpdate(this, listOwner, list));
		}
	}

	@Override
	public void onUserProfileUpdate(User updatedUser) {
		if (isStarted) {
			super.onUserProfileUpdate(updatedUser);
		} else {
			filteringQueue.add(new DelayedOnUserProfileUpdate(this, updatedUser));
		}
	}

	/**
	 * stop message delaying
	 */
	protected synchronized void start() {
		logger.info("stop message delaying");
		isStarted = true;
		while (!filteringQueue.isEmpty()) {
			filteringQueue.poll().run();
		}
	}

	/**
	 * start message delaying
	 */
	protected synchronized void stop() {
		logger.info("start message delaying");
		isStarted = false;
	}
}

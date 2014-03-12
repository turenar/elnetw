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

package jp.syuriken.snsw.twclient.bus;

import java.util.TreeSet;

import jp.syuriken.snsw.twclient.CacheManager;
import jp.syuriken.snsw.twclient.ClientConfiguration;
import jp.syuriken.snsw.twclient.ClientMessageListener;
import jp.syuriken.snsw.twclient.twitter.TwitterStatus;
import jp.syuriken.snsw.twclient.twitter.TwitterUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

	private static final Logger logger = LoggerFactory.getLogger(VirtualMessagePublisher.class);
	private final String[] paths;
	private final MessageBus messageBus;
	private final CacheManager cacheManager;
	private volatile int modifiedCount;
	private volatile ClientMessageListener[] cachedListeners;

	public VirtualMessagePublisher(MessageBus messageBus, boolean recursive, String accountId,
			String[] notifierNames) {
		this.messageBus = messageBus;
		// 重複をなくすためArrayListではない。が、TreeSetのほうが結果的に重いかも
		TreeSet<String> paths = new TreeSet<>();
		for (String notifierName : notifierNames) {
			if (recursive) {
				messageBus.getRecursivePaths(paths, accountId, notifierName);
			} else {
				paths.add(messageBus.getPath(accountId, notifierName));
			}
		}
		this.paths = paths.toArray(new String[paths.size()]);
		cacheManager = ClientConfiguration.getInstance().getCacheManager();
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
		if (!(listeners.length == 0 || status instanceof TwitterStatus)) {
			TwitterStatus cachedStatus = cacheManager.getCachedStatus(status.getId());
			if (cachedStatus == null) {
				return cacheManager.getCachedStatus(new TwitterStatus(status));
			} else {
				return cachedStatus;
			}
		} else {
			return status;
		}
	}

	private User getUser(User user, ClientMessageListener[] listeners) {
		if (!(listeners.length == 0 || user instanceof TwitterUser)) {
			TwitterUser cachedUser = cacheManager.getCachedUser(user.getId());
			if (cachedUser == null) {
				return cacheManager.getCachedUser(new TwitterUser(user));
			} else {
				return cachedUser;
			}
		} else {
			return user;
		}
	}

	@Override
	public void onBlock(User source, User blockedUser) {
		ClientMessageListener[] listeners = getListeners();
		source = getUser(source, listeners);
		blockedUser = getUser(blockedUser, listeners);

		for (ClientMessageListener listener : listeners) {
			try {
				listener.onBlock(source, blockedUser);
			} catch (RuntimeException ex) {
				logger.warn("uncaught exception", ex);
			}
		}
	}

	@Override
	public void onChangeAccount(boolean forWrite) {
		ClientMessageListener[] listeners = getListeners();
		for (ClientMessageListener listener : listeners) {
			try {
				listener.onChangeAccount(forWrite);
			} catch (RuntimeException ex) {
				logger.warn("uncaught exception", ex);
			}
		}
	}

	@Override
	public void onCleanUp() {
		ClientMessageListener[] listeners = getListeners();
		for (ClientMessageListener listener : listeners) {
			try {
				listener.onCleanUp();
			} catch (RuntimeException ex) {
				logger.warn("uncaught exception", ex);
			}
		}
	}

	@Override
	public void onClientMessage(String mesName, Object arg) {
		ClientMessageListener[] listeners = getListeners();
		for (ClientMessageListener listener : listeners) {
			try {
				listener.onClientMessage(mesName, arg);
			} catch (RuntimeException ex) {
				logger.warn("uncaught exception", ex);
			}
		}
	}

	@Override
	public void onConnect() {
		ClientMessageListener[] listeners = getListeners();
		for (ClientMessageListener listener : listeners) {
			try {
				listener.onConnect();
			} catch (RuntimeException ex) {
				logger.warn("uncaught exception", ex);
			}
		}
	}

	@Override
	public void onDeletionNotice(long directMessageId, long userId) {
		ClientMessageListener[] listeners = getListeners();
		for (ClientMessageListener listener : listeners) {
			try {
				listener.onDeletionNotice(directMessageId, userId);
			} catch (RuntimeException ex) {
				logger.warn("uncaught exception", ex);
			}
		}
	}

	@Override
	public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
		ClientMessageListener[] listeners = getListeners();
		for (ClientMessageListener listener : listeners) {
			try {
				listener.onDeletionNotice(statusDeletionNotice);
			} catch (RuntimeException ex) {
				logger.warn("uncaught exception", ex);
			}
		}
	}

	@Override
	public void onDirectMessage(DirectMessage directMessage) {
		ClientMessageListener[] listeners = getListeners();
		for (ClientMessageListener listener : listeners) {
			try {
				listener.onDirectMessage(directMessage);
			} catch (RuntimeException ex) {
				logger.warn("uncaught exception", ex);
			}
		}
	}

	@Override
	public void onDisconnect() {
		ClientMessageListener[] listeners = getListeners();
		for (ClientMessageListener listener : listeners) {
			try {
				listener.onDisconnect();
			} catch (RuntimeException ex) {
				logger.warn("uncaught exception", ex);
			}
		}
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
		source = getUser(source, listeners);
		target = getUser(target, listeners);
		favoritedStatus = getStatus(favoritedStatus, listeners);
		for (ClientMessageListener listener : listeners) {
			try {
				listener.onFavorite(source, target, favoritedStatus);
			} catch (RuntimeException ex) {
				logger.warn("uncaught exception", ex);
			}
		}
	}

	@Override
	public void onFollow(User source, User followedUser) {
		ClientMessageListener[] listeners = getListeners();
		source = getUser(source, listeners);
		followedUser = getUser(followedUser, listeners);
		for (ClientMessageListener listener : listeners) {
			try {
				listener.onFollow(source, followedUser);
			} catch (RuntimeException ex) {
				logger.warn("uncaught exception", ex);
			}
		}
	}

	@Override
	public void onFriendList(long[] friendIds) {
		ClientMessageListener[] listeners = getListeners();
		for (ClientMessageListener listener : listeners) {
			try {
				listener.onFriendList(friendIds);
			} catch (RuntimeException ex) {
				logger.warn("uncaught exception", ex);
			}
		}
	}

	@Override
	public void onScrubGeo(long userId, long upToStatusId) {
		ClientMessageListener[] listeners = getListeners();
		for (ClientMessageListener listener : listeners) {
			try {
				listener.onScrubGeo(userId, upToStatusId);
			} catch (RuntimeException ex) {
				logger.warn("uncaught exception", ex);
			}
		}
	}

	@Override
	public void onStallWarning(StallWarning warning) {
		ClientMessageListener[] listeners = getListeners();
		for (ClientMessageListener listener : listeners) {
			try {
				listener.onStallWarning(warning);
			} catch (RuntimeException ex) {
				logger.warn("uncaught exception", ex);
			}
		}
	}

	@Override
	public void onStatus(Status status) {
		ClientMessageListener[] listeners = getListeners();
		status = getStatus(status, listeners);

		for (ClientMessageListener listener : listeners) {
			try {
				listener.onStatus(status);
			} catch (RuntimeException ex) {
				logger.warn("uncaught exception", ex);
			}
		}
	}

	@Override
	public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
		ClientMessageListener[] listeners = getListeners();
		for (ClientMessageListener listener : listeners) {
			try {
				listener.onTrackLimitationNotice(numberOfLimitedStatuses);
			} catch (RuntimeException ex) {
				logger.warn("uncaught exception", ex);
			}
		}
	}

	@Override
	public void onUnblock(User source, User unblockedUser) {
		ClientMessageListener[] listeners = getListeners();
		source = getUser(source, listeners);
		unblockedUser = getUser(unblockedUser, listeners);

		for (ClientMessageListener listener : listeners) {
			try {
				listener.onUnblock(source, unblockedUser);
			} catch (RuntimeException ex) {
				logger.warn("uncaught exception", ex);
			}
		}
	}

	@Override
	public void onUnfavorite(User source, User target, Status unfavoritedStatus) {
		ClientMessageListener[] listeners = getListeners();
		source = getUser(source, listeners);
		target = getUser(target, listeners);
		unfavoritedStatus = getStatus(unfavoritedStatus, listeners);

		for (ClientMessageListener listener : listeners) {
			try {
				listener.onUnfavorite(source, target, unfavoritedStatus);
			} catch (RuntimeException ex) {
				logger.warn("uncaught exception", ex);
			}
		}
	}

	@Override
	public void onUserListCreation(User listOwner, UserList list) {
		ClientMessageListener[] listeners = getListeners();
		listOwner = getUser(listOwner, listeners);

		for (ClientMessageListener listener : listeners) {
			try {
				listener.onUserListCreation(listOwner, list);
			} catch (RuntimeException ex) {
				logger.warn("uncaught exception", ex);
			}
		}
	}

	@Override
	public void onUserListDeletion(User listOwner, UserList list) {
		ClientMessageListener[] listeners = getListeners();
		listOwner = getUser(listOwner, listeners);

		for (ClientMessageListener listener : listeners) {
			try {
				listener.onUserListDeletion(listOwner, list);
			} catch (RuntimeException ex) {
				logger.warn("uncaught exception", ex);
			}
		}
	}

	@Override
	public void onUserListMemberAddition(User addedMember, User listOwner, UserList list) {
		ClientMessageListener[] listeners = getListeners();
		addedMember = getUser(addedMember, listeners);
		listOwner = getUser(listOwner, listeners);


		for (ClientMessageListener listener : listeners) {
			try {
				listener.onUserListMemberAddition(addedMember, listOwner, list);
			} catch (RuntimeException ex) {
				logger.warn("uncaught exception", ex);
			}
		}
	}

	@Override
	public void onUserListMemberDeletion(User deletedMember, User listOwner, UserList list) {
		ClientMessageListener[] listeners = getListeners();
		deletedMember = getUser(deletedMember, listeners);
		listOwner = getUser(listOwner, listeners);

		for (ClientMessageListener listener : listeners) {
			try {
				listener.onUserListMemberDeletion(deletedMember, listOwner, list);
			} catch (RuntimeException ex) {
				logger.warn("uncaught exception", ex);
			}
		}
	}

	@Override
	public void onUserListSubscription(User subscriber, User listOwner, UserList list) {
		ClientMessageListener[] listeners = getListeners();
		subscriber = getUser(subscriber, listeners);
		listOwner = getUser(listOwner, listeners);

		for (ClientMessageListener listener : listeners) {
			try {
				listener.onUserListSubscription(subscriber, listOwner, list);
			} catch (RuntimeException ex) {
				logger.warn("uncaught exception", ex);
			}
		}
	}

	@Override
	public void onUserListUnsubscription(User subscriber, User listOwner, UserList list) {
		ClientMessageListener[] listeners = getListeners();
		subscriber = getUser(subscriber, listeners);
		listOwner = getUser(listOwner, listeners);


		for (ClientMessageListener listener : listeners) {
			try {
				listener.onUserListUnsubscription(subscriber, listOwner, list);
			} catch (RuntimeException ex) {
				logger.warn("uncaught exception", ex);
			}
		}
	}

	@Override
	public void onUserListUpdate(User listOwner, UserList list) {
		ClientMessageListener[] listeners = getListeners();
		listOwner = getUser(listOwner, listeners);

		for (ClientMessageListener listener : listeners) {
			try {
				listener.onUserListUpdate(listOwner, list);
			} catch (RuntimeException ex) {
				logger.warn("uncaught exception", ex);
			}
		}
	}

	@Override
	public void onUserProfileUpdate(User updatedUser) {
		ClientMessageListener[] listeners = getListeners();
		updatedUser = getUser(updatedUser, listeners);

		for (ClientMessageListener listener : listeners) {
			try {
				listener.onUserProfileUpdate(updatedUser);
			} catch (RuntimeException ex) {
				logger.warn("uncaught exception", ex);
			}
		}
	}
}

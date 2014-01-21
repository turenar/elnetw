package jp.syuriken.snsw.twclient.net;

import java.util.HashSet;
import java.util.TreeSet;

import jp.syuriken.snsw.twclient.ClientMessageListener;
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
class VirtualMultipleMessageDispatcher implements ClientMessageListener {

	private static final Logger logger = LoggerFactory.getLogger(VirtualMultipleMessageDispatcher.class);
	private final String[] paths;
	private final TwitterDataFetchScheduler scheduler;
	private volatile int modifiedCount;
	private volatile ClientMessageListener[] cachedListeners;

	public VirtualMultipleMessageDispatcher(TwitterDataFetchScheduler scheduler, boolean recursive, String accountId,
			String[] notifierNames) {
		this.scheduler = scheduler;
		// 重複をなくすためArrayListではない。が、TreeSetのほうが結果的に重いかも
		TreeSet<String> paths = new TreeSet<>();
		for (String notifierName : notifierNames) {
			if (recursive) {
				scheduler.getRecursivePaths(paths, accountId, notifierName);
			} else {
				paths.add(scheduler.getPath(accountId, notifierName));
			}
		}
		this.paths = paths.toArray(new String[paths.size()]);
	}

	private ClientMessageListener[] getListeners() {
		ClientMessageListener[] listeners = cachedListeners;
		if (modifiedCount != scheduler.getModifiedCount()) {
			synchronized (this) {
				int newModifiedCount = scheduler.getModifiedCount();
				if (modifiedCount != newModifiedCount) {
					logger.debug("Update notifier cache");
					HashSet<ClientMessageListener> listenersSet = new HashSet<>();
					for (String path : paths) {
						for (ClientMessageListener listener : scheduler.getInternalListeners(path)) {
							listenersSet.add(listener);
						}
					}
					listeners = listenersSet.toArray(new ClientMessageListener[listenersSet.size()]);
					cachedListeners = listeners;
					modifiedCount = newModifiedCount;
				}
			}
		}
		return listeners;
	}

	@Override
	public void onBlock(User source, User blockedUser) {
		ClientMessageListener[] listeners = getListeners();
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
		for (ClientMessageListener listener : listeners) {
			try {
				listener.onUserProfileUpdate(updatedUser);
			} catch (RuntimeException ex) {
				logger.warn("uncaught exception", ex);
			}
		}
	}
}

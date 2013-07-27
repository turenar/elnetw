package jp.syuriken.snsw.twclient.net;

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
/*package*/class VirtualMultipleMessageDispatcher implements ClientMessageListener {

	private static final Logger logger = LoggerFactory.getLogger(VirtualMultipleMessageDispatcher.class);
	private final String[] paths;
	private TwitterDataFetchScheduler scheduler;

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

	@Override
	public void onBlock(User source, User blockedUser) {
		for (String name : paths) {
			for (ClientMessageListener listener : scheduler.getInternalListeners(name)) {
				try {
					listener.onBlock(source, blockedUser);
				} catch (RuntimeException ex) {
					logger.warn("uncaught exception", ex);
				}
			}
		}
	}

	@Override
	public void onChangeAccount(boolean forWrite) {
		for (String name : paths) {
			for (ClientMessageListener listener : scheduler.getInternalListeners(name)) {
				try {
					listener.onChangeAccount(forWrite);
				} catch (RuntimeException ex) {
					logger.warn("uncaught exception", ex);
				}
			}
		}
	}

	@Override
	public void onCleanUp() {
		for (String name : paths) {
			for (ClientMessageListener listener : scheduler.getInternalListeners(name)) {
				try {
					listener.onCleanUp();
				} catch (RuntimeException ex) {
					logger.warn("uncaught exception", ex);
				}
			}
		}
	}

	@Override
	public void onClientMessage(String mesName, Object arg) {
		for (String name : paths) {
			for (ClientMessageListener listener : scheduler.getInternalListeners(name)) {
				try {
					listener.onClientMessage(mesName, arg);
				} catch (RuntimeException ex) {
					logger.warn("uncaught exception", ex);
				}
			}
		}
	}

	@Override
	public void onConnect() {
		for (String name : paths) {
			for (ClientMessageListener listener : scheduler.getInternalListeners(name)) {
				try {
					listener.onConnect();
				} catch (RuntimeException ex) {
					logger.warn("uncaught exception", ex);
				}
			}
		}
	}

	@Override
	public void onDeletionNotice(long directMessageId, long userId) {
		for (String name : paths) {
			for (ClientMessageListener listener : scheduler.getInternalListeners(name)) {
				try {
					listener.onDeletionNotice(directMessageId, userId);
				} catch (RuntimeException ex) {
					logger.warn("uncaught exception", ex);
				}
			}
		}
	}

	@Override
	public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
		for (String name : paths) {
			for (ClientMessageListener listener : scheduler.getInternalListeners(name)) {
				try {
					listener.onDeletionNotice(statusDeletionNotice);
				} catch (RuntimeException ex) {
					logger.warn("uncaught exception", ex);
				}
			}
		}
	}

	@Override
	public void onDirectMessage(DirectMessage directMessage) {
		for (String name : paths) {
			for (ClientMessageListener listener : scheduler.getInternalListeners(name)) {
				try {
					listener.onDirectMessage(directMessage);
				} catch (RuntimeException ex) {
					logger.warn("uncaught exception", ex);
				}
			}
		}
	}

	@Override
	public void onDisconnect() {
		for (String name : paths) {
			for (ClientMessageListener listener : scheduler.getInternalListeners(name)) {
				try {
					listener.onDisconnect();
				} catch (RuntimeException ex) {
					logger.warn("uncaught exception", ex);
				}
			}
		}
	}

	@Override
	public void onException(Exception ex) {
		for (String name : paths) {
			for (ClientMessageListener listener : scheduler.getInternalListeners(name)) {
				try {
					listener.onException(ex);
				} catch (RuntimeException re) {
					logger.warn("uncaught exception", re);
				}
			}
		}
	}

	@Override
	public void onFavorite(User source, User target, Status favoritedStatus) {
		for (String name : paths) {
			for (ClientMessageListener listener : scheduler.getInternalListeners(name)) {
				try {
					listener.onFavorite(source, target, favoritedStatus);
				} catch (RuntimeException ex) {
					logger.warn("uncaught exception", ex);
				}
			}
		}
	}

	@Override
	public void onFollow(User source, User followedUser) {
		for (String name : paths) {
			for (ClientMessageListener listener : scheduler.getInternalListeners(name)) {
				try {
					listener.onFollow(source, followedUser);
				} catch (RuntimeException ex) {
					logger.warn("uncaught exception", ex);
				}
			}
		}
	}

	@Override
	public void onFriendList(long[] friendIds) {
		for (String name : paths) {
			for (ClientMessageListener listener : scheduler.getInternalListeners(name)) {
				try {
					listener.onFriendList(friendIds);
				} catch (RuntimeException ex) {
					logger.warn("uncaught exception", ex);
				}
			}
		}
	}

	@Override
	public void onScrubGeo(long userId, long upToStatusId) {
		for (String name : paths) {
			for (ClientMessageListener listener : scheduler.getInternalListeners(name)) {
				try {
					listener.onScrubGeo(userId, upToStatusId);
				} catch (RuntimeException ex) {
					logger.warn("uncaught exception", ex);
				}
			}
		}
	}

	@Override
	public void onStallWarning(StallWarning warning) {
		for (String name : paths) {
			for (ClientMessageListener listener : scheduler.getInternalListeners(name)) {
				try {
					listener.onStallWarning(warning);
				} catch (RuntimeException ex) {
					logger.warn("uncaught exception", ex);
				}
			}
		}
	}

	@Override
	public void onStatus(Status status) {
		for (String name : paths) {
			for (ClientMessageListener listener : scheduler.getInternalListeners(name)) {
				try {
					listener.onStatus(status);
				} catch (RuntimeException ex) {
					logger.warn("uncaught exception", ex);
				}
			}
		}
	}

	@Override
	public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
		for (String name : paths) {
			for (ClientMessageListener listener : scheduler.getInternalListeners(name)) {
				try {
					listener.onTrackLimitationNotice(numberOfLimitedStatuses);
				} catch (RuntimeException ex) {
					logger.warn("uncaught exception", ex);
				}
			}
		}
	}

	@Override
	public void onUnblock(User source, User unblockedUser) {
		for (String name : paths) {
			for (ClientMessageListener listener : scheduler.getInternalListeners(name)) {
				try {
					listener.onUnblock(source, unblockedUser);
				} catch (RuntimeException ex) {
					logger.warn("uncaught exception", ex);
				}
			}
		}
	}

	@Override
	public void onUnfavorite(User source, User target, Status unfavoritedStatus) {
		for (String name : paths) {
			for (ClientMessageListener listener : scheduler.getInternalListeners(name)) {
				try {
					listener.onUnfavorite(source, target, unfavoritedStatus);
				} catch (RuntimeException ex) {
					logger.warn("uncaught exception", ex);
				}
			}
		}
	}

	@Override
	public void onUserListCreation(User listOwner, UserList list) {
		for (String name : paths) {
			for (ClientMessageListener listener : scheduler.getInternalListeners(name)) {
				try {
					listener.onUserListCreation(listOwner, list);
				} catch (RuntimeException ex) {
					logger.warn("uncaught exception", ex);
				}
			}
		}
	}

	@Override
	public void onUserListDeletion(User listOwner, UserList list) {
		for (String name : paths) {
			for (ClientMessageListener listener : scheduler.getInternalListeners(name)) {
				try {
					listener.onUserListDeletion(listOwner, list);
				} catch (RuntimeException ex) {
					logger.warn("uncaught exception", ex);
				}
			}
		}
	}

	@Override
	public void onUserListMemberAddition(User addedMember, User listOwner, UserList list) {
		for (String name : paths) {
			for (ClientMessageListener listener : scheduler.getInternalListeners(name)) {
				try {
					listener.onUserListMemberAddition(addedMember, listOwner, list);
				} catch (RuntimeException ex) {
					logger.warn("uncaught exception", ex);
				}
			}
		}
	}

	@Override
	public void onUserListMemberDeletion(User deletedMember, User listOwner, UserList list) {
		for (String name : paths) {
			for (ClientMessageListener listener : scheduler.getInternalListeners(name)) {
				try {
					listener.onUserListMemberDeletion(deletedMember, listOwner, list);
				} catch (RuntimeException ex) {
					logger.warn("uncaught exception", ex);
				}
			}
		}
	}

	@Override
	public void onUserListSubscription(User subscriber, User listOwner, UserList list) {
		for (String name : paths) {
			for (ClientMessageListener listener : scheduler.getInternalListeners(name)) {
				try {
					listener.onUserListSubscription(subscriber, listOwner, list);
				} catch (RuntimeException ex) {
					logger.warn("uncaught exception", ex);
				}
			}
		}
	}

	@Override
	public void onUserListUnsubscription(User subscriber, User listOwner, UserList list) {
		for (String name : paths) {
			for (ClientMessageListener listener : scheduler.getInternalListeners(name)) {
				try {
					listener.onUserListUnsubscription(subscriber, listOwner, list);
				} catch (RuntimeException ex) {
					logger.warn("uncaught exception", ex);
				}
			}
		}
	}

	@Override
	public void onUserListUpdate(User listOwner, UserList list) {
		for (String name : paths) {
			for (ClientMessageListener listener : scheduler.getInternalListeners(name)) {
				try {
					listener.onUserListUpdate(listOwner, list);
				} catch (RuntimeException ex) {
					logger.warn("uncaught exception", ex);
				}
			}
		}
	}

	@Override
	public void onUserProfileUpdate(User updatedUser) {
		for (String name : paths) {
			for (ClientMessageListener listener : scheduler.getInternalListeners(name)) {
				try {
					listener.onUserProfileUpdate(updatedUser);
				} catch (RuntimeException ex) {
					logger.warn("uncaught exception", ex);
				}
			}
		}
	}
}

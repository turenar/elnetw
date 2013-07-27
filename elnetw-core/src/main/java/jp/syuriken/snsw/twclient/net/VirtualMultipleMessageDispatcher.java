package jp.syuriken.snsw.twclient.net;

import java.util.TreeSet;

import jp.syuriken.snsw.twclient.ClientMessageListener;
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
				listener.onBlock(source, blockedUser);
			}
		}
	}

	@Override
	public void onChangeAccount(boolean forWrite) {
		for (String name : paths) {
			for (ClientMessageListener listener : scheduler.getInternalListeners(name)) {
				listener.onChangeAccount(forWrite);
			}
		}
	}

	@Override
	public void onCleanUp() {
		for (String name : paths) {
			for (ClientMessageListener listener : scheduler.getInternalListeners(name)) {
				listener.onCleanUp();
			}
		}
	}

	@Override
	public void onClientMessage(String mesName, Object arg) {
		for (String name : paths) {
			for (ClientMessageListener listener : scheduler.getInternalListeners(name)) {
				listener.onClientMessage(mesName, arg);
			}
		}
	}

	@Override
	public void onConnect() {
		for (String name : paths) {
			for (ClientMessageListener listener : scheduler.getInternalListeners(name)) {
				listener.onConnect();
			}
		}
	}

	@Override
	public void onDeletionNotice(long directMessageId, long userId) {
		for (String name : paths) {
			for (ClientMessageListener listener : scheduler.getInternalListeners(name)) {
				listener.onDeletionNotice(directMessageId, userId);
			}
		}
	}

	@Override
	public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
		for (String name : paths) {
			for (ClientMessageListener listener : scheduler.getInternalListeners(name)) {
				listener.onDeletionNotice(statusDeletionNotice);
			}
		}
	}

	@Override
	public void onDirectMessage(DirectMessage directMessage) {
		for (String name : paths) {
			for (ClientMessageListener listener : scheduler.getInternalListeners(name)) {
				listener.onDirectMessage(directMessage);
			}
		}
	}

	@Override
	public void onDisconnect() {
		for (String name : paths) {
			for (ClientMessageListener listener : scheduler.getInternalListeners(name)) {
				listener.onDisconnect();
			}
		}
	}

	@Override
	public void onException(Exception ex) {
		for (String name : paths) {
			for (ClientMessageListener listener : scheduler.getInternalListeners(name)) {
				listener.onException(ex);
			}
		}
	}

	@Override
	public void onFavorite(User source, User target, Status favoritedStatus) {
		for (String name : paths) {
			for (ClientMessageListener listener : scheduler.getInternalListeners(name)) {
				listener.onFavorite(source, target, favoritedStatus);
			}
		}
	}

	@Override
	public void onFollow(User source, User followedUser) {
		for (String name : paths) {
			for (ClientMessageListener listener : scheduler.getInternalListeners(name)) {
				listener.onFollow(source, followedUser);
			}
		}
	}

	@Override
	public void onFriendList(long[] friendIds) {
		for (String name : paths) {
			for (ClientMessageListener listener : scheduler.getInternalListeners(name)) {
				listener.onFriendList(friendIds);
			}
		}
	}

	@Override
	public void onScrubGeo(long userId, long upToStatusId) {
		for (String name : paths) {
			for (ClientMessageListener listener : scheduler.getInternalListeners(name)) {
				listener.onScrubGeo(userId, upToStatusId);
			}
		}
	}

	@Override
	public void onStallWarning(StallWarning warning) {
		for (String name : paths) {
			for (ClientMessageListener listener : scheduler.getInternalListeners(name)) {
				listener.onStallWarning(warning);
			}
		}
	}

	@Override
	public void onStatus(Status status) {
		for (String name : paths) {
			for (ClientMessageListener listener : scheduler.getInternalListeners(name)) {
				listener.onStatus(status);
			}
		}
	}

	@Override
	public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
		for (String name : paths) {
			for (ClientMessageListener listener : scheduler.getInternalListeners(name)) {
				listener.onTrackLimitationNotice(numberOfLimitedStatuses);
			}
		}
	}

	@Override
	public void onUnblock(User source, User unblockedUser) {
		for (String name : paths) {
			for (ClientMessageListener listener : scheduler.getInternalListeners(name)) {
				listener.onUnblock(source, unblockedUser);
			}
		}
	}

	@Override
	public void onUnfavorite(User source, User target, Status unfavoritedStatus) {
		for (String name : paths) {
			for (ClientMessageListener listener : scheduler.getInternalListeners(name)) {
				listener.onUnfavorite(source, target, unfavoritedStatus);
			}
		}
	}

	@Override
	public void onUserListCreation(User listOwner, UserList list) {
		for (String name : paths) {
			for (ClientMessageListener listener : scheduler.getInternalListeners(name)) {
				listener.onUserListCreation(listOwner, list);
			}
		}
	}

	@Override
	public void onUserListDeletion(User listOwner, UserList list) {
		for (String name : paths) {
			for (ClientMessageListener listener : scheduler.getInternalListeners(name)) {
				listener.onUserListDeletion(listOwner, list);
			}
		}
	}

	@Override
	public void onUserListMemberAddition(User addedMember, User listOwner, UserList list) {
		for (String name : paths) {
			for (ClientMessageListener listener : scheduler.getInternalListeners(name)) {
				listener.onUserListMemberAddition(addedMember, listOwner, list);
			}
		}
	}

	@Override
	public void onUserListMemberDeletion(User deletedMember, User listOwner, UserList list) {
		for (String name : paths) {
			for (ClientMessageListener listener : scheduler.getInternalListeners(name)) {
				listener.onUserListMemberDeletion(deletedMember, listOwner, list);
			}
		}
	}

	@Override
	public void onUserListSubscription(User subscriber, User listOwner, UserList list) {
		for (String name : paths) {
			for (ClientMessageListener listener : scheduler.getInternalListeners(name)) {
				listener.onUserListSubscription(subscriber, listOwner, list);
			}
		}
	}

	@Override
	public void onUserListUnsubscription(User subscriber, User listOwner, UserList list) {
		for (String name : paths) {
			for (ClientMessageListener listener : scheduler.getInternalListeners(name)) {
				listener.onUserListUnsubscription(subscriber, listOwner, list);
			}
		}
	}

	@Override
	public void onUserListUpdate(User listOwner, UserList list) {
		for (String name : paths) {
			for (ClientMessageListener listener : scheduler.getInternalListeners(name)) {
				listener.onUserListUpdate(listOwner, list);
			}
		}
	}

	@Override
	public void onUserProfileUpdate(User updatedUser) {
		for (String name : paths) {
			for (ClientMessageListener listener : scheduler.getInternalListeners(name)) {
				listener.onUserProfileUpdate(updatedUser);
			}
		}
	}
}

package jp.syuriken.snsw.twclient;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import jp.syuriken.snsw.twclient.filter.MessageFilter;
import twitter4j.DirectMessage;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.User;
import twitter4j.UserList;

/**
 * フィルターサービス。おそらくこのクラスはroot filterにしか使えません...
 *
 * <p>
 * このクラスでは登録されているフィルタが平等に呼び出されることはありません。
 * 登録順に呼び出され、該当関数がフィルターした場合 (falseまたはnullを返した時など) には以降の関数は呼び出されません。
 * なおこのクラスは並列的にフィルタを呼び出すため、フィルタのマルチスレッド対応が必要です。
 * @author Turenar <snswinhaiku dot lo at gmail dot com>
 */
public class FilterService implements ClientMessageListener {

	/**
	 * FilterDispatch元クラス。内部的に用いられる。
	 *
	 * @author Turenar <snswinhaiku dot lo at gmail dot com>
	 */
	public abstract class FilterDispatcher implements ParallelRunnable {

		/**
		 * 子フィルタを呼び出す
		 *
		 * @param messageFilter フィルタ
		 * @return 続行するときはtrue
		 */
		protected abstract boolean callDispatch(MessageFilter messageFilter);

		/**
		 * ディスパッチする。run()から呼び出されるだけ
		 */
		public void dispatch() {
			filterListLock.readLock().lock();
			boolean isFiltered = false;
			for (MessageFilter messageFilter : filterList) {
				isFiltered = callDispatch(messageFilter);
				if (isFiltered) {
					break;
				}
			}
			filterListLock.readLock().unlock();
			if (isFiltered == false) {
				tabsListLock.readLock().lock();
				for (ClientTab tab : tabsList) {
					notifyClientMessage(tab.getRenderer());
				}
				tabsListLock.readLock().unlock();
			}
		}

		/**
		 * ClientMessageListenerを呼び出すだけの関数。
		 *
		 * @param notifyListener ClientMessageListenerインスタンス
		 */
		protected abstract void notifyClientMessage(ClientMessageListener notifyListener);

		@Override
		public void run() {
			dispatch();
		}
	}


	/** 設定情報 */
	protected final ClientConfiguration configuration;

	/** フィルタを管理するためのリスト。使用前に必ず {@link #filterListLock} をreadないしwriteでロックすること */
	protected final ArrayList<MessageFilter> filterList;

	/** フィルタを管理する時に用いられるロック。 */
	protected final ReentrantReadWriteLock filterListLock;

	/**
	 * タブを管理するためのリスト。({@link ClientConfiguration#getFrameTabs()}のコピー;
	 * 使用前に必ず {@link #tabsListLock}をロックすること)
	 */
	protected final List<ClientTab> tabsList;

	/** タブを管理するためのリストロック */
	protected final ReentrantReadWriteLock tabsListLock;

	private CacheManager cacheManager;


	/**
	 * インスタンスを生成する。
	 *
	 * @param configuration 設定
	 */
	public FilterService(ClientConfiguration configuration) {
		this.configuration = configuration;
		cacheManager = configuration.getCacheManager();
		filterList = new ArrayList<MessageFilter>();
		filterListLock = new ReentrantReadWriteLock();
		tabsList = configuration.getFrameTabs();
		tabsListLock = configuration.getFrameTabsLock();
	}

	/**
	 * このクラスにフィルタを追加する。
	 *
	 * @param messageFilter フィルタ
	 */
	public void addFilter(MessageFilter messageFilter) {
		try {
			filterListLock.writeLock().lock();
			filterList.add(messageFilter);
		} finally {
			filterListLock.writeLock().unlock();
		}
	}

	/**
	 * ジョブキューに仕事を追加する (ラッパ)
	 *
	 * @param runnable 仕事
	 */
	protected void addJob(Runnable runnable) {
		configuration.getFrameApi().addJob(runnable);
	}

	/**
	 * JobQueueを利用してフィルターする。
	 *
	 * @param filterDispatcher フィルタ
	 */
	protected void filter(FilterDispatcher filterDispatcher) {
		addJob(filterDispatcher);
	}

	private Status getTwitterStatus(Status originalStatus) {
		if (originalStatus instanceof TwitterStatus) {
			return originalStatus;
		}

		Status cachedStatus = cacheManager.getCachedStatus(originalStatus.getId());
		if (cachedStatus == null) {
			Status status =
					(originalStatus instanceof TwitterStatus) ? originalStatus : new TwitterStatus(configuration,
							originalStatus);
			cachedStatus = cacheManager.cacheStatusIfAbsent(status);
			if (cachedStatus == null) {
				cachedStatus = status;
			}
		}
		return cachedStatus;
	}

	@Override
	public void onBlock(User source, User blockedUser) {

	}

	@Override
	public void onChangeAccount(final boolean forWrite) {
		filter(new FilterDispatcher() {

			@Override
			protected boolean callDispatch(MessageFilter messageFilter) {
				return messageFilter.onChangeAccount(forWrite);
			}

			@Override
			protected void notifyClientMessage(ClientMessageListener notifyListener) {
				notifyListener.onChangeAccount(forWrite);
			}
		});
	}

	@Override
	public void onCleanUp() {
		filter(new FilterDispatcher() {

			@Override
			protected boolean callDispatch(MessageFilter messageFilter) {
				return messageFilter.onStreamCleanUp();
			}

			@Override
			protected void notifyClientMessage(ClientMessageListener notifyListener) {
				notifyListener.onCleanUp();
			}
		});
	}

	@Override
	public void onClientMessage(final String name, final Object arg) {
		filter(new FilterDispatcher() {

			@Override
			protected boolean callDispatch(MessageFilter messageFilter) {
				return messageFilter.onClientMessage(name, arg);
			}

			@Override
			protected void notifyClientMessage(ClientMessageListener notifyListener) {
				notifyListener.onClientMessage(name, arg);
			}
		});
	}

	@Override
	public void onConnect() {
		filter(new FilterDispatcher() {

			@Override
			protected boolean callDispatch(MessageFilter messageFilter) {
				return messageFilter.onStreamConnect();
			}

			@Override
			protected void notifyClientMessage(ClientMessageListener notifyListener) {
				notifyListener.onConnect();
			}
		});
	}

	@Override
	public void onDeletionNotice(final long directMessageId, final long userId) {
		filter(new FilterDispatcher() {

			@Override
			protected boolean callDispatch(MessageFilter messageFilter) {
				return messageFilter.onDeletionNotice(directMessageId, userId);
			}

			@Override
			protected void notifyClientMessage(ClientMessageListener notifyListener) {
				notifyListener.onDeletionNotice(directMessageId, userId);
			}
		});
	}

	@Override
	public void onDeletionNotice(final StatusDeletionNotice statusDeletionNotice) {
		filter(new FilterDispatcher() {

			private StatusDeletionNotice obj = statusDeletionNotice;


			@Override
			protected boolean callDispatch(MessageFilter messageFilter) {
				obj = messageFilter.onDeletionNotice(obj);
				return obj == null;
			}

			@Override
			protected void notifyClientMessage(ClientMessageListener notifyListener) {
				notifyListener.onDeletionNotice(obj);
			}
		});
	}

	@Override
	public void onDirectMessage(final DirectMessage directMessage) {
		filter(new FilterDispatcher() {

			private DirectMessage message = directMessage;


			@Override
			protected boolean callDispatch(MessageFilter messageFilter) {
				message = messageFilter.onDirectMessage(message);
				return message == null;
			}

			@Override
			protected void notifyClientMessage(ClientMessageListener notifyListener) {
				notifyListener.onDirectMessage(message);
			}
		});
	}

	@Override
	public void onDisconnect() {
		filter(new FilterDispatcher() {

			@Override
			protected boolean callDispatch(MessageFilter messageFilter) {
				return messageFilter.onStreamDisconnect();
			}

			@Override
			protected void notifyClientMessage(ClientMessageListener notifyListener) {
				notifyListener.onDisconnect();
			}
		});
	}

	@Override
	public void onException(final Exception ex) {
		filter(new FilterDispatcher() {

			private Exception obj = ex;


			@Override
			protected boolean callDispatch(MessageFilter messageFilter) {
				return messageFilter.onException(obj);
			}

			@Override
			protected void notifyClientMessage(ClientMessageListener notifyListener) {
				notifyListener.onException(obj);
			}
		});
	}

	@Override
	public void onFavorite(final User source, final User target, final Status favoritedStatus) {
		filter(new FilterDispatcher() {

			@Override
			protected boolean callDispatch(MessageFilter messageFilter) {
				return messageFilter.onFavorite(source, target, favoritedStatus);
			}

			@Override
			protected void notifyClientMessage(ClientMessageListener notifyListener) {
				notifyListener.onFavorite(source, target, favoritedStatus);
			}
		});
	}

	@Override
	public void onFollow(final User source, final User followedUser) {
		filter(new FilterDispatcher() {

			@Override
			protected boolean callDispatch(MessageFilter messageFilter) {
				return messageFilter.onFollow(source, followedUser);
			}

			@Override
			protected void notifyClientMessage(ClientMessageListener notifyListener) {
				notifyListener.onFollow(source, followedUser);
			}
		});
	}

	@Override
	public void onFriendList(final long[] friendIds) {
		filter(new FilterDispatcher() {

			long[] arr = friendIds;


			@Override
			protected boolean callDispatch(MessageFilter messageFilter) {
				arr = messageFilter.onFriendList(arr);
				return arr == null || arr.length == 0;
			}

			@Override
			protected void notifyClientMessage(ClientMessageListener notifyListener) {
				notifyListener.onFriendList(arr);
			}
		});
	}

	@Override
	public void onRetweet(final User source, final User target, final Status retweetedStatus) {
		filter(new FilterDispatcher() {

			@Override
			protected boolean callDispatch(MessageFilter messageFilter) {
				return messageFilter.onRetweet(source, target, retweetedStatus);
			}

			@Override
			protected void notifyClientMessage(ClientMessageListener notifyListener) {
				notifyListener.onRetweet(source, target, retweetedStatus);
			}
		});
	}

	@Override
	public void onScrubGeo(final long userId, final long upToStatusId) {
		filter(new FilterDispatcher() {

			@Override
			protected boolean callDispatch(MessageFilter messageFilter) {
				return messageFilter.onScrubGeo(userId, upToStatusId);
			}

			@Override
			protected void notifyClientMessage(ClientMessageListener notifyListener) {
				notifyListener.onScrubGeo(userId, upToStatusId);
			}
		});
	}

	@Override
	public void onStallWarning(final StallWarning warning) {
		filter(new FilterDispatcher() {

			@Override
			protected boolean callDispatch(MessageFilter messageFilter) {
				return messageFilter.onStallWarning(warning);
			}

			@Override
			protected void notifyClientMessage(ClientMessageListener notifyListener) {
				notifyListener.onStallWarning(warning);
			}
		});
	}

	@Override
	public void onStatus(Status originalStatus) {
		final Status status = getTwitterStatus(originalStatus);

		filter(new FilterDispatcher() {

			private Status obj = status;


			@Override
			protected boolean callDispatch(MessageFilter messageFilter) {
				obj = messageFilter.onStatus(obj);
				return obj == null;
			}

			@Override
			protected void notifyClientMessage(ClientMessageListener notifyListener) {
				notifyListener.onStatus(obj);
			}
		});
	}

	@Override
	public void onTrackLimitationNotice(final int numberOfLimitedStatuses) {
		filter(new FilterDispatcher() {

			@Override
			protected boolean callDispatch(MessageFilter messageFilter) {
				return messageFilter.onTrackLimitationNotice(numberOfLimitedStatuses);
			}

			@Override
			protected void notifyClientMessage(ClientMessageListener notifyListener) {
				notifyListener.onTrackLimitationNotice(numberOfLimitedStatuses);
			}
		});
	}

	@Override
	public void onUnblock(final User source, final User unblockedUser) {
		filter(new FilterDispatcher() {

			@Override
			protected boolean callDispatch(MessageFilter messageFilter) {
				return messageFilter.onUnblock(source, unblockedUser);
			}

			@Override
			protected void notifyClientMessage(ClientMessageListener notifyListener) {
				notifyListener.onUnblock(source, unblockedUser);
			}
		});
	}

	@Override
	public void onUnfavorite(final User source, final User target, final Status unfavoritedStatus) {
		filter(new FilterDispatcher() {

			@Override
			protected boolean callDispatch(MessageFilter messageFilter) {
				return messageFilter.onUnfavorite(source, target, unfavoritedStatus);
			}

			@Override
			protected void notifyClientMessage(ClientMessageListener notifyListener) {
				notifyListener.onUnfavorite(source, target, unfavoritedStatus);
			}
		});
	}

	@Override
	public void onUserListCreation(final User listOwner, final UserList list) {
		filter(new FilterDispatcher() {

			@Override
			protected boolean callDispatch(MessageFilter messageFilter) {
				return messageFilter.onUserListCreation(listOwner, list);
			}

			@Override
			protected void notifyClientMessage(ClientMessageListener notifyListener) {
				notifyListener.onUserListCreation(listOwner, list);
			}
		});
	}

	@Override
	public void onUserListDeletion(final User listOwner, final UserList list) {
		filter(new FilterDispatcher() {

			@Override
			protected boolean callDispatch(MessageFilter messageFilter) {
				return messageFilter.onUserListDeletion(listOwner, list);
			}

			@Override
			protected void notifyClientMessage(ClientMessageListener notifyListener) {
				notifyListener.onUserListDeletion(listOwner, list);
			}
		});
	}

	@Override
	public void onUserListMemberAddition(final User addedMember, final User listOwner, final UserList list) {
		filter(new FilterDispatcher() {

			@Override
			protected boolean callDispatch(MessageFilter messageFilter) {
				return messageFilter.onUserListMemberAddition(addedMember, listOwner, list);
			}

			@Override
			protected void notifyClientMessage(ClientMessageListener notifyListener) {
				notifyListener.onUserListMemberAddition(addedMember, listOwner, list);
			}
		});
	}

	@Override
	public void onUserListMemberDeletion(final User deletedMember, final User listOwner, final UserList list) {
		filter(new FilterDispatcher() {

			@Override
			protected boolean callDispatch(MessageFilter messageFilter) {
				return messageFilter.onUserListMemberDeletion(deletedMember, listOwner, list);
			}

			@Override
			protected void notifyClientMessage(ClientMessageListener notifyListener) {
				notifyListener.onUserListMemberDeletion(deletedMember, listOwner, list);
			}
		});
	}

	@Override
	public void onUserListSubscription(final User subscriber, final User listOwner, final UserList list) {
		filter(new FilterDispatcher() {

			@Override
			protected boolean callDispatch(MessageFilter messageFilter) {
				return messageFilter.onUserListSubscription(subscriber, listOwner, list);
			}

			@Override
			protected void notifyClientMessage(ClientMessageListener notifyListener) {
				notifyListener.onUserListSubscription(subscriber, listOwner, list);
			}
		});
	}

	@Override
	public void onUserListUnsubscription(final User subscriber, final User listOwner, final UserList list) {
		filter(new FilterDispatcher() {

			@Override
			protected boolean callDispatch(MessageFilter messageFilter) {
				return messageFilter.onUserListUnsubscription(subscriber, listOwner, list);
			}

			@Override
			protected void notifyClientMessage(ClientMessageListener notifyListener) {
				notifyListener.onUserListUnsubscription(subscriber, listOwner, list);
			}
		});
	}

	@Override
	public void onUserListUpdate(final User listOwner, final UserList list) {
		filter(new FilterDispatcher() {

			@Override
			protected boolean callDispatch(MessageFilter messageFilter) {
				return messageFilter.onUserListUpdate(listOwner, list);
			}

			@Override
			protected void notifyClientMessage(ClientMessageListener notifyListener) {
				notifyListener.onUserListUpdate(listOwner, list);
			}
		});
	}

	@Override
	public void onUserProfileUpdate(final User updatedUser) {
		filter(new FilterDispatcher() {

			@Override
			protected boolean callDispatch(MessageFilter messageFilter) {
				return messageFilter.onUserProfileUpdate(updatedUser);
			}

			@Override
			protected void notifyClientMessage(ClientMessageListener notifyListener) {
				notifyListener.onUserProfileUpdate(updatedUser);
			}
		});
	}
}

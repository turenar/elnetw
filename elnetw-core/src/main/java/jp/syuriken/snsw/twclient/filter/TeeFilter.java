package jp.syuriken.snsw.twclient.filter;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.TreeSet;

import jp.syuriken.snsw.twclient.ClientConfiguration;
import jp.syuriken.snsw.twclient.ClientProperties;
import jp.syuriken.snsw.twclient.gui.TabRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.DirectMessage;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.User;
import twitter4j.UserList;

/**
 * 入力 → {@link FilterDispatcherBase} → {@link TabRenderer} とするユーティリティークラス。
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class TeeFilter implements TabRenderer, PropertyChangeListener {

	private static final Logger logger = LoggerFactory.getLogger(TeeFilter.class);

	private final String filterPropertyName;

	private final ClientProperties configProperties;

	private final MessageFilter[] globalFilters;

	private FilterDispatcherBase filterQuery;

	private TabRenderer renderer;

	private ClientConfiguration configuration;
	/**
	 * インスタンスを生成する。グローバルフィルタを使用する。
	 * @param uniqId ユニークなID
	 * @param tabRenderer 移譲先レンダラ
	 */
	public TeeFilter(String uniqId, TabRenderer tabRenderer) {
		this(uniqId, tabRenderer, true);
	}

	/**
	 * インスタンスを生成する。
	 * @param uniqId ユニークなID
	 * @param tabRenderer 移譲先レンダラ
	 * @param useGlobalFilter グローバルフィルタを使用するかどうか
	 */
	public TeeFilter(String uniqId, TabRenderer tabRenderer, boolean useGlobalFilter) {
		configuration = ClientConfiguration.getInstance();
		renderer = tabRenderer;
		configProperties = configuration.getConfigProperties();
		filterPropertyName = "core.filter._tabs." + uniqId;
		initQuery();
		configProperties.addPropertyChangedListener(this);
		if (useGlobalFilter) {
			globalFilters = configuration.getFilters();
		} else {
			globalFilters = new MessageFilter[0];
		}
	}

	/**
	 * 初期化
	 *
	 * @throws IllegalSyntaxException 正しくない文法のためエラー
	 */
	protected void initQuery() {
		String filterQueryString = configProperties.getProperty(filterPropertyName);
		if (filterQueryString == null || filterQueryString.trim().isEmpty()) {
			filterQuery = NullFilter.getInstance();
		} else {
			try {
				filterQuery = FilterCompiler.getCompiledObject(configuration, filterQueryString);
			} catch (IllegalSyntaxException e) {
				logger.error("Illegal syntax filter query", e);
				filterQuery = NullFilter.getInstance();
			}
		}
	}

	@Override
	public void onBlock(User source, User blockedUser) {
		for (MessageFilter filter : globalFilters) {
			if (filter.onBlock(source, blockedUser)) {
				return;
			}
		}

		renderer.onBlock(source, blockedUser);
	}

	@Override
	public void onChangeAccount(boolean forWrite) {
		for (MessageFilter filter : globalFilters) {
			if (filter.onChangeAccount(forWrite)) {
				return;
			}
		}
		renderer.onChangeAccount(forWrite);
	}

	@Override
	public void onCleanUp() {
		for (MessageFilter filter : globalFilters) {
			if (filter.onCleanUp()) {
				return;
			}
		}
		renderer.onCleanUp();
	}

	@Override
	public void onClientMessage(String name, Object arg) {
		for (MessageFilter filter : globalFilters) {
			if (filter.onClientMessage(name, arg)) {
				return;
			}
		}
		renderer.onClientMessage(name, arg);
	}

	@Override
	public void onConnect() {
		for (MessageFilter filter : globalFilters) {
			if (filter.onConnect()) {
				return;
			}
		}
		renderer.onConnect();
	}

	@Override
	public void onDeletionNotice(long directMessageId, long userId) {
		for (MessageFilter filter : globalFilters) {
			if (filter.onDeletionNotice(directMessageId, userId)) {
				return;
			}
		}
		renderer.onDeletionNotice(directMessageId, userId);
	}

	@Override
	public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
		for (MessageFilter filter : globalFilters) {
			if ((statusDeletionNotice = filter.onDeletionNotice(statusDeletionNotice)) == null) {
				return;
			}
		}
		renderer.onDeletionNotice(statusDeletionNotice);
	}

	@Override
	public void onDirectMessage(DirectMessage directMessage) {
		for (MessageFilter filter : globalFilters) {
			if ((directMessage = filter.onDirectMessage(directMessage)) == null) {
				return;
			}
		}
		if (filterQuery.filter(directMessage) == false) {
			renderer.onDirectMessage(directMessage);
		}
	}

	@Override
	public void onDisconnect() {
		for (MessageFilter filter : globalFilters) {
			if (filter.onDisconnect()) {
				return;
			}
		}
		renderer.onDisconnect();
	}

	@Override
	public void onException(Exception ex) {
		for (MessageFilter filter : globalFilters) {
			if (filter.onException(ex)) {
				return;
			}
		}
		renderer.onException(ex);
	}

	@Override
	public void onFavorite(User source, User target, Status favoritedStatus) {
		for (MessageFilter filter : globalFilters) {
			if (filter.onFavorite(source, target, favoritedStatus)) {
				return;
			}
		}
		if (filterQuery.filter(favoritedStatus) == false) {
			renderer.onFavorite(source, target, favoritedStatus);
		}
	}

	@Override
	public void onFollow(User source, User followedUser) {
		for (MessageFilter filter : globalFilters) {
			if (filter.onFollow(source, followedUser)) {
				return;
			}
		}
		renderer.onFollow(source, followedUser);
	}

	@Override
	public void onFriendList(long[] friendIds) {
		for (MessageFilter filter : globalFilters) {
			if ((friendIds = filter.onFriendList(friendIds)) == null) {
				return;
			}
		}
		renderer.onFriendList(friendIds);
	}

	@Override
	public void onScrubGeo(long userId, long upToStatusId) {
		for (MessageFilter filter : globalFilters) {
			if (filter.onScrubGeo(userId, upToStatusId)) {
				return;
			}
		}
		renderer.onScrubGeo(userId, upToStatusId);
	}

	@Override
	public void onStallWarning(StallWarning warning) {
		for (MessageFilter filter : globalFilters) {
			if (filter.onStallWarning(warning)) {
				return;
			}
		}
		renderer.onStallWarning(warning);
	}

	@Override
	public void onStatus(Status status) {
		synchronized (statusSet) {
			if (statusSet.contains(status.getId())) {
				return;
			} else {
				// TODO
				//status = status.isRetweet() ? status.getRetweetedStatus() : status;
				statusSet.add(status.getId());
			}
		}

		for (MessageFilter filter : globalFilters) {
			if ((status = filter.onStatus(status)) == null) {
				return;
			}
		}
		if (filterQuery.filter(status) == false) {
			renderer.onStatus(status);
		}
	}

	@Override
	public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
		for (MessageFilter filter : globalFilters) {
			if (filter.onTrackLimitationNotice(numberOfLimitedStatuses)) {
				return;
			}
		}
		renderer.onTrackLimitationNotice(numberOfLimitedStatuses);
	}

	@Override
	public void onUnblock(User source, User unblockedUser) {
		for (MessageFilter filter : globalFilters) {
			if (filter.onUnblock(source, unblockedUser)) {
				return;
			}
		}
		renderer.onUnblock(source, unblockedUser);
	}

	@Override
	public void onUnfavorite(User source, User target, Status unfavoritedStatus) {
		for (MessageFilter filter : globalFilters) {
			if (filter.onUnfavorite(source, target, unfavoritedStatus)) {
				return;
			}
		}
		if (filterQuery.filter(unfavoritedStatus) == false) {
			renderer.onUnfavorite(source, target, unfavoritedStatus);
		}
	}

	@Override
	public void onUserListCreation(User listOwner, UserList list) {
		for (MessageFilter filter : globalFilters) {
			if (filter.onUserListCreation(listOwner, list)) {
				return;
			}
		}
		renderer.onUserListCreation(listOwner, list);
	}

	@Override
	public void onUserListDeletion(User listOwner, UserList list) {
		for (MessageFilter filter : globalFilters) {
			if (filter.onUserListDeletion(listOwner, list)) {
				return;
			}
		}
		renderer.onUserListDeletion(listOwner, list);
	}

	@Override
	public void onUserListMemberAddition(User addedMember, User listOwner, UserList list) {
		for (MessageFilter filter : globalFilters) {
			if (filter.onUserListMemberAddition(addedMember, listOwner, list)) {
				return;
			}
		}
		renderer.onUserListMemberAddition(addedMember, listOwner, list);
	}

	@Override
	public void onUserListMemberDeletion(User deletedMember, User listOwner, UserList list) {
		for (MessageFilter filter : globalFilters) {
			if (filter.onUserListMemberDeletion(deletedMember, listOwner, list)) {
				return;
			}
		}
		renderer.onUserListMemberDeletion(deletedMember, listOwner, list);
	}

	@Override
	public void onUserListSubscription(User subscriber, User listOwner, UserList list) {
		for (MessageFilter filter : globalFilters) {
			if (filter.onUserListSubscription(subscriber, listOwner, list)) {
				return;
			}
		}
		renderer.onUserListSubscription(subscriber, listOwner, list);
	}

	@Override
	public void onUserListUnsubscription(User subscriber, User listOwner, UserList list) {
		for (MessageFilter filter : globalFilters) {
			if (filter.onUserListUnsubscription(subscriber, listOwner, list)) {
				return;
			}
		}
		renderer.onUserListUnsubscription(subscriber, listOwner, list);
	}

	@Override
	public void onUserListUpdate(User listOwner, UserList list) {
		for (MessageFilter filter : globalFilters) {
			if (filter.onUserListUpdate(listOwner, list)) {
				return;
			}
		}
		renderer.onUserListUpdate(listOwner, list);
	}
	private TreeSet<Long> statusSet=new TreeSet<>();
	@Override
	public void onUserProfileUpdate(User updatedUser) {
		for (MessageFilter filter : globalFilters) {
			if (filter.onUserProfileUpdate(updatedUser)) {
				return;
			}
		}
		renderer.onUserProfileUpdate(updatedUser);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		String propertyName = evt.getPropertyName();
		if (propertyName.equals(filterPropertyName)) {
			initQuery();
		}
	}
}

package jp.syuriken.snsw.twclient.filter;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import jp.syuriken.snsw.twclient.ClientConfiguration;
import jp.syuriken.snsw.twclient.ClientProperties;
import jp.syuriken.snsw.twclient.TabRenderer;
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

	private FilterDispatcherBase filterQuery;

	private TabRenderer renderer;

	private ClientConfiguration configuration;


	/**
	 * インスタンスを生成する。
	 * @param configuration 設定
	 * @param uniqId ユニークなID
	 * @param tabRenderer 移譲先レンダラ
	 * @throws IllegalSyntaxException 正しくない文法のクエリ
	 */
	public TeeFilter(ClientConfiguration configuration, String uniqId, TabRenderer tabRenderer)
			throws IllegalSyntaxException {
		this.configuration = configuration;
		renderer = tabRenderer;
		configProperties = configuration.getConfigProperties();
		filterPropertyName = "core.filter._tabs." + uniqId;
		init();
		configProperties.addPropertyChangedListener(this);
	}

	/**
	 * 初期化
	 *
	 * @throws IllegalSyntaxException 正しくない文法のためエラー
	 */
	protected void init() throws IllegalSyntaxException {
		String filterQueryString = configProperties.getProperty(filterPropertyName);
		if (filterQueryString == null || filterQueryString.trim().isEmpty()) {
			filterQuery = NullFilter.getInstance();
		} else {
			filterQuery = FilterCompiler.getCompiledObject(configuration, filterQueryString);
		}
	}

	@Override
	public void onBlock(User source, User blockedUser) {
		renderer.onBlock(source, blockedUser);
	}

	@Override
	public void onChangeAccount(boolean forWrite) {
		renderer.onChangeAccount(forWrite);
	}

	@Override
	public void onCleanUp() {
		renderer.onCleanUp();
	}

	@Override
	public void onClientMessage(String name, Object arg) {
		renderer.onClientMessage(name, arg);
	}

	@Override
	public void onConnect() {
		renderer.onConnect();
	}

	@Override
	public void onDeletionNotice(long directMessageId, long userId) {
		renderer.onDeletionNotice(directMessageId, userId);
	}

	@Override
	public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
		renderer.onDeletionNotice(statusDeletionNotice);
	}

	@Override
	public void onDirectMessage(DirectMessage directMessage) {
		if (filterQuery.filter(directMessage) == false) {
			renderer.onDirectMessage(directMessage);
		}
	}

	@Override
	public void onDisconnect() {
		renderer.onDisconnect();
	}

	@Override
	public void onException(Exception ex) {
		renderer.onException(ex);
	}

	@Override
	public void onFavorite(User source, User target, Status favoritedStatus) {
		if (filterQuery.filter(favoritedStatus) == false) {
			renderer.onFavorite(source, target, favoritedStatus);
		}
	}

	@Override
	public void onFollow(User source, User followedUser) {
		renderer.onFollow(source, followedUser);
	}

	@Override
	public void onFriendList(long[] friendIds) {
		renderer.onFriendList(friendIds);
	}

	@Override
	public void onScrubGeo(long userId, long upToStatusId) {
		renderer.onScrubGeo(userId, upToStatusId);
	}

	@Override
	public void onStallWarning(StallWarning warning) {
		renderer.onStallWarning(warning);
	}

	@Override
	public void onStatus(Status status) {
		if (filterQuery.filter(status) == false) {
			renderer.onStatus(status);
		}
	}

	@Override
	public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
		renderer.onTrackLimitationNotice(numberOfLimitedStatuses);
	}

	@Override
	public void onUnblock(User source, User unblockedUser) {
		renderer.onUnblock(source, unblockedUser);
	}

	@Override
	public void onUnfavorite(User source, User target, Status unfavoritedStatus) {
		if (filterQuery.filter(unfavoritedStatus) == false) {
			renderer.onUnfavorite(source, target, unfavoritedStatus);
		}
	}

	@Override
	public void onUserListCreation(User listOwner, UserList list) {
		renderer.onUserListCreation(listOwner, list);
	}

	@Override
	public void onUserListDeletion(User listOwner, UserList list) {
		renderer.onUserListDeletion(listOwner, list);
	}

	@Override
	public void onUserListMemberAddition(User addedMember, User listOwner, UserList list) {
		renderer.onUserListMemberAddition(addedMember, listOwner, list);
	}

	@Override
	public void onUserListMemberDeletion(User deletedMember, User listOwner, UserList list) {
		renderer.onUserListMemberDeletion(deletedMember, listOwner, list);
	}

	@Override
	public void onUserListSubscription(User subscriber, User listOwner, UserList list) {
		renderer.onUserListSubscription(subscriber, listOwner, list);
	}

	@Override
	public void onUserListUnsubscription(User subscriber, User listOwner, UserList list) {
		renderer.onUserListUnsubscription(subscriber, listOwner, list);
	}

	@Override
	public void onUserListUpdate(User listOwner, UserList list) {
		renderer.onUserListUpdate(listOwner, list);
	}

	@Override
	public void onUserProfileUpdate(User updatedUser) {
		renderer.onUserProfileUpdate(updatedUser);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		String propertyName = evt.getPropertyName();
		if (propertyName.equals(filterPropertyName)) {
			try {
				init();
			} catch (IllegalSyntaxException e) {
				logger.warn("正しくない文法のためフィルタを更新できません: " + propertyName, e);
			}
		}
	}
}

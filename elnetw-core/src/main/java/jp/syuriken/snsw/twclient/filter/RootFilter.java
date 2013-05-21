package jp.syuriken.snsw.twclient.filter;

import java.text.MessageFormat;
import java.util.TreeSet;

import jp.syuriken.snsw.twclient.CacheManager;
import jp.syuriken.snsw.twclient.ClientConfiguration;
import jp.syuriken.snsw.twclient.ImageCacher;
import jp.syuriken.snsw.twclient.TwitterStatus;
import jp.syuriken.snsw.twclient.TwitterUser;
import jp.syuriken.snsw.twclient.internal.InitialMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.DirectMessage;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.User;
import twitter4j.UserList;

/**
 * ごにょごにょ用のフィルターハンドラー。キャッシュとか通知の作成とか
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class RootFilter implements MessageFilter {

	private final ClientConfiguration configuration;

	private TreeSet<Long> statusSet;

	private ImageCacher imageCacher;

	private Logger logger = LoggerFactory.getLogger(RootFilter.class);

	private CacheManager cacheManager;


	/**
	 * インスタンスを生成する。
	 * @param configuration 設定
	 *
	 */
	public RootFilter(ClientConfiguration configuration) {
		this.configuration = configuration;
		imageCacher = configuration.getImageCacher();
		cacheManager = configuration.getCacheManager();
		statusSet = new TreeSet<Long>();
	}

	private User getUser(User originalUser) {
		if (originalUser instanceof TwitterUser) {
			return originalUser;
		}

		User cachedUser = cacheManager.getCachedUser(originalUser.getId());
		if (cachedUser == null) {
			User user = new TwitterUser(configuration, originalUser);
			cachedUser = cacheManager.cacheUserIfAbsent(user);
			if (cachedUser == null) {
				cachedUser = user;
			}
		}
		return cachedUser;
	}

	@Override
	public boolean onChangeAccount(boolean forWrite) {
		configuration.getFetchScheduler().onChangeAccount(forWrite);
		return false;
	}

	@Override
	public boolean onClientMessage(String name, Object arg) {
		return false;
	}

	@Override
	public boolean onDeletionNotice(long directMessageId, long userId) {
		return false;
	}

	@Override
	public StatusDeletionNotice onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
		cacheManager.removeCachedStatus(statusDeletionNotice.getStatusId());
		return statusDeletionNotice;
	}

	@Override
	public DirectMessage onDirectMessage(DirectMessage message) {
		if (message instanceof InitialMessage == false) {
			User sender = message.getSender();
			configuration
				.getFrameApi()
				.getUtility()
				.sendNotify(MessageFormat.format("{0} ({1})", sender.getScreenName(), sender.getName()),
						"DMを受信しました：" + message.getText(), imageCacher.getImageFile(sender));
		}
		return message;
	}

	@Override
	public boolean onException(Exception ex) {
		logger.warn("handling onException", ex);
		return false;
	}

	@Override
	public boolean onFavorite(User source, User target, Status favoritedStatus) {
		return false;
	}

	@Override
	public boolean onFollow(User source, User followedUser) {
		return false;
	}

	@Override
	public long[] onFriendList(long[] userIds) {
		for (long userId : userIds) {
			cacheManager.queueFetchingUser(userId);
		}
		return userIds;
	}

	@Override
	public boolean onRetweet(User source, User target, Status retweetedStatus) {
		return false;
	}

	@Override
	public boolean onScrubGeo(long userId, long upToStatusId) {
		return false;
	}

	@Override
	public boolean onStallWarning(StallWarning warning) {
		return false;
	}

	@Override
	public Status onStatus(Status originalStatus) {
		Status status;
		synchronized (statusSet) {
			if (statusSet.contains(originalStatus.getId())) {
				return null;
			} else {
				status = originalStatus.isRetweet() ? originalStatus.getRetweetedStatus() : originalStatus;
				statusSet.add(originalStatus.getId());
			}
		}

		if ((status instanceof TwitterStatus ? ((TwitterStatus) status).isLoadedInitialization() == false
				: true) && configuration.isMentioned(status.getUserMentionEntities())) {
			configuration.getUtility().sendNotify(originalStatus.getUser().getName(), originalStatus.getText(),
					imageCacher.getImageFile(originalStatus.getUser()));
		}
		return originalStatus;
	}

	@Override
	public boolean onStreamCleanUp() {
		return false;
	}

	@Override
	public boolean onStreamConnect() {
		return false;
	}

	@Override
	public boolean onStreamDisconnect() {
		return false;
	}

	@Override
	public boolean onTrackLimitationNotice(int numberOfLimitedStatuses) {
		return false;
	}

	@Override
	public boolean onUnblock(User source, User unblockedUser) {
		return false;
	}

	@Override
	public boolean onUnfavorite(User source, User target, Status unfavoritedStatus) {
		return false;
	}

	@Override
	public boolean onUserListCreation(User listOwner, UserList list) {
		return false;
	}

	@Override
	public boolean onUserListDeletion(User listOwner, UserList list) {
		return false;
	}

	@Override
	public boolean onUserListMemberAddition(User addedMember, User listOwner, UserList list) {
		return false;
	}

	@Override
	public boolean onUserListMemberDeletion(User deletedMember, User listOwner, UserList list) {
		return false;
	}

	@Override
	public boolean onUserListSubscription(User subscriber, User listOwner, UserList list) {
		return false;
	}

	@Override
	public boolean onUserListUnsubscription(User subscriber, User listOwner, UserList list) {
		return false;
	}

	@Override
	public boolean onUserListUpdate(User listOwner, UserList list) {
		return false;
	}

	@Override
	public boolean onUserProfileUpdate(User updatedUser) {
		User cachedUser = cacheManager.getCachedUser(updatedUser.getId());
		if (cachedUser == null) {
			cacheManager.cacheUserIfAbsent(getUser(updatedUser));
		} else {
			if (cachedUser instanceof TwitterUser) {
				((TwitterUser) cachedUser).updateUser(updatedUser);
			}
		}
		return false;
	}
}

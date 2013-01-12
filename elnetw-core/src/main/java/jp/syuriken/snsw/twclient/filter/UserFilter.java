package jp.syuriken.snsw.twclient.filter;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.TreeSet;

import jp.syuriken.snsw.twclient.ClientConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.DirectMessage;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.User;
import twitter4j.UserList;
import twitter4j.UserMentionEntity;

/**
 * ユーザー設定によりフィルタを行うフィルタクラス
 *
 * @author Turenar <snswinhaiku dot lo at gmail dot com>
 */
public class UserFilter extends MessageFilterAdapter implements PropertyChangeListener {

	private static final String PROPERTY_KEY_FILTER_GLOBAL_QUERY = "core.filter._global";

	private static final String PROPERTY_KEY_FILTER_IDS = "core.filter.user.ids";

	private TreeSet<Long> filterIds;

	private final ClientConfiguration configuration;

	private final Logger logger = LoggerFactory.getLogger(UserFilter.class);

	private FilterDispatcherBase query;


	/**
	* インスタンスを生成する。
	*
	* @param configuration 設定
	*/
	public UserFilter(ClientConfiguration configuration) {
		this.configuration = configuration;
		configuration.getConfigBuilder().getGroup("フィルタ")
			.addConfig("<ignore>", "フィルタの編集", "", new FilterConfigurator(configuration));
		configuration.getConfigProperties().addPropertyChangedListener(this);
		filterIds = new TreeSet<Long>();
		initFilterIds();
		initFilterQueries();
	}

	private boolean filterUser(long userId) {
		return filterIds.contains(userId);
	}

	private boolean filterUser(Status status) {
		return filterUser(status.getUser());
	}

	private boolean filterUser(User user) {
		return filterUser(user.getId());
	}

	private void initFilterIds() {
		String idsString = configuration.getConfigProperties().getProperty(PROPERTY_KEY_FILTER_IDS);
		if (idsString == null) {
			return;
		}
		for (int offset = 0; offset < idsString.length();) {
			int end = idsString.indexOf(' ', offset);
			if (end < 0) {
				end = idsString.length();
			}
			String idString = idsString.substring(offset, end);
			try {
				filterIds.add(Long.parseLong(idString));
			} catch (NumberFormatException e) {
				logger.warn("filterIdsの読み込み中にエラー: {} は数値ではありません", idString);
			}
			offset = end + 1;
		}
	}

	private void initFilterQueries() {
		String query = configuration.getConfigProperties().getProperty(PROPERTY_KEY_FILTER_GLOBAL_QUERY);
		if (query == null || query.trim().isEmpty()) {
			this.query = NullFilter.getInstance();
		} else {
			try {
				this.query = FilterCompiler.getCompiledObject(configuration, query);
			} catch (IllegalSyntaxException e) {
				logger.warn("#initFilterQueries()", e);
			}
		}
	}

	@Override
	public boolean onDeletionNotice(long directMessageId, long userId) {
		return filterUser(userId);
	}

	@Override
	public StatusDeletionNotice onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
		return filterUser(statusDeletionNotice.getUserId()) ? null : statusDeletionNotice;
	}

	@Override
	public DirectMessage onDirectMessage(DirectMessage message) {
		boolean filtered;
		filtered = filterUser(message.getSenderId());
		if (filtered == false) {
			filtered = filterUser(message.getRecipientId());
		}
		if (filtered == false) {
			filtered = query.filter(message);
		}
		return filtered ? null : message;
	}

	@Override
	public boolean onFavorite(User source, User target, Status favoritedStatus) {
		boolean filtered;
		filtered = filterUser(source);
		if (filtered == false) {
			filtered = filterUser(target);
		}
		if (filtered == false) {
			filtered = onStatus(favoritedStatus) == null;
		}
		return filtered;
	}

	@Override
	public boolean onFollow(User source, User followedUser) {
		boolean filtered;
		filtered = filterUser(source);
		if (filtered == false) {
			filtered = filterUser(followedUser);
		}
		return filtered;
	}

	@Override
	public boolean onRetweet(User source, User target, Status retweetedStatus) {
		boolean filtered;
		filtered = filterUser(source);
		if (filtered == false) {
			filtered = filterUser(target);
		}
		if (filtered == false) {
			filtered = onStatus(retweetedStatus) == null;
		}
		if (filtered == false) {
			filtered = query.filter(retweetedStatus);
		}
		return filtered;
	}

	@Override
	public boolean onScrubGeo(long userId, long upToStatusId) {
		return filterUser(userId);
	}

	@Override
	public Status onStatus(Status status) {
		boolean filtered;
		filtered = filterUser(status);
		if (filtered == false && status.isRetweet()) {
			filtered = onStatus(status.getRetweetedStatus()) == null;
		}
		if (filtered == false && status.getInReplyToUserId() != -1) {
			filtered = filterUser(status.getInReplyToUserId());
		}
		UserMentionEntity[] userMentionEntities = status.getUserMentionEntities();
		if (filtered == false && userMentionEntities != null) {
			final int length = userMentionEntities.length;
			for (int i = 0; filtered == false && i < length; i++) {
				filtered = filterUser(userMentionEntities[i].getId());
			}
		}
		if (filtered == false) {
			filtered = query.filter(status);
		}
		return filtered ? null : status;
	}

	@Override
	public boolean onUnblock(User source, User unblockedUser) {
		boolean filtered;
		filtered = filterUser(source);
		if (filtered == false) {
			filtered = filterUser(unblockedUser);
		}
		return filtered;
	}

	@Override
	public boolean onUnfavorite(User source, User target, Status unfavoritedStatus) {
		boolean filtered;
		filtered = filterUser(source);
		if (filtered == false) {
			filtered = filterUser(target);
		}
		if (filtered == false) {
			filtered = onStatus(unfavoritedStatus) == null;
		}
		return filtered;
	}

	@Override
	public boolean onUserListCreation(User listOwner, UserList list) {
		return filterUser(listOwner);
	}

	@Override
	public boolean onUserListDeletion(User listOwner, UserList list) {
		return filterUser(listOwner);
	}

	@Override
	public boolean onUserListMemberAddition(User addedMember, User listOwner, UserList list) {
		boolean filtered;
		filtered = filterUser(addedMember);
		if (filtered == false) {
			filtered = filterUser(listOwner);
		}
		return filtered;
	}

	@Override
	public boolean onUserListMemberDeletion(User deletedMember, User listOwner, UserList list) {
		boolean filtered;
		filtered = filterUser(deletedMember);
		if (filtered == false) {
			filtered = filterUser(listOwner);
		}
		return filtered;
	}

	@Override
	public boolean onUserListSubscription(User subscriber, User listOwner, UserList list) {
		boolean filtered;
		filtered = filterUser(subscriber);
		if (filtered == false) {
			filtered = filterUser(listOwner);
		}
		return filtered;
	}

	@Override
	public boolean onUserListUnsubscription(User subscriber, User listOwner, UserList list) {
		boolean filtered;
		filtered = filterUser(subscriber);
		if (filtered == false) {
			filtered = filterUser(listOwner);
		}
		return filtered;
	}

	@Override
	public boolean onUserListUpdate(User listOwner, UserList list) {
		return filterUser(listOwner);
	}

	@Override
	public boolean onUserProfileUpdate(User updatedUser) {
		return filterUser(updatedUser);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals(PROPERTY_KEY_FILTER_IDS)) {
			initFilterIds();
		} else if (evt.getPropertyName().equals(PROPERTY_KEY_FILTER_GLOBAL_QUERY)) {
			initFilterQueries();
		}
	}
}

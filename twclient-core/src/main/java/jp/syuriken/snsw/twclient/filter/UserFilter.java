package jp.syuriken.snsw.twclient.filter;

import java.util.TreeSet;

import jp.syuriken.snsw.twclient.ClientConfiguration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import twitter4j.DirectMessage;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.User;

/**
 * ユーザー設定によりフィルタを行うフィルタクラス
 * 
 * @author $Author$
 */
public class UserFilter implements MessageFilter {
	
	private TreeSet<Long> filterIds;
	
	private final ClientConfiguration configuration;
	
	private final Logger logger = LoggerFactory.getLogger(UserFilter.class);
	
	
	/**
	 * インスタンスを生成する。
	 * 
	 * @param configuration 設定
	 */
	public UserFilter(ClientConfiguration configuration) {
		this.configuration = configuration;
		filterIds = new TreeSet<Long>();
		initFilterIds();
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
		String idsString = configuration.getConfigProperties().getProperty("core.filter.user.ids");
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
	
	@Override
	public boolean onChangeAccount(boolean forWrite) {
		return false;
	}
	
	@Override
	public boolean onClientMessage(String name, Object arg) {
		return false;
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
		return filtered ? null : message;
	}
	
	@Override
	public boolean onException(Exception obj) {
		return false;
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
	public long[] onFriendList(long[] arr) {
		return arr;
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
		return filtered;
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
		return filtered ? null : status;
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
}

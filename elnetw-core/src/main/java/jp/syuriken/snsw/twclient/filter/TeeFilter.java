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

package jp.syuriken.snsw.twclient.filter;

import java.util.Arrays;
import java.util.TreeSet;

import jp.syuriken.snsw.twclient.ClientConfiguration;
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
public class TeeFilter implements TabRenderer {
	private static final Logger logger = LoggerFactory.getLogger(TeeFilter.class);
	private final String filterPropertyName;
	private final MessageFilter[] filters;
	private final TreeSet<Long> statusSet = new TreeSet<>();
	private TabRenderer renderer;
	private ClientConfiguration configuration;

	/**
	 * インスタンスを生成する。グローバルフィルタを使用する。
	 *
	 * @param uniqId      ユニークなID
	 * @param tabRenderer 移譲先レンダラ
	 */
	public TeeFilter(String uniqId, TabRenderer tabRenderer) {
		this(uniqId, tabRenderer, true);
	}

	/**
	 * インスタンスを生成する。
	 *
	 * @param uniqId          ユニークなID
	 * @param tabRenderer     移譲先レンダラ
	 * @param useGlobalFilter グローバルフィルタを使用するかどうか
	 */
	public TeeFilter(String uniqId, TabRenderer tabRenderer, boolean useGlobalFilter) {
		configuration = ClientConfiguration.getInstance();
		renderer = tabRenderer;
		filterPropertyName = "core.filter._tabs." + uniqId;
		UserFilter userFilter = new UserFilter(filterPropertyName);
		if (useGlobalFilter) {
			MessageFilter[] globalFilters = configuration.getFilters();
			globalFilters = Arrays.copyOf(globalFilters, globalFilters.length + 1);
			globalFilters[globalFilters.length - 1] = userFilter;
			filters = globalFilters;
		} else {
			filters = new MessageFilter[]{userFilter};
		}
	}

	@Override
	public void onBlock(User source, User blockedUser) {
		for (MessageFilter filter : filters) {
			if (filter.onBlock(source, blockedUser)) {
				return;
			}
		}

		renderer.onBlock(source, blockedUser);
	}

	@Override
	public void onChangeAccount(boolean forWrite) {
		for (MessageFilter filter : filters) {
			if (filter.onChangeAccount(forWrite)) {
				return;
			}
		}
		renderer.onChangeAccount(forWrite);
	}

	@Override
	public void onCleanUp() {
		for (MessageFilter filter : filters) {
			if (filter.onCleanUp()) {
				return;
			}
		}
		renderer.onCleanUp();
	}

	@Override
	public void onClientMessage(String name, Object arg) {
		for (MessageFilter filter : filters) {
			if (filter.onClientMessage(name, arg)) {
				return;
			}
		}
		renderer.onClientMessage(name, arg);
	}

	@Override
	public void onConnect() {
		for (MessageFilter filter : filters) {
			if (filter.onConnect()) {
				return;
			}
		}
		renderer.onConnect();
	}

	@Override
	public void onDeletionNotice(long directMessageId, long userId) {
		for (MessageFilter filter : filters) {
			if (filter.onDeletionNotice(directMessageId, userId)) {
				return;
			}
		}
		renderer.onDeletionNotice(directMessageId, userId);
	}

	@Override
	public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
		for (MessageFilter filter : filters) {
			if (filter.onDeletionNotice(statusDeletionNotice)) {
				return;
			}
		}
		renderer.onDeletionNotice(statusDeletionNotice);
	}

	@Override
	public void onDirectMessage(DirectMessage directMessage) {
		for (MessageFilter filter : filters) {
			if ((directMessage = filter.onDirectMessage(directMessage)) == null) {
				return;
			}
		}
		renderer.onDirectMessage(directMessage);
	}

	@Override
	public void onDisconnect() {
		for (MessageFilter filter : filters) {
			if (filter.onDisconnect()) {
				return;
			}
		}
		renderer.onDisconnect();
	}

	@Override
	public void onDisplayRequirement() {
		renderer.onDisplayRequirement();
	}

	@Override
	public void onException(Exception ex) {
		for (MessageFilter filter : filters) {
			if (filter.onException(ex)) {
				return;
			}
		}
		renderer.onException(ex);
	}

	@Override
	public void onFavorite(User source, User target, Status favoritedStatus) {
		for (MessageFilter filter : filters) {
			if (filter.onFavorite(source, target, favoritedStatus)) {
				return;
			}
		}
		renderer.onFavorite(source, target, favoritedStatus);
	}

	@Override
	public void onFollow(User source, User followedUser) {
		for (MessageFilter filter : filters) {
			if (filter.onFollow(source, followedUser)) {
				return;
			}
		}
		renderer.onFollow(source, followedUser);
	}

	@Override
	public void onFriendList(long[] friendIds) {
		for (MessageFilter filter : filters) {
			if ((friendIds = filter.onFriendList(friendIds)) == null) {
				return;
			}
		}
		renderer.onFriendList(friendIds);
	}

	@Override
	public void onScrubGeo(long userId, long upToStatusId) {
		for (MessageFilter filter : filters) {
			if (filter.onScrubGeo(userId, upToStatusId)) {
				return;
			}
		}
		renderer.onScrubGeo(userId, upToStatusId);
	}

	@Override
	public void onStallWarning(StallWarning warning) {
		for (MessageFilter filter : filters) {
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

		for (MessageFilter filter : filters) {
			if ((status = filter.onStatus(status)) == null) {
				return;
			}
		}
		renderer.onStatus(status);
	}

	@Override
	public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
		for (MessageFilter filter : filters) {
			if (filter.onTrackLimitationNotice(numberOfLimitedStatuses)) {
				return;
			}
		}
		renderer.onTrackLimitationNotice(numberOfLimitedStatuses);
	}

	@Override
	public void onUnblock(User source, User unblockedUser) {
		for (MessageFilter filter : filters) {
			if (filter.onUnblock(source, unblockedUser)) {
				return;
			}
		}
		renderer.onUnblock(source, unblockedUser);
	}

	@Override
	public void onUnfavorite(User source, User target, Status unfavoritedStatus) {
		for (MessageFilter filter : filters) {
			if (filter.onUnfavorite(source, target, unfavoritedStatus)) {
				return;
			}
		}
		renderer.onUnfavorite(source, target, unfavoritedStatus);
	}

	@Override
	public void onUnfollow(User source, User unfollowedUser) {
		for (MessageFilter filter : filters) {
			if (filter.onUnfollow(source, unfollowedUser)) {
				return;
			}
		}
		renderer.onUnfollow(source, unfollowedUser);
	}

	@Override
	public void onUserListCreation(User listOwner, UserList list) {
		for (MessageFilter filter : filters) {
			if (filter.onUserListCreation(listOwner, list)) {
				return;
			}
		}
		renderer.onUserListCreation(listOwner, list);
	}

	@Override
	public void onUserListDeletion(User listOwner, UserList list) {
		for (MessageFilter filter : filters) {
			if (filter.onUserListDeletion(listOwner, list)) {
				return;
			}
		}
		renderer.onUserListDeletion(listOwner, list);
	}

	@Override
	public void onUserListMemberAddition(User addedMember, User listOwner, UserList list) {
		for (MessageFilter filter : filters) {
			if (filter.onUserListMemberAddition(addedMember, listOwner, list)) {
				return;
			}
		}
		renderer.onUserListMemberAddition(addedMember, listOwner, list);
	}

	@Override
	public void onUserListMemberDeletion(User deletedMember, User listOwner, UserList list) {
		for (MessageFilter filter : filters) {
			if (filter.onUserListMemberDeletion(deletedMember, listOwner, list)) {
				return;
			}
		}
		renderer.onUserListMemberDeletion(deletedMember, listOwner, list);
	}

	@Override
	public void onUserListSubscription(User subscriber, User listOwner, UserList list) {
		for (MessageFilter filter : filters) {
			if (filter.onUserListSubscription(subscriber, listOwner, list)) {
				return;
			}
		}
		renderer.onUserListSubscription(subscriber, listOwner, list);
	}

	@Override
	public void onUserListUnsubscription(User subscriber, User listOwner, UserList list) {
		for (MessageFilter filter : filters) {
			if (filter.onUserListUnsubscription(subscriber, listOwner, list)) {
				return;
			}
		}
		renderer.onUserListUnsubscription(subscriber, listOwner, list);
	}

	@Override
	public void onUserListUpdate(User listOwner, UserList list) {
		for (MessageFilter filter : filters) {
			if (filter.onUserListUpdate(listOwner, list)) {
				return;
			}
		}
		renderer.onUserListUpdate(listOwner, list);
	}

	@Override
	public void onUserProfileUpdate(User updatedUser) {
		for (MessageFilter filter : filters) {
			if (filter.onUserProfileUpdate(updatedUser)) {
				return;
			}
		}
		renderer.onUserProfileUpdate(updatedUser);
	}
}

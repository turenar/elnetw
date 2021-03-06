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

package jp.mydns.turenar.twclient.notifier;

import java.util.HashSet;
import java.util.List;

import jp.mydns.turenar.lib.primitive.LongHashSet;
import jp.mydns.turenar.twclient.ClientConfiguration;
import jp.mydns.turenar.twclient.ClientMessageAdapter;
import jp.mydns.turenar.twclient.Utility;
import jp.mydns.turenar.twclient.bus.MessageBus;
import jp.mydns.turenar.twclient.conf.PropertyUpdateEvent;
import jp.mydns.turenar.twclient.conf.PropertyUpdateListener;
import jp.mydns.turenar.twclient.storage.CacheStorage;
import jp.mydns.turenar.twclient.storage.DirEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.DirectMessage;
import twitter4j.Status;
import twitter4j.User;
import twitter4j.UserMentionEntity;

import static jp.mydns.turenar.twclient.ClientConfiguration.PROPERTY_ACCOUNT_LIST;

/**
 * notify reply, retweeted, directmessage...
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class NotifyHandler extends ClientMessageAdapter implements PropertyUpdateListener {
	private class SourceTarget {
		private User source;
		private Object target;

		public SourceTarget(User source, Object target) {
			this.source = source;
			this.target = target;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof SourceTarget) {
				SourceTarget another = (SourceTarget) obj;
				return source.equals(another.source) && target.equals(another.target);
			}
			return false;
		}

		@Override
		public int hashCode() {
			return source.hashCode() * 31 + target.hashCode();
		}
	}

	private static final Logger logger = LoggerFactory.getLogger(NotifyHandler.class);
	private static final NotifyHandler instance = new NotifyHandler();
	private static final long DEFAULT_EXPIRE_TIME = 60 * 60 * 1000;
	private static final String CACHE_DIR_PATH = "/elnetw/notifier/NotifyHandler";

	/**
	 * register handler into message bus
	 */
	public static void register() {
		instance.start();
	}

	public static void serialize(CacheStorage storage) {
		instance.store(storage);
	}

	private final ClientConfiguration configuration;
	private LongHashSet accountList;
	private LongHashSet notifiedStatusSet = new LongHashSet();
	private HashSet<SourceTarget> notifiedFavoritesSet = new HashSet<>();

	private NotifyHandler() {
		configuration = ClientConfiguration.getInstance();
	}

	private boolean checkMyAccount(long userId) {
		return accountList.contains(userId);
	}

	private boolean checkNotExpired(long createdAt) {
		return createdAt + DEFAULT_EXPIRE_TIME >= System.currentTimeMillis();
	}

	private boolean checkNotNotified(DirectMessage directMessage) {
		// direct message id is "negative" number
		return !notifiedStatusSet.contains(-directMessage.getId());
	}

	private boolean checkNotNotified(Status status) {
		// status is positive number in status set
		return !notifiedStatusSet.contains(status.getId());
	}

	@Override
	public void onDirectMessage(DirectMessage directMessage) {
		if (checkNotExpired(directMessage.getCreatedAt().getTime()) && checkNotNotified(directMessage)) {
			if (!checkMyAccount(directMessage.getSenderId()) && checkMyAccount(directMessage.getRecipientId())) {
				try {
					configuration.getUtility().sendNotify(
							String.format("@%s (%s) からのダイレクトメッセージ", directMessage.getSenderScreenName(),
									directMessage.getSender().getName()),
							directMessage.getText(), configuration.getImageCacher().getImageFile(directMessage.getSender()));
					notifiedStatusSet.add(-directMessage.getId());
				} catch (InterruptedException e) {
					logger.warn("Interrupted", e);
				}
			}
		}
	}

	@Override
	public void onFavorite(User source, User target, Status favoritedStatus) {
		if (!checkMyAccount(source.getId()) && checkMyAccount(target.getId())) {
			SourceTarget obj = new SourceTarget(source, favoritedStatus);
			if (!notifiedFavoritesSet.contains(obj)) {
				try {
					configuration.getUtility().sendNotify(
							String.format("@%s (%s) にふぁぼられました", source.getScreenName(), source.getName()),
							favoritedStatus.getText(), configuration.getImageCacher().getImageFile(source));
					notifiedFavoritesSet.add(obj);
				} catch (InterruptedException e) {
					logger.warn("Interrupted", e);
				}
			}
		}
	}

	@Override
	public void onStatus(Status status) {
		if (checkNotExpired(status.getCreatedAt().getTime()) && !checkMyAccount(status.getUser().getId())
				&& checkNotNotified(status)) {
			try {
				if (status.isRetweet()) {
					if (checkMyAccount(status.getRetweetedStatus().getUser().getId())) {
						configuration.getUtility().sendNotify(
								String.format("@%s (%s) があなたのツイートをリツイート",
										status.getUser().getScreenName(), status.getUser().getName()),
								status.getText(), configuration.getImageCacher().getImageFile(status.getUser()));
						notifiedStatusSet.add(status.getId());
					}
				} else {
					if (checkMyAccount(status.getInReplyToUserId())) {
						configuration.getUtility().sendNotify(
								String.format("@%s (%s) からのリプライ",
										status.getUser().getScreenName(), status.getUser().getName()),
								status.getText(), configuration.getImageCacher().getImageFile(status.getUser()));
						notifiedStatusSet.add(status.getId());
						return;
					}
					UserMentionEntity[] userMentionEntities = status.getUserMentionEntities();
					if (userMentionEntities != null) {
						for (UserMentionEntity userMentionEntity : userMentionEntities) {
							if (checkMyAccount(userMentionEntity.getId())) {
								configuration.getUtility().sendNotify(
										String.format("@%s (%s) からのメンション",
												status.getUser().getScreenName(), status.getUser().getName()),
										status.getText(), configuration.getImageCacher().getImageFile(status.getUser()));
								notifiedStatusSet.add(status.getId());
								return;
							}
						}
					}
				}
			} catch (InterruptedException e) {
				logger.warn("Interrupted", e);
			}
		}
	}

	@Override
	public void onUnfavorite(User source, User target, Status unfavoritedStatus) {
		notifiedFavoritesSet.remove(new SourceTarget(source, unfavoritedStatus));
	}

	@Override
	public void propertyUpdate(PropertyUpdateEvent evt) {
		if (evt.getPropertyName().equals(PROPERTY_ACCOUNT_LIST)) {
			LongHashSet accountList = new LongHashSet();
			List<String> accountStringIDs = evt.getSource().getList(PROPERTY_ACCOUNT_LIST);
			for (String id : accountStringIDs) {
				accountList.add(Long.parseLong(id));
			}
			this.accountList = accountList;
		}
	}

	private void start() {
		CacheStorage storage = configuration.getCacheStorage();
		if (storage.isDirEntry(CACHE_DIR_PATH)) {
			DirEntry dirEntry = storage.getDirEntry(CACHE_DIR_PATH);
			dirEntry.readLongList("notified").stream()
					.mapToLong(Long::longValue)
					.forEach(notifiedStatusSet::add);
		}

		configuration.getConfigProperties().addPropertyUpdatedListener(this);
		propertyUpdate(new PropertyUpdateEvent(configuration.getConfigProperties(), PROPERTY_ACCOUNT_LIST, null, null));
		configuration.getMessageBus().establish(MessageBus.ALL_ACCOUNT_ID, "all", this);
	}

	private void store(CacheStorage storage) {
		long baseTime = System.currentTimeMillis() - DEFAULT_EXPIRE_TIME;
		Long[] notifiedList = notifiedStatusSet.stream()
				.filter(v -> Utility.snowflakeIdToMilliSec(v > 0 ? v : -v) > baseTime)
				.boxed()
				.toArray(Long[]::new);
		DirEntry dirEntry = storage.mkdir(CACHE_DIR_PATH, true);
		dirEntry.writeList("notified", (Object[]) notifiedList);
	}
}

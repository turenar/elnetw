package jp.mydns.turenar.twclient.notifier;

import java.util.List;

import jp.mydns.turenar.lib.primitive.LongHashSet;
import jp.mydns.turenar.twclient.ClientConfiguration;
import jp.mydns.turenar.twclient.ClientMessageAdapter;
import jp.mydns.turenar.twclient.bus.MessageBus;
import jp.mydns.turenar.twclient.conf.PropertyUpdateEvent;
import jp.mydns.turenar.twclient.conf.PropertyUpdateListener;
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
	private static final Logger logger = LoggerFactory.getLogger(NotifyHandler.class);
	private static final NotifyHandler instance = new NotifyHandler();
	private static final long DEFAULT_EXPIRE_TIME = 60 * 60 * 1000;

	/**
	 * register handler into message bus
	 */
	public static void register() {
		instance.start();
	}

	private final ClientConfiguration configuration;
	private LongHashSet accountList;
	private LongHashSet notifiedStatusSet = new LongHashSet();

	private NotifyHandler() {
		configuration = ClientConfiguration.getInstance();
	}

	private boolean checkExpired(long createdAt) {
		return createdAt + DEFAULT_EXPIRE_TIME < System.currentTimeMillis();
	}

	private boolean checkMyAccount(long userId) {
		return accountList.contains(userId);
	}

	private boolean checkNotified(DirectMessage directMessage) {
		// direct message id is "negative" number
		return notifiedStatusSet.add(-directMessage.getId());
	}

	private boolean checkNotified(Status status) {
		// status is positive number in status set
		return notifiedStatusSet.contains(status.getId());
	}

	@Override
	public void onDirectMessage(DirectMessage directMessage) {
		if (!checkExpired(directMessage.getCreatedAt().getTime()) && !checkNotified(directMessage)) {
			if (!accountList.contains(directMessage.getSenderId()) && accountList.contains(directMessage.getRecipientId())) {
				try {
					configuration.getUtility().sendNotify(
							String.format("@%s (%s) からのダイレクトメッセージ", directMessage.getSenderScreenName(),
									directMessage.getSender().getName()),
							directMessage.getText(), configuration.getImageCacher().getImageFile(directMessage.getSender()));
				} catch (InterruptedException e) {
					logger.warn("Interrupted", e);
				}
			}
		}
	}

	@Override
	public void onFavorite(User source, User target, Status favoritedStatus) {
		if (!accountList.contains(source.getId()) && accountList.contains(target.getId())) {
			try {
				configuration.getUtility().sendNotify(
						String.format("@%s (%s) にふぁぼられました", source.getScreenName(), source.getName()),
						favoritedStatus.getText(), configuration.getImageCacher().getImageFile(source));
			} catch (InterruptedException e) {
				logger.warn("Interrupted", e);
			}
		}
	}

	@Override
	public void onStatus(Status status) {
		if (checkExpired(status.getCreatedAt().getTime()) || checkMyAccount(status.getUser().getId())
				|| checkNotified(status)) {
			return;
		}
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
		configuration.getConfigProperties().addPropertyUpdatedListener(this);
		propertyUpdate(new PropertyUpdateEvent(configuration.getConfigProperties(), PROPERTY_ACCOUNT_LIST, null, null));
		configuration.getMessageBus().establish(MessageBus.ALL_ACCOUNT_ID, "my/timeline", this);
	}
}

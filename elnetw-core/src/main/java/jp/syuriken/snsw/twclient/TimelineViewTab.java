package jp.syuriken.snsw.twclient;

import java.awt.Color;
import java.awt.EventQueue;
import java.text.MessageFormat;
import java.util.Date;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.DirectMessage;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.TwitterException;
import twitter4j.User;
import twitter4j.UserList;
import twitter4j.internal.org.json.JSONException;
import twitter4j.internal.org.json.JSONObject;

/**
 * タイムラインビュー
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class TimelineViewTab extends DefaultClientTab {

	/*package*/ static final Logger logger = LoggerFactory.getLogger(TimelineViewTab.class);

	private static final String TAB_ID = "timeline";

	private DefaultRenderer renderer = new DefaultRenderer() {

		@Override
		public void onChangeAccount(boolean forWrite) {
			StatusData statusData = new StatusData(null, new Date());
			statusData.backgroundColor = Color.LIGHT_GRAY;
			statusData.foregroundColor = Color.BLACK;
			statusData.image = new JLabel();
			statusData.sentBy = new JLabel(ClientConfiguration.APPLICATION_NAME);
			if (forWrite) {
				statusData.user = "!core.change.account!write";
				statusData.data = new JLabel("書き込み用アカウントを変更しました。");
			} else {
				statusData.user = "!core.change.account!read";
				statusData.data = new JLabel("読み込み用アカウントを変更しました。");
			}
			addStatus(statusData, frameApi.getInfoSurviveTime());
		}

		@Override
		public void onCleanUp() {
		}

		@Override
		public void onConnect() {
		}

		@Override
		public void onDeletionNotice(long directMessageId, long userId) {
			// TODO DM Deletion is not supported yet.
		}

		@Override
		public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
			logger.trace("onDeletionNotice: {}", statusDeletionNotice);

			StatusData statusData = getStatus(statusDeletionNotice.getStatusId());
			if (statusData != null) {
				if (statusData.tag instanceof Status == false) {
					return;
				}
				Status status = (Status) statusData.tag;
				StatusData deletionStatusData = new StatusData(statusDeletionNotice, new Date());
				deletionStatusData.backgroundColor = Color.LIGHT_GRAY;
				deletionStatusData.foregroundColor = Color.RED;
				deletionStatusData.image = new JLabel();
				deletionStatusData.sentBy = new JLabel(status.getUser().getScreenName());
				deletionStatusData.user = "!twdel." + statusDeletionNotice.getUserId();
				deletionStatusData.data = new JLabel("DELETED: " + status.getText());
				addStatus(deletionStatusData, getInfoSurviveTime() * 2);
				removeStatus(statusData, getInfoSurviveTime() * 2);
			}
		}

		@Override
		public void onDirectMessage(DirectMessage directMessage) {
			StatusData statusData = new StatusData(directMessage, directMessage.getCreatedAt());
			statusData.backgroundColor = Color.LIGHT_GRAY;
			statusData.foregroundColor = Color.CYAN;
			statusData.image = new JLabel();
			statusData.sentBy = new JLabel(directMessage.getSenderScreenName());
			statusData.user = "!dm." + directMessage.getSenderScreenName();
			String message = MessageFormat.format("DMを受信しました: \"{0}\"", directMessage.getText());
			statusData.data = new JLabel(message);
			addStatus(statusData);
		}

		@Override
		public void onDisconnect() {
		}

		@Override
		public void onException(Exception ex) {
			StatusData statusData = new StatusData(ex, new Date());
			statusData.backgroundColor = Color.BLACK;
			statusData.foregroundColor = Color.RED;
			statusData.image = new JLabel();
			statusData.sentBy = new JLabel("!ERROR!");
			statusData.user = "!ex." + ex.getClass().getName();
			String exString;
			if (ex instanceof TwitterException) {
				TwitterException twex = (TwitterException) ex;
				if (twex.isCausedByNetworkIssue()) {
					exString = twex.getCause().toString();
				} else {
					exString = twex.getStatusCode() + ": " + twex.getErrorMessage();
				}
			} else {
				exString = ex.toString();
			}
			if (exString.length() > 256) {
				exString = new StringBuilder().append(exString, 0, 254).append("..").toString();
			}
			statusData.data = new JLabel(exString);
			addStatus(statusData);
		}

		@Override
		public void onFavorite(User source, User target, Status favoritedStatus) {
			if (target.getId() == frameApi.getLoginUser().getId()) {
				StatusData statusData = new StatusData(favoritedStatus, new Date());
				statusData.backgroundColor = Color.GRAY;
				statusData.foregroundColor = Color.YELLOW;
				statusData.image = new JLabel(new ImageIcon(source.getProfileImageURL()));
				statusData.sentBy = new JLabel(source.getScreenName());
				statusData.user = "!fav." + source.getScreenName();
				String message = MessageFormat.format("ふぁぼられました: \"{0}\"", favoritedStatus.getText());
				statusData.data = new JLabel(message);
				addStatus(statusData);
				try {
					configuration.getUtility().sendNotify(
							MessageFormat.format("{0} ({1})", source.getScreenName(), source.getName()), message,
							imageCacher.getImageFile(source));
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
			if (source.getId() == frameApi.getLoginUser().getId()) {
				StatusData statusData = getStatus(favoritedStatus.getId());
				if (statusData.tag instanceof TwitterStatus) {
					TwitterStatus status = (TwitterStatus) statusData.tag;
					status.setFavorited(true);
				}
			}
		}

		@Override
		public void onFollow(User source, User followedUser) {
			if (followedUser.getId() == frameApi.getLoginUser().getId()) {
				StatusData statusData = new StatusData(null, new Date());
				statusData.backgroundColor = Color.GRAY;
				statusData.foregroundColor = Color.YELLOW;
				statusData.image = new JLabel(new ImageIcon(source.getProfileImageURL()));
				statusData.sentBy = new JLabel(source.getScreenName());
				statusData.user = "!follow." + source.getScreenName();
				String message = "@" + followedUser.getScreenName() + " をフォローしました";
				statusData.data = new JLabel(message);
				addStatus(statusData);
				try {
					configuration.getUtility().sendNotify(
							MessageFormat.format("{0} ({1})", source.getScreenName(), source.getName()), message,
							imageCacher.getImageFile(source));
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
		}

		@Override
		public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
			logger.trace("onTrackLimitationNotice: {}", numberOfLimitedStatuses);
			StatusData statusData = new StatusData(null, new Date());
			statusData.backgroundColor = Color.BLACK;
			statusData.foregroundColor = Color.LIGHT_GRAY;
			statusData.image = new JLabel();
			statusData.sentBy = new JLabel();
			statusData.user = "!stream.overlimit";
			statusData.data =
					new JLabel("TwitterStreamは " + numberOfLimitedStatuses + " ツイート数をスキップしました： TrackLimitationNotice");
			addStatus(statusData, getInfoSurviveTime() * 2);
		}

		@Override
		public void onUnfavorite(User source, User target, Status unfavoritedStatus) {
			if (logger.isTraceEnabled()) {
				logger.trace("onUnFavorite: source={}, target={}, unfavoritedStatus={}",
						Utility.toArray(source, target, unfavoritedStatus));
			}
			if (target.getId() == frameApi.getLoginUser().getId()) {
				StatusData statusData = new StatusData(unfavoritedStatus, new Date());
				statusData.backgroundColor = Color.GRAY;
				statusData.foregroundColor = Color.LIGHT_GRAY;
				statusData.image = new JLabel(new ImageIcon(source.getProfileImageURL()));
				statusData.sentBy = new JLabel(source.getScreenName());
				statusData.user = "!unfav." + source.getScreenName();
				String message = "ふぁぼやめられました: \"" + unfavoritedStatus.getText() + "\"";
				statusData.data = new JLabel(message);
				addStatus(statusData);
				try {
					configuration.getUtility()
							.sendNotify(MessageFormat.format("{0} ({1})", source.getScreenName(), source.getName()), message,
									imageCacher.getImageFile(source));
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
			if (source.getId() == frameApi.getLoginUser().getId()) {
				StatusData statusData = getStatus(unfavoritedStatus.getId());
				if (statusData.tag instanceof TwitterStatus) {
					TwitterStatus status = (TwitterStatus) statusData.tag;
					status.setFavorited(false);
				}
			}
		}

		@Override
		public void onUserListCreation(User listOwner, UserList list) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onUserListDeletion(User listOwner, UserList list) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onUserListMemberAddition(User addedMember, User listOwner, UserList list) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onUserListMemberDeletion(User deletedMember, User listOwner, UserList list) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onUserListSubscription(User subscriber, User listOwner, UserList list) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onUserListUnsubscription(User subscriber, User listOwner, UserList list) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onUserListUpdate(User listOwner, UserList list) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onUserProfileUpdate(User updatedUser) {
			// TODO Auto-generated method stub

		}
	};

	private volatile boolean focusGained;

	private volatile boolean isDirty;

	/**
	 * インスタンスを生成する。
	 *
	 * @param configuration 設定
	 */
	public TimelineViewTab(ClientConfiguration configuration) {
		super(configuration);
	}

	/**
	 * インスタンスを生成する。
	 *
	 * @param configuration 設定
	 * @param data          保存されたデータ
	 * @throws JSONException JSON例外
	 */
	public TimelineViewTab(ClientConfiguration configuration, String data) throws JSONException {
		super(configuration, data);
	}

	@Override
	public StatusPanel addStatus(StatusData statusData) {
		if (focusGained == false && isDirty == false) {
			isDirty = true;
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					configuration.refreshTab(TimelineViewTab.this);
				}
			});
		}
		return super.addStatus(statusData);
	}

	@Override
	public void focusGained() {
		super.focusGained();
		focusGained = true;
		isDirty = false;
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				configuration.refreshTab(TimelineViewTab.this);
			}
		});
	}

	@Override
	public void focusLost() {
		focusGained = false;
	}

	@Override
	public DefaultRenderer getActualRenderer() {
		return renderer;
	}

	@Override
	public Icon getIcon() {
		return null; // TODO
	}

	@Override
	protected Object getSerializedExtendedData() {
		return JSONObject.NULL;
	}

	@Override
	public String getTabId() {
		return TAB_ID;
	}

	@Override
	public String getTitle() {
		return isDirty ? "Timeline*" : "Timeline";
	}

	@Override
	public String getToolTip() {
		return "HomeTimeline";
	}

}

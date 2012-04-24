package jp.syuriken.snsw.twclient;

import java.awt.Color;
import java.text.MessageFormat;
import java.util.Arrays;
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

/**
 * タイムラインビュー
 * 
 * @author $Author$
 */
public class TimelineViewTab extends DefaultClientTab {
	
	private DefaultRenderer renderer = new DefaultRenderer() {
		
		@Override
		public void onBlock(User source, User blockedUser) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onChangeAccount(boolean forWrite) {
			StatusData statusData = new StatusData(null, new Date());
			statusData.backgroundColor = Color.LIGHT_GRAY;
			statusData.foregroundColor = Color.BLACK;
			statusData.image = new JLabel();
			statusData.sentBy = new JLabel(TwitterClientFrame.APPLICATION_NAME);
			if (forWrite) {
				statusData.sentBy.setName("!core.change.account!write");
				statusData.data = new JLabel("書き込み用アカウントを変更しました。");
			} else {
				statusData.sentBy.setName("!core.change.account!read");
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
				deletionStatusData.sentBy = new JLabel(((JLabel) (statusData.sentBy)).getText()); // TODO
				deletionStatusData.sentBy.setName("!twdel." + statusDeletionNotice.getUserId());
				deletionStatusData.data = new JLabel("DELETED: " + status.getText());
				addStatus(deletionStatusData, getInfoSurviveTime() * 2);
				removeStatus(statusData, getInfoSurviveTime() * 2);
			}
		}
		
		@Override
		public void onDirectMessage(DirectMessage directMessage) {
			logger.trace("onDirectMessage: {}", directMessage);
			
			StatusData statusData = new StatusData(directMessage, directMessage.getCreatedAt());
			statusData.backgroundColor = Color.LIGHT_GRAY;
			statusData.foregroundColor = Color.CYAN;
			statusData.image = new JLabel();
			statusData.sentBy = new JLabel(directMessage.getSenderScreenName());
			statusData.sentBy.setName("!dm." + directMessage.getSenderScreenName());
			String message = MessageFormat.format("DMを受信しました: \"{0}\"", directMessage.getText());
			statusData.data = new JLabel(message);
			addStatus(statusData);
			User sender = directMessage.getSender();
			configuration
				.getFrameApi()
				.getUtility()
				.sendNotify(MessageFormat.format("{0} ({1})", sender.getScreenName(), sender.getName()), message,
						imageCacher.getImageFile(sender));
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
			statusData.sentBy.setName("!ex." + ex.getClass().getName());
			String exString;
			if (ex instanceof TwitterException) {
				TwitterException twex = (TwitterException) ex;
				exString = twex.getStatusCode() + ": " + twex.getErrorMessage();
			} else {
				exString = ex.toString();
				if (exString.length() > 256) {
					exString = new StringBuilder().append(exString, 0, 254).append("..").toString();
				}
			}
			statusData.data = new JLabel(exString);
			addStatus(statusData);
		}
		
		@Override
		public void onFavorite(User source, User target, Status favoritedStatus) {
			logger.trace("onFavorite: {}", favoritedStatus);
			
			if (target.getId() == frameApi.getLoginUser().getId()) {
				StatusData statusData = new StatusData(favoritedStatus, new Date());
				statusData.backgroundColor = Color.GRAY;
				statusData.foregroundColor = Color.YELLOW;
				statusData.image = new JLabel(new ImageIcon(source.getProfileImageURL()));
				statusData.sentBy = new JLabel(source.getScreenName());
				statusData.sentBy.setName("!fav." + source.getScreenName());
				String message = MessageFormat.format("ふぁぼられました: \"{0}\"", favoritedStatus.getText());
				statusData.data = new JLabel(message);
				addStatus(statusData);
				configuration.getUtility().sendNotify(
						MessageFormat.format("{0} ({1})", source.getScreenName(), source.getName()), message,
						imageCacher.getImageFile(source));
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
			logger.trace("onFollow: {} {}", source, followedUser);
			if (followedUser.getId() == frameApi.getLoginUser().getId()) {
				StatusData statusData = new StatusData(null, new Date());
				statusData.backgroundColor = Color.GRAY;
				statusData.foregroundColor = Color.YELLOW;
				statusData.image = new JLabel(new ImageIcon(source.getProfileImageURL()));
				statusData.sentBy = new JLabel(source.getScreenName());
				statusData.sentBy.setName("!follow." + source.getScreenName());
				String message = "@" + followedUser.getScreenName() + " をフォローしました";
				statusData.data = new JLabel(message);
				addStatus(statusData);
				configuration.getUtility().sendNotify(
						MessageFormat.format("{0} ({1})", source.getScreenName(), source.getName()), message,
						imageCacher.getImageFile(source));
			}
		}
		
		@Override
		public void onFriendList(long[] friendIds) {
			if (logger.isTraceEnabled()) {
				logger.trace("onFriendList: count={}, {}", friendIds.length, Arrays.toString(friendIds));
			}
		}
		
		@Override
		public void onRetweet(User source, User target, Status retweetedStatus) {
			if (logger.isTraceEnabled()) {
				logger.trace("onRetweet: source={}, target={}, retweet={}",
						Utility.toArray(source, target, retweetedStatus));
			} else if (logger.isDebugEnabled()) {
				logger.debug("id={}, retweetedid={}, status={}", Utility.toArray(retweetedStatus.getId(),
						retweetedStatus.getRetweetedStatus().getId(), retweetedStatus));
			}
			addStatus(retweetedStatus);
		}
		
		@Override
		public void onScrubGeo(long userId, long upToStatusId) {
			// TODO Auto-generated method stub
		}
		
		@Override
		public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
			logger.trace("onTrackLimitationNotice: {}", numberOfLimitedStatuses);
			StatusData statusData = new StatusData(null, new Date());
			statusData.backgroundColor = Color.BLACK;
			statusData.foregroundColor = Color.LIGHT_GRAY;
			statusData.image = new JLabel();
			statusData.sentBy = new JLabel();
			statusData.sentBy.setName("!stream.overlimit");
			statusData.data =
					new JLabel("TwitterStreamは " + numberOfLimitedStatuses + " ツイート数をスキップしました： TrackLimitationNotice");
			addStatus(statusData, getInfoSurviveTime() * 2);
		}
		
		@Override
		public void onUnblock(User source, User unblockedUser) {
			// TODO Auto-generated method stub
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
				statusData.sentBy.setName("!unfav." + source.getScreenName());
				String message = "ふぁぼやめられました: \"" + unfavoritedStatus.getText() + "\"";
				statusData.data = new JLabel(message);
				addStatus(statusData);
				configuration
					.getFrameApi()
					.getUtility()
					.sendNotify(MessageFormat.format("{0} ({1})", source.getScreenName(), source.getName()), message,
							imageCacher.getImageFile(source));
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
	
	private boolean focusGained;
	
	private boolean isDirty;
	
	private Logger logger = LoggerFactory.getLogger(TimelineViewTab.class);
	
	
	/**
	 * インスタンスを生成する。
	 * 
	 * @param configuration 設定
	 */
	protected TimelineViewTab(ClientConfiguration configuration) {
		super(configuration);
	}
	
	@Override
	public StatusPanel addStatus(StatusData statusData) {
		if (focusGained == false && isDirty == false) {
			isDirty = true;
			configuration.refreshTab(this);
		}
		return super.addStatus(statusData);
	}
	
	@Override
	public void focusGained() {
		focusGained = true;
		isDirty = false;
		configuration.refreshTab(this);
	}
	
	@Override
	public void focusLost() {
		focusGained = false;
	}
	
	@Override
	public Icon getIcon() {
		return null; // TODO
	}
	
	@Override
	public DefaultRenderer getRenderer() {
		return renderer;
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

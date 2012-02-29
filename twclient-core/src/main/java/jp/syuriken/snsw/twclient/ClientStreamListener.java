package jp.syuriken.snsw.twclient;

import java.awt.Color;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Date;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import twitter4j.DirectMessage;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.User;
import twitter4j.UserList;
import twitter4j.UserStreamListener;

/**
 * ユーザーストリームリスナ。
 * 
 * @author $Author$
 */
public class ClientStreamListener implements UserStreamListener {
	
	private final ClientFrameApi frameApi;
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	
	/**
	 * インスタンスを生成する。
	 * 
	 * @param twitterClientFrame API
	 */
	public ClientStreamListener(ClientFrameApi twitterClientFrame) {
		frameApi = twitterClientFrame;
	}
	
	@Override
	public void onBlock(User source, User blockedUser) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onDeletionNotice(long directMessageId, long userId) {
		// TODO DM Deletion is not supported yet.
	}
	
	@Override
	public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
		logger.trace("onDeletionNotice: {}", statusDeletionNotice);
		
		StatusData statusData = frameApi.getStatus(statusDeletionNotice.getStatusId());
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
			frameApi.addStatus(deletionStatusData, frameApi.getInfoSurviveTime() * 2);
			frameApi.removeStatus(statusData, frameApi.getInfoSurviveTime() * 2);
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
		frameApi.addStatus(statusData);
		User sender = directMessage.getSender();
		frameApi.getUtility().sendNotify(MessageFormat.format("{0} ({1})", sender.getScreenName(), sender.getName()),
				message, frameApi.getImageCacher().getImageFile(sender));
	}
	
	@Override
	public void onException(Exception ex) {
		frameApi.handleException(ex);
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
			frameApi.addStatus(statusData);
			frameApi.getUtility().sendNotify(
					MessageFormat.format("{0} ({1})", source.getScreenName(), source.getName()), message,
					frameApi.getImageCacher().getImageFile(source));
		}
		if (source.getId() == frameApi.getLoginUser().getId()) {
			StatusData statusData = frameApi.getStatus(favoritedStatus.getId());
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
			String message = followedUser.getScreenName() + " にフォローされました";
			statusData.data = new JLabel(message);
			frameApi.addStatus(statusData);
			frameApi.getUtility().sendNotify(
					MessageFormat.format("{0} ({1})", source.getScreenName(), source.getName()), message,
					frameApi.getImageCacher().getImageFile(source));
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
		}
		
		if (logger.isDebugEnabled()) {
			logger.debug("id={}, retweetedid={}, status={}", Utility.toArray(retweetedStatus.getId(), retweetedStatus
				.getRetweetedStatus().getId(), retweetedStatus));
		}
		frameApi.addStatus(retweetedStatus);
	}
	
	@Override
	public void onScrubGeo(long userId, long upToStatusId) {
		// TODO Auto-generated method stub
	}
	
	@Override
	public void onStatus(Status originalStatus) {
		frameApi.addStatus(originalStatus);
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
				new JLabel("TwitterStreamは " + numberOfLimitedStatuses + " ツイートをスキップしました： TrackLimitationNotice");
		frameApi.addStatus(statusData, frameApi.getInfoSurviveTime() * 2);
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
			frameApi.addStatus(statusData);
			frameApi.getUtility().sendNotify(
					MessageFormat.format("{0} ({1})", source.getScreenName(), source.getName()), message,
					frameApi.getImageCacher().getImageFile(source));
		}
		if (source.getId() == frameApi.getLoginUser().getId()) {
			StatusData statusData = frameApi.getStatus(unfavoritedStatus.getId());
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
	
}

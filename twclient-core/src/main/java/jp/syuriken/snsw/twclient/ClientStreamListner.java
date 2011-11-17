package jp.syuriken.snsw.twclient;

import java.awt.Color;
import java.util.Date;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

import twitter4j.DirectMessage;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.User;
import twitter4j.UserList;
import twitter4j.UserStreamListener;

/**
 * TODO snsoftware
 * 
 * @author $Author$
 */
public class ClientStreamListner implements UserStreamListener {
	
	private final TwitterClientFrame twitterClientFrame;
	
	
	/**
	 * インスタンスを生成する。
	 * 
	 * @param twitterClientFrame
	 */
	public ClientStreamListner(TwitterClientFrame twitterClientFrame) {
		this.twitterClientFrame = twitterClientFrame;
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
		StatusData statusData = new StatusData(statusDeletionNotice, new Date());
		statusData.backgroundColor = Color.LIGHT_GRAY;
		statusData.foregroundColor = Color.RED;
		statusData.image = new JLabel();
		statusData.sentBy = new JLabel(String.valueOf(statusDeletionNotice.getUserId())); // TODO
		statusData.sentBy.setName("!twdel." + statusDeletionNotice.getUserId());
		statusData.data = new JLabel("DELETED: " + statusDeletionNotice.getStatusId());
		twitterClientFrame.addStatus(statusData, 10000);
	}
	
	@Override
	public void onDirectMessage(DirectMessage directMessage) {
		StatusData statusData = new StatusData(directMessage, directMessage.getCreatedAt());
		statusData.backgroundColor = Color.LIGHT_GRAY;
		statusData.foregroundColor = Color.CYAN;
		statusData.image = new JLabel();
		statusData.sentBy = new JLabel(directMessage.getSenderScreenName());
		statusData.sentBy.setName("!dm." + directMessage.getSenderScreenName());
		statusData.data = new JLabel("DMを受信しました: " + directMessage.getText());
		twitterClientFrame.addStatus(statusData);
	}
	
	@Override
	public void onException(Exception ex) {
		twitterClientFrame.handleException(ex);
	}
	
	@Override
	public void onFavorite(User source, User target, Status favoritedStatus) {
		if (target.getId() == twitterClientFrame.getLoginUser().getId()) {
			StatusData statusData = new StatusData(favoritedStatus, new Date());
			statusData.backgroundColor = Color.GRAY;
			statusData.foregroundColor = Color.YELLOW;
			statusData.image = new JLabel(new ImageIcon(source.getProfileImageURL()));
			statusData.sentBy = new JLabel(source.getScreenName());
			statusData.sentBy.setName("!fav." + source.getScreenName());
			statusData.data = new JLabel("ふぁぼられました: " + favoritedStatus.getText());
			twitterClientFrame.addStatus(statusData);
		}
	}
	
	@Override
	public void onFollow(User source, User followedUser) {
		if (followedUser.getId() == twitterClientFrame.getLoginUser().getId()) {
			StatusData statusData = new StatusData(null, new Date());
			statusData.backgroundColor = Color.GRAY;
			statusData.foregroundColor = Color.YELLOW;
			statusData.image = new JLabel(new ImageIcon(source.getProfileImageURL()));
			statusData.sentBy = new JLabel(source.getScreenName());
			statusData.sentBy.setName("!follow." + source.getScreenName());
			statusData.data = new JLabel("フォローされました" + followedUser.getScreenName());
			twitterClientFrame.addStatus(statusData);
		}
	}
	
	@Override
	public void onFriendList(long[] friendIds) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onRetweet(User source, User target, Status retweetedStatus) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onScrubGeo(long userId, long upToStatusId) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onStatus(Status originalStatus) {
		twitterClientFrame.addStatus(originalStatus);
	}
	
	@Override
	public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
		StatusData statusData = new StatusData(null, new Date());
		statusData.backgroundColor = Color.BLACK;
		statusData.foregroundColor = Color.LIGHT_GRAY;
		statusData.image = new JLabel();
		statusData.sentBy = new JLabel();
		statusData.sentBy.setName("!stream.overlimit");
		statusData.data =
				new JLabel("TwitterStreamは " + numberOfLimitedStatuses + " ツイート数をスキップしました： TrackLimitationNotice");
		twitterClientFrame.addStatus(statusData);
	}
	
	@Override
	public void onUnblock(User source, User unblockedUser) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onUnfavorite(User source, User target, Status unfavoritedStatus) {
		// TODO Auto-generated method stub
		
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

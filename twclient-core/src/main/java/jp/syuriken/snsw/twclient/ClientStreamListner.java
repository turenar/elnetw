package jp.syuriken.snsw.twclient;

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
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onDirectMessage(DirectMessage directMessage) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onException(Exception ex) {
		// TODO Auto-generated method stub
		ex.printStackTrace();
	}
	
	@Override
	public void onFavorite(User source, User target, Status favoritedStatus) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onFollow(User source, User followedUser) {
		// TODO Auto-generated method stub
		
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
		// TODO Auto-generated method stub
		
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

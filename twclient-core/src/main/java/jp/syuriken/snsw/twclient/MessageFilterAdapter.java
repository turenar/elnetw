package jp.syuriken.snsw.twclient;

import twitter4j.DirectMessage;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.User;

/**
 * TODO Megumi
 * 
 * @author $Author$
 */
public abstract class MessageFilterAdapter implements MessageFilter {
	
	@Override
	public boolean onChangeAccount(boolean forWrite) {
		return true;
	}
	
	@Override
	public boolean onClientMessage(String name, Object arg) {
		return true;
	}
	
	@Override
	public boolean onDeletionNotice(long directMessageId, long userId) {
		return true;
	}
	
	@Override
	public StatusDeletionNotice onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
		return statusDeletionNotice;
	}
	
	@Override
	public DirectMessage onDirectMessage(DirectMessage message) {
		return message;
	}
	
	@Override
	public boolean onException(Exception obj) {
		return true;
	}
	
	@Override
	public boolean onFavorite(User source, User target, Status favoritedStatus) {
		return true;
	}
	
	@Override
	public boolean onFollow(User source, User followedUser) {
		return true;
	}
	
	@Override
	public long[] onFriendList(long[] arr) {
		return arr;
	}
	
	@Override
	public boolean onRetweet(User source, User target, Status retweetedStatus) {
		return true;
	}
	
	@Override
	public Status onStatus(Status status) {
		return status;
	}
	
	@Override
	public boolean onStreamCleanUp() {
		return true;
	}
	
	@Override
	public boolean onStreamConnect() {
		return true;
	}
	
	@Override
	public boolean onStreamDisconnect() {
		return true;
	}
	
	@Override
	public boolean onTrackLimitationNotice(int numberOfLimitedStatuses) {
		return true;
	}
	
	@Override
	public boolean onUnfavorite(User source, User target, Status unfavoritedStatus) {
		return true;
	}
	
}

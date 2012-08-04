package jp.syuriken.snsw.twclient.filter;

import java.util.Date;

import twitter4j.DirectMessage;
import twitter4j.RateLimitStatus;
import twitter4j.User;

@SuppressWarnings("serial")
class TestMessage implements DirectMessage {
	
	private final long sender;
	
	private final long recipient;
	
	
	public TestMessage(long sender, long recipient) {
		this.sender = sender;
		this.recipient = recipient;
	}
	
	@Override
	public int getAccessLevel() {
		return 0;
	}
	
	@Override
	public Date getCreatedAt() {
		return null;
	}
	
	@Override
	public long getId() {
		return 0;
	}
	
	@Override
	public RateLimitStatus getRateLimitStatus() {
		return null;
	}
	
	@Override
	public User getRecipient() {
		return null;
	}
	
	@Override
	public long getRecipientId() {
		return recipient;
	}
	
	@Override
	public String getRecipientScreenName() {
		return null;
	}
	
	@Override
	public User getSender() {
		return null;
	}
	
	@Override
	public long getSenderId() {
		return sender;
	}
	
	@Override
	public String getSenderScreenName() {
		return null;
	}
	
	@Override
	public String getText() {
		return null;
	}
	
}
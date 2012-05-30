package jp.syuriken.snsw.twclient.internal;

import java.util.Date;

import twitter4j.DirectMessage;
import twitter4j.RateLimitStatus;
import twitter4j.User;

/**
 * 初期化時に読み込まれたDM。通知を発行しない。
 * 
 * @author $Author$
 */
@SuppressWarnings("serial")
public class InitialMessage implements DirectMessage {
	
	private final DirectMessage originalMessage;
	
	
	/**
	 * インスタンスを生成する。
	 * @param originalMessage 元のDM
	 */
	public InitialMessage(DirectMessage originalMessage) {
		this.originalMessage = originalMessage;
	}
	
	@Override
	public int getAccessLevel() {
		return originalMessage.getAccessLevel();
	}
	
	@Override
	public Date getCreatedAt() {
		return originalMessage.getCreatedAt();
	}
	
	@Override
	public long getId() {
		return originalMessage.getId();
	}
	
	@Override
	public RateLimitStatus getRateLimitStatus() {
		return originalMessage.getRateLimitStatus();
	}
	
	@Override
	public User getRecipient() {
		return originalMessage.getRecipient();
	}
	
	@Override
	public long getRecipientId() {
		return originalMessage.getRecipientId();
	}
	
	@Override
	public String getRecipientScreenName() {
		return originalMessage.getSenderScreenName();
	}
	
	@Override
	public User getSender() {
		return originalMessage.getSender();
	}
	
	@Override
	public long getSenderId() {
		return originalMessage.getSenderId();
	}
	
	@Override
	public String getSenderScreenName() {
		return originalMessage.getSenderScreenName();
	}
	
	@Override
	public String getText() {
		return originalMessage.getText();
	}
}

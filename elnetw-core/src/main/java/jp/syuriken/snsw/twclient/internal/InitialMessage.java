package jp.syuriken.snsw.twclient.internal;

import java.util.Date;

import twitter4j.DirectMessage;
import twitter4j.HashtagEntity;
import twitter4j.MediaEntity;
import twitter4j.RateLimitStatus;
import twitter4j.URLEntity;
import twitter4j.User;
import twitter4j.UserMentionEntity;

/**
 * 初期化時に読み込まれたDM。通知を発行しない。
 *
 * @author Turenar <snswinhaiku dot lo at gmail dot com>
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
	public HashtagEntity[] getHashtagEntities() {
		return originalMessage.getHashtagEntities();
	}

	@Override
	public long getId() {
		return originalMessage.getId();
	}

	@Override
	public MediaEntity[] getMediaEntities() {
		return originalMessage.getMediaEntities();
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

	@Override
	public URLEntity[] getURLEntities() {
		return originalMessage.getURLEntities();
	}

	@Override
	public UserMentionEntity[] getUserMentionEntities() {
		return originalMessage.getUserMentionEntities();
	}
}

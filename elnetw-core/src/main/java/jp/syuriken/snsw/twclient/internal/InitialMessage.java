/*
 * The MIT License (MIT)
 * Copyright (c) 2011-2014 Turenai Project
 *
 * Permission is hereby granted, free of charge,
 *  to any person obtaining a copy of this software and associated documentation files (the "Software"),
 *  to deal in the Software without restriction, including without limitation the rights to
 *  use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 *  and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 *  in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 *  INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 *  IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 *  DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package jp.syuriken.snsw.twclient.internal;

import java.util.Date;

import twitter4j.DirectMessage;
import twitter4j.HashtagEntity;
import twitter4j.MediaEntity;
import twitter4j.RateLimitStatus;
import twitter4j.SymbolEntity;
import twitter4j.URLEntity;
import twitter4j.User;
import twitter4j.UserMentionEntity;

/**
 * 初期化時に読み込まれたDM。通知を発行しない。
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class InitialMessage implements DirectMessage {

	private static final long serialVersionUID = -646279578339845549L;
	private final DirectMessage originalMessage;


	/**
	 * インスタンスを生成する。
	 *
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
	public SymbolEntity[] getSymbolEntities() {
		return originalMessage.getSymbolEntities();
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

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

package jp.syuriken.snsw.twclient.filter;

import java.util.Date;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import twitter4j.DirectMessage;
import twitter4j.HashtagEntity;
import twitter4j.MediaEntity;
import twitter4j.RateLimitStatus;
import twitter4j.SymbolEntity;
import twitter4j.URLEntity;
import twitter4j.User;
import twitter4j.UserMentionEntity;

@SuppressFBWarnings
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
	public HashtagEntity[] getHashtagEntities() {
		return null;
	}

	@Override
	public long getId() {
		return 0;
	}

	@Override
	public MediaEntity[] getMediaEntities() {
		return null;
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
	public SymbolEntity[] getSymbolEntities() {
		return new SymbolEntity[0];
	}

	@Override
	public String getText() {
		return null;
	}

	@Override
	public URLEntity[] getURLEntities() {
		return null;
	}

	@Override
	public UserMentionEntity[] getUserMentionEntities() {
		return null;
	}
}

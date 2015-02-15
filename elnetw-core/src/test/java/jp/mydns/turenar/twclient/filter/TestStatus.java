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

package jp.mydns.turenar.twclient.filter;

import java.util.Date;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jp.mydns.turenar.twclient.internal.NullStatus;
import twitter4j.GeoLocation;
import twitter4j.HashtagEntity;
import twitter4j.MediaEntity;
import twitter4j.Place;
import twitter4j.RateLimitStatus;
import twitter4j.Scopes;
import twitter4j.Status;
import twitter4j.SymbolEntity;
import twitter4j.URLEntity;
import twitter4j.User;
import twitter4j.UserMentionEntity;

@SuppressFBWarnings
@SuppressWarnings("serial")
/*package*/class TestStatus extends NullStatus {

	private final TestUser user;
	private final Status retweetedStatus;
	private final long inReplyToUserId;
	private final long id;


	public TestStatus(long id) {
		retweetedStatus = null;
		inReplyToUserId = -1;
		user = null;
		this.id = id;
	}

	public TestStatus(long createdUserId, Status retweetedStatus, long inReplyToUserId) {
		this.retweetedStatus = retweetedStatus;
		this.inReplyToUserId = inReplyToUserId;
		user = new TestUser(createdUserId);
		id = 0;
	}

	@Override
	public long getInReplyToUserId() {
		return inReplyToUserId;
	}

	@Override
	public TestUser getUser() {
		return user;
	}

	@Override
	public long getId() {
		return id;
	}

	@Override
	public Status getRetweetedStatus() {
		return retweetedStatus;
	}

	@Override
	public boolean isRetweet() {
		return retweetedStatus != null;
	}
}

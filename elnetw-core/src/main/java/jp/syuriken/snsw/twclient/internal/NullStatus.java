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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import twitter4j.GeoLocation;
import twitter4j.HashtagEntity;
import twitter4j.MediaEntity;
import twitter4j.Place;
import twitter4j.RateLimitStatus;
import twitter4j.Status;
import twitter4j.SymbolEntity;
import twitter4j.URLEntity;
import twitter4j.User;
import twitter4j.UserMentionEntity;

/**
 * 空のStatus実装
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
@SuppressFBWarnings("PZLA_PREFER_ZERO_LENGTH_ARRAYS")
public class NullStatus implements Status {

	private static final long serialVersionUID = -5283262110868599454L;
	/** 使いまわし用のインスタンス */
	public static final NullStatus INSTANCE = new NullStatus();

	@Override
	public int compareTo(Status o) {
		return 0;
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof NullStatus;
	}

	@Override
	public int getAccessLevel() {
		return -1;
	}

	@Override
	public long[] getContributors() {
		return null;
	}

	@Override
	public Date getCreatedAt() {
		return null;
	}

	@Override
	public long getCurrentUserRetweetId() {
		return 0;
	}

	@Override
	public int getFavoriteCount() {
		return 0;
	}

	@Override
	public GeoLocation getGeoLocation() {
		return null;
	}

	@Override
	public HashtagEntity[] getHashtagEntities() {
		return null;
	}

	@Override
	public long getId() {
		return -1;
	}

	@Override
	public String getInReplyToScreenName() {
		return null;
	}

	@Override
	public long getInReplyToStatusId() {
		return -1;
	}

	@Override
	public long getInReplyToUserId() {
		return -1;
	}

	@Override
	public String getIsoLanguageCode() {
		return null;
	}

	@Override
	public MediaEntity[] getMediaEntities() {
		return null;
	}

	@Override
	public Place getPlace() {
		return null;
	}

	@Override
	public RateLimitStatus getRateLimitStatus() {
		return null;
	}

	@Override
	public int getRetweetCount() {
		return -1;
	}

	@Override
	public Status getRetweetedStatus() {
		return null;
	}

	@Override
	public String getSource() {
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
	public User getUser() {
		return null;
	}

	@Override
	public UserMentionEntity[] getUserMentionEntities() {
		return null;
	}

	@Override
	public int hashCode() {
		return 0;
	}

	@Override
	public boolean isFavorited() {
		return false;
	}

	@Override
	public boolean isPossiblySensitive() {
		return false;
	}

	@Override
	public boolean isRetweet() {
		return false;
	}

	@Override
	public boolean isRetweeted() {
		return false;
	}

	@Override
	public boolean isRetweetedByMe() {
		return false;
	}

	@Override
	public boolean isTruncated() {
		return false;
	}
}

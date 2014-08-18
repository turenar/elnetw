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

package jp.syuriken.snsw.twclient.impl;

import java.util.ArrayList;

import twitter4j.PagableResponseList;
import twitter4j.RateLimitStatus;
import twitter4j.User;

/**
 * Pagable Response List implementation for test methods
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class UserPagableResponseListImpl extends ArrayList<User> implements PagableResponseList<User> {
	private long nextCursor = -1L;

	@Override
	public int getAccessLevel() {
		return 0;
	}

	@Override
	public long getNextCursor() {
		return nextCursor;
	}

	@Override
	public long getPreviousCursor() {
		return 0;
	}

	@Override
	public RateLimitStatus getRateLimitStatus() {
		return null;
	}

	@Override
	public boolean hasNext() {
		return nextCursor != -1L;
	}

	@Override
	public boolean hasPrevious() {
		return false;
	}

	public void setNextCursor(long nextCursor) {
		this.nextCursor = nextCursor;
	}
}

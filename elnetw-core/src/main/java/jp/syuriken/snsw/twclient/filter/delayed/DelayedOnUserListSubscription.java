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

package jp.syuriken.snsw.twclient.filter.delayed;

import jp.syuriken.snsw.twclient.filter.MessageFilter;
import twitter4j.User;
import twitter4j.UserList;

/*package*/ class DelayedOnUserListSubscription implements Runnable {
	private final MessageFilter filter;
	private final User subscriber;
	private final User listOwner;
	private final UserList list;

	public DelayedOnUserListSubscription(MessageFilter filter, User subscriber, User listOwner, UserList list) {
		this.filter = filter;
		this.subscriber = subscriber;
		this.listOwner = listOwner;
		this.list = list;
	}

	@Override
	public void run() {
		filter.onUserListSubscription(subscriber, listOwner, list);
	}
}

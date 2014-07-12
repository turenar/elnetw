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

package jp.syuriken.snsw.twclient.filter.query.prop;

import jp.syuriken.snsw.twclient.filter.IllegalSyntaxException;
import jp.syuriken.snsw.twclient.filter.query.QueryProperty;
import jp.syuriken.snsw.twclient.filter.query.QueryPropertyFactory;

/**
 * factory for jp.syuriken.snsw.twclient.filter.query.prop.*
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public final class StandardPropertyFactory implements QueryPropertyFactory {
	public static final StandardPropertyFactory SINGLETON = new StandardPropertyFactory();

	private StandardPropertyFactory() {
	}

	@Override
	public QueryProperty getInstance(String name, String operator, Object value) throws IllegalSyntaxException {
		switch (name) {
			case "userid":
			case "user_id":
				return new UserIdProperty(name, operator, value);
			case "in_reply_to":
			case "in_reply_to_userid":
			case "send_to":
				return new InReplyToUserIdProperty(name, operator, value);
			case "rtcount":
				return new RetweetCountProperty(name, operator, value);
			case "timediff":
				return new TimeDiffProperty(name, operator, value);
			case "retweeted":
				return new RetweetedProperty(name, operator, value);
			case "mine":
			case "my_tweet":
			case "my_dm":
				return new MyTweetProperty(name, operator, value);
			case "protected":
			case "is_protected":
				return new IsProtectedProperty(name, operator, value);
			case "verified":
			case "is_verified":
				return new IsVerifiedProperty(name, operator, value);
			case "status":
			case "is_status":
				return new IsStatusProperty(name, operator, value);
			case "dm":
			case "directmessage":
			case "direct_message":
			case "is_dm":
			case "is_directmessage":
			case "is_direct_message":
				return new IsDirectMessageProperty(name, operator, value);
			case "user":
			case "author":
			case "screen_name":
				return new ScreenNameProperty(name, operator, value);
			case "text":
				return new StatusTextProperty(name, operator, value);
			case "client":
				return new ClientProperty(name, operator, value);
			case "in_list":
				return new InListProperty(name, operator, value);
			default:
				throw new IllegalSyntaxException("Not supported factor");
		}
	}
}

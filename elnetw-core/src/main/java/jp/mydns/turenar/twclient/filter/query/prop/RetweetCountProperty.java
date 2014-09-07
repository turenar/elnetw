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

package jp.mydns.turenar.twclient.filter.query.prop;

import jp.mydns.turenar.twclient.filter.IllegalSyntaxException;
import jp.mydns.turenar.twclient.filter.query.QueryProperty;
import twitter4j.DirectMessage;
import twitter4j.Status;

/**
 * retweet count or 0 (DM)
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class RetweetCountProperty extends AbstractIntArgProperty implements QueryProperty {

	/**
	 * インスタンスを生成する。
	 *
	 * @param name     プロパティ名
	 * @param operator 演算子文字列。ない場合は null。
	 * @param value    比較する値。ない場合は null。
	 * @throws jp.mydns.turenar.twclient.filter.IllegalSyntaxException 正しくない文法のクエリ
	 */
	public RetweetCountProperty(String name, String operator, Object value)
			throws IllegalSyntaxException {
		super(name, operator, value);
	}

	@Override
	public long getPropertyValue(DirectMessage directMessage) {
		return 0L;
	}

	@Override
	public long getPropertyValue(Status status) {
		return (long) status.getRetweetCount();
	}
}

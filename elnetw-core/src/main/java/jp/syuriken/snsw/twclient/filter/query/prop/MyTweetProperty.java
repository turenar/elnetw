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

import jp.syuriken.snsw.twclient.ClientConfiguration;
import jp.syuriken.snsw.twclient.filter.IllegalSyntaxException;
import twitter4j.DirectMessage;
import twitter4j.Status;

/**
 * is that my tweet or message?
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class MyTweetProperty extends AbstractBoolArgProperty {
	private final ClientConfiguration configuration;

	/**
	 * インスタンスを生成する。
	 *
	 * @param name     プロパティ名
	 * @param operator 演算子文字列。ない場合は null。
	 * @param value    比較する値。ない場合は null。
	 * @throws jp.syuriken.snsw.twclient.filter.IllegalSyntaxException 正しくない文法のクエリ
	 */
	public MyTweetProperty(String name, String operator, Object value)
			throws IllegalSyntaxException {
		super(name, operator, value);
		this.configuration = ClientConfiguration.getInstance();
	}

	@Override
	public boolean getPropertyValue(DirectMessage directMessage) {
		return configuration.isMyAccount(directMessage.getSenderId());
	}

	@Override
	public boolean getPropertyValue(Status status) {
		return configuration.isMyAccount(status.getUser().getId());
	}
}

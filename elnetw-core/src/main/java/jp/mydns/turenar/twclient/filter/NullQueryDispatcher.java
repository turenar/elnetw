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

import jp.mydns.turenar.twclient.filter.query.QueryDispatcherBase;
import twitter4j.DirectMessage;
import twitter4j.Status;

/**
 * 何もしないフィルタ
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class NullQueryDispatcher implements QueryDispatcherBase {
	private static final QueryDispatcherBase instance = new NullQueryDispatcher();

	/**
	 * 唯一インスタンスを取得する。
	 *
	 * @return インスタンス
	 */
	public static QueryDispatcherBase getInstance() {
		return instance;
	}

	private NullQueryDispatcher() {
	}

	@Override
	public boolean filter(DirectMessage directMessage) {
		return false;
	}

	@Override
	public boolean filter(Status status) {
		return false;
	}

	@Override
	public void init() {
	}
}

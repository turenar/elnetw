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

package jp.syuriken.snsw.twclient.filter.query.func;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jp.syuriken.snsw.twclient.filter.IllegalSyntaxException;
import jp.syuriken.snsw.twclient.filter.query.QueryDispatcherBase;
import twitter4j.DirectMessage;
import twitter4j.Status;

/**
 * どれかマッチすることを確認するフィルタ関数
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class OrQueryFunction extends AbstractQueryFunction {

	/**
	 * インスタンスを生成する。
	 *
	 * @param functionName 関数名
	 * @param child        子要素の配列
	 * @throws IllegalSyntaxException エラー
	 */
	@SuppressFBWarnings("EI_EXPOSE_REP2")
	public OrQueryFunction(String functionName, QueryDispatcherBase[] child) throws IllegalSyntaxException {
		if (child.length == 0) {
			throw new IllegalSyntaxException("func<" + functionName + ">: 子要素の個数が0です");
		}
		this.child = child;
	}

	@Override
	public boolean filter(DirectMessage directMessage) {
		for (QueryDispatcherBase operator : child) {
			if (operator.filter(directMessage)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean filter(Status status) {
		for (QueryDispatcherBase operator : child) {
			if (operator.filter(status)) {
				return true;
			}
		}
		return false;
	}
}

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

import jp.syuriken.snsw.twclient.filter.IllegalSyntaxException;
import jp.syuriken.snsw.twclient.filter.query.QueryDispatcherBase;
import jp.syuriken.snsw.twclient.filter.query.QueryFunction;
import twitter4j.DirectMessage;
import twitter4j.Status;

/** 'if' filter function: if(expr, trueCond [, falseCond]) */
public class IfQueryFunction implements QueryFunction {

	private final QueryDispatcherBase trueCond;
	private final QueryDispatcherBase falseCond;
	private QueryDispatcherBase expr;

	/**
	 * インスタンスを生成する。
	 *
	 * @param name  関数名
	 * @param child 子要素
	 * @throws jp.syuriken.snsw.twclient.filter.IllegalSyntaxException エラー
	 */
	public IfQueryFunction(String name, QueryDispatcherBase[] child) throws IllegalSyntaxException {
		int len = child.length;
		if (len < 2 || len > 3) {
			throw new IllegalSyntaxException("func<" + name + "> の引数は2つまたは3つでなければなりません");
		}
		this.expr = child[0];
		this.trueCond = child[1];
		this.falseCond = len == 3 ? child[2] : null;
	}

	@Override
	public boolean filter(DirectMessage directMessage) {
		if (expr.filter(directMessage)) {
			return trueCond.filter(directMessage);
		} else {
			return falseCond != null && falseCond.filter(directMessage);
		}
	}

	@Override
	public boolean filter(Status status) {
		if (expr.filter(status)) {
			return trueCond.filter(status);
		} else {
			return falseCond != null && falseCond.filter(status);
		}
	}
}
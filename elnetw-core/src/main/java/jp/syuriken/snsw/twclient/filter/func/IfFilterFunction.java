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
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 *  INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 *  IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 *  DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package jp.syuriken.snsw.twclient.filter.func;

import java.lang.reflect.Constructor;

import jp.syuriken.snsw.twclient.filter.FilterDispatcherBase;
import jp.syuriken.snsw.twclient.filter.FilterFunction;
import jp.syuriken.snsw.twclient.filter.IllegalSyntaxException;
import twitter4j.DirectMessage;
import twitter4j.Status;

/** 'if' filter function: if(expr, trueCond [, falseCond]) */
public class IfFilterFunction implements FilterFunction {

	private static Constructor<IfFilterFunction> constructor;

	/**
	 * ファクトリーメソッドを取得する。
	 *
	 * @return コンストラクタ
	 */
	public static Constructor<? extends FilterFunction> getFactory() {
		return constructor;
	}

	private final FilterDispatcherBase trueCond;
	private final FilterDispatcherBase falseCond;
	private FilterDispatcherBase expr;

	static {
		try {
			constructor = IfFilterFunction.class.getConstructor(String.class, FilterDispatcherBase[].class);
		} catch (Exception e) {
			throw new AssertionError(e);
		}
	}

	/**
	 * インスタンスを生成する。
	 *
	 * @param name  関数名
	 * @param child 子要素
	 * @throws jp.syuriken.snsw.twclient.filter.IllegalSyntaxException エラー
	 */
	public IfFilterFunction(String name, FilterDispatcherBase[] child) throws IllegalSyntaxException {
		int len = child.length;
		if (len < 2 || len > 3) {
			throw new IllegalSyntaxException("func<if> の引数は2つまたは3つでなければなりません");
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
			return falseCond == null ? false : falseCond.filter(directMessage);
		}
	}

	@Override
	public boolean filter(Status status) {
		if (expr.filter(status)) {
			return trueCond.filter(status);
		} else {
			return falseCond == null ? false : falseCond.filter(status);
		}
	}
}

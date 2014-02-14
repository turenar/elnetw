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


/** extract: {@link jp.syuriken.snsw.twclient.filter.FilterEditFrame}用 */
public class ExtractFilterFunction implements FilterFunction {

	private static Constructor<ExtractFilterFunction> constructor;

	/**
	 * コンストラクタを取得する。
	 *
	 * @return コンストラクタ
	 */
	public static Constructor<ExtractFilterFunction> getFactory() {
		return constructor;
	}

	private final FilterDispatcherBase child;

	static {
		try {
			constructor = ExtractFilterFunction.class.getConstructor(String.class, FilterDispatcherBase[].class);
		} catch (Exception e) {
			throw new AssertionError(e);
		}
	}

	/**
	 * インスタンスを生成する。
	 *
	 * @param functionName 関数名
	 * @param child        子要素の配列
	 * @throws IllegalSyntaxException エラー
	 */
	public ExtractFilterFunction(String functionName, FilterDispatcherBase[] child) throws IllegalSyntaxException {
		int length = child.length;
		if (length == 0) {
			this.child = null;
		} else if (length == 1) {
			this.child = child[0];
		} else {
			throw new IllegalSyntaxException("func<" + functionName + "> の引数は一つでなければなりません");
		}
	}

	@Override
	public boolean filter(DirectMessage directMessage) {
		FilterDispatcherBase child = this.child;
		if (child == null) {
			return true;
		} else {
			return child.filter(directMessage) == false;
		}
	}

	@Override
	public boolean filter(Status status) {
		FilterDispatcherBase child = this.child;
		if (child == null) {
			return true;
		} else {
			return child.filter(status) == false;
		}
	}
}

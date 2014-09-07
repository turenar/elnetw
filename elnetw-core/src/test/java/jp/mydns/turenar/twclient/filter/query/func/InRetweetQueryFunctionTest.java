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

package jp.mydns.turenar.twclient.filter.query.func;

import jp.mydns.turenar.twclient.filter.FilterConstants;
import jp.mydns.turenar.twclient.filter.IllegalSyntaxException;
import jp.mydns.turenar.twclient.filter.query.QueryDispatcherBase;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * TODO tyanar
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class InRetweetQueryFunctionTest extends FilterConstants {

	/** コンストラクタのテスト */
	@Test
	public void testConstructor() {
		try {
			new InRetweetQueryFunction("inrt", new QueryDispatcherBase[]{});
			fail("childの個数を無視したよう");
		} catch (IllegalSyntaxException e) {
			// do nothing
		}

		try {
			new InRetweetQueryFunction("inrt", thisDispatcher);
		} catch (IllegalSyntaxException e) {
			throw new AssertionError(e);
		}

		QueryDispatcherBase[] arr = new QueryDispatcherBase[2];
		try {
			new InRetweetQueryFunction("inrt", arr);
			fail("childの個数を無視したよう");
		} catch (IllegalSyntaxException e) {
			// do nothing
		}
	}

	/**
	 * {@link InRetweetQueryFunction#filter(twitter4j.DirectMessage)} のためのテスト・メソッド。
	 *
	 * @throws IllegalSyntaxException エラー
	 */
	@Test
	public void testFilterDirectMessage() throws IllegalSyntaxException {
		InRetweetQueryFunction filterFunction = new InRetweetQueryFunction("inrt", thisDispatcher);

		assertFalse(filterFunction.filter(DM_1));
		assertFilteringObject(DM_1);
	}

	/**
	 * {@link InRetweetQueryFunction#filter(twitter4j.Status)} のためのテスト・メソッド。
	 *
	 * @throws IllegalSyntaxException エラー
	 */
	@Test
	public void testFilterStatus() throws IllegalSyntaxException {
		InRetweetQueryFunction filterFunction = new InRetweetQueryFunction("inrt", thisDispatcher);

		assertFalse(filterFunction.filter(STATUS_1));
		assertFilteringObject(STATUS_1);

		assertFalse(filterFunction.filter(STATUS_2));
		assertFilteringObject(STATUS_2);

		assertFalse(filterFunction.filter(STATUS_3));
		assertFilteringObject(STATUS_3);

		assertFalse(filterFunction.filter(STATUS_4));
		assertFilteringObject(STATUS_4.getRetweetedStatus());
	}
}

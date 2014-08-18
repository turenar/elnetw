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

import jp.syuriken.snsw.twclient.filter.FilterConstants;
import jp.syuriken.snsw.twclient.filter.IllegalSyntaxException;
import jp.syuriken.snsw.twclient.filter.query.QueryDispatcherBase;
import org.junit.Test;
import twitter4j.DirectMessage;
import twitter4j.Status;

import static org.junit.Assert.*;

/** Test for IfFilterFunction */
public class IfQueryFunctionTest extends FilterConstants {

	private void test(Object obj) throws IllegalSyntaxException {
		test1(obj, false, false, false);
		test1(obj, false, false, true);
		test1(obj, false, true, false);
		test1(obj, false, true, true);
		test1(obj, true, false, false);
		test1(obj, true, false, true);
		test1(obj, true, true, false);
		test1(obj, true, true, true);
		test1(obj, false, false);
		test1(obj, false, true);
		test1(obj, true, false);
		test1(obj, true, true);
	}

	private void test1(Object obj, boolean expr, boolean trueCond, boolean falseCond) throws IllegalSyntaxException {
		IfQueryFunction ifFilterFunction = new IfQueryFunction("if", new QueryDispatcherBase[]{
				expr ? TRUE_DISPATCHER : FALSE_DISPATCHER,
				trueCond ? TRUE_DISPATCHER : FALSE_DISPATCHER,
				falseCond ? TRUE_DISPATCHER : FALSE_DISPATCHER
		});
		if (obj instanceof Status) {
			assertEquals(expr ? trueCond : falseCond, ifFilterFunction.filter((Status) obj));
		} else if (obj instanceof DirectMessage) {
			assertEquals(expr ? trueCond : falseCond, ifFilterFunction.filter((DirectMessage) obj));
		}
	}

	private void test1(Object obj, boolean expr, boolean trueCond) throws IllegalSyntaxException {
		IfQueryFunction ifFilterFunction = new IfQueryFunction("if", new QueryDispatcherBase[]{
				expr ? TRUE_DISPATCHER : FALSE_DISPATCHER,
				trueCond ? TRUE_DISPATCHER : FALSE_DISPATCHER,
		});
		if (obj instanceof Status) {
			assertEquals(expr && trueCond, ifFilterFunction.filter((Status) obj));
		} else if (obj instanceof DirectMessage) {
			assertEquals(expr && trueCond, ifFilterFunction.filter((DirectMessage) obj));
		}
	}

	/** コンストラクタのテスト */
	@Test
	public void testConstructor() {
		try {
			new IfQueryFunction("if", new QueryDispatcherBase[]{});
			fail("childの個数を無視したよう");
		} catch (IllegalSyntaxException e) {
			// do nothing
		}

		QueryDispatcherBase[] arr = new QueryDispatcherBase[4];
		try {
			new IfQueryFunction("if", arr);
			fail("childの個数を無視したよう");
		} catch (IllegalSyntaxException e) {
			// do nothing
		}
	}

	/**
	 * {@link IfQueryFunction#filter(twitter4j.DirectMessage)} のためのテスト・メソッド。
	 *
	 * @throws IllegalSyntaxException エラー
	 */
	@Test
	public void testFilterDirectMessage() throws IllegalSyntaxException {
		test(DM_1);
	}

	/**
	 * {@link IfQueryFunction#filter(twitter4j.Status)} のためのテスト・メソッド。
	 *
	 * @throws IllegalSyntaxException エラー
	 */
	@Test
	public void testFilterStatus() throws IllegalSyntaxException {
		test(STATUS_1);
		test(STATUS_2);
		test(STATUS_3);
		test(STATUS_4);
		test(STATUS_5);
	}
}

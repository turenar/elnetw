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

package jp.syuriken.snsw.twclient.filter.func;

import jp.syuriken.snsw.twclient.filter.FilterConstants;
import jp.syuriken.snsw.twclient.filter.IllegalSyntaxException;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * {@link AndFilterFunction}のためのテスト
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class AndFilterFunctionTest extends FilterConstants {

	private AndFilterFunction get(boolean... bools) throws IllegalSyntaxException {
		return new AndFilterFunction("and", getDispatchers(bools));
	}

	/**
	 * コンストラクタのテスト
	 *
	 * @throws IllegalSyntaxException エラー
	 */
	@Test
	public void testConstructor() throws IllegalSyntaxException {
		try {
			get();
			fail("子要素の個数を無視したよう");
		} catch (IllegalSyntaxException e) {
			// do nothing
		}
		try {
			get(true);
			get(true, false, true);
		} catch (IllegalSyntaxException ex) {
			throw ex;
		}
	}

	/**
	 * {@link jp.syuriken.snsw.twclient.filter.func.AndFilterFunction#filter(twitter4j.DirectMessage)} のためのテスト・メソッド。
	 *
	 * @throws IllegalSyntaxException エラー
	 */
	@Test
	public void testFilterDirectMessage() throws IllegalSyntaxException {
		assertTrue(get(true).filter(DM_1));
		assertFalse(get(false).filter(DM_1));
		assertTrue(get(true, true).filter(DM_1));
		assertFalse(get(true, false).filter(DM_1));
		assertFalse(get(false, true).filter(DM_1));
		assertFalse(get(false, false).filter(DM_1));
		assertTrue(get(true, true, true, true, true, true, true, true, true, true).filter(DM_1));
		assertFalse(get(true, false, false, true, true, true, false, true, true, true).filter(DM_1));
		assertFalse(get(true, false, false, false, false, false, false, false, false, false).filter(DM_1));
		assertFalse(get(false, false, false, false, false, false, false, false, false, false).filter(DM_1));
	}

	/**
	 * {@link jp.syuriken.snsw.twclient.filter.func.AndFilterFunction#filter(twitter4j.Status)} のためのテスト・メソッド。
	 *
	 * @throws IllegalSyntaxException エラー
	 */
	@Test
	public void testFilterStatus() throws IllegalSyntaxException {
		assertTrue(get(true).filter(STATUS_1));
		assertFalse(get(false).filter(STATUS_1));
		assertTrue(get(true, true).filter(STATUS_1));
		assertFalse(get(true, false).filter(STATUS_1));
		assertFalse(get(false, true).filter(STATUS_1));
		assertFalse(get(false, false).filter(STATUS_1));
		assertTrue(get(true, true, true, true, true, true, true, true, true, true).filter(STATUS_1));
		assertFalse(get(true, false, false, true, true, true, false, true, true, true).filter(STATUS_1));
		assertFalse(get(true, false, false, false, false, false, false, false, false, false).filter(STATUS_1));
		assertFalse(get(false, false, false, false, false, false, false, false, false, false).filter(STATUS_1));
	}
}

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

import java.util.regex.Pattern;

import com.twitter.Regex;
import jp.mydns.turenar.twclient.filter.query.QueryOperator;
import org.junit.Test;

import static jp.mydns.turenar.twclient.filter.query.QueryOperator.EQ;
import static jp.mydns.turenar.twclient.filter.query.QueryOperator.GT;
import static jp.mydns.turenar.twclient.filter.query.QueryOperator.GTE;
import static jp.mydns.turenar.twclient.filter.query.QueryOperator.IS;
import static jp.mydns.turenar.twclient.filter.query.QueryOperator.IS_NOT;
import static jp.mydns.turenar.twclient.filter.query.QueryOperator.LT;
import static jp.mydns.turenar.twclient.filter.query.QueryOperator.LTE;
import static jp.mydns.turenar.twclient.filter.query.QueryOperator.NE;
import static jp.mydns.turenar.twclient.filter.query.QueryOperator.compileOperatorBool;
import static jp.mydns.turenar.twclient.filter.query.QueryOperator.compileOperatorInt;
import static org.junit.Assert.*;

/**
 * {@link jp.mydns.turenar.twclient.filter.query.QueryOperator}のためのテスト
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class QueryOperatorTest {

	private void check(boolean expected, String haystack, String needle) throws RuntimeException,
			IllegalSyntaxException {
		assertTrue(expected == EQ.compare(haystack, QueryOperator.compileValueString(needle)));
		assertFalse(expected == NE.compare(haystack, QueryOperator.compileValueString(needle)));
	}

	/**
	 * {@link QueryOperator#compare(String, Object)} のためのテスト・メソッド
	 * 完全一致
	 *
	 * @throws IllegalSyntaxException エラー
	 * @throws RuntimeException       エラー
	 */
	@Test
	public void compareStringExactlyMatch() throws RuntimeException, IllegalSyntaxException {
		check(false, "abcdefghijk", "aiueo");
		check(true, "abcdefghijk", "abcdefghijk");
		check(false, "abcdefghijk", "abcdef");
		check(false, "abcdefghijk", "fghijk");
		check(false, "abcdefghijk", "def");
	}

	/**
	 * {@link QueryOperator#compare(String, Object)} のためのテスト・メソッド
	 * 先頭一致
	 *
	 * @throws IllegalSyntaxException エラー
	 * @throws RuntimeException       エラー
	 */
	@Test
	public void compareStringFirstMatch() throws RuntimeException, IllegalSyntaxException {
		check(false, "abcdefghijk", "aiueo*");
		check(true, "abcdefghijk", "abcdefghijk*");
		check(true, "abcdefghijk", "abcdef*");
		check(false, "abcdefghijk", "fghijk*");
		check(false, "abcdefghijk", "def*");
	}

	/**
	 * {@link QueryOperator#compare(String, Object)} のためのテスト・メソッド
	 * 末尾一致
	 *
	 * @throws IllegalSyntaxException エラー
	 * @throws RuntimeException       エラー
	 */
	@Test
	public void compareStringLastMatch() throws RuntimeException, IllegalSyntaxException {
		check(false, "abcdefghijk", "*aiueo");
		check(true, "abcdefghijk", "*abcdefghijk");
		check(false, "abcdefghijk", "*abcdef");
		check(true, "abcdefghijk", "*fghijk");
		check(false, "abcdefghijk", "*def");
	}

	/**
	 * {@link QueryOperator#compare(String, Object)} のためのテスト・メソッド
	 * 部分一致
	 *
	 * @throws IllegalSyntaxException エラー
	 * @throws RuntimeException       エラー
	 */
	@Test
	public void compareStringPartialMatch() throws RuntimeException, IllegalSyntaxException {
		check(false, "abcdefghijk", "*aiueo*");
		check(true, "abcdefghijk", "*abcdefghijk*");
		check(true, "abcdefghijk", "*abcdef*");
		check(true, "abcdefghijk", "*fghijk*");
		check(true, "abcdefghijk", "*def*");
	}

	/**
	 * {@link QueryOperator#compare(String, Object)} のためのテスト・メソッド
	 * 正規表現
	 *
	 * @throws IllegalSyntaxException エラー
	 * @throws RuntimeException       エラー
	 */
	@Test
	public void compareStringRegexMatch() throws RuntimeException, IllegalSyntaxException {
		assertTrue(EQ.compare("http://twitter.com/ture7", Regex.VALID_URL));
		assertFalse(NE.compare("http://twitter.com/ture7", Regex.VALID_URL));

		check(true, "turenar died.", "/turenar.died\\.");
		check(true, "addd", "/^ad");
		check(true, "addd", "/dd$");
		check(false, "turenar died.", "/^[^t]");
	}

	/**
	 * {@link QueryOperator#compare(String, Object)} のためのテスト・メソッド
	 * 予期しない演算子
	 *
	 * @throws IllegalSyntaxException エラー
	 * @throws RuntimeException       エラー
	 */
	@Test
	public void compareStringUnexpectedOperator() throws RuntimeException, IllegalSyntaxException {
		try {
			IS.compare("aiueo", "aiueo");
			fail();
		} catch (RuntimeException e) {
			// success
		}
		try {
			IS_NOT.compare("aiueo", "aiueo");
			fail();
		} catch (RuntimeException e) {
			// success
		}
		try {
			LT.compare("aiueo", "aiueo");
			fail();
		} catch (RuntimeException e) {
			// success
		}
		try {
			LTE.compare("aiueo", "aiueo");
			fail();
		} catch (RuntimeException e) {
			// success
		}
		try {
			GT.compare("aiueo", "aiueo");
			fail();
		} catch (RuntimeException e) {
			// success
		}
		try {
			GTE.compare("aiueo", "aiueo");
			fail();
		} catch (RuntimeException e) {
			// success
		}
	}

	/**
	 * {@link QueryOperator#compileValueString(String)} のためのテスト・メソッド
	 *
	 * @throws IllegalSyntaxException エラー
	 */
	@Test
	public void compileValueString() throws IllegalSyntaxException {
		assertTrue(QueryOperator.compileValueString("") instanceof String);
		assertTrue(QueryOperator.compileValueString("aiueo") instanceof String);
		assertTrue(QueryOperator.compileValueString("/.+") instanceof Pattern);
	}

	/** {@link QueryOperator#compare(boolean, boolean)} のためのテスト・メソッド。 */
	@Test
	public void testCompareBooleanBoolean() {
		assertFalse(IS.compare(false, false));
		assertFalse(IS.compare(false, true));
		assertTrue(IS.compare(true, false));
		assertTrue(IS.compare(true, true));
		assertTrue(IS_NOT.compare(false, false));
		assertTrue(IS_NOT.compare(false, true));
		assertFalse(IS_NOT.compare(true, false));
		assertFalse(IS_NOT.compare(true, true));
		assertTrue(EQ.compare(false, false));
		assertFalse(EQ.compare(false, true));
		assertFalse(EQ.compare(true, false));
		assertTrue(EQ.compare(true, true));
		assertFalse(NE.compare(false, false));
		assertTrue(NE.compare(false, true));
		assertTrue(NE.compare(true, false));
		assertFalse(NE.compare(true, true));

		try {
			LT.compare(true, true);
			fail();
		} catch (RuntimeException e) {
			// success
		}
		try {
			LTE.compare(true, true);
			fail();
		} catch (RuntimeException e) {
			// success
		}
		try {
			GT.compare(true, true);
			fail();
		} catch (RuntimeException e) {
			// success
		}
		try {
			GTE.compare(true, true);
			fail();
		} catch (RuntimeException e) {
			// success
		}
	}

	/** {@link QueryOperator#compare(long, long)} のためのテスト・メソッド。 */
	@Test
	public void testCompareLongLong() {
		assertFalse(EQ.compare(100, -200));
		assertFalse(EQ.compare(-200, 100));
		assertTrue(EQ.compare(0, 0));
		assertTrue(NE.compare(100, -200));
		assertTrue(NE.compare(-200, 100));
		assertFalse(NE.compare(0, 0));
		assertFalse(LT.compare(100, -200));
		assertTrue(LT.compare(-200, 100));
		assertFalse(LT.compare(0, 0));
		assertFalse(LTE.compare(100, -200));
		assertTrue(LTE.compare(-200, 100));
		assertTrue(LTE.compare(0, 0));
		assertTrue(GT.compare(100, -200));
		assertFalse(GT.compare(-200, 100));
		assertFalse(GT.compare(0, 0));
		assertTrue(GTE.compare(100, -200));
		assertFalse(GTE.compare(-200, 100));
		assertTrue(GTE.compare(0, 0));
	}

	/** {@link QueryOperator#compileOperatorBool(java.lang.String)} のためのテスト・メソッド。 */
	@Test
	public void testCompileOperatorBool() {
		assertEquals(compileOperatorBool("?"), IS);
		assertEquals(compileOperatorBool("!?"), IS_NOT);
		assertEquals(compileOperatorBool(":"), EQ);
		assertEquals(compileOperatorBool("="), EQ);
		assertEquals(compileOperatorBool("=="), EQ);
		assertEquals(compileOperatorBool("!:"), NE);
		assertEquals(compileOperatorBool("!="), NE);
		assertEquals(compileOperatorBool(null), IS);
		assertNull(compileOperatorBool(">"));
		assertNull(compileOperatorBool("<"));
		assertNull(compileOperatorBool("<="));
		assertNull(compileOperatorBool(">="));
	}

	/** {@link QueryOperator#compileOperatorInt(java.lang.String)} のためのテスト・メソッド。 */
	@Test
	public void testCompileOperatorInt() {
		assertEquals(compileOperatorInt(":"), EQ);
		assertEquals(compileOperatorInt("="), EQ);
		assertEquals(compileOperatorInt("=="), EQ);
		assertEquals(compileOperatorInt("!:"), NE);
		assertEquals(compileOperatorInt("!="), NE);
		assertEquals(compileOperatorInt("<"), LT);
		assertEquals(compileOperatorInt("<="), LTE);
		assertEquals(compileOperatorInt(">"), GT);
		assertEquals(compileOperatorInt(">="), GTE);
		assertNull(compileOperatorInt("?"));
		assertNull(compileOperatorInt("!?"));
	}

	/**
	 * {@link QueryOperator#compileValueBool(java.lang.String, java.lang.String)} のためのテスト・メソッド。
	 *
	 * @throws IllegalSyntaxException エラー
	 */
	@Test
	public void testCompileValueBool() throws IllegalSyntaxException {
		IS.compileValueBool("**", null);
		IS_NOT.compileValueBool("**", null);

		assertTrue(EQ.compileValueBool("**", "true"));
		assertFalse(EQ.compileValueBool("**", "false"));
		assertTrue(EQ.compileValueBool("**", "yes"));
		assertFalse(EQ.compileValueBool("**", "no"));

		assertTrue(EQ.compileValueBool("**", "true"));
		assertTrue(NE.compileValueBool("**", "true"));

		// value must null
		try {
			IS.compileValueBool("**", "true");
			fail();
		} catch (IllegalSyntaxException e) {
			// success
		}
		try {
			IS_NOT.compileValueBool("**", "true");
			fail();
		} catch (IllegalSyntaxException e) {
			// success
		}
		// not correct operator for boolean
		try {
			LT.compileValueBool("**", "true");
			fail();
		} catch (IllegalSyntaxException e) {
			// success
		}
		try {
			LTE.compileValueBool("**", "true");
			fail();
		} catch (IllegalSyntaxException e) {
			// success
		}
		try {
			GT.compileValueBool("**", "true");
			fail();
		} catch (IllegalSyntaxException e) {
			// success
		}
		try {
			GTE.compileValueBool("**", "true");
			fail();
		} catch (IllegalSyntaxException e) {
			// success
		}
	}
}

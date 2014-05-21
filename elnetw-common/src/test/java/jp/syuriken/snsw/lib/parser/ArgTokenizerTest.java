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

package jp.syuriken.snsw.lib.parser;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test for ArgTokenizer
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class ArgTokenizerTest {
	private ArgTokenizer t(String... args) {
		return new ArgTokenizer(args);
	}

	@Test
	public void testTokenizer0() throws Exception {
		ArgTokenizer tokenizer = t();
		assertFalse(tokenizer.next());
	}

	@Test
	public void testTokenizer1A() throws Exception {
		ArgTokenizer tokenizer = t("--hoge=fuga");
		assertTrue(tokenizer.next());
		assertEquals("--hoge", tokenizer.getOpt());
		assertEquals("fuga", tokenizer.getArg());
		tokenizer.consumeArg();
		assertFalse(tokenizer.next());
	}

	@Test
	public void testTokenizer1B() throws Exception {
		ArgTokenizer tokenizer = t("--hoge", "fuga");
		assertTrue(tokenizer.next());
		assertEquals("--hoge", tokenizer.getOpt());
		assertEquals("fuga", tokenizer.getArg());
		tokenizer.consumeArg();
		assertFalse(tokenizer.next());
	}

	@Test
	public void testTokenizer1C() throws Exception {
		ArgTokenizer tokenizer = t("--hoge");
		assertTrue(tokenizer.next());
		assertEquals("--hoge", tokenizer.getOpt());
		assertNull(tokenizer.getArg());
		tokenizer.consumeArg();
		assertFalse(tokenizer.next());
	}

	@Test
	public void testTokenizer1D1() throws Exception {
		ArgTokenizer tokenizer = t("-ab");
		assertTrue(tokenizer.next());
		assertEquals("-a", tokenizer.getOpt());
		assertEquals("b", tokenizer.getArg());
		assertTrue(tokenizer.next());
		assertEquals("-b", tokenizer.getOpt());
		assertNull(tokenizer.getArg());
		tokenizer.consumeArg();
		assertFalse(tokenizer.next());
	}

	@Test
	public void testTokenizer1D2() throws Exception {
		ArgTokenizer tokenizer = t("-ab");
		assertTrue(tokenizer.next());
		assertEquals("-a", tokenizer.getOpt());
		assertEquals("b", tokenizer.getArg());
		tokenizer.consumeArg();
		assertFalse(tokenizer.next());
	}

	@Test
	public void testTokenizer1E() throws Exception {
		ArgTokenizer tokenizer = t("-a");
		assertTrue(tokenizer.next());
		assertEquals("-a", tokenizer.getOpt());
		assertNull(tokenizer.getArg());
		assertFalse(tokenizer.next());
	}

	@Test
	public void testTokenizer1F() throws Exception {
		ArgTokenizer tokenizer = t("--");
		assertTrue(tokenizer.next());
		assertEquals("--", tokenizer.getOpt());
		assertNull(tokenizer.getArg());
		tokenizer.consumeArg();
		assertFalse(tokenizer.next());
	}

	@Test
	public void testTokenizer1G() throws Exception {
		ArgTokenizer tokenizer = t("argument");
		assertTrue(tokenizer.next());
		assertNull(tokenizer.getOpt());
		assertEquals("argument", tokenizer.getArg());
		tokenizer.consumeArg();
		assertFalse(tokenizer.next());
	}

	@Test
	public void testTokenizer2Complex() throws Exception {
		ArgTokenizer tokenizer = t("--hoge=fuga", "--hoge", "fuga", "-w13", "-rf", "-a", "astronauts", "--", "-j4",
				"--extra");
		assertTrue(tokenizer.next());
		assertEquals("--hoge", tokenizer.getOpt());
		assertEquals("fuga", tokenizer.getArg());
		tokenizer.consumeArg();
		assertTrue(tokenizer.next());
		assertEquals("--hoge", tokenizer.getOpt());
		assertEquals("fuga", tokenizer.getArg());
		tokenizer.consumeArg();
		assertTrue(tokenizer.next());
		assertEquals("-w", tokenizer.getOpt());
		assertEquals("13", tokenizer.getArg());
		tokenizer.consumeArg();
		assertTrue(tokenizer.next());
		assertEquals("-r", tokenizer.getOpt());
		assertTrue(tokenizer.next());
		assertEquals("-f", tokenizer.getOpt());
		assertEquals("-a", tokenizer.getArg());
		assertTrue(tokenizer.next());
		assertEquals("-a", tokenizer.getOpt());
		assertTrue(tokenizer.next());
		assertEquals(null, tokenizer.getOpt());
		assertEquals("astronauts", tokenizer.getArg());
		tokenizer.consumeArg();
		assertTrue(tokenizer.next());
		assertEquals("--", tokenizer.getOpt());
		assertEquals(null, tokenizer.getArg());
		tokenizer.consumeArg();
		assertTrue(tokenizer.next());
		assertEquals(null, tokenizer.getOpt());
		assertEquals("-j4", tokenizer.getArg());
		tokenizer.consumeArg();
		assertTrue(tokenizer.next());
		assertEquals(null, tokenizer.getOpt());
		assertEquals("--extra", tokenizer.getArg());
		tokenizer.consumeArg();
		assertFalse(tokenizer.next());
	}
}

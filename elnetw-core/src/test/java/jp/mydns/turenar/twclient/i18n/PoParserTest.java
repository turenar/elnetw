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

package jp.mydns.turenar.twclient.i18n;

import java.io.InputStreamReader;
import java.util.Map;

import org.junit.Test;

import static org.junit.Assert.*;

public class PoParserTest {
	private Map<String, String> getEntries(String fileName) {
		return PoParser.parse(new InputStreamReader(PoParserTest.class.getResourceAsStream(fileName)));
	}

	@Test
	public void testCommentEntries() throws Exception {
		Map<String, String> entries = getEntries("comment.po");
		assertEquals("テスト", entries.get(PoParser.getMessageId("Noun", "test")));
		assertEquals("テストする", entries.get(PoParser.getMessageId("Verb", "test")));
	}

	@Test
	public void testFuzzyEntries() throws Exception {
		Map<String, String> entries = getEntries("fuzzy.po");
		assertEquals(null, entries.get("test"));
	}

	@Test
	public void testMultiLineEntries() throws Exception {
		Map<String, String> entries = getEntries("multiline.po");
		assertEquals("テスト\nこれはテストです。", entries.get("test\nThis is a test."));
	}

	@Test
	public void testSimpleEntries() throws Exception {
		Map<String, String> entries = getEntries("simple.po");
		assertEquals("テスト", entries.get("test"));
	}
}

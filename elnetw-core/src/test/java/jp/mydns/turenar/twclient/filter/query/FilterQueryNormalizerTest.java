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

package jp.mydns.turenar.twclient.filter.query;

import java.io.StringReader;

import jp.mydns.turenar.twclient.filter.tokenizer.FilterParser;
import jp.mydns.turenar.twclient.filter.tokenizer.ParseException;
import jp.mydns.turenar.twclient.filter.tokenizer.QueryTokenStart;
import org.junit.Test;

import static org.junit.Assert.*;

public class FilterQueryNormalizerTest {
	@Test
	public void testComment() throws Exception {
		StringBuilder stringBuilder = new StringBuilder();
		FilterQueryNormalizer normalizer = new FilterQueryNormalizer(stringBuilder);
		QueryTokenStart node = tokenize(" /**/ test /**//* */(" +
				"\n/*\t*//**/    fuga\n/**/\n/**/?\t\t/**//**/\t,\n/**/     /**/fuga\t\t/**//**/\t)\t/**/ ");
		node.jjtAccept(normalizer, null);
		final String expected = "/**/test/**//* */(/*\t*//**/fuga/**//**/?/**//**/,/**//**/fuga/**//**/)/**/";
		assertEquals(expected, stringBuilder.toString());
	}

	private QueryTokenStart tokenize(String s) throws ParseException {
		return new FilterParser(new StringReader(s)).Start();
	}
}
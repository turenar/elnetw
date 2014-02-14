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

package jp.syuriken.snsw.twclient.filter;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import jp.syuriken.snsw.twclient.filter.tokenizer.FilterParserVisitor;
import jp.syuriken.snsw.twclient.filter.tokenizer.ParseException;
import jp.syuriken.snsw.twclient.filter.tokenizer.QueryTokenFunction;
import jp.syuriken.snsw.twclient.filter.tokenizer.QueryTokenProperty;
import jp.syuriken.snsw.twclient.filter.tokenizer.QueryTokenPropertyOperator;
import jp.syuriken.snsw.twclient.filter.tokenizer.QueryTokenPropertyValue;
import jp.syuriken.snsw.twclient.filter.tokenizer.QueryTokenQuery;
import jp.syuriken.snsw.twclient.filter.tokenizer.QueryTokenStart;
import jp.syuriken.snsw.twclient.filter.tokenizer.SimpleNode;
import org.junit.Test;

import static jp.syuriken.snsw.twclient.filter.tokenizer.FilterParserTreeConstants.JJTFUNCTION;
import static jp.syuriken.snsw.twclient.filter.tokenizer.FilterParserTreeConstants.JJTPROPERTY;
import static jp.syuriken.snsw.twclient.filter.tokenizer.FilterParserTreeConstants.JJTPROPERTYOPERATOR;
import static jp.syuriken.snsw.twclient.filter.tokenizer.FilterParserTreeConstants.JJTPROPERTYVALUE;
import static jp.syuriken.snsw.twclient.filter.tokenizer.FilterParserTreeConstants.jjtNodeName;
import static org.junit.Assert.*;

/**
 * {@link FilterCompiler} のためのテスト・クラス
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class FilterCompilerTest implements FilterParserVisitor {

	private static class FunctionEndVirtualNode extends SimpleNode {

		public FunctionEndVirtualNode() {
			super(0xf0000000);
		}
	}

	private void assertNoValidToken(List<SimpleNode> list) {
		assertTrue(list.isEmpty());
	}

	private void assertToken(Queue<SimpleNode> list, int kind, String value) {
		SimpleNode node = list.poll();
		assertEquals(jjtNodeName[kind], node.toString());
		if (value != null) {
			assertEquals(value, node.jjtGetValue());
		}
	}

	private void assertTokenFuncEnd(Queue<SimpleNode> list) {
		assertTrue(list.poll() instanceof FunctionEndVirtualNode);
	}

	@SuppressWarnings("unchecked")
	private void recordVisit(SimpleNode node, Object data) {
		((Queue<SimpleNode>) data).add(node);
	}

	/**
	 * {@link jp.syuriken.snsw.twclient.filter.FilterCompiler#tokenize(String)} のためのテスト・メソッド。
	 *
	 * @throws IllegalSyntaxException エラー
	 * @throws ParseException         パース例外
	 */
	@Test
	public void testNextToken1WithPropertyNameOnly() throws IllegalSyntaxException, ParseException {
		QueryTokenStart node = FilterCompiler.tokenize(" hoge ");
		LinkedList<SimpleNode> list = new LinkedList<SimpleNode>();
		node.jjtAccept(this, list);
		assertToken(list, JJTPROPERTY, "hoge");
		assertNoValidToken(list);
	}

	/**
	 * {@link jp.syuriken.snsw.twclient.filter.FilterCompiler#tokenize(String)} のためのテスト・メソッド。
	 *
	 * @throws IllegalSyntaxException エラー
	 * @throws ParseException         パース例外
	 */
	@Test
	public void testNextToken2WithPropertyOperator() throws IllegalSyntaxException, ParseException {
		QueryTokenStart node = FilterCompiler.tokenize("hoge:");
		LinkedList<SimpleNode> list = new LinkedList<SimpleNode>();
		node.jjtAccept(this, list);
		assertToken(list, JJTPROPERTY, "hoge");
		assertToken(list, JJTPROPERTYOPERATOR, ":");
		assertNoValidToken(list);

		node = FilterCompiler.tokenize(" \t    \nhoge \n\t: ");
		list = new LinkedList<SimpleNode>();
		node.jjtAccept(this, list);
		assertToken(list, JJTPROPERTY, "hoge");
		assertToken(list, JJTPROPERTYOPERATOR, ":");
		assertNoValidToken(list);
	}

	/**
	 * {@link jp.syuriken.snsw.twclient.filter.FilterCompiler#tokenize(String)} のためのテスト・メソッド。
	 *
	 * @throws IllegalSyntaxException エラー
	 * @throws ParseException         パース例外
	 */
	@Test
	public void testNextToken3WithPropertyComparedWithString() throws IllegalSyntaxException, ParseException {
		QueryTokenStart node = FilterCompiler.tokenize("hoge:\"aaaa\"");
		LinkedList<SimpleNode> list = new LinkedList<SimpleNode>();
		node.jjtAccept(this, list);
		assertToken(list, JJTPROPERTY, "hoge");
		assertToken(list, JJTPROPERTYOPERATOR, ":");
		assertToken(list, JJTPROPERTYVALUE, "\"aaaa\"");
		assertNoValidToken(list);

		node = FilterCompiler.tokenize("hoge:\" \"");
		node.jjtAccept(this, list);
		assertToken(list, JJTPROPERTY, "hoge");
		assertToken(list, JJTPROPERTYOPERATOR, ":");
		assertToken(list, JJTPROPERTYVALUE, "\" \"");
		assertNoValidToken(list);
	}

	/**
	 * {@link jp.syuriken.snsw.twclient.filter.FilterCompiler#tokenize(String)} のためのテスト・メソッド。
	 *
	 * @throws IllegalSyntaxException エラー
	 * @throws ParseException         パース例外
	 */
	@Test
	public void testNextToken4WithPropertyComparedWithInt() throws IllegalSyntaxException, ParseException {
		QueryTokenStart node = FilterCompiler.tokenize("hoge:1234");
		LinkedList<SimpleNode> list = new LinkedList<SimpleNode>();
		node.jjtAccept(this, list);
		assertToken(list, JJTPROPERTY, "hoge");
		assertToken(list, JJTPROPERTYOPERATOR, ":");
		assertToken(list, JJTPROPERTYVALUE, "1234");
		assertNoValidToken(list);

		node = FilterCompiler.tokenize("hoge== 1234");
		node.jjtAccept(this, list);
		assertToken(list, JJTPROPERTY, "hoge");
		assertToken(list, JJTPROPERTYOPERATOR, "==");
		assertToken(list, JJTPROPERTYVALUE, "1234");
		assertNoValidToken(list);

		node = FilterCompiler.tokenize("hoge != 1234");
		node.jjtAccept(this, list);
		assertToken(list, JJTPROPERTY, "hoge");
		assertToken(list, JJTPROPERTYOPERATOR, "!=");
		assertToken(list, JJTPROPERTYVALUE, "1234");
		assertNoValidToken(list);

		node = FilterCompiler.tokenize(" hoge <= 1234 ");
		node.jjtAccept(this, list);
		assertToken(list, JJTPROPERTY, "hoge");
		assertToken(list, JJTPROPERTYOPERATOR, "<=");
		assertToken(list, JJTPROPERTYVALUE, "1234");
		assertNoValidToken(list);
	}

	/**
	 * {@link jp.syuriken.snsw.twclient.filter.FilterCompiler#tokenize(String)} のためのテスト・メソッド。
	 *
	 * @throws IllegalSyntaxException エラー
	 * @throws ParseException         パース例外
	 */
	@Test
	public void testNextToken5WithFunction() throws IllegalSyntaxException, ParseException {
		QueryTokenStart node = FilterCompiler.tokenize(" hoge (  ) ");
		LinkedList<SimpleNode> list = new LinkedList<SimpleNode>();
		node.jjtAccept(this, list);
		assertToken(list, JJTFUNCTION, "hoge");
		assertTokenFuncEnd(list);
		assertNoValidToken(list);
	}

	/**
	 * {@link jp.syuriken.snsw.twclient.filter.FilterCompiler#tokenize(String)} のためのテスト・メソッド。
	 *
	 * @throws IllegalSyntaxException エラー
	 * @throws ParseException         パース例外
	 */
	@Test
	public void testNextToken6WithDeepFunction() throws IllegalSyntaxException, ParseException {
		QueryTokenStart node = FilterCompiler.tokenize(" hoge ( fuga ) ");
		LinkedList<SimpleNode> list = new LinkedList<SimpleNode>();
		node.jjtAccept(this, list);
		assertToken(list, JJTFUNCTION, "hoge");
		assertToken(list, JJTPROPERTY, "fuga");
		assertTokenFuncEnd(list);
		assertNoValidToken(list);

		node = FilterCompiler.tokenize(" hoge ( fuga ?) ");
		node.jjtAccept(this, list);
		assertToken(list, JJTFUNCTION, "hoge");
		assertToken(list, JJTPROPERTY, "fuga");
		assertToken(list, JJTPROPERTYOPERATOR, "?");
		assertTokenFuncEnd(list);
		assertNoValidToken(list);

		node = FilterCompiler.tokenize(" hoge ( fuga == 1234) ");
		node.jjtAccept(this, list);
		assertToken(list, JJTFUNCTION, "hoge");
		assertToken(list, JJTPROPERTY, "fuga");
		assertToken(list, JJTPROPERTYOPERATOR, "==");
		assertToken(list, JJTPROPERTYVALUE, "1234");
		assertTokenFuncEnd(list);
		assertNoValidToken(list);

		node = FilterCompiler.tokenize(" hoge ( hoge ( ) ) ");
		node.jjtAccept(this, list);
		assertToken(list, JJTFUNCTION, "hoge");
		assertToken(list, JJTFUNCTION, "hoge");
		assertTokenFuncEnd(list);
		assertTokenFuncEnd(list);
		assertNoValidToken(list);
	}

	/**
	 * {@link jp.syuriken.snsw.twclient.filter.FilterCompiler#tokenize(String)} のためのテスト・メソッド。
	 *
	 * @throws IllegalSyntaxException エラー
	 * @throws ParseException         パース例外
	 */
	@Test
	public void testNextToken7WithFunctionSeparator() throws IllegalSyntaxException, ParseException {
		QueryTokenStart node = FilterCompiler.tokenize(" hoge ( fuga?, fuga ) ");
		LinkedList<SimpleNode> list = new LinkedList<SimpleNode>();
		node.jjtAccept(this, list);
		assertToken(list, JJTFUNCTION, "hoge");
		assertToken(list, JJTPROPERTY, "fuga");
		assertToken(list, JJTPROPERTYOPERATOR, "?");
		assertToken(list, JJTPROPERTY, "fuga");
		assertTokenFuncEnd(list);
		assertNoValidToken(list);

		node = FilterCompiler.tokenize(" hoge ( fuga?, hoge(fuga\n==9876, fuga\t??  ) ) ");
		node.jjtAccept(this, list);
		assertToken(list, JJTFUNCTION, "hoge");
		assertToken(list, JJTPROPERTY, "fuga");
		assertToken(list, JJTPROPERTYOPERATOR, "?");
		assertToken(list, JJTFUNCTION, "hoge");
		assertToken(list, JJTPROPERTY, "fuga");
		assertToken(list, JJTPROPERTYOPERATOR, "==");
		assertToken(list, JJTPROPERTYVALUE, "9876");
		assertToken(list, JJTPROPERTY, "fuga");
		assertToken(list, JJTPROPERTYOPERATOR, "??");
		assertTokenFuncEnd(list);
		assertTokenFuncEnd(list);
		assertNoValidToken(list);

		node = FilterCompiler.tokenize("a(b?, c(d\n==9876, e\t??, f(g:\" \\\" \", h:\" \\'\\n\\\\ \")  ), k? ) ");
		node.jjtAccept(this, list);
		assertToken(list, JJTFUNCTION, "a");
		assertToken(list, JJTPROPERTY, "b");
		assertToken(list, JJTPROPERTYOPERATOR, "?");
		assertToken(list, JJTFUNCTION, "c");
		assertToken(list, JJTPROPERTY, "d");
		assertToken(list, JJTPROPERTYOPERATOR, "==");
		assertToken(list, JJTPROPERTYVALUE, "9876");
		assertToken(list, JJTPROPERTY, "e");
		assertToken(list, JJTPROPERTYOPERATOR, "??");
		assertToken(list, JJTFUNCTION, "f");
		assertToken(list, JJTPROPERTY, "g");
		assertToken(list, JJTPROPERTYOPERATOR, ":");
		assertToken(list, JJTPROPERTYVALUE, "\" \\\" \"");
		assertToken(list, JJTPROPERTY, "h");
		assertToken(list, JJTPROPERTYOPERATOR, ":");
		assertToken(list, JJTPROPERTYVALUE, "\" \\'\\n\\\\ \"");
		assertTokenFuncEnd(list);
		assertTokenFuncEnd(list);
		assertToken(list, JJTPROPERTY, "k");
		assertToken(list, JJTPROPERTYOPERATOR, "?");
		assertTokenFuncEnd(list);
		assertNoValidToken(list);
	}

	@Override
	public Object visit(QueryTokenFunction node, Object data) {
		recordVisit(node, data);
		node.childrenAccept(this, data);
		recordVisit(new FunctionEndVirtualNode(), data);
		return null;
	}

	@Override
	public Object visit(QueryTokenProperty node, Object data) {
		recordVisit(node, data);
		node.childrenAccept(this, data);
		return null;
	}

	@Override
	public Object visit(QueryTokenPropertyOperator node, Object data) {
		recordVisit(node, data);
		node.childrenAccept(this, data);
		return null;
	}

	@Override
	public Object visit(QueryTokenPropertyValue node, Object data) {
		recordVisit(node, data);
		node.childrenAccept(this, data);
		return null;
	}

	@Override
	public Object visit(QueryTokenQuery node, Object data) {
		node.childrenAccept(this, data);
		return null;
	}

	@Override
	public Object visit(QueryTokenStart node, Object data) {
		node.childrenAccept(this, data);
		return null;
	}

	@Override
	public Object visit(SimpleNode node, Object data) {
		node.childrenAccept(this, data);
		return null;
	}
}

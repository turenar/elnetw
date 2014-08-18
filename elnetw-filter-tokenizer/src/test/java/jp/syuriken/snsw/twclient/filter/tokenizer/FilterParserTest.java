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

package jp.syuriken.snsw.twclient.filter.tokenizer;

import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.junit.Test;

import static jp.syuriken.snsw.twclient.filter.tokenizer.FilterParserTreeConstants.JJTFUNCTION;
import static jp.syuriken.snsw.twclient.filter.tokenizer.FilterParserTreeConstants.JJTFUNCTIONARGSEPARATOR;
import static jp.syuriken.snsw.twclient.filter.tokenizer.FilterParserTreeConstants.JJTFUNCTIONLEFTPARENTHESIS;
import static jp.syuriken.snsw.twclient.filter.tokenizer.FilterParserTreeConstants.JJTPROPERTY;
import static jp.syuriken.snsw.twclient.filter.tokenizer.FilterParserTreeConstants.JJTPROPERTYOPERATOR;
import static jp.syuriken.snsw.twclient.filter.tokenizer.FilterParserTreeConstants.JJTPROPERTYVALUE;
import static org.junit.Assert.*;

/**
 * {@link jp.syuriken.snsw.twclient.filter.tokenizer.FilterParser} のためのテスト・クラス
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class FilterParserTest implements FilterParserVisitor {


	private static class CommentVirtualNode extends MyNode {
		public CommentVirtualNode(String image) {
			super(0xf0000001);
			jjtSetValue(image);
		}
	}

	private static class FunctionEndVirtualNode extends MyNode {
		public FunctionEndVirtualNode() {
			super(0xf0000000);
		}
	}

	private void assertComment(Queue<MyNode> list, String comment) {
		MyNode node = list.poll();
		assertTrue(node instanceof CommentVirtualNode);
		assertEquals(comment, node.jjtGetValue());
	}

	private void assertComment(Queue<MyNode> list) {
		assertComment(list, "/**/");
	}

	private void assertNoValidToken(List<MyNode> list) {
		assertTrue(list.isEmpty());
	}

	private void assertToken(Queue<MyNode> list, int kind, String value) {
		MyNode node = list.poll();
		assertNotNull(node);
		assertEquals(kind, node.getTypeId());
		if (value != null) {
			assertEquals(value, node.jjtGetValue());
		}
	}

	private void assertTokenFuncEnd(Queue<MyNode> list) {
		assertTrue(list.poll() instanceof QueryTokenFunctionRightParenthesis);
		assertTrue(list.poll() instanceof FunctionEndVirtualNode);
	}

	private void handleComment(Queue<MyNode> data, Token tok) {
		if (tok.specialToken != null) {
			handleComment(data, tok.specialToken);
		}
		data.add(new CommentVirtualNode(tok.image));
	}

	@SuppressWarnings("unchecked")
	private void recordVisit(MyNode node, Object dataObj, boolean commentOnly) {
		Token tok = node.getSpecialToken();
		Queue<MyNode> data = (Queue<MyNode>) dataObj;
		if (tok != null) {
			handleComment(data, tok);
		}
		if (!commentOnly) {
			data.add(node);
		}
	}

	@Test
	public void test1PropertyNameOnly() throws ParseException {
		QueryTokenStart node = tokenize(" hoge ");
		LinkedList<MyNode> list = new LinkedList<>();
		node.jjtAccept(this, list);
		assertToken(list, JJTPROPERTY, "hoge");
		assertNoValidToken(list);
	}

	@Test
	public void test1PropertyOperator2() throws ParseException {
		QueryTokenStart node = tokenize(" \t    \nhoge \n\t: ");
		LinkedList<MyNode> list = new LinkedList<>();
		node.jjtAccept(this, list);
		assertToken(list, JJTPROPERTY, "hoge");
		assertToken(list, JJTPROPERTYOPERATOR, ":");
		assertNoValidToken(list);
	}

	@Test
	public void test2Comment() throws Exception {
		QueryTokenStart node = tokenize("/* */ hoge /* */");
		LinkedList<MyNode> list = new LinkedList<>();
		node.jjtAccept(this, list);
		assertComment(list, "/* */");
		assertToken(list, JJTPROPERTY, "hoge");
		assertComment(list, "/* */");
		assertNoValidToken(list);
	}

	@Test
	public void test2CommentMultiline() throws Exception {
		QueryTokenStart node = tokenize("/*\n */ hoge /* \n*/");
		LinkedList<MyNode> list = new LinkedList<>();
		node.jjtAccept(this, list);
		assertComment(list, "/*\n */");
		assertToken(list, JJTPROPERTY, "hoge");
		assertComment(list, "/* \n*/");
		assertNoValidToken(list);
	}

	@Test
	public void test2CommentShortHand() throws Exception {
		QueryTokenStart node = tokenize("/**/ hoge /***/");
		LinkedList<MyNode> list = new LinkedList<>();
		node.jjtAccept(this, list);
		assertComment(list);
		assertToken(list, JJTPROPERTY, "hoge");
		assertComment(list, "/***/");
		assertNoValidToken(list);
	}

	@Test
	public void test2CommentWithAstarisk() throws Exception {
		QueryTokenStart node = tokenize("/** */ hoge /* **/");
		LinkedList<MyNode> list = new LinkedList<>();
		node.jjtAccept(this, list);
		assertComment(list, "/** */");
		assertToken(list, JJTPROPERTY, "hoge");
		assertComment(list, "/* **/");
		assertNoValidToken(list);
	}

	@Test
	public void test2Function() throws ParseException {
		QueryTokenStart node = tokenize(" hoge (  ) ");
		LinkedList<MyNode> list = new LinkedList<>();
		node.jjtAccept(this, list);
		assertToken(list, JJTFUNCTION, "hoge");
		assertToken(list, JJTFUNCTIONLEFTPARENTHESIS, null);
		assertTokenFuncEnd(list);
		assertNoValidToken(list);
	}

	@Test
	public void test2PropertyOperator1() throws ParseException {
		QueryTokenStart node = tokenize("hoge:");
		LinkedList<MyNode> list = new LinkedList<>();
		node.jjtAccept(this, list);
		assertToken(list, JJTPROPERTY, "hoge");
		assertToken(list, JJTPROPERTYOPERATOR, ":");
		assertNoValidToken(list);
	}

	@Test
	public void test3CommentWithProperty() throws Exception {
		QueryTokenStart node = tokenize("/**/hoge/**/ == /**/\"cjk\"");
		LinkedList<MyNode> list = new LinkedList<>();
		node.jjtAccept(this, list);
		assertComment(list);
		assertToken(list, JJTPROPERTY, "hoge");
		assertComment(list);
		assertToken(list, JJTPROPERTYOPERATOR, "==");
		assertComment(list);
		assertToken(list, JJTPROPERTYVALUE, "\"cjk\"");
		assertNoValidToken(list);
	}

	@Test
	public void test3FunctionDeeply1() throws ParseException {
		QueryTokenStart node = tokenize(" hoge ( fuga ) ");
		LinkedList<MyNode> list = new LinkedList<>();
		node.jjtAccept(this, list);
		assertToken(list, JJTFUNCTION, "hoge");
		assertToken(list, JJTFUNCTIONLEFTPARENTHESIS, null);
		assertToken(list, JJTPROPERTY, "fuga");
		assertTokenFuncEnd(list);
		assertNoValidToken(list);
	}

	@Test
	public void test3FunctionDeeply2() throws ParseException {
		QueryTokenStart node = tokenize(" hoge ( fuga ?) ");
		LinkedList<MyNode> list = new LinkedList<>();
		node.jjtAccept(this, list);
		assertToken(list, JJTFUNCTION, "hoge");
		assertToken(list, JJTFUNCTIONLEFTPARENTHESIS, null);
		assertToken(list, JJTPROPERTY, "fuga");
		assertToken(list, JJTPROPERTYOPERATOR, "?");
		assertTokenFuncEnd(list);
		assertNoValidToken(list);
	}

	@Test
	public void test3FunctionDeeply3() throws ParseException {
		QueryTokenStart node = tokenize(" hoge ( fuga == 1234) ");
		LinkedList<MyNode> list = new LinkedList<>();
		node.jjtAccept(this, list);
		assertToken(list, JJTFUNCTION, "hoge");
		assertToken(list, JJTFUNCTIONLEFTPARENTHESIS, null);
		assertToken(list, JJTPROPERTY, "fuga");
		assertToken(list, JJTPROPERTYOPERATOR, "==");
		assertToken(list, JJTPROPERTYVALUE, "1234");
		assertTokenFuncEnd(list);
		assertNoValidToken(list);
	}

	@Test
	public void test3FunctionDeeply4() throws ParseException {
		QueryTokenStart node = tokenize(" hoge ( hoge ( ) ) ");
		LinkedList<MyNode> list = new LinkedList<>();
		node.jjtAccept(this, list);
		assertToken(list, JJTFUNCTION, "hoge");
		assertToken(list, JJTFUNCTIONLEFTPARENTHESIS, null);
		assertToken(list, JJTFUNCTION, "hoge");
		assertToken(list, JJTFUNCTIONLEFTPARENTHESIS, null);
		assertTokenFuncEnd(list);
		assertTokenFuncEnd(list);
		assertNoValidToken(list);
	}

	@Test
	public void test3PropertyInt1() throws ParseException {
		QueryTokenStart node = tokenize("hoge:1234");
		LinkedList<MyNode> list = new LinkedList<>();
		node.jjtAccept(this, list);
		assertToken(list, JJTPROPERTY, "hoge");
		assertToken(list, JJTPROPERTYOPERATOR, ":");
		assertToken(list, JJTPROPERTYVALUE, "1234");
		assertNoValidToken(list);
	}

	@Test
	public void test3PropertyInt2() throws ParseException {
		QueryTokenStart node = tokenize("hoge== 1234");
		LinkedList<MyNode> list = new LinkedList<>();
		node.jjtAccept(this, list);
		assertToken(list, JJTPROPERTY, "hoge");
		assertToken(list, JJTPROPERTYOPERATOR, "==");
		assertToken(list, JJTPROPERTYVALUE, "1234");
		assertNoValidToken(list);
	}

	@Test
	public void test3PropertyInt3() throws ParseException {
		QueryTokenStart node = tokenize("hoge != 1234");
		LinkedList<MyNode> list = new LinkedList<>();
		node.jjtAccept(this, list);
		assertToken(list, JJTPROPERTY, "hoge");
		assertToken(list, JJTPROPERTYOPERATOR, "!=");
		assertToken(list, JJTPROPERTYVALUE, "1234");
		assertNoValidToken(list);
	}

	@Test
	public void test3PropertyInt4() throws ParseException {
		QueryTokenStart node = tokenize(" hoge <= 1234 ");
		LinkedList<MyNode> list = new LinkedList<>();
		node.jjtAccept(this, list);
		assertToken(list, JJTPROPERTY, "hoge");
		assertToken(list, JJTPROPERTYOPERATOR, "<=");
		assertToken(list, JJTPROPERTYVALUE, "1234");
		assertNoValidToken(list);
	}

	@Test
	public void test3PropertyString1() throws ParseException {
		QueryTokenStart node = tokenize("hoge:\"aaaa\"");
		LinkedList<MyNode> list = new LinkedList<>();
		node.jjtAccept(this, list);
		assertToken(list, JJTPROPERTY, "hoge");
		assertToken(list, JJTPROPERTYOPERATOR, ":");
		assertToken(list, JJTPROPERTYVALUE, "\"aaaa\"");
		assertNoValidToken(list);
	}

	@Test
	public void test3PropertyString2() throws ParseException {
		QueryTokenStart node = tokenize("hoge:\" \"");
		LinkedList<MyNode> list = new LinkedList<>();
		node.jjtAccept(this, list);
		assertToken(list, JJTPROPERTY, "hoge");
		assertToken(list, JJTPROPERTYOPERATOR, ":");
		assertToken(list, JJTPROPERTYVALUE, "\" \"");
		assertNoValidToken(list);
	}

	@Test
	public void test4FunctionMultiArg1() throws ParseException {
		QueryTokenStart node = tokenize("a(b?, c(d\n==9876, e\t??, f(g:\" \\\" \", h:\" \\'\\n\\\\ \")  ), k? ) ");
		LinkedList<MyNode> list = new LinkedList<>();
		node.jjtAccept(this, list);
		assertToken(list, JJTFUNCTION, "a");
		assertToken(list, JJTFUNCTIONLEFTPARENTHESIS, null);
		assertToken(list, JJTPROPERTY, "b");
		assertToken(list, JJTPROPERTYOPERATOR, "?");
		assertToken(list, JJTFUNCTIONARGSEPARATOR, null);
		assertToken(list, JJTFUNCTION, "c");
		assertToken(list, JJTFUNCTIONLEFTPARENTHESIS, null);
		assertToken(list, JJTPROPERTY, "d");
		assertToken(list, JJTPROPERTYOPERATOR, "==");
		assertToken(list, JJTPROPERTYVALUE, "9876");
		assertToken(list, JJTFUNCTIONARGSEPARATOR, null);
		assertToken(list, JJTPROPERTY, "e");
		assertToken(list, JJTPROPERTYOPERATOR, "??");
		assertToken(list, JJTFUNCTIONARGSEPARATOR, null);
		assertToken(list, JJTFUNCTION, "f");
		assertToken(list, JJTFUNCTIONLEFTPARENTHESIS, null);
		assertToken(list, JJTPROPERTY, "g");
		assertToken(list, JJTPROPERTYOPERATOR, ":");
		assertToken(list, JJTPROPERTYVALUE, "\" \\\" \"");
		assertToken(list, JJTFUNCTIONARGSEPARATOR, null);
		assertToken(list, JJTPROPERTY, "h");
		assertToken(list, JJTPROPERTYOPERATOR, ":");
		assertToken(list, JJTPROPERTYVALUE, "\" \\'\\n\\\\ \"");
		assertTokenFuncEnd(list);
		assertTokenFuncEnd(list);
		assertToken(list, JJTFUNCTIONARGSEPARATOR, null);
		assertToken(list, JJTPROPERTY, "k");
		assertToken(list, JJTPROPERTYOPERATOR, "?");
		assertTokenFuncEnd(list);
		assertNoValidToken(list);
	}

	@Test
	public void test4FunctionMultiArg2() throws ParseException {
		QueryTokenStart node = tokenize(" hoge ( fuga?, fuga ) ");
		LinkedList<MyNode> list = new LinkedList<>();
		node.jjtAccept(this, list);
		assertToken(list, JJTFUNCTION, "hoge");
		assertToken(list, JJTFUNCTIONLEFTPARENTHESIS, null);
		assertToken(list, JJTPROPERTY, "fuga");
		assertToken(list, JJTPROPERTYOPERATOR, "?");
		assertToken(list, JJTFUNCTIONARGSEPARATOR, null);
		assertToken(list, JJTPROPERTY, "fuga");
		assertTokenFuncEnd(list);
		assertNoValidToken(list);
	}

	@Test
	public void test4FunctionMultiArg3() throws ParseException {
		QueryTokenStart node = tokenize(" hoge ( fuga?, hoge(fuga\n==9876, fuga\t??  ) ) ");
		LinkedList<MyNode> list = new LinkedList<>();
		node.jjtAccept(this, list);
		assertToken(list, JJTFUNCTION, "hoge");
		assertToken(list, JJTFUNCTIONLEFTPARENTHESIS, null);
		assertToken(list, JJTPROPERTY, "fuga");
		assertToken(list, JJTPROPERTYOPERATOR, "?");
		assertToken(list, JJTFUNCTIONARGSEPARATOR, null);
		assertToken(list, JJTFUNCTION, "hoge");
		assertToken(list, JJTFUNCTIONLEFTPARENTHESIS, null);
		assertToken(list, JJTPROPERTY, "fuga");
		assertToken(list, JJTPROPERTYOPERATOR, "==");
		assertToken(list, JJTPROPERTYVALUE, "9876");
		assertToken(list, JJTFUNCTIONARGSEPARATOR, null);
		assertToken(list, JJTPROPERTY, "fuga");
		assertToken(list, JJTPROPERTYOPERATOR, "??");
		assertTokenFuncEnd(list);
		assertTokenFuncEnd(list);
		assertNoValidToken(list);
	}

	@Test
	public void test5CommentWithFunction() throws Exception {
		QueryTokenStart node = tokenize(" /**/ test/**//**/(/* *//**/fuga/**//**/?/**//**/,/**//**/fuga/**//**/)/**/ ");
		LinkedList<MyNode> list = new LinkedList<>();
		node.jjtAccept(this, list);
		assertComment(list);
		assertToken(list, JJTFUNCTION, "test");
		assertComment(list);
		assertComment(list);
		assertToken(list, JJTFUNCTIONLEFTPARENTHESIS, null);
		assertComment(list, "/* */");
		assertComment(list);
		assertToken(list, JJTPROPERTY, "fuga");
		assertComment(list);
		assertComment(list);
		assertToken(list, JJTPROPERTYOPERATOR, "?");
		assertComment(list);
		assertComment(list);
		assertToken(list, JJTFUNCTIONARGSEPARATOR, null);
		assertComment(list);
		assertComment(list);
		assertToken(list, JJTPROPERTY, "fuga");
		assertComment(list);
		assertComment(list);
		assertTokenFuncEnd(list);
		assertComment(list);
		assertNoValidToken(list);
	}

	private QueryTokenStart tokenize(String query) throws ParseException {
		return new FilterParser(new StringReader(query)).Start();
	}

	@Override
	public Object visit(QueryTokenFunction node, Object data) {
		recordVisit(node, data, false);
		node.childrenAccept(this, data);
		recordVisit(new FunctionEndVirtualNode(), data, false);
		return null;
	}

	@Override
	public Object visit(QueryTokenFunctionArgSeparator node, Object data) {
		recordVisit(node, data, false);
		node.childrenAccept(this, data);
		return null;
	}

	@Override
	public Object visit(QueryTokenFunctionLeftParenthesis node, Object data) {
		recordVisit(node, data, false);
		node.childrenAccept(this, data);
		return null;
	}

	@Override
	public Object visit(QueryTokenFunctionRightParenthesis node, Object data) {
		recordVisit(node, data, false);
		node.childrenAccept(this, data);
		return null;
	}

	@Override
	public Object visit(QueryTokenProperty node, Object data) {
		recordVisit(node, data, false);
		node.childrenAccept(this, data);
		return null;
	}

	@Override
	public Object visit(QueryTokenPropertyOperator node, Object data) {
		recordVisit(node, data, false);
		node.childrenAccept(this, data);
		return null;
	}

	@Override
	public Object visit(QueryTokenPropertyValue node, Object data) {
		recordVisit(node, data, false);
		node.childrenAccept(this, data);
		return null;
	}

	@Override
	public Object visit(QueryTokenQuery node, Object data) {
		recordVisit(node, data, true);
		node.childrenAccept(this, data);
		return null;
	}

	@Override
	public Object visit(SimpleNode node, Object data) {
		return null;
	}

	@Override
	public Object visit(QueryTokenStart node, Object data) {
		recordVisit(node, data, true);
		node.childrenAccept(this, data);
		return null;
	}

	@Override
	public Object visit(QueryTokenEndOfData node, Object data) {
		recordVisit(node, data, true);
		node.childrenAccept(this, data);
		return null;
	}
}

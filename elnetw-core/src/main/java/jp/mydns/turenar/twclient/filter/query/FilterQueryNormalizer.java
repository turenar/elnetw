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

import jp.mydns.turenar.twclient.filter.tokenizer.FilterParserVisitor;
import jp.mydns.turenar.twclient.filter.tokenizer.MyNode;
import jp.mydns.turenar.twclient.filter.tokenizer.Node;
import jp.mydns.turenar.twclient.filter.tokenizer.QueryTokenEndOfData;
import jp.mydns.turenar.twclient.filter.tokenizer.QueryTokenFunction;
import jp.mydns.turenar.twclient.filter.tokenizer.QueryTokenFunctionArgSeparator;
import jp.mydns.turenar.twclient.filter.tokenizer.QueryTokenFunctionLeftParenthesis;
import jp.mydns.turenar.twclient.filter.tokenizer.QueryTokenFunctionRightParenthesis;
import jp.mydns.turenar.twclient.filter.tokenizer.QueryTokenProperty;
import jp.mydns.turenar.twclient.filter.tokenizer.QueryTokenPropertyOperator;
import jp.mydns.turenar.twclient.filter.tokenizer.QueryTokenPropertyValue;
import jp.mydns.turenar.twclient.filter.tokenizer.QueryTokenQuery;
import jp.mydns.turenar.twclient.filter.tokenizer.QueryTokenStart;
import jp.mydns.turenar.twclient.filter.tokenizer.SimpleNode;
import jp.mydns.turenar.twclient.filter.tokenizer.Token;

/** クエリを見やすくするフォーマッタ */
public class FilterQueryNormalizer implements FilterParserVisitor {
	private final StringBuilder stringBuilder;

	/**
	 * create instance
	 *
	 * @param stringBuilder string builder
	 */
	public FilterQueryNormalizer(StringBuilder stringBuilder) {
		this.stringBuilder = stringBuilder;
	}

	private void handleComment(MyNode node) {
		Token token = node.getSpecialToken();
		if (token != null) {
			handleComment(token);
		}
	}

	private void handleComment(Token token) {
		if (token.specialToken != null) {
			handleComment(token.specialToken);
		}
		stringBuilder.append(token);
	}

	@Override
	public Object visit(QueryTokenFunction node, Object data) {
		handleComment(node);

		stringBuilder.append(node.jjtGetValue());
		int childrenCount = node.jjtGetNumChildren();
		for (int i = 0; i < childrenCount; i++) {
			Node childNode = node.jjtGetChild(i);
			childNode.jjtAccept(this, data);
		}
		return null;
	}

	@Override
	public Object visit(QueryTokenFunctionArgSeparator node, Object data) {
		handleComment(node);
		stringBuilder.append(',');
		return null;
	}

	@Override
	public Object visit(QueryTokenFunctionLeftParenthesis node, Object data) {
		handleComment(node);
		stringBuilder.append('(');
		return null;
	}

	@Override
	public Object visit(QueryTokenFunctionRightParenthesis node, Object data) {
		handleComment(node);
		return stringBuilder.append(")");
	}

	@Override
	public Object visit(QueryTokenProperty node, Object data) {
		handleComment(node);
		stringBuilder.append(node.jjtGetValue());
		return node.childrenAccept(this, data);
	}

	@Override
	public Object visit(QueryTokenPropertyOperator node, Object data) {
		handleComment(node);
		return stringBuilder.append(node.jjtGetValue());
	}

	@Override
	public Object visit(QueryTokenPropertyValue node, Object data) {
		handleComment(node);
		return stringBuilder.append(node.jjtGetValue());

	}

	@Override
	public Object visit(QueryTokenQuery node, Object data) {
		handleComment(node);
		return node.childrenAccept(this, data);
	}

	@Override
	public Object visit(QueryTokenStart node, Object data) {
		handleComment(node);
		return node.childrenAccept(this, data);
	}

	@Override
	public Object visit(QueryTokenEndOfData node, Object data) {
		handleComment(node);
		return null;
	}

	@Override
	public Object visit(SimpleNode node, Object data) {
		return null;
	}
}

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

/**
 * extends node
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class MyNode extends SimpleNode {
	private Token specialToken;

	public MyNode(FilterParser p, int i) {
		super(p, i);
	}

	public MyNode(int i) {
		super(i);
	}

	public Token getSpecialToken() {
		return specialToken;
	}

	public int getTypeId() {
		return id;
	}

	@Override
	public void jjtSetLastToken(Token token) {
		super.jjtSetLastToken(token);
		setSpecialToken(jjtGetFirstToken().specialToken);
		jjtGetFirstToken().specialToken = null;
	}

	public void setSpecialToken(Token token) {
		this.specialToken = token;
	}

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder(super.toString())
				.append("{children[")
				.append(jjtGetNumChildren())
				.append("], value=")
				.append(jjtGetValue())
				.append(", specialToken=")
				.append(specialToken)
				.append("}");
		return s.toString();
	}
}

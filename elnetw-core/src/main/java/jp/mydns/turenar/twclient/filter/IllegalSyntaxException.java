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

/**
 * 正しくない文法のクエリの時の例外クラス。
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
@SuppressWarnings("serial")
public class IllegalSyntaxException extends Exception {

	/**
	 * インスタンスを生成する。
	 *
	 * @param message 詳細メッセージ
	 */
	public IllegalSyntaxException(String message) {
		super(message);
	}

	/**
	 * インスタンスを生成する。
	 *
	 * @param message 詳細メッセージ
	 * @param cause   この例外クラスが作られる原因となった {@link Throwable}
	 */
	public IllegalSyntaxException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * インスタンスを生成する。
	 *
	 * @param cause この例外クラスが作られる原因となった {@link Throwable}
	 */
	public IllegalSyntaxException(Throwable cause) {
		super(cause);
	}
}

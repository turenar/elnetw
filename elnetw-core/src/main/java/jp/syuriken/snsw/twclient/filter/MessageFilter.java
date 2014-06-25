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

package jp.syuriken.snsw.twclient.filter;

import jp.syuriken.snsw.twclient.ClientMessageListener;

/**
 * フィルタクラス。
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public interface MessageFilter extends ClientMessageListener, Cloneable {
	/**
	 * set filter as last of child
	 *
	 * @param filter filter
	 * @throws java.lang.UnsupportedOperationException child operation is not permitted
	 */
	void addChild(MessageFilter filter) throws UnsupportedOperationException;

	/**
	 * clone this instance. this interface implementation should be cloneable.
	 * @return cloned instance
	 * @throws CloneNotSupportedException clone operation is not supported
	 */
	MessageFilter clone() throws CloneNotSupportedException;

	/**
	 * get child filter
	 *
	 * @return child filter
	 * @throws java.lang.UnsupportedOperationException child operation is not permitted
	 */
	MessageFilter getChild() throws UnsupportedOperationException;

	/**
	 * set filter as the child of this class.
	 *
	 * @param child child filter
	 * @throws java.lang.UnsupportedOperationException child operation is not permitted
	 */
	void setChild(MessageFilter child) throws UnsupportedOperationException;
}

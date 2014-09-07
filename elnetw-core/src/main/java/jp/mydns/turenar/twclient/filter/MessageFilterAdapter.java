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

import jp.mydns.turenar.twclient.ClientMessageAdapter;

/**
 * {@link MessageFilter}のためのアダプタークラス
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public abstract class MessageFilterAdapter extends ClientMessageAdapter implements MessageFilter {
	protected MessageFilter child;

	/**
	 * create instance
	 */
	public MessageFilterAdapter() {
		this(null);
	}

	/**
	 * create instance
	 *
	 * @param child child filter
	 */
	public MessageFilterAdapter(MessageFilter child) {
		this.child = child;
	}

	@Override
	public synchronized void addChild(MessageFilter filter) {
		if (child == null) {
			setChild(filter);
		} else {
			MessageFilter next = child;
			while (next.getChild() != null) {
				next = next.getChild();
			}
			next.addChild(filter);
		}
	}

	@Override
	public MessageFilterAdapter clone() throws CloneNotSupportedException {
		return (MessageFilterAdapter) super.clone();
	}

	@Override
	public MessageFilter getChild() {
		return child;
	}

	/**
	 * set child message dispatcher
	 *
	 * @param child child message dispatcher
	 */
	public synchronized void setChild(MessageFilter child) {
		this.child = child;
	}
}

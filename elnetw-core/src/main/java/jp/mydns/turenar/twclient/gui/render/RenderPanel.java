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

package jp.mydns.turenar.twclient.gui.render;

import java.awt.LayoutManager;

import javax.swing.JPanel;


/**
 * StatusDataを格納するだけのなんちゃら
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
@SuppressWarnings("serial")
public class RenderPanel extends JPanel implements Comparable<RenderPanel> {

	private final transient RenderObject renderObject;

	/**
	 * インスタンスを生成する。
	 *
	 * @param layout       レイアウトマネージャ
	 * @param renderObject 元になる {@link RenderObject}
	 */
	public RenderPanel(LayoutManager layout, RenderObject renderObject) {
		super(layout);
		this.renderObject = renderObject;
	}

	/**
	 * インスタンスを生成する。
	 *
	 * @param renderObject 元になる {@link jp.mydns.turenar.twclient.gui.render.RenderObject}
	 */
	public RenderPanel(RenderObject renderObject) {
		super();
		this.renderObject = renderObject;
	}

	/**
	 * 元になる情報が作成された日時で比較する。同じ場合はidを比較する。
	 * {@inheritDoc}
	 */
	@Override
	public int compareTo(RenderPanel o) {
		int result = renderObject.getDate().compareTo(o.renderObject.getDate());
		if (result == 0) {
			result = renderObject.getUniqId().compareTo(o.renderObject.getUniqId());
		}
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		return (obj instanceof RenderPanel) && (compareTo((RenderPanel) obj) == 0);
	}

	/**
	 * renderObject
	 *
	 * @return the renderObject
	 */
	public RenderObject getRenderObject() {
		return renderObject;
	}

	@Override
	public int hashCode() {
		return super.hashCode() + renderObject.hashCode();
	}

	/**
	 * event (client message) handler
	 *
	 * @param name event name
	 * @param arg  argument
	 */
	public void onEvent(String name, Object arg) {
		renderObject.onEvent(name, arg);
	}
}

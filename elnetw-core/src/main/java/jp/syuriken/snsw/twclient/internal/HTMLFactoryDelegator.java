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

package jp.syuriken.snsw.twclient.internal;

import javax.swing.text.AttributeSet;
import javax.swing.text.Element;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.View;
import javax.swing.text.html.HTMLEditorKit.HTMLFactory;
import javax.swing.text.html.ParagraphView;

/**
 * HTMLFactory を弄ぶクラス
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class HTMLFactoryDelegator extends HTMLFactory {

	/**
	 * ParagraphViewを弄ぶクラス
	 *
	 * @author Turenar (snswinhaiku dot lo at gmail dot com)
	 */
	public static class ParagraphViewDelegator extends ParagraphView {

		/**
		 * インスタンスを生成する。
		 *
		 * @param view ビュー
		 */
		public ParagraphViewDelegator(View view) {
			super(view.getElement());
			setInsets((short) -10, (short) 0, (short) -10, (short) 0); // CAUTION: ignore rule // CS-IGNORE
			setPropertiesFromAttributes();
		}

		@Override
		public AttributeSet getAttributes() {
			AttributeSet attributes = super.getAttributes();
			if (!(attributes instanceof MutableAttributeSet)) {
				attributes = new SimpleAttributeSet(attributes);
			}
			StyleConstants.setLineSpacing((MutableAttributeSet) attributes, -0.25f); // CAUTION: ignore rule //CS-IGNORE
			return attributes;
		}

		@Override
		public int getHeight() {
			return super.getHeight() - 20; //CS-IGNORE
		}
	}

	@Override
	public View create(Element elem) {
		View view = super.create(elem);
		if (view instanceof ParagraphView) {
			return new ParagraphViewDelegator(view);
		} else {
			return view;
		}
	}
}

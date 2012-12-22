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
 * @author Turenar <snswinhaiku dot lo at gmail dot com>
 */
public class HTMLFactoryDelegator extends HTMLFactory {

	/**
	 * ParagraphViewを弄ぶクラス
	 *
	 * @author Turenar <snswinhaiku dot lo at gmail dot com>
	 */
	public static class ParagraphViewDelegator extends ParagraphView {

		/**
		 * インスタンスを生成する。
		 *
		 * @param view ビュー
		 */
		public ParagraphViewDelegator(View view) {
			super(view.getElement());
			setInsets((short) -10, (short) 0, (short) -10, (short) 0); // CAUTION: ignore rule
			setPropertiesFromAttributes();
		}

		@Override
		public AttributeSet getAttributes() {
			AttributeSet attributes = super.getAttributes();
			if (attributes instanceof MutableAttributeSet == false) {
				attributes = new SimpleAttributeSet(attributes);
			}
			StyleConstants.setLineSpacing((MutableAttributeSet) attributes, -0.25f); // CAUTION: ignore rule
			return attributes;
		}

		@Override
		public int getHeight() {
			return super.getHeight() - 20;
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

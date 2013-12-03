package jp.syuriken.snsw.twclient.gui.render;

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
	 * @param renderObject 元になる {@link jp.syuriken.snsw.twclient.gui.render.RenderObject}
	 */
	public RenderPanel(RenderObject renderObject) {
		super();
		this.renderObject = renderObject;
	}

	/** 元になる情報が作成された日時で比較する。同じ場合はidを比較する。 */
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

	public void onEvent(String name, Object arg) {
		renderObject.onEvent(name, arg);
	}
}

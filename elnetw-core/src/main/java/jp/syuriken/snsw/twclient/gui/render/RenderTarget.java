package jp.syuriken.snsw.twclient.gui.render;

import java.awt.event.FocusEvent;

/**
 * Created with IntelliJ IDEA.
 * Date: 13/08/31
 * Time: 18:52
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public interface RenderTarget {
	void addStatus(RenderObject renderObject);

	void focusGained(FocusEvent e);
}

package jp.syuriken.snsw.twclient.gui.render;

import java.awt.event.FocusEvent;

/**
 * render target
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public interface RenderTarget {
	/**
	 * render statuses
	 *
	 * @param renderObject rendered object
	 */
	void addStatus(RenderObject renderObject);

	/**
	 * render panel gain focus
	 *
	 * @param e event info
	 */
	void focusGained(FocusEvent e);

	/**
	 * request to remove status
	 *
	 * @param renderObject rendered object removal requested
	 */
	void removeStatus(RenderObject renderObject);
}

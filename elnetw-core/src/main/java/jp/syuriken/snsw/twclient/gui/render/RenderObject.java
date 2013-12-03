package jp.syuriken.snsw.twclient.gui.render;

import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * Date: 13/08/31
 * Time: 18:53
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public interface RenderObject {
	RenderPanel getComponent();

	String getCreatedBy();

	Date getDate();

	String getUniqId();

	Object getBasedObject();

	void onEvent(String name, Object arg);

	void requestCopyToClipboard();
}

package jp.syuriken.snsw.twclient.gui;

import jp.syuriken.snsw.twclient.ClientMessageListener;

/**
 * タブレンダラ
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public interface TabRenderer extends ClientMessageListener {
	void onDisplayRequirement();
}

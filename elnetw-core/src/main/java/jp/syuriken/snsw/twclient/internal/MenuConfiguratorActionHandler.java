package jp.syuriken.snsw.twclient.internal;

import javax.swing.JMenuItem;

import jp.syuriken.snsw.twclient.ActionHandler;
import jp.syuriken.snsw.twclient.ClientConfiguration;
import jp.syuriken.snsw.twclient.handler.IntentArguments;

/**
 * 設定フレームを表示するアクションハンドラ
 *
 * @author Turenar <snswinhaiku dot lo at gmail dot com>
 */
public class MenuConfiguratorActionHandler implements ActionHandler {
	@Override
	public JMenuItem createJMenuItem(IntentArguments args) {
		return null;
	}

	@Override
	public void handleAction(IntentArguments args) {
		ClientConfiguration.getInstance().getConfigBuilder().show();
	}

	@Override
	public void popupMenuWillBecomeVisible(JMenuItem menuItem, IntentArguments args) {
		// This is always enabled.
	}
}

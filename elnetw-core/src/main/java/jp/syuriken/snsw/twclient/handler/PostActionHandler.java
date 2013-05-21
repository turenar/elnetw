package jp.syuriken.snsw.twclient.handler;

import java.awt.event.KeyEvent;

import javax.swing.JMenuItem;

import jp.syuriken.snsw.twclient.ActionHandler;
import jp.syuriken.snsw.twclient.ClientFrameApi;
import jp.syuriken.snsw.twclient.StatusData;

/**
 * TODO snsoftware
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class PostActionHandler implements ActionHandler {

	@Override
	public JMenuItem createJMenuItem(String commandName) {
		return new JMenuItem("投稿(P)", KeyEvent.VK_P);
	}

	@Override
	public void handleAction(String actionName, StatusData statusData, ClientFrameApi api) {
		api.doPost();
	}

	@Override
	public void popupMenuWillBecomeVisible(JMenuItem menuItem, StatusData statusData, ClientFrameApi api) {
	}

}

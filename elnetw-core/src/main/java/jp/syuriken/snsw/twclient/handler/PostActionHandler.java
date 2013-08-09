package jp.syuriken.snsw.twclient.handler;

import java.awt.event.KeyEvent;

import javax.swing.JMenuItem;

import jp.syuriken.snsw.twclient.ActionHandler;
import jp.syuriken.snsw.twclient.ClientConfiguration;

/**
 * TODO snsoftware
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class PostActionHandler implements ActionHandler {

	private final ClientConfiguration configuration;

	public PostActionHandler() {
		configuration = ClientConfiguration.getInstance();
	}

	@Override
	public JMenuItem createJMenuItem(IntentArguments arguments) {
		return new JMenuItem("投稿(P)", KeyEvent.VK_P);
	}

	@Override
	public void handleAction(IntentArguments arguments) {
		configuration.getFrameApi().doPost();
	}

	@Override
	public void popupMenuWillBecomeVisible(JMenuItem menuItem, IntentArguments arguments) {
	}
}

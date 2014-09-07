package jp.syuriken.snsw.twclient.handler;

import javax.swing.JMenuItem;

import jp.syuriken.snsw.twclient.ActionHandler;
import jp.syuriken.snsw.twclient.ClientConfiguration;
import jp.syuriken.snsw.twclient.gui.AddClientTabConfirmFrame;
import jp.syuriken.snsw.twclient.gui.tab.ClientTabFactory;
import jp.syuriken.snsw.twclient.internal.IntentActionListener;

/**
 * action handler for adding client tab
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class AddClientTabActionHandler implements ActionHandler {
	@Override
	public JMenuItem createJMenuItem(IntentArguments args) {
		String tabId = args.getExtraObj("tabId", String.class);
		if (tabId == null) {
			throw new IllegalArgumentException("tabId is not specified!");
		}
		ClientTabFactory factory = ClientConfiguration.getClientTabFactory(tabId);
		if (factory == null) {
			throw new IllegalArgumentException("tabId[" + tabId + "] is unknown!");
		}
		JMenuItem factoryItem = new JMenuItem(factory.getName());
		factoryItem.addActionListener(new IntentActionListener("tab_add").putExtra("tabId", tabId));
		return factoryItem;
	}

	@Override
	public void handleAction(IntentArguments args) {
		new AddClientTabConfirmFrame(args.getExtraObj("tabId", String.class)).setVisible(true);
	}

	@Override
	public void popupMenuWillBecomeVisible(JMenuItem menuItem, IntentArguments args) {

	}
}

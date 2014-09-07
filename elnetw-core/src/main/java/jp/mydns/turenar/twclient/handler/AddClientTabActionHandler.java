package jp.mydns.turenar.twclient.handler;

import javax.swing.JMenuItem;

import jp.mydns.turenar.twclient.ActionHandler;
import jp.mydns.turenar.twclient.ClientConfiguration;
import jp.mydns.turenar.twclient.gui.AddClientTabConfirmFrame;
import jp.mydns.turenar.twclient.gui.tab.ClientTabFactory;
import jp.mydns.turenar.twclient.internal.IntentActionListener;

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

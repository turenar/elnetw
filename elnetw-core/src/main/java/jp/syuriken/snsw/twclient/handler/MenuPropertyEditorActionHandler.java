package jp.syuriken.snsw.twclient.handler;

import javax.swing.JMenuItem;

import jp.syuriken.snsw.twclient.ActionHandler;
import jp.syuriken.snsw.twclient.gui.PropertyEditorFrame;

/**
 * メニューのプロパティエディタを開くためのアクションハンドラ
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class MenuPropertyEditorActionHandler implements ActionHandler {

	@Override
	public JMenuItem createJMenuItem(IntentArguments args) {
		return null;
	}

	@Override
	public void handleAction(IntentArguments args) {
		PropertyEditorFrame propertyEditorFrame = new PropertyEditorFrame();
		propertyEditorFrame.setVisible(true);
	}

	@Override
	public void popupMenuWillBecomeVisible(JMenuItem menuItem, IntentArguments args) {
		// This is always enabled.
	}
}

package jp.syuriken.snsw.twclient.handler;

import javax.swing.JMenuItem;

import jp.syuriken.snsw.twclient.ActionHandler;
import jp.syuriken.snsw.twclient.ClientConfiguration;

/**
 * リログインするためのアクションハンドラ
 *
 * @author Turenar <snswinhaiku dot lo at gmail dot com>
 */
public class ReloginActionHandler implements ActionHandler {

	private final boolean forWrite;

	private final ClientConfiguration configuration;

	/**
	 * インスタンスを生成する。
	 *
	 * @param forWrite 書き込み用
	 */
	public ReloginActionHandler(boolean forWrite) {
		this.forWrite = forWrite;
		configuration = ClientConfiguration.getInstance();
	}

	@Override
	public JMenuItem createJMenuItem(IntentArguments args) {
		return null;
	}

	@Override
	public void handleAction(IntentArguments args) {
		String actionName = args.getIntentName();
		String accountId = actionName.substring(actionName.indexOf('!') + 1);
		if (forWrite) {
			configuration.setAccountIdForWrite(accountId);
		} else {
			configuration.setAccountIdForRead(accountId);
		}
	}

	@Override
	public void popupMenuWillBecomeVisible(JMenuItem menuItem, IntentArguments args) {
		// TODO Auto-generated method stub

	}
}

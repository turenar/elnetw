package jp.syuriken.snsw.twclient.handler;

import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.JMenuItem;

import jp.syuriken.snsw.twclient.ActionHandler;
import jp.syuriken.snsw.twclient.ClientConfiguration;

/**
 * リストを閲覧するアクションハンドラ
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class ListActionHandler implements ActionHandler {

	@Override
	public JMenuItem createJMenuItem(IntentArguments args) {
		return null;
	}

	@Override
	public void handleAction(IntentArguments args) {
		Object user = args.getExtra("user");
		String userName;
		if (user == null) {
			throw new IllegalArgumentException("Specify extraArg `user`");
		} else if (user instanceof String) {
			userName = (String) user;
		} else {
			throw new IllegalArgumentException("extraArg `user` must be String");
		}
		Object listNameObj = args.getExtra("listName");
		String listName;
		if (listNameObj == null) {
			throw new IllegalArgumentException("Specify extraArg `listName`");
		} else if (user instanceof String) {
			listName = (String) listNameObj;
		} else {
			throw new IllegalArgumentException("extraArg `listName` must be String");
		}

		try {
			ClientConfiguration.getInstance().getUtility().openBrowser(
					new URI("http", "twitter.com", userName + "/" + listName).toASCIIString());
		} catch (URISyntaxException e) {
			throw new AssertionError(e);
		}
	}

	@Override
	public void popupMenuWillBecomeVisible(JMenuItem menuItem, IntentArguments args) {
	}
}

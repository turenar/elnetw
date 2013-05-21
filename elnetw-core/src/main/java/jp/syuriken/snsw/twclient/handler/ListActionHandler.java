package jp.syuriken.snsw.twclient.handler;

import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.JMenuItem;

import jp.syuriken.snsw.twclient.ActionHandler;
import jp.syuriken.snsw.twclient.ClientFrameApi;
import jp.syuriken.snsw.twclient.StatusData;

/**
 * リストを閲覧するアクションハンドラ
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class ListActionHandler implements ActionHandler {

	@Override
	public JMenuItem createJMenuItem(String commandName) {
		return null;
	}

	@Override
	public void handleAction(String actionName, StatusData statusData, ClientFrameApi api) {
		int separatorPosition = actionName.indexOf('!');
		if (separatorPosition == -1 || actionName.contains("/") == false) {
			throw new IllegalArgumentException("list AH must contain user and list. usage: list!<user>/<list>");
		}
		try {
			api.getUtility().openBrowser(
					new URI("http", "twitter.com", actionName.substring(separatorPosition + 1)).toASCIIString());
		} catch (URISyntaxException e) {
			throw new AssertionError(e);
		}
	}

	@Override
	public void popupMenuWillBecomeVisible(JMenuItem menuItem, StatusData statusData, ClientFrameApi api) {
	}
}

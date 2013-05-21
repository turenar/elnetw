package jp.syuriken.snsw.twclient.handler;

import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.JMenuItem;

import jp.syuriken.snsw.twclient.ActionHandler;
import jp.syuriken.snsw.twclient.ClientFrameApi;
import jp.syuriken.snsw.twclient.StatusData;

/**
 * 検索するアクションハンドラ
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class SearchActionHandler implements ActionHandler {

	@Override
	public JMenuItem createJMenuItem(String commandName) {
		return null;
	}

	@Override
	public void handleAction(String actionName, StatusData statusData, ClientFrameApi api) {
		if (actionName.contains("!") == false) {
			throw new IllegalArgumentException("actionName must be include search query: search!<query>");
		}
		String query = actionName.substring(actionName.indexOf('!') + 1);
		try {
			api.getUtility().openBrowser(new URI("http://twitter.com/search/" + query).toASCIIString()); // TODO
		} catch (URISyntaxException e) {
			throw new AssertionError(e);
		}
	}

	@Override
	public void popupMenuWillBecomeVisible(JMenuItem menuItem, StatusData statusData, ClientFrameApi api) {
	}

}

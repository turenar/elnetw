package jp.syuriken.snsw.twclient.handler;

import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.JMenuItem;

import jp.syuriken.snsw.twclient.ActionHandler;
import jp.syuriken.snsw.twclient.ClientConfiguration;

/**
 * 検索するアクションハンドラ
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class SearchActionHandler implements ActionHandler {

	private final ClientConfiguration configuration;

	public SearchActionHandler() {
		configuration = ClientConfiguration.getInstance();
	}

	@Override
	public JMenuItem createJMenuItem(IntentArguments args) {
		return null;
	}

	@Override
	public void handleAction(IntentArguments args) {
		String queryStr = args.getExtraObj("query", String.class);
		if (queryStr == null) {
			throw new IllegalArgumentException("arg `query' is required.");
		}
		try {
			configuration.getUtility().openBrowser(
					new URI("http://twitter.com/search/" + queryStr).toASCIIString()); // TODO
		} catch (URISyntaxException e) {
			throw new AssertionError(e);
		}
	}

	@Override
	public void popupMenuWillBecomeVisible(JMenuItem menuItem, IntentArguments args) {
	}

}

package jp.syuriken.snsw.twclient.handler;

import javax.swing.JMenuItem;

import jp.syuriken.snsw.twclient.ActionHandler;
import jp.syuriken.snsw.twclient.ClientConfiguration;

/**
 * ハッシュタグを処理するアクションハンドラ
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class HashtagActionHandler implements ActionHandler {

	private final ClientConfiguration configuration;

	public HashtagActionHandler() {
		configuration = ClientConfiguration.getInstance();
	}

	@Override
	public JMenuItem createJMenuItem(IntentArguments arguments) {
		return null;
	}

	@Override
	public void handleAction(IntentArguments arguments) {
		String name = arguments.getExtraObj("name", String.class);
		if (name == null) {
			throw new IllegalArgumentException("actionName must be include hashtag: hashtag!<hashtag>");
		}

		IntentArguments query = arguments.clone().setIntentName("search").putExtra("query", "%23" + name);
		configuration.handleAction(query); //TODO
	}

	@Override
	public void popupMenuWillBecomeVisible(JMenuItem menuItem, IntentArguments arguments) {
	}

}

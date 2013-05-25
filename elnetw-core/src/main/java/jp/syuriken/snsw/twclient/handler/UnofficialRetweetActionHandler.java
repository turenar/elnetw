package jp.syuriken.snsw.twclient.handler;

import javax.swing.JMenuItem;

import jp.syuriken.snsw.twclient.ClientFrameApi;
import twitter4j.Status;

/**
 * Unofficial RT (like QT:) Action Handler
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class UnofficialRetweetActionHandler extends StatusActionHandlerBase {

	@Override
	public JMenuItem createJMenuItem(IntentArguments args) {
		return new JMenuItem("非公式RT");
	}

	@Override
	public void handleAction(IntentArguments args) {
		Status status = getStatus(args);
		if (status == null) {
			throwIllegalArgument();
		}
		ClientFrameApi api = configuration.getFrameApi();
		api.setPostText(String.format(" RT @%s: %s", status.getUser().getScreenName(), status.getText()), 0, 0);
		api.focusPostBox();
	}

	@Override
	public void popupMenuWillBecomeVisible(JMenuItem menuItem, IntentArguments args) {
		Status status = getStatus(args);
		menuItem.setEnabled(status != null);
	}

}

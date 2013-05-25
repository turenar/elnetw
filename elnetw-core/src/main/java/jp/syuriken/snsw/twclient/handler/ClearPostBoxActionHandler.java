package jp.syuriken.snsw.twclient.handler;

import javax.swing.JMenuItem;

import jp.syuriken.snsw.twclient.ClientFrameApi;

/**
 * Clear text in PostBox
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class ClearPostBoxActionHandler extends StatusActionHandlerBase {

	@Override
	public JMenuItem createJMenuItem(IntentArguments arguments) {
		return null;
	}

	@Override
	public void handleAction(IntentArguments arguments) {
		ClientFrameApi api = configuration.getFrameApi();
		api.setPostText("");
		api.setTweetLengthCalculator(null);
	}

	@Override
	public void popupMenuWillBecomeVisible(JMenuItem menuItem, IntentArguments arguments) {
	}
}

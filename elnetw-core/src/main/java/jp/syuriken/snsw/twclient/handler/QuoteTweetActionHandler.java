package jp.syuriken.snsw.twclient.handler;

import java.awt.event.KeyEvent;

import javax.swing.JMenuItem;

import jp.syuriken.snsw.twclient.ClientFrameApi;
import jp.syuriken.snsw.twclient.internal.QuoteTweetLengthCalculator;
import twitter4j.Status;

/**
 * QTするためのアクションハンドラ
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class QuoteTweetActionHandler extends StatusActionHandlerBase {

	@Override
	public JMenuItem createJMenuItem(IntentArguments arguments) {
		JMenuItem quoteMenuItem = new JMenuItem("引用(Q)", KeyEvent.VK_Q);
		return quoteMenuItem;
	}

	@Override
	public void handleAction(IntentArguments arguments) {
		Status status = getStatus(arguments);
		if (status == null) {
			throwIllegalArgument();
		}

		ClientFrameApi api = configuration.getFrameApi();
		api.setInReplyToStatus(status);
		api.setPostText(String.format(" QT @%s: %s", status.getUser().getScreenName(), status.getText()), 0, 0);
		api.focusPostBox();
		api.setTweetLengthCalculator(new QuoteTweetLengthCalculator(api));
	}

	@Override
	public void popupMenuWillBecomeVisible(JMenuItem menuItem, IntentArguments arguments) {
		if (getStatus(arguments) != null) {
			menuItem.setEnabled(true);
		} else {
			menuItem.setEnabled(false);
		}
	}

}

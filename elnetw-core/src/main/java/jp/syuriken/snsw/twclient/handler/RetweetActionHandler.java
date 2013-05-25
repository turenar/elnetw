package jp.syuriken.snsw.twclient.handler;

import java.awt.event.KeyEvent;

import javax.swing.JMenuItem;

import jp.syuriken.snsw.twclient.ClientConfiguration;
import jp.syuriken.snsw.twclient.internal.TwitterRunnable;
import twitter4j.Status;
import twitter4j.TwitterException;

/**
 * 公式リツイートするためのアクションハンドラ
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class RetweetActionHandler extends StatusActionHandlerBase {

	private class RetweetTask extends TwitterRunnable {

		private final Status status;

		public RetweetTask(Status status) {
			this.status = status;
		}

		@Override
		public void access() throws TwitterException {
			configuration.getTwitterForWrite().retweetStatus(status.getId());
		}
	}

	private final ClientConfiguration configuration;

	public RetweetActionHandler() {
		configuration = ClientConfiguration.getInstance();
	}

	@Override
	public JMenuItem createJMenuItem(IntentArguments args) {
		JMenuItem retweetMenuItem = new JMenuItem("リツイート(T)", KeyEvent.VK_T);
		return retweetMenuItem;
	}

	@Override
	public void handleAction(IntentArguments args) {
		Status status = getStatus(args);
		if (status == null) {
			throwIllegalArgument();
		}

		configuration.addJob(new RetweetTask(status));
	}

	@Override
	public void popupMenuWillBecomeVisible(JMenuItem menuItem, IntentArguments args) {
		Status status = getStatus(args);
		if (status == null) {
			menuItem.setVisible(false);
			menuItem.setEnabled(false);
		} else {
			menuItem.setEnabled(status.getUser().isProtected() == false);
			menuItem.setVisible(status.getUser().getId() != getLoginUserId());
		}
	}
}

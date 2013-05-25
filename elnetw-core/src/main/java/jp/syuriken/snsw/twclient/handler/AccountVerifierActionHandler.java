package jp.syuriken.snsw.twclient.handler;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import jp.syuriken.snsw.twclient.ActionHandler;
import jp.syuriken.snsw.twclient.ClientConfiguration;

/**
 * アカウント認証するアクションハンドラ
 *
 * @author Turenar <snswinhaiku dot lo at gmail dot com>
 */
public class AccountVerifierActionHandler implements ActionHandler {

	@Override
	public JMenuItem createJMenuItem(IntentArguments args) {
		return null;
	}

	@Override
	public void handleAction(IntentArguments args) {
		final ClientConfiguration configuration= ClientConfiguration.getInstance();
		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {
				Exception exception = configuration.tryGetOAuthToken();
				if (exception != null) {
					JOptionPane.showMessageDialog(configuration.getFrameApi().getFrame(),
							"認証に失敗しました: " + exception.getMessage(), "エラー",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		}, "oauth-thread");
		thread.start();
	}

	@Override
	public void popupMenuWillBecomeVisible(JMenuItem menuItem,IntentArguments args) {
	}
}

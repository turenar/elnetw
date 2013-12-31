package jp.syuriken.snsw.twclient.handler;

import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import jp.syuriken.snsw.twclient.ClientConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.Status;
import twitter4j.URLEntity;

/**
 * ツイートに含まれるURLを開くアクションハンドラ
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class UrlActionHandler extends StatusActionHandlerBase {

	private static final Logger logger = LoggerFactory.getLogger(UrlActionHandler.class);
	private final ClientConfiguration configuration;

	public UrlActionHandler() {
		configuration = ClientConfiguration.getInstance();
	}

	@Override
	public JMenuItem createJMenuItem(IntentArguments arguments) {
		JMenu openUrlMenu = new JMenu("ツイートのURLをブラウザで開く");
		return openUrlMenu;
	}

	@Override
	public void handleAction(IntentArguments arguments) {
		String url = arguments.getExtraObj("url", String.class);
		if (url == null) {
			throw new IllegalArgumentException("arg `url' is not found");
		}
		try {
			configuration.getUtility().openBrowser(url);
		} catch (Exception e) {
			logger.warn("Failed open browser", e);
		}
	}

	@Override
	public void popupMenuWillBecomeVisible(JMenuItem menuItem, IntentArguments arguments) {
		if (menuItem instanceof JMenu == false) {
			throw new AssertionError("UrlActionHandler#pMWBV transfered menuItem which is not instanceof JMenu");
		}
		JMenu menu = (JMenu) menuItem;

		Status status = getStatus(arguments);
		if (status != null) {
			menu.removeAll();

			URLEntity[] urlEntities = status.getURLEntities();
			if (urlEntities == null || urlEntities.length == 0) {
				menu.setEnabled(false);
			} else {
				for (URLEntity entity : status.getURLEntities()) {
					JMenuItem urlMenu = new JMenuItem();
					if (entity.getDisplayURL() == null) {
						urlMenu.setText(entity.getURL());
					} else {
						urlMenu.setText(entity.getDisplayURL());
					}
					urlMenu.setActionCommand("url!" + entity.getURL());
					for (ActionListener listener : menu.getActionListeners()) {
						urlMenu.addActionListener(listener);
					}
					menu.add(urlMenu);
				}
				menu.setEnabled(true);
			}
		} else {
			menu.setEnabled(false);
		}
	}
}

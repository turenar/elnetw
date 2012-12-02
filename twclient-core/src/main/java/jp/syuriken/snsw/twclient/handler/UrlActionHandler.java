package jp.syuriken.snsw.twclient.handler;

import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import jp.syuriken.snsw.twclient.ActionHandler;
import jp.syuriken.snsw.twclient.ClientFrameApi;
import jp.syuriken.snsw.twclient.StatusData;
import twitter4j.Status;
import twitter4j.URLEntity;

/**
 * ツイートに含まれるURLを開くアクションハンドラ
 * 
 * @author Turenar <snswinhaiku dot lo at gmail dot com>
 */
public class UrlActionHandler implements ActionHandler {
	
	@Override
	public JMenuItem createJMenuItem(String commandName) {
		JMenu openUrlMenu = new JMenu("ツイートのURLをブラウザで開く");
		return openUrlMenu;
	}
	
	@Override
	public void handleAction(String actionName, StatusData statusData, ClientFrameApi api) {
		String url = actionName.substring(actionName.indexOf('!') + 1);
		try {
			api.getUtility().openBrowser(url);
		} catch (Exception e) {
			e.printStackTrace(); //TODO
		}
	}
	
	@Override
	public void popupMenuWillBecomeVisible(JMenuItem menuItem, StatusData statusData, ClientFrameApi api) {
		if (menuItem instanceof JMenu == false) {
			throw new AssertionError("UrlActionHandler#pMWBV transfered menuItem which is not instanceof JMenu");
		}
		JMenu menu = (JMenu) menuItem;
		if (statusData.isSystemNotify() == false && statusData.tag instanceof Status) {
			Status status = (Status) statusData.tag;
			menu.removeAll();
			
			URLEntity[] urlEntities = status.getURLEntities();
			if (urlEntities == null || urlEntities.length == 0) {
				menu.setEnabled(false);
			} else {
				for (URLEntity entity : status.getURLEntities()) {
					JMenuItem urlMenu = new JMenuItem();
					if (entity.getDisplayURL() == null) {
						urlMenu.setText(entity.getURL().toString());
					} else {
						urlMenu.setText(entity.getDisplayURL());
					}
					urlMenu.setActionCommand("url!" + entity.getURL().toString());
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

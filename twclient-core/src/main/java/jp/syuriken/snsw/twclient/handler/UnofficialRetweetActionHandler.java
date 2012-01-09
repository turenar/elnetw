package jp.syuriken.snsw.twclient.handler;

import javax.swing.JMenuItem;

import jp.syuriken.snsw.twclient.ActionHandler;
import jp.syuriken.snsw.twclient.ClientFrameApi;
import jp.syuriken.snsw.twclient.StatusData;
import twitter4j.Status;

/**
 * TODO snsoftware
 * 
 * @author $Author$
 */
public class UnofficialRetweetActionHandler implements ActionHandler {
	
	@Override
	public JMenuItem createJMenuItem(String commandName) {
		return new JMenuItem("非公式RT");
	}
	
	@Override
	public void handleAction(String actionName, StatusData statusData, ClientFrameApi api) {
		if (statusData.tag instanceof Status) {
			Status status = (Status) statusData.tag;
			api.setPostText(String.format(" RT @%s: %s", status.getUser().getScreenName(), status.getText()), 0, 0);
			api.focusPostBox();
		}
	}
	
	@Override
	public void popupMenuWillBecomeVisible(JMenuItem menuItem, StatusData statusData, ClientFrameApi api) {
		if ((statusData.isSystemNotify() == false) && (statusData.tag instanceof Status)) {
			menuItem.setEnabled(true);
		} else {
			menuItem.setEnabled(false);
		}
	}
	
}

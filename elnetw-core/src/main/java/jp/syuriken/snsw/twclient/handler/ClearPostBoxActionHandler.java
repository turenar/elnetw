package jp.syuriken.snsw.twclient.handler;

import javax.swing.JMenuItem;

import jp.syuriken.snsw.twclient.ActionHandler;
import jp.syuriken.snsw.twclient.ClientFrameApi;
import jp.syuriken.snsw.twclient.StatusData;

/**
 * TODO snsoftware
 * 
 * @author Turenar <snswinhaiku dot lo at gmail dot com>
 */
public class ClearPostBoxActionHandler implements ActionHandler {
	
	@Override
	public JMenuItem createJMenuItem(String commandName) {
		return null;
	}
	
	@Override
	public void handleAction(String actionName, StatusData statusData, ClientFrameApi api) {
		api.setPostText("");
		api.setTweetLengthCalculator(null);
	}
	
	@Override
	public void popupMenuWillBecomeVisible(JMenuItem menuItem, StatusData statusData, ClientFrameApi api) {
	}
}

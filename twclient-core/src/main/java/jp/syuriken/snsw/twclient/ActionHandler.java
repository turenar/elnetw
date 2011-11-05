package jp.syuriken.snsw.twclient;

import javax.swing.JMenuItem;

/**
 * TODO snsoftware
 * 
 * @author $Author$
 */
public interface ActionHandler {
	
	void dispatchAction(String actionName, StatusData statusData, TwitterClientFrame frameInstance);
	
	void popupMenuWillBecomeVisible(JMenuItem menuItem, StatusData statusData);
}

package jp.syuriken.snsw.twclient.handler;

import javax.swing.JMenuItem;

import jp.syuriken.snsw.twclient.ActionHandler;
import jp.syuriken.snsw.twclient.ClientFrameApi;
import jp.syuriken.snsw.twclient.StatusData;

/**
 * ハッシュタグを処理するアクションハンドラ
 * 
 * @author $Author$
 */
public class HashtagActionHandler implements ActionHandler {
	
	@Override
	public JMenuItem createJMenuItem(String commandName) {
		return null;
	}
	
	@Override
	public void handleAction(String actionName, StatusData statusData, ClientFrameApi api) {
		if (actionName.contains("!") == false) {
			throw new IllegalArgumentException("actionName must be include hashtag: hashtag!<hashtag>");
		}
		String hashtag = actionName.substring(actionName.indexOf('!') + 1);
		api.handleAction("search!%23" + hashtag, statusData); //TODO
	}
	
	@Override
	public void popupMenuWillBecomeVisible(JMenuItem menuItem, StatusData statusData, ClientFrameApi api) {
	}
	
}

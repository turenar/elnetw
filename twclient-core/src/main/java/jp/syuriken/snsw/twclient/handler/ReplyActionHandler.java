package jp.syuriken.snsw.twclient.handler;

import java.awt.event.KeyEvent;

import javax.swing.JMenuItem;

import jp.syuriken.snsw.twclient.ActionHandler;
import jp.syuriken.snsw.twclient.ClientFrameApi;
import jp.syuriken.snsw.twclient.StatusData;
import twitter4j.Status;

/**
 * リプライするためのアクションハンドラ
 * 
 * @author $Author$
 */
public class ReplyActionHandler implements ActionHandler {
	
	@Override
	public JMenuItem createJMenuItem(String commandName) {
		JMenuItem replyMenuItem = new JMenuItem("Reply", KeyEvent.VK_R);
		return replyMenuItem;
	}
	
	@Override
	public void handleAction(String actionName, StatusData statusData, ClientFrameApi api) {
		if (statusData.tag instanceof Status) {
			Status status = (Status) statusData.tag;
			String text = String.format("@%s ", status.getUser().getScreenName());
			api.setPostText(text, text.length(), text.length());
			api.setInReplyToStatus(status);
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

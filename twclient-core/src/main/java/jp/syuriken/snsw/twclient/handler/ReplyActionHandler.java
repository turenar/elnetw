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
 * @author Turenar <snswinhaiku dot lo at gmail dot com>
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
			String text;
			String screenName = status.getUser().getScreenName();
			if (actionName.equals("reply!append")) {
				String postText = api.getPostText();
				if (postText.trim().isEmpty()) {
					text = String.format(".@%s ", screenName);
				} else {
					text = String.format("%s@%s ", postText, screenName);
				}
				api.setPostText(text);
			} else {
				text = String.format("@%s ", screenName);
				api.focusPostBox();
			}
			api.setPostText(text, text.length(), text.length());
			api.setInReplyToStatus(status);
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

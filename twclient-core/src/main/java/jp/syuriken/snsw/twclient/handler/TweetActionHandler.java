package jp.syuriken.snsw.twclient.handler;

import javax.swing.JMenuItem;

import jp.syuriken.snsw.twclient.ActionHandler;
import jp.syuriken.snsw.twclient.ClientFrameApi;
import jp.syuriken.snsw.twclient.ClientMessageListener;
import jp.syuriken.snsw.twclient.StatusData;
import jp.syuriken.snsw.twclient.Utility;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ツイートをどうにかするアクションハンドラ
 * 
 * @author $Author$
 */
public class TweetActionHandler implements ActionHandler {
	
	private static Logger logger = LoggerFactory.getLogger(TweetActionHandler.class);
	
	
	@Override
	public JMenuItem createJMenuItem(String commandName) {
		return null;
	}
	
	@Override
	public void handleAction(String actionName, StatusData statusData, ClientFrameApi api) {
		String messageName;
		if (Utility.equalString(actionName, "tweet!copy")) {
			messageName = ClientMessageListener.REQUEST_COPY;
		} else if (Utility.equalString(actionName, "tweet!copyurl")) {
			messageName = ClientMessageListener.REQUEST_COPY_URL;
		} else if (Utility.equalString(actionName, "tweet!copyuserid")) {
			messageName = ClientMessageListener.REQUEST_COPY_USERID;
		} else if (Utility.equalString(actionName, "tweet!browser_user")) {
			messageName = ClientMessageListener.REQUEST_BROWSER_USER_HOME;
		} else if (Utility.equalString(actionName, "tweet!browser_status")) {
			messageName = ClientMessageListener.REQUEST_BROWSER_STATUS;
		} else if (Utility.equalString(actionName, "tweet!browser_replyTo")) {
			messageName = ClientMessageListener.REQUEST_BROWSER_IN_REPLY_TO;
		} else if (Utility.equalString(actionName, "tweet!openurls")) {
			messageName = ClientMessageListener.REQUEST_BROWSER_OPENURLS;
		} else {
			logger.warn("{} is not action", actionName);
			return;
		}
		api.getSelectingTab().getRenderer().onClientMessage(messageName, null);
	}
	
	@Override
	public void popupMenuWillBecomeVisible(JMenuItem menuItem, StatusData statusData, ClientFrameApi api) {
		// do nothing
	}
	
}

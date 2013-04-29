package jp.syuriken.snsw.twclient.handler;

import javax.swing.JMenuItem;

import jp.syuriken.snsw.twclient.ActionHandler;
import jp.syuriken.snsw.twclient.ClientFrameApi;
import jp.syuriken.snsw.twclient.ClientMessageListener;
import jp.syuriken.snsw.twclient.StatusData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ツイートをどうにかするアクションハンドラ
 *
 * @author Turenar <snswinhaiku dot lo at gmail dot com>
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
		switch (actionName) {
			case "tweet!copy":
				messageName = ClientMessageListener.REQUEST_COPY;
				break;
			case "tweet!copyurl":
				messageName = ClientMessageListener.REQUEST_COPY_URL;
				break;
			case "tweet!copyuserid":
				messageName = ClientMessageListener.REQUEST_COPY_USERID;
				break;
			case "tweet!browser_user":
				messageName = ClientMessageListener.REQUEST_BROWSER_USER_HOME;
				break;
			case "tweet!browser_status":
				messageName = ClientMessageListener.REQUEST_BROWSER_STATUS;
				break;
			case "tweet!browser_replyTo":
				messageName = ClientMessageListener.REQUEST_BROWSER_IN_REPLY_TO;
				break;
			case "tweet!openurls":
				messageName = ClientMessageListener.REQUEST_BROWSER_OPENURLS;
				break;
			default:
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

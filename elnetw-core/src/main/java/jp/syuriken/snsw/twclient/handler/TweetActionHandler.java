package jp.syuriken.snsw.twclient.handler;

import javax.swing.JMenuItem;

import jp.syuriken.snsw.twclient.ClientMessageListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ツイートをどうにかするアクションハンドラ
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class TweetActionHandler extends StatusActionHandlerBase {

	private static Logger logger = LoggerFactory.getLogger(TweetActionHandler.class);

	@Override
	public JMenuItem createJMenuItem(IntentArguments arguments) {
		return null;
	}

	@Override
	public void handleAction(IntentArguments arguments) {
		String actionName = arguments.getExtraObj("action", String.class);
		String messageName;
		switch (actionName) {
			case "copy":
				messageName = ClientMessageListener.REQUEST_COPY;
				break;
			case "copyurl":
				messageName = ClientMessageListener.REQUEST_COPY_URL;
				break;
			case "copyuserid":
				messageName = ClientMessageListener.REQUEST_COPY_USERID;
				break;
			case "browser_user":
				messageName = ClientMessageListener.REQUEST_BROWSER_USER_HOME;
				break;
			case "browser_status":
				messageName = ClientMessageListener.REQUEST_BROWSER_STATUS;
				break;
			case "browser_replyTo":
				messageName = ClientMessageListener.REQUEST_BROWSER_IN_REPLY_TO;
				break;
			case "openurls":
				messageName = ClientMessageListener.REQUEST_BROWSER_OPENURLS;
				break;
			default:
				logger.warn("{} is not action", actionName);
				return;
		}
		configuration.getFrameApi().getSelectingTab().getRenderer().onClientMessage(messageName, null);
	}

	@Override
	public void popupMenuWillBecomeVisible(JMenuItem menuItem, IntentArguments arguments) {
		// do nothing
	}

}

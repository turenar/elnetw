package jp.syuriken.snsw.twclient.handler;

import java.awt.event.KeyEvent;

import javax.swing.JMenuItem;

import jp.syuriken.snsw.twclient.ClientConfiguration;
import jp.syuriken.snsw.twclient.ClientFrameApi;
import twitter4j.Status;

/**
 * リプライするためのアクションハンドラ
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class ReplyActionHandler extends StatusActionHandlerBase {

	private final ClientConfiguration configuration;
	private final ClientFrameApi frameApi;

	public ReplyActionHandler() {
		configuration = ClientConfiguration.getInstance();
		frameApi = configuration.getFrameApi();
	}

	@Override
	public JMenuItem createJMenuItem(IntentArguments args) {
		JMenuItem replyMenuItem = new JMenuItem("Reply", KeyEvent.VK_R);
		return replyMenuItem;
	}

	@Override
	public void handleAction(IntentArguments args) {
		Status status = getStatus(args);
		if (status == null) {
			throwIllegalArgument();
		}

		String text;
		String screenName = status.getUser().getScreenName();

		Object appendFlagObj = args.getExtra("append");
		boolean appendFlag;
		if (appendFlagObj instanceof Boolean) {
			appendFlag = (Boolean) appendFlagObj;
		} else if (appendFlagObj != null) {
			appendFlag = true;
		} else {
			appendFlag = false;
		}
		if (appendFlag) {
			String postText = configuration.getFrameApi().getPostText();
			if (postText.trim().isEmpty()) {
				text = String.format(".@%s ", screenName);
			} else {
				text = String.format("%s@%s ", postText, screenName);
			}
			frameApi.setPostText(text);
		} else {
			text = String.format("@%s ", screenName);
			frameApi.focusPostBox();
		}
		frameApi.setPostText(text, text.length(), text.length());
		frameApi.setInReplyToStatus(status);
	}

	@Override
	public void popupMenuWillBecomeVisible(JMenuItem menuItem, IntentArguments arguments) {
		Status status = getStatus(arguments);
		menuItem.setEnabled(status != null);
	}
}

package jp.syuriken.snsw.twclient.handler;

import java.awt.event.KeyEvent;

import javax.swing.JMenuItem;

import jp.syuriken.snsw.twclient.ActionHandler;
import jp.syuriken.snsw.twclient.ClientFrameApi;
import jp.syuriken.snsw.twclient.ParallelRunnable;
import jp.syuriken.snsw.twclient.StatusData;
import twitter4j.Status;
import twitter4j.TwitterException;

/**
 * 公式リツイートするためのアクションハンドラ
 * 
 * @author $Author$
 */
public class RetweetActionHandler implements ActionHandler {
	
	@Override
	public JMenuItem createJMenuItem(String commandName) {
		JMenuItem retweetMenuItem = new JMenuItem("リツイート(T)", KeyEvent.VK_T);
		return retweetMenuItem;
	}
	
	@Override
	public void handleAction(String actionName, StatusData statusData, final ClientFrameApi api) {
		if (statusData.tag instanceof Status) {
			final Status retweetStatus = (Status) statusData.tag;
			api.addJob(new ParallelRunnable() {
				
				@Override
				public void run() {
					try {
						api.getTwitter().retweetStatus(retweetStatus.getId());
					} catch (TwitterException e) {
						api.handleException(e);
					}
				}
			});
		}
	}
	
	@Override
	public void popupMenuWillBecomeVisible(JMenuItem menuItem, StatusData statusData, ClientFrameApi api) {
		if (statusData.isSystemNotify() || (statusData.tag instanceof Status) == false) {
			menuItem.setEnabled(false);
		}
		if (statusData.tag instanceof Status) {
			Status status = (Status) statusData.tag;
			menuItem.setEnabled(status.getUser().getId() != api.getLoginUser().getId());
		}
	}
	
}

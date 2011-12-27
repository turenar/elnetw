package jp.syuriken.snsw.twclient.handler;

import java.awt.event.KeyEvent;
import java.text.MessageFormat;

import javax.swing.JMenuItem;

import jp.syuriken.snsw.twclient.ActionHandler;
import jp.syuriken.snsw.twclient.ClientFrameApi;
import jp.syuriken.snsw.twclient.JobQueue.Priority;
import jp.syuriken.snsw.twclient.ParallelRunnable;
import jp.syuriken.snsw.twclient.StatusData;
import jp.syuriken.snsw.twclient.Utility;
import twitter4j.Status;

/**
 * ユーザー情報を表示するアクションハンドラ
 * 
 * @author $Author$
 */
public class UserInfoViewActionHandler implements ActionHandler {
	
	@Override
	public JMenuItem createJMenuItem(String commandName) {
		JMenuItem aboutMenuItem = new JMenuItem("ユーザーについて(A)...", KeyEvent.VK_A);
		return aboutMenuItem;
	}
	
	@Override
	public void handleAction(String actionName, final StatusData statusData, ClientFrameApi api) {
		if (statusData.tag instanceof Status) {
			api.addJob(Priority.MEDIUM, new ParallelRunnable() {
				
				@Override
				public void run() {
					Status status = (Status) statusData.tag;
					try {
						Utility.openBrowser("http://twitter.com/" + status.getUser().getScreenName());
					} catch (Exception e) {
						e.printStackTrace(); //TODO
					}
				}
			});
		}
	}
	
	@Override
	public void popupMenuWillBecomeVisible(JMenuItem menuItem, StatusData statusData, ClientFrameApi api) {
		if ((statusData.isSystemNotify() == false) && (statusData.tag instanceof Status)) {
			Status status = (Status) statusData.tag;
			if (status.isRetweet()) {
				status = status.getRetweetedStatus();
			}
			menuItem.setText(MessageFormat.format("@{0} ({1}) について(A)", status.getUser().getScreenName(), status
				.getUser().getName()));
			menuItem.setEnabled(true);
		} else {
			menuItem.setEnabled(false);
		}
	}
}

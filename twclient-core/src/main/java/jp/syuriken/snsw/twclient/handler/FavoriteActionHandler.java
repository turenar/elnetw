package jp.syuriken.snsw.twclient.handler;

import java.awt.event.KeyEvent;

import javax.swing.JMenuItem;

import jp.syuriken.snsw.twclient.ActionHandler;
import jp.syuriken.snsw.twclient.ClientFrameApi;
import jp.syuriken.snsw.twclient.ParallelRunnable;
import jp.syuriken.snsw.twclient.StatusData;
import jp.syuriken.snsw.twclient.TwitterStatus;
import twitter4j.Status;
import twitter4j.TwitterException;

/**
 * ふぁぼる
 * 
 * @author $Author$
 */
public class FavoriteActionHandler implements ActionHandler {
	
	@Override
	public JMenuItem createJMenuItem(String commandName) {
		JMenuItem favMenuItem = new JMenuItem("ふぁぼる(F)", KeyEvent.VK_F);
		return favMenuItem;
	}
	
	@Override
	public void handleAction(final String actionName, final StatusData statusData, final ClientFrameApi api) {
		if (statusData.tag instanceof Status) {
			api.addJob(new ParallelRunnable() {
				
				@Override
				public void run() {
					Status status = (Status) statusData.tag;
					boolean unfavorite;
					if (actionName.equals("fav!force=f")) {
						unfavorite = false;
					} else if (actionName.equals("fav!force=u")) {
						unfavorite = true;
					} else {
						if (status instanceof TwitterStatus && ((TwitterStatus) status).isFavorited()) {
							unfavorite = true;
						} else {
							unfavorite = false;
						}
					}
					try {
						if (unfavorite) {
							api.getTwitterForWrite().destroyFavorite(status.getId());
						} else {
							api.getTwitterForWrite().createFavorite(status.getId());
						}
					} catch (TwitterException e) {
						api.handleException(e);
					}
				}
			});
		}
	}
	
	@Override
	public void popupMenuWillBecomeVisible(JMenuItem menuItem, StatusData statusData, ClientFrameApi api) {
		if ((statusData.isSystemNotify() == false) && (statusData.tag instanceof Status)) {
			if (statusData.tag instanceof TwitterStatus) {
				TwitterStatus tag = (TwitterStatus) statusData.tag;
				menuItem.setText(tag.isFavorited() ? "ふぁぼを解除する(F)" : "ふぁぼる(F)");
			} else {
				menuItem.setText("ふぁぼる(F)");
			}
			menuItem.setEnabled(true);
		} else {
			menuItem.setEnabled(false);
		}
	}
}

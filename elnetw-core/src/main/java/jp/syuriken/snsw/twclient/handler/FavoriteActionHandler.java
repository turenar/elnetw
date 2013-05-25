package jp.syuriken.snsw.twclient.handler;

import java.awt.event.KeyEvent;

import javax.swing.JMenuItem;

import jp.syuriken.snsw.twclient.ClientConfiguration;
import jp.syuriken.snsw.twclient.TwitterStatus;
import jp.syuriken.snsw.twclient.internal.TwitterRunnable;
import twitter4j.Status;
import twitter4j.TwitterException;

/**
 * ふぁぼる
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class FavoriteActionHandler extends StatusActionHandlerBase {

	private static class FavTask extends TwitterRunnable {

		private final boolean favFlag;

		private final long statusId;

		private final Status status;

		public FavTask(boolean favFlag, Status status) {
			this.favFlag = favFlag;
			this.statusId = status.getId();
			this.status = status;
		}

		@Override
		public void access() throws TwitterException {
			Status newStatus;
			if (favFlag) {
				newStatus = configuration.getTwitterForWrite().createFavorite(statusId);
			} else {
				newStatus = configuration.getTwitterForWrite().destroyFavorite(statusId);
			}
			if (status instanceof TwitterStatus) {
				((TwitterStatus) status).update(newStatus);
			}
		}
	}

	@Override
	public JMenuItem createJMenuItem(IntentArguments arguments) {
		JMenuItem favMenuItem = new JMenuItem("ふぁぼる(F)", KeyEvent.VK_F);
		return favMenuItem;
	}

	@Override
	public void handleAction(IntentArguments arguments) throws IllegalArgumentException {
		Status status = getStatus(arguments);
		if (status == null) {
			throwIllegalArgument();
		}
		boolean favFlag = !status.isFavorited();

		String forceFlag = arguments.getExtraObj("force", String.class);
		if (forceFlag.equals("f") || forceFlag.equals("fav")) {
			favFlag = true;
		} else if (forceFlag.equals("u") || forceFlag.equals("unfav")) {
			favFlag = false;
		}

		ClientConfiguration.getInstance().addJob(new FavTask(favFlag, status));
	}

	@Override
	public void popupMenuWillBecomeVisible(JMenuItem menuItem, IntentArguments arguments) {
		Status status = getStatus(arguments);
		if (status != null) {
			if (status instanceof TwitterStatus) {
				TwitterStatus tag = (TwitterStatus) status;
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

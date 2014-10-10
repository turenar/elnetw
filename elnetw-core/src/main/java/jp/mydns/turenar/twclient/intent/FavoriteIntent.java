/*
 * The MIT License (MIT)
 * Copyright (c) 2011-2014 Turenai Project
 *
 * Permission is hereby granted, free of charge,
 *  to any person obtaining a copy of this software and associated documentation files (the "Software"),
 *  to deal in the Software without restriction, including without limitation the rights to
 *  use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 *  and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 *  in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 *  INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 *  IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 *  DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package jp.mydns.turenar.twclient.intent;

import javax.swing.JMenuItem;

import jp.mydns.turenar.twclient.ClientConfiguration;
import jp.mydns.turenar.twclient.Utility;
import jp.mydns.turenar.twclient.internal.TwitterRunnable;
import jp.mydns.turenar.twclient.twitter.TwitterStatus;
import twitter4j.Status;
import twitter4j.TwitterException;

import static jp.mydns.turenar.twclient.i18n.LocalizationResource.tr;

/**
 * ふぁぼる
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class FavoriteIntent extends AbstractIntent {

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
	public void createJMenuItem(PopupMenuDispatcher dispatcher, IntentArguments args) {
		JMenuItem menuItem = new JMenuItem();
		Utility.setMnemonic(menuItem, tr("&Favorite"));
		TwitterStatus status = getStatus(args);
		if (status != null) {
			String forceFlag = args.getExtraObj("force", String.class, "undefined");
			boolean favFlag;
			switch (forceFlag) {
				case "f":
				case "fav":
					favFlag = false; // unfav->fav
					break;
				case "u":
				case "unfav":
					favFlag = true; // fav->unfav
					break;
				default:
					favFlag = status.isFavorited();
					break;
			}
			menuItem.setText(favFlag ? tr("Un&favorite") : tr("&Favorite"));
			menuItem.setEnabled(true);
		} else {
			menuItem.setEnabled(false);
		}
		dispatcher.addMenu(menuItem, args);
	}

	@Override
	public void handleAction(IntentArguments arguments) throws IllegalArgumentException {
		Status status = getStatus(arguments);
		if (status == null) {
			throwIllegalArgument();
			return; // dead code
		}

		boolean favFlag = !status.isFavorited();

		String forceFlag = arguments.getExtraObj("force", String.class);
		if ("f".equals(forceFlag) || "fav".equals(forceFlag)) {
			favFlag = true;
		} else if ("u".equals(forceFlag) || "unfav".equals(forceFlag)) {
			favFlag = false;
		}

		ClientConfiguration.getInstance().addJob(new FavTask(favFlag, status));
	}
}

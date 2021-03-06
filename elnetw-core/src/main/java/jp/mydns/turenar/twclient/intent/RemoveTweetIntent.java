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

import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import jp.mydns.turenar.twclient.ParallelRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.Status;
import twitter4j.TwitterException;

import static jp.mydns.turenar.twclient.i18n.LocalizationResource.tr;

/**
 * ツイートを削除するためのアクションハンドラ
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class RemoveTweetIntent extends AbstractIntent {

	private class TweetDeleteTask implements ParallelRunnable {

		private final Status status;

		public TweetDeleteTask(Status status) {
			this.status = status;
		}

		@Override
		public void run() {
			try {
				configuration.getTwitterForWrite().destroyStatus(status.getId());
			} catch (TwitterException e) {
				logger.warn("Failed removing tweet", e);
			}
		}
	}

	/*package*/static final Logger logger = LoggerFactory.getLogger(RemoveTweetIntent.class);

	@Override
	public void createJMenuItem(PopupMenuDispatcher dispatcher, IntentArguments args) {
		JMenuItem deleteMenuItem = new JMenuItem(tr("Delete..."), KeyEvent.VK_D);
		Status status = getStatus(args);
		if (status != null) {
			boolean isTweetedByMe = status.getUser().getId() == getLoginUserId();
			deleteMenuItem.setVisible(isTweetedByMe);
			deleteMenuItem.setEnabled(isTweetedByMe);
		} else {
			deleteMenuItem.setVisible(false);
			deleteMenuItem.setEnabled(false);
		}

		dispatcher.addMenu(deleteMenuItem, args);
	}

	@Override
	public void handleAction(IntentArguments arguments) {
		final Status status = getStatus(arguments);
		if (status == null) {
			throwIllegalArgument();
		}
		boolean isTweetedByMe = status.getUser().getId() == getLoginUserId();
		if (isTweetedByMe) {
			JPanel panel = new JPanel();
			BoxLayout layout = new BoxLayout(panel, BoxLayout.Y_AXIS);
			panel.setLayout(layout);
			panel.add(new JLabel(tr("Delete below tweet?")));
			panel.add(Box.createVerticalStrut(15));
			panel.add(new JLabel(status.getText()));
			final JOptionPane pane =
					new JOptionPane(panel, JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
			JDialog dialog = pane.createDialog(null, tr("Confirm"));
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			pane.addPropertyChangeListener(new PropertyChangeListener() {

				@Override
				public void propertyChange(PropertyChangeEvent evt) {
					if (evt.getPropertyName().equals(JOptionPane.VALUE_PROPERTY)) {
						if (Integer.valueOf(JOptionPane.OK_OPTION).equals(pane.getValue())) {
							configuration.addJob(new TweetDeleteTask(status));
						}
					}
				}
			});
			dialog.setVisible(true);
		}
	}
}

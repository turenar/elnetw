package jp.syuriken.snsw.twclient.handler;

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

import jp.syuriken.snsw.twclient.ActionHandler;
import jp.syuriken.snsw.twclient.ClientFrameApi;
import jp.syuriken.snsw.twclient.ParallelRunnable;
import jp.syuriken.snsw.twclient.StatusData;
import twitter4j.Status;
import twitter4j.TwitterException;

/**
 * ツイートを削除するためのアクションハンドラ
 *
 * @author Turenar <snswinhaiku dot lo at gmail dot com>
 */
public class RemoveTweetActionHandler implements ActionHandler {

	@Override
	public JMenuItem createJMenuItem(String commandName) {
		JMenuItem deleteMenuItem = new JMenuItem("削除(D)...", KeyEvent.VK_D);
		return deleteMenuItem;
	}

	@Override
	public void handleAction(String actionName, StatusData statusData, final ClientFrameApi api) {
		if (statusData.tag instanceof Status) {
			final Status status = (Status) statusData.tag;
			boolean isTweetedByMe = status.getUser().getId() == api.getLoginUser().getId();
			if (isTweetedByMe) {
				JPanel panel = new JPanel();
				BoxLayout layout = new BoxLayout(panel, BoxLayout.Y_AXIS);
				panel.setLayout(layout);
				panel.add(new JLabel("次のツイートを削除しますか？"));
				panel.add(Box.createVerticalStrut(15));
				panel.add(new JLabel(status.getText()));
				final JOptionPane pane =
						new JOptionPane(panel, JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
				JDialog dialog = pane.createDialog(null, "確認");
				dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
				pane.addPropertyChangeListener(new PropertyChangeListener() {

					@Override
					public void propertyChange(PropertyChangeEvent evt) {
						if (evt.getPropertyName().equals(JOptionPane.VALUE_PROPERTY)) {
							if (Integer.valueOf(JOptionPane.OK_OPTION).equals(pane.getValue())) {
								api.addJob(new ParallelRunnable() {

									@Override
									public void run() {
										try {
											api.getTwitterForWrite().destroyStatus(status.getId());
										} catch (TwitterException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
									}
								});
							}
						}
					}

				});
				dialog.setVisible(true);
			}
		}
	}

	@Override
	public void popupMenuWillBecomeVisible(JMenuItem menuItem, StatusData statusData, ClientFrameApi api) {
		if ((statusData.isSystemNotify() == false) && (statusData.tag instanceof Status)) {
			boolean isTweetedByMe = ((Status) statusData.tag).getUser().getId() == api.getLoginUser().getId();
			menuItem.setVisible(isTweetedByMe);
			menuItem.setEnabled(isTweetedByMe);
		} else {
			menuItem.setVisible(false);
			menuItem.setEnabled(false);
		}
	}
}

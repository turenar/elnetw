package jp.syuriken.snsw.twclient.handler;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.MessageFormat;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import jp.syuriken.snsw.twclient.ClientProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.Status;
import twitter4j.User;

/**
 * ミュートするオプションを提供するアクションハンドラ
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class MuteActionHandler extends StatusActionHandlerBase {

	private static final Logger logger = LoggerFactory.getLogger(MuteActionHandler.class);

	@Override
	public JMenuItem createJMenuItem(IntentArguments arguments) {
		return new JMenuItem("ミュートに追加する");
	}

	@Override
	public void handleAction(IntentArguments arguments) {
		Status status = getStatus(arguments);
		if (status == null) {
			throwIllegalArgument();
		}
		if (status.isRetweet()) {
			status = status.getRetweetedStatus();
		}
		final User user = status.getUser();
		boolean isTweetedByMe = user.getId() == getLoginUserId();
		if (isTweetedByMe == false) {
			JPanel panel = new JPanel();
			BoxLayout layout = new BoxLayout(panel, BoxLayout.Y_AXIS);
			panel.setLayout(layout);
			panel.add(new JLabel("次のツイートをミュートしますか？"));
			panel.add(Box.createVerticalStrut(15));
			panel.add(new JLabel(MessageFormat.format("@{0} ({1})", user.getScreenName(), user.getName())));
			final JOptionPane pane =
					new JOptionPane(panel, JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
			JDialog dialog = pane.createDialog(null, "確認");
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			pane.addPropertyChangeListener(new PropertyChangeListener() {

				@Override
				public void propertyChange(PropertyChangeEvent evt) {
					if (evt.getPropertyName().equals(JOptionPane.VALUE_PROPERTY)) {
						if (Integer.valueOf(JOptionPane.OK_OPTION).equals(pane.getValue())) {
							ClientProperties configProperties = configuration.getConfigProperties();
							String idsString = configProperties.getProperty("core.filter.user.ids");
							idsString =
									idsString == null || idsString.trim().isEmpty() ? String.valueOf(user.getId())
											: idsString + " " + user.getId();
							configProperties.setProperty("core.filter.user.ids", idsString);
						}
					}
				}
			});
			dialog.setVisible(true);
		}
	}

	@Override
	public void popupMenuWillBecomeVisible(JMenuItem menuItem, IntentArguments arguments) {
		Status status = getStatus(arguments);
		if (status != null) {
			if (status.isRetweet()) {
				status = status.getRetweetedStatus();
			}
			User user = status.getUser();

			boolean isTweetedByMe = user.getId() == getLoginUserId();

			String idsString = configuration.getConfigProperties().getProperty("core.filter.user.ids");
			String[] ids = idsString.split(" ");
			String userIdString = String.valueOf(user.getId());
			boolean filtered = false;
			for (String id : ids) {
				if (id.equals(userIdString)) {
					filtered = true;
					break;
				}
			}
			logger.trace("filtered: {}", filtered);
			menuItem.setText("ミュートに追加する");
			menuItem.setToolTipText(filtered ? "すでに追加済みだよ！" : (isTweetedByMe ? "それはあなたなんだからねっ！" : null));
			menuItem.setVisible(!isTweetedByMe);
			menuItem.setEnabled((isTweetedByMe || filtered) == false);
		} else {
			menuItem.setVisible(false);
			menuItem.setEnabled(false);
		}
	}
}

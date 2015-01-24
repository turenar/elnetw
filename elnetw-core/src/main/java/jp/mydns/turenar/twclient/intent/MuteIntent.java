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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import jp.mydns.turenar.twclient.conf.ClientProperties;
import jp.mydns.turenar.twclient.filter.ExtendedMuteFilter;
import jp.mydns.turenar.twclient.filter.GlobalUserIdFilter;
import jp.mydns.turenar.twclient.internal.NullUser;
import twitter4j.Status;
import twitter4j.User;
import twitter4j.UserMentionEntity;

import static jp.mydns.turenar.twclient.i18n.LocalizationResource.tr;

/**
 * ミュートするオプションを提供するアクションハンドラ
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class MuteIntent extends AbstractIntent {
	private static class EntityUser extends NullUser {

		private static final long serialVersionUID = -8212441575399194595L;
		private final String screenName;
		private final String name;
		private final long id;

		public EntityUser(UserMentionEntity entity) {
			screenName = entity.getScreenName();
			name = entity.getName();
			id = entity.getId();
		}

		@Override
		public boolean equals(Object obj) {
			return super.equals(obj); // suppress fb warning
		}

		@Override
		public int hashCode() {
			return (int )id;
		}

		@Override
		public long getId() {
			return id;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public String getScreenName() {
			return screenName;
		}
	}

	private void addMenu(PopupMenuDispatcher dispatcher, IntentArguments arguments, String text,
			boolean isTweetedByMe, boolean filtered) {
		JMenuItem menuItem = new JMenuItem();
		menuItem.setText(text);
		menuItem.setToolTipText(filtered ? tr("Already muted") : (isTweetedByMe ? tr("This is you!") : null));
		menuItem.setEnabled(!(isTweetedByMe || filtered));
		dispatcher.addMenu(menuItem, arguments);
	}

	private boolean checkAlreadyFiltered(String propName, String propValue) {
		boolean filtered;
		List<String> idsList = configuration.getConfigProperties().getList(propName);
		filtered = idsList.contains(propValue);
		return filtered;
	}

	private void createClientJMenu(PopupMenuDispatcher dispatcher, IntentArguments arguments, String source) {
		String text = toMenuText(source);
		String propName = ExtendedMuteFilter.PROPERTY_KEY_FILTER_CLIENT;
		String propValue = getClientName(source);
		boolean filtered = checkAlreadyFiltered(propName, propValue);

		addMenu(dispatcher, getCleanArgument(arguments).putExtra("client", propValue), text, false, filtered);

	}

	@Override
	public void createJMenuItem(PopupMenuDispatcher dispatcher, IntentArguments arguments) {
		boolean auto = getBoolean(arguments, "auto", false)
				|| arguments.getExtraObj(IntentArguments.UNNAMED_ARG, String.class).equals("auto");
		if (auto) {
			PopupMenuDispatcher subDispatcher = dispatcher.createSubMenu(tr("Mute"));
			createJMenuItem(subDispatcher, getCleanArgument(arguments).putExtra(IntentArguments.UNNAMED_ARG, "user"));
			createJMenuItem(subDispatcher, getCleanArgument(arguments).putExtra(IntentArguments.UNNAMED_ARG, "rt_user"));
			if (getStatus(arguments).isRetweet()) {
				createJMenuItem(subDispatcher, getCleanArgument(arguments).putExtra(IntentArguments.UNNAMED_ARG, "rt_mention"));
			} else {
				createJMenuItem(subDispatcher, getCleanArgument(arguments).putExtra(IntentArguments.UNNAMED_ARG, "mention"));
			}
			createJMenuItem(subDispatcher, getCleanArgument(arguments).putExtra(IntentArguments.UNNAMED_ARG, "client"));
			createJMenuItem(subDispatcher, getCleanArgument(arguments).putExtra(IntentArguments.UNNAMED_ARG, "rt_client"));
			createJMenuItem(subDispatcher, getCleanArgument(arguments).putExtra(IntentArguments.UNNAMED_ARG, "text"));
			return;
		}
		if (arguments.getExtraObj("user", User.class) != null) {
			createUserJMenu(dispatcher, arguments, arguments.getExtraObj("user", User.class));
		} else if (arguments.getExtraObj("client", String.class) != null) {
			createClientJMenu(dispatcher, arguments, arguments.getExtraObj("client", String.class));
		} else {
			Status status = getStatus(arguments);
			if (status != null) {
				String arg = arguments.getExtraObj(IntentArguments.UNNAMED_ARG, String.class, "user");
				if (arg.startsWith("rt_")) {
					if (!status.isRetweet()) {
						return;
					}

					status = status.getRetweetedStatus();
					arg = arg.substring("rt_".length());
				}
				switch (arg) {
					case "user":
						createUserJMenu(dispatcher, arguments, status.getUser());
						break;
					case "mention":
						UserMentionEntity[] entities = status.getUserMentionEntities();
						HashSet<Long> set = new HashSet<>();
						if (!(entities == null || entities.length == 0)) {
							for (UserMentionEntity entity : entities) {
								if (set.add(entity.getId())) {
									createUserJMenu(dispatcher, arguments, new EntityUser(entity));
								}
							}
						}
						break;
					case "client":
						createClientJMenu(dispatcher, arguments, status.getSource());
						break;
					case "text":
						createTextJMenu(dispatcher, arguments);
						break;
					default:
						throw new IllegalArgumentException("Unsupported mute type: " + arg);
				}
			}
		}
	}

	private void createTextJMenu(PopupMenuDispatcher dispatcher, IntentArguments arguments) {
		addMenu(dispatcher, getCleanArgument(arguments).putExtra("confirm", "text"), tr("Word..."), false, false);

	}

	private void createUserJMenu(PopupMenuDispatcher dispatcher, IntentArguments arguments, User user) {
		String text = toMenuText(user);
		String propName = GlobalUserIdFilter.PROPERTY_KEY_FILTER_IDS;
		String propValue = String.valueOf(user.getId());
		boolean isTweetedByMe = configuration.isMyAccount(user.getId());
		boolean filtered = checkAlreadyFiltered(propName, propValue);

		addMenu(dispatcher, getCleanArgument(arguments).putExtra("user", user), text, isTweetedByMe, filtered);
	}

	private IntentArguments getCleanArgument(IntentArguments arguments) {
		return arguments.clone().removeExtra(IntentArguments.UNNAMED_ARG).removeExtra("auto");
	}

	private String getClientName(String source) {
		int startTagEnd = source.indexOf('>');
		int endTagStart = source.lastIndexOf('<');
		return startTagEnd < 0 ? source : source.substring(startTagEnd + 1, endTagStart);
	}

	@Override
	public void handleAction(IntentArguments arguments) {
		String text;
		String propName;
		String propValue;
		if (arguments.hasExtraObj("user", User.class)) {
			User user = arguments.getExtraObj("user", User.class);
			text = toMenuText(user);
			propName = GlobalUserIdFilter.PROPERTY_KEY_FILTER_IDS;
			propValue = String.valueOf(user.getId());
		} else if (arguments.hasExtraObj("client", String.class)) {
			String source = arguments.getExtraObj("client", String.class);
			text = toMenuText(source);
			propName = ExtendedMuteFilter.PROPERTY_KEY_FILTER_CLIENT;
			propValue = getClientName(source);
		} else if (arguments.getExtraObj("confirm", String.class).equals("text")) {
			showMuteTextInput();
			return;
		} else {
			throw new IllegalArgumentException("Unsupported argument");
		}
		showConfirmDialog(propName, propValue, text);
	}


	private void showConfirmDialog(final String propName, final String propValue, String text) {
		JPanel panel = new JPanel();
		BoxLayout layout = new BoxLayout(panel, BoxLayout.Y_AXIS);
		panel.setLayout(layout);
		panel.add(new JLabel(tr("Mute below?")));
		panel.add(Box.createVerticalStrut(15));
		panel.add(new JLabel(text));
		final JOptionPane pane =
				new JOptionPane(panel, JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
		JDialog dialog = pane.createDialog(null, tr("Confirm"));
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		pane.addPropertyChangeListener(new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if (evt.getPropertyName().equals(JOptionPane.VALUE_PROPERTY)) {
					if (Integer.valueOf(JOptionPane.OK_OPTION).equals(pane.getValue())) {
						ClientProperties configProperties = configuration.getConfigProperties();
						List<String> idsString = configProperties.getList(propName);
						idsString.add(propValue);
					}
				}
			}
		});
		dialog.setVisible(true);
	}

	private void showMuteTextInput() {
		String message = "";
		String lastRegex = null;
		do {
			String muteRegexStr = (String) JOptionPane.showInputDialog(null,
					tr("Specify muted text as regex.%s", message), tr("Mute"), JOptionPane.QUESTION_MESSAGE,
					null, null, lastRegex);
			try {
				if (muteRegexStr == null) {
					// user cancelled
					return;
				}
				// check compilable
				Pattern pattern = Pattern.compile(muteRegexStr);
				List<String> muteList = configuration.getConfigProperties().getList("core.filter.words");
				muteList.add(pattern.toString());
				return;
			} catch (PatternSyntaxException e) {
				message = "\n\n" + e.getLocalizedMessage();
				lastRegex = muteRegexStr;
			}
		} while (true);
	}

	private String toMenuText(String source) {
		return tr("Client: %s", getClientName(source));
	}

	private String toMenuText(User user) {
		return tr("User: @%s (%s)", user.getScreenName(), user.getName());
	}
}

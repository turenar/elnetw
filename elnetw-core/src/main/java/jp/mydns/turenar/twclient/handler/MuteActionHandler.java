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

package jp.mydns.turenar.twclient.handler;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
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

import jp.mydns.turenar.twclient.ClientProperties;
import jp.mydns.turenar.twclient.filter.ExtendedMuteFilter;
import jp.mydns.turenar.twclient.filter.GlobalUserIdFilter;
import twitter4j.Status;
import twitter4j.User;

/**
 * ミュートするオプションを提供するアクションハンドラ
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class MuteActionHandler extends StatusActionHandlerBase {
	@Override
	public JMenuItem createJMenuItem(IntentArguments arguments) {
		Status status = getStatus(arguments);
		if (status == null) {
			throwIllegalArgument();
		}
		String arg = arguments.getExtraObj("_arg", String.class, "user");
		String text;
		switch (arg) {
			case "user":
				text = toMenuText(status.getUser());
				break;
			case "rt_user":
				text = status.isRetweet() ? toMenuText(status.getRetweetedStatus().getUser()) : null;
				break;
			case "client":
				text = toMenuText(status.getSource());
				break;
			case "rt_client":
				text = status.isRetweet() ? toMenuText(status.getRetweetedStatus().getSource()) : null;
				break;
			case "text":
				text = "ワード...";
				break;
			default:
				throw new IllegalArgumentException("Unsupported mute type: " + arg);
		}
		return text == null ? null : new JMenuItem(text);
	}

	private String getClientName(String source) {
		int startTagEnd = source.indexOf('>');
		int endTagStart = source.lastIndexOf('<');
		return startTagEnd < 0 ? source : source.substring(startTagEnd + 1, endTagStart);
	}

	@Override
	public void handleAction(IntentArguments arguments) {
		Status status = getStatus(arguments);
		if (status == null) {
			throwIllegalArgument();
		}
		String arg = arguments.getExtraObj("_arg", String.class, "user");
		final String propName;
		final String propValue;
		String text;
		switch (arg) {
			case "user":
				text = toMenuText(status.getUser());
				propName = GlobalUserIdFilter.PROPERTY_KEY_FILTER_IDS;
				propValue = String.valueOf(status.getUser().getId());
				break;
			case "rt_user":
				text = status.isRetweet() ? toMenuText(status.getRetweetedStatus().getUser()) : null;
				propName = GlobalUserIdFilter.PROPERTY_KEY_FILTER_IDS;
				propValue = status.isRetweet() ? String.valueOf(status.getRetweetedStatus().getUser().getId()) : null;
				break;
			case "client":
				text = toMenuText(status.getSource());
				propName = ExtendedMuteFilter.PROPERTY_KEY_FILTER_CLIENT;
				propValue = getClientName(status.getSource());
				break;
			case "rt_client":
				text = status.isRetweet() ? toMenuText(status.getRetweetedStatus().getSource()) : null;
				propName = ExtendedMuteFilter.PROPERTY_KEY_FILTER_CLIENT;
				propValue = status.isRetweet() ? getClientName(status.getRetweetedStatus().getSource()) : null;
				break;
			case "text":
				showMuteTextInput();
				return;
			default:
				throw new IllegalArgumentException("Unsupported mute type: " + arg);
		}
		JPanel panel = new JPanel();
		BoxLayout layout = new BoxLayout(panel, BoxLayout.Y_AXIS);
		panel.setLayout(layout);
		panel.add(new JLabel("次をミュートしますか？"));
		panel.add(Box.createVerticalStrut(15));
		panel.add(new JLabel(text));
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
						List<String> idsString = configProperties.getList(propName);
						idsString.add(propValue);
					}
				}
			}
		});
		dialog.setVisible(true);
	}

	@Override
	public void popupMenuWillBecomeVisible(JMenuItem menuItem, IntentArguments arguments) {
		Status status = getStatus(arguments);
		if (status != null) {
			String arg = arguments.getExtraObj("_arg", String.class);
			final String propName;
			final String propValue;
			String text;
			if (arg.startsWith("rt_")) {
				if (!status.isRetweet()) {
					menuItem.setVisible(false);
					return;
				}

				status = status.getRetweetedStatus();
				arg = arg.substring("rt_".length());
			}
			boolean isTweetedByMe = false;
			switch (arg) {
				case "user":
					text = toMenuText(status.getUser());
					propName = GlobalUserIdFilter.PROPERTY_KEY_FILTER_IDS;
					propValue = String.valueOf(status.getUser().getId());
					isTweetedByMe = configuration.isMyAccount(status.getUser().getId());
					break;
				case "client":
					text = toMenuText(status.getSource());
					propName = ExtendedMuteFilter.PROPERTY_KEY_FILTER_CLIENT;
					propValue = getClientName(status.getSource());
					break;
				case "text":
					text = "ワード...";
					propName = null;
					propValue = ""; // stub: this value is not checked at all
					break;
				default:
					throw new IllegalArgumentException("Unsupported mute type: " + arg);
			}

			boolean filtered = false;
			if (propName != null) {
				List<String> idsList = configuration.getConfigProperties().getList(propName);
				filtered = idsList.contains(propValue);
				isTweetedByMe = configuration.isMyAccount(propValue);
			}
			menuItem.setText(text);
			menuItem.setToolTipText(filtered ? "すでに追加済みだよ！" : (isTweetedByMe ? "それはあなたなんだからねっ！" : null));
			menuItem.setVisible(true);
			menuItem.setEnabled(!(isTweetedByMe || filtered));
		} else {
			menuItem.setVisible(false);
			menuItem.setEnabled(false);
		}
	}

	private void showMuteTextInput() {
		String message = "";
		String lastRegex = null;
		do {
			String muteRegexStr = (String) JOptionPane.showInputDialog(null,
					"ミュートするテキストを正規表現で指定してください。" + message, "ミュート", JOptionPane.QUESTION_MESSAGE,
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
		return "クライアント: " + getClientName(source);
	}

	private String toMenuText(User user) {
		return "ユーザー: @" + user.getScreenName() + " (" + user.getName() + ")";
	}
}

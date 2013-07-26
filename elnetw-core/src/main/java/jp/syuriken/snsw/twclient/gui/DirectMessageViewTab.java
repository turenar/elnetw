package jp.syuriken.snsw.twclient.gui;

import java.awt.Color;
import java.awt.event.FocusEvent;
import java.text.MessageFormat;
import java.util.regex.Matcher;

import javax.swing.Icon;
import javax.swing.JLabel;

import com.twitter.Regex;
import jp.syuriken.snsw.twclient.ClientConfiguration;
import jp.syuriken.snsw.twclient.StatusData;
import jp.syuriken.snsw.twclient.StatusPanel;
import jp.syuriken.snsw.twclient.Utility;
import jp.syuriken.snsw.twclient.filter.IllegalSyntaxException;
import twitter4j.DirectMessage;
import twitter4j.User;
import twitter4j.internal.org.json.JSONException;
import twitter4j.internal.org.json.JSONObject;

import static jp.syuriken.snsw.twclient.ClientFrameApi.DO_NOTHING_WHEN_POINTED;

/**
 * ダイレクトメッセージを表示するタブ
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class DirectMessageViewTab extends DefaultClientTab {

	private static final String TAB_ID = "directmessage";

	private static void nl2br(StringBuffer stringBuffer) {
		int start = stringBuffer.length();
		int offset = start;
		int position;
		while ((position = stringBuffer.indexOf("\n", offset)) >= 0) {
			stringBuffer.replace(position, position + 1, "<br>");
			offset = position + 1;
		}
		offset = start;
		while ((position = stringBuffer.indexOf(" ", offset)) >= 0) {
			stringBuffer.replace(position, position + 1, "&nbsp;");
			offset = position + 1;
		}
		offset = start;
		while ((position = stringBuffer.indexOf("&amp;", offset)) >= 0) {
			stringBuffer.replace(position, position + 5, "&amp;amp;");
			offset = position + 9;
		}
	}
	private DefaultRenderer renderer = new DefaultRenderer() {

		@Override
		public void onDirectMessage(twitter4j.DirectMessage directMessage) {
			StatusData statusData = new StatusData(directMessage, directMessage.getCreatedAt());

			if (configProperties.getBoolean(ClientConfiguration.PROPERTY_ID_STRICT_MATCH)) {
				if (directMessage.getSenderId() == frameApi.getLoginUser().getId()) {
					statusData.foregroundColor = Color.BLUE;
				}
			} else {
				if (directMessage.getSenderScreenName().startsWith(frameApi.getLoginUser().getScreenName())) {
					statusData.foregroundColor = Color.BLUE;
				}
			}

			User user = directMessage.getSender();
			JLabel icon = new JLabel();
			imageCacher.setImageIcon(icon, user);
			icon.setHorizontalAlignment(JLabel.CENTER);
			statusData.image = icon;

			String screenName = user.getScreenName();
			statusData.user = screenName;
			if (screenName.length() > 11) {
				screenName = screenName.substring(0, 9) + "..";
			}
			JLabel sentBy = new JLabel(screenName);
			sentBy.setFont(DEFAULT_FONT);
			statusData.sentBy = sentBy;

			JLabel statusText =
					new JLabel("(to @" + directMessage.getRecipientScreenName() + ") " + directMessage.getText());
			statusData.data = statusText;
			statusData.popupMenu = tweetPopupMenu;
			addStatus(statusData);
		}

		@Override
		public void onStatus(twitter4j.Status originalStatus) {
			// do nothing
		}
	};
	private boolean focusGained;
	private boolean isDirty;


	/**
	 * インスタンスを生成する。
	 *
	 * @throws IllegalSyntaxException クエリエラー
	 */
	public DirectMessageViewTab() throws IllegalSyntaxException {
		super();
		configuration.getFetchScheduler().establish("$reader", "my/timeline", getRenderer());
	}

	/**
	 * インスタンスを生成する。
	 *
	 * @param data 保存されたデータ
	 * @throws JSONException JSON例外
	 * @throws IllegalSyntaxException クエリエラー
	 */
	public DirectMessageViewTab(String data) throws JSONException,
			IllegalSyntaxException {
		super(data);
		configuration.getFetchScheduler().establish("$reader", "my/timeline", getRenderer());
	}

	@Override
	public StatusPanel addStatus(StatusData statusData) {
		if (focusGained == false && isDirty == false) {
			isDirty = true;
			configuration.refreshTab(this);
		}
		return super.addStatus(statusData);
	}

	@Override
	protected void focusGainOfLinePanel(FocusEvent e) throws IllegalArgumentException, NumberFormatException {
		if (selectingPost != null) {
			selectingPost.setBackground(selectingPost.getStatusData().backgroundColor);
		}
		selectingPost = (StatusPanel) e.getComponent();
		selectingPost.setBackground(Utility.blendColor(selectingPost.getStatusData().backgroundColor,
				configProperties.getColor(ClientConfiguration.PROPERTY_COLOR_FOCUS_LIST)));

		StatusData statusData = selectingPost.getStatusData();
		if (statusData.tag instanceof DirectMessage) {
			DirectMessage directMessage = (DirectMessage) statusData.tag;
			String text = directMessage.getText();
			StringBuffer oldBuffer = new StringBuffer();
			StringBuffer newBuffer = new StringBuffer(text.length());

			{
				Matcher urlMatcher = Regex.VALID_URL.matcher(text);
				while (urlMatcher.find()) {
					urlMatcher.appendReplacement(newBuffer, "$" + Regex.VALID_URL_GROUP_BEFORE + "<a href='$"
							+ Regex.VALID_URL_GROUP_URL + "'>$" + Regex.VALID_URL_GROUP_URL + "</a>");
				}
				urlMatcher.appendTail(newBuffer);
			}
			{
				StringBuffer tempBuffer = oldBuffer;
				oldBuffer = newBuffer;
				newBuffer = tempBuffer;
				newBuffer.setLength(0);
				Matcher hashtagMatcher = Regex.VALID_HASHTAG.matcher(oldBuffer);
				while (hashtagMatcher.find()) {
					hashtagMatcher.appendReplacement(newBuffer, "$" + Regex.VALID_HASHTAG_GROUP_BEFORE
							+ "<a href='http://command/hashtag!name=$" + Regex.VALID_HASHTAG_GROUP_TAG + "'>$"
							+ Regex.VALID_HASHTAG_GROUP_HASH + "$" + Regex.VALID_HASHTAG_GROUP_TAG + "</a>");
				}
				hashtagMatcher.appendTail(newBuffer);
			}
			{
				StringBuffer tempBuffer = oldBuffer;
				oldBuffer = newBuffer;
				newBuffer = tempBuffer;
				newBuffer.setLength(0);
				Matcher userMatcher = Regex.VALID_MENTION_OR_LIST.matcher(oldBuffer);
				while (userMatcher.find()) {
					String list = userMatcher.group(Regex.VALID_MENTION_OR_LIST_GROUP_LIST);
					if (list == null) {
						userMatcher.appendReplacement(newBuffer, "$" + Regex.VALID_MENTION_OR_LIST_GROUP_BEFORE
								+ "<a href='http://command/userinfo!screenName=$"
								+ Regex.VALID_MENTION_OR_LIST_GROUP_USERNAME + "'>$"
								+ Regex.VALID_MENTION_OR_LIST_GROUP_AT + "$"
								+ Regex.VALID_MENTION_OR_LIST_GROUP_USERNAME + "</a>");
					} else {
						userMatcher.appendReplacement(newBuffer, "$" + Regex.VALID_MENTION_OR_LIST_GROUP_BEFORE
								+ "<a href='http://command/list!user=$"
								+ Regex.VALID_MENTION_OR_LIST_GROUP_USERNAME
								+ ";listName=$" + Regex.VALID_MENTION_OR_LIST_GROUP_LIST + "'>$"
								+ Regex.VALID_MENTION_OR_LIST_GROUP_AT + "$"
								+ Regex.VALID_MENTION_OR_LIST_GROUP_USERNAME + "$"
								+ Regex.VALID_MENTION_OR_LIST_GROUP_LIST + "</a>");
					}
				}
				userMatcher.appendTail(newBuffer);
			}

			nl2br(newBuffer);
			String tweetText = newBuffer.toString();
			String createdBy =
					MessageFormat.format("@{0} ({1}) -> @{2} ({3})", directMessage.getSender().getScreenName(),
							directMessage.getSender().getName(), directMessage.getRecipient().getScreenName(),
							directMessage.getRecipient().getName());
			String createdAt = Utility.getDateString(directMessage.getCreatedAt(), true);
			frameApi.clearTweetView();
			frameApi.setTweetViewText(tweetText, null, DO_NOTHING_WHEN_POINTED);
			frameApi.setTweetViewCreatedAt(createdAt, null, DO_NOTHING_WHEN_POINTED);
			frameApi.setTweetViewCreatedBy(((JLabel) statusData.image).getIcon(), createdBy, null,
					DO_NOTHING_WHEN_POINTED);
		} else {
			// for DisplayRequirements...
			//throw new AssertionError("DirectMessageViewTab must contain only DirectMessage");
		}
	}

	@Override
	public void focusGained() {
		focusGained = true;
		isDirty = false;
		configuration.refreshTab(this);
	}

	@Override
	public void focusLost() {
		focusGained = false;
	}

	@Override
	public DefaultRenderer getActualRenderer() {
		return renderer;
	}

	@Override
	public Icon getIcon() {
		return null; // TODO
	}

	@Override
	protected Object getSerializedExtendedData() {
		return JSONObject.NULL;
	}

	@Override
	public String getTabId() {
		return TAB_ID;
	}

	@Override
	public String getTitle() {
		return isDirty ? "DM*" : "DM";
	}

	@Override
	public String getToolTip() {
		return "DirectMessages";
	}
}

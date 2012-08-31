package jp.syuriken.snsw.twclient;

import java.awt.Color;
import java.awt.event.FocusEvent;
import java.text.MessageFormat;
import java.util.regex.Matcher;

import javax.swing.Icon;
import javax.swing.JLabel;

import com.twitter.Regex;

import twitter4j.DirectMessage;
import twitter4j.User;

/**
 * ダイレクトメッセージを表示するタブ
 * 
 * @author $Author$
 */
public class DirectMessageViewTab extends DefaultClientTab {
	
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
			
			if (configData.mentionIdStrictMatch) {
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
			sentBy.setFont(TwitterClientFrame.DEFAULT_FONT);
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
	 * @param configuration 設定
	 */
	protected DirectMessageViewTab(ClientConfiguration configuration) {
		super(configuration);
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
	public void focusGained() {
		focusGained = true;
		isDirty = false;
		configuration.refreshTab(this);
	}
	
	@Override
	protected void focusGainOfLinePanel(FocusEvent e) throws IllegalArgumentException, NumberFormatException {
		if (selectingPost != null) {
			selectingPost.setBackground(selectingPost.getStatusData().backgroundColor);
		}
		selectingPost = (StatusPanel) e.getComponent();
		selectingPost.setBackground(Utility.blendColor(selectingPost.getStatusData().backgroundColor,
				frameApi.getConfigData().colorOfFocusList));
		
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
				Matcher hashtagMatcher = Regex.AUTO_LINK_HASHTAGS.matcher(oldBuffer);
				while (hashtagMatcher.find()) {
					hashtagMatcher.appendReplacement(newBuffer, "$" + Regex.AUTO_LINK_HASHTAGS_GROUP_BEFORE
							+ "<a href='http://command/hashtag!$" + Regex.AUTO_LINK_HASHTAGS_GROUP_TAG + "'>$"
							+ Regex.AUTO_LINK_HASHTAGS_GROUP_HASH + "$" + Regex.AUTO_LINK_HASHTAGS_GROUP_TAG + "</a>");
				}
				hashtagMatcher.appendTail(newBuffer);
			}
			{
				StringBuffer tempBuffer = oldBuffer;
				oldBuffer = newBuffer;
				newBuffer = tempBuffer;
				newBuffer.setLength(0);
				Matcher userMatcher = Regex.AUTO_LINK_USERNAMES_OR_LISTS.matcher(oldBuffer);
				while (userMatcher.find()) {
					String list = userMatcher.group(Regex.AUTO_LINK_USERNAME_OR_LISTS_GROUP_LIST);
					if (list == null) {
						userMatcher.appendReplacement(newBuffer, "$" + Regex.AUTO_LINK_USERNAME_OR_LISTS_GROUP_BEFORE
								+ "<a href='http://command/userinfo!$"
								+ Regex.AUTO_LINK_USERNAME_OR_LISTS_GROUP_USERNAME + "'>$"
								+ Regex.AUTO_LINK_USERNAME_OR_LISTS_GROUP_AT + "$"
								+ Regex.AUTO_LINK_USERNAME_OR_LISTS_GROUP_USERNAME + "</a>");
					} else {
						userMatcher.appendReplacement(newBuffer, "$" + Regex.AUTO_LINK_USERNAME_OR_LISTS_GROUP_BEFORE
								+ "<a href='http://command/list!$" + Regex.AUTO_LINK_USERNAME_OR_LISTS_GROUP_USERNAME
								+ "$" + Regex.AUTO_LINK_USERNAME_OR_LISTS_GROUP_LIST + "'>$"
								+ Regex.AUTO_LINK_USERNAME_OR_LISTS_GROUP_AT + "$"
								+ Regex.AUTO_LINK_USERNAME_OR_LISTS_GROUP_USERNAME + "$"
								+ Regex.AUTO_LINK_USERNAME_OR_LISTS_GROUP_LIST + "</a>");
					}
				}
				userMatcher.appendTail(newBuffer);
			}
			
			nl2br(newBuffer);
			String tweetText = newBuffer.toString();
			String createdBy =
					MessageFormat.format("@{0} ({1}) -> @{2} ({3})", directMessage.getSenderScreenName(), directMessage
						.getSender().getName(), directMessage.getRecipientScreenName(), directMessage.getRecipient()
						.getName());
			String createdAt = dateFormat.get().format(directMessage.getCreatedAt());
			frameApi.setTweetViewText(tweetText, createdBy, null, createdAt, null,
					((JLabel) statusData.image).getIcon(), null);
		} else {
			throw new AssertionError("DirectMessageViewTab must contain only DirectMessage");
		}
	}
	
	@Override
	public void focusLost() {
		focusGained = false;
	}
	
	@Override
	public Icon getIcon() {
		return null; // TODO
	}
	
	@Override
	public DefaultRenderer getRenderer() {
		return renderer;
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

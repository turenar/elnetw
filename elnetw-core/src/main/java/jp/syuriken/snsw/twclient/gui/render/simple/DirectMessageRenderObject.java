package jp.syuriken.snsw.twclient.gui.render.simple;

import java.awt.Color;
import java.awt.event.FocusEvent;
import java.text.MessageFormat;
import java.util.Date;
import java.util.regex.Matcher;

import javax.swing.JLabel;

import com.twitter.Regex;
import jp.syuriken.snsw.twclient.ClientConfiguration;
import jp.syuriken.snsw.twclient.Utility;
import twitter4j.DirectMessage;

import static jp.syuriken.snsw.twclient.ClientFrameApi.DO_NOTHING_WHEN_POINTED;

/**
 * Render object for direct messages
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class DirectMessageRenderObject extends AbstractRenderObject {
	private final DirectMessage directMessage;

	public DirectMessageRenderObject(SimpleRenderer simpleRenderer, DirectMessage directMessage) {
		super(simpleRenderer);
		this.directMessage = directMessage;
		foregroundColor = Color.CYAN;
		backgroundColor = Color.LIGHT_GRAY;
	}

	@Override
	public void focusGained(FocusEvent e) {
		linePanel.setBackground(Utility.blendColor(backgroundColor,
				getConfigProperties().getColor(ClientConfiguration.PROPERTY_COLOR_FOCUS_LIST)));

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

		getFrameApi().clearTweetView();
		getFrameApi().setTweetViewText(tweetText, null, DO_NOTHING_WHEN_POINTED);
		getFrameApi().setTweetViewCreatedAt(createdAt, null, DO_NOTHING_WHEN_POINTED);
		getFrameApi().setTweetViewCreatedBy(componentUserIcon.getIcon(), createdBy, null,
				DO_NOTHING_WHEN_POINTED);

		super.focusGained(e);
	}

	@Override
	public void focusLost(FocusEvent e) {
		super.focusLost(e);
		linePanel.setBackground(backgroundColor);
	}

	@Override
	public Object getBasedObject() {
		return directMessage;
	}

	@Override
	public String getCreatedBy() {
		return directMessage.getSenderScreenName();
	}

	@Override
	public Date getDate() {
		return directMessage.getCreatedAt();
	}

	@Override
	public String getUniqId() {
		return "dm/" + directMessage.getId();
	}

	@Override
	protected void initComponents() {
		componentUserIcon = new JLabel();
		componentUserIcon.setHorizontalAlignment(JLabel.CENTER);
		renderer.getImageCacher().setImageIcon(componentUserIcon, directMessage.getSender());

		componentSentBy = new JLabel(getShortenString(directMessage.getSenderScreenName(), CREATED_BY_MAX_LEN));
		componentSentBy.setFont(renderer.getDefaultFont());

		componentStatusText = new JLabel(directMessage.getText());
	}
}

package jp.syuriken.snsw.twclient.gui.render.simple;

import java.awt.Color;
import java.awt.event.FocusEvent;
import java.text.MessageFormat;
import java.util.Date;
import java.util.regex.Matcher;

import javax.swing.Icon;
import javax.swing.JLabel;

import com.twitter.Regex;
import jp.syuriken.snsw.twclient.Utility;
import jp.syuriken.snsw.twclient.gui.ImageResource;
import twitter4j.DirectMessage;
import twitter4j.HashtagEntity;
import twitter4j.MediaEntity;
import twitter4j.TweetEntity;
import twitter4j.URLEntity;
import twitter4j.UserMentionEntity;

import static jp.syuriken.snsw.twclient.ClientFrameApi.DO_NOTHING_WHEN_POINTED;
import static jp.syuriken.snsw.twclient.ClientFrameApi.SET_FOREGROUND_COLOR_BLUE;
import static jp.syuriken.snsw.twclient.ClientFrameApi.UNDERLINE;

/**
 * Render object for direct messages
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class DirectMessageRenderObject extends AbstractRenderObject {
	private final DirectMessage directMessage;
	private String uniqId;

	public DirectMessageRenderObject(SimpleRenderer simpleRenderer, DirectMessage directMessage) {
		super(simpleRenderer);
		this.directMessage = directMessage;
		foregroundColor = Color.CYAN;
		backgroundColor = Color.LIGHT_GRAY;
		uniqId = "dm/" + directMessage.getId();
	}

	@Override
	public void focusGained(FocusEvent e) {
		super.focusGained(e);

		String text = directMessage.getText();
		StringBuilder stringBuilder = new StringBuilder(text.length() * 2);

		TweetEntity[] entities = sortEntities(directMessage);
		int offset = 0;
		for (Object entity : entities) {
			int start;
			int end;
			String replaceText;
			String url;
			if (entity instanceof HashtagEntity) {
				HashtagEntity hashtagEntity = (HashtagEntity) entity;
				start = hashtagEntity.getStart();
				end = hashtagEntity.getEnd();
				replaceText = null;
				url = "http://command/hashtag!name=" + hashtagEntity.getText();
			} else if (entity instanceof URLEntity) {
				URLEntity urlEntity = (URLEntity) entity;
				if (urlEntity instanceof MediaEntity) {
					MediaEntity mediaEntity = (MediaEntity) urlEntity;
					url = "http://command/openimg!url=" + mediaEntity.getMediaURL();
				} else {
					url = urlEntity.getURL();
				}
				start = urlEntity.getStart();
				end = urlEntity.getEnd();
				replaceText = urlEntity.getDisplayURL();
			} else if (entity instanceof UserMentionEntity) {
				UserMentionEntity mentionEntity = (UserMentionEntity) entity;
				start = mentionEntity.getStart();
				end = mentionEntity.getEnd();
				replaceText = null;
				url = "http://command/userinfo!screenName=" + mentionEntity.getScreenName();
			} else {
				throw new AssertionError();
			}

			String insertText = "<a href='" + url + "'>"
					+ escapeHTML(replaceText == null ? text.substring(start, end) : replaceText)
					+ "</a>";
			stringBuilder.append(escapeHTML(text.substring(offset, start)));
			stringBuilder.append(insertText);
			offset = end;
		}
		escapeHTML(text.substring(offset), stringBuilder);
		String tweetText = stringBuilder.toString();
		String createdBy;
		createdBy = MessageFormat.format("@{0} ({1}) -> @{2} ({3})", directMessage.getSender().getScreenName(),
				directMessage.getSender().getName(), directMessage.getRecipient().getScreenName(),
				directMessage.getRecipient().getName());
		String createdAt = Utility.getDateString(directMessage.getCreatedAt(), true);
		Icon userProfileIcon = componentUserIcon.getIcon();
		getFrameApi().clearTweetView();
		getFrameApi().setTweetViewCreatedAt(createdAt, null, DO_NOTHING_WHEN_POINTED);
		getFrameApi().setTweetViewCreatedBy(userProfileIcon, createdBy, null, DO_NOTHING_WHEN_POINTED);
		getFrameApi().setTweetViewText(tweetText, null, DO_NOTHING_WHEN_POINTED);
		getFrameApi().setTweetViewOperationPanel(null);
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
		return uniqId;
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

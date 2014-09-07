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

package jp.mydns.turenar.twclient.gui.render.simple;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.MouseEvent;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import jp.mydns.turenar.twclient.ClientConfiguration;
import jp.mydns.turenar.twclient.ClientEventConstants;
import jp.mydns.turenar.twclient.Utility;
import jp.mydns.turenar.twclient.gui.ImageViewerFrame;
import jp.mydns.turenar.twclient.handler.IntentArguments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.DirectMessage;
import twitter4j.User;

import static jp.mydns.turenar.twclient.ClientFrameApi.DO_NOTHING_WHEN_POINTED;
import static jp.mydns.turenar.twclient.ClientFrameApi.SET_CURSOR_HAND;

/**
 * Render object for direct messages
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class DirectMessageRenderObject extends EntitySupportRenderObject {
	private static class IntentActionListener implements ActionListener {
		private static final ClientConfiguration configuration = ClientConfiguration.getInstance();
		private final IntentArguments intentArguments;

		public IntentActionListener(IntentArguments intentArguments) {
			this.intentArguments = intentArguments;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			configuration.handleAction(intentArguments);
		}
	}

	private static final Logger logger = LoggerFactory.getLogger(DirectMessageRenderObject.class);
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
		String tweetText = getTweetViewText(directMessage, directMessage.getText());
		String createdBy = String.format("@%1$s (%2$s) -> @%3$s (%4$s)",
				directMessage.getSender().getScreenName(), directMessage.getSender().getName(),
				directMessage.getRecipient().getScreenName(), directMessage.getRecipient().getName());
		String createdAt = Utility.getDateString(directMessage.getCreatedAt(), true);
		Icon userProfileIcon = componentUserIcon.getIcon();

		getFrameApi().setTweetViewCreatedAt(createdAt, null, DO_NOTHING_WHEN_POINTED);
		getFrameApi().setTweetViewCreatedBy(userProfileIcon, createdBy, null, SET_CURSOR_HAND);
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

	/**
	 * CreatedByをクリックした時に表示されるポップアップメニューの中身を作成
	 *
	 * @param user ユーザー
	 * @return JMenuItem インスタンス
	 */
	protected JMenuItem getNamePopup(User user) {
		JMenuItem senderItem = new JMenuItem(String.format("@%s (%s)", user.getScreenName(), user.getName()));
		senderItem.addActionListener(new IntentActionListener(new IntentArguments("userinfo").putExtra("user", user)));
		return senderItem;
	}

	@Override
	public String getUniqId() {
		return uniqId;
	}

	@Override
	protected void initComponents() {
		componentUserIcon = new JLabel();
		componentUserIcon.setHorizontalAlignment(JLabel.CENTER);
		try {
			renderer.getImageCacher().setImageIcon(componentUserIcon, directMessage.getSender());
		} catch (InterruptedException e) {
			logger.warn("Interrupted", e);
			Thread.currentThread().interrupt();
		}

		componentSentBy = new JLabel(getShortenString(directMessage.getSenderScreenName(), CREATED_BY_MAX_LEN));
		componentSentBy.setFont(renderer.getDefaultFont());

		setStatusTextWithEntities(directMessage, directMessage.getText());
	}

	@Override
	public void onEvent(String name, Object arg) {
		super.onEvent(name, arg);
		switch (name) {
			case ClientEventConstants.EVENT_CLICKED_USERICON:
				try {
					new ImageViewerFrame(new URL(directMessage.getSender().getOriginalProfileImageURLHttps()))
							.setVisible(true);
				} catch (MalformedURLException e) {
					logger.error("failed getting original profile image", e);
				}
				break;
			case ClientEventConstants.EVENT_CLICKED_CREATED_BY:
				if (directMessage.getSenderId() == directMessage.getRecipientId()) {
					getConfiguration().handleAction(
							new IntentArguments("userinfo").putExtra("user", directMessage.getSender()));
				} else {
					JPopupMenu menu = new JPopupMenu();
					menu.add(getNamePopup(directMessage.getSender()));
					menu.add(getNamePopup(directMessage.getRecipient()));
					MouseEvent event = (MouseEvent) arg;
					menu.show(event.getComponent(), event.getX(), event.getY());
				}
				break;
			default:
				// do nothing
		}
	}
}

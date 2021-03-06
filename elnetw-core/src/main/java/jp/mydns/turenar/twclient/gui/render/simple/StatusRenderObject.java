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
import java.awt.Dimension;
import java.awt.datatransfer.StringSelection;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

import javax.swing.GroupLayout;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import jp.mydns.turenar.twclient.ClientConfiguration;
import jp.mydns.turenar.twclient.gui.ImageResource;
import jp.mydns.turenar.twclient.gui.ImageViewerFrame;
import jp.mydns.turenar.twclient.gui.render.RendererManager;
import jp.mydns.turenar.twclient.intent.Intent;
import jp.mydns.turenar.twclient.intent.IntentArguments;
import jp.mydns.turenar.twclient.twitter.TwitterStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.Status;
import twitter4j.URLEntity;
import twitter4j.User;
import twitter4j.UserMentionEntity;

import static jp.mydns.turenar.twclient.ClientFrameApi.SET_CURSOR_HAND;
import static jp.mydns.turenar.twclient.ClientFrameApi.SET_FOREGROUND_COLOR_BLUE;
import static jp.mydns.turenar.twclient.ClientFrameApi.UNDERLINE;

/**
 * Render object for status
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class StatusRenderObject extends EntitySupportRenderObject {
	/**
	 * the size of operation button besides tweet text
	 */
	private static final int OP_BUTTON_SIZE = 32;
	private static final Dimension OPERATION_PANEL_SIZE = new Dimension(OP_BUTTON_SIZE, OP_BUTTON_SIZE);
	private static final Logger logger = LoggerFactory.getLogger(StatusRenderObject.class);
	private static final Dimension OPERATION_GROUP_PANEL_SIZE = new Dimension(76, 76);
	private final long userId;
	private final TwitterStatus status;
	private final String uniqId;
	private JLabel tweetViewRetweetButton;
	private JLabel tweetViewReplyButton;
	private JLabel tweetViewFavoriteButton;
	private JPanel tweetViewOperationPanel;
	private JLabel tweetViewOtherButton;

	/**
	 * インスタンスの生成
	 *
	 * @param userId   ユーザーID
	 * @param status   ステータス
	 * @param renderer レンダラ
	 */
	public StatusRenderObject(long userId, Status status, SimpleRenderer renderer) {
		super(renderer);
		this.userId = userId;
		this.status = status instanceof TwitterStatus ? (TwitterStatus) status : new TwitterStatus(status);
		uniqId = RendererManager.getStatusUniqId(status.getId());
	}

	private void copyToClipboard(String str) {
		StringSelection stringSelection = new StringSelection(str);
		clipboard.setContents(stringSelection, stringSelection);
	}

	/**
	 * ポストパネルがフォーカスを得た時のハンドラ
	 *
	 * @param e Focusイベント
	 * @throws IllegalArgumentException 正しくないプロパティ
	 * @throws NumberFormatException    数値ではないプロパティ
	 */
	protected void focusGainOfLinePanel(FocusEvent e) throws IllegalArgumentException {
		Status originalStatus = status;
		Status status = originalStatus.isRetweet() ? originalStatus.getRetweetedStatus() : originalStatus;
		String text = status.getText();
		String tweetText = getTweetViewText(status, text, status.isPossiblySensitive());
		String createdBy;
		createdBy = getCreatedByLongText(status);
		String createdAtToolTip = getViaString(originalStatus);
		String createdAt = renderer.toDateString(status.getCreatedAt(), true);
		String overlayString;
		if (originalStatus.isRetweet()) {
			overlayString =
					"<html><span style='color:#33cc33;'>Retweeted by @" + originalStatus.getUser().getScreenName()
							+ " (" + originalStatus.getUser().getName() + ")</span>";
		} else {
			overlayString = null;
		}
		if (status.isFavorited()) {
			getTweetViewFavoriteButton().setIcon(ImageResource.getImgFavOn());
		} else {
			getTweetViewFavoriteButton().setIcon(ImageResource.getImgFavOff());
		}
		Icon userProfileIcon = componentUserIcon.getIcon();

		getFrameApi().setTweetViewCreatedAt(createdAt, createdAtToolTip, SET_FOREGROUND_COLOR_BLUE | UNDERLINE);
		getFrameApi().setTweetViewCreatedBy(userProfileIcon, createdBy, null,
				SET_FOREGROUND_COLOR_BLUE | UNDERLINE | SET_CURSOR_HAND);
		getFrameApi().setTweetViewText(tweetText, overlayString, UNDERLINE);
		getFrameApi().setTweetViewOperationPanel(getTweetViewOperationPanel());
	}

	@Override
	public void focusGained(FocusEvent e) {
		super.focusGained(e);
		// should scroll? if focus-window changed, i skip scrolling
		//boolean scroll = (e.getOppositeComponent() == null && selectingPost != null);
		focusGainOfLinePanel(e);
	}

	@Override
	public Object getBasedObject() {
		return status;
	}

	@Override
	public String getCreatedBy() {
		return status.getUser().getScreenName();
	}

	@Override
	public Date getDate() {
		return status.getCreatedAt();
	}

	private User getOriginalUser(TwitterStatus status) {
		return status.isRetweet() ? status.getRetweetedStatus().getUser() : status.getUser();
	}

	@Override
	protected String getPopupMenuType() {
		return "status";
	}

	/**
	 * ツイートビューの隣に表示するふぁぼボタン
	 *
	 * @return ふぁぼボタン
	 */
	protected JLabel getTweetViewFavoriteButton() {
		if (tweetViewFavoriteButton == null) {
			tweetViewFavoriteButton = new JLabel(ImageResource.getImgFavOff(), SwingConstants.CENTER);
			tweetViewFavoriteButton.setBorder(new LineBorder(Color.GRAY, 1));
			tweetViewFavoriteButton.setMinimumSize(OPERATION_PANEL_SIZE);
			tweetViewFavoriteButton.setMaximumSize(OPERATION_PANEL_SIZE);
			tweetViewFavoriteButton.setPreferredSize(OPERATION_PANEL_SIZE);
			tweetViewFavoriteButton.addAncestorListener(new AncestorListener() {

				@Override
				public void ancestorAdded(AncestorEvent event) {
					ancestorMoved(event);
				}

				@Override
				public void ancestorMoved(AncestorEvent event) {
					if (status.isFavorited()) {
						tweetViewFavoriteButton.setIcon(ImageResource.getImgFavOn());
					} else {
						tweetViewFavoriteButton.setIcon(ImageResource.getImgFavOff());
					}
				}

				@Override
				public void ancestorRemoved(AncestorEvent event) {
					ancestorMoved(event);
				}
			});
			tweetViewFavoriteButton.addMouseListener(new MouseAdapter() {

				@Override
				public void mouseClicked(MouseEvent e) {
					updateFavIcon();
					getConfiguration().handleAction(getIntentArguments("fav"));
				}

				@Override
				public void mouseEntered(MouseEvent e) {
					tweetViewFavoriteButton.setIcon(ImageResource.getImgFavHover());
				}

				@Override
				public void mouseExited(MouseEvent e) {
					updateFavIcon();
				}

				private void updateFavIcon() {
					if (status.isFavorited()) {
						tweetViewFavoriteButton.setIcon(ImageResource.getImgFavOn());
					} else {
						tweetViewFavoriteButton.setIcon(ImageResource.getImgFavOff());
					}
				}
			});
		}
		return tweetViewFavoriteButton;
	}

	/**
	 * ツイートビューの隣に表示するボタンの集まり
	 *
	 * @return ボタンの集まり
	 */
	protected JPanel getTweetViewOperationPanel() {
		if (tweetViewOperationPanel == null) {
			tweetViewOperationPanel = new JPanel();
			tweetViewOperationPanel.setPreferredSize(OPERATION_GROUP_PANEL_SIZE);
			tweetViewOperationPanel.setMinimumSize(OPERATION_GROUP_PANEL_SIZE);
			GroupLayout layout = new GroupLayout(tweetViewOperationPanel);
			layout.setHorizontalGroup(layout.createParallelGroup()
					.addGroup(layout.createSequentialGroup()
							.addComponent(getTweetViewReplyButton(), OP_BUTTON_SIZE, OP_BUTTON_SIZE, OP_BUTTON_SIZE)
							.addComponent(getTweetViewRetweetButton(), OP_BUTTON_SIZE, OP_BUTTON_SIZE, OP_BUTTON_SIZE))
					.addGroup(layout.createSequentialGroup()
							.addComponent(getTweetViewFavoriteButton(), OP_BUTTON_SIZE, OP_BUTTON_SIZE, OP_BUTTON_SIZE)
							.addComponent(getTweetViewOtherButton(), OP_BUTTON_SIZE, OP_BUTTON_SIZE, OP_BUTTON_SIZE)));
			layout.setVerticalGroup(layout.createSequentialGroup()
					.addGroup(layout.createParallelGroup()
							.addComponent(getTweetViewReplyButton(), OP_BUTTON_SIZE, OP_BUTTON_SIZE, OP_BUTTON_SIZE)
							.addComponent(getTweetViewRetweetButton(), OP_BUTTON_SIZE, OP_BUTTON_SIZE, OP_BUTTON_SIZE))
					.addGroup(layout.createParallelGroup()
							.addComponent(getTweetViewFavoriteButton(), OP_BUTTON_SIZE, OP_BUTTON_SIZE, OP_BUTTON_SIZE)
							.addComponent(getTweetViewOtherButton(), OP_BUTTON_SIZE, OP_BUTTON_SIZE, OP_BUTTON_SIZE)));
		}
		return tweetViewOperationPanel;
	}

	/**
	 * ツイートビューの隣に表示するその他用ボタン
	 *
	 * @return その他用ボタン
	 */
	protected JLabel getTweetViewOtherButton() {
		if (tweetViewOtherButton == null) {
			tweetViewOtherButton = new JLabel("？", SwingConstants.CENTER);
			tweetViewOtherButton.setBorder(new LineBorder(Color.GRAY, 1));
			tweetViewOtherButton.setToolTipText("ユーザー情報を見る");
			tweetViewOtherButton.setMinimumSize(OPERATION_PANEL_SIZE);
			tweetViewOtherButton.setMaximumSize(OPERATION_PANEL_SIZE);
			tweetViewOtherButton.setPreferredSize(OPERATION_PANEL_SIZE);
			tweetViewOtherButton.addMouseListener(new MouseAdapter() {

				@Override
				public void mouseClicked(MouseEvent e) {
					getConfiguration().handleAction(getIntentArguments("userinfo"));
				}
			});
		}
		return tweetViewOtherButton;
	}

	/**
	 * ツイートビューの隣に表示するリプライボタン
	 *
	 * @return リプライボタン
	 */
	protected JLabel getTweetViewReplyButton() {
		if (tweetViewReplyButton == null) {
			tweetViewReplyButton = new JLabel("Re", SwingConstants.CENTER);
			tweetViewReplyButton.setBorder(new LineBorder(Color.GRAY, 1));
			tweetViewReplyButton.setToolTipText("@返信を行う");
			tweetViewReplyButton.setMinimumSize(OPERATION_PANEL_SIZE);
			tweetViewReplyButton.setMaximumSize(OPERATION_PANEL_SIZE);
			tweetViewReplyButton.setPreferredSize(OPERATION_PANEL_SIZE);
			tweetViewReplyButton.addMouseListener(new MouseAdapter() {

				@Override
				public void mouseClicked(MouseEvent e) {
					getConfiguration().handleAction(getIntentArguments("reply"));
				}
			});
		}
		return tweetViewReplyButton;
	}

	/**
	 * ツイートビューの隣に表示するリツイートボタン
	 *
	 * @return リツイートボタン
	 */
	protected JLabel getTweetViewRetweetButton() {
		if (tweetViewRetweetButton == null) {
			tweetViewRetweetButton = new JLabel("RT", SwingConstants.CENTER);
			tweetViewRetweetButton.setBorder(new LineBorder(Color.GRAY, 1));
			tweetViewRetweetButton.setToolTipText("公式リツイート");
			tweetViewRetweetButton.setMinimumSize(OPERATION_PANEL_SIZE);
			tweetViewRetweetButton.setMaximumSize(OPERATION_PANEL_SIZE);
			tweetViewRetweetButton.setPreferredSize(OPERATION_PANEL_SIZE);
			tweetViewRetweetButton.addMouseListener(new MouseAdapter() {

				@Override
				public void mouseClicked(MouseEvent e) {
					getConfiguration().handleAction(getIntentArguments("rt"));
				}
			});
		}
		return tweetViewRetweetButton;
	}

	@Override
	public String getUniqId() {
		return uniqId;
	}

	private String getViaString(Status status) {
		StringBuilder stringBuilder = new StringBuilder("via ");
		getViaString(status.isRetweet() ? status.getRetweetedStatus() : status, stringBuilder);
		if (status.isRetweet()) {
			getViaString(status, stringBuilder.append(" (Retweeted via "));
			stringBuilder.append(")");
		}
		return stringBuilder.toString();
	}

	private void getViaString(Status status, StringBuilder builder) {
		String source = status.getSource();
		int tagIndexOf = source.indexOf('>');
		int tagLastIndexOf = source.lastIndexOf('<');
		builder.append(source, tagIndexOf + 1, tagLastIndexOf == -1 ? source.length() : tagLastIndexOf);
	}

	private void handleAction(IntentArguments intentArguments) {
		intentArguments.putExtra(Intent.INTENT_ARG_NAME_SELECTING_POST_DATA, this);
		getConfiguration().handleAction(intentArguments);
	}

	@Override
	protected void initComponents() {
		Status twitterStatus = this.status;

		Status status;
		if (twitterStatus.isRetweet()) {
			status = twitterStatus.getRetweetedStatus();
		} else {
			status = twitterStatus;
		}
		User user = status.getUser();

		if (getConfiguration().isMyAccount(user.getId())) {
			foregroundColor = Color.BLUE;
		}

		componentUserIcon = new JLabel();
		try {
			getImageCacher().setImageIcon(componentUserIcon, status.getUser());
		} catch (InterruptedException e) {
			logger.warn("Interrupted", e);
			Thread.currentThread().interrupt();
		}
		componentUserIcon.setHorizontalAlignment(JLabel.CENTER);

		String screenName = user.getScreenName();
		screenName = getShortenString(screenName, CREATED_BY_MAX_LEN);
		componentSentBy = new JLabel(screenName);
		componentSentBy.setFont(renderer.getDefaultFont());

		setStatusTextWithEntities(status, status.getText());

		// in Windows, tooltip keep MouseEvent from being fired.
		// I don't know why. On any other operating systems, is this reproduced?
		/*if(twitterStatus.isRetweet()) {
			componentStatusText.setToolTipText("Retweeted by @" + twitterStatus.getUser().getScreenName());
		}*/

		if (twitterStatus.isRetweet()) {
			foregroundColor = Color.GREEN;
		} else {
			UserMentionEntity[] userMentionEntities = status.getUserMentionEntities();
			if (getConfiguration().isMentioned(userId, userMentionEntities)) {
				foregroundColor = Color.RED;
			}
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		super.mouseClicked(e);
			/* if (e.isPopupTrigger()) {
				selectingPost = (RenderPanel) e.getComponent();
			} */
		if (e.getClickCount() == 2) {
			getConfiguration().handleAction(getIntentArguments("reply"));
		}
	}

	@Override
	public void onEvent(String name, Object arg) {
		super.onEvent(name, arg);
		switch (name) {
			case REQUEST_COPY:
				copyToClipboard("@" + status.getUser().getScreenName() + ": " + status.getText());
				break;
			case REQUEST_COPY_URL: {
				Status status = this.status.isRetweet() ? this.status.getRetweetedStatus() : this.status;
				String url = "https://twitter.com/" + status.getUser().getScreenName() + "/status/"
						+ status.getId();
				copyToClipboard(url);
			}
			break;
			case REQUEST_COPY_USERID:
				copyToClipboard(status.getUser().getScreenName());
				break;
			case REQUEST_BROWSER_USER_HOME: {
				Status status = this.status.isRetweet() ? this.status.getRetweetedStatus() : this.status;
				String url = "https://twitter.com/" + status.getUser().getScreenName();
				copyToClipboard(url);
			}
			break;
			case REQUEST_BROWSER_STATUS:
			case REQUEST_BROWSER_PERMALINK:
			case EVENT_CLICKED_CREATED_AT: {
				Status status = this.status.isRetweet() ? this.status.getRetweetedStatus() : this.status;
				String url = "https://twitter.com/" + status.getUser().getScreenName() + "/status/"
						+ status.getId();
				openBrowser(url);
			}
			break;
			case REQUEST_BROWSER_IN_REPLY_TO:
				if (status.getInReplyToStatusId() != -1) {
					String url = "http://twitter.com/" + status.getInReplyToScreenName() + "/status/"
							+ status.getInReplyToStatusId();
					openBrowser(url);
				}
				break;
			case REQUEST_BROWSER_OPENURLS: {
				URLEntity[] urlEntities = status.getURLEntities();
				for (URLEntity urlEntity : urlEntities) {
					openBrowser(urlEntity.getURL());
				}
			}
			break;
			case EVENT_CLICKED_CREATED_BY: {
				Status status = this.status.isRetweet() ? this.status.getRetweetedStatus() : this.status;
				if (status.isRetweet()) {
					status = status.getRetweetedStatus();
				}
				handleAction(new IntentArguments("userinfo").putExtra("user", status.getUser()));
			}
			break;
			case EVENT_CLICKED_OVERLAY_LABEL:
				if (status.isRetweet()) {
					handleAction(new IntentArguments("userinfo").putExtra("user", status.getUser()));
				}
				break;
			case EVENT_CLICKED_USERICON:
				try {
					new ImageViewerFrame(status.isPossiblySensitive(),
							new URL(getOriginalUser(status).getOriginalProfileImageURLHttps()),
							new URL(getOriginalUser(status).getOriginalProfileImageURL()))
							.setVisible(true);
				} catch (MalformedURLException e) {
					logger.error("failed getting original profile image", e);
				}
				break;
			default:
				// do nothing
		}
	}

	private void openBrowser(String url) {
		try {
			ClientConfiguration.getInstance().getUtility().openBrowser(url);
		} catch (Exception e) {
			renderer.onException(e);
		}
	}

	@Override
	public String toString() {
		return "StatusRenderObject{statusId=" + status.getId() + "}";
	}
}

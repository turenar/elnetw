package jp.syuriken.snsw.twclient.gui.render.simple;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.datatransfer.StringSelection;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;

import javax.swing.GroupLayout;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import jp.syuriken.snsw.twclient.ActionHandler;
import jp.syuriken.snsw.twclient.ClientConfiguration;
import jp.syuriken.snsw.twclient.TwitterStatus;
import jp.syuriken.snsw.twclient.Utility;
import jp.syuriken.snsw.twclient.gui.ImageResource;
import jp.syuriken.snsw.twclient.gui.render.RendererManager;
import jp.syuriken.snsw.twclient.handler.IntentArguments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.HashtagEntity;
import twitter4j.MediaEntity;
import twitter4j.Status;
import twitter4j.TweetEntity;
import twitter4j.URLEntity;
import twitter4j.User;
import twitter4j.UserMentionEntity;

import static jp.syuriken.snsw.twclient.ClientFrameApi.SET_FOREGROUND_COLOR_BLUE;
import static jp.syuriken.snsw.twclient.ClientFrameApi.UNDERLINE;

/**
 * Render object for status
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class StatusRenderObject extends AbstractRenderObject {
	/** Entityの開始位置を比較する */
	private static final class EntityComparator implements Comparator<TweetEntity>, Serializable {
		private static final long serialVersionUID = -3995117038146393663L;

		@Override
		public int compare(TweetEntity o1, TweetEntity o2) {
			return o1.getStart() - o2.getStart();
		}
	}

	private static final Logger logger = LoggerFactory.getLogger(StatusRenderObject.class);
	private static final Dimension OPERATION_PANEL_SIZE = new Dimension(32, 32);
	private final long userId;
	private final Status status;
	private final String uniqId;
	private String tooltip;
	private JLabel tweetViewRetweetButton;
	private JLabel tweetViewReplyButton;
	private JLabel tweetViewFavoriteButton;
	private JPanel tweetViewOperationPanel;
	private JLabel tweetViewOtherButton;

	public StatusRenderObject(long userId, Status status, SimpleRenderer renderer) {
		super(renderer);
		this.userId = userId;
		this.status = status;
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
		StringBuilder stringBuilder = new StringBuilder(text.length() * 2);

		TweetEntity[] entities = sortEntities(status);
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
		createdBy = getCreatedByLongText(status);
		String source = status.getSource();
		int tagIndexOf = source.indexOf('>');
		int tagLastIndexOf = source.lastIndexOf('<');
		String createdAtToolTip =
				MessageFormat.format("from {0}",
						source.substring(tagIndexOf + 1, tagLastIndexOf == -1 ? source.length() : tagLastIndexOf));
		String createdAt = Utility.getDateString(status.getCreatedAt(), true);
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
		getFrameApi().clearTweetView();
		getFrameApi().setTweetViewCreatedAt(createdAt, createdAtToolTip, SET_FOREGROUND_COLOR_BLUE | UNDERLINE);
		getFrameApi().setTweetViewCreatedBy(userProfileIcon, createdBy, null, SET_FOREGROUND_COLOR_BLUE | UNDERLINE);
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
			tweetViewOperationPanel.setPreferredSize(new Dimension(76, 76));
			tweetViewOperationPanel.setMinimumSize(new Dimension(76, 76));
			GroupLayout layout = new GroupLayout(tweetViewOperationPanel);
			layout.setHorizontalGroup(layout
					.createParallelGroup()
					.addGroup(
							layout.createSequentialGroup().addComponent(getTweetViewReplyButton(), 32, 32, 32)
									.addComponent(getTweetViewRetweetButton(), 32, 32, 32))
					.addGroup(
							layout.createSequentialGroup().addComponent(getTweetViewFavoriteButton(), 32, 32, 32)
									.addComponent(getTweetViewOtherButton(), 32, 32, 32)));
			layout.setVerticalGroup(layout
					.createSequentialGroup()
					.addGroup(
							layout.createParallelGroup().addComponent(getTweetViewReplyButton(), 32, 32, 32)
									.addComponent(getTweetViewRetweetButton(), 32, 32, 32))
					.addGroup(
							layout.createParallelGroup().addComponent(getTweetViewFavoriteButton(), 32, 32, 32)
									.addComponent(getTweetViewOtherButton(), 32, 32, 32)));
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

	private void handleAction(IntentArguments intentArguments) {
		intentArguments.putExtra(ActionHandler.INTENT_ARG_NAME_SELECTING_POST_DATA, this);
		getConfiguration().handleAction(intentArguments);
	}

	protected void initComponents() {
		Status twitterStatus = status instanceof TwitterStatus ? status
				: new TwitterStatus(status);

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
		getImageCacher().setImageIcon(componentUserIcon, status.getUser());
		componentUserIcon.setHorizontalAlignment(JLabel.CENTER);

		String screenName = user.getScreenName();
		screenName = getShortenString(screenName, CREATED_BY_MAX_LEN);
		componentSentBy = new JLabel(screenName);
		componentSentBy.setFont(renderer.getDefaultFont());

		componentStatusText = new JLabel(status.getText());

		if (twitterStatus.isRetweet()) {
			tooltip = "Retweeted by @" + twitterStatus.getUser().getScreenName();
		}

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
	public void keyPressed(KeyEvent e) {
		logger.trace("{}", e);
		getFrameApi().handleShortcutKey("list", e);
	}

	@Override
	public void keyReleased(KeyEvent e) {
		getFrameApi().handleShortcutKey("list", e);
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
		switch (name) {
			case REQUEST_COPY:
				copyToClipboard("@" + status.getUser().getScreenName() + ": " + status.getText());
				break;
			case REQUEST_COPY_URL: {
				Status status = this.status.isRetweet() ? this.status.getRetweetedStatus() : this.status;
				String url = "https://twitter.com/" + status.getUser().getScreenName() + "/status/" +
						status.getId();
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
				String url = "https://twitter.com/" + status.getUser().getScreenName() + "/status/" +
						status.getId();
				ClientConfiguration.getInstance().getUtility().openBrowser(url);
			}
			break;
			case REQUEST_BROWSER_IN_REPLY_TO:
				if (status.getInReplyToStatusId() != -1) {
					String url = "http://twitter.com/" + status.getInReplyToScreenName() + "/status/"
							+ status.getInReplyToStatusId();
					ClientConfiguration.getInstance().getUtility().openBrowser(url);
				}
				break;
			case REQUEST_BROWSER_OPENURLS: {
				URLEntity[] urlEntities = status.getURLEntities();
				for (URLEntity urlEntity : urlEntities) {
					ClientConfiguration.getInstance().getUtility().openBrowser(urlEntity.getURL());
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
		}
	}

	private TweetEntity[] sortEntities(Status status) {
		int entitiesLen;
		HashtagEntity[] hashtagEntities = status.getHashtagEntities();
		entitiesLen = hashtagEntities == null ? 0 : hashtagEntities.length;
		URLEntity[] urlEntities = status.getURLEntities();
		entitiesLen += urlEntities == null ? 0 : urlEntities.length;
		MediaEntity[] mediaEntities = status.getMediaEntities();
		entitiesLen += mediaEntities == null ? 0 : mediaEntities.length;
		UserMentionEntity[] mentionEntities = status.getUserMentionEntities();
		entitiesLen += mentionEntities == null ? 0 : mentionEntities.length;
		TweetEntity[] entities = new TweetEntity[entitiesLen];

		if (entitiesLen != 0) {
			int copyOffset = 0;
			if (hashtagEntities != null) {
				System.arraycopy(hashtagEntities, 0, entities, copyOffset, hashtagEntities.length);
				copyOffset += hashtagEntities.length;
			}
			if (urlEntities != null) {
				System.arraycopy(urlEntities, 0, entities, copyOffset, urlEntities.length);
				copyOffset += urlEntities.length;
			}
			if (mediaEntities != null) {
				System.arraycopy(mediaEntities, 0, entities, copyOffset, mediaEntities.length);
				copyOffset += mediaEntities.length;
			}
			if (mentionEntities != null) {
				System.arraycopy(mentionEntities, 0, entities, copyOffset, mentionEntities.length);
			}
		}
		Arrays.sort(entities, new EntityComparator());
		return entities;
	}
}

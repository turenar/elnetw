package jp.syuriken.snsw.twclient;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FontMetrics;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.Locale;
import java.util.TimerTask;
import java.util.TreeMap;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import jp.syuriken.snsw.twclient.TwitterClientFrame.ConfigData;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import twitter4j.DirectMessage;
import twitter4j.HashtagEntity;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.URLEntity;
import twitter4j.User;
import twitter4j.UserList;
import twitter4j.UserMentionEntity;
import twitter4j.internal.http.HTMLEntity;

/**
 * ツイート表示用のタブ
 * 
 * @author $Author$
 */
public abstract class DefaultClientTab implements ClientTab {
	
	/**
	 * レンダラ。このクラスをextendして {@link DefaultClientTab#getRenderer()} でインスタンスを返すことにより
	 * 自由にフィルタしながら使うことができます。
	 * 
	 * @author $Author$
	 */
	public class DefaultRenderer implements TabRenderer {
		
		private final Logger logger = LoggerFactory.getLogger(getClass());
		
		
		private int getInfoSurviveTime() {
			return frameApi.getInfoSurviveTime();
		}
		
		@Override
		public void onBlock(User source, User blockedUser) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onChangeAccount(boolean forWrite) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onCleanUp() {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onClientMessage(String name, Object arg) {
			if (Utility.equalString(name, REQUEST_FOCUS_TAB_COMPONENT)) {
				if (selectingPost == null) {
					getTabComponent().getViewport().getView().requestFocusInWindow();
				} else {
					selectingPost.requestFocusInWindow();
				}
			} else if (Utility.equalString(name, REQUEST_FOCUS_NEXT_COMPONENT)) {
				if (selectingPost == null) {
					getTabComponent().getViewport().getView().requestFocusInWindow();
				} else {
					getChildComponent().requestFocusNextOf(selectingPost);
				}
			} else if (Utility.equalString(name, REQUEST_FOCUS_PREV_COMPONENT)) {
				if (selectingPost == null) {
					getTabComponent().getViewport().getView().requestFocusInWindow();
				} else {
					getChildComponent().requestFocusPreviousOf(selectingPost);
				}
			}
		}
		
		@Override
		public void onConnect() {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onDeletionNotice(long directMessageId, long userId) {
			// TODO DM Deletion is not supported yet.
		}
		
		@Override
		public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
			logger.trace("onDeletionNotice: {}", statusDeletionNotice);
			
			StatusData statusData = getStatus(statusDeletionNotice.getStatusId());
			if (statusData != null) {
				if (statusData.tag instanceof Status == false) {
					return;
				}
				Status status = (Status) statusData.tag;
				StatusData deletionStatusData = new StatusData(statusDeletionNotice, new Date());
				deletionStatusData.backgroundColor = Color.LIGHT_GRAY;
				deletionStatusData.foregroundColor = Color.RED;
				deletionStatusData.image = new JLabel();
				deletionStatusData.sentBy = new JLabel(((JLabel) (statusData.sentBy)).getText()); // TODO
				deletionStatusData.sentBy.setName("!twdel." + statusDeletionNotice.getUserId());
				deletionStatusData.data = new JLabel("DELETED: " + status.getText());
				addStatus(deletionStatusData, getInfoSurviveTime() * 2);
				removeStatus(statusData, getInfoSurviveTime() * 2);
			}
		}
		
		@Override
		public void onDirectMessage(DirectMessage directMessage) {
			logger.trace("onDirectMessage: {}", directMessage);
			
			StatusData statusData = new StatusData(directMessage, directMessage.getCreatedAt());
			statusData.backgroundColor = Color.LIGHT_GRAY;
			statusData.foregroundColor = Color.CYAN;
			statusData.image = new JLabel();
			statusData.sentBy = new JLabel(directMessage.getSenderScreenName());
			statusData.sentBy.setName("!dm." + directMessage.getSenderScreenName());
			String message = MessageFormat.format("DMを受信しました: \"{0}\"", directMessage.getText());
			statusData.data = new JLabel(message);
			addStatus(statusData);
			User sender = directMessage.getSender();
			configuration
				.getFrameApi()
				.getUtility()
				.sendNotify(MessageFormat.format("{0} ({1})", sender.getScreenName(), sender.getName()), message,
						imageCacher.getImageFile(sender));
		}
		
		@Override
		public void onDisconnect() {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onException(Exception ex) {
			logger.warn("onException:", ex);
		}
		
		@Override
		public void onFavorite(User source, User target, Status favoritedStatus) {
			logger.trace("onFavorite: {}", favoritedStatus);
			
			if (target.getId() == frameApi.getLoginUser().getId()) {
				StatusData statusData = new StatusData(favoritedStatus, new Date());
				statusData.backgroundColor = Color.GRAY;
				statusData.foregroundColor = Color.YELLOW;
				statusData.image = new JLabel(new ImageIcon(source.getProfileImageURL()));
				statusData.sentBy = new JLabel(source.getScreenName());
				statusData.sentBy.setName("!fav." + source.getScreenName());
				String message = MessageFormat.format("ふぁぼられました: \"{0}\"", favoritedStatus.getText());
				statusData.data = new JLabel(message);
				addStatus(statusData);
				configuration.getUtility().sendNotify(
						MessageFormat.format("{0} ({1})", source.getScreenName(), source.getName()), message,
						imageCacher.getImageFile(source));
			}
			if (source.getId() == frameApi.getLoginUser().getId()) {
				StatusData statusData = getStatus(favoritedStatus.getId());
				if (statusData.tag instanceof TwitterStatus) {
					TwitterStatus status = (TwitterStatus) statusData.tag;
					status.setFavorited(true);
				}
			}
		}
		
		@Override
		public void onFollow(User source, User followedUser) {
			logger.trace("onFollow: {} {}", source, followedUser);
			if (followedUser.getId() == frameApi.getLoginUser().getId()) {
				StatusData statusData = new StatusData(null, new Date());
				statusData.backgroundColor = Color.GRAY;
				statusData.foregroundColor = Color.YELLOW;
				statusData.image = new JLabel(new ImageIcon(source.getProfileImageURL()));
				statusData.sentBy = new JLabel(source.getScreenName());
				statusData.sentBy.setName("!follow." + source.getScreenName());
				String message = "フォローされました: " + followedUser.getScreenName();
				statusData.data = new JLabel(message);
				addStatus(statusData);
				configuration.getUtility().sendNotify(
						MessageFormat.format("{0} ({1})", source.getScreenName(), source.getName()), message,
						imageCacher.getImageFile(source));
			}
		}
		
		@Override
		public void onFriendList(long[] friendIds) {
			if (logger.isTraceEnabled()) {
				logger.trace("onFriendList: count={}, {}", friendIds.length, Arrays.toString(friendIds));
			}
		}
		
		@Override
		public void onRetweet(User source, User target, Status retweetedStatus) {
			if (logger.isTraceEnabled()) {
				logger.trace("onRetweet: source={}, target={}, retweet={}",
						Utility.toArray(source, target, retweetedStatus));
			}
			
			if (logger.isDebugEnabled()) {
				logger.debug("id={}, retweetedid={}, status={}", Utility.toArray(retweetedStatus.getId(),
						retweetedStatus.getRetweetedStatus().getId(), retweetedStatus));
			}
			addStatus(retweetedStatus);
		}
		
		@Override
		public void onScrubGeo(long userId, long upToStatusId) {
			// TODO Auto-generated method stub
		}
		
		@Override
		public void onStatus(Status originalStatus) {
			addStatus(originalStatus);
		}
		
		@Override
		public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
			logger.trace("onTrackLimitationNotice: {}", numberOfLimitedStatuses);
			StatusData statusData = new StatusData(null, new Date());
			statusData.backgroundColor = Color.BLACK;
			statusData.foregroundColor = Color.LIGHT_GRAY;
			statusData.image = new JLabel();
			statusData.sentBy = new JLabel();
			statusData.sentBy.setName("!stream.overlimit");
			statusData.data =
					new JLabel("TwitterStreamは " + numberOfLimitedStatuses + " ツイート数をスキップしました： TrackLimitationNotice");
			addStatus(statusData, getInfoSurviveTime() * 2);
		}
		
		@Override
		public void onUnblock(User source, User unblockedUser) {
			// TODO Auto-generated method stub
		}
		
		@Override
		public void onUnfavorite(User source, User target, Status unfavoritedStatus) {
			if (logger.isTraceEnabled()) {
				logger.trace("onUnFavorite: source={}, target={}, unfavoritedStatus={}",
						Utility.toArray(source, target, unfavoritedStatus));
			}
			if (target.getId() == frameApi.getLoginUser().getId()) {
				StatusData statusData = new StatusData(unfavoritedStatus, new Date());
				statusData.backgroundColor = Color.GRAY;
				statusData.foregroundColor = Color.LIGHT_GRAY;
				statusData.image = new JLabel(new ImageIcon(source.getProfileImageURL()));
				statusData.sentBy = new JLabel(source.getScreenName());
				statusData.sentBy.setName("!unfav." + source.getScreenName());
				String message = "ふぁぼやめられました: \"" + unfavoritedStatus.getText() + "\"";
				statusData.data = new JLabel(message);
				addStatus(statusData);
				configuration
					.getFrameApi()
					.getUtility()
					.sendNotify(MessageFormat.format("{0} ({1})", source.getScreenName(), source.getName()), message,
							imageCacher.getImageFile(source));
			}
			if (source.getId() == frameApi.getLoginUser().getId()) {
				StatusData statusData = getStatus(unfavoritedStatus.getId());
				if (statusData.tag instanceof TwitterStatus) {
					TwitterStatus status = (TwitterStatus) statusData.tag;
					status.setFavorited(false);
				}
			}
		}
		
		@Override
		public void onUserListCreation(User listOwner, UserList list) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onUserListDeletion(User listOwner, UserList list) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onUserListMemberAddition(User addedMember, User listOwner, UserList list) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onUserListMemberDeletion(User deletedMember, User listOwner, UserList list) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onUserListSubscription(User subscriber, User listOwner, UserList list) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onUserListUnsubscription(User subscriber, User listOwner, UserList list) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onUserListUpdate(User listOwner, UserList list) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onUserProfileUpdate(User updatedUser) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	/**
	 * ポストリストリスナ。
	 * 
	 * @author $Author$
	 */
	protected class PostListListener implements MouseListener, FocusListener, KeyListener {
		
		@Override
		public void focusGained(FocusEvent e) {
			focusGainOfLinePanel(e);
		}
		
		@Override
		public void focusLost(FocusEvent e) {
		}
		
		@Override
		public void keyPressed(KeyEvent e) {
		}
		
		@Override
		public void keyReleased(KeyEvent e) {
			String keyString = Utility.toKeyString(e.getKeyCode(), e.getModifiersEx());
			String actionCommandName = frameApi.getActionCommandByShortcutKey(keyString);
			if (actionCommandName != null) {
				frameApi.handleAction(actionCommandName, selectingPost.getStatusData());
				e.consume();
			}
		}
		
		@Override
		public void keyTyped(KeyEvent e) {
		}
		
		@Override
		public void mouseClicked(MouseEvent e) {
			e.getComponent().requestFocusInWindow();
			/* if (e.isPopupTrigger()) {
				selectingPost = (StatusPanel) e.getComponent();
			} */
			if (e.getClickCount() == 2) {
				StatusPanel panel = ((StatusPanel) e.getComponent());
				frameApi.handleAction("reply", panel.getStatusData());
			}
		}
		
		@Override
		public void mouseEntered(MouseEvent e) {
		}
		
		@Override
		public void mouseExited(MouseEvent e) {
		}
		
		@Override
		public void mousePressed(MouseEvent e) {
		}
		
		@Override
		public void mouseReleased(MouseEvent e) {
		}
		
	}
	
	/**
	 * StatusPanelのポップアップメニューリスナ
	 * 
	 * @author $Author$
	 */
	protected class TweetPopupMenuListener implements PopupMenuListener, ActionListener {
		
		@Override
		public void actionPerformed(ActionEvent e) {
			StatusData statusData;
			if (selectingPost == null) {
				statusData = null;
			} else {
				statusData = selectingPost.getStatusData();
			}
			frameApi.handleAction(e.getActionCommand(), statusData);
		}
		
		@Override
		public void popupMenuCanceled(PopupMenuEvent arg0) {
		}
		
		@Override
		public void popupMenuWillBecomeInvisible(PopupMenuEvent arg0) {
		}
		
		@Override
		public void popupMenuWillBecomeVisible(PopupMenuEvent arg0) {
			if (selectingPost == null) {
				getChildComponent().requestFocusFirstComponent();
			}
			JPopupMenu popupMenu = (JPopupMenu) arg0.getSource();
			Component[] components = popupMenu.getComponents();
			for (Component component : components) {
				JMenuItem menuItem = (JMenuItem) component;
				StatusData statusData = selectingPost.getStatusData();
				if (statusData == null) {
					menuItem.setEnabled(false);
				} else {
					ActionHandler actionHandler = frameApi.getActionHandler(menuItem.getActionCommand());
					if (actionHandler != null) {
						actionHandler.popupMenuWillBecomeVisible(menuItem, statusData, frameApi);
					} else {
						logger.warn("ActionHandler is not found: {}", menuItem.getActionCommand());
						menuItem.setEnabled(false);
					}
				}
			}
		}
		
	}
	
	/**
	 * PostListを更新する。
	 * 
	 * @author $Author$
	 */
	public class UpdatePostList extends TimerTask {
		
		@Override
		public void run() {
			EventQueue.invokeLater(new Runnable() {
				
				@Override
				public void run() {
					synchronized (postListAddQueue) {
						int size = postListAddQueue.size();
						
						sortedPostListPanel.add(postListAddQueue);
						Point viewPosition = postListScrollPane.getViewport().getViewPosition();
						if (viewPosition.y < fontHeight) {
							postListScrollPane.getViewport().setViewPosition(new Point(viewPosition.x, 0));
						} else {
							postListScrollPane.getViewport().setViewPosition(
									new Point(viewPosition.x, viewPosition.y + (fontHeight + 3) * size));
						}
					}
				}
			});
		}
	}
	
	
	/** DateFormatを管理する */
	protected static ThreadLocal<SimpleDateFormat> dateFormat = new ThreadLocal<SimpleDateFormat>() {
		
		@Override
		protected SimpleDateFormat initialValue() {
			return new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
		}
	};
	
	/** {@link ClientConfiguration#getFrameApi()} */
	protected final ClientFrameApi frameApi;
	
	/** 現在選択しているポスト */
	public StatusPanel selectingPost;
	
	/** SortedPostListPanelインスタンス */
	protected final SortedPostListPanel sortedPostListPanel;
	
	/** 設定 */
	protected final ClientConfiguration configuration;
	
	/** {@link ClientConfiguration#getImageCacher()} */
	protected final ImageCacher imageCacher;
	
	/** 取得したフォントメトリックス (Default Font) */
	protected FontMetrics fontMetrics;
	
	/** フォントの高さ */
	protected int fontHeight;
	
	private Dimension linePanelSizeOfSentBy;
	
	/** アイコンを表示するときのサイズ */
	protected Dimension iconSize;
	
	/** ポストリストリスナのシングルインスタンス */
	protected final PostListListener postListListenerSingleton = new PostListListener();
	
	/** <K=ユーザーID, V=ユーザーのツイートなど> */
	protected TreeMap<String, ArrayList<StatusData>> listItems = new TreeMap<String, ArrayList<StatusData>>();
	
	/** <K=ステータスID, V=ツイートなど> */
	protected TreeMap<Long, StatusData> statusMap = new TreeMap<Long, StatusData>();
	
	/** スクロールペーン */
	protected JScrollPane postListScrollPane;
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	private final ConfigData configData;
	
	private LinkedList<StatusPanel> postListAddQueue = new LinkedList<StatusPanel>();
	
	private JPopupMenu tweetPopupMenu;
	
	
	/**
	 * インスタンスを生成する。
	 * 
	 * @param configuration 設定
	 */
	protected DefaultClientTab(ClientConfiguration configuration) {
		this.configuration = configuration;
		imageCacher = configuration.getImageCacher();
		sortedPostListPanel = new SortedPostListPanel();
		frameApi = configuration.getFrameApi();
		configData = frameApi.getConfigData();
		fontMetrics = getTabComponent().getFontMetrics(frameApi.getDefaultFont());
		int str12width = fontMetrics.stringWidth("0123456789abc");
		fontHeight = fontMetrics.getHeight();
		linePanelSizeOfSentBy = new Dimension(str12width, fontHeight);
		iconSize = new Dimension(64, fontHeight);
		frameApi.getTimer().schedule(new UpdatePostList(), configData.intervalOfPostListUpdate,
				configData.intervalOfPostListUpdate);
		tweetPopupMenu = ((TwitterClientFrame) (frameApi)).generatePopupMenu(new TweetPopupMenuListener());
		tweetPopupMenu.addPopupMenuListener(new TweetPopupMenuListener());
	}
	
	/**
	 * リストにステータスを追加する。
	 * 
	 * @param originalStatus 元となるStatus
	 */
	public void addStatus(Status originalStatus) {
		Status twitterStatus = new TwitterStatus(originalStatus);
		StatusData statusData = new StatusData(twitterStatus, originalStatus.getCreatedAt(), originalStatus.getId());
		
		Status status;
		if (originalStatus.isRetweet()) {
			status = originalStatus.getRetweetedStatus();
		} else {
			status = originalStatus;
		}
		User user = status.getUser();
		
		// TODO 
		/* if (configProperties.getBoolean("client.main.match.id_strict_match")) {
			if (user.getId() == getLoginUser().getId()) {
				statusData.foregroundColor = Color.BLUE;
			}
		} else { */
		if (user.getScreenName().startsWith(frameApi.getLoginUser().getScreenName())) {
			statusData.foregroundColor = Color.BLUE;
		}
		/* } */
		
		JLabel icon = new JLabel();
		imageCacher.setImageIcon(icon, status.getUser());
		icon.setHorizontalAlignment(JLabel.CENTER);
		statusData.image = icon;
		
		String screenName = user.getScreenName();
		if (screenName.length() > 11) {
			screenName = screenName.substring(0, 9) + "..";
		}
		JLabel sentBy = new JLabel(screenName);
		sentBy.setName(user.getScreenName());
		sentBy.setFont(TwitterClientFrame.DEFAULT_FONT);
		statusData.sentBy = sentBy;
		
		JLabel statusText = new JLabel(status.getText());
		statusData.data = statusText;
		
		statusData.popupMenu = tweetPopupMenu;
		
		if (originalStatus.isRetweet()) {
			StringBuilder stringBuilder = new StringBuilder();
			stringBuilder.append("Retweeted by @").append(originalStatus.getUser().getScreenName());
			statusData.tooltip = stringBuilder.toString();
		}
		
		if (originalStatus.isRetweet()) {
			statusData.foregroundColor = Color.GREEN;
		} else {
			UserMentionEntity[] userMentionEntities = status.getUserMentionEntities();
			if (isMentioned(userMentionEntities)) {
				statusData.foregroundColor = Color.RED;
			}
		}
		
		addStatus(statusData);
	}
	
	/**
	 * リストにステータスを追加する。
	 * 
	 * @param statusData StatusDataインスタンス。
	 * @return 追加された StatusPanel
	 */
	public StatusPanel addStatus(StatusData statusData) {
		final StatusPanel linePanel = new StatusPanel(statusData);
		BoxLayout layout = new BoxLayout(linePanel, BoxLayout.X_AXIS);
		linePanel.setLayout(layout);
		linePanel.setAlignmentX(JComponent.LEFT_ALIGNMENT);
		statusData.image.setInheritsPopupMenu(true);
		statusData.image.setFocusable(true);
		statusData.image.setMinimumSize(iconSize);
		statusData.image.setMaximumSize(iconSize);
		linePanel.add(statusData.image);
		linePanel.add(Box.createHorizontalStrut(3));
		statusData.sentBy.setInheritsPopupMenu(true);
		statusData.sentBy.setFocusable(true);
		statusData.sentBy.setMinimumSize(linePanelSizeOfSentBy);
		statusData.sentBy.setMaximumSize(linePanelSizeOfSentBy);
		statusData.sentBy.setFont(frameApi.getDefaultFont());
		linePanel.add(statusData.sentBy);
		linePanel.add(Box.createHorizontalStrut(3));
		statusData.data.setInheritsPopupMenu(true);
		statusData.data.setFocusable(true);
		statusData.data.setFont(frameApi.getDefaultFont());
		int dataWidth = fontMetrics.stringWidth(statusData.data.getText());
		
		linePanel.add(statusData.data);
		linePanel.setComponentPopupMenu(statusData.popupMenu);
		/* if (information.isSystemNotify()) {
			information.backgroundColor = Color.BLACK;
		} */
		linePanel.setForeground(statusData.foregroundColor);
		linePanel.setBackground(statusData.backgroundColor);
		Dimension minSize =
				new Dimension(iconSize.width + linePanelSizeOfSentBy.width + dataWidth + 3 * 2, fontHeight + 4);
		linePanel.setMinimumSize(minSize);
		linePanel.setPreferredSize(minSize);
		linePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, fontHeight + 4));
		linePanel.setFocusable(true);
		linePanel.setToolTipText(statusData.tooltip);
		linePanel.addMouseListener(postListListenerSingleton);
		linePanel.addFocusListener(postListListenerSingleton);
		linePanel.addKeyListener(postListListenerSingleton);
		statusData.image.setForeground(statusData.foregroundColor);
		statusData.sentBy.setForeground(statusData.foregroundColor);
		statusData.data.setForeground(statusData.foregroundColor);
		
		synchronized (listItems) {
			statusMap.put(statusData.id, statusData);
			ArrayList<StatusData> list = listItems.get(statusData.sentBy.getName());
			if (list == null) {
				list = new ArrayList<StatusData>();
				listItems.put(statusData.sentBy.getName(), list);
			}
			list.add(statusData);
		}
		synchronized (postListAddQueue) {
			postListAddQueue.add(linePanel);
		}
		return linePanel;
	}
	
	/**
	 * リストにステータスを追加する。その後deltionDelayミリ秒後に該当するステータスを削除する。
	 * 
	 * @param statusData StatusDataインスタンス。
	 * @param deletionDelay 削除を予約する時間。ミリ秒
	 * @return 追加された (もしくはそのあと削除された) ステータス。
	 */
	public JPanel addStatus(StatusData statusData, int deletionDelay) {
		final StatusPanel status = addStatus(statusData);
		removeStatus(statusData, deletionDelay);
		return status;
	}
	
	/**
	 * ポストパネルがフォーカスを得た時のハンドラ
	 * 
	 * @param e Focusイベント
	 * @throws IllegalArgumentException 正しくないプロパティ
	 * @throws NumberFormatException 数値ではないプロパティ
	 */
	protected void focusGainOfLinePanel(FocusEvent e) throws IllegalArgumentException, NumberFormatException {
		if (selectingPost != null) {
			selectingPost.setBackground(selectingPost.getStatusData().backgroundColor);
		}
		selectingPost = (StatusPanel) e.getComponent();
		selectingPost.setBackground(Utility.blendColor(selectingPost.getStatusData().backgroundColor, configuration
			.getFrameApi().getConfigData().colorOfFocusList));
		
		StatusData statusData = selectingPost.getStatusData();
		if (statusData.tag instanceof Status) {
			Status status = (Status) statusData.tag;
			status = status.isRetweet() ? status.getRetweetedStatus() : status;
			String originalStatusText;
			StringBuffer stringBuilder = new StringBuffer(status.getText());
			HTMLEntity.escape(stringBuilder);
			/*int nlposition;
			int offset = 0;
			while ((nlposition = originalStatusTextBuffer.indexOf("&amp;", offset)) >= 0) {
				originalStatusTextBuffer.replace(nlposition, nlposition + 5, "&");
				offset = nlposition;
			}*/
			originalStatusText = stringBuilder.toString();
			String originalStatusTextLowerCased = originalStatusText.toLowerCase(Locale.getDefault());
			stringBuilder.setLength(0);
			
			HashtagEntity[] hashtagEntities = status.getHashtagEntities();
			hashtagEntities = hashtagEntities == null ? new HashtagEntity[0] : hashtagEntities;
			URLEntity[] urlEntities = status.getURLEntities();
			urlEntities = urlEntities == null ? new URLEntity[0] : urlEntities;
			UserMentionEntity[] mentionEntities = status.getUserMentionEntities();
			mentionEntities = mentionEntities == null ? new UserMentionEntity[0] : mentionEntities;
			Object[] entities = new Object[hashtagEntities.length + urlEntities.length + mentionEntities.length];
			
			if (entities.length != 0) {
				int copyOffset = 0;
				System.arraycopy(hashtagEntities, 0, entities, copyOffset, hashtagEntities.length);
				copyOffset += hashtagEntities.length;
				System.arraycopy(urlEntities, 0, entities, copyOffset, urlEntities.length);
				copyOffset += urlEntities.length;
				System.arraycopy(mentionEntities, 0, entities, copyOffset, mentionEntities.length);
			}
			Arrays.sort(entities, new Comparator<Object>() {
				
				@Override
				public int compare(Object o1, Object o2) {
					return getStart(o1) - getStart(o2);
				}
				
				private int getStart(Object obj) {
					if (obj instanceof HashtagEntity) {
						return ((HashtagEntity) obj).getStart();
					} else if (obj instanceof URLEntity) {
						return ((URLEntity) obj).getStart();
					} else if (obj instanceof UserMentionEntity) {
						return ((UserMentionEntity) obj).getStart();
					} else {
						throw new AssertionError();
					}
				}
			});
			int offset = 0;
			for (Object entity : entities) {
				int start;
				int end;
				String replaceText;
				String url;
				if (entity instanceof HashtagEntity) {
					HashtagEntity hashtagEntity = (HashtagEntity) entity;
					String hashtag = "#" + hashtagEntity.getText();
					start = originalStatusText.indexOf(hashtag, offset);
					end = start + hashtag.length();
					replaceText = null;
					url = "http://command/hashtag!" + hashtagEntity.getText();
				} else if (entity instanceof URLEntity) {
					URLEntity urlEntity = (URLEntity) entity;
					url = urlEntity.getURL().toExternalForm();
					start = originalStatusText.indexOf(url, offset);
					end = start + url.length();
					replaceText = urlEntity.getDisplayURL();
				} else if (entity instanceof UserMentionEntity) {
					UserMentionEntity mentionEntity = (UserMentionEntity) entity;
					String screenName = "@" + mentionEntity.getScreenName().toLowerCase(Locale.getDefault());
					start = originalStatusTextLowerCased.indexOf(screenName, offset);
					end = start + screenName.length();
					replaceText = null;
					url = "http://command/userinfo!" + mentionEntity.getScreenName();
				} else {
					throw new AssertionError();
				}
				
				if (offset < start) {
					stringBuilder.append(nl2br(originalStatusText.substring(offset, start)));
				}
				stringBuilder.append("<a href=\"");
				stringBuilder.append(url);
				stringBuilder.append("\">");
				stringBuilder.append(replaceText == null ? originalStatusText.substring(start, end) : replaceText);
				stringBuilder.append("</a>");
				
				offset = end;
			}
			stringBuilder.append(nl2br(originalStatusText.substring(offset)));
			String tweetText = stringBuilder.toString();
			String createdBy =
					MessageFormat.format("@{0} ({1})", status.getUser().getScreenName(), status.getUser().getName());
			String createdByToolTip = MessageFormat.format("from {0}", status.getSource());
			String createdAt = dateFormat.get().format(status.getCreatedAt());
			frameApi.setTweetViewText(tweetText, createdBy, createdByToolTip, createdAt, null,
					((JLabel) statusData.image).getIcon());
		} else {
			frameApi.setTweetViewText(statusData.data.getText(), statusData.sentBy.getName(), null, dateFormat.get()
				.format(statusData.date), null, ((JLabel) statusData.image).getIcon());
		}
	}
	
	/**
	 * {@link #getSclollPane()}の子コンポーネント
	 * 
	 * @return {@link SortedPostListPanel}インスタンス
	 */
	protected SortedPostListPanel getChildComponent() {
		return sortedPostListPanel;
	}
	
	/**
	 * スクロールペーン。
	 * 
	 * @return JScrollPane
	 */
	@SuppressWarnings("serial")
	protected JScrollPane getSclollPane() {
		if (postListScrollPane == null) {
			postListScrollPane = new JScrollPane() {
				
				@Override
				protected boolean processKeyBinding(KeyStroke ks, KeyEvent e, int condition, boolean pressed) {
					logger.trace("jscrollpane#processKeyBinding: keyStroke={}, Event={}, condition={}, pressed={}",
							Utility.toArray(ks, e, condition, pressed));
					switch (ks.getKeyCode()) {
						case KeyEvent.VK_DOWN:
						case KeyEvent.VK_UP:
						case KeyEvent.VK_RIGHT:
						case KeyEvent.VK_LEFT:
							return false;
						default:
							return super.processKeyBinding(ks, e, condition, pressed);
					}
				}
				
				@Override
				protected void processKeyEvent(KeyEvent e) {
					logger.trace("jscrollpane#processKeyEvent: {}", e);
				}
			};
			postListScrollPane.getViewport().setView(getChildComponent());
			postListScrollPane.getVerticalScrollBar().setUnitIncrement(frameApi.getConfigData().scrollAmount);
		}
		return postListScrollPane;
	}
	
	/**
	 * ステータスを取得する。
	 * 
	 * @param statusId ステータスID
	 * @return ステータスデータ
	 */
	public StatusData getStatus(long statusId) {
		return statusMap.get(statusId);
	}
	
	@Override
	public JScrollPane getTabComponent() {
		return getSclollPane();
	}
	
	/**
	 * IDが呼ばれたかどうかを判定する
	 * 
	 * @param userMentionEntities エンティティ
	 * @return 呼ばれたかどうか
	 */
	protected boolean isMentioned(UserMentionEntity[] userMentionEntities) {
		return configuration.isMentioned(userMentionEntities);
	}
	
	/**
	 * nl-&gt;br および 空白を &amp;nbsp;に置き換えする
	 * 
	 * @param text テキスト
	 * @return &lt;br&gt;に置き換えられた文章
	 */
	protected String nl2br(String text) {
		StringBuilder stringBuilder = new StringBuilder(text);
		int offset = 0;
		int position;
		while ((position = stringBuilder.indexOf("\n", offset)) >= 0) {
			stringBuilder.replace(position, position + 1, "<br>");
			offset = position;
		}
		offset = 0;
		while ((position = stringBuilder.indexOf(" ", offset)) >= 0) {
			stringBuilder.replace(position, position + 1, "&nbsp;");
			offset = position;
		}
		return stringBuilder.toString();
	}
	
	/**
	 * ステータスを削除する (未実装)
	 * 
	 * @param statusData ステータスデータ
	 * @param delay 遅延秒
	 */
	public void removeStatus(StatusData statusData, int delay) {
		// TODO Auto-generated method stub
		
	}
	
}

package jp.syuriken.snsw.twclient;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FontMetrics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
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
import java.util.LinkedList;
import java.util.Stack;
import java.util.TimerTask;
import java.util.TreeMap;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import jp.syuriken.snsw.twclient.ClientConfiguration.ConfigData;
import jp.syuriken.snsw.twclient.internal.MomemtumScroller;
import jp.syuriken.snsw.twclient.internal.MomemtumScroller.BoundsTranslator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import twitter4j.DirectMessage;
import twitter4j.HashtagEntity;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.URLEntity;
import twitter4j.User;
import twitter4j.UserList;
import twitter4j.UserMentionEntity;

/**
 * ツイート表示用のタブ
 * 
 * @author $Author$
 */
public abstract class DefaultClientTab implements ClientTab {
	
	/**
	 * レンダラ。このクラスをextendすることによりリスト移動やステータスの受信はできるようになるかも。
	 * 
	 * @author $Author$
	 */
	public abstract class DefaultRenderer implements TabRenderer {
		
		/**
		 * この時間ぐらい情報を置いておけばいいんじゃないですか的な秒数を取得する
		 * 
		 * @return ミリ秒
		 */
		protected int getInfoSurviveTime() {
			return frameApi.getInfoSurviveTime();
		}
		
		@Override
		public void onBlock(User source, User blockedUser) {
		}
		
		@Override
		public void onChangeAccount(boolean forWrite) {
		}
		
		@Override
		public void onCleanUp() {
		}
		
		@Override
		public void onClientMessage(String name, Object arg) {
			if (Utility.equalString(name, REQUEST_FOCUS_TAB_COMPONENT)) {
				if (selectingPost == null) {
					getSortedPostListPanel().requestFocusInWindow();
				} else {
					selectingPost.requestFocusInWindow();
				}
			} else if (Utility.equalString(name, REQUEST_FOCUS_NEXT_COMPONENT)) {
				if (selectingPost == null) {
					getSortedPostListPanel().requestFocusInWindow();
				} else {
					getSortedPostListPanel().requestFocusNextOf(selectingPost);
				}
			} else if (Utility.equalString(name, REQUEST_FOCUS_PREV_COMPONENT)) {
				if (selectingPost == null) {
					getSortedPostListPanel().requestFocusInWindow();
				} else {
					getSortedPostListPanel().requestFocusPreviousOf(selectingPost);
				}
			} else if (Utility.equalString(name, ClientMessageListener.REQUEST_FOCUS_USER_PREV_COMPONENT)) {
				if (selectingPost == null) {
					getSortedPostListPanel().requestFocusInWindow();
				} else {
					ArrayList<StatusPanel> arrayList = listItems.get(selectingPost.getStatusData().user);
					int indexOf = arrayList.indexOf(selectingPost);
					if (indexOf >= 0 && indexOf < arrayList.size() - 1) {
						arrayList.get(indexOf + 1).requestFocusInWindow();
					}
				}
			} else if (Utility.equalString(name, ClientMessageListener.REQUEST_FOCUS_USER_NEXT_COMPONENT)) {
				if (selectingPost == null) {
					getSortedPostListPanel().requestFocusInWindow();
				} else {
					ArrayList<StatusPanel> arrayList = listItems.get(selectingPost.getStatusData().user);
					int indexOf = arrayList.indexOf(selectingPost);
					if (indexOf > 0) {
						arrayList.get(indexOf - 1).requestFocusInWindow();
					}
				}
			} else if (Utility.equalString(name, ClientMessageListener.REQUEST_FOCUS_FIRST_COMPONENT)) {
				getSortedPostListPanel().requestFocusFirstComponent();
			} else if (Utility.equalString(name, ClientMessageListener.REQUEST_FOCUS_WINDOW_FIRST_COMPONENT)) {
				getSortedPostListPanel().getComponentAt(0, getScrollPane().getViewport().getViewPosition().y)
					.requestFocusInWindow();
			} else if (Utility.equalString(name, ClientMessageListener.REQUEST_FOCUS_WINDOW_LAST_COMPONENT)) {
				JViewport viewport = getScrollPane().getViewport();
				getSortedPostListPanel().getComponentAt(0, viewport.getViewPosition().y + viewport.getHeight())
					.requestFocusInWindow();
			} else if (Utility.equalString(name, ClientMessageListener.REQUEST_SCROLL_AS_WINDOW_LAST)) {
				if (selectingPost == null) {
					getSortedPostListPanel().requestFocusInWindow();
				} else {
					Rectangle bounds = getSortedPostListPanel().getBoundsOf(selectingPost);
					JViewport viewport = getScrollPane().getViewport();
					int x = viewport.getViewPosition().x;
					int y = bounds.y - (viewport.getHeight() - bounds.height);
					viewport.setViewPosition(new Point(x, y));
				}
			} else if (Utility.equalString(name, ClientMessageListener.REQUEST_FOCUS_IN_REPLY_TO)) {
				if (selectingPost == null) {
					getSortedPostListPanel().requestFocusInWindow();
				} else {
					StatusData statusData = selectingPost.getStatusData();
					if (statusData.tag instanceof Status) {
						Status tag = (Status) statusData.tag;
						inReplyToStack.push(selectingPost);
						statusMap.get(tag.getInReplyToStatusId()).requestFocusInWindow();
					}
				}
			} else if (Utility.equalString(name, ClientMessageListener.REQUEST_FOCUS_BACK_REPLIED_BY)) {
				if (selectingPost == null) {
					getSortedPostListPanel().requestFocusInWindow();
				} else {
					if (inReplyToStack.isEmpty() == false) {
						inReplyToStack.pop().requestFocusInWindow();
					}
				}
			} else if (Utility.equalString(name, ClientMessageListener.REQUEST_COPY)) {
				if (selectingPost != null) {
					StatusData statusData = selectingPost.getStatusData();
					/* TODO: StringSelection is not copied into gnome-terminal */
					StringSelection stringSelection = new StringSelection(statusData.data.getText());
					clipboard.setContents(stringSelection, stringSelection);
				}
			} else if (Utility.equalString(name, ClientMessageListener.REQUEST_COPY_URL)) {
				if (selectingPost != null) {
					StatusData statusData = selectingPost.getStatusData();
					if (statusData.tag instanceof Status) {
						Status status = (Status) statusData.tag;
						status = status.isRetweet() ? status.getRetweetedStatus() : status;
						String url =
								"http://twitter.com/" + status.getUser().getScreenName() + "/status/" + status.getId();
						/* TODO: StringSelection is not copied into gnome-terminal */
						StringSelection stringSelection = new StringSelection(url);
						clipboard.setContents(stringSelection, stringSelection);
					}
				}
			} else if (Utility.equalString(name, ClientMessageListener.REQUEST_COPY_USERID)) {
				if (selectingPost != null) {
					StatusData statusData = selectingPost.getStatusData();
					/* TODO: StringSelection is not copied into gnome-terminal */
					StringSelection stringSelection = new StringSelection(statusData.user);
					clipboard.setContents(stringSelection, stringSelection);
				}
			} else if (Utility.equalString(name, ClientMessageListener.REQUEST_BROWSER_USER_HOME)) {
				if (selectingPost != null) {
					StatusData statusData = selectingPost.getStatusData();
					if (statusData.tag instanceof Status) {
						Status status = (Status) statusData.tag;
						status = status.isRetweet() ? status.getRetweetedStatus() : status;
						String url = "http://twitter.com/" + status.getUser().getScreenName();
						utility.openBrowser(url);
					}
				}
			} else if (Utility.equalString(name, ClientMessageListener.REQUEST_BROWSER_STATUS)) {
				if (selectingPost != null) {
					StatusData statusData = selectingPost.getStatusData();
					if (statusData.tag instanceof Status) {
						Status status = (Status) statusData.tag;
						status = status.isRetweet() ? status.getRetweetedStatus() : status;
						String url =
								"http://twitter.com/" + status.getUser().getScreenName() + "/status/" + status.getId();
						utility.openBrowser(url);
					}
				}
			} else if (Utility.equalString(name, ClientMessageListener.REQUEST_BROWSER_IN_REPLY_TO)) {
				if (selectingPost != null) {
					StatusData statusData = selectingPost.getStatusData();
					if (statusData.tag instanceof Status) {
						Status status = (Status) statusData.tag;
						if (status.getInReplyToStatusId() != -1) {
							String url =
									"http://twitter.com/" + status.getInReplyToScreenName() + "/status/"
											+ status.getInReplyToStatusId();
							utility.openBrowser(url);
						}
					}
				}
			} else if (Utility.equalString(name, ClientMessageListener.REQUEST_BROWSER_OPENURLS)) {
				if (selectingPost != null) {
					StatusData statusData = selectingPost.getStatusData();
					if (statusData.tag instanceof Status) {
						Status status = (Status) statusData.tag;
						URLEntity[] urlEntities = status.getURLEntities();
						for (URLEntity urlEntity : urlEntities) {
							utility.openBrowser(urlEntity.getURL().toString());
						}
					}
				}
			}
		}
		
		@Override
		public void onConnect() {
		}
		
		@Override
		public void onDeletionNotice(long directMessageId, long userId) {
		}
		
		@Override
		public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
		}
		
		@Override
		public void onDirectMessage(DirectMessage directMessage) {
		}
		
		@Override
		public void onDisconnect() {
		}
		
		@Override
		public void onException(Exception ex) {
		}
		
		@Override
		public void onFavorite(User source, User target, Status favoritedStatus) {
		}
		
		@Override
		public void onFollow(User source, User followedUser) {
		}
		
		@Override
		public void onFriendList(long[] friendIds) {
		}
		
		@Override
		public void onRetweet(User source, User target, Status retweetedStatus) {
		}
		
		@Override
		public void onScrubGeo(long userId, long upToStatusId) {
		}
		
		@Override
		public void onStallWarning(StallWarning warning) {
		}
		
		@Override
		public void onStatus(Status originalStatus) {
			addStatus(originalStatus);
		}
		
		@Override
		public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
		}
		
		@Override
		public void onUnblock(User source, User unblockedUser) {
		}
		
		@Override
		public void onUnfavorite(User source, User target, Status unfavoritedStatus) {
		}
		
		@Override
		public void onUserListCreation(User listOwner, UserList list) {
		}
		
		@Override
		public void onUserListDeletion(User listOwner, UserList list) {
		}
		
		@Override
		public void onUserListMemberAddition(User addedMember, User listOwner, UserList list) {
		}
		
		@Override
		public void onUserListMemberDeletion(User deletedMember, User listOwner, UserList list) {
		}
		
		@Override
		public void onUserListSubscription(User subscriber, User listOwner, UserList list) {
		}
		
		@Override
		public void onUserListUnsubscription(User subscriber, User listOwner, UserList list) {
		}
		
		@Override
		public void onUserListUpdate(User listOwner, UserList list) {
		}
		
		@Override
		public void onUserProfileUpdate(User updatedUser) {
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
			kineticScroller.scrollTo(selectingPost);
		}
		
		@Override
		public void focusLost(FocusEvent e) {
		}
		
		@Override
		public void keyPressed(KeyEvent e) {
			frameApi.handleShortcutKey("list", e);
		}
		
		@Override
		public void keyReleased(KeyEvent e) {
			frameApi.handleShortcutKey("list", e);
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
	 * PostListを更新する。
	 * 
	 * @author $Author$
	 */
	public class PostListUpdater extends TimerTask {
		
		@Override
		public void run() {
			EventQueue.invokeLater(new Runnable() {
				
				@Override
				public void run() {
					synchronized (postListAddQueue) {
						int size = postListAddQueue.size();
						
						getSortedPostListPanel().add(postListAddQueue);
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
				getSortedPostListPanel().requestFocusFirstComponent();
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
	
	
	private static Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
	
	private Stack<StatusPanel> inReplyToStack = new Stack<StatusPanel>();
	
	/** TODO Megumi */
	private static final int PADDING_OF_POSTLIST = 1;
	
	/** DateFormatを管理する */
	protected static ThreadLocal<SimpleDateFormat> dateFormat = new ThreadLocal<SimpleDateFormat>() {
		
		@Override
		protected SimpleDateFormat initialValue() {
			return new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		}
	};
	
	
	/**
	 * nl-&gt;br および 空白を &amp;nbsp;に置き換える
	 * 
	 * @param stringBuilder テキスト
	 * @param start 置き換え開始位置
	 */
	protected static void nl2br(StringBuilder stringBuilder, int start) {
		int offset = start;
		int position;
		while ((position = stringBuilder.indexOf("\n", offset)) >= 0) {
			stringBuilder.replace(position, position + 1, "<br>");
			offset = position + 1;
		}
		offset = start;
		while ((position = stringBuilder.indexOf(" ", offset)) >= 0) {
			stringBuilder.replace(position, position + 1, "&nbsp;");
			offset = position + 1;
		}
		offset = start;
		while ((position = stringBuilder.indexOf("&amp;", offset)) >= 0) {
			stringBuilder.replace(position, position + 5, "&amp;amp;");
			offset = position + 9;
		}
	}
	
	/**
	 * nl-&gt;br および 空白を &amp;nbsp;に置き換える
	 * 
	 * @param stringBuilder テキスト
	 * @param append 追加する文字列
	 */
	protected static void nl2br(StringBuilder stringBuilder, String append) {
		int offset = stringBuilder.length();
		stringBuilder.append(append);
		nl2br(stringBuilder, offset);
	}
	
	
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
	
	/** 送信元ラベルのサイズ */
	protected Dimension linePanelSizeOfSentBy;
	
	/** アイコンを表示するときのサイズ */
	protected Dimension iconSize;
	
	/** ポストリストリスナのシングルインスタンス */
	protected final PostListListener postListListenerSingleton = new PostListListener();
	
	/** <K=ユーザーID, V=ユーザーのツイートなど> */
	protected TreeMap<String, ArrayList<StatusPanel>> listItems = new TreeMap<String, ArrayList<StatusPanel>>();
	
	/** <K=ステータスID, V=ツイートなど> */
	protected TreeMap<Long, StatusPanel> statusMap = new TreeMap<Long, StatusPanel>();
	
	/** スクロールペーン */
	protected JScrollPane postListScrollPane;
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	/** {@link ClientConfiguration#getConfigData()} */
	protected final ConfigData configData;
	
	private LinkedList<StatusPanel> postListAddQueue = new LinkedList<StatusPanel>();
	
	/** ポップアップメニュー */
	protected JPopupMenu tweetPopupMenu;
	
	/** 慣性スクローラー */
	protected MomemtumScroller kineticScroller;
	
	/** {@link ClientConfiguration#getUtility()} */
	protected Utility utility;
	
	
	/**
	 * インスタンスを生成する。
	 * 
	 * @param configuration 設定
	 */
	protected DefaultClientTab(ClientConfiguration configuration) {
		this.configuration = configuration;
		imageCacher = configuration.getImageCacher();
		frameApi = configuration.getFrameApi();
		utility = configuration.getUtility();
		sortedPostListPanel = new SortedPostListPanel();
		configData = frameApi.getConfigData();
		fontMetrics = getSortedPostListPanel().getFontMetrics(frameApi.getDefaultFont());
		int str12width = fontMetrics.stringWidth("0123456789abc");
		fontHeight = fontMetrics.getHeight();
		linePanelSizeOfSentBy = new Dimension(str12width, fontHeight);
		iconSize = new Dimension(64, fontHeight);
		frameApi.getTimer().schedule(new PostListUpdater(), configData.intervalOfPostListUpdate,
				configData.intervalOfPostListUpdate);
		tweetPopupMenu = ((TwitterClientFrame) (frameApi)).generatePopupMenu(new TweetPopupMenuListener());
		tweetPopupMenu.addPopupMenuListener(new TweetPopupMenuListener());
		kineticScroller = new MomemtumScroller(getScrollPane(), new BoundsTranslator() {
			
			@Override
			public Rectangle translate(JComponent component) {
				return sortedPostListPanel.getBoundsOf((StatusPanel) component);
			}
		});
	}
	
	/**
	 * リストにステータスを追加する。
	 * 
	 * @param originalStatus 元となるStatus
	 */
	public void addStatus(Status originalStatus) {
		Status twitterStatus = new TwitterStatus(originalStatus);
		StatusData statusData = new StatusData(twitterStatus, twitterStatus.getCreatedAt(), twitterStatus.getId());
		
		Status status;
		if (twitterStatus.isRetweet()) {
			status = twitterStatus.getRetweetedStatus();
		} else {
			status = twitterStatus;
		}
		User user = status.getUser();
		
		if (configData.mentionIdStrictMatch) {
			if (user.getId() == frameApi.getLoginUser().getId()) {
				statusData.foregroundColor = Color.BLUE;
			}
		} else {
			if (user.getScreenName().startsWith(frameApi.getLoginUser().getScreenName())) {
				statusData.foregroundColor = Color.BLUE;
			}
		}
		
		JLabel icon = new JLabel();
		imageCacher.setImageIcon(icon, status.getUser());
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
		
		JLabel statusText = new JLabel(status.getText());
		statusData.data = statusText;
		
		statusData.popupMenu = tweetPopupMenu;
		
		if (twitterStatus.isRetweet()) {
			StringBuilder stringBuilder = new StringBuilder();
			stringBuilder.append("Retweeted by @").append(twitterStatus.getUser().getScreenName());
			statusData.tooltip = stringBuilder.toString();
		}
		
		if (twitterStatus.isRetweet()) {
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
				new Dimension(iconSize.width + linePanelSizeOfSentBy.width + dataWidth + 3 * 2, fontHeight
						+ PADDING_OF_POSTLIST);
		linePanel.setMinimumSize(minSize);
		linePanel.setPreferredSize(minSize);
		linePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, fontHeight + PADDING_OF_POSTLIST));
		linePanel.setFocusable(true);
		linePanel.setToolTipText(statusData.tooltip);
		linePanel.addMouseListener(postListListenerSingleton);
		linePanel.addFocusListener(postListListenerSingleton);
		linePanel.addKeyListener(postListListenerSingleton);
		statusData.image.setForeground(statusData.foregroundColor);
		statusData.sentBy.setForeground(statusData.foregroundColor);
		statusData.data.setForeground(statusData.foregroundColor);
		
		synchronized (listItems) {
			statusMap.put(statusData.id, linePanel);
			ArrayList<StatusPanel> list = listItems.get(statusData.user);
			if (list == null) {
				list = new ArrayList<StatusPanel>();
				listItems.put(statusData.user, list);
			}
			list.add(linePanel);
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
		selectingPost.setBackground(Utility.blendColor(selectingPost.getStatusData().backgroundColor,
				frameApi.getConfigData().colorOfFocusList));
		
		StatusData statusData = selectingPost.getStatusData();
		if (statusData.tag instanceof TwitterStatus) {
			TwitterStatus status = (TwitterStatus) statusData.tag;
			status = status.isRetweet() ? status.getRetweetedStatus() : status;
			String escapedText = status.getEscapedText();
			StringBuilder stringBuilder = new StringBuilder(escapedText.length());
			
			HashtagEntity[] hashtagEntities = status.getOriginalStatus().getHashtagEntities();
			hashtagEntities = hashtagEntities == null ? new HashtagEntity[0] : hashtagEntities;
			URLEntity[] urlEntities = status.getOriginalStatus().getURLEntities();
			urlEntities = urlEntities == null ? new URLEntity[0] : urlEntities;
			UserMentionEntity[] mentionEntities = status.getOriginalStatus().getUserMentionEntities();
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
					return TwitterStatus.getEntityStart(o1) - TwitterStatus.getEntityStart(o2);
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
					start = TwitterStatus.getEntityStart(hashtagEntity);
					end = TwitterStatus.getEntityEnd(hashtagEntity);
					replaceText = null;
					url = "http://command/hashtag!" + hashtagEntity.getText();
				} else if (entity instanceof URLEntity) {
					URLEntity urlEntity = (URLEntity) entity;
					url = urlEntity.getURL().toExternalForm();
					start = TwitterStatus.getEntityStart(urlEntity);
					end = TwitterStatus.getEntityEnd(urlEntity);
					replaceText = urlEntity.getDisplayURL();
				} else if (entity instanceof UserMentionEntity) {
					UserMentionEntity mentionEntity = (UserMentionEntity) entity;
					start = TwitterStatus.getEntityStart(mentionEntity);
					end = TwitterStatus.getEntityEnd(mentionEntity);
					replaceText = null;
					url = "http://command/userinfo!" + mentionEntity.getScreenName();
				} else {
					throw new AssertionError();
				}
				
				if (offset < start) {
					nl2br(stringBuilder, escapedText.substring(offset, start));
				}
				stringBuilder.append("<a href=\"");
				stringBuilder.append(url);
				stringBuilder.append("\">");
				stringBuilder.append(replaceText == null ? escapedText.substring(start, end) : replaceText);
				stringBuilder.append("</a>");
				
				offset = end;
			}
			nl2br(stringBuilder, escapedText.substring(offset));
			String tweetText = stringBuilder.toString();
			String createdBy =
					MessageFormat.format("@{0} ({1})", status.getUser().getScreenName(), status.getUser().getName());
			String source = status.getSource();
			int tagIndexOf = source.indexOf('>');
			int tagLastIndexOf = source.lastIndexOf('<');
			String createdAtToolTip =
					MessageFormat.format("from {0}",
							source.substring(tagIndexOf + 1, tagLastIndexOf == -1 ? source.length() : tagLastIndexOf));
			String createdAt = dateFormat.get().format(status.getCreatedAt());
			frameApi.setTweetViewText(tweetText, createdBy, null, createdAt, createdAtToolTip,
					((JLabel) statusData.image).getIcon());
		} else if (statusData.tag instanceof Exception) {
			Exception ex = (Exception) statusData.tag;
			StringBuilder stringBuilder = new StringBuilder(ex.getLocalizedMessage());
			nl2br(stringBuilder, 0);
			frameApi.setTweetViewText(stringBuilder.toString(), ex.getClass().getName(), null,
					dateFormat.get().format(statusData.date), null, ((JLabel) statusData.image).getIcon());
		} else {
			frameApi.setTweetViewText(statusData.data.getText(), statusData.user, null,
					dateFormat.get().format(statusData.date), null, ((JLabel) statusData.image).getIcon());
		}
	}
	
	/**
	 * {@link #getScrollPane()}の子コンポーネント
	 * 
	 * @return {@link SortedPostListPanel}インスタンス
	 */
	protected JComponent getChildComponent() {
		return getSortedPostListPanel();
	}
	
	/**
	* スクロールペーン。
	* 
	* @return JScrollPane
	*/
	@SuppressWarnings("serial")
	protected JScrollPane getScrollPane() {
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
	 * SortedPostListPanelを取得する(レンダラ用)
	 * 
	 * @return {@link SortedPostListPanel}インスタンス
	 */
	protected SortedPostListPanel getSortedPostListPanel() {
		return sortedPostListPanel;
	}
	
	/**
	 * ステータスを取得する。
	 * 
	 * @param statusId ステータスID
	 * @return ステータスデータ
	 */
	public StatusData getStatus(long statusId) {
		StatusPanel statusPanel = statusMap.get(statusId);
		return statusPanel == null ? null : statusPanel.getStatusData();
	}
	
	@Override
	public JComponent getTabComponent() {
		return getScrollPane();
	}
	
	@Override
	public void handleAction(String command) {
		frameApi.handleAction(command, selectingPost.getStatusData());
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
	 * ステータスを削除する (未実装)
	 * 
	 * @param statusData ステータスデータ
	 * @param delay 遅延秒
	 */
	public void removeStatus(StatusData statusData, int delay) {
		// TODO Auto-generated method stub
		
	}
	
}
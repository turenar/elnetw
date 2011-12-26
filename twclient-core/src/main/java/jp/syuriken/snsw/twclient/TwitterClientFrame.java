package jp.syuriken.snsw.twclient;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import jp.syuriken.snsw.twclient.JobQueue.Priority;
import jp.syuriken.snsw.twclient.handler.FavoriteActionHandler;
import jp.syuriken.snsw.twclient.handler.QuoteTweetActionHandler;
import jp.syuriken.snsw.twclient.handler.RemoveTweetActionHandler;
import jp.syuriken.snsw.twclient.handler.ReplyActionHandler;
import jp.syuriken.snsw.twclient.handler.RetweetActionHandler;
import jp.syuriken.snsw.twclient.handler.UrlActionHandler;
import jp.syuriken.snsw.twclient.handler.UserInfoViewActionHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import twitter4j.HashtagEntity;
import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.URLEntity;
import twitter4j.User;
import twitter4j.UserMentionEntity;
import twitter4j.internal.http.HTMLEntity;

/**
 * twclientのメインウィンドウ
 * @author $Author$
 */
@SuppressWarnings("serial")
/*package*/class TwitterClientFrame extends javax.swing.JFrame implements ClientFrameApi {
	
	/**
	 * MenuItemのActionListenerの実装。
	 * 
	 * @author $Author$
	 */
	private final class ActionListenerImplementation implements ActionListener {
		
		@Override
		public void actionPerformed(ActionEvent e) {
			StatusData statusData;
			if (selectingPost == null) {
				statusData = null;
			} else {
				statusData = statusMap.get(selectingPost.getStatusData().id);
			}
			handleAction(e.getActionCommand(), statusData);
		}
	}
	
	/**
	 * メニューのプロパティエディタを開くためのアクションハンドラ
	 * 
	 * @author $Author$
	 */
	public class MenuPropertyEditorActionHandler implements ActionHandler {
		
		@Override
		public JMenuItem createJMenuItem(String commandName) {
			return null;
		}
		
		@Override
		public void handleAction(String actionName, StatusData statusData, ClientFrameApi api) {
			PropertyEditorFrame propertyEditorFrame = new PropertyEditorFrame(configuration);
			propertyEditorFrame.setVisible(true);
		}
		
		@Override
		public void popupMenuWillBecomeVisible(JMenuItem menuItem, StatusData statusData, ClientFrameApi api) {
			// This is always enabled.
		}
		
	}
	
	/**
	 * 終了するためのアクションハンドラ
	 * 
	 * @author $Author$
	 */
	public class MenuQuitActionHandler implements ActionHandler {
		
		@Override
		public JMenuItem createJMenuItem(String commandName) {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public void handleAction(String actionName, StatusData statusData, ClientFrameApi api) {
			setVisible(false);
			dispose();
		}
		
		@Override
		public void popupMenuWillBecomeVisible(JMenuItem menuItem, StatusData statusData, ClientFrameApi api) {
			// This is always enabled
		}
		
	}
	
	private class PostListMouseListner extends MouseAdapter {
		
		@Override
		public void mouseClicked(MouseEvent e) {
			e.getComponent().requestFocusInWindow();
			if (e.isPopupTrigger()) {
				selectingPost = (StatusPanel) e.getComponent();
			}
			if (e.getClickCount() == 2) {
				StatusPanel panel = ((StatusPanel) e.getComponent());
				long uniqId = panel.getStatusData().id;
				StatusData statusData = statusMap.get(uniqId);
				actionHandlerTable.get("reply").handleAction("reply", statusData, TwitterClientFrame.this);
			}
			
		}
	}
	
	/**
	 * StatusPanelのポップアップメニューリスナ
	 * 
	 * @author $Author$
	 */
	public class TweetPopupMenuListner implements PopupMenuListener {
		
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
				StatusData statusData = statusMap.get(selectingPost.getStatusData().id);
				if (statusData == null) {
					menuItem.setEnabled(false);
				} else {
					ActionHandler actionHandler = getActionHandler(menuItem.getActionCommand());
					if (actionHandler != null) {
						actionHandler.popupMenuWillBecomeVisible(menuItem, statusData, TwitterClientFrame.this);
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
						if (viewPosition.y < fontHeight + 4) {
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
	
	
	/** アプリケーション名 */
	private static final String APPLICATION_NAME = "Astarotte";
	
	private StatusPanel selectingPost;
	
	private Hashtable<String, ActionHandler> actionHandlerTable;
	
	private Status inReplyToStatus = null;
	
	private final JobQueue jobQueue = new JobQueue();
	
	private JobWorkerThread jobWorkerThread;
	
	private JPanel editPanel;
	
	private JPanel postPanel;
	
	private JScrollPane postDataTextAreaScrollPane;
	
	private JScrollPane postListScrollPane;
	
	private JSplitPane jSplitPane1;
	
	private JButton postActionButton;
	
	private JTextArea postDataTextArea;
	
	private TwitterStream stream;
	
	private SortedPostListPanel sortedPostListPanel;
	
	private TreeMap<String, ArrayList<StatusData>> listItems;
	
	private TreeMap<Long, StatusData> statusMap;
	
	private Twitter twitter;
	
	/** デフォルトフォント: TODO from config */
	public static final Font DEFAULT_FONT = new Font(Font.MONOSPACED, Font.PLAIN, 12);
	
	/** UIフォント: TODO from config */
	public static final Font UI_FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 13);
	
	/** フォントの高さ */
	protected int fontHeight;
	
	private MouseListener postListMouseListnerSingleton = new PostListMouseListner();
	
	private static final Dimension ICON_SIZE = new Dimension(64, 18);
	
	private JPopupMenu tweetPopupMenu;
	
	private User loginUser;
	
	private LinkedList<StatusPanel> postListAddQueue = new LinkedList<StatusPanel>();
	
	private Timer timer;
	
	private UpdatePostList updatePostListDispatcher;
	
	private ClientProperties configProperties;
	
	private JMenuBar clientMenu;
	
	private final ClientConfiguration configuration;
	
	private JPanel tweetViewPanel;
	
	private JScrollPane tweetViewScrollPane;
	
	private JEditorPane tweetViewEditorPane;
	
	private JLabel tweetViewSourceLabel;
	
	private JLabel tweetViewDateLabel;
	
	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
	
	private FontMetrics fontMetrics;
	
	private Dimension linePanelSizeOfSentBy;
	
	private final Object mainThreadHolder;
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	private ImageCacher imageCacher;
	
	
	/** 
	 * Creates new form TwitterClientFrame 
	 * @param configuration 設定
	 * @param threadHolder スレッドホルダ
	 */
	public TwitterClientFrame(ClientConfiguration configuration, Object threadHolder) {
		logger.info("initializing frame");
		
		this.configuration = configuration;
		configuration.setFrameApi(this);
		mainThreadHolder = threadHolder;
		configProperties = configuration.getConfigProperties();
		timer = new Timer("timer");
		actionHandlerTable = new Hashtable<String, ActionHandler>();
		initActionHandlerTable(actionHandlerTable);
		listItems = new TreeMap<String, ArrayList<StatusData>>();
		statusMap = new TreeMap<Long, StatusData>();
		twitter = new TwitterFactory(configuration.getTwitterConfiguration()).getInstance();
		getLoginUser();
		getPopupMenu();
		initComponents();
		updatePostListDispatcher = new UpdatePostList();
		timer.schedule(updatePostListDispatcher, configProperties.getInteger("client.main.interval.list_update"),
				configProperties.getInteger("client.main.interval.list_update"));
		
		timer.schedule(new TimerTask() {
			
			@Override
			public void run() {
				synchronized (postListAddQueue) {
					logger.debug(sortedPostListPanel.toString());
				}
			}
		}, 1000, 10000);
		imageCacher = new ImageCacher(configuration);
		
		stream = new TwitterStreamFactory(configuration.getTwitterConfiguration()).getInstance();
		stream.addConnectionLifeCycleListener(new ClientConnectionLifeCycleListner(this));
		stream.addListener(new ClientStreamListener(this));
		
		fontMetrics = getFontMetrics(DEFAULT_FONT);
		int str12width = fontMetrics.stringWidth("0123456789abc");
		linePanelSizeOfSentBy = new Dimension(str12width, fontHeight);
		logger.info("initialized");
	}
	
	/**
	 * ジョブを追加する
	 * 
	 * @param priority 優先度
	 * @param job ジョブ
	 */
	@Override
	public void addJob(Priority priority, Runnable job) {
		jobQueue.addJob(priority, job);
	}
	
	/**
	 * ジョブを追加する
	 * 
	 * @param job ジョブ
	 */
	@Override
	public void addJob(Runnable job) {
		jobQueue.addJob(job);
	}
	
	/**
	 * リストにステータスを追加する。
	 * 
	 * @param originalStatus 元となるStatus
	 */
	@Override
	public void addStatus(Status originalStatus) {
		synchronized (listItems) {
			if (statusMap.containsKey(originalStatus.getId())) {
				return; // It was already added.
			}
		}
		Status twitterStatus = new TwitterStatus(originalStatus);
		StatusData statusData = new StatusData(twitterStatus, originalStatus.getCreatedAt(), originalStatus.getId());
		
		Status status;
		if (originalStatus.isRetweet()) {
			status = originalStatus.getRetweetedStatus();
		} else {
			status = originalStatus;
		}
		User user = status.getUser();
		
		if (configProperties.getBoolean("client.main.match.id_strict_match")) {
			if (user.getId() == loginUser.getId()) {
				statusData.foregroundColor = Color.BLUE;
			}
		} else {
			if (user.getScreenName().startsWith(loginUser.getScreenName())) {
				statusData.foregroundColor = Color.BLUE;
			}
		}
		
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
		sentBy.setFont(DEFAULT_FONT);
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
			for (UserMentionEntity userMentionEntity : userMentionEntities) {
				boolean mentioned = false;
				if (configProperties.getBoolean("client.main.match.id_strict_match")) {
					if (userMentionEntity.getId() == getLoginUser().getId()) {
						mentioned = true;
					}
				} else {
					if (userMentionEntity.getScreenName().startsWith(loginUser.getScreenName())) {
						mentioned = true;
					}
				}
				if (mentioned) {
					statusData.foregroundColor = Color.RED;
					Utility.sendNotify(user.getName(), originalStatus.getText(), imageCacher.getImageFile(user));
				}
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
	@Override
	public StatusPanel addStatus(StatusData statusData) {
		final StatusPanel linePanel = new StatusPanel(statusData);
		BoxLayout layout = new BoxLayout(linePanel, BoxLayout.X_AXIS);
		linePanel.setLayout(layout);
		linePanel.setAlignmentX(LEFT_ALIGNMENT);
		statusData.image.setInheritsPopupMenu(true);
		statusData.image.setFocusable(true);
		statusData.image.setMinimumSize(ICON_SIZE);
		statusData.image.setMaximumSize(ICON_SIZE);
		linePanel.add(statusData.image);
		linePanel.add(Box.createHorizontalStrut(3));
		statusData.sentBy.setInheritsPopupMenu(true);
		statusData.sentBy.setFocusable(true);
		statusData.sentBy.setMinimumSize(linePanelSizeOfSentBy);
		statusData.sentBy.setMaximumSize(linePanelSizeOfSentBy);
		statusData.sentBy.setFont(DEFAULT_FONT);
		linePanel.add(statusData.sentBy);
		linePanel.add(Box.createHorizontalStrut(3));
		statusData.data.setInheritsPopupMenu(true);
		statusData.data.setFocusable(true);
		statusData.data.setFont(DEFAULT_FONT);
		int dataWidth = fontMetrics.stringWidth(statusData.data.getText());
		
		linePanel.add(statusData.data);
		linePanel.setComponentPopupMenu(statusData.popupMenu);
		/* if (information.isSystemNotify()) {
			information.backgroundColor = Color.BLACK;
		} */
		linePanel.setForeground(statusData.foregroundColor);
		linePanel.setBackground(statusData.backgroundColor);
		Dimension minSize =
				new Dimension(ICON_SIZE.width + linePanelSizeOfSentBy.width + dataWidth + 3 * 2, fontHeight + 4);
		linePanel.setMinimumSize(minSize);
		linePanel.setPreferredSize(minSize);
		linePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, fontHeight + 4));
		linePanel.addMouseListener(postListMouseListnerSingleton);
		linePanel.setFocusable(true);
		linePanel.setToolTipText(statusData.tooltip);
		linePanel.addFocusListener(new FocusAdapter() {
			
			@Override
			public void focusGained(FocusEvent e) {
				focusGainOfLinePanel(e);
			}
		});
		statusData.image.setForeground(statusData.foregroundColor);
		statusData.sentBy.setForeground(statusData.foregroundColor);
		statusData.data.setForeground(statusData.foregroundColor);
		
		statusData.addStatusPanel(linePanel);
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
	@Override
	public JPanel addStatus(StatusData statusData, int deletionDelay) {
		final StatusPanel status = addStatus(statusData);
		removeStatus(statusData, deletionDelay);
		return status;
	}
	
	/**
	 * 終了できるようにお掃除する
	 */
	public void cleanUp() {
		configuration.setShutdownPhase(true);
		stream.shutdown();
		timer.cancel();
		jobWorkerThread.cleanUp();
	}
	
	/**
	 * ポストパネルがフォーカスを得た時のハンドラ
	 * 
	 * @param e Focusイベント
	 * @throws IllegalArgumentException 正しくないプロパティ
	 * @throws NumberFormatException 数値ではないプロパティ
	 */
	private void focusGainOfLinePanel(FocusEvent e) throws IllegalArgumentException, NumberFormatException {
		if (selectingPost != null) {
			selectingPost.setBackground(statusMap.get(selectingPost.getStatusData().id).backgroundColor);
		}
		e.getComponent().setBackground(configProperties.getColor("client.main.color.list.focus"));
		selectingPost = (StatusPanel) e.getComponent();
		
		JEditorPane editor = getTweetViewEditorPane();
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
					String screenName = "@" + mentionEntity.getScreenName();
					start = originalStatusText.indexOf(screenName, offset);
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
			editor.setText(stringBuilder.toString());
			
			JLabel viewSourceLabel = getTweetViewSourceLabel();
			viewSourceLabel.setText(MessageFormat.format("@{0} ({1})", status.getUser().getScreenName(), status
				.getUser().getName()));
			viewSourceLabel.setToolTipText(MessageFormat.format("from {0}", status.getSource()));
			
			JLabel viewDateLabel = getTweetViewDateLabel();
			viewDateLabel.setText(dateFormat.format(status.getCreatedAt()));
		} else {
			editor.setText(statusData.data.getText());
			getTweetViewSourceLabel().setText(statusData.sentBy.getName());
			getTweetViewDateLabel().setText(dateFormat.format(statusData.date));
		}
	}
	
	@Override
	public void focusPostBox() {
		getPostDataTextArea().requestFocusInWindow();
	}
	
	/**
	 * アクションハンドラを取得する
	 * 
	 * @param name アクション名。!を含んでいても可
	 * @return アクションハンドラ
	 */
	private ActionHandler getActionHandler(String name) {
		int indexOf = name.indexOf('!');
		String commandName = indexOf < 0 ? name : name.substring(0, indexOf);
		ActionHandler actionHandler = actionHandlerTable.get(commandName);
		return actionHandler;
	}
	
	/**
	 * メニューバーを取得する。
	 * 
	 * @return メニューバー
	 */
	private JMenuBar getClientMenuBar() {
		if (clientMenu == null) {
			clientMenu = new JMenuBar();
			{
				JMenu applicationMenu = new JMenu("アプリケーション");
				JMenuItem quitMenuItem = new JMenuItem("終了(Q)", KeyEvent.VK_Q);
				quitMenuItem.setActionCommand("menu_quit");
				quitMenuItem.addActionListener(new ActionListenerImplementation());
				applicationMenu.add(quitMenuItem);
				clientMenu.add(applicationMenu);
			}
			{
				JMenu configMenu = new JMenu("設定");
				JMenuItem propertyEditorMenuItem = new JMenuItem("プロパティエディター(P)", KeyEvent.VK_P);
				propertyEditorMenuItem.setActionCommand("menu_propeditor");
				propertyEditorMenuItem.addActionListener(new ActionListenerImplementation());
				configMenu.add(propertyEditorMenuItem);
				clientMenu.add(configMenu);
			}
		}
		return clientMenu;
	}
	
	private JPanel getEditPanel() {
		if (editPanel == null) {
			editPanel = new JPanel();
			GroupLayout layout = new GroupLayout(editPanel);
			editPanel.setLayout(layout);
			layout.setHorizontalGroup( //
				layout
					.createParallelGroup(GroupLayout.Alignment.LEADING)
					.addComponent(getPostPanel(), GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE,
							GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE) //
					.addComponent(getTweetViewPanel()));
			layout.setVerticalGroup( //
				layout.createParallelGroup(GroupLayout.Alignment.LEADING) //
					.addGroup(
							layout.createSequentialGroup()
								.addComponent(getPostPanel(), 32, 64, GroupLayout.PREFERRED_SIZE)
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addComponent(getTweetViewPanel(), 64, Short.MAX_VALUE, GroupLayout.PREFERRED_SIZE)));
			
		}
		return editPanel;
	}
	
	/**
	 * 一時的な情報を追加するときに、この時間たったら削除してもいーよ的な時間を取得する。
	 * 若干重要度が高いときは *2 とかしてみよう！
	 * 
	 * @return 一時的な情報が生き残る時間
	 */
	@Override
	public int getInfoSurviveTime() {
		return configProperties.getInteger("client.info.survive_time");
	}
	
	/**
	 * ログインしているユーザーを取得する。取得出来なかった場合nullの可能性あり。また、ブロックする可能性あり。
	 * 
	 * @return the loginUser
	 */
	@Override
	public User getLoginUser() {
		if (loginUser == null) {
			try {
				loginUser = twitter.verifyCredentials();
			} catch (TwitterException e) {
				handleException(e);
			}
		}
		return loginUser;
	}
	
	private JPopupMenu getPopupMenu() {
		if (tweetPopupMenu == null) {
			ActionListener actionListner = new ActionListenerImplementation();
			JPopupMenu popupMenu = new JPopupMenu();
			popupMenu.addPopupMenuListener(new TweetPopupMenuListner());
			
			String[] popupMenus = configProperties.getProperty("gui.menu.popup").split(" ");
			
			for (String actionCommand : popupMenus) {
				ActionHandler handler = getActionHandler(actionCommand);
				if (handler == null) {
					logger.warn("handler {} is not found.", actionCommand); //TODO
				} else {
					JMenuItem menuItem = handler.createJMenuItem(actionCommand);
					menuItem.setActionCommand(actionCommand);
					menuItem.addActionListener(actionListner);
					popupMenu.add(menuItem);
				}
			}
			tweetPopupMenu = popupMenu;
		}
		return tweetPopupMenu;
	}
	
	private JButton getPostActionButton() {
		if (postActionButton == null) {
			postActionButton = new javax.swing.JButton();
			postActionButton.setText("投稿");
			postActionButton.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					postActionButtonMouseClicked(e);
				}
			});
			
		}
		return postActionButton;
	}
	
	private JTextArea getPostDataTextArea() {
		if (postDataTextArea == null) {
			postDataTextArea = new javax.swing.JTextArea();
			postDataTextArea.setColumns(20);
			postDataTextArea.setRows(3);
			postDataTextArea.setFont(UI_FONT);
			postDataTextArea.addKeyListener(new KeyAdapter() {
				
				@Override
				public void keyReleased(KeyEvent e) {
					if (e.isControlDown()) {
						if (e.getKeyCode() == KeyEvent.VK_ENTER) {
							getPostActionButton().doClick();
						}
					}
				}
			});
		}
		return postDataTextArea;
	}
	
	private JScrollPane getPostDataTextAreaScrollPane() {
		if (postDataTextAreaScrollPane == null) {
			postDataTextAreaScrollPane = new JScrollPane();
			postDataTextAreaScrollPane.setViewportView(getPostDataTextArea());
		}
		return postDataTextAreaScrollPane;
	}
	
	private JScrollPane getPostListScrollPane() {
		if (postListScrollPane == null) {
			postListScrollPane = new javax.swing.JScrollPane();
			postListScrollPane.getViewport().setView(getSortedPostListPanel());
			postListScrollPane.getVerticalScrollBar().setUnitIncrement(
					configProperties.getInteger("client.main.list.scroll"));
			
		}
		return postListScrollPane;
	}
	
	private JPanel getPostPanel() {
		if (postPanel == null) {
			postPanel = new JPanel();
			GroupLayout layout = new GroupLayout(postPanel);
			postPanel.setLayout(layout);
			layout.setHorizontalGroup( //
				layout.createParallelGroup(GroupLayout.Alignment.LEADING) //
					.addGroup(
							GroupLayout.Alignment.TRAILING, //
							layout
								.createSequentialGroup()
								.addContainerGap()
								.addComponent(getPostDataTextAreaScrollPane(), GroupLayout.DEFAULT_SIZE, 475,
										Short.MAX_VALUE)
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED) //
								.addComponent(getPostActionButton()).addGap(18, 18, 18)));
			layout.setVerticalGroup( //
				layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(
						layout
							.createSequentialGroup()
							.addGroup(
									layout
										.createParallelGroup(GroupLayout.Alignment.TRAILING)
										.addComponent(getPostActionButton())
										.addComponent(getPostDataTextAreaScrollPane(), 32, 64,
												GroupLayout.PREFERRED_SIZE)).addContainerGap(6, Short.MAX_VALUE)));
			
		}
		return postPanel;
	}
	
	/**
	 * 今現在ポストボックスに入力されている文字列を返す。
	 * 
	 * @return ポストボックスに入力されている文字列
	 */
	@Override
	public String getPostText() {
		return getPostDataTextArea().getText();
	}
	
	/**
	 * ソート済みリストパネルを取得する。将来的にはマルチタブになる予定なのでこのメソッドは廃止される予定です。
	 * 
	 * @return the sortedPostListPanel
	 */
	public SortedPostListPanel getSortedPostListPanel() {
		if (sortedPostListPanel == null) {
			sortedPostListPanel =
					new SortedPostListPanel(configProperties.getInteger("client.main.list.split_size"),
							configProperties.getInteger("client.main.list.max_size"));
			
			sortedPostListPanel.setBackground(Color.WHITE);
		}
		return sortedPostListPanel;
	}
	
	private JSplitPane getSplitPane1() {
		if (jSplitPane1 == null) {
			jSplitPane1 = new JSplitPane();
			jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
			jSplitPane1.setTopComponent(getEditPanel());
			jSplitPane1.setRightComponent(getPostListScrollPane());
		}
		return jSplitPane1;
	}
	
	@Override
	public StatusData getStatus(long statusId) {
		synchronized (listItems) {
			return statusMap.get(statusId);
		}
	}
	
	/**
	 * タイマーを取得する。
	 * 
	 * @return タイマー
	 */
	@Override
	public Timer getTimer() {
		return timer;
	}
	
	private JLabel getTweetViewDateLabel() {
		if (tweetViewDateLabel == null) {
			tweetViewDateLabel = new JLabel();
			tweetViewDateLabel.setText("2012/1/1 00:00:00");
		}
		return tweetViewDateLabel;
	}
	
	private JEditorPane getTweetViewEditorPane() {
		if (tweetViewEditorPane == null) {
			tweetViewEditorPane = new JEditorPane();
			tweetViewEditorPane.setEditable(false);
			tweetViewEditorPane.setContentType("text/html");
			tweetViewEditorPane.setFont(UI_FONT);
			tweetViewEditorPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
			tweetViewEditorPane.setText(APPLICATION_NAME + "へようこそ！<br><b>ゆっくりしていってね！</b>");
			tweetViewEditorPane.addHyperlinkListener(new HyperlinkListener() {
				
				@Override
				public void hyperlinkUpdate(HyperlinkEvent e) {
					if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
						String url = e.getURL().toString();
						if (url.startsWith("http://command/")) {
							String command = url.substring("http://command/".length());
							handleAction(command, selectingPost == null ? null : selectingPost.getStatusData());
						} else {
							try {
								Utility.openBrowser(url);
							} catch (Exception e1) {
								e1.printStackTrace(); //TODO
							}
						}
					}
				}
			});
		}
		return tweetViewEditorPane;
	}
	
	private JPanel getTweetViewPanel() {
		if (tweetViewPanel == null) {
			tweetViewPanel = new JPanel();
			GroupLayout layout = new GroupLayout(tweetViewPanel);
			tweetViewPanel.setLayout(layout);
			layout.setVerticalGroup( //
				layout.createSequentialGroup() //
					.addGroup(layout.createParallelGroup(Alignment.LEADING) // 
						.addComponent(getTweetViewSourceLabel(), Alignment.LEADING) //
						.addComponent(getTweetViewDateLabel(), Alignment.LEADING)) //
					.addContainerGap().addComponent(getTweetViewScrollPane()));
			layout.setHorizontalGroup( //
				layout.createParallelGroup(Alignment.LEADING) //
					.addGroup(layout.createSequentialGroup() // 
						.addComponent(getTweetViewSourceLabel()) //
						.addPreferredGap(ComponentPlacement.RELATED).addContainerGap(8, Short.MAX_VALUE) //
						.addComponent(getTweetViewDateLabel())) //
					.addComponent(getTweetViewScrollPane()));
		}
		return tweetViewPanel;
	}
	
	private JScrollPane getTweetViewScrollPane() {
		if (tweetViewScrollPane == null) {
			tweetViewScrollPane = new JScrollPane();
			tweetViewScrollPane.getViewport().setView(getTweetViewEditorPane());
			tweetViewScrollPane.getVerticalScrollBar().setUnitIncrement(
					configProperties.getInteger("client.main.list.scroll"));
		}
		return tweetViewScrollPane;
	}
	
	private JLabel getTweetViewSourceLabel() {
		if (tweetViewSourceLabel == null) {
			tweetViewSourceLabel = new JLabel();
			tweetViewSourceLabel.setText("@twclient (仮の名前＠だれか名前考えて)");
			tweetViewSourceLabel.setToolTipText("from 暗黒の調和師");
		}
		return tweetViewSourceLabel;
	}
	
	@Override
	public Twitter getTwitter() {
		return twitter;
	}
	
	/**
	* Actionをhandleする。
	* 
	* @param name Action名
	* @param statusData ステータス情報
	*/
	protected void handleAction(String name, StatusData statusData) {
		ActionHandler actionHandler = getActionHandler(name);
		if (actionHandler == null) {
			logger.warn("ActionHandler {} is not found.", name);
		} else {
			logger.trace("ActionHandler {} called.", name);
			actionHandler.handleAction(name, statusData, this);
		}
	}
	
	/**
	 * 例外を処理する。
	 * 
	 * @param ex 例外
	 */
	@Override
	public void handleException(Exception ex) {
		if (ex instanceof TwitterException) {
			handleException((TwitterException) ex);
			//TODO
		} else {
			logger.warn(null, ex);
		}
	}
	
	/**
	 * 例外を処理する。
	 * @param e 例外
	 */
	@Override
	public void handleException(TwitterException e) {
		Date date = new Date(System.currentTimeMillis() + 10000);
		logger.warn(null, e);
		StatusData information = new StatusData(e, date);
		information.foregroundColor = Color.RED;
		information.backgroundColor = Color.BLACK;
		information.image = new JLabel();
		information.sentBy = new JLabel("ERROR!");
		information.sentBy.setName("!sys.ex.TwitterException");
		String errorMessage = e.getErrorMessage();
		information.data =
				new JLabel(errorMessage == null ? e.getLocalizedMessage() : errorMessage + ": "
						+ postDataTextArea.getText());
		addStatus(information);
	}
	
	/**
	 * アクションハンドラーテーブルを初期化する。
	 * @param table アクションハンドラーテーブル
	 */
	private void initActionHandlerTable(Hashtable<String, ActionHandler> table) {
		table.put("reply", new ReplyActionHandler());
		table.put("qt", new QuoteTweetActionHandler());
		table.put("rt", new RetweetActionHandler());
		table.put("fav", new FavoriteActionHandler());
		table.put("remove", new RemoveTweetActionHandler());
		table.put("userinfo", new UserInfoViewActionHandler());
		table.put("url", new UrlActionHandler());
		table.put("menu_quit", new MenuQuitActionHandler());
		table.put("menu_propeditor", new MenuPropertyEditorActionHandler());
	}
	
	/**
	 * This method is called from within the constructor to
	 * initialize the form.
	 */
	private void initComponents() {
		setTitle(APPLICATION_NAME);
		setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
		
		addWindowListener(new WindowAdapter() {
			
			@Override
			public void windowClosed(WindowEvent e) {
				synchronized (mainThreadHolder) {
					mainThreadHolder.notifyAll();
				}
			}
		});
		
		GroupLayout layout = new GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(
				getSplitPane1(), GroupLayout.DEFAULT_SIZE, 553, Short.MAX_VALUE));
		layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(getSplitPane1(),
				GroupLayout.DEFAULT_SIZE, 324, Short.MAX_VALUE));
		
		fontHeight = getSortedPostListPanel().getFontMetrics(DEFAULT_FONT).getHeight();
		
		pack();
		
		setJMenuBar(getClientMenuBar());
		setSize(500, 500);
	}
	
	/**
	 * nl-&gt;br および 空白を &amp;nbsp;に置き換えする
	 * 
	 * @param text テキスト
	 * @return &lt;br&gt;に置き換えられた文章
	 */
	private String nl2br(String text) {
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
	
	private void postActionButtonMouseClicked(final ActionEvent e) {
		if (postDataTextArea.getText().isEmpty() == false) {
			if (selectingPost != null) {
				selectingPost.requestFocusInWindow();
			}
			postActionButton.setEnabled(false);
			postDataTextArea.setEnabled(false);
			
			addJob(Priority.HIGH, new Runnable() {
				
				@Override
				public void run() {
					try {
						StatusUpdate statusUpdate = new StatusUpdate(postDataTextArea.getText());
						if (inReplyToStatus != null) {
							statusUpdate.setInReplyToStatusId(inReplyToStatus.getId());
						}
						twitter.updateStatus(statusUpdate);
						postDataTextArea.setText("");
						inReplyToStatus = null;
					} catch (TwitterException e) {
						handleException(e);
					} finally {
						try {
							EventQueue.invokeAndWait(new Runnable() {
								
								@Override
								public void run() {
									postActionButton.setEnabled(true);
									postDataTextArea.setEnabled(true);
								}
							});
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (InvocationTargetException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			});
		}
	}
	
	/**
	 * ステータスを削除する。
	 * 
	 * @param statusData ステータスデータ
	 */
	@Override
	public void removeStatus(final StatusData statusData) {
		try {
			for (StatusPanel panel : statusData.getStatusPanels()) {
				sortedPostListPanel.remove(panel);
			}
		} catch (RuntimeException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * ステータスを削除する。
	 * 
	 * @param statusData ステータスデータ
	 * @param delay 遅延 (ms)
	 */
	@Override
	public void removeStatus(final StatusData statusData, int delay) {
		timer.schedule(new TimerTask() {
			
			@Override
			public void run() {
				removeStatus(statusData);
			}
		}, delay);
	}
	
	/**
	 * inReplyToStatusを付加する。
	 * 
	 * @param status ステータス
	 * @return 前設定されたinReplyToStatus
	 */
	@Override
	public Status setInReplyToStatus(Status status) {
		Status previousInReplyToStatus = inReplyToStatus;
		inReplyToStatus = status;
		return previousInReplyToStatus;
	}
	
	@Override
	public String setPostText(String text) {
		return setPostText(text, text.length(), text.length());
	}
	
	@Override
	public String setPostText(String text, int selectionStart, int selectionEnd) {
		JTextArea textArea = getPostDataTextArea();
		String oldText = textArea.getText();
		textArea.setText(text);
		textArea.select(selectionStart, selectionEnd);
		return oldText;
	}
	
	/**
	 * 開始する
	 */
	public void start() {
		EventQueue.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				setVisible(true);
				logger.info("visibled");
			}
		});
		stream.user();
		jobQueue.addJob(new Runnable() {
			
			@Override
			public void run() {
				ResponseList<Status> homeTimeline;
				try {
					Paging paging =
							new Paging().count(configProperties.getInteger("client.main.page.initial_timeline"));
					homeTimeline = twitter.getHomeTimeline(paging);
					for (Status status : homeTimeline) {
						addStatus(status);
					}
				} catch (TwitterException e) {
					handleException(e);
				}
			}
		});
		timer.schedule(new TimerTask() {
			
			@Override
			public void run() {
				jobQueue.addJob(new Runnable() {
					
					@Override
					public void run() {
						Paging paging = new Paging().count(configProperties.getInteger("client.main.page.timeline"));
						ResponseList<Status> timeline;
						try {
							timeline = twitter.getHomeTimeline(paging);
							for (Status status : timeline) {
								addStatus(status);
							}
						} catch (TwitterException e) {
							handleException(e);
						}
					}
				});
			}
		}, configProperties.getInteger("client.main.interval.timeline"),
				configProperties.getInteger("client.main.interval.timeline"));
		jobWorkerThread = new JobWorkerThread(jobQueue);
		jobWorkerThread.start();
	}
}

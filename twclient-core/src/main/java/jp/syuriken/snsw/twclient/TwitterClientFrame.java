package jp.syuriken.snsw.twclient;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.SystemTray;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
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
import javax.swing.KeyStroke;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.text.ViewFactory;
import javax.swing.text.html.HTMLEditorKit;

import jp.syuriken.snsw.twclient.JobQueue.Priority;
import jp.syuriken.snsw.twclient.config.ActionButtonConfigType;
import jp.syuriken.snsw.twclient.config.BooleanConfigType;
import jp.syuriken.snsw.twclient.config.ConfigFrameBuilder;
import jp.syuriken.snsw.twclient.config.IntegerConfigType;
import jp.syuriken.snsw.twclient.handler.ClearPostBoxActionHandler;
import jp.syuriken.snsw.twclient.handler.FavoriteActionHandler;
import jp.syuriken.snsw.twclient.handler.PostActionHandler;
import jp.syuriken.snsw.twclient.handler.QuoteTweetActionHandler;
import jp.syuriken.snsw.twclient.handler.RemoveTweetActionHandler;
import jp.syuriken.snsw.twclient.handler.ReplyActionHandler;
import jp.syuriken.snsw.twclient.handler.RetweetActionHandler;
import jp.syuriken.snsw.twclient.handler.UnofficialRetweetActionHandler;
import jp.syuriken.snsw.twclient.handler.UrlActionHandler;
import jp.syuriken.snsw.twclient.handler.UserInfoViewActionHandler;
import jp.syuriken.snsw.twclient.internal.DefaultTweetLengthCalculator;
import jp.syuriken.snsw.twclient.internal.HTMLFactoryDelegator;
import jp.syuriken.snsw.twclient.internal.MomemtumScroller;
import jp.syuriken.snsw.twclient.internal.MomemtumScroller.BoundsTranslator;
import jp.syuriken.snsw.twclient.internal.TwitterRunnable;

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

/**
 * twclientのメインウィンドウ
 * @author $Author$
 */
@SuppressWarnings("serial")
/*package*/final class TwitterClientFrame extends javax.swing.JFrame implements WindowListener, ClientFrameApi {
	
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
	
	private class ConfigData implements PropertyChangeListener {
		
		private static final String PROPERTY_INTERVAL_TIMELINE = "twitter.interval.timeline";
		
		private static final String PROPERTY_PAGING_TIMELINE = "twitter.page.timeline";
		
		private static final String PROPERTY_PAGING_INITIAL_TIMELINE = "twitter.page.initial_timeline";
		
		private static final String PROPERTY_INTERVAL_POSTLIST_UPDATE = "gui.interval.list_update";
		
		private static final String PROPERTY_LIST_SCROLL = "gui.list.scroll";
		
		private static final String PROPERTY_COLOR_FOCUS_LIST = "gui.color.list.focus";
		
		private static final String PROPERTY_ID_STRICT_MATCH = "core.id_strict_match";
		
		private static final String PROPERTY_INFO_SURVIVE_TIME = "core.info.survive_time";
		
		public int intervalOfPostListUpdate = configProperties.getInteger(PROPERTY_INTERVAL_POSTLIST_UPDATE);
		
		public int intervalOfGetTimeline = configProperties.getInteger(PROPERTY_INTERVAL_TIMELINE);
		
		public Color colorOfFocusList = configProperties.getColor(PROPERTY_COLOR_FOCUS_LIST);
		
		public Paging pagingOfGettingTimeline = new Paging().count(configProperties
			.getInteger(PROPERTY_PAGING_TIMELINE));
		
		public Paging pagingOfGettingInitialTimeline = new Paging().count(configProperties
			.getInteger(PROPERTY_PAGING_INITIAL_TIMELINE));
		
		public boolean mentionIdStrictMatch = configProperties.getBoolean(PROPERTY_ID_STRICT_MATCH);
		
		public int scrollAmount = configProperties.getInteger(PROPERTY_LIST_SCROLL);
		
		public int timeOfSurvivingInfo = configProperties.getInteger(PROPERTY_INFO_SURVIVE_TIME);
		
		
		public ConfigData() {
			configProperties.addPropertyChangedListener(this);
		}
		
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			String name = evt.getPropertyName();
			if (Utility.equalString(name, PROPERTY_INTERVAL_POSTLIST_UPDATE)) {
				intervalOfPostListUpdate = configProperties.getInteger(PROPERTY_INTERVAL_POSTLIST_UPDATE);
			} else if (Utility.equalString(name, PROPERTY_INTERVAL_TIMELINE)) {
				intervalOfGetTimeline = configProperties.getInteger(PROPERTY_INTERVAL_TIMELINE);
			} else if (Utility.equalString(name, PROPERTY_COLOR_FOCUS_LIST)) {
				colorOfFocusList = configProperties.getColor(PROPERTY_COLOR_FOCUS_LIST);
			} else if (Utility.equalString(name, PROPERTY_PAGING_TIMELINE)) {
				pagingOfGettingTimeline = new Paging().count(configProperties.getInteger(PROPERTY_PAGING_TIMELINE));
			} else if (Utility.equalString(name, PROPERTY_PAGING_INITIAL_TIMELINE)) {
				pagingOfGettingInitialTimeline =
						new Paging().count(configProperties.getInteger(PROPERTY_PAGING_INITIAL_TIMELINE));
			} else if (Utility.equalString(name, PROPERTY_ID_STRICT_MATCH)) {
				mentionIdStrictMatch = configProperties.getBoolean(PROPERTY_ID_STRICT_MATCH);
			} else if (Utility.equalString(name, PROPERTY_LIST_SCROLL)) {
				scrollAmount = configProperties.getInteger(PROPERTY_LIST_SCROLL);
			} else if (Utility.equalString(name, PROPERTY_INFO_SURVIVE_TIME)) {
				timeOfSurvivingInfo = configProperties.getInteger(PROPERTY_INFO_SURVIVE_TIME);
			}
		}
	}
	
	/**
	 * "core!*" アクションハンドラ
	 * 
	 * @author $Author$
	 */
	public class CoreFrameActionHandler implements ActionHandler {
		
		@Override
		public JMenuItem createJMenuItem(String commandName) {
			return null;
		}
		
		@Override
		public void handleAction(String actionName, StatusData statusData, ClientFrameApi api) {
			if (Utility.equalString(actionName, "core!focusinput")) {
				getPostBox().requestFocusInWindow();
			} else if (Utility.equalString(actionName, "core!focuslist")) {
				if (selectingPost == null) {
					sortedPostListPanel.requestFocusFirstComponent();
				} else {
					selectingPost.requestFocusInWindow();
				}
			} else if (Utility.equalString(actionName, "core!postnext")) {
				if (selectingPost == null) {
					sortedPostListPanel.requestFocusFirstComponent();
				} else {
					sortedPostListPanel.requestFocusNextOf(selectingPost);
				}
			} else if (Utility.equalString(actionName, "core!postprev")) {
				if (selectingPost == null) {
					sortedPostListPanel.requestFocusFirstComponent();
				} else {
					sortedPostListPanel.requestFocusPreviousOf(selectingPost);
				}
			} else if (Utility.equalString(actionName, "core!version")) {
				VersionInfoFrame frame = new VersionInfoFrame();
				frame.setVisible(true);
			} else {
				logger.warn("[core] {} is not command", actionName);
			}
		}
		
		@Override
		public void popupMenuWillBecomeVisible(JMenuItem menuItem, StatusData statusData, ClientFrameApi api) {
		}
		
	}
	
	/**
	 * 設定フレームを表示するアクションハンドラ
	 * 
	 * @author $Author$
	 */
	private class MenuConfiguratorActionHandler implements ActionHandler {
		
		private Method showMethod;
		
		
		private MenuConfiguratorActionHandler() {
			try {
				showMethod = ConfigFrameBuilder.class.getDeclaredMethod("show");
				showMethod.setAccessible(true);
			} catch (SecurityException e) {
				logger.error("blocked reflection of ConfigFrameBuilder#show()", e);
			} catch (NoSuchMethodException e) {
				logger.error("failed reflection of ConfigFrameBuilder#show()", e);
				e.printStackTrace();
			}
		}
		
		@Override
		public JMenuItem createJMenuItem(String commandName) {
			return null;
		}
		
		@Override
		public void handleAction(String actionName, StatusData statusData, ClientFrameApi api) {
			try {
				showMethod.invoke(configuration.getConfigBuilder());
			} catch (IllegalArgumentException e) {
				logger.warn("failed invocation of ConfigFrameBuilder#show()", e);
			} catch (IllegalAccessException e) {
				logger.warn("failed invocation of ConfigFrameBuilder#show()", e);
			} catch (InvocationTargetException e) {
				logger.warn("failed invocation of ConfigFrameBuilder#show()", e);
			}
		}
		
		@Override
		public void popupMenuWillBecomeVisible(JMenuItem menuItem, StatusData statusData, ClientFrameApi api) {
			// This is always enabled.
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
	
	private class PostListListener implements MouseListener, FocusListener, KeyListener {
		
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
			handleShortcutKey("list", e);
		}
		
		@Override
		public void keyReleased(KeyEvent e) {
			handleShortcutKey("list", e);
		}
		
		@Override
		public void keyTyped(KeyEvent e) {
		}
		
		@Override
		public void mouseClicked(MouseEvent e) {
			e.getComponent().requestFocusInWindow();
			if (e.isPopupTrigger()) {
				selectingPost = (StatusPanel) e.getComponent();
			}
			if (e.getClickCount() == 2) {
				StatusPanel panel = ((StatusPanel) e.getComponent());
				handleAction("reply", panel.getStatusData());
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
						if (postListAddQueue.isEmpty()) {
							return;
						}
						int size = postListAddQueue.size();
						sortedPostListPanel.add(postListAddQueue);
						Point viewPosition = postListScrollPane.getViewport().getViewPosition();
						if (viewPosition.y < ICON_SIZE.height + PADDING_OF_POSTLIST) {
							postListScrollPane.getViewport().setViewPosition(new Point(viewPosition.x, 0));
						} else {
							postListScrollPane.getViewport().setViewPosition(
									new Point(viewPosition.x, viewPosition.y + (ICON_SIZE.height + PADDING_OF_POSTLIST)
											* size));
						}
					}
				}
			});
		}
	}
	
	
	/** ポストの最小サイズ */
	private static final int POSTLIST_MIN_SIZE = 18;
	
	/** パディング */
	private static final int PADDING_OF_POSTLIST = 1;
	
	/** アプリケーション名 */
	public static final String APPLICATION_NAME = "Astarotte";
	
	private StatusPanel selectingPost;
	
	private Hashtable<String, ActionHandler> actionHandlerTable;
	
	private Status inReplyToStatus = null;
	
	private final JobQueue jobQueue = new JobQueue();
	
	private JobWorkerThread jobWorkerThread;
	
	private JPanel editPanel;
	
	private JPanel postPanel;
	
	private JScrollPane postBoxScrollPane;
	
	private JScrollPane postListScrollPane;
	
	private JSplitPane jSplitPane1;
	
	private JButton postActionButton;
	
	private JTextArea postBox;
	
	private TwitterStream stream;
	
	private SortedPostListPanel sortedPostListPanel;
	
	private TreeMap<String, ArrayList<StatusData>> listItems;
	
	private TreeMap<Long, StatusData> statusMap;
	
	private Twitter twitter;
	
	/** デフォルトフォント: TODO from config */
	public static final Font DEFAULT_FONT = new Font(Font.MONOSPACED, Font.PLAIN, 12);
	
	/** UIフォント: TODO from config */
	public static final Font UI_FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 11);
	
	/** フォントの高さ */
	private int fontHeight;
	
	private PostListListener postListListenerSingleton = new PostListListener();
	
	private final Dimension ICON_SIZE;
	
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
	
	private JLabel tweetViewUserIconLabel;
	
	private Map<String, String> shortcutKeyMap = new HashMap<String, String>();
	
	private ConfigData configData;
	
	private JLabel postLengthLabel;
	
	private final TweetLengthCalculator DEFAULT_TWEET_LENGTH_CALCULATOR = new DefaultTweetLengthCalculator(this);
	
	private TweetLengthCalculator tweetLengthCalculator = DEFAULT_TWEET_LENGTH_CALCULATOR;
	
	private MomemtumScroller kineticScroller;
	
	
	/** 
	 * Creates new form TwitterClientFrame 
	 * @param configuration 設定
	 * @param threadHolder スレッドホルダ
	 */
	public TwitterClientFrame(ClientConfiguration configuration, Object threadHolder) {
		logger.info("initializing frame");
		this.configuration = configuration;
		configuration.setFrameApi(this);
		initConfigurator();
		jobWorkerThread = new JobWorkerThread(jobQueue);
		jobWorkerThread.start();
		mainThreadHolder = threadHolder;
		configProperties = configuration.getConfigProperties();
		configData = new ConfigData();
		timer = new Timer("timer");
		actionHandlerTable = new Hashtable<String, ActionHandler>();
		initActionHandlerTable();
		initShortcutKey();
		
		listItems = new TreeMap<String, ArrayList<StatusData>>();
		statusMap = new TreeMap<Long, StatusData>();
		twitter = new TwitterFactory(configuration.getTwitterConfiguration()).getInstance();
		getLoginUser();
		getPopupMenu();
		initComponents();
		updatePostListDispatcher = new UpdatePostList();
		timer.schedule(updatePostListDispatcher, configData.intervalOfPostListUpdate,
				configData.intervalOfPostListUpdate);
		
		timer.schedule(new TimerTask() {
			
			@Override
			public void run() {
				synchronized (postListAddQueue) {
					logger.debug(sortedPostListPanel.toString());
				}
			}
		}, 10000, 10000);
		imageCacher = new ImageCacher(configuration);
		
		stream = new TwitterStreamFactory(configuration.getTwitterConfiguration()).getInstance();
		stream.addConnectionLifeCycleListener(new ClientConnectionLifeCycleListner(this));
		stream.addListener(new ClientStreamListener(this));
		
		fontMetrics = getFontMetrics(DEFAULT_FONT);
		int str12width = fontMetrics.stringWidth("0123456789abc");
		fontHeight = fontMetrics.getHeight();
		linePanelSizeOfSentBy = new Dimension(str12width, fontHeight);
		ICON_SIZE = new Dimension(64, fontHeight < POSTLIST_MIN_SIZE ? POSTLIST_MIN_SIZE : fontHeight);
		logger.debug("{}", linePanelSizeOfSentBy);
		logger.info("initialized");
		kineticScroller = new MomemtumScroller(getPostListScrollPane(), new BoundsTranslator() {
			
			@Override
			public Rectangle translate(JComponent component) {
				return sortedPostListPanel.getBoundsOf((StatusPanel) component);
			}
		});
	}
	
	@Override
	public ActionHandler addActionHandler(String name, ActionHandler handler) {
		return actionHandlerTable.put(name, handler);
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
	
	@Override
	public void addShortcutKey(String keyString, String actionName) {
		shortcutKeyMap.put(keyString, actionName);
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
		StatusData statusData = new StatusData(twitterStatus, twitterStatus.getCreatedAt(), twitterStatus.getId());
		
		Status status;
		if (twitterStatus.isRetweet()) {
			status = twitterStatus.getRetweetedStatus();
		} else {
			status = twitterStatus;
		}
		User user = status.getUser();
		if (configData.mentionIdStrictMatch) {
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
		
		if (twitterStatus.isRetweet()) {
			StringBuilder stringBuilder = new StringBuilder();
			stringBuilder.append("Retweeted by @").append(twitterStatus.getUser().getScreenName());
			statusData.tooltip = stringBuilder.toString();
		}
		
		if (twitterStatus.isRetweet()) {
			statusData.foregroundColor = Color.GREEN;
		} else {
			UserMentionEntity[] userMentionEntities = status.getUserMentionEntities();
			boolean mentioned = false;
			for (UserMentionEntity userMentionEntity : userMentionEntities) {
				if (configData.mentionIdStrictMatch) {
					if (userMentionEntity.getId() == getLoginUser().getId()) {
						mentioned = true;
						break;
					}
				} else {
					if (userMentionEntity.getScreenName().startsWith(loginUser.getScreenName())) {
						mentioned = true;
						break;
					}
				}
			}
			if (mentioned) {
				statusData.foregroundColor = Color.RED;
				configuration.getUtility().sendNotify(user.getName(), twitterStatus.getText(),
						imageCacher.getImageFile(user));
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
		statusData.image.setForeground(statusData.foregroundColor);
		statusData.sentBy.setForeground(statusData.foregroundColor);
		statusData.data.setForeground(statusData.foregroundColor);
		Dimension size =
				new Dimension(ICON_SIZE.width + linePanelSizeOfSentBy.width + dataWidth + 3 * 2, ICON_SIZE.height
						+ PADDING_OF_POSTLIST);
		linePanel.setMinimumSize(size);
		linePanel.setPreferredSize(size);
		linePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, ICON_SIZE.height + PADDING_OF_POSTLIST));
		linePanel.setFocusable(true);
		linePanel.setToolTipText(statusData.tooltip);
		linePanel.addMouseListener(postListListenerSingleton);
		linePanel.addFocusListener(postListListenerSingleton);
		linePanel.addKeyListener(postListListenerSingleton);
		linePanel.setAlignmentY(JPanel.CENTER_ALIGNMENT);
		
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
	
	@Override
	public void doPost() {
		if (postActionButton.isEnabled() && postBox.getText().isEmpty() == false) {
			if (selectingPost != null) {
				selectingPost.requestFocusInWindow();
			}
			final String text = tweetLengthCalculator.getShortenedText(getPostBox().getText());
			postActionButton.setEnabled(false);
			postBox.setEnabled(false);
			
			addJob(Priority.HIGH, new ParallelRunnable() {
				
				@Override
				public void run() {
					try {
						// String escapedText = HTMLEntity.escape(text);
						String escapedText = text;
						StatusUpdate statusUpdate = new StatusUpdate(escapedText);
						
						if (inReplyToStatus != null) {
							statusUpdate.setInReplyToStatusId(inReplyToStatus.getId());
						}
						twitter.updateStatus(statusUpdate);
						postBox.setText("");
						updatePostLength();
						inReplyToStatus = null;
					} catch (TwitterException e) {
						handleException(e);
					} finally {
						try {
							Runnable enabler = new Runnable() {
								
								@Override
								public void run() {
									postActionButton.setEnabled(true);
									postBox.setEnabled(true);
									tweetLengthCalculator = DEFAULT_TWEET_LENGTH_CALCULATOR;
								}
							};
							if (EventQueue.isDispatchThread()) {
								enabler.run();
							} else {
								EventQueue.invokeAndWait(enabler);
							}
						} catch (InterruptedException e) {
							// do nothing
						} catch (InvocationTargetException e) {
							logger.warn("doPost", e);
						}
					}
				}
			});
		}
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
			selectingPost.setBackground(selectingPost.getStatusData().backgroundColor);
		}
		selectingPost = (StatusPanel) e.getComponent();
		selectingPost.setBackground(Utility.blendColor(selectingPost.getStatusData().backgroundColor,
				configData.colorOfFocusList));
		
		JEditorPane editor = getTweetViewEditorPane();
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
			editor.setText(stringBuilder.toString());
			
			User user = status.getUser();
			JLabel viewSourceLabel = getTweetViewSourceLabel();
			viewSourceLabel.setText(MessageFormat.format("@{0} ({1})", user.getScreenName(), user.getName()));
			viewSourceLabel.setToolTipText(MessageFormat.format("{0}tweets, {1}follow/{2}follower, {3}",
					user.getStatusesCount(), user.getFriendsCount(), user.getFollowersCount(), user.getDescription()));
			
			JLabel viewDateLabel = getTweetViewDateLabel();
			viewDateLabel.setText(dateFormat.format(status.getCreatedAt()));
			
			String source = status.getSource();
			int tagIndexOf = source.indexOf('>');
			int tagLastIndexOf = source.lastIndexOf('<');
			viewDateLabel.setToolTipText(MessageFormat.format("from {0}",
					source.substring(tagIndexOf + 1, tagLastIndexOf == -1 ? source.length() : tagLastIndexOf)));
			
		} else {
			editor.setText(statusData.data.getText());
			getTweetViewSourceLabel().setText(statusData.sentBy.getName());
			getTweetViewDateLabel().setText(dateFormat.format(statusData.date));
		}
		// if (statusData.image instanceof JLabel) {
		Icon icon = ((JLabel) statusData.image).getIcon();
		getTweetViewUserIconLabel().setIcon(icon);
		// } else {
		// 	getTweetViewUserIconLabel().setIcon(null);
		// }
	}
	
	@Override
	public void focusPostBox() {
		getPostBox().requestFocusInWindow();
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
	
	@Override
	public ClientConfiguration getClientConfiguration() {
		return configuration;
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
				JMenuItem configMenuItem = new JMenuItem("設定(C)", KeyEvent.VK_C);
				configMenuItem.setActionCommand("menu_config");
				configMenuItem.addActionListener(new ActionListenerImplementation());
				applicationMenu.add(configMenuItem);
				
				applicationMenu.addSeparator();
				
				JMenuItem quitMenuItem = new JMenuItem("終了(Q)", KeyEvent.VK_Q);
				quitMenuItem.setActionCommand("menu_quit");
				quitMenuItem.addActionListener(new ActionListenerImplementation());
				applicationMenu.add(quitMenuItem);
				clientMenu.add(applicationMenu);
			}
			{
				JMenu infoMenu = new JMenu("情報");
				JMenuItem versionMenuItem = new JMenuItem("バージョン情報(V)", KeyEvent.VK_V);
				versionMenuItem.setActionCommand("core!version");
				versionMenuItem.addActionListener(new ActionListenerImplementation());
				infoMenu.add(versionMenuItem);
				clientMenu.add(infoMenu);
			}
		}
		return clientMenu;
	}
	
	@Override
	public Font getDefaultFont() {
		return DEFAULT_FONT;
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
								.addComponent(getPostPanel(), 64, 64, GroupLayout.PREFERRED_SIZE)
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addComponent(getTweetViewPanel(), 64, 64, Short.MAX_VALUE)));
			
		}
		return editPanel;
	}
	
	@Override
	public ImageCacher getImageCacher() {
		return imageCacher;
	}
	
	/**
	 * 一時的な情報を追加するときに、この時間たったら削除してもいーよ的な時間を取得する。
	 * 若干重要度が高いときは *2 とかしてみよう！
	 * 
	 * @return 一時的な情報が生き残る時間
	 */
	@Override
	public int getInfoSurviveTime() {
		return configData.timeOfSurvivingInfo;
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
					doPost();
				}
			});
			
		}
		return postActionButton;
	}
	
	private JTextArea getPostBox() {
		if (postBox == null) {
			postBox = new javax.swing.JTextArea();
			postBox.setColumns(20);
			postBox.setRows(3);
			postBox.setFont(UI_FONT);
			postBox.addKeyListener(new KeyAdapter() {
				
				@Override
				public void keyPressed(KeyEvent e) {
					handleShortcutKey("postbox", e);
				}
				
				@Override
				public void keyReleased(KeyEvent e) {
					updatePostLength();
					handleShortcutKey("postbox", e);
				}
			});
		}
		return postBox;
	}
	
	private JScrollPane getPostBoxScrollPane() {
		if (postBoxScrollPane == null) {
			postBoxScrollPane = new JScrollPane();
			postBoxScrollPane.setViewportView(getPostBox());
		}
		return postBoxScrollPane;
	}
	
	private JLabel getPostLengthLabel() {
		if (postLengthLabel == null) {
			postLengthLabel = new JLabel();
			postLengthLabel.setText("0");
			postLengthLabel.setFont(UI_FONT);
		}
		return postLengthLabel;
	}
	
	private JScrollPane getPostListScrollPane() {
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
			postListScrollPane.getViewport().setView(getSortedPostListPanel());
			postListScrollPane.getVerticalScrollBar().setUnitIncrement(configData.scrollAmount);
			
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
								.addComponent(getPostBoxScrollPane(), GroupLayout.DEFAULT_SIZE, 475, Short.MAX_VALUE)
								.addPreferredGap(ComponentPlacement.RELATED)
								//
								.addGroup(
										layout.createParallelGroup(Alignment.TRAILING)
											.addComponent(getPostActionButton()).addComponent(getPostLengthLabel()))
								.addGap(18, 18, 18)));
			layout.setVerticalGroup( //
				layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(
						layout
							.createSequentialGroup()
							.addGroup(
									layout
										.createParallelGroup(GroupLayout.Alignment.TRAILING)
										.addGroup(
												layout.createSequentialGroup().addComponent(getPostLengthLabel())
													.addComponent(getPostActionButton()))
										.addComponent(getPostBoxScrollPane(), 32, 64, GroupLayout.PREFERRED_SIZE))
							.addContainerGap(6, Short.MAX_VALUE)));
			
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
		return getPostBox().getText();
	}
	
	/**
	 * ソート済みリストパネルを取得する。将来的にはマルチタブになる予定なのでこのメソッドは廃止される予定です。
	 * 
	 * @return the sortedPostListPanel
	 */
	public SortedPostListPanel getSortedPostListPanel() {
		if (sortedPostListPanel == null) {
			sortedPostListPanel =
					new SortedPostListPanel(configProperties.getInteger("core.postlist.split_size"),
							configProperties.getInteger("core.postlist.max_size"));
			
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
			String pos = configProperties.getProperty("gui.main.split.pos");
			if (pos != null) {
				jSplitPane1.setDividerLocation(Integer.parseInt(pos));
			} else {
				jSplitPane1.setDividerLocation(-1);
			}
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
			tweetViewEditorPane.setEditorKit(new HTMLEditorKit() {
				
				private HTMLFactory viewFactory = new HTMLFactoryDelegator();
				
				
				@Override
				public ViewFactory getViewFactory() {
					return viewFactory;
				}
			});
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
								configuration.getUtility().openBrowser(url);
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
				layout
					.createSequentialGroup()
					.addGroup(layout.createParallelGroup(Alignment.LEADING) // 
						.addComponent(getTweetViewSourceLabel(), Alignment.LEADING) //
						.addComponent(getTweetViewDateLabel(), Alignment.LEADING))
					.addContainerGap()
					.addGroup(
							layout
								.createParallelGroup(Alignment.LEADING)
								.addGroup(
										layout.createSequentialGroup()
										
										.addComponent(getTweetViewUserIconLabel(), GroupLayout.PREFERRED_SIZE, 48,
												GroupLayout.PREFERRED_SIZE))
								.addComponent(getTweetViewScrollPane(), Alignment.LEADING)));
			layout.setHorizontalGroup( //
				layout
					.createParallelGroup(Alignment.LEADING)
					.addGroup(layout.createSequentialGroup() // 
						.addComponent(getTweetViewSourceLabel()) //
						.addPreferredGap(ComponentPlacement.RELATED).addContainerGap(8, Short.MAX_VALUE) //
						.addComponent(getTweetViewDateLabel()))
					.addGroup(
							layout
								.createSequentialGroup()
								.addGap(4, 4, 4)
								.addComponent(getTweetViewUserIconLabel(), GroupLayout.PREFERRED_SIZE, 48,
										GroupLayout.PREFERRED_SIZE)
								.addGap(4, 4, 4)
								.addComponent(getTweetViewScrollPane(), GroupLayout.PREFERRED_SIZE, 200,
										Short.MAX_VALUE)));
		}
		return tweetViewPanel;
	}
	
	private JScrollPane getTweetViewScrollPane() {
		if (tweetViewScrollPane == null) {
			tweetViewScrollPane = new JScrollPane();
			tweetViewScrollPane.getViewport().setView(getTweetViewEditorPane());
			tweetViewScrollPane.getVerticalScrollBar().setUnitIncrement(configData.scrollAmount);
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
	
	private JLabel getTweetViewUserIconLabel() {
		if (tweetViewUserIconLabel == null) {
			tweetViewUserIconLabel = new JLabel();
			tweetViewUserIconLabel.setHorizontalAlignment(JLabel.CENTER);
			tweetViewUserIconLabel.setVerticalAlignment(JLabel.CENTER);
		}
		return tweetViewUserIconLabel;
	}
	
	@Override
	public Twitter getTwitter() {
		return twitter;
	}
	
	@Override
	public Font getUiFont() {
		return UI_FONT;
	}
	
	@Override
	public Utility getUtility() {
		return configuration.getUtility();
	}
	
	@Override
	public void handleAction(String name, StatusData statusData) {
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
				new JLabel(errorMessage == null ? e.getLocalizedMessage() : errorMessage + ": " + postBox.getText());
		addStatus(information);
	}
	
	/*package*/void handleShortcutKey(String component, KeyEvent e) {
		int id = e.getID();
		if (id == KeyEvent.KEY_TYPED) {
			throw new IllegalArgumentException("KeyEvent.getID() must not be KEY_TYPED");
		}
		synchronized (shortcutKeyMap) {
			String keyString = Utility.toKeyString(e);
			String actionCommandName = shortcutKeyMap.get(component + "." + keyString);
			if (actionCommandName == null) {
				actionCommandName = shortcutKeyMap.get("all." + keyString);
			}
			if (actionCommandName != null) {
				handleAction(actionCommandName, selectingPost.getStatusData());
				e.consume();
			}
		}
	}
	
	/**
	 * アクションハンドラーテーブルを初期化する。
	 */
	private void initActionHandlerTable() {
		addActionHandler("reply", new ReplyActionHandler());
		addActionHandler("qt", new QuoteTweetActionHandler());
		addActionHandler("unofficial_rt", new UnofficialRetweetActionHandler());
		addActionHandler("rt", new RetweetActionHandler());
		addActionHandler("fav", new FavoriteActionHandler());
		addActionHandler("remove", new RemoveTweetActionHandler());
		addActionHandler("userinfo", new UserInfoViewActionHandler());
		addActionHandler("url", new UrlActionHandler());
		addActionHandler("clear", new ClearPostBoxActionHandler());
		addActionHandler("post", new PostActionHandler());
		addActionHandler("core", new CoreFrameActionHandler());
		addActionHandler("menu_quit", new MenuQuitActionHandler());
		addActionHandler("menu_propeditor", new MenuPropertyEditorActionHandler());
		addActionHandler("menu_config", new MenuConfiguratorActionHandler());
	}
	
	/**
	 * This method is called from within the constructor to
	 * initialize the form.
	 */
	private void initComponents() {
		setTitle(APPLICATION_NAME);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		addWindowListener(this);
		
		GroupLayout layout = new GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(
				getSplitPane1(), GroupLayout.DEFAULT_SIZE, 553, Short.MAX_VALUE));
		layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(getSplitPane1(),
				GroupLayout.DEFAULT_SIZE, 324, Short.MAX_VALUE));
		
		pack();
		
		setJMenuBar(getClientMenuBar());
		Dimension size = configProperties.getDimension("gui.main.size");
		if (size != null) {
			setSize(size);
			// setSize(500, 500);
		}
	}
	
	/**
	 * 設定ウィンドウの初期化
	 */
	private void initConfigurator() {
		ConfigFrameBuilder configBuilder = configuration.getConfigBuilder();
		configBuilder.getGroup("Twitter").getSubgroup("取得間隔 (秒)")
			.addConfig("twitter.interval.timeline", "タイムライン", "秒数", new IntegerConfigType(0, 3600, 1000))
			.getParentGroup().getSubgroup("取得数 (ツイート数)")
			.addConfig("twitter.page.timeline", "タイムライン", "(ツイート)", new IntegerConfigType(1, 200))
			.addConfig("twitter.page.initial_timeline", "タイムライン (起動時)", "(ツイート)", new IntegerConfigType(1, 200));
		configBuilder.getGroup("UI")
			.addConfig("gui.interval.list_update", "UI更新間隔 (ミリ秒)", "ミリ秒(ms)", new IntegerConfigType(100, 5000))
			.addConfig("gui.list.scroll", "スクロール量", null, new IntegerConfigType(1, 100));
		configBuilder
			.getGroup("core")
			.addConfig("core.info.survive_time", "一時的な情報を表示する時間 (ツイートの削除通知など)", "秒", new IntegerConfigType(1, 5, 1000))
			.addConfig("core.match.id_strict_match", "リプライ判定時のIDの厳格な一致", "チェックが入っていないときは先頭一致になります",
					new BooleanConfigType());
		configBuilder.getGroup("高度な設定").addConfig(null, "設定を直接編集する (動作保証対象外です)", null,
				new ActionButtonConfigType("プロパティーエディターを開く...", "menu_propeditor", this));
	}
	
	/**
	 * ショートカットキーテーブルを初期化する。
	 */
	protected void initShortcutKey() {
		Properties shortcutkeyProperties = new Properties();
		try {
			shortcutkeyProperties.load(getClass().getClassLoader().getResourceAsStream(
					"jp/syuriken/snsw/twclient/ui_main_shortcutkey.properties"));
		} catch (IOException e) {
			logger.error("ショートカットキーの読み込みに失敗", e);
		}
		for (Object obj : shortcutkeyProperties.keySet()) {
			String key = (String) obj;
			addShortcutKey(key, shortcutkeyProperties.getProperty(key));
		}
	}
	
	/**
	 * nl-&gt;br および 空白を &amp;nbsp;に置き換える
	 * 
	 * @param stringBuilder テキスト
	 * @param start 置き換え開始位置
	 */
	private void nl2br(StringBuilder stringBuilder, int start) {
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
	}
	
	/**
	 * nl-&gt;br および 空白を &amp;nbsp;に置き換える
	 * 
	 * @param stringBuilder テキスト
	 * @param append 追加する文字列
	 */
	private void nl2br(StringBuilder stringBuilder, String append) {
		int offset = stringBuilder.length();
		stringBuilder.append(append);
		nl2br(stringBuilder, offset);
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
		JTextArea textArea = getPostBox();
		String oldText = textArea.getText();
		textArea.setText(text);
		textArea.select(selectionStart, selectionEnd);
		updatePostLength();
		return oldText;
	}
	
	@Override
	public TweetLengthCalculator setTweetLengthCalculator(TweetLengthCalculator newCalculator) {
		TweetLengthCalculator oldCalculator = tweetLengthCalculator;
		tweetLengthCalculator = newCalculator == null ? DEFAULT_TWEET_LENGTH_CALCULATOR : newCalculator;
		return oldCalculator;
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
		jobQueue.addJob(new TwitterRunnable() {
			
			@Override
			protected void access() throws TwitterException {
				ResponseList<Status> homeTimeline;
				Paging paging = configData.pagingOfGettingInitialTimeline;
				homeTimeline = twitter.getHomeTimeline(paging);
				for (Status status : homeTimeline) {
					addStatus(status);
				}
				configuration.setInitializing(false);
			}
			
			@Override
			protected ClientConfiguration getConfiguration() {
				return configuration;
			}
			
			@Override
			protected void handleException(TwitterException ex) {
				TwitterClientFrame.this.handleException(ex);
			}
		});
		timer.schedule(new TimerTask() {
			
			@Override
			public void run() {
				jobQueue.addJob(new TwitterRunnable() {
					
					@Override
					protected void access() throws TwitterException {
						Paging paging = configData.pagingOfGettingTimeline;
						ResponseList<Status> timeline;
						timeline = twitter.getHomeTimeline(paging);
						for (Status status : timeline) {
							addStatus(status);
						}
					}
					
					@Override
					protected ClientConfiguration getConfiguration() {
						return configuration;
					}
					
					@Override
					protected void handleException(TwitterException ex) {
						TwitterClientFrame.this.handleException(ex);
					}
				});
			}
		}, configData.intervalOfGetTimeline, configData.intervalOfGetTimeline);
		if (SystemTray.isSupported()) {
			try {
				SystemTray.getSystemTray().add(configuration.getTrayIcon());
			} catch (AWTException e) {
				logger.warn("SystemTrayへの追加に失敗", e);
			}
		}
	}
	
	private void updatePostLength() {
		tweetLengthCalculator.calcTweetLength(getPostBox().getText());
	}
	
	@Override
	public void updatePostLength(String length, Color color, String tooltip) {
		JLabel label = getPostLengthLabel();
		label.setText(length);
		label.setForeground(color);
		label.setToolTipText(tooltip);
	}
	
	@Override
	public void windowActivated(WindowEvent e) {
		// do nothing
	}
	
	@Override
	public void windowClosed(WindowEvent e) {
		logger.debug("closing main-window...");
		configProperties.setDimension("gui.main.size", getSize());
		configProperties.setInteger("gui.main.split.pos", getSplitPane1().getDividerLocation());
		configProperties.store();
		if (SystemTray.isSupported()) {
			SystemTray.getSystemTray().remove(configuration.getTrayIcon());
		}
		synchronized (mainThreadHolder) {
			mainThreadHolder.notifyAll();
		}
	}
	
	@Override
	public void windowClosing(WindowEvent e) {
		// do nothing
	}
	
	@Override
	public void windowDeactivated(WindowEvent e) {
		// do nothing
	}
	
	@Override
	public void windowDeiconified(WindowEvent e) {
		// do nothing
	}
	
	@Override
	public void windowIconified(WindowEvent e) {
		// do nothing
	}
	
	@Override
	public void windowOpened(WindowEvent e) {
		// do nothing
	}
}

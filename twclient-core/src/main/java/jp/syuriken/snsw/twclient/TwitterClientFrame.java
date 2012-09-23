package jp.syuriken.snsw.twclient;

import static java.lang.Math.max;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Insets;
import java.awt.LayoutManager2;
import java.awt.SystemTray;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.ViewFactory;
import javax.swing.text.html.HTMLEditorKit;

import jp.syuriken.snsw.twclient.ClientConfiguration.ConfigData;
import jp.syuriken.snsw.twclient.JobQueue.Priority;
import jp.syuriken.snsw.twclient.config.ActionButtonConfigType;
import jp.syuriken.snsw.twclient.config.BooleanConfigType;
import jp.syuriken.snsw.twclient.config.ConfigFrameBuilder;
import jp.syuriken.snsw.twclient.config.IntegerConfigType;
import jp.syuriken.snsw.twclient.handler.ClearPostBoxActionHandler;
import jp.syuriken.snsw.twclient.handler.FavoriteActionHandler;
import jp.syuriken.snsw.twclient.handler.HashtagActionHandler;
import jp.syuriken.snsw.twclient.handler.ListActionHandler;
import jp.syuriken.snsw.twclient.handler.MuteActionHandler;
import jp.syuriken.snsw.twclient.handler.PostActionHandler;
import jp.syuriken.snsw.twclient.handler.QuoteTweetActionHandler;
import jp.syuriken.snsw.twclient.handler.RemoveTweetActionHandler;
import jp.syuriken.snsw.twclient.handler.ReplyActionHandler;
import jp.syuriken.snsw.twclient.handler.RetweetActionHandler;
import jp.syuriken.snsw.twclient.handler.SearchActionHandler;
import jp.syuriken.snsw.twclient.handler.TweetActionHandler;
import jp.syuriken.snsw.twclient.handler.UnofficialRetweetActionHandler;
import jp.syuriken.snsw.twclient.handler.UrlActionHandler;
import jp.syuriken.snsw.twclient.handler.UserInfoViewActionHandler;
import jp.syuriken.snsw.twclient.internal.DefaultTweetLengthCalculator;
import jp.syuriken.snsw.twclient.internal.HTMLFactoryDelegator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;

/**
 * twclientのメインウィンドウ
 * @author $Author$
 */
@SuppressWarnings("serial")
/*package*/class TwitterClientFrame extends javax.swing.JFrame implements WindowListener, ClientFrameApi {
	
	/**
	 * アカウント認証するアクションハンドラ
	 * 
	 * @author $Author$
	 */
	public class AccountVerifierActionHandler implements ActionHandler {
		
		@Override
		public JMenuItem createJMenuItem(String commandName) {
			return null;
		}
		
		@Override
		public void handleAction(String actionName, StatusData statusData, final ClientFrameApi frameInstance) {
			Thread thread = new Thread(new Runnable() {
				
				@Override
				public void run() {
					Exception exception = configuration.tryGetOAuthToken();
					if (exception != null) {
						JOptionPane.showMessageDialog(TwitterClientFrame.this, "認証に失敗しました: " + exception.getMessage(),
								"エラー", JOptionPane.ERROR_MESSAGE);
					}
				}
			});
			thread.start();
		}
		
		@Override
		public void popupMenuWillBecomeVisible(JMenuItem menuItem, StatusData statusData, ClientFrameApi api) {
		}
	}
	
	/**
	 * MenuItemのActionListenerの実装。
	 * 
	 * @author $Author$
	 */
	private final class ActionListenerImplementation implements ActionListener {
		
		@Override
		public void actionPerformed(ActionEvent e) {
			/* StatusData statusData;
			 if (selectingPost == null) {
			statusData = null;
			} else {
			statusData = statusMap.get(selectingPost.getStatusData().id);
			} */
			handleAction(e.getActionCommand(), null);
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
			if (Utility.equalString(actionName, "core!move_between_list_and_postbox")) {
				if (getPostBox().isFocusOwner()) {
					actionName = "core!focuslist";
				} else {
					actionName = "core!focusinput";
				}
			}
			
			if (Utility.equalString(actionName, "core!submenu")) {
				return;
			} else if (Utility.equalString(actionName, "core!version")) {
				VersionInfoFrame frame = new VersionInfoFrame();
				frame.setVisible(true);
			} else if (Utility.equalString(actionName, "core!focusinput")) {
				getPostBox().requestFocusInWindow();
			} else if (Utility.equalString(actionName, "core!tabswitch_prev")) {
				JTabbedPane tab = getViewTab();
				int selectedIndex = tab.getSelectedIndex();
				if (selectedIndex > 0) {
					tab.setSelectedIndex(selectedIndex - 1);
				}
			} else if (Utility.equalString(actionName, "core!tabswitch_next")) {
				JTabbedPane tab = getViewTab();
				int selectedIndex = tab.getSelectedIndex();
				if (selectedIndex < tab.getTabCount() - 1) {
					tab.setSelectedIndex(selectedIndex + 1);
				}
			} else {
				String messageName;
				Object arg = null;
				if (Utility.equalString(actionName, "core!focuslist")) {
					messageName = ClientMessageListener.REQUEST_FOCUS_TAB_COMPONENT;
				} else if (Utility.equalString(actionName, "core!postnext")) {
					messageName = ClientMessageListener.REQUEST_FOCUS_NEXT_COMPONENT;
				} else if (Utility.equalString(actionName, "core!postprev")) {
					messageName = ClientMessageListener.REQUEST_FOCUS_PREV_COMPONENT;
				} else if (Utility.equalString(actionName, "core!postuserprev")) {
					messageName = ClientMessageListener.REQUEST_FOCUS_USER_PREV_COMPONENT;
				} else if (Utility.equalString(actionName, "core!postusernext")) {
					messageName = ClientMessageListener.REQUEST_FOCUS_USER_NEXT_COMPONENT;
				} else if (Utility.equalString(actionName, "core!postfirst")) {
					messageName = ClientMessageListener.REQUEST_FOCUS_FIRST_COMPONENT;
				} else if (Utility.equalString(actionName, "core!postwindowfirst")) {
					messageName = ClientMessageListener.REQUEST_FOCUS_WINDOW_FIRST_COMPONENT;
				} else if (Utility.equalString(actionName, "core!postwindowlast")) {
					messageName = ClientMessageListener.REQUEST_FOCUS_WINDOW_LAST_COMPONENT;
				} else if (Utility.equalString(actionName, "core!scroll_as_windowlast")) {
					messageName = ClientMessageListener.REQUEST_SCROLL_AS_WINDOW_LAST;
				} else if (Utility.equalString(actionName, "core!jump_inReplyTo")) {
					messageName = ClientMessageListener.REQUEST_FOCUS_IN_REPLY_TO;
				} else if (Utility.equalString(actionName, "core!jump_inReplyToBack")) {
					messageName = ClientMessageListener.REQUEST_FOCUS_BACK_REPLIED_BY;
				} else {
					logger.warn("[core AH] {} is not command", actionName);
					return;
				}
				getSelectingTab().getRenderer().onClientMessage(messageName, arg);
			}
		}
		
		@Override
		public void popupMenuWillBecomeVisible(JMenuItem menuItem, StatusData statusData, ClientFrameApi api) {
			if (Utility.equalString(menuItem.getActionCommand(), "core!submenu")) {
				if (menuItem instanceof JMenu == false) {
					logger.error("\"core!submenu\" argued menuItem not as JMenu");
					throw new AssertionError();
				}
				Component[] subItems = ((JMenu) menuItem).getMenuComponents();
				for (Component subItem : subItems) {
					if (subItem instanceof JMenuItem) {
						JMenuItem subMenuItem = (JMenuItem) subItem;
						String actionCommand = subMenuItem.getActionCommand();
						getActionHandler(actionCommand).popupMenuWillBecomeVisible(subMenuItem, statusData, api);
					}
				}
			}
		}
		
	}
	
	/**
	 * JLabel および JEditorPane用
	 * 
	 * @author $Author$
	 */
	private final class DefaultMouseListener implements MouseListener {
		
		private static final String DEL_FTAG = "<!-- del -->";
		
		private static final String DEL_ETAG = "<!-- /del -->";
		
		private static final String DEL_ALL = "<!-- delbefthis -->";
		
		private static final String UNDERLINE_TAG = DEL_FTAG
				+ "<span style='text-decoration:underline' class='autoinserted'>" + DEL_ETAG;
		
		private static final String HTML_UNDERLINE_TAG =
				"<html><span style='text-decoration:underline' class='autohtmled'>" + DEL_ALL;
		
		private static final String END_TAG = DEL_FTAG + "</span>" + DEL_ETAG;
		
		
		@Override
		public void mouseClicked(MouseEvent e) {
			if (tweetViewingTab != null) {
				Component component = e.getComponent();
				String messageName;
				if (component == tweetViewCreatedAtLabel) {
					messageName = ClientMessageListener.EVENT_CLICKED_CREATED_AT;
				} else if (component == tweetViewCreatedByLabel) {
					messageName = ClientMessageListener.EVENT_CLICKED_CREATED_BY;
				} else if (component == tweetViewTextOverlayLabel) {
					messageName = ClientMessageListener.EVENT_CLICKED_OVERLAY_LABEL;
				} else {
					return;
				}
				tweetViewingTab.getRenderer().onClientMessage(messageName, e);
			}
		}
		
		@Override
		public void mouseEntered(MouseEvent e) {
			Component component = e.getComponent();
			if (component instanceof JLabel) {
				JLabel label = (JLabel) component;
				int flag;
				if (label == tweetViewCreatedAtLabel) {
					flag = tweetViewCreatedAtFlag;
				} else if (label == tweetViewCreatedByLabel) {
					flag = tweetViewCreatedByFlag;
				} else if (label == tweetViewTextOverlayLabel) {
					flag = tweetViewTextOverlayFlag;
				} else {
					return;
				}
				
				if ((flag & SET_FOREGROUND_COLOR_BLUE) != 0) {
					label.setForeground(Color.BLUE);
				}
				if ((flag & UNDERLINE) != 0) {
					StringBuilder stringBuilder = new StringBuilder(label.getText());
					if (stringBuilder.indexOf("<html>") == 0) {
						stringBuilder.insert("<html>".length(), UNDERLINE_TAG);
						stringBuilder.append(END_TAG);
					} else {
						stringBuilder.insert(0, HTML_UNDERLINE_TAG);
						stringBuilder.append(END_TAG);
					}
					label.setText(stringBuilder.toString());
				}
			}
		}
		
		@Override
		public void mouseExited(MouseEvent e) {
			Component component = e.getComponent();
			if (component instanceof JLabel) {
				JLabel label = (JLabel) e.getComponent();
				int flag;
				if (label == getTweetViewCreatedAtLabel()) {
					flag = tweetViewCreatedAtFlag;
				} else if (label == getTweetViewCreatedByLabel()) {
					flag = tweetViewCreatedByFlag;
				} else if (label == getTweetViewTextOverlayLabel()) {
					flag = tweetViewTextOverlayFlag;
				} else {
					return;
				}
				
				if ((flag & SET_FOREGROUND_COLOR_BLUE) != 0) {
					label.setForeground(Color.BLACK);
				}
				if ((flag & UNDERLINE) != 0) {
					StringBuilder stringBuilder = new StringBuilder(label.getText());
					while (true) {
						int indexOf = stringBuilder.indexOf(DEL_FTAG);
						if (indexOf == -1) {
							break;
						}
						int etagStart = stringBuilder.indexOf(DEL_ETAG, indexOf);
						if (etagStart == -1) {
							break;
						}
						stringBuilder.delete(indexOf, etagStart + DEL_ETAG.length());
					}
					{
						int indexOf = stringBuilder.indexOf(DEL_ALL);
						if (indexOf != -1) {
							stringBuilder.delete(0, indexOf + DEL_ALL.length());
						}
					}
					label.setText(stringBuilder.toString());
				}
			}
		}
		
		@Override
		public void mousePressed(MouseEvent e) {
		}
		
		@Override
		public void mouseReleased(MouseEvent e) {
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
	
	/**
	 * リログインするためのアクションハンドラ
	 * 
	 * @author $Author$
	 */
	public class ReloginActionHandler implements ActionHandler {
		
		private final boolean forWrite;
		
		
		/**
		 * インスタンスを生成する。
		 * 
		 * @param forWrite 書き込み用
		 */
		public ReloginActionHandler(boolean forWrite) {
			this.forWrite = forWrite;
		}
		
		@Override
		public JMenuItem createJMenuItem(String commandName) {
			return null;
		}
		
		@Override
		public void handleAction(String actionName, StatusData ignore, ClientFrameApi frameInstance) {
			String accountId = actionName.substring(actionName.indexOf('!') + 1);
			if (forWrite) {
				configuration.setAccountIdForWrite(accountId);
			} else {
				configuration.setAccountIdForRead(accountId);
			}
		}
		
		@Override
		public void popupMenuWillBecomeVisible(JMenuItem menuItem, StatusData statusData, ClientFrameApi api) {
			// TODO Auto-generated method stub
			
		}
	}
	
	/**
	 * ログイン出来るユーザー情報を取得する
	 * 
	 * @author $Author$
	 */
	private final class UserInfoFetcher implements Runnable {
		
		public final String[] accountList = configuration.getAccountList();
		
		public int offset = 0;
		
		public JMenuItem[] readTimelineMenuItems;
		
		public JMenuItem[] postToMenuItems;
		
		
		/**
		 * インスタンスを生成する。
		 * 
		 */
		public UserInfoFetcher() {
			readTimelineMenuItems = new JMenuItem[accountList.length];
			postToMenuItems = new JMenuItem[accountList.length];
			String defaultAccountId = configuration.getDefaultAccountId();
			ButtonGroup readButtonGroup = new ButtonGroup();
			ButtonGroup writeButtonGroup = new ButtonGroup();
			for (int i = 0; i < accountList.length; i++) {
				String accountId = accountList[i];
				
				JMenuItem readMenuItem = new JRadioButtonMenuItem(accountId);
				readMenuItem.setActionCommand("menu_login_read!" + accountId);
				readMenuItem.addActionListener(menuActionListener);
				if (accountId.equals(defaultAccountId)) {
					readMenuItem.setSelected(true);
					readMenuItem.setFont(readMenuItem.getFont().deriveFont(Font.BOLD));
				}
				readTimelineMenuItems[i] = readMenuItem;
				getReadTimelineJMenu().add(readMenuItem);
				readButtonGroup.add(readMenuItem);
				
				JMenuItem writeMenuItem = new JRadioButtonMenuItem(accountId);
				writeMenuItem.setActionCommand("menu_login_write!" + accountId);
				writeMenuItem.addActionListener(menuActionListener);
				if (accountId.equals(defaultAccountId)) {
					writeMenuItem.setSelected(true);
					writeMenuItem.setFont(writeMenuItem.getFont().deriveFont(Font.BOLD));
				}
				postToMenuItems[i] = writeMenuItem;
				getPostToJMenu().add(writeMenuItem);
				writeButtonGroup.add(writeMenuItem);
			}
		}
		
		@Override
		public void run() {
			if (offset < accountList.length) {
				int lookupUsersSize = accountList.length - offset;
				lookupUsersSize = lookupUsersSize <= 100 ? lookupUsersSize : 100;
				try {
					long[] ids = new long[lookupUsersSize];
					
					for (int i = 0; i < lookupUsersSize; i++) {
						ids[i] = Long.parseLong(accountList[offset + i]);
					}
					
					ResponseList<User> users = configuration.getTwitterForRead().lookupUsers(ids);
					
					int finish = offset + lookupUsersSize;
					for (User user : users) {
						for (int i = offset; i < finish; i++) {
							if (accountList[i].equals(String.valueOf(user.getId()))) {
								readTimelineMenuItems[i].setText(user.getScreenName());
								postToMenuItems[i].setText(user.getScreenName());
							}
						}
					}
					offset += lookupUsersSize;
				} catch (TwitterException e) {
					e.printStackTrace(); //TODO
				}
				getTimer().schedule(new TimerTask() {
					
					@Override
					public void run() {
						addJob(Priority.LOW, UserInfoFetcher.this);
					}
				}, 10000);
			}
		}
	}
	
	
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
	
	private JSplitPane jSplitPane1;
	
	private JButton postActionButton;
	
	private JTextArea postBox;
	
	private JTabbedPane viewTab;
	
	/** デフォルトフォント: TODO from config */
	public static final Font DEFAULT_FONT = new Font(Font.MONOSPACED, Font.PLAIN, 12);
	
	/** UIフォント: TODO from config */
	public static final Font UI_FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 11);
	
	private User loginUser;
	
	private Timer timer;
	
	private ClientProperties configProperties;
	
	private JMenuBar clientMenu;
	
	private final ClientConfiguration configuration;
	
	private final ActionListener menuActionListener = new ActionListenerImplementation();
	
	private JPanel tweetViewPanel;
	
	private JMenu accountMenu;
	
	private JMenu readTimelineJMenu;
	
	private JMenu postToJMenu;
	
	private JScrollPane tweetViewScrollPane;
	
	private JEditorPane tweetViewEditorPane;
	
	private JLabel tweetViewCreatedByLabel;
	
	private JLabel tweetViewCreatedAtLabel;
	
	private final Object mainThreadHolder;
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	private ImageCacher imageCacher;
	
	private JLabel tweetViewUserIconLabel;
	
	private Map<String, String> shortcutKeyMap = new HashMap<String, String>();
	
	private ConfigData configData;
	
	private FilterService rootFilterService;
	
	private JLabel postLengthLabel;
	
	protected ClientTab selectingTab;
	
	private final TweetLengthCalculator DEFAULT_TWEET_LENGTH_CALCULATOR = new DefaultTweetLengthCalculator(this);
	
	private TweetLengthCalculator tweetLengthCalculator = DEFAULT_TWEET_LENGTH_CALCULATOR;
	
	private DefaultMouseListener tweetViewListener = new DefaultMouseListener();
	
	private ClientTab tweetViewingTab;
	
	private JPanel operationPanelContainer;
	
	private JLayeredPane tweetViewTextLayeredPane;
	
	private JLabel tweetViewTextOverlayLabel;
	
	private int tweetViewCreatedByFlag;
	
	private int tweetViewCreatedAtFlag;
	
	private int tweetViewTextOverlayFlag;
	
	
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
		jobWorkerThread = new JobWorkerThread(jobQueue, configuration);
		jobWorkerThread.start();
		rootFilterService = configuration.getRootFilterService();
		mainThreadHolder = threadHolder;
		configProperties = configuration.getConfigProperties();
		configData = configuration.getConfigData();
		timer = new Timer("timer");
		actionHandlerTable = new Hashtable<String, ActionHandler>();
		initActionHandlerTable();
		initShortcutKey();
		
		getLoginUser();
		generatePopupMenu(new ActionListenerImplementation());
		initComponents();
		// timer.schedule(updatePostListDispatcher, configData.intervalOfPostListUpdate,
		//		configData.intervalOfPostListUpdate);
		
		imageCacher = new ImageCacher(configuration);
		
		logger.info("initialized");
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
	
	protected void addTab(ClientTab tab) {
		getViewTab().add(tab.getTitle(), tab.getTabComponent());
		tab.initTimeline();
	}
	
	/**
	 * 終了できるようにお掃除する
	 */
	public void cleanUp() {
		configuration.setShutdownPhase(true);
		timer.cancel();
		jobWorkerThread.cleanUp();
	}
	
	@Override
	public void clearTweetView() {
		setTweetViewText(null, null, DO_NOTHING_WHEN_POINTED);
		setTweetViewCreatedAt(null, null, DO_NOTHING_WHEN_POINTED);
		setTweetViewCreatedBy(null, null, null, DO_NOTHING_WHEN_POINTED);
		setTweetViewOperationPanel(null);
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
						configuration.getTwitterForWrite().updateStatus(statusUpdate);
						postBox.setText("");
						updatePostLength();
						inReplyToStatus = null;
					} catch (TwitterException e) {
						rootFilterService.onException(e);
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
	
	/*package*/void focusFrameTab(ClientTab tab, int index) {
		getViewTab().setSelectedIndex(index);
	}
	
	@Override
	public void focusPostBox() {
		getPostBox().requestFocusInWindow();
	}
	
	JPopupMenu generatePopupMenu(ActionListener actionListener) {
		JPopupMenu popupMenu = new JPopupMenu();
		Container nowProcessingMenu = popupMenu;
		String[] popupMenus = configProperties.getProperty("gui.menu.popup").split(" ");
		
		for (String actionCommand : popupMenus) {
			if (actionCommand.trim().isEmpty()) {
				continue;
			} else if (actionCommand.startsWith("<") && actionCommand.endsWith(">")) {
				JMenu jMenu = new JMenu(actionCommand.substring(1, actionCommand.length() - 1));
				jMenu.setActionCommand("core!submenu");
				nowProcessingMenu = jMenu;
				popupMenu.add(nowProcessingMenu);
				continue;
			}
			ActionHandler handler = getActionHandler(actionCommand);
			if (handler == null) {
				logger.warn("handler {} is not found.", actionCommand); //TODO
			} else {
				JMenuItem menuItem = handler.createJMenuItem(actionCommand);
				menuItem.setActionCommand(actionCommand);
				menuItem.addActionListener(actionListener);
				if (nowProcessingMenu instanceof JPopupMenu) {
					((JPopupMenu) nowProcessingMenu).add(menuItem);
				} else {
					((JMenu) nowProcessingMenu).add(menuItem);
				}
			}
		}
		return popupMenu;
	}
	
	private JMenu getAccountMenu() {
		if (accountMenu == null) {
			accountMenu = new JMenu("アカウント");
			
			addJob(Priority.LOW, new UserInfoFetcher());
			accountMenu.add(getReadTimelineJMenu());
			accountMenu.add(getPostToJMenu());
			
			JMenuItem verifyAccountMenuItem = new JMenuItem("アカウント認証(V)...", KeyEvent.VK_V);
			verifyAccountMenuItem.setActionCommand("menu_account_verify");
			verifyAccountMenuItem.addActionListener(menuActionListener);
			accountMenu.add(verifyAccountMenuItem);
		}
		return accountMenu;
	}
	
	@Override
	public String getActionCommandByShortcutKey(String component, String keyString) {
		synchronized (shortcutKeyMap) {
			String actionCommand = shortcutKeyMap.get(component + "." + keyString);
			if (actionCommand == null) {
				actionCommand = shortcutKeyMap.get("all" + "." + keyString);
			}
			return actionCommand;
		}
	}
	
	/**
	 * アクションハンドラを取得する
	 * 
	 * @param name アクション名。!を含んでいても可
	 * @return アクションハンドラ
	 */
	@Override
	public ActionHandler getActionHandler(String name) {
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
				quitMenuItem.addActionListener(menuActionListener);
				applicationMenu.add(quitMenuItem);
				clientMenu.add(applicationMenu);
			}
			clientMenu.add(getAccountMenu());
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
	public ConfigData getConfigData() {
		return configData;
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
								.addComponent(getTweetViewPanel(), 72, 72, Short.MAX_VALUE)));
			
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
				loginUser = configuration.getTwitterForRead().verifyCredentials();
			} catch (TwitterException e) {
				handleException(e);
			}
		}
		return loginUser;
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
	
	private JMenu getPostToJMenu() {
		if (postToJMenu == null) {
			postToJMenu = new JMenu("投稿先");
		}
		return postToJMenu;
	}
	
	private JMenu getReadTimelineJMenu() {
		if (readTimelineJMenu == null) {
			readTimelineJMenu = new JMenu("タイムライン読み込み");
		}
		return readTimelineJMenu;
	}
	
	@Override
	public ClientTab getSelectingTab() {
		return selectingTab;
	}
	
	private JSplitPane getSplitPane1() {
		if (jSplitPane1 == null) {
			jSplitPane1 = new JSplitPane();
			jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
			jSplitPane1.setTopComponent(getEditPanel());
			jSplitPane1.setRightComponent(getViewTab());
			String pos = configProperties.getProperty("gui.main.split.pos");
			if (pos != null) {
				jSplitPane1.setDividerLocation(Integer.parseInt(pos));
			} else {
				jSplitPane1.setDividerLocation(-1);
			}
		}
		return jSplitPane1;
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
	
	private JLabel getTweetViewCreatedAtLabel() {
		if (tweetViewCreatedAtLabel == null) {
			tweetViewCreatedAtLabel = new JLabel();
			tweetViewCreatedAtLabel.setText("2012/1/1 00:00:00");
			tweetViewCreatedAtLabel.setHorizontalAlignment(JLabel.RIGHT);
			tweetViewCreatedAtLabel.setAlignmentX(RIGHT_ALIGNMENT);
			tweetViewCreatedAtLabel.addMouseListener(tweetViewListener);
		}
		return tweetViewCreatedAtLabel;
	}
	
	private JLabel getTweetViewCreatedByLabel() {
		if (tweetViewCreatedByLabel == null) {
			tweetViewCreatedByLabel = new JLabel();
			tweetViewCreatedByLabel.setText("@twclient (仮の名前＠だれか名前考えて)");
			tweetViewCreatedByLabel.setToolTipText("from 暗黒の調和師");
			tweetViewCreatedByLabel.addMouseListener(tweetViewListener);
		}
		return tweetViewCreatedByLabel;
	}
	
	private JEditorPane getTweetViewEditorPane() {
		if (tweetViewEditorPane == null) {
			tweetViewEditorPane = new JEditorPane();
			tweetViewEditorPane.setEditable(false);
			tweetViewEditorPane.setContentType("text/html");
			tweetViewEditorPane.setFont(UI_FONT);
			tweetViewEditorPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
			tweetViewEditorPane.setEditorKit(new HTMLEditorKit() {
				
				private HTMLFactory viewFactory = new HTMLFactoryDelegator();
				
				
				@Override
				public ViewFactory getViewFactory() {
					return viewFactory;
				}
			});
			tweetViewEditorPane.setText(APPLICATION_NAME + "へようこそ！<br><b>ゆっくりしていってね！</b>");
			tweetViewEditorPane.addHyperlinkListener(new HyperlinkListener() {
				
				@Override
				public void hyperlinkUpdate(HyperlinkEvent e) {
					if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
						String url = e.getURL().toString();
						if (url.startsWith("http://command/")) {
							String command = url.substring("http://command/".length());
							selectingTab.handleAction(command);
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
	
	private JPanel getTweetViewOperationPanelContainer() {
		if (operationPanelContainer == null) {
			operationPanelContainer = new JPanel();
		}
		return operationPanelContainer;
	}
	
	private JPanel getTweetViewPanel() {
		if (tweetViewPanel == null) {
			tweetViewPanel = new JPanel();
			GroupLayout layout = new GroupLayout(tweetViewPanel);
			tweetViewPanel.setLayout(layout);
			layout.setVerticalGroup( //
				layout
					.createParallelGroup()
					.addGroup(
							layout
								.createSequentialGroup()
								.addGroup(layout.createParallelGroup(Alignment.LEADING) // 
									.addComponent(getTweetViewCreatedByLabel(), Alignment.LEADING) //
									.addComponent(getTweetViewCreatedAtLabel(), Alignment.LEADING))
								.addContainerGap()
								.addGroup(
										layout
											.createParallelGroup(Alignment.LEADING)
											.addGroup(
													layout.createSequentialGroup().addComponent(
															getTweetViewUserIconLabel(), GroupLayout.PREFERRED_SIZE,
															48, GroupLayout.PREFERRED_SIZE))
											.addComponent(getTweetViewTextLayeredPane(), Alignment.LEADING)))
					.addComponent(getTweetViewOperationPanelContainer(), Alignment.CENTER, 0,
							GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE));
			layout.setHorizontalGroup( //
				layout
					.createSequentialGroup()
					.addGroup(
							layout
								.createParallelGroup(Alignment.LEADING)
								.addGroup(layout.createSequentialGroup() // 
									.addComponent(getTweetViewCreatedByLabel()) //
									.addPreferredGap(ComponentPlacement.RELATED).addContainerGap(8, Short.MAX_VALUE) //
									.addComponent(getTweetViewCreatedAtLabel()))
								.addGroup(
										layout
											.createSequentialGroup()
											.addGap(4, 4, 4)
											.addComponent(getTweetViewUserIconLabel(), GroupLayout.PREFERRED_SIZE, 48,
													GroupLayout.PREFERRED_SIZE)
											.addGap(4, 4, 4)
											.addComponent(getTweetViewTextLayeredPane(), GroupLayout.PREFERRED_SIZE,
													200, Short.MAX_VALUE)))
					.addComponent(getTweetViewOperationPanelContainer(), 0, GroupLayout.PREFERRED_SIZE,
							GroupLayout.PREFERRED_SIZE));
		}
		return tweetViewPanel;
	}
	
	private JLayeredPane getTweetViewTextLayeredPane() {
		if (tweetViewTextLayeredPane == null) {
			tweetViewTextLayeredPane = new JLayeredPane();
			tweetViewTextLayeredPane.setLayout(new LayoutManager2() {
				
				Dimension minimumSize;
				
				Dimension prefferedSize;
				
				Dimension maximumSize;
				
				final Dimension MAXIMUM_SIZE = new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
				
				
				@Override
				public void addLayoutComponent(Component comp, Object constraints) {
					invalidateLayout();
				}
				
				@Override
				public void addLayoutComponent(String name, Component comp) {
					if (tweetViewTextLayeredPane != comp.getParent()) {
						throw new IllegalArgumentException("parent is already setted");
					}
					invalidateLayout();
				}
				
				private void calculateLayout(Container parent) {
					if (minimumSize != null && prefferedSize != null && maximumSize != null) {
						return;
					}
					int minw = 0, minh = 0;
					int prefw = 0, prefh = 0;
					int maxw = 0, maxh = 0;
					int count = parent.getComponentCount();
					for (int i = 0; i < count; i++) {
						Component component = parent.getComponent(i);
						Dimension size = component.getMinimumSize();
						minw = max(minw, size.width);
						minh = max(minh, size.height);
						size = component.getPreferredSize();
						prefw = max(prefw, size.width);
						prefh = max(prefh, size.height);
						size = component.getMaximumSize();
						maxw = max(maxw, size.width);
						maxh = max(maxh, size.height);
					}
					minimumSize = new Dimension(minw, minh);
					prefferedSize = new Dimension(prefw, prefh);
					maximumSize = new Dimension(maxh, maxw);
					logger.debug("min={}, pref={}", minimumSize, prefferedSize);
				}
				
				@Override
				public float getLayoutAlignmentX(Container target) {
					return 0;
				}
				
				@Override
				public float getLayoutAlignmentY(Container target) {
					return 0;
				}
				
				private void invalidateLayout() {
					minimumSize = null;
					prefferedSize = null;
					maximumSize = null;
				}
				
				@Override
				public void invalidateLayout(Container target) {
					invalidateLayout();
				}
				
				@Override
				public void layoutContainer(Container parent) {
					final Insets insets = parent.getInsets();
					final Dimension size = parent.getSize();
					final int width = size.width - insets.left - insets.right;
					final int height = size.height - insets.top - insets.bottom;
					final int count = parent.getComponentCount();
					logger.debug("width={}, height={}", width, height);
					for (int i = 0; i < count; i++) {
						Component comp = parent.getComponent(i);
						Dimension prefSize = comp.getPreferredSize();
						Dimension minSize = comp.getMinimumSize();
						int compw, x;
						int comph, y;
						if (comp.getAlignmentX() == Component.CENTER_ALIGNMENT) {
							compw = width;
							x = 0;
						} else {
							compw =
									width < prefSize.width ? ((width > minSize.width) ? width : minSize.width)
											: prefSize.width;
							x = (int) ((width - compw) * comp.getAlignmentX());
						}
						if (comp.getAlignmentY() == Component.CENTER_ALIGNMENT) {
							comph = height;
							y = 0;
						} else {
							comph =
									height < prefSize.height ? ((height > minSize.height) ? height : minSize.height)
											: prefSize.height;
							y = (int) ((height - comph) * comp.getAlignmentY());
						}
						comp.setBounds(x, y, compw, comph);
						logger.debug("setted x={},y={},w={},h={} to {}", Utility.toArray(x, y, compw, comph, comp));
					}
				}
				
				@Override
				public Dimension maximumLayoutSize(Container target) {
					/*calculateLayout(target);
					return maximumSize;*/
					return MAXIMUM_SIZE;
				}
				
				@Override
				public Dimension minimumLayoutSize(Container parent) {
					calculateLayout(parent);
					return minimumSize;
				}
				
				@Override
				public Dimension preferredLayoutSize(Container parent) {
					calculateLayout(parent);
					return prefferedSize;
				}
				
				@Override
				public void removeLayoutComponent(Component comp) {
					invalidateLayout();
				}
			});
			tweetViewTextLayeredPane.add(getTweetViewTextOverlayLabel(), JLayeredPane.MODAL_LAYER);
			tweetViewTextLayeredPane.add(getTweetViewTextScrollPane(), JLayeredPane.DEFAULT_LAYER);
		}
		return tweetViewTextLayeredPane;
	}
	
	private JLabel getTweetViewTextOverlayLabel() {
		if (tweetViewTextOverlayLabel == null) {
			tweetViewTextOverlayLabel = new JLabel();
			tweetViewTextOverlayLabel.setAlignmentX(JLabel.RIGHT_ALIGNMENT);
			tweetViewTextOverlayLabel.setAlignmentY(JLabel.BOTTOM_ALIGNMENT);
			tweetViewTextOverlayLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 24));
			tweetViewTextOverlayLabel.addMouseListener(tweetViewListener);
		}
		return tweetViewTextOverlayLabel;
	}
	
	private JScrollPane getTweetViewTextScrollPane() {
		if (tweetViewScrollPane == null) {
			tweetViewScrollPane = new JScrollPane();
			tweetViewScrollPane.getViewport().setView(getTweetViewEditorPane());
			tweetViewScrollPane.getVerticalScrollBar().setUnitIncrement(configData.scrollAmount);
			tweetViewScrollPane.setAlignmentX(Component.CENTER_ALIGNMENT);
			tweetViewScrollPane.setAlignmentY(Component.CENTER_ALIGNMENT);
		}
		return tweetViewScrollPane;
	}
	
	private JLabel getTweetViewUserIconLabel() {
		if (tweetViewUserIconLabel == null) {
			tweetViewUserIconLabel = new JLabel();
			tweetViewUserIconLabel.setHorizontalAlignment(JLabel.CENTER);
			tweetViewUserIconLabel.setVerticalAlignment(JLabel.CENTER);
		}
		return tweetViewUserIconLabel;
	}
	
	@SuppressWarnings("deprecation")
	@Deprecated
	@Override
	public Twitter getTwitter() {
		return getTwitterForRead();
	}
	
	@Deprecated
	@Override
	public Twitter getTwitterForRead() {
		return configuration.getTwitterForRead();
	}
	
	@Deprecated
	@Override
	public Twitter getTwitterForWrite() {
		return configuration.getTwitterForWrite();
	}
	
	@Override
	public Font getUiFont() {
		return UI_FONT;
	}
	
	@Override
	public Utility getUtility() {
		return configuration.getUtility();
	}
	
	private JTabbedPane getViewTab() {
		if (viewTab == null) {
			viewTab = new JTabbedPane();
			viewTab.setBackground(Color.WHITE);
			viewTab.addChangeListener(new ChangeListener() {
				
				@Override
				public void stateChanged(ChangeEvent e) {
					if (selectingTab != null) {
						selectingTab.focusLost();
					}
					selectingTab = configuration.getFrameTab(viewTab.getSelectedIndex());
					if (selectingTab != null) {
						selectingTab.focusGained();
					}
				}
			});
		}
		return viewTab;
	}
	
	/**
	* Actionをhandleする。
	* 
	* @param name Action名
	* @param statusData ステータス情報
	*/
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
		rootFilterService.onException(ex);
	}
	
	/**
	 * 例外を処理する。
	 * @param e 例外
	 */
	@Override
	public void handleException(TwitterException e) {
		handleException((Exception) e);
	}
	
	@Override
	public void handleShortcutKey(String component, KeyEvent e) {
		int id = e.getID();
		if (id == KeyEvent.KEY_TYPED) {
			throw new IllegalArgumentException("KeyEvent.getID() must not be KEY_TYPED");
		}
		synchronized (shortcutKeyMap) {
			String keyString = Utility.toKeyString(e);
			String actionCommandName = getActionCommandByShortcutKey(component, keyString);
			if (actionCommandName != null) {
				getSelectingTab().handleAction(actionCommandName);
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
		addActionHandler("mute", new MuteActionHandler());
		addActionHandler("tweet", new TweetActionHandler());
		addActionHandler("list", new ListActionHandler());
		addActionHandler("hashtag", new HashtagActionHandler());
		addActionHandler("search", new SearchActionHandler());
		addActionHandler("menu_quit", new MenuQuitActionHandler());
		addActionHandler("menu_propeditor", new MenuPropertyEditorActionHandler());
		addActionHandler("menu_account_verify", new AccountVerifierActionHandler());
		addActionHandler("menu_login_read", new ReloginActionHandler(false));
		addActionHandler("menu_login_write", new ReloginActionHandler(true));
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
		String parentConfigName = configProperties.getProperty("gui.shortcutkey.parent");
		Properties shortcutkeyProperties = new Properties();
		if (parentConfigName.trim().isEmpty() == false) {
			try {
				shortcutkeyProperties.load(getClass().getClassLoader().getResourceAsStream(
						"jp/syuriken/snsw/twclient/shortcutkey/" + parentConfigName + ".properties"));
			} catch (IOException e) {
				logger.error("ショートカットキーの読み込みに失敗", e);
			}
		}
		File file = new File(configuration.getConfigRootDir(), "shortcutkey.cfg");
		if (file.exists()) {
			try {
				shortcutkeyProperties.load(new FileReader(file));
			} catch (FileNotFoundException e) {
				logger.error("ショートカットキーファイルのオープンに失敗", e);
			} catch (IOException e) {
				logger.error("ショートカットキーの読み込みに失敗", e);
			}
		}
		for (Object obj : shortcutkeyProperties.keySet()) {
			String key = (String) obj;
			addShortcutKey(key, shortcutkeyProperties.getProperty(key));
		}
	}
	
	/*package*/boolean isFocusTab(int index) {
		return getViewTab().getSelectedIndex() == index;
	}
	
	/*package*/void refreshTab(int indexOf, ClientTab tab) {
		JTabbedPane tabbedPane = getViewTab();
		tabbedPane.setIconAt(indexOf, tab.getIcon());
		tabbedPane.setTitleAt(indexOf, tab.getTitle());
		tabbedPane.setToolTipTextAt(indexOf, tab.getToolTip());
	}
	
	/**
	 * フレームタブを削除するよ♪
	 * @param indexOf インデックス 
	 * @param tab タブ
	 */
	/*package*/void removeFrameTab(int indexOf, ClientTab tab) {
		JTabbedPane viewTab = getViewTab();
		viewTab.remove(indexOf);
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
	
	@Override
	public void setTweetViewCreatedAt(String createdAt, String toolTip, int actionFlag) {
		getTweetViewCreatedAtLabel().setText(createdAt);
		getTweetViewCreatedAtLabel().setToolTipText(toolTip);
		tweetViewCreatedAtFlag = actionFlag;
	}
	
	@Override
	public void setTweetViewCreatedBy(Icon icon, String createdBy, String toolTip, int actionFlag) {
		getTweetViewUserIconLabel().setIcon(icon);
		getTweetViewCreatedByLabel().setText(createdBy);
		getTweetViewCreatedByLabel().setToolTipText(toolTip);
		tweetViewCreatedByFlag = actionFlag;
	}
	
	@Override
	public void setTweetViewOperationPanel(JPanel operationPanel) {
		operationPanelContainer.removeAll();
		if (operationPanel != null) {
			operationPanelContainer.add(operationPanel);
		}
	}
	
	@Override
	public void setTweetViewText(String tweetData, String overlayString, int actionFlag) {
		tweetViewingTab = selectingTab;
		getTweetViewEditorPane().setText(tweetData);
		getTweetViewTextOverlayLabel().setText(overlayString);
		tweetViewTextOverlayFlag = actionFlag;
	}
	
	@SuppressWarnings("deprecation")
	@Deprecated
	@Override
	public void setTweetViewText(String tweetData, String createdBy, String createdByToolTip, String createdAt,
			String createdAtToolTip, Icon icon, JPanel operationPanel) {
		clearTweetView();
		setTweetViewText(tweetData, null, DO_NOTHING_WHEN_POINTED);
		setTweetViewCreatedAt(createdAt, createdAtToolTip, DO_NOTHING_WHEN_POINTED);
		setTweetViewCreatedBy(icon, createdBy, createdByToolTip, DO_NOTHING_WHEN_POINTED);
		setTweetViewOperationPanel(operationPanel);
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

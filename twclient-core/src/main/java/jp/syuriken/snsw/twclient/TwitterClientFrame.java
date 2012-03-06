package jp.syuriken.snsw.twclient;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.SystemTray;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
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

import jp.syuriken.snsw.twclient.JobQueue.Priority;
import jp.syuriken.snsw.twclient.Utility.IllegalKeyStringException;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
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
	
	public class ConfigData implements PropertyChangeListener {
		
		private static final String PROPERTY_PAGING_INITIAL_MENTION = "twitter.page.initial_mention";
		
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
		
		public Paging pagingOfGettingInitialMentions = new Paging().count(configProperties
			.getInteger(PROPERTY_PAGING_INITIAL_MENTION));
		
		
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
			} else if (Utility.equalString(name, PROPERTY_PAGING_INITIAL_MENTION)) {
				timeOfSurvivingInfo = configProperties.getInteger(PROPERTY_PAGING_INITIAL_MENTION);
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
				getSelectingTab().getRenderer()
					.onClientMessage(ClientMessageListener.REQUEST_FOCUS_TAB_COMPONENT, null);
			} else if (Utility.equalString(actionName, "core!postnext")) {
				getSelectingTab().getRenderer().onClientMessage(ClientMessageListener.REQUEST_FOCUS_NEXT_COMPONENT,
						null);
			} else if (Utility.equalString(actionName, "core!postprev")) {
				getSelectingTab().getRenderer().onClientMessage(ClientMessageListener.REQUEST_FOCUS_PREV_COMPONENT,
						null);
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
				reloginForWrite(accountId);
				rootFilterService.onChangeAccount(true);
			} else {
				reloginForRead(accountId);
				stream.user();
				rootFilterService.onChangeAccount(false);
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
					
					ResponseList<User> users = twitterForRead.lookupUsers(ids);
					
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
	
	private TwitterStream stream;
	
	private JTabbedPane viewTab;
	
	private Twitter twitterForRead;
	
	private Twitter twitterForWrite;
	
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
	
	private JLabel tweetViewSourceLabel;
	
	private JLabel tweetViewDateLabel;
	
	private Dimension linePanelSizeOfSentBy;
	
	private final Object mainThreadHolder;
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	private ImageCacher imageCacher;
	
	private JLabel tweetViewUserIconLabel;
	
	private Map<String, String> shortcutKeyMap = new HashMap<String, String>();
	
	private ConfigData configData;
	
	private FilterService rootFilterService;
	
	protected ClientTab selectingTab;
	
	
	/** 
	 * Creates new form TwitterClientFrame 
	 * @param configuration 設定
	 * @param threadHolder スレッドホルダ
	 */
	public TwitterClientFrame(ClientConfiguration configuration, Object threadHolder) {
		logger.info("initializing frame");
		this.configuration = configuration;
		configuration.setFrameApi(this);
		jobWorkerThread = new JobWorkerThread(jobQueue);
		jobWorkerThread.start();
		rootFilterService = configuration.getRootFilterService();
		mainThreadHolder = threadHolder;
		configProperties = configuration.getConfigProperties();
		configData = new ConfigData();
		timer = new Timer("timer");
		actionHandlerTable = new Hashtable<String, ActionHandler>();
		initActionHandlerTable();
		initShortcutKey();
		
		reloginForRead(configuration.getDefaultAccountId());
		twitterForWrite = twitterForRead; // On initializing, reader is also writer.
		getLoginUser();
		generatePopupMenu(new ActionListenerImplementation());
		initComponents();
		// timer.schedule(updatePostListDispatcher, configData.intervalOfPostListUpdate,
		//		configData.intervalOfPostListUpdate);
		
		timer.schedule(new TimerTask() {
			
			@Override
			public void run() {
				logger.debug("jobQueue={}", jobQueue.size());
				/*synchronized (postListAddQueue) {
					logger.debug(viewTab.toString());
				}*/
			}
		}, 10000, 10000);
		
		imageCacher = new ImageCacher(configuration);
		
		logger.debug("{}", linePanelSizeOfSentBy);
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
		if (postBox.getText().isEmpty() == false) {
			if (selectingPost != null) {
				selectingPost.requestFocusInWindow();
			}
			postActionButton.setEnabled(false);
			postBox.setEnabled(false);
			
			addJob(Priority.HIGH, new ParallelRunnable() {
				
				@Override
				public void run() {
					try {
						StatusUpdate statusUpdate = new StatusUpdate(postBox.getText());
						if (inReplyToStatus != null) {
							statusUpdate.setInReplyToStatusId(inReplyToStatus.getId());
						}
						twitterForWrite.updateStatus(statusUpdate);
						postBox.setText("");
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
								}
							};
							if (EventQueue.isDispatchThread()) {
								EventQueue.invokeAndWait(enabler);
							} else {
								enabler.run();
							}
						} catch (InterruptedException e) {
							// do nothing
						} catch (InvocationTargetException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
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
		
		String[] popupMenus = configProperties.getProperty("gui.menu.popup").split(" ");
		
		for (String actionCommand : popupMenus) {
			ActionHandler handler = getActionHandler(actionCommand);
			if (handler == null) {
				logger.warn("handler {} is not found.", actionCommand); //TODO
			} else {
				JMenuItem menuItem = handler.createJMenuItem(actionCommand);
				menuItem.setActionCommand(actionCommand);
				menuItem.addActionListener(actionListener);
				popupMenu.add(menuItem);
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
	public String getActionCommandByShortcutKey(String keyString) {
		synchronized (shortcutKeyMap) {
			return shortcutKeyMap.get(keyString);
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
				JMenuItem quitMenuItem = new JMenuItem("終了(Q)", KeyEvent.VK_Q);
				quitMenuItem.setActionCommand("menu_quit");
				quitMenuItem.addActionListener(menuActionListener);
				applicationMenu.add(quitMenuItem);
				clientMenu.add(applicationMenu);
			}
			clientMenu.add(getAccountMenu());
			{
				JMenu configMenu = new JMenu("設定");
				JMenuItem propertyEditorMenuItem = new JMenuItem("プロパティエディター(P)", KeyEvent.VK_P);
				propertyEditorMenuItem.setActionCommand("menu_propeditor");
				propertyEditorMenuItem.addActionListener(menuActionListener);
				configMenu.add(propertyEditorMenuItem);
				clientMenu.add(configMenu);
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
				loginUser = twitterForRead.verifyCredentials();
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
				public void keyReleased(KeyEvent e) {
					if (e.isControlDown()) {
						switch (e.getKeyCode()) {
							case KeyEvent.VK_ENTER:
								getPostActionButton().doClick();
								break;
							case KeyEvent.VK_L:
								handleAction("core!focuslist", null);
							default:
								break;
						}
					}
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
	
	private JPanel getPostPanel() {
		if (postPanel == null) {
			postPanel = new JPanel();
			GroupLayout layout = new GroupLayout(postPanel);
			postPanel.setLayout(layout);
			layout.setHorizontalGroup( //
				layout.createParallelGroup(GroupLayout.Alignment.LEADING) //
					.addGroup(
							GroupLayout.Alignment.TRAILING, //
							layout.createSequentialGroup().addContainerGap()
								.addComponent(getPostBoxScrollPane(), GroupLayout.DEFAULT_SIZE, 475, Short.MAX_VALUE)
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED) //
								.addComponent(getPostActionButton()).addGap(18, 18, 18)));
			layout.setVerticalGroup( //
				layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(
						layout
							.createSequentialGroup()
							.addGroup(
									layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
										.addComponent(getPostActionButton())
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
		return configuration.getFrameTab(getViewTab().getSelectedIndex());
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
	
	@SuppressWarnings("deprecation")
	@Deprecated
	@Override
	public Twitter getTwitter() {
		return twitterForRead;
	}
	
	@Override
	public Twitter getTwitterForRead() {
		return twitterForRead;
	}
	
	@Override
	public Twitter getTwitterForWrite() {
		return twitterForWrite;
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
		addActionHandler("menu_account_verify", new AccountVerifierActionHandler());
		addActionHandler("menu_login_read", new ReloginActionHandler(false));
		addActionHandler("menu_login_write", new ReloginActionHandler(true));
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
	
	private void initShortcutKey() {
		Properties shortcutkeyProperties = new Properties();
		try {
			shortcutkeyProperties.load(getClass().getClassLoader().getResourceAsStream(
					"jp/syuriken/snsw/twclient/ui_main_shortcutkey.properties"));
		} catch (IOException e) {
			logger.error("ショートカットキーの読み込みに失敗", e);
		}
		for (Object obj : shortcutkeyProperties.keySet()) {
			String key = (String) obj;
			try {
				addShortcutKey(key, shortcutkeyProperties.getProperty(key));
			} catch (IllegalKeyStringException e) {
				logger.warn("ショートカットキーの読み込み中にエラー", e);
			}
		}
	}
	
	/*package*/boolean isFocusTab(int index) {
		return getViewTab().getSelectedIndex() == index;
	}
	
	/*package*/void refreashTab(int indexOf, ClientTab tab) {
		JTabbedPane tabbedPane = getViewTab();
		tabbedPane.setIconAt(indexOf, tab.getIcon());
		tabbedPane.setTitleAt(indexOf, tab.getTitle());
		tabbedPane.setToolTipTextAt(indexOf, tab.getToolTip());
	}
	
	private void reloginForRead(String accountId) {
		twitterForRead = new TwitterFactory(configuration.getTwitterConfiguration(accountId)).getInstance();
		loginUser = null;
		if (stream != null) {
			final TwitterStream oldStream = stream;
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					oldStream.cleanUp();
				}
			}, "stream disconnector").start();
		}
		stream = new TwitterStreamFactory(configuration.getTwitterConfiguration(accountId)).getInstance();
		stream.addConnectionLifeCycleListener(rootFilterService);
		stream.addListener(rootFilterService);
	}
	
	private void reloginForWrite(String accountId) {
		twitterForWrite = new TwitterFactory(configuration.getTwitterConfiguration(accountId)).getInstance();
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
		return oldText;
	}
	
	@Override
	public void setTweetViewText(String tweetData, String createdBy, String createdByToolTip, String createdAt,
			String createdAtToolTip, Icon icon) {
		getTweetViewEditorPane().setText(tweetData);
		getTweetViewSourceLabel().setText(createdBy);
		getTweetViewSourceLabel().setToolTipText(createdByToolTip);
		getTweetViewDateLabel().setText(createdAt);
		getTweetViewDateLabel().setToolTipText(createdAtToolTip);
		getTweetViewUserIconLabel().setIcon(icon);
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
					Paging paging = configData.pagingOfGettingInitialTimeline;
					homeTimeline = twitterForRead.getHomeTimeline(paging);
					for (Status status : homeTimeline) {
						rootFilterService.onStatus(status);
					}
				} catch (TwitterException e) {
					handleException(e);
				}
				configuration.setInitializing(false);
			}
		});
		jobQueue.addJob(new Runnable() {
			
			@Override
			public void run() {
				try {
					Paging paging = configData.pagingOfGettingInitialMentions;
					ResponseList<Status> mentions = twitterForRead.getMentions(paging);
					for (Status status : mentions) {
						rootFilterService.onStatus(status);
					}
				} catch (TwitterException e) {
					handleException(e);
				} finally {
					configuration.setInitializing(false);
				}
			}
		});
		timer.schedule(new TimerTask() {
			
			@Override
			public void run() {
				jobQueue.addJob(new Runnable() {
					
					@Override
					public void run() {
						Paging paging = configData.pagingOfGettingTimeline;
						ResponseList<Status> timeline;
						try {
							timeline = twitterForRead.getHomeTimeline(paging);
							for (Status status : timeline) {
								rootFilterService.onStatus(status);
							}
						} catch (TwitterException e) {
							handleException(e);
						}
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

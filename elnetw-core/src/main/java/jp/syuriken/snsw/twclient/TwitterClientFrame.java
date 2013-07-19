package jp.syuriken.snsw.twclient;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Insets;
import java.awt.LayoutManager2;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
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

import jp.syuriken.snsw.twclient.JobQueue.Priority;
import jp.syuriken.snsw.twclient.handler.IntentArguments;
import jp.syuriken.snsw.twclient.internal.DefaultTweetLengthCalculator;
import jp.syuriken.snsw.twclient.internal.HTMLFactoryDelegator;
import jp.syuriken.snsw.twclient.internal.TwitterRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;

import static java.lang.Math.abs;
import static java.lang.Math.max;

/**
 * elnetwのメインウィンドウ
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
@SuppressWarnings("serial")
/*package*/class TwitterClientFrame extends javax.swing.JFrame implements WindowListener, ClientFrameApi {

	private static final class HTMLEditorKitExtension extends HTMLEditorKit {

		private transient HTMLFactory viewFactory = new HTMLFactoryDelegator();

		@Override
		public ViewFactory getViewFactory() {
			return viewFactory;
		}
	}

	/**
	 * MenuItemのActionListenerの実装。
	 *
	 * @author Turenar (snswinhaiku dot lo at gmail dot com)
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
			configuration.handleAction(Utility.getIntentArguments(e.getActionCommand()));
		}
	}

	/**
	 * "core!*" アクションハンドラ
	 *
	 * @author Turenar (snswinhaiku dot lo at gmail dot com)
	 */
	public class CoreFrameActionHandler implements ActionHandler {

		@Override
		public JMenuItem createJMenuItem(IntentArguments args) {
			return null;
		}

		@Override
		public void handleAction(IntentArguments args) {
			String actionName = args.getExtraObj("_arg", String.class);
			if (actionName == null) {
				throw new IllegalArgumentException("`action` is not found");
			}
			if (actionName.equals("move_between_list_and_postbox")) {
				if (getPostBox().isFocusOwner()) {
					actionName = "focuslist";
				} else {
					actionName = "focusinput";
				}
			}

			switch (actionName) {
				case "submenu":
					return;
				case "version": {
					VersionInfoFrame frame = new VersionInfoFrame(configuration);
					frame.setVisible(true);
					break;
				}
				case "focusinput":
					getPostBox().requestFocusInWindow();
					break;
				case "tabswitch_prev": {
					JTabbedPane tab = getViewTab();
					int selectedIndex = tab.getSelectedIndex();
					if (selectedIndex > 0) {
						tab.setSelectedIndex(selectedIndex - 1);
					}
					break;
				}
				case "tabswitch_next": {
					JTabbedPane tab = getViewTab();
					int selectedIndex = tab.getSelectedIndex();
					if (selectedIndex < tab.getTabCount() - 1) {
						tab.setSelectedIndex(selectedIndex + 1);
					}
					break;
				}
				default:
					String messageName;
					switch (actionName) {
						case "focuslist":
							messageName = ClientMessageListener.REQUEST_FOCUS_TAB_COMPONENT;
							break;
						case "postnext":
							messageName = ClientMessageListener.REQUEST_FOCUS_NEXT_COMPONENT;
							break;
						case "postprev":
							messageName = ClientMessageListener.REQUEST_FOCUS_PREV_COMPONENT;
							break;
						case "postuserprev":
							messageName = ClientMessageListener.REQUEST_FOCUS_USER_PREV_COMPONENT;
							break;
						case "postusernext":
							messageName = ClientMessageListener.REQUEST_FOCUS_USER_NEXT_COMPONENT;
							break;
						case "postfirst":
							messageName = ClientMessageListener.REQUEST_FOCUS_FIRST_COMPONENT;
							break;
						case "postlast":
							messageName = ClientMessageListener.REQUEST_FOCUS_LAST_COMPONENT;
							break;
						case "postwindowfirst":
							messageName = ClientMessageListener.REQUEST_FOCUS_WINDOW_FIRST_COMPONENT;
							break;
						case "postwindowlast":
							messageName = ClientMessageListener.REQUEST_FOCUS_WINDOW_LAST_COMPONENT;
							break;
						case "scroll_as_windowlast":
							messageName = ClientMessageListener.REQUEST_SCROLL_AS_WINDOW_LAST;
							break;
						case "jump_inReplyTo":
							messageName = ClientMessageListener.REQUEST_FOCUS_IN_REPLY_TO;
							break;
						case "jump_inReplyToBack":
							messageName = ClientMessageListener.REQUEST_FOCUS_BACK_REPLIED_BY;
							break;
						default:
							logger.warn("[core AH] {} is not command", actionName);
							return;
					}
					getSelectingTab().getRenderer().onClientMessage(messageName, null);
			}
		}

		@Override
		public void popupMenuWillBecomeVisible(JMenuItem menuItem, IntentArguments args) {
			if ("core!submenu".equals(menuItem.getActionCommand())) {
				if (menuItem instanceof JMenu == false) {
					logger.error("\"core!submenu\" argued menuItem not as JMenu");
					throw new AssertionError();
				}
				Component[] subItems = ((JMenu) menuItem).getMenuComponents();
				for (Component subItem : subItems) {
					if (subItem instanceof JMenuItem) {
						JMenuItem subMenuItem = (JMenuItem) subItem;
						String actionCommand = subMenuItem.getActionCommand();
						IntentArguments intentArguments = new IntentArguments(actionCommand);
						configuration.getActionHandler(intentArguments).popupMenuWillBecomeVisible(subMenuItem,
								intentArguments);
					}
				}
			}
		}

	}

	/**
	 * JLabel および JEditorPane用
	 *
	 * @author Turenar (snswinhaiku dot lo at gmail dot com)
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
	 * ログイン出来るユーザー情報を取得する
	 *
	 * @author Turenar (snswinhaiku dot lo at gmail dot com)
	 */
	/*package*/final class UserInfoFetcher implements Runnable {

		public final String[] accountList = configuration.getAccountList();

		final UserInfoFetcher this$uif = UserInfoFetcher.this;

		final TwitterClientFrame this$tcf = TwitterClientFrame.this;

		public int offset = 0;

		public JMenuItem[] readTimelineMenuItems;

		public JMenuItem[] postToMenuItems;

		/** インスタンスを生成する。 */
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
						this$tcf.addJob(Priority.LOW, this$uif);
					}
				}, 10000);
			}
		}
	}

	private class PostTask extends TwitterRunnable implements ParallelRunnable {

		private final String text;
		private ClientTab tab;

		public PostTask(String text, ClientTab selectingTab) {
			this.text = text;
			tab = selectingTab;
		}

		@Override
		protected void access() throws TwitterException {
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
				tweetLengthCalculator = DEFAULT_TWEET_LENGTH_CALCULATOR;
			} finally {
				try {
					final TwitterClientFrame this$tcf = TwitterClientFrame.this;
					Runnable enabler = new Runnable() {

						@Override
						public void run() {
							this$tcf.postActionButton.setEnabled(true);
							this$tcf.postBox.setEnabled(true);
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

		@Override
		protected void handleException(TwitterException ex) {
			tab.getRenderer().onException(ex);
		}
	}

	/**
	 * アプリケーション名
	 *
	 * @deprecated use {@link ClientConfiguration#APPLICATION_NAME}
	 */
	@Deprecated
	public static final String APPLICATION_NAME = "elnetw";

	/** デフォルトフォント: TODO from config */
	public static final Font DEFAULT_FONT = new Font(Font.MONOSPACED, Font.PLAIN, 12);

	/** UIフォント: TODO from config */
	public static final Font UI_FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 11);

	/*package*/static final Logger logger = LoggerFactory.getLogger(TwitterClientFrame.class);

	/*package*/final transient ClientConfiguration configuration;

	/*package*/final transient ActionListener menuActionListener = new ActionListenerImplementation();

	/*package*/final transient TweetLengthCalculator DEFAULT_TWEET_LENGTH_CALCULATOR =
			new DefaultTweetLengthCalculator(this);

	/*package*/ Status inReplyToStatus = null;

	/*package*/JPanel editPanel;

	/*package*/JPanel postPanel;

	/*package*/JScrollPane postBoxScrollPane;

	/*package*/JSplitPane jSplitPane1;

	/*package*/JButton postActionButton;

	/*package*/JTextArea postBox;

	/*package*/JTabbedPane viewTab;

	/*package*/ClientProperties configProperties;

	/*package*/JMenuBar clientMenu;

	/*package*/JPanel tweetViewPanel;

	/*package*/JMenu accountMenu;

	/*package*/JMenu readTimelineJMenu;

	/*package*/JMenu postToJMenu;

	/*package*/JScrollPane tweetViewScrollPane;

	/*package*/JEditorPane tweetViewEditorPane;

	/*package*/JLabel tweetViewCreatedByLabel;

	/*package*/JLabel tweetViewCreatedAtLabel;

	/*package*/transient ImageCacher imageCacher;

	/*package*/JLabel tweetViewUserIconLabel;

	/*package*/Map<String, String> shortcutKeyMap = new HashMap<String, String>();

	/*package*/transient FilterService rootFilterService;

	/*package*/JLabel postLengthLabel;

	protected transient ClientTab selectingTab;

	/*package*/transient TweetLengthCalculator tweetLengthCalculator = DEFAULT_TWEET_LENGTH_CALCULATOR;

	/*package*/transient DefaultMouseListener tweetViewListener = new DefaultMouseListener();

	/*package*/transient ClientTab tweetViewingTab;

	/*package*/JPanel operationPanelContainer;

	/*package*/JLayeredPane tweetViewTextLayeredPane;

	/*package*/JLabel tweetViewTextOverlayLabel;

	/*package*/int tweetViewCreatedByFlag;

	/*package*/int tweetViewCreatedAtFlag;

	/*package*/int tweetViewTextOverlayFlag;

	/**
	 * Creates new form TwitterClientFrame
	 *
	 * @param configuration 設定
	 */
	public TwitterClientFrame(ClientConfiguration configuration) {
		logger.info("initializing frame");
		this.configuration = configuration;
		configuration.setFrameApi(this);

		rootFilterService = configuration.getRootFilterService();
		configProperties = configuration.getConfigProperties();
		initActionHandlerTable();

		getLoginUser();
		initComponents();

		imageCacher = new ImageCacher(configuration);

		logger.info("frame initialized");
	}

	@Deprecated
	@Override
	public ActionHandler addActionHandler(String name, ActionHandler handler) {
		return configuration.addActionHandler(name, handler);
	}

	@Override
	@Deprecated
	public void addJob(Priority priority, Runnable job) {
		configuration.addJob(priority, job);
	}

	@Override
	@Deprecated
	public void addJob(Runnable job) {
		configuration.addJob(job);
	}

	@Override
	public void addShortcutKey(String keyString, String actionName) {
		shortcutKeyMap.put(keyString, actionName);
	}

	protected void addTab(ClientTab tab) {
		getViewTab().add(tab.getTitle(), tab.getTabComponent());
		tab.initTimeline();
	}

	/** 終了できるようにお掃除する */
	public void cleanUp() {
		configuration.setShutdownPhase(true);
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
			final String text = tweetLengthCalculator.getShortenedText(getPostBox().getText());
			postActionButton.setEnabled(false);
			postBox.setEnabled(false);

			addJob(Priority.HIGH, new PostTask(text, getSelectingTab()));
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
			ActionHandler handler = configuration.getActionHandler(new IntentArguments(actionCommand));
			if (handler == null) {
				logger.warn("handler {} is not found.", actionCommand); //TODO
			} else {
				JMenuItem menuItem = handler.createJMenuItem(new IntentArguments(actionCommand));
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
	public Font getDefaultFont() {
		return DEFAULT_FONT;
	}

	private JPanel getEditPanel() {
		if (editPanel == null) {
			editPanel = new JPanel();
			GroupLayout layout = new GroupLayout(editPanel);
			editPanel.setLayout(layout);
			layout.setHorizontalGroup(
					layout.createParallelGroup(GroupLayout.Alignment.LEADING)
							.addComponent(getPostPanel(), GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE,
									GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
							.addComponent(getTweetViewPanel()));
			layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
					.addGroup(layout.createSequentialGroup()
							.addComponent(getPostPanel(), 64, 64, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
							.addComponent(getTweetViewPanel(), 72, 72, Short.MAX_VALUE)));

		}
		return editPanel;
	}

	@Override
	public Component getFrame() {
		return this;
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
		return configProperties.getInteger(ClientConfiguration.PROPERTY_INFO_SURVIVE_TIME);
	}

	/**
	 * ログインしているユーザーを取得する。取得出来なかった場合nullの可能性あり。また、ブロックする可能性あり。
	 *
	 * @return the loginUser
	 */
	@Override
	public User getLoginUser() {
		return configuration.getCacheManager().getUser(Long.parseLong(configuration.getAccountIdForRead()));
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

	/*package*/JTextArea getPostBox() {
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
			layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
					.addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
							.addContainerGap()
							.addComponent(getPostBoxScrollPane(), GroupLayout.DEFAULT_SIZE, 475,
									Short.MAX_VALUE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addGroup(layout.createParallelGroup(Alignment.TRAILING)
									.addComponent(getPostActionButton())
									.addComponent(getPostLengthLabel()))
							.addGap(18, 18, 18)));
			layout.setVerticalGroup(
					layout.createParallelGroup(GroupLayout.Alignment.LEADING)
							.addGroup(
									layout.createSequentialGroup()
											.addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
													.addGroup(layout.createSequentialGroup()
															.addComponent(getPostLengthLabel())
															.addComponent(getPostActionButton()))
													.addComponent(getPostBoxScrollPane(), 32, 64,
															GroupLayout.PREFERRED_SIZE))
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

	/*package*/JMenu getPostToJMenu() {
		if (postToJMenu == null) {
			postToJMenu = new JMenu("投稿先");
		}
		return postToJMenu;
	}

	/*package*/JMenu getReadTimelineJMenu() {
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
	@Deprecated
	@Override
	public Timer getTimer() {
		return configuration.getTimer();
	}

	/*package*/JLabel getTweetViewCreatedAtLabel() {
		if (tweetViewCreatedAtLabel == null) {
			tweetViewCreatedAtLabel = new JLabel();
			tweetViewCreatedAtLabel.setText("2012/1/1 00:00:00");
			tweetViewCreatedAtLabel.setHorizontalAlignment(JLabel.RIGHT);
			tweetViewCreatedAtLabel.setAlignmentX(RIGHT_ALIGNMENT);
			tweetViewCreatedAtLabel.addMouseListener(tweetViewListener);
		}
		return tweetViewCreatedAtLabel;
	}

	/*package*/JLabel getTweetViewCreatedByLabel() {
		if (tweetViewCreatedByLabel == null) {
			tweetViewCreatedByLabel = new JLabel();
			tweetViewCreatedByLabel.setText("@elnetw (名前はこれで決まり？)");
			tweetViewCreatedByLabel.setToolTipText("from 暗黒の調和師");
			tweetViewCreatedByLabel.addMouseListener(tweetViewListener);
		}
		return tweetViewCreatedByLabel;
	}

	/*package*/JEditorPane getTweetViewEditorPane() {
		if (tweetViewEditorPane == null) {
			tweetViewEditorPane = new JEditorPane();
			tweetViewEditorPane.setEditable(false);
			tweetViewEditorPane.setContentType("text/html");
			tweetViewEditorPane.setFont(UI_FONT);
			tweetViewEditorPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
			tweetViewEditorPane.setEditorKit(new HTMLEditorKitExtension());
			tweetViewEditorPane.setText(APPLICATION_NAME + "へようこそ！<br><b>ゆっくりしていってね！</b>");
			tweetViewEditorPane.addHyperlinkListener(new HyperlinkListener() {

				@Override
				public void hyperlinkUpdate(HyperlinkEvent e) {
					if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
						String url = e.getURL().toString();
						if (url.startsWith("http://command/")) {
							String command = url.substring("http://command/".length());
							selectingTab.handleAction(Utility.getIntentArguments(command));
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
			layout.setVerticalGroup(layout.createParallelGroup()
					.addGroup(layout.createSequentialGroup()
							.addGroup(layout.createParallelGroup(Alignment.LEADING)
									.addComponent(getTweetViewCreatedByLabel(), Alignment.LEADING)
									.addComponent(getTweetViewCreatedAtLabel(), Alignment.LEADING))
							.addContainerGap()
							.addGroup(layout.createParallelGroup(Alignment.LEADING)
									.addGroup(layout.createSequentialGroup()
											.addComponent(getTweetViewUserIconLabel(),
													GroupLayout.PREFERRED_SIZE, 48, GroupLayout.PREFERRED_SIZE))
									.addComponent(getTweetViewTextLayeredPane(), Alignment.LEADING)))
					.addComponent(getTweetViewOperationPanelContainer(), Alignment.CENTER, 0,
							GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE));
			layout.setHorizontalGroup(layout.createSequentialGroup()
					.addGroup(layout.createParallelGroup(Alignment.LEADING)
							.addGroup(layout.createSequentialGroup()
									.addComponent(getTweetViewCreatedByLabel())
									.addPreferredGap(ComponentPlacement.RELATED).addContainerGap(8, Short.MAX_VALUE)
									.addComponent(getTweetViewCreatedAtLabel()))
							.addGroup(layout.createSequentialGroup()
									.addGap(4, 4, 4)
									.addComponent(getTweetViewUserIconLabel(), GroupLayout.PREFERRED_SIZE, 48,
											GroupLayout.PREFERRED_SIZE)
									.addGap(4, 4, 4)
									.addComponent(getTweetViewTextLayeredPane(), GroupLayout.PREFERRED_SIZE, 200,
											Short.MAX_VALUE)))
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
					int minw = 0;
					int minh = 0;
					int prefw = 0;
					int prefh = 0;
					int maxw = 0;
					int maxh = 0;
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
					for (int i = 0; i < count; i++) {
						Component comp = parent.getComponent(i);
						Dimension prefSize = comp.getPreferredSize();
						Dimension minSize = comp.getMinimumSize();
						int compw;
						int x;
						int comph;
						int y;

						if (abs(comp.getAlignmentX() - Component.CENTER_ALIGNMENT) < .0000001) {
							compw = width;
							x = 0;
						} else {
							compw =
									width < prefSize.width ? ((width > minSize.width) ? width : minSize.width)
											: prefSize.width;
							x = (int) ((width - compw) * comp.getAlignmentX());
						}
						if (abs(comp.getAlignmentY() - Component.CENTER_ALIGNMENT) < .0000001) {
							comph = height;
							y = 0;
						} else {
							comph =
									height < prefSize.height ? ((height > minSize.height) ? height : minSize.height)
											: prefSize.height;
							y = (int) ((height - comph) * comp.getAlignmentY());
						}
						comp.setBounds(x, y, compw, comph);
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

	/*package*/JLabel getTweetViewTextOverlayLabel() {
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
			tweetViewScrollPane.getVerticalScrollBar().setUnitIncrement(
					configProperties.getInteger(ClientConfiguration.PROPERTY_LIST_SCROLL));
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

	/*package*/JTabbedPane getViewTab() {
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

	@Deprecated
	@Override
	public void handleAction(String name, StatusData statusData) {
		IntentArguments intentArguments = new IntentArguments(name);
		ActionHandler actionHandler = configuration.getActionHandler(intentArguments);
		if (actionHandler == null) {
			logger.warn("ActionHandler {} is not found.", name);
		} else {
			logger.trace("ActionHandler {} called.", name);
			actionHandler.handleAction(intentArguments);
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
	 *
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
				getSelectingTab().handleAction(Utility.getIntentArguments(actionCommandName));
				e.consume();
			}
		}
	}

	/** アクションハンドラーテーブルを初期化する。 */
	private void initActionHandlerTable() {
		configuration.addActionHandler("core", new CoreFrameActionHandler());
	}

	/**
	 * This method is called from within the constructor to
	 * initialize the form.
	 */
	private void initComponents() {
		setTitle(ClientConfiguration.APPLICATION_NAME);
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

	/*package*/boolean isFocusTab(int index) {
		return getViewTab().getSelectedIndex() == index;
	}

	private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
		stream.defaultReadObject();
		initActionHandlerTable();
	}

	/*package*/void refreshTab(int indexOf, ClientTab tab) {
		JTabbedPane tabbedPane = getViewTab();
		tabbedPane.setIconAt(indexOf, tab.getIcon());
		tabbedPane.setTitleAt(indexOf, tab.getTitle());
		tabbedPane.setToolTipTextAt(indexOf, tab.getToolTip());
	}

	/**
	 * フレームタブを削除するよ♪
	 *
	 * @param indexOf インデックス
	 * @param tab     タブ
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

	/** 開始する */
	public void start() {
		EventQueue.invokeLater(new Runnable() {

			@Override
			public void run() {
				setVisible(true);
			}
		});
	}

	/*package*/void updatePostLength() {
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
		StringBuilder tabs = new StringBuilder();
		for (ClientTab tab : configuration.getFrameTabs()) {
			String tabId = tab.getTabId();
			String uniqId = tab.getUniqId();
			tabs.append(tabId).append(':').append(uniqId).append(' ');
			configProperties.setProperty("gui.tabs.data." + uniqId, tab.getSerializedData());
		}
		configProperties.setProperty("gui.tabs.list", tabs.toString().trim());

		configProperties.setDimension("gui.main.size", getSize());
		configProperties.setInteger("gui.main.split.pos", getSplitPane1().getDividerLocation());
		configProperties.store();
		TwitterClientMain.quit();
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

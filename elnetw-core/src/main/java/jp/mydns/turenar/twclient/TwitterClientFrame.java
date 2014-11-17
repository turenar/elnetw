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

package jp.mydns.turenar.twclient;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.AbstractMap;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

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

import jp.mydns.turenar.twclient.JobQueue.Priority;
import jp.mydns.turenar.twclient.bus.MessageBus;
import jp.mydns.turenar.twclient.conf.ClientProperties;
import jp.mydns.turenar.twclient.gui.ShortcutKeyManager;
import jp.mydns.turenar.twclient.gui.VersionInfoFrame;
import jp.mydns.turenar.twclient.gui.tab.ClientTab;
import jp.mydns.turenar.twclient.gui.tab.ClientTabFactory;
import jp.mydns.turenar.twclient.intent.Intent;
import jp.mydns.turenar.twclient.intent.IntentArguments;
import jp.mydns.turenar.twclient.intent.PopupMenuDispatcher;
import jp.mydns.turenar.twclient.internal.DefaultTweetLengthCalculator;
import jp.mydns.turenar.twclient.internal.HTMLFactoryDelegator;
import jp.mydns.turenar.twclient.internal.IntentActionListener;
import jp.mydns.turenar.twclient.internal.LayeredLayoutManager;
import jp.mydns.turenar.twclient.internal.TwitterRunnable;
import jp.mydns.turenar.twclient.twitter.TwitterUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.TwitterException;
import twitter4j.User;

import static jp.mydns.turenar.twclient.ClientConfiguration.APPLICATION_NAME;
import static jp.mydns.turenar.twclient.i18n.LocalizationResource.tr;

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

	private static class TabFactoryEntryComparator implements Comparator<Map.Entry<String, ClientTabFactory>>,
			Serializable {

		private static final long serialVersionUID = -9038414469346041741L;

		@Override
		public int compare(Map.Entry<String, ClientTabFactory> o1, Map.Entry<String, ClientTabFactory> o2) {
			return o1.getValue().getPriority() - o2.getValue().getPriority();
		}
	}

	/**
	 * "core!*" アクションハンドラ
	 *
	 * @author Turenar (snswinhaiku dot lo at gmail dot com)
	 */
	public class CoreFrameIntent implements Intent {
		@Override
		public void createJMenuItem(PopupMenuDispatcher dispatcher, IntentArguments args) {
		}

		@Override
		public void handleAction(IntentArguments args) {
			String actionName = args.getExtraObj(IntentArguments.UNNAMED_ARG, String.class);
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
				case "version": {
					VersionInfoFrame frame = new VersionInfoFrame();
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
					String text = label.getText();
					if (text != null) {
						StringBuilder stringBuilder = new StringBuilder(text);
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
					String text = label.getText();
					if (text != null) {
						StringBuilder stringBuilder = new StringBuilder(text);
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
		}

		@Override
		public void mousePressed(MouseEvent e) {
		}

		@Override
		public void mouseReleased(MouseEvent e) {
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
				tweetLengthCalculator = defaultTweetLengthCalculator;
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
		protected void onException(TwitterException ex) {
			tab.getRenderer().onException(ex);
		}
	}

	/**
	 * ログイン出来るユーザー情報を取得する
	 *
	 * @author Turenar (snswinhaiku dot lo at gmail dot com)
	 */
	/*package*/final class UserInfoFetcher implements Runnable {

		public final List<String> accountList;
		public JMenuItem[] readTimelineMenuItems;
		public JMenuItem[] postToMenuItems;

		/** インスタンスを生成する。 */
		public UserInfoFetcher() {
			accountList = configuration.getAccountList();
			readTimelineMenuItems = new JMenuItem[accountList.size()];
			postToMenuItems = new JMenuItem[accountList.size()];
			String defaultAccountId = configuration.getDefaultAccountId();
			ButtonGroup readButtonGroup = new ButtonGroup();
			ButtonGroup writeButtonGroup = new ButtonGroup();
			CacheManager cacheManager = configuration.getCacheManager();
			for (int i = 0; i < accountList.size(); i++) {
				String accountId = accountList.get(i);

				cacheManager.queueFetchingUser(Long.parseLong(accountId));

				JMenuItem readMenuItem = new JRadioButtonMenuItem(accountId);
				readMenuItem.addActionListener(new IntentActionListener("menu_login_read")
						.putExtra("accountId", accountId));
				if (accountId.equals(defaultAccountId)) {
					readMenuItem.setSelected(true);
					readMenuItem.setFont(readMenuItem.getFont().deriveFont(Font.BOLD));
				}
				readTimelineMenuItems[i] = readMenuItem;
				getReadTimelineJMenu().add(readMenuItem);
				readButtonGroup.add(readMenuItem);

				JMenuItem writeMenuItem = new JRadioButtonMenuItem(accountId);
				new IntentArguments("menu_login_write").putExtra("accountId", accountId).setMenu(writeMenuItem);
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
			CacheManager cacheManager = configuration.getCacheManager();
			for (int i = 0, accountListSize = accountList.size(); i < accountListSize; i++) {
				String accountId = accountList.get(i);
				TwitterUser user = cacheManager.getUser(Long.parseLong(accountId));
				readTimelineMenuItems[i].setText(user.getScreenName());
				postToMenuItems[i].setText(user.getScreenName());
			}
		}
	}


	/*package*/static final Logger logger = LoggerFactory.getLogger(TwitterClientFrame.class);
	/** デフォルトフォント */
	public final Font defaultFont;
	/** UIフォント */
	public final Font uiFont;
	/*package*/final transient ClientConfiguration configuration;
	/*package*/final transient TweetLengthCalculator defaultTweetLengthCalculator =
			new DefaultTweetLengthCalculator(this);
	/*package*/ Status inReplyToStatus = null;
	/*package*/ JPanel editPanel;
	/*package*/ JPanel postPanel;
	/*package*/ JScrollPane postBoxScrollPane;
	/*package*/ JSplitPane jSplitPane1;
	/*package*/ JButton postActionButton;
	/*package*/ JTextArea postBox;
	/*package*/ JTabbedPane viewTab;
	/*package*/transient ClientProperties configProperties;
	/*package*/ JMenuBar clientMenu;
	/*package*/ JPanel tweetViewPanel;
	/*package*/ JMenu accountMenu;
	/*package*/ JMenu readTimelineJMenu;
	/*package*/ JMenu postToJMenu;
	/*package*/ JScrollPane tweetViewScrollPane;
	/*package*/ JEditorPane tweetViewEditorPane;
	/*package*/ JLabel tweetViewCreatedByLabel;
	/*package*/ JLabel tweetViewCreatedAtLabel;
	/*package*/ JLabel tweetViewUserIconLabel;
	/*package*/ JLabel postLengthLabel;
	protected transient ClientTab selectingTab;
	/*package*/transient TweetLengthCalculator tweetLengthCalculator = defaultTweetLengthCalculator;
	/*package*/transient DefaultMouseListener tweetViewListener = new DefaultMouseListener();
	/*package*/transient ClientTab tweetViewingTab;
	/*package*/ JPanel operationPanelContainer;
	/*package*/ JLayeredPane tweetViewTextLayeredPane;
	/*package*/ JLabel tweetViewTextOverlayLabel;
	/*package*/ int tweetViewCreatedByFlag;
	/*package*/ int tweetViewCreatedAtFlag;
	/*package*/ int tweetViewTextOverlayFlag;
	/*package*/transient HashMap<String, IntentArguments> urlIntentMap = new HashMap<>();
	private int nextIntentUrlId;

	/**
	 * Creates new form TwitterClientFrame
	 *
	 * @param configuration 設定
	 */
	public TwitterClientFrame(ClientConfiguration configuration) {
		logger.info("initializing frame");
		this.configuration = configuration;
		configuration.setFrameApi(this);

		configProperties = configuration.getConfigProperties();
		uiFont = configProperties.getFont("gui.font.ui");
		defaultFont = configProperties.getFont("gui.font.default");
		initIntentTable();

		getLoginUser();
		initComponents();

		logger.info("frame initialized");
	}

	protected void addTab(ClientTab tab) {
		getViewTab().addTab(null, tab.getIcon(), tab.getTabComponent());
		getViewTab().setTabComponentAt(getViewTab().getTabCount() - 1, tab.getTitleComponent());
		tab.initTimeline();
	}

	@Override
	public void clearTweetView() {
		setTweetViewText(null, null, DO_NOTHING_WHEN_POINTED);
		setTweetViewCreatedAt(null, null, DO_NOTHING_WHEN_POINTED);
		setTweetViewCreatedBy(null, null, null, DO_NOTHING_WHEN_POINTED);
		setTweetViewOperationPanel(null);
		urlIntentMap.clear();
		nextIntentUrlId = 0;
	}

	@Override
	public void doPost() {
		if (postActionButton.isEnabled() && !postBox.getText().isEmpty()) {
			final String text = tweetLengthCalculator.getShortenedText(getPostBox().getText());
			postActionButton.setEnabled(false);
			postBox.setEnabled(false);

			configuration.addJob(Priority.HIGH, new PostTask(text, getSelectingTab()));
		}
	}

	/*package*/void focusFrameTab(ClientTab tab, int index) {
		getViewTab().setSelectedIndex(index);
	}

	@Override
	public void focusPostBox() {
		getPostBox().requestFocusInWindow();
	}

	/**
	 * メニューバーを取得する。
	 *
	 * @return メニューバー
	 */
	private JMenuBar getClientMenuBar() {
		if (clientMenu == null) {
			clientMenu = new JMenuBar();
			clientMenu.add(getMenuApplication());
			clientMenu.add(getMenuAccount());
			clientMenu.add(getMenuAdd());
			clientMenu.add(getMenuInfo());
		}
		return clientMenu;
	}

	@Override
	public String getCommandUrl(IntentArguments intentArguments) {
		String url = "http://command/?id=" + nextIntentUrlId;
		urlIntentMap.put(url, intentArguments);
		nextIntentUrlId++;
		return url;
	}

	@Override
	public Font getDefaultFont() {
		return defaultFont;
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
							.addComponent(getTweetViewPanel())
			);
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

	private JMenu getMenuAccount() {
		if (accountMenu == null) {
			accountMenu = new JMenu();
			Utility.setMnemonic(accountMenu,tr("A&ccount"));

			configuration.addJob(Priority.LOW, new UserInfoFetcher());
			accountMenu.add(getReadTimelineJMenu());
			accountMenu.add(getPostToJMenu());

			JMenuItem verifyAccountMenuItem = new JMenuItem();
			Utility.setMnemonic(verifyAccountMenuItem, tr("&Verify account..."));
			new IntentArguments("menu_account_verify").setMenu(verifyAccountMenuItem);
			accountMenu.add(verifyAccountMenuItem);
		}
		return accountMenu;
	}
	private JMenu getMenuAdd() {
		JMenu addMenu = new JMenu();
		Utility.setMnemonic(addMenu, tr("&Add"));
		addMenu.add(getMenuAddTab());

		return addMenu;
	}

	private JMenu getMenuAddTab() {
		JMenu tabMenu = new JMenu();
		Utility.setMnemonic(tabMenu, tr("Add &Tab"));
		TreeSet<Map.Entry<String, ClientTabFactory>> entries = new TreeSet<>(new TabFactoryEntryComparator());
		for (Map.Entry<String, ClientTabFactory> entry : ClientConfiguration.getClientTabFactories().entrySet()) {
			// avoid reused entry instance
			entries.add(new AbstractMap.SimpleEntry<>(entry));
		}

		int lastPriority = Integer.MIN_VALUE;
		for (Map.Entry<String, ClientTabFactory> factoryEntry : entries) {
			String tabId = factoryEntry.getKey();
			ClientTabFactory factory = factoryEntry.getValue();
			if (lastPriority != Integer.MIN_VALUE
					&& (lastPriority & PRIO_BITMASK) != (factory.getPriority() & PRIO_BITMASK)) {
				tabMenu.addSeparator();
			}
			JMenuItem factoryItem = new JMenuItem(factory.getName());
			new IntentArguments("tab_add").putExtra("tabId", tabId).setMenu(factoryItem);
			tabMenu.add(factoryItem);
			lastPriority = factory.getPriority();
		}
		return tabMenu;
	}

	private JMenu getMenuApplication() {
		JMenu applicationMenu = new JMenu();
		Utility.setMnemonic(applicationMenu, tr("A&pplication"));
		JMenuItem configMenuItem = new JMenuItem();
		Utility.setMnemonic(configMenuItem, tr("&Configuration"));
		new IntentArguments("menu_config").setMenu(configMenuItem);
		applicationMenu.add(configMenuItem);

		applicationMenu.addSeparator();

		JMenuItem quitMenuItem = new JMenuItem();
		Utility.setMnemonic(quitMenuItem, tr("&Quit"));
		quitMenuItem.addActionListener(new IntentActionListener("menu_quit"));
		new IntentArguments("menu_quit").setMenu(quitMenuItem);
		applicationMenu.add(quitMenuItem);
		return applicationMenu;
	}

	private JMenu getMenuInfo() {
		JMenu infoMenu = new JMenu();
		Utility.setMnemonic(infoMenu, tr("&Information"));
		JMenuItem versionMenuItem = new JMenuItem();
		Utility.setMnemonic(versionMenuItem, tr("&About"));
		new IntentArguments("core").putExtra(IntentArguments.UNNAMED_ARG, "version").setMenu(versionMenuItem);
		infoMenu.add(versionMenuItem);
		return infoMenu;
	}

	private JButton getPostActionButton() {
		if (postActionButton == null) {
			postActionButton = new javax.swing.JButton();
			postActionButton.setText(tr("Post"));
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
			postBox.setFont(uiFont);
			ShortcutKeyManager.setKeyMap("postbox", postBox);
			postBox.addKeyListener(new KeyAdapter() {
				@Override
				public void keyReleased(KeyEvent e) {
					updatePostLength();
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
			postLengthLabel.setFont(uiFont);
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
											.addContainerGap(6, Short.MAX_VALUE)
							)
			);
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
			postToJMenu = new JMenu(tr("Post target account"));
		}
		return postToJMenu;
	}

	/*package*/JMenu getReadTimelineJMenu() {
		if (readTimelineJMenu == null) {
			readTimelineJMenu = new JMenu(tr("Read timeline account"));
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

	/*package*/JLabel getTweetViewCreatedAtLabel() {
		if (tweetViewCreatedAtLabel == null) {
			tweetViewCreatedAtLabel = new JLabel();
			tweetViewCreatedAtLabel.setHorizontalAlignment(JLabel.RIGHT);
			tweetViewCreatedAtLabel.setAlignmentX(RIGHT_ALIGNMENT);
			tweetViewCreatedAtLabel.addMouseListener(tweetViewListener);
		}
		return tweetViewCreatedAtLabel;
	}

	/*package*/JLabel getTweetViewCreatedByLabel() {
		if (tweetViewCreatedByLabel == null) {
			tweetViewCreatedByLabel = new JLabel();
			tweetViewCreatedByLabel.setText(tr("@elnetw"));
			tweetViewCreatedByLabel.setToolTipText(tr("from Dark Flame Master"));
			tweetViewCreatedByLabel.addMouseListener(tweetViewListener);
		}
		return tweetViewCreatedByLabel;
	}

	/*package*/JEditorPane getTweetViewEditorPane() {
		if (tweetViewEditorPane == null) {
			tweetViewEditorPane = new JEditorPane();
			tweetViewEditorPane.setEditable(false);
			tweetViewEditorPane.setContentType("text/html");
			tweetViewEditorPane.setFont(uiFont);
			tweetViewEditorPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
			tweetViewEditorPane.setEditorKit(new HTMLEditorKitExtension());
			tweetViewEditorPane.setText(tr("Welcome to %s!<br><b>YUKKURI SITE ITTENE!!</b>", APPLICATION_NAME));
			tweetViewEditorPane.addHyperlinkListener(new HyperlinkListener() {

				@Override
				public void hyperlinkUpdate(HyperlinkEvent e) {
					if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
						String url = e.getURL().toString();
						logger.debug(url);
						IntentArguments intentArguments = urlIntentMap.get(url);
						if (intentArguments != null) {
							intentArguments.invoke();
						} else {
							try {
								configuration.getUtility().openBrowser(url);
							} catch (Exception e1) {
								configuration.getMessageBus().getListeners(MessageBus.READER_ACCOUNT_ID,
										"error").onException(e1);
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
			tweetViewTextLayeredPane.setLayout(new LayeredLayoutManager());
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
			tweetViewUserIconLabel.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					getSelectingTab().getRenderer().onClientMessage(ClientEventConstants.EVENT_CLICKED_USERICON, e);
				}
			});
		}
		return tweetViewUserIconLabel;
	}

	@Override
	public Font getUiFont() {
		return uiFont;
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

	/**
	 * 例外を処理する。
	 *
	 * @param ex 例外
	 */
	private void handleException(Exception ex) {
		// TODO rootFilterService.onException(ex);
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

	/**
	 * This method is called from within the constructor to
	 * initialize the form.
	 */
	private void initComponents() {
		setTitle(VersionInfo.isSnapshot()
						? VersionInfo.getCodeName() + " (" + APPLICATION_NAME + ")"
						: APPLICATION_NAME
		);
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
		if (size == null) {
			setSize(800, 600);
		} else {
			setSize(size);
		}
	}

	/** アクションハンドラーテーブルを初期化する。 */
	private void initIntentTable() {
		configuration.addIntent("core", new CoreFrameIntent());
	}

	/*package*/boolean isFocusTab(int index) {
		return getViewTab().getSelectedIndex() == index;
	}

	private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
		stream.defaultReadObject();
		initIntentTable();
	}

	/*package*/void refreshTab(int indexOf, ClientTab tab) {
		JTabbedPane tabbedPane = getViewTab();
		tabbedPane.setIconAt(indexOf, tab.getIcon());
		tabbedPane.setTabComponentAt(indexOf, tab.getTitleComponent());
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
		tab.close();
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
		tweetLengthCalculator = newCalculator == null ? defaultTweetLengthCalculator : newCalculator;
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
		getTweetViewUserIconLabel().setCursor((actionFlag & SET_CURSOR_HAND) == 0 ? Cursor.getDefaultCursor()
				: Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
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

package jp.syuriken.snsw.twclient.handler;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.TreeSet;
import java.util.regex.Matcher;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.ViewFactory;
import javax.swing.text.html.HTMLEditorKit;

import jp.syuriken.snsw.twclient.ActionHandler;
import jp.syuriken.snsw.twclient.ClientConfiguration;
import jp.syuriken.snsw.twclient.ClientFrameApi;
import jp.syuriken.snsw.twclient.ClientProperties;
import jp.syuriken.snsw.twclient.DefaultClientTab;
import jp.syuriken.snsw.twclient.StatusData;
import jp.syuriken.snsw.twclient.StatusPanel;
import jp.syuriken.snsw.twclient.TabRenderer;
import jp.syuriken.snsw.twclient.internal.HTMLFactoryDelegator;
import jp.syuriken.snsw.twclient.internal.TwitterRunnable;

import com.twitter.Regex;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import twitter4j.DirectMessage;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.TwitterException;
import twitter4j.User;
import twitter4j.UserList;

/**
 * ユーザー情報を表示するアクションハンドラ
 *
 * @author Turenar <snswinhaiku dot lo at gmail dot com>
 */
public class UserInfoViewActionHandler implements ActionHandler {

	/*package*/static final class HTMLEditorKitExtension extends HTMLEditorKit {

		private static final long serialVersionUID = -4658062223359389042L;

		private transient HTMLFactory viewFactory = new HTMLFactoryDelegator();


		@Override
		public ViewFactory getViewFactory() {
			return viewFactory;
		}
	}

	/**
	 * ユーザー情報を表示するFrameTab
	 *
	 * @author Turenar <snswinhaiku dot lo at gmail dot com>
	 */
	public static class UserInfoFrameTab extends DefaultClientTab {

		/**
		 * 指定されたユーザーの発言のみを表示するレンダラ
		 *
		 * @author Turenar <snswinhaiku dot lo at gmail dot com>
		 */
		public class UserInfoTweetsRenderer extends DefaultRenderer {

			private final TreeSet<Long> treeSet = new TreeSet<Long>();


			@Override
			public void onBlock(User source, User blockedUser) {
				// do nothing
			}

			@Override
			public void onChangeAccount(boolean forWrite) {
				// do nothing
			}

			@Override
			public void onCleanUp() {
				// do nothing
			}

			@Override
			public void onConnect() {
				// do nothing
			}

			@Override
			public void onDeletionNotice(long directMessageId, long userId) {
				// do nothing
			}

			@Override
			public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
				// do nothing
			}

			@Override
			public void onDirectMessage(DirectMessage directMessage) {
				// do nothing
			}

			@Override
			public void onDisconnect() {
				// do nothing
			}

			@Override
			public void onFavorite(User source, User target, Status favoritedStatus) {
				// do nothing
			}

			@Override
			public void onFollow(User source, User followedUser) {
				// do nothing
			}

			@Override
			public void onFriendList(long[] friendIds) {
				// do nothing
			}

			@Override
			public void onScrubGeo(long userId, long upToStatusId) {
				// do nothing
			}

			@Override
			public void onStatus(Status originalStatus) {
				if (originalStatus.getUser().getId() == user.getId()) {
					synchronized (treeSet) {
						if (treeSet.contains(originalStatus.getId())) {
							return;
						}

						treeSet.add(originalStatus.getId());
					}
					addStatus(originalStatus);
				}
			}

			@Override
			public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
				// do nothing
			}

			@Override
			public void onUnblock(User source, User unblockedUser) {
				// do nothing
			}

			@Override
			public void onUnfavorite(User source, User target, Status unfavoritedStatus) {
				// do nothing
			}

			@Override
			public void onUserListCreation(User listOwner, UserList list) {
				// do nothing
			}

			@Override
			public void onUserListDeletion(User listOwner, UserList list) {
				// do nothing
			}

			@Override
			public void onUserListMemberAddition(User addedMember, User listOwner, UserList list) {
				// do nothing
			}

			@Override
			public void onUserListMemberDeletion(User deletedMember, User listOwner, UserList list) {
				// do nothing
			}

			@Override
			public void onUserListSubscription(User subscriber, User listOwner, UserList list) {
				// do nothing
			}

			@Override
			public void onUserListUnsubscription(User subscriber, User listOwner, UserList list) {
				// do nothing
			}

			@Override
			public void onUserListUpdate(User listOwner, UserList list) {
				// do nothing
			}

			@Override
			public void onUserProfileUpdate(User updatedUser) {
				// do nothing
			}

		}


		/** 指定されたユーザー */
		protected final User user;

		/** レンダラ */
		protected TabRenderer renderer = new UserInfoTweetsRenderer();

		private JScrollPane componentBio;

		private JLabel componentLocation;

		private JPanel componentOperationsPanel;

		private JLabel componentUserIcon;

		private JLabel componentUserName;

		private JLabel componentUserURL;

		private JPanel componentUserInfoPanel;

		private JPanel tabComponent;

		private boolean focusGained;

		private boolean isDirty;

		private StringBuilder stringBuilder = new StringBuilder();

		/*package*/JCheckBox muteCheckBox;

		private final Font operationFont = frameApi.getUiFont().deriveFont(frameApi.getUiFont().getSize() - 1);

		private JLabel componentTwitterLogo;


		/**
		 * インスタンスを生成する。
		 *
		 * @param clientConfiguration 設定
		 * @param user ユーザー
		 */
		public UserInfoFrameTab(ClientConfiguration clientConfiguration, User user) {
			super(clientConfiguration);
			this.user = user;
		}

		@Override
		public StatusPanel addStatus(StatusData statusData) {
			if (focusGained == false && isDirty == false) {
				isDirty = true;
				configuration.refreshTab(this);
			}
			return super.addStatus(statusData);
		}

		@Override
		public void focusGained() {
			super.focusGained();
			focusGained = true;
			isDirty = false;
			configuration.refreshTab(this);
		}

		@Override
		public void focusLost() {
			focusGained = false;
		}

		private Component getComponentBio() {
			if (componentBio == null) {
				componentBio = new JScrollPane();
				componentBio.setOpaque(false);
				JEditorPane componentBioEditorPane = new JEditorPane();
				componentBio.getViewport().setOpaque(false);
				componentBio.getViewport().setView(componentBioEditorPane);
				componentBioEditorPane.setEditorKit(new HTMLEditorKitExtension());
				componentBioEditorPane.setContentType("text/html");
				componentBioEditorPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
				componentBioEditorPane.setEditable(false);
				componentBioEditorPane.setFont(frameApi.getUiFont());
				componentBioEditorPane.addHyperlinkListener(new HyperlinkListener() {

					@Override
					public void hyperlinkUpdate(HyperlinkEvent e) {
						if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
							String url = e.getURL().toString();
							if (url.startsWith("http://command/")) {
								String command = url.substring("http://command/".length());
								handleAction(command);
							} else {
								openBrowser(url);
							}
						}
					}

				});

				Color bkgrnd = getComponentLocation().getBackground();
				componentBioEditorPane.setBackground(bkgrnd);
				componentBioEditorPane.setOpaque(false);

				String bio = user.getDescription();
				StringBuilder builder = stringBuilder;
				builder.setLength(0);
				builder.append("<html>");
				int index = 0;
				for (; index < bio.length();) {
					int end = bio.indexOf('\n', index);
					if (end == -1) {
						end = bio.length();
					}
					builder.append(bio, index, end).append("<br>");
					index = end + 1;
				}
				bio = builder.toString();

				StringBuffer buffer = new StringBuffer(bio.length());
				Matcher matcher = Regex.VALID_URL.matcher(bio);
				while (matcher.find()) {
					matcher.appendReplacement(buffer, "$" + Regex.VALID_URL_GROUP_BEFORE + "<a href='$"
							+ Regex.VALID_URL_GROUP_URL + "'>$" + Regex.VALID_URL_GROUP_URL + "</a>");
				}
				matcher.appendTail(buffer);
				bio = buffer.toString();

				buffer.setLength(0);
				matcher = Regex.AUTO_LINK_HASHTAGS.matcher(bio);
				while (matcher.find()) {
					matcher.appendReplacement(buffer, "$" + Regex.AUTO_LINK_HASHTAGS_GROUP_BEFORE
							+ "<a href='http://command/hashtag!$" + Regex.AUTO_LINK_HASHTAGS_GROUP_TAG + "'>$"
							+ Regex.AUTO_LINK_HASHTAGS_GROUP_HASH + "$" + Regex.AUTO_LINK_HASHTAGS_GROUP_TAG + "</a>");
				}
				matcher.appendTail(buffer);
				bio = buffer.toString();

				buffer.setLength(0);
				matcher = Regex.AUTO_LINK_USERNAMES_OR_LISTS.matcher(bio);
				while (matcher.find()) {
					String list = matcher.group(Regex.AUTO_LINK_USERNAME_OR_LISTS_GROUP_LIST);
					if (list == null) {
						matcher.appendReplacement(buffer, "$" + Regex.AUTO_LINK_USERNAME_OR_LISTS_GROUP_BEFORE
								+ "<a href='http://command/userinfo!$"
								+ Regex.AUTO_LINK_USERNAME_OR_LISTS_GROUP_USERNAME + "'>$"
								+ Regex.AUTO_LINK_USERNAME_OR_LISTS_GROUP_AT + "$"
								+ Regex.AUTO_LINK_USERNAME_OR_LISTS_GROUP_USERNAME + "</a>");
					} else {
						matcher.appendReplacement(buffer, "$" + Regex.AUTO_LINK_USERNAME_OR_LISTS_GROUP_BEFORE
								+ "<a href='http://command/list!$" + Regex.AUTO_LINK_USERNAME_OR_LISTS_GROUP_USERNAME
								+ "$" + Regex.AUTO_LINK_USERNAME_OR_LISTS_GROUP_LIST + "'>$"
								+ Regex.AUTO_LINK_USERNAME_OR_LISTS_GROUP_AT + "$"
								+ Regex.AUTO_LINK_USERNAME_OR_LISTS_GROUP_USERNAME + "$"
								+ Regex.AUTO_LINK_USERNAME_OR_LISTS_GROUP_LIST + "</a>");
					}
				}
				matcher.appendTail(buffer);

				componentBioEditorPane.setText(buffer.toString());
			}
			return componentBio;
		}

		private Component getComponentLocation() {
			if (componentLocation == null) {
				componentLocation = new JLabel();
				String location = user.getLocation();
				if (location != null) {
					stringBuilder.setLength(0);
					stringBuilder.append("<html><i>Location: </i>").append(location);
					componentLocation.setText(stringBuilder.toString());
				}
			}
			return componentLocation;
		}

		private JCheckBox getComponentMuteCheckBox() {
			if (muteCheckBox == null) {
				String idsString = configuration.getConfigProperties().getProperty("core.filter.user.ids");
				String[] ids = idsString.split(" ");
				String userIdString = String.valueOf(user.getId());
				boolean filtered = false;
				for (String id : ids) {
					if (id.equals(userIdString)) {
						filtered = true;
						break;
					}
				}
				muteCheckBox = new JCheckBox("ミュート", filtered);
				if (frameApi.getLoginUser().getId() == user.getId()) {
					muteCheckBox.setEnabled(false);
					muteCheckBox.setToolTipText("そ、それはあなたなんだからね！");
				}
				muteCheckBox.setFont(operationFont);
				muteCheckBox.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						ClientProperties configProperties = getConfigProperties();
						String idsString = configProperties.getProperty("core.filter.user.ids");
						if (muteCheckBox.isSelected()) {
							idsString =
									idsString == null || idsString.trim().isEmpty() ? String.valueOf(user.getId())
											: idsString + " " + user.getId();
						} else {
							idsString = idsString == null ? "" : idsString.replace(String.valueOf(user.getId()), "");
						}
						configProperties.setProperty("core.filter.user.ids", idsString);
					}
				});
			}
			return muteCheckBox;
		}

		private Component getComponentOperationsPanel() {
			if (componentOperationsPanel == null) {
				componentOperationsPanel = new JPanel(); //TODO
				componentOperationsPanel.setLayout(new BoxLayout(componentOperationsPanel, BoxLayout.Y_AXIS));
				try {
					final JLabel closeIcon =
							new JLabel(
									new ImageIcon(ImageIO.read(UserInfoFrameTab.class.getResource("../close16.png"))));
					closeIcon.setText("閉じる");
					closeIcon.setFont(operationFont);
					closeIcon.addMouseListener(new MouseAdapter() {

						@Override
						public void mouseClicked(MouseEvent e) {
							removeFrameTab(UserInfoFrameTab.this);
						}
					});
					componentOperationsPanel.add(closeIcon);
				} catch (IOException e) {
					logger.warn("#getComponentOperationsPanel: Failed load resource");
				}

				componentOperationsPanel.add(getComponentMuteCheckBox());
			}
			return componentOperationsPanel;
		}

		private Component getComponentTweetsScrollPane() {
			return getScrollPane();
		}

		private Component getComponentTwitterLogo() {
			if (componentTwitterLogo == null) {
				Image scaledInstance = IMG_TWITTER_LOGO.getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH);
				componentTwitterLogo = new JLabel(new ImageIcon(scaledInstance));
				componentTwitterLogo.setMaximumSize(new Dimension(16, 16));
			}
			return componentTwitterLogo;
		}

		private Component getComponentUserIcon() {
			if (componentUserIcon == null) {
				componentUserIcon = new JLabel();
				frameApi.getImageCacher().setImageIcon(componentUserIcon, user);
			}
			return componentUserIcon;
		}

		private JPanel getComponentUserInfo() {
			if (componentUserInfoPanel == null) {
				componentUserInfoPanel = new JPanel();
				GroupLayout layout = new GroupLayout(componentUserInfoPanel);
				componentUserInfoPanel.setLayout(layout);
				layout.setVerticalGroup(layout
					.createParallelGroup(Alignment.LEADING)
					.addGroup(layout.createSequentialGroup().addGap(4, 4, 4) //
						.addComponent(getComponentUserIcon(), 48, 48, 48).addContainerGap(4, 4) //
						.addComponent(getComponentOperationsPanel()))
					.addGroup(
							layout
								.createSequentialGroup()
								.addGroup(
										layout.createParallelGroup()
											.addComponent(getComponentTwitterLogo(), Alignment.CENTER, 16, 16, 16)
											.addComponent(getComponentUserName()) //
											.addComponent(getComponentLocation()))
								//
								.addGap(4, 4, 4).addComponent(getComponentUserURL())
								//
								.addGap(4, 4, 4).addComponent(getComponentBio())));

				layout.setHorizontalGroup(layout
					.createSequentialGroup()
					.addGap(4, 4, 4)
					.addGroup(
							layout.createParallelGroup()
								.addComponent(getComponentUserIcon(), Alignment.CENTER, 48, 48, 48) //
								.addComponent(getComponentOperationsPanel()))
					.addGap(4, 4, 4)
					.addGroup(
							layout
								.createParallelGroup()
								.addGroup(
										layout
											.createSequentialGroup()
											.addComponent(getComponentTwitterLogo(), 16, 16, 16)
											.addComponent(getComponentUserName(), 64, GroupLayout.DEFAULT_SIZE,
													GroupLayout.PREFERRED_SIZE)
											.addGap(16, 128, 128)
											.addComponent(getComponentLocation(), 64, GroupLayout.DEFAULT_SIZE,
													GroupLayout.DEFAULT_SIZE)) //
								.addComponent(getComponentUserURL()) //
								.addComponent(getComponentBio(), 64, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)));
			}
			return componentUserInfoPanel;
		}

		private Component getComponentUserName() {
			if (componentUserName == null) {
				componentUserName = new JLabel();
				componentUserName.setText(MessageFormat.format("@{0} ({1})", user.getScreenName(), user.getName()));
			}
			return componentUserName;
		}

		private Component getComponentUserURL() {
			if (componentUserURL == null) {
				componentUserURL = new JLabel();
				if (user.getURL() != null) {
					stringBuilder.setLength(0);
					stringBuilder.append("<html>URL:&nbsp;<a style='color:blue;text-decoration: underline;'>");
					stringBuilder.append(user.getURL()).append("</a>");
					componentUserURL.setText(stringBuilder.toString());
				}
				componentUserURL.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				componentUserURL.addMouseListener(new MouseAdapter() {

					@Override
					public void mouseClicked(MouseEvent e) {
						if (user.getURL() != null) {
							handleAction("url!" + user.getURL());
						}
					}
				});
			}
			return componentUserURL;
		}

		/*package*/ClientProperties getConfigProperties() {
			return configProperties;
		}

		@Override
		public Icon getIcon() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public TabRenderer getRenderer() {
			return renderer;
		}

		@Override
		public JComponent getTabComponent() {
			tabComponent = new JPanel();
			GroupLayout layout = new GroupLayout(tabComponent);
			tabComponent.setLayout(layout);
			layout.setVerticalGroup( //
				layout.createSequentialGroup()
					.addComponent(getComponentUserInfo(), 128, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
					.addComponent(getComponentTweetsScrollPane()));

			layout.setHorizontalGroup(layout.createParallelGroup()
				.addComponent(getComponentUserInfo(), 96, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE) //
				.addComponent(getComponentTweetsScrollPane()));
			return tabComponent;
		}

		@Override
		public String getTitle() {
			stringBuilder.setLength(0);
			stringBuilder.append('@').append(user.getScreenName());
			if (isDirty) {
				stringBuilder.append('*');
			}
			return stringBuilder.toString();
		}

		@Override
		public String getToolTip() {
			return user.getName() + " のユーザー情報";
		}

		@Override
		public void initTimeline() {
			// use other way for display requirements...
			//super.initTimeline();
		}

		/**
		 * ブラウザを開く
		 *
		 * @param url URL
		 */
		protected void openBrowser(String url) {
			try {
				configuration.getUtility().openBrowser(url);
			} catch (Exception e1) {
				getRenderer().onException(e1);
			}
		}

		/*package*/void removeFrameTab(UserInfoFrameTab tab) {
			configuration.removeFrameTab(tab);
		}
	}

	/**
	 * ユーザータイムラインfetcher
	 *
	 * @author Turenar <snswinhaiku dot lo at gmail dot com>
	 */
	protected static final class UserTimelineFetcher extends TwitterRunnable {

		private final UserInfoFrameTab tab;

		private final long userId;

		private final ClientFrameApi api;


		private UserTimelineFetcher(UserInfoFrameTab tab, long userId, ClientFrameApi api) {
			this.tab = tab;
			this.userId = userId;
			this.api = api;
		}

		@Override
		protected void access() throws TwitterException {
			ResponseList<Status> timeline = api.getTwitterForRead().getUserTimeline(userId);
			for (Status status : timeline) {
				tab.getRenderer().onStatus(status);
			}
			for (Status status : getConfiguration().getCacheManager().getStatusSet()) {
				if (status.getUser().getId() == userId) {
					tab.getRenderer().onStatus(status);
				}
			}
		}

		@Override
		protected ClientConfiguration getConfiguration() {
			return api.getClientConfiguration();
		}

		@Override
		protected void handleException(TwitterException ex) {
			tab.getRenderer().onException(ex);
		}
	}


	private static Logger logger = LoggerFactory.getLogger(UserInfoViewActionHandler.class);


	@Override
	public JMenuItem createJMenuItem(String commandName) {
		JMenuItem aboutMenuItem = new JMenuItem("ユーザーについて(A)...", KeyEvent.VK_A);
		return aboutMenuItem;
	}

	@Override
	public void handleAction(final String actionName, final StatusData statusData, final ClientFrameApi api) {
		User user = null;
		if (actionName.contains("!")) {
			user =
					api.getClientConfiguration().getCacheManager()
						.getUser(Long.parseLong(actionName.substring(actionName.indexOf('!') + 1)));
		} else if (statusData != null && statusData.tag instanceof Status) {
			Status status = (Status) statusData.tag;
			if (status.isRetweet()) {
				status = status.getRetweetedStatus();
			}
			user = status.getUser();
		} else {
			throw new IllegalArgumentException(
					"[userinfo AH] must call as userinfo!<screenName> or must statusData.tag is Status");
		}

		final UserInfoFrameTab tab = new UserInfoFrameTab(api.getClientConfiguration(), user);
		final long userId = user.getId();
		api.addJob(new UserTimelineFetcher(tab, userId, api));
		api.getClientConfiguration().addFrameTab(tab);
		api.getClientConfiguration().focusFrameTab(tab);
	}

	@Override
	public void popupMenuWillBecomeVisible(JMenuItem menuItem, StatusData statusData, ClientFrameApi api) {
		if ((statusData.isSystemNotify() == false) && (statusData.tag instanceof Status)) {
			Status status = (Status) statusData.tag;
			if (status.isRetweet()) {
				status = status.getRetweetedStatus();
			}
			menuItem.setText(MessageFormat.format("@{0} ({1}) について(A)", status.getUser().getScreenName(), status
				.getUser().getName()));
			menuItem.setEnabled(true);
		} else {
			menuItem.setEnabled(false);
		}
	}
}

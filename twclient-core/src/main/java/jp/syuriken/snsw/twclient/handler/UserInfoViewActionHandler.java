package jp.syuriken.snsw.twclient.handler;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.regex.Matcher;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

import jp.syuriken.snsw.twclient.ActionHandler;
import jp.syuriken.snsw.twclient.ClientConfiguration;
import jp.syuriken.snsw.twclient.ClientFrameApi;
import jp.syuriken.snsw.twclient.ClientProperties;
import jp.syuriken.snsw.twclient.DefaultClientTab;
import jp.syuriken.snsw.twclient.StatusData;
import jp.syuriken.snsw.twclient.StatusPanel;
import jp.syuriken.snsw.twclient.TabRenderer;
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
 * @author $Author$
 */
public class UserInfoViewActionHandler implements ActionHandler {
	
	/**
	 * ユーザー情報を表示するFrameTab
	 * 
	 * @author $Author$
	 */
	public static class UserInfoFrameTab extends DefaultClientTab {
		
		/**
		 * 指定されたユーザーの発言のみを表示するレンダラ
		 * 
		 * @author $Author$
		 */
		public class UserInfoTweetsRenderer extends DefaultRenderer {
			
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
			public void onException(Exception ex) {
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
			public void onRetweet(User source, User target, Status retweetedStatus) {
				// do nothing
			}
			
			@Override
			public void onScrubGeo(long userId, long upToStatusId) {
				// do nothing
			}
			
			@Override
			public void onStatus(Status originalStatus) {
				if (originalStatus.getUser().getId() == user.getId()) {
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
		
		private JLabel componentBio;
		
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
		
		private JCheckBox muteCheckBox;
		
		private final Font operationFont = frameApi.getUiFont().deriveFont(frameApi.getUiFont().getSize() - 1);
		
		
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
				componentBio = new JLabel();
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
				
				componentBio.setText(buffer.toString());
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
						ClientProperties configProperties = configuration.getConfigProperties();
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
							new JLabel(new ImageIcon(ImageIO.read(getClass().getResource("../close16.png"))));
					closeIcon.setText("閉じる");
					closeIcon.setFont(operationFont);
					closeIcon.addMouseListener(new MouseAdapter() {
						
						@Override
						public void mouseClicked(MouseEvent e) {
							configuration.removeFrameTab(UserInfoFrameTab.this);
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
		
		private Component getComponentUserIcon() {
			if (componentUserIcon == null) {
				componentUserIcon = new JLabel();
				frameApi.getImageCacher().setImageIcon(componentUserIcon, user);
			}
			return componentUserIcon;
		}
		
		private JPanel getComponentUserInfo() {
			if (componentUserIcon == null) {
				componentUserInfoPanel = new JPanel();
			}
			GroupLayout layout = new GroupLayout(componentUserInfoPanel);
			componentUserInfoPanel.setLayout(layout);
			layout.setVerticalGroup(layout
				.createParallelGroup(Alignment.LEADING)
				.addGroup(layout.createSequentialGroup().addGap(4, 4, 4) // 
					.addComponent(getComponentUserIcon(), 48, 48, 48).addContainerGap(4, 4) // 
					.addComponent(getComponentOperationsPanel()))
				.addGroup(
						layout.createSequentialGroup()
							.addGroup(layout.createParallelGroup().addComponent(getComponentUserName()) //
								.addComponent(getComponentLocation())) //
							.addContainerGap(4, 4).addComponent(getComponentUserURL()) //
							.addContainerGap(4, 4).addComponent(getComponentBio())));
			layout
				.setHorizontalGroup(layout
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
											.addComponent(getComponentUserName(), 64, GroupLayout.DEFAULT_SIZE,
													GroupLayout.PREFERRED_SIZE)
											.addGap(16, 128, 128)
											.addComponent(getComponentLocation(), 64, GroupLayout.DEFAULT_SIZE,
													GroupLayout.DEFAULT_SIZE)) //
								.addComponent(getComponentUserURL()) //
								.addComponent(getComponentBio(), 64, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE)));
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
					stringBuilder.append(user.getURL().toString()).append("</a>");
					componentUserURL.setText(stringBuilder.toString());
				}
				componentUserURL.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				componentUserURL.addMouseListener(new MouseAdapter() {
					
					@Override
					public void mouseClicked(MouseEvent e) {
						if (user.getURL() != null) {
							frameApi.handleAction("url!" + user.getURL().toString(), null);
						}
					}
				});
			}
			return componentUserURL;
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
				layout
					.createSequentialGroup()
					.addComponent(getComponentUserInfo(), 72, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
					.addComponent(getComponentTweetsScrollPane(), 120, GroupLayout.DEFAULT_SIZE,
							GroupLayout.DEFAULT_SIZE));
			
			layout.setHorizontalGroup(layout.createParallelGroup()
				.addComponent(getComponentUserInfo(), 96, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE) // 
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
	}
	
	
	private static Logger logger = LoggerFactory.getLogger(UserInfoViewActionHandler.class);
	
	
	@Override
	public JMenuItem createJMenuItem(String commandName) {
		JMenuItem aboutMenuItem = new JMenuItem("ユーザーについて(A)...", KeyEvent.VK_A);
		return aboutMenuItem;
	}
	
	@Override
	public void handleAction(String actionName, final StatusData statusData, final ClientFrameApi api) {
		User user = null;
		if (actionName.contains("!")) {
			for (int i = 10; i > 0; i--) {
				try {
					user = api.getTwitterForRead().showUser(actionName.substring(actionName.indexOf('!') + 1)); //TODO
					break;
				} catch (TwitterException e) {
					if (e.getStatusCode() == TwitterException.SERVICE_UNAVAILABLE) {
						if (i != 1) { //not last
							continue;
						}
					}
					throw new RuntimeException("Failed Twitter#showUser", e);
				}
			}
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
		api.addJob(new TwitterRunnable() {
			
			@Override
			protected void access() throws TwitterException {
				ResponseList<Status> timeline = api.getTwitterForRead().getUserTimeline(userId);
				for (Status status : timeline) {
					tab.getRenderer().onStatus(status);
				}
				
			}
			
			@Override
			protected ClientConfiguration getConfiguration() {
				return api.getClientConfiguration();
			}
			
			@Override
			protected void handleException(TwitterException ex) {
				getConfiguration().getRootFilterService().onException(ex);
			}
		});
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

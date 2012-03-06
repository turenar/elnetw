package jp.syuriken.snsw.twclient.handler;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.text.MessageFormat;

import javax.imageio.ImageIO;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

import jp.syuriken.snsw.twclient.ActionHandler;
import jp.syuriken.snsw.twclient.ClientConfiguration;
import jp.syuriken.snsw.twclient.ClientFrameApi;
import jp.syuriken.snsw.twclient.DefaultClientTab;
import jp.syuriken.snsw.twclient.ParallelRunnable;
import jp.syuriken.snsw.twclient.StatusData;
import jp.syuriken.snsw.twclient.StatusPanel;
import jp.syuriken.snsw.twclient.TabRenderer;

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
				StringBuilder builder = new StringBuilder("<html>");
				int index = 0;
				for (; index < bio.length();) {
					int end = bio.indexOf('\n', index);
					if (end == -1) {
						end = bio.length();
					}
					builder.append(bio, index, end).append("<br>");
					index = end + 1;
				}
				componentBio.setText(builder.toString());
			}
			return componentBio;
		}
		
		private Component getComponentLocation() {
			if (componentLocation == null) {
				componentLocation = new JLabel();
				stringBuilder.setLength(0);
				stringBuilder.append("<html><i>Location: </i>").append(user.getLocation());
				componentLocation.setText(stringBuilder.toString());
			}
			return componentLocation;
		}
		
		private Component getComponentOperationsPanel() {
			if (componentOperationsPanel == null) {
				componentOperationsPanel = new JPanel(); //TODO
				try {
					JLabel closeIcon =
							new JLabel(new ImageIcon(ImageIO.read(getClass().getResource("../close16.png"))));
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
			layout.setVerticalGroup(layout.createParallelGroup(Alignment.LEADING)
			//
				.addGroup(layout.createSequentialGroup().addGap(4, 4, 4) // 
					.addComponent(getComponentUserIcon(), 48, 48, 48).addContainerGap(4, 4) // 
					.addComponent(getComponentOperationsPanel()))
				// 
				.addGroup(layout.createSequentialGroup()
				//
					.addGroup(layout.createParallelGroup()
					//
						.addComponent(getComponentUserName()) //
						.addComponent(getComponentLocation())) //
					.addContainerGap(4, 4).addComponent(getComponentUserURL()) //
					.addContainerGap(4, 4).addComponent(getComponentBio())));
			layout.setHorizontalGroup(layout.createSequentialGroup()
			// 
				.addGap(4, 4, 4).addGroup(layout.createParallelGroup()
				//
					.addGroup(layout.createSequentialGroup().addComponent(getComponentUserIcon(), 48, 48, 48)) //
					.addComponent(getComponentOperationsPanel(), 48, 48, 48)) //
				.addGap(4, 4, 4).addGroup(layout.createParallelGroup()
				// 
					.addGroup(layout.createSequentialGroup()
					//
						.addComponent(getComponentUserName(), 64, GroupLayout.DEFAULT_SIZE, 512) // 
						.addComponent(getComponentLocation(), 64, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE)) //
					.addComponent(getComponentUserURL()) //
					.addComponent(getComponentBio())));
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
				layout.createSequentialGroup()
				//
					.addComponent(getComponentUserInfo(), 72, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
					//
					.addComponent(getComponentTweetsScrollPane(), 120, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE));
			
			layout.setHorizontalGroup(layout.createParallelGroup()
			//
				.addComponent(getComponentUserInfo(), 96, GroupLayout.DEFAULT_SIZE, 512) // 
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
		if (statusData.tag instanceof Status) {
			final User user = ((Status) statusData.tag).getUser();
			final UserInfoFrameTab tab = new UserInfoFrameTab(api.getClientConfiguration(), user);
			api.addJob(new ParallelRunnable() {
				
				@Override
				public void run() {
					try {
						ResponseList<Status> timeline = api.getTwitterForRead().getUserTimeline(user.getId());
						for (Status status : timeline) {
							tab.getRenderer().onStatus(status);
						}
					} catch (TwitterException e) {
						api.handleException(e);
					}
				}
			});
			api.getClientConfiguration().addFrameTab(tab);
			api.getClientConfiguration().focusFrameTab(tab);
		}
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

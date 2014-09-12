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

package jp.mydns.turenar.twclient.gui.tab;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.ViewFactory;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

import com.twitter.Regex;
import jp.mydns.turenar.twclient.ClientConfiguration;
import jp.mydns.turenar.twclient.ClientProperties;
import jp.mydns.turenar.twclient.ParallelRunnable;
import jp.mydns.turenar.twclient.cache.AbstractImageSetter;
import jp.mydns.turenar.twclient.gui.BackgroundImagePanel;
import jp.mydns.turenar.twclient.gui.ImageResource;
import jp.mydns.turenar.twclient.gui.ImageViewerFrame;
import jp.mydns.turenar.twclient.handler.IntentArguments;
import jp.mydns.turenar.twclient.handler.UserInfoViewActionHandler;
import jp.mydns.turenar.twclient.internal.HTMLFactoryDelegator;
import jp.mydns.turenar.twclient.internal.TwitterRunnable;
import jp.mydns.turenar.twclient.twitter.TwitterUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.User;

import static javax.swing.GroupLayout.PREFERRED_SIZE;

/**
 * ユーザー情報を表示するFrameTab
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class UserInfoFrameTab extends AbstractClientTab {

	private static final class HTMLEditorKitExtension extends HTMLEditorKit {

		private static final long serialVersionUID = 7554202708087468592L;
		private transient HTMLFactory viewFactory = new HTMLFactoryDelegator();

		@Override
		public ViewFactory getViewFactory() {
			return viewFactory;
		}
	}

	/**
	 * 指定されたユーザーの発言のみを表示するレンダラ
	 *
	 * @author Turenar (snswinhaiku dot lo at gmail dot com)
	 */
	public class UserInfoTweetsRenderer extends DelegateRenderer {

		@Override
		public void onStatus(Status originalStatus) {
			if (user.getId() == originalStatus.getUser().getId()) {
				actualRenderer.onStatus(originalStatus);
			}
		}
	}

	private static final String TAB_ID = "userinfo";
	private static final Logger logger = LoggerFactory.getLogger(UserInfoFrameTab.class);
	public static final Dimension NEW_BANNER_SIZE = new Dimension(600, 200);

	private static Color setAlpha(Color color, int alpha) {
		return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
	}

	private final Font operationFont = frameApi.getUiFont().deriveFont(Font.PLAIN, frameApi.getUiFont().getSize() - 1);
	private final HashMap<String, IntentArguments> urlIntentMap;
	/** 指定されたユーザー */
	protected TwitterUser user;
	/** レンダラ */
	protected DelegateRenderer renderer = new UserInfoTweetsRenderer();
	private JScrollPane componentBio;
	private JLabel componentLocation;
	private JPanel componentOperationsPanel;
	private JLabel componentUserIcon;
	private JLabel componentUserName;
	private JLabel componentUserURL;
	private BackgroundImagePanel componentUserInfoPanel;
	private JPanel tabComponent;
	/*package*/ JCheckBoxMenuItem muteCheckBox;
	private JLabel componentTwitterLogo;
	private JEditorPane componentBioEditorPane;
	private ImageIcon imageIcon;
	private int nextUrlId = 0;
	private JComponent componentOperationBox;
	private JMenuItem showHeaderItem;

	/**
	 * インスタンスを生成する。
	 *
	 * @param uniqId unique identifier
	 */
	public UserInfoFrameTab(String uniqId) {
		super(uniqId);
		final long userId = configProperties.getLong(getPropertyPrefix() + ".targetUserId");
		configuration.getMessageBus().establish(accountId, "statuses/user_timeline?" + userId, getRenderer());
		configuration.addJob(new ParallelRunnable() {
			@Override
			public void run() {
				setUser(configuration.getCacheManager().getUser(userId));
			}
		});
		urlIntentMap = new HashMap<>();
	}

	/**
	 * インスタンスを生成する。
	 *
	 * @param accountId        account id
	 * @param targetScreenName target screen name
	 * @see #UserInfoFrameTab(String, jp.mydns.turenar.twclient.twitter.TwitterUser)
	 */
	public UserInfoFrameTab(final String accountId, final String targetScreenName) {
		super(accountId);
		configuration.addJob(new TwitterRunnable() {
			@Override
			protected void access() throws TwitterException {
				User userInfo = configuration.getTwitter(configuration.getMessageBus().getActualUserIdString(accountId))
						.showUser(targetScreenName);
				TwitterUser user = TwitterUser.getInstance(userInfo);
				setUser(configuration.getCacheManager().cacheUser(user));
			}

			@Override
			protected void onException(TwitterException ex) {
				UserInfoFrameTab.this.getRenderer().onException(ex);
			}
		});
		urlIntentMap = new HashMap<>();
	}

	/**
	 * インスタンスを生成する。
	 *
	 * @param accountId reader account id
	 * @param user      ユーザー
	 */
	public UserInfoFrameTab(String accountId, TwitterUser user) {
		super(accountId);
		setUser(user);
		urlIntentMap = new HashMap<>();
	}

	private String getBioHtml() {
		String bio = user.getDescription();
		StringBuilder builder = new StringBuilder();
		int index = 0;
		for (; index < bio.length(); ) {
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
					+ Regex.VALID_URL_GROUP_URL + "'>$" + Regex.VALID_URL_GROUP_URL
					+ "</a>");
		}
		matcher.appendTail(buffer);
		bio = buffer.toString();

		buffer.setLength(0);
		matcher = Regex.VALID_HASHTAG.matcher(bio);
		while (matcher.find()) {
			IntentArguments intent = getIntentArguments("hashtag");
			intent.putExtra("name", matcher.group(Regex.VALID_HASHTAG_GROUP_TAG));

			matcher.appendReplacement(buffer, "$" + Regex.VALID_HASHTAG_GROUP_BEFORE
					+ "<a href='" + getCommandUrl(intent) + "'>$"
					+ Regex.VALID_HASHTAG_GROUP_HASH + "$" + Regex.VALID_HASHTAG_GROUP_TAG + "</a>");
		}
		matcher.appendTail(buffer);
		bio = buffer.toString();

		buffer.setLength(0);
		matcher = Regex.VALID_MENTION_OR_LIST.matcher(bio);
		buffer.append("<html><span></span><span>");
		while (matcher.find()) {
			String list = matcher.group(Regex.VALID_MENTION_OR_LIST_GROUP_LIST);
			if (list == null) {
				IntentArguments intent = getIntentArguments("userinfo");
				intent.putExtra("screenName", matcher.group(Regex.VALID_MENTION_OR_LIST_GROUP_USERNAME));

				matcher.appendReplacement(buffer, "$" + Regex.VALID_MENTION_OR_LIST_GROUP_BEFORE
						+ "<a href='" + getCommandUrl(intent) + "'>$"
						+ Regex.VALID_MENTION_OR_LIST_GROUP_AT + "$"
						+ Regex.VALID_MENTION_OR_LIST_GROUP_USERNAME + "</a>");
			} else {
				IntentArguments intent = getIntentArguments("list");
				intent.putExtra("user", matcher.group(Regex.VALID_MENTION_OR_LIST_GROUP_USERNAME));
				intent.putExtra("listName", matcher.group(Regex.VALID_MENTION_OR_LIST_GROUP_LIST));

				matcher.appendReplacement(buffer, "$" + Regex.VALID_MENTION_OR_LIST_GROUP_BEFORE
						+ "<a href='" + getCommandUrl(intent) + "'>$"
						+ Regex.VALID_MENTION_OR_LIST_GROUP_AT + "$"
						+ Regex.VALID_MENTION_OR_LIST_GROUP_USERNAME + "$"
						+ Regex.VALID_MENTION_OR_LIST_GROUP_LIST + "</a>");
			}
		}
		matcher.appendTail(buffer);
		return buffer.toString();
	}

	/*package*/ClientConfiguration getClientConfiguration() {
		return configuration;
	}

	/**
	 * get virtual url for intent
	 *
	 * @param intentArguments intent
	 * @return virtual url
	 */
	protected String getCommandUrl(IntentArguments intentArguments) {
		String url = "http://command/?id=" + (nextUrlId++);
		urlIntentMap.put(url, intentArguments);
		return url;
	}

	private JScrollPane getComponentBio() {
		if (componentBio == null) {
			componentBio = new JScrollPane();
			componentBio.setOpaque(false);
			componentBio.getViewport().setOpaque(false);
			componentBio.getViewport().setView(getComponentBioEditorPane());
		}
		return componentBio;
	}

	private JEditorPane getComponentBioEditorPane() {
		if (componentBioEditorPane == null) {
			componentBioEditorPane = new JEditorPane();
			HTMLEditorKitExtension kit = new HTMLEditorKitExtension();
			StyleSheet styleSheet = new StyleSheet();
			styleSheet.addRule("span { color: #ffffff; }");
			styleSheet.addRule("a { color: #80ffff; }");
			kit.setStyleSheet(styleSheet);
			componentBioEditorPane.setEditorKit(kit);
			componentBioEditorPane.setContentType("text/html");
			componentBioEditorPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
			componentBioEditorPane.setEditable(false);
			componentBioEditorPane.setFont(frameApi.getUiFont());
			componentBioEditorPane.addHyperlinkListener(new HyperlinkListener() {

				@Override
				public void hyperlinkUpdate(HyperlinkEvent e) {
					if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
						String url = e.getURL().toString();
						IntentArguments intentArguments = urlIntentMap.get(url);
						if (intentArguments != null) {
							intentArguments.invoke();
						} else {
							try {
								getConfiguration().getUtility().openBrowser(url);
							} catch (Exception e1) {
								getRenderer().onException(e1);
							}
						}
					}
				}
			});

			componentBioEditorPane.setBackground(getComponentLocation().getBackground());
			componentBioEditorPane.setOpaque(false);
			componentBioEditorPane.setForeground(Color.WHITE);
			componentBioEditorPane.setText("読込中...");
		}
		return componentBioEditorPane;
	}

	private JLabel getComponentLocation() {
		if (componentLocation == null) {
			componentLocation = new JLabel();
			componentLocation.setForeground(Color.WHITE);
		}
		return componentLocation;
	}

	private JCheckBoxMenuItem getComponentMuteCheckBox() {
		if (muteCheckBox == null) {
			muteCheckBox = new JCheckBoxMenuItem("ミュート");
			muteCheckBox.setEnabled(false);
			muteCheckBox.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					ClientProperties configProperties = getConfiguration().getConfigProperties();
					List<String> idsList = configProperties.getList("core.filter.user.ids");
					if (muteCheckBox.isSelected()) {
						idsList.add(String.valueOf(user.getId()));
					} else {
						idsList.remove(String.valueOf(user.getId()));
					}
				}
			});
		}
		return muteCheckBox;
	}

	private JComponent getComponentOperationBox() {
		if (componentOperationBox == null) {
			componentOperationBox = new JButton("その他▼");
			componentOperationBox.setFont(operationFont);
			componentOperationBox.setAlignmentX(Component.CENTER_ALIGNMENT);
			final JPopupMenu jPopupMenu = new JPopupMenu();
			showHeaderItem = new JMenuItem("ヘッダーを表示");
			showHeaderItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					try {
						new ImageViewerFrame(new URL(user.getProfileBannerLargeURL())).setVisible(true);
					} catch (MalformedURLException e1) {
						throw new AssertionError(e1);
					}
				}
			});

			jPopupMenu.add(showHeaderItem);
			jPopupMenu.add(getComponentMuteCheckBox());
			componentOperationBox.addMouseListener(new MouseAdapter() {
				@Override
				public void mousePressed(MouseEvent e) {
					jPopupMenu.show(componentOperationBox, e.getX(), e.getY());
				}
			});
		}
		return componentOperationBox;
	}

	private Component getComponentOperationsPanel() {
		if (componentOperationsPanel == null) {
			componentOperationsPanel = new JPanel(); //TODO
			componentOperationsPanel.setBackground(setAlpha(componentOperationsPanel.getBackground(), 192));
			componentOperationsPanel.setLayout(new BoxLayout(componentOperationsPanel, BoxLayout.Y_AXIS));
			try {
				final JLabel closeIcon =
						new JLabel(new ImageIcon(ImageIO.read(UserInfoViewActionHandler.class
								.getResource("/jp/mydns/turenar/twclient/img/close16.png"))));
				closeIcon.setText("閉じる");
				closeIcon.setFont(operationFont);
				closeIcon.addMouseListener(new MouseAdapter() {

					@Override
					public void mouseClicked(MouseEvent e) {
						getConfiguration().removeFrameTab(UserInfoFrameTab.this);
					}
				});
				componentOperationsPanel.add(closeIcon);
			} catch (IOException e) {
				logger.warn("#getComponentOperationsPanel: Failed load resource");
			}

			componentOperationsPanel.add(getComponentOperationBox());
		}
		return componentOperationsPanel;
	}

	private Component getComponentTweetsScrollPane() {
		return getScrollPane();
	}

	private Component getComponentTwitterLogo() {
		if (componentTwitterLogo == null) {
			Image scaledInstance = ImageResource.getImgTwitterLogo().getImage().getScaledInstance(16, 16,
					Image.SCALE_SMOOTH);
			componentTwitterLogo = new JLabel(new ImageIcon(scaledInstance));
			componentTwitterLogo.setMaximumSize(new Dimension(16, 16));
		}
		return componentTwitterLogo;
	}

	private JLabel getComponentUserIcon() {
		if (componentUserIcon == null) {
			componentUserIcon = new JLabel();
			componentUserIcon.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			componentUserIcon.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					try {
						new ImageViewerFrame(new URL(user.getOriginalProfileImageURLHttps())).setVisible(true);
					} catch (MalformedURLException ex) {
						logger.warn("conversion of originalProfileImageURLHttps to URL failed", ex);
					}
				}
			});
		}
		return componentUserIcon;
	}

	private BackgroundImagePanel getComponentUserInfo() {
		if (componentUserInfoPanel == null) {
			componentUserInfoPanel = new BackgroundImagePanel();
			GroupLayout layout = new GroupLayout(componentUserInfoPanel);
			componentUserInfoPanel.setLayout(layout);
			layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
					.addGroup(layout.createSequentialGroup().addGap(4, 4, 4)
							.addComponent(getComponentUserIcon(), 48, 48, 48).addContainerGap(4, 4)
							.addComponent(getComponentOperationsPanel()))
					.addGroup(layout.createSequentialGroup()
							.addGroup(layout.createParallelGroup()
									.addComponent(getComponentTwitterLogo(), GroupLayout.Alignment.CENTER, 16, 16, 16)
									.addComponent(getComponentUserName())
									.addComponent(getComponentLocation()))
							.addGap(4, 4, 4).addComponent(getComponentUserURL())
							.addGap(4, 4, 4).addComponent(getComponentBio())));

			layout.setHorizontalGroup(layout.createSequentialGroup().addGap(4, 4, 4)
					.addGroup(layout.createParallelGroup()
							.addComponent(getComponentUserIcon(), GroupLayout.Alignment.CENTER, 48, 48, 48)
							.addComponent(getComponentOperationsPanel()))
					.addGap(4, 4, 4)
					.addGroup(layout.createParallelGroup()
							.addGroup(layout.createSequentialGroup()
									.addComponent(getComponentTwitterLogo(), 16, 16, 16)
									.addComponent(getComponentUserName(), 64, GroupLayout.DEFAULT_SIZE,
											Short.MAX_VALUE).addGap(4, 128, 128)
									.addComponent(getComponentLocation(), 64, GroupLayout.DEFAULT_SIZE,
											Short.MAX_VALUE))
							.addComponent(getComponentUserURL())
							.addComponent(getComponentBio(), 64, PREFERRED_SIZE, Short.MAX_VALUE)));
		}
		return componentUserInfoPanel;
	}

	private JLabel getComponentUserName() {
		if (componentUserName == null) {
			componentUserName = new JLabel();
			componentUserName.setForeground(Color.WHITE);
		}
		return componentUserName;
	}

	private JLabel getComponentUserURL() {
		if (componentUserURL == null) {
			componentUserURL = new JLabel();
			componentUserURL.setForeground(Color.WHITE);
		}
		return componentUserURL;
	}

	/*package*/ClientConfiguration getConfiguration() {
		return configuration;
	}

	@Override
	public DelegateRenderer getDelegateRenderer() {
		return renderer;
	}

	@Override
	public Icon getIcon() {
		if (imageIcon != null) {
			return imageIcon;
		} else if (user != null) {
			Image image = imageCacher.getImage(user);
			if (image != null) {
				image = image.getScaledInstance(24, 24, Image.SCALE_AREA_AVERAGING);
				imageIcon = new ImageIcon(image);
				return imageIcon;
			}
		}
		return null;
	}

	@Override
	public JComponent getTabComponent() {
		if (tabComponent == null) {
			tabComponent = new JPanel();
			GroupLayout layout = new GroupLayout(tabComponent);
			tabComponent.setLayout(layout);
			layout.setVerticalGroup(
					layout.createSequentialGroup()
							.addComponent(getComponentUserInfo(), PREFERRED_SIZE, PREFERRED_SIZE, PREFERRED_SIZE)
							.addComponent(getComponentTweetsScrollPane())
			);

			layout.setHorizontalGroup(layout.createParallelGroup()
					.addComponent(getComponentUserInfo(), PREFERRED_SIZE, PREFERRED_SIZE, PREFERRED_SIZE)
					.addComponent(getComponentTweetsScrollPane()));
		}
		return tabComponent;
	}

	@Override
	public String getTabId() {
		return TAB_ID;
	}

	@Override
	public String getTitle() {
		StringBuilder stringBuilder = new StringBuilder();
		if (user == null) {
			stringBuilder.append("読込中");
		} else {
			stringBuilder.append('@').append(user.getScreenName());
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

	@Override
	public void serialize() {
		super.serialize();
		configProperties.setLong(getPropertyPrefix() + ".targetUserId", user.getId());
	}

	/*package*/ void setUser(final TwitterUser user) {
		this.user = user;
		configuration.getMessageBus().establish(accountId, "all", getRenderer());
		configuration.getMessageBus().establish(accountId, "statuses/user_timeline?" + user.getId(), getRenderer());
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				StringBuilder stringBuilder = new StringBuilder();

				String location = user.getLocation();
				if (location != null) {
					stringBuilder.append("<html><i>Location: </i>").append(location);
					getComponentLocation().setText(stringBuilder.toString());
				}

				getComponentUserName().setText(
						MessageFormat.format("@{0} ({1})", user.getScreenName(), user.getName()));

				JLabel componentUserURL = getComponentUserURL();
				if (user.getURL() != null) {
					stringBuilder.setLength(0);
					stringBuilder.append("<html>URL:&nbsp;<a style='color: #80ffff;text-decoration: underline;'>");
					stringBuilder.append(user.getURL()).append("</a>");
					componentUserURL.setText(stringBuilder.toString());
				}
				componentUserURL.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				componentUserURL.addMouseListener(new MouseAdapter() {

					@Override
					public void mouseClicked(MouseEvent e) {
						if (user.getURL() != null) {
							IntentArguments arg = getIntentArguments("url").putExtra("url", user.getURL());
							handleAction(arg);
						}
					}
				});
				try {
					configuration.getImageCacher().setImageIcon(getComponentUserIcon(), user);
					String profileBannerURL = user.getProfileBannerMediumURL();
					if (profileBannerURL != null) {
						configuration.getImageCacher().setImageIcon(new AbstractImageSetter() {
							@Override
							public void setImage(Image image) {
								try {
									getComponentUserInfo().setBackgroundImage(image);
								} catch (InterruptedException e) {
									Thread.currentThread().interrupt();
								}
							}
						}, new URL(profileBannerURL));
					}
				} catch (InterruptedException e) {
					logger.warn("Interrupted", e);
					Thread.currentThread().interrupt();
				} catch (MalformedURLException e) {
					throw new AssertionError(e);
				}

				getComponentUserName().setText(
						MessageFormat.format("@{0} ({1})", user.getScreenName(), user.getName()));

				getComponentBioEditorPane().setText(getBioHtml());

				List<String> idsList = configuration.getConfigProperties().getList("core.filter.user.ids");
				String userIdString = String.valueOf(user.getId());
				boolean filtered = false;
				for (String id : idsList) {
					if (id.equals(userIdString)) {
						filtered = true;
						break;
					}
				}
				JCheckBoxMenuItem componentMuteCheckBox = getComponentMuteCheckBox();
				componentMuteCheckBox.setSelected(filtered);
				if (configuration.isMyAccount(user.getId())) {
					componentMuteCheckBox.setEnabled(false);
					componentMuteCheckBox.setToolTipText("そ、それはあなたなんだからね！");
				} else {
					componentMuteCheckBox.setEnabled(true);
				}

				String bannerURL = user.getProfileBannerLargeURL();
				if (bannerURL != null) {
					componentUserInfoPanel.setMaximumSize(NEW_BANNER_SIZE);
					componentUserInfoPanel.setPreferredSize(NEW_BANNER_SIZE);
				} else {
					showHeaderItem.setEnabled(false);
				}
				titleLabel.setText(getTitle());
			}
		});
	}
}

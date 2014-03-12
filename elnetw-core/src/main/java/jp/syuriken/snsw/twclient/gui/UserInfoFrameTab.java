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

package jp.syuriken.snsw.twclient.gui;

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
import java.util.regex.Matcher;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.ViewFactory;
import javax.swing.text.html.HTMLEditorKit;

import com.twitter.Regex;
import jp.syuriken.snsw.twclient.ClientConfiguration;
import jp.syuriken.snsw.twclient.ClientProperties;
import jp.syuriken.snsw.twclient.filter.IllegalSyntaxException;
import jp.syuriken.snsw.twclient.gui.render.RenderObject;
import jp.syuriken.snsw.twclient.handler.IntentArguments;
import jp.syuriken.snsw.twclient.handler.UserInfoViewActionHandler;
import jp.syuriken.snsw.twclient.internal.HTMLFactoryDelegator;
import jp.syuriken.snsw.twclient.internal.TwitterRunnable;
import jp.syuriken.snsw.twclient.twitter.TwitterUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.User;
import twitter4j.internal.org.json.JSONException;
import twitter4j.internal.org.json.JSONObject;

/**
 * ユーザー情報を表示するFrameTab
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class UserInfoFrameTab extends DefaultClientTab {

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
	private final Font operationFont = frameApi.getUiFont().deriveFont(Font.PLAIN, frameApi.getUiFont().getSize() - 1);
	private final HashMap<String, IntentArguments> urlIntentMap;
	/** 指定されたユーザー */
	protected User user;
	/** レンダラ */
	protected DelegateRenderer renderer = new UserInfoTweetsRenderer();
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
	/*package*/ JCheckBox muteCheckBox;
	private JLabel componentTwitterLogo;
	private JEditorPane componentBioEditorPane;
	private ImageIcon imageIcon;
	private int nextUrlId = 0;

	/**
	 * インスタンスを生成する。
	 *
	 * @param jsonObject 設定が格納されたJSONオブジェクト
	 * @throws JSONException          JSON例外
	 * @throws IllegalSyntaxException クエリエラー
	 */
	public UserInfoFrameTab(JSONObject jsonObject) throws JSONException,
			IllegalSyntaxException {
		super(jsonObject);
		final long userId = jsonObject.getJSONObject("extended").getLong("userId");

		configuration.addJob(new TwitterRunnable() {
			@Override
			protected void access() throws TwitterException {
				ResponseList<Status> timeline =
						configuration.getTwitterForRead().getUserTimeline(userId);
				for (Status status : timeline) {
					getRenderer().onStatus(status);
				}
			}

			@Override
			protected void onException(TwitterException ex) {
				UserInfoFrameTab.this.getRenderer().onException(ex);
			}

			@Override
			public void run() {
				setUser(configuration.getCacheManager().getUser(userId));
				super.run();
			}
		});
		urlIntentMap = new HashMap<>();
	}

	/**
	 * インスタンスを生成する。
	 *
	 * @param jsonString シリアル化されたデータ
	 * @throws twitter4j.internal.org.json.JSONException               JSON例外
	 * @throws jp.syuriken.snsw.twclient.filter.IllegalSyntaxException クエリエラー
	 */
	public UserInfoFrameTab(String jsonString) throws JSONException,
			IllegalSyntaxException {
		this(new JSONObject(jsonString));
	}

	/**
	 * インスタンスを生成する。
	 *
	 * @param user ユーザー
	 */
	public UserInfoFrameTab(TwitterUser user) {
		super();
		setUser(user);
		urlIntentMap = new HashMap<>();
	}

	@Override
	public void addStatus(RenderObject renderObject) {
		super.addStatus(renderObject);
		if (!(focusGained || isDirty)) {
			isDirty = true;
			EventQueue.invokeLater(new Runnable() {
				@Override
				public void run() {
					configuration.refreshTab(UserInfoFrameTab.this);
				}
			});
		}
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

	private String getBioHtml() {
		String bio = user.getDescription();
		StringBuilder builder = new StringBuilder();
		builder.append("<html>");
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
					+ Regex.VALID_URL_GROUP_URL + "'>$" + Regex.VALID_URL_GROUP_URL + "</a>");
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
			componentBioEditorPane.setText("読込中...");
		}
		return componentBioEditorPane;
	}

	private JLabel getComponentLocation() {
		if (componentLocation == null) {
			componentLocation = new JLabel();
		}
		return componentLocation;
	}

	private JCheckBox getComponentMuteCheckBox() {
		if (muteCheckBox == null) {
			muteCheckBox = new JCheckBox("ミュート");
			muteCheckBox.setEnabled(false);
			muteCheckBox.setFont(operationFont);
			muteCheckBox.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					ClientProperties configProperties = getConfiguration().getConfigProperties();
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
						new JLabel(new ImageIcon(ImageIO.read(UserInfoViewActionHandler.class
								.getResource("/jp/syuriken/snsw/twclient/img/close16.png"))));
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

			componentOperationsPanel.add(getComponentMuteCheckBox());
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

	private JPanel getComponentUserInfo() {
		if (componentUserInfoPanel == null) {
			componentUserInfoPanel = new JPanel();
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
											GroupLayout.PREFERRED_SIZE).addGap(16, 128, 128)
									.addComponent(getComponentLocation(), 64, GroupLayout.DEFAULT_SIZE,
											GroupLayout.DEFAULT_SIZE))
							.addComponent(getComponentUserURL())
							.addComponent(getComponentBio(), 64, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)));
		}
		return componentUserInfoPanel;
	}

	private JLabel getComponentUserName() {
		if (componentUserName == null) {
			componentUserName = new JLabel();
		}
		return componentUserName;
	}

	private JLabel getComponentUserURL() {
		if (componentUserURL == null) {
			componentUserURL = new JLabel();
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
	protected Object getSerializedExtendedData() throws JSONException {
		return new JSONObject().put("userId", user.getId());
	}

	@Override
	public JComponent getTabComponent() {
		tabComponent = new JPanel();
		GroupLayout layout = new GroupLayout(tabComponent);
		tabComponent.setLayout(layout);
		layout.setVerticalGroup(
				layout.createSequentialGroup()
						.addComponent(getComponentUserInfo(), 128, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
						.addComponent(getComponentTweetsScrollPane()));

		layout.setHorizontalGroup(layout.createParallelGroup()
				.addComponent(getComponentUserInfo(), 96, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addComponent(getComponentTweetsScrollPane()));
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

	/*package*/ void setUser(final User user) {
		this.user = user;
		configuration.getMessageBus().establish(accountId, "all", getRenderer());
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
					stringBuilder.append("<html>URL:&nbsp;<a style='color:blue;text-decoration: underline;'>");
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
				configuration.getImageCacher().setImageIcon(getComponentUserIcon(), user);

				getComponentUserName().setText(
						MessageFormat.format("@{0} ({1})", user.getScreenName(), user.getName()));

				getComponentBioEditorPane().setText(getBioHtml());

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
				JCheckBox componentMuteCheckBox = getComponentMuteCheckBox();
				componentMuteCheckBox.setSelected(filtered);
				if (frameApi.getLoginUser().getId() == user.getId()) {
					componentMuteCheckBox.setEnabled(false);
					componentMuteCheckBox.setToolTipText("そ、それはあなたなんだからね！");
				} else {
					componentMuteCheckBox.setEnabled(true);
				}
			}
		});
	}
}

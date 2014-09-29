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
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.GroupLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.border.LineBorder;

import jp.mydns.turenar.twclient.ClientMessageAdapter;
import jp.mydns.turenar.twclient.ParallelRunnable;
import jp.mydns.turenar.twclient.cache.AbstractImageSetter;
import jp.mydns.turenar.twclient.filter.MessageFilter;
import jp.mydns.turenar.twclient.gui.BackgroundImagePanel;
import jp.mydns.turenar.twclient.gui.FileChooserUtil;
import jp.mydns.turenar.twclient.gui.ImageResource;
import jp.mydns.turenar.twclient.gui.ImageViewerFrame;
import jp.mydns.turenar.twclient.internal.IntentActionListener;
import jp.mydns.turenar.twclient.twitter.TwitterUser;
import twitter4j.Twitter;
import twitter4j.TwitterException;

import static javax.swing.GroupLayout.Alignment;
import static javax.swing.GroupLayout.DEFAULT_SIZE;

/**
 * メンション表示用タブ
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class UpdateProfileTab extends AbstractClientTab {

	private static class NullRenderer extends ClientMessageAdapter implements TabRenderer {
		@Override
		public void addChild(MessageFilter filter) throws UnsupportedOperationException {
		}

		@SuppressWarnings("CloneDoesntCallSuperClone")
		@Override
		public MessageFilter clone() throws CloneNotSupportedException { // CS-IGNORE
			throw new CloneNotSupportedException();
		}

		@Override
		public MessageFilter getChild() throws UnsupportedOperationException {
			return null;
		}

		@Override
		public String getUserId() {
			return null;
		}

		@Override
		public void onDisplayRequirement() {
		}

		@Override
		public void setChild(MessageFilter child) throws UnsupportedOperationException {
		}
	}

	private abstract class AbstractImageUploadActionListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			FileChooserUtil fileChooserUtil = FileChooserUtil.newInstance();
			fileChooserUtil.setTitle(getChooserTitle());
			fileChooserUtil.addFilter("画像ファイル", "gif", "jpg", "jpeg", "png", "bmp");
			File selectedFile = fileChooserUtil.openDialog(false, getTabComponent());
			if (selectedFile != null) {
//					JFileChooser fileChooser = new JFileChooser();
//					fileChooser.addChoosableFileFilter(
//							new FileNameExtensionFilter("画像ファイル", "gif", "jpg", "jpeg", "png", "bmp"));
//					int result = fileChooser.showOpenDialog(componentUserIcon);
//					if (result == JFileChooser.APPROVE_OPTION) {
//						File selectedFile = fileChooser.getSelectedFile();
				if (selectedFile.exists()) {
					uploadImage(configuration.getTwitter(String.valueOf(account.getId())), selectedFile);
				}
			}
		}

		public abstract String getChooserTitle();

		protected void updateText(final String text) {
			EventQueue.invokeLater(new Runnable() {
				@Override
				public void run() {
					componentMessage.setText(text);
				}
			});
		}

		protected abstract void uploadImage(Twitter twitter, File selectedFile);
	}

	private class BackgroundEditActionListener extends AbstractImageUploadActionListener {
		@Override
		public String getChooserTitle() {
			return "背景として設定する画像を選択してください";
		}

		@Override
		protected void uploadImage(final Twitter twitter, final File selectedFile) {
			final int result = JOptionPane.showConfirmDialog(getTabComponent(), "指定した画像をタイル状に並べて表示しますか？",
					"背景設定", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
			if (result == JOptionPane.CANCEL_OPTION) {
				return;
			}
			updateText("背景画像の設定中...");

			configuration.addJob(new ParallelRunnable() {
				@Override
				public void run() {
					try {
						account.update(twitter.updateProfileBackgroundImage(selectedFile,
								result == JOptionPane.YES_OPTION));
						updateText("背景画像の設定完了！");
					} catch (TwitterException e1) {
						updateText(e1.getLocalizedMessage());
						logger.warn("Failed to update profile image", e1);
					}
				}
			});
		}
	}

	private class BannerEditActionListener extends AbstractImageUploadActionListener {
		@Override
		public String getChooserTitle() {
			return "ヘッダーとして設定する画像を選択してください";
		}

		@Override
		protected void uploadImage(final Twitter twitter, final File selectedFile) {
			updateText("ヘッダー画像の設定中...");

			configuration.addJob(new ParallelRunnable() {
				@Override
				public void run() {
					try {
						twitter.updateProfileBanner(selectedFile);
						updateText("ヘッダー画像の設定完了！");
					} catch (TwitterException e1) {
						updateText(e1.getLocalizedMessage());
						logger.warn("Failed to update profile image", e1);

					}

				}
			});
		}
	}

	private class UserIconEditActionListener extends AbstractImageUploadActionListener {
		@Override
		public String getChooserTitle() {
			return "ユーザーアイコンに設定する画像を選択してください";
		}

		@Override
		protected void uploadImage(final Twitter twitter, final File selectedFile) {
			updateText("ユーザーアイコンの設定中...");

			configuration.addJob(new ParallelRunnable() {
				@Override
				public void run() {
					try {
						account.update(twitter.updateProfileImage(selectedFile));
						updateText("ユーザーアイコンの設定完了！");
					} catch (TwitterException e1) {
						updateText(e1.getLocalizedMessage());
						logger.warn("Failed to update profile image", e1);
					}
				}
			});
		}
	}

	private static final String TAB_ID = "update_profile";
	/**
	 * new large banner size
	 */
	public static final Dimension NEW_BANNER_SIZE = new Dimension(900, 300);
	/**
	 * transparent color
	 */
	public static final Color TRANSPARENT = new Color(0, 0, 0, 0);
	private final TwitterUser account;
	private JPanel childComponent;
	private NullRenderer nullRenderer = new NullRenderer();
	private BackgroundImagePanel componentMainPanel;
	private JLabel componentUserIcon;
	private JLabel componentMessage;
	private JLabel componentScreenNameLabel;
	private JLabel componentNameLabel;
	private JTextField componentNameField;
	private JLabel componentLocationLabel;
	private JTextField componentLocationField;
	private JLabel componentURLLabel;
	private JTextField componentURLField;
	private JLabel componentDescriptionLabel;
	private JTextArea componentDescriptionArea;
	private JButton componentUpdateButton;
	private JButton componentCancelButton;
	private ImageIcon userIcon;

	/**
	 * インスタンスを生成する。
	 *
	 * @param account account
	 */
	public UpdateProfileTab(TwitterUser account) {
		super(String.valueOf(account.getId()));
		this.account = account;
		initUserIcon();
	}

	/**
	 * インスタンスを生成する。
	 *
	 * @param tabId  ignored
	 * @param uniqId unique identifier
	 */
	public UpdateProfileTab(String tabId, String uniqId) {
		super(tabId, uniqId);
		final long userId = configProperties.getLong(getPropertyPrefix() + ".targetUserId");
		account = configuration.getCacheManager().getUser(userId);
		initUserIcon();
	}

	private JPopupMenu createMainPanelPopup() {
		JPopupMenu popup = new JPopupMenu();
		{
			JMenu headerMenu = new JMenu("ヘッダー");

			JMenuItem headerShowMenu = new JMenuItem("表示");
			headerShowMenu.addActionListener(new IntentActionListener("openimg")
					.putExtra("url", account.getProfileBannerLargeURL()));
			headerMenu.add(headerShowMenu);

			JMenuItem headerChangeMenu = new JMenuItem("変更");
			headerChangeMenu.addActionListener(new BannerEditActionListener());
			headerMenu.add(headerChangeMenu);

			popup.add(headerMenu);
		}
		{
			JMenu backgroundMenu = new JMenu("背景");

			JMenuItem backgroundShowMenu = new JMenuItem("表示");
			backgroundShowMenu.addActionListener(new IntentActionListener("openimg")
					.putExtra("url", account.getProfileBackgroundImageUrlHttps()));
			backgroundMenu.add(backgroundShowMenu);

			JMenuItem backgroundEditMenu = new JMenuItem("変更");
			backgroundEditMenu.addActionListener(new BackgroundEditActionListener());
			backgroundMenu.add(backgroundEditMenu);

			popup.add(backgroundMenu);
		}
		{
			JMenu iconMenu = new JMenu("アイコン");

			JMenuItem iconShowMenu = new JMenuItem("表示");
			iconShowMenu.addActionListener(new IntentActionListener("openimg")
					.putExtra("url", account.getOriginalProfileImageURLHttps()));
			iconMenu.add(iconShowMenu);

			JMenuItem iconEditMenu = new JMenuItem("変更");
			iconEditMenu.addActionListener(new UserIconEditActionListener());
			iconMenu.add(iconEditMenu);

			popup.add(iconMenu);
		}
		return popup;
	}

	@Override
	protected JLabel createTitleLabel() {
		if (account == null) {
			return null;
		} else {
			return super.createTitleLabel();
		}
	}

	@Override
	protected JComponent getChildComponent() {
		if (childComponent == null) {
			childComponent = new JPanel();
			childComponent.add(getComponentMainPanel());
		}
		return childComponent;
	}

	private JButton getComponentCancelButton() {
		if (componentCancelButton == null) {
			componentCancelButton = new JButton("キャンセル");
			componentCancelButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					configuration.removeFrameTab(UpdateProfileTab.this);
				}
			});
		}
		return componentCancelButton;
	}

	private JTextArea getComponentDescriptionArea() {
		if (componentDescriptionArea == null) {
			componentDescriptionArea = new JTextArea(account.getDescription());
			componentDescriptionArea.setForeground(Color.WHITE);
			componentDescriptionArea.setBackground(TRANSPARENT);
			componentDescriptionArea.setOpaque(false);
			componentDescriptionArea.setBorder(new LineBorder(Color.GRAY, 1));
			componentDescriptionArea.setCaretColor(Color.LIGHT_GRAY);
			componentDescriptionArea.setBorder(new LineBorder(Color.LIGHT_GRAY, 1));
		}
		return componentDescriptionArea;
	}

	private JLabel getComponentDescriptionLabel() {
		if (componentDescriptionLabel == null) {
			componentDescriptionLabel = new JLabel("自己紹介:");
			componentDescriptionLabel.setForeground(Color.WHITE);
			componentDescriptionLabel.setBackground(TRANSPARENT);
			componentDescriptionLabel.setOpaque(false);
		}
		return componentDescriptionLabel;
	}

	private JTextField getComponentLocationField() {
		if (componentLocationField == null) {
			componentLocationField = new JTextField(account.getLocation());
			componentLocationField.setForeground(Color.WHITE);
			componentLocationField.setBackground(TRANSPARENT);
			componentLocationField.setOpaque(false);
			componentLocationField.setCaretColor(Color.LIGHT_GRAY);
			componentLocationField.setBorder(new LineBorder(Color.LIGHT_GRAY, 1));
		}
		return componentLocationField;
	}

	private JLabel getComponentLocationLabel() {
		if (componentLocationLabel == null) {
			componentLocationLabel = new JLabel("場所:");
			componentLocationLabel.setForeground(Color.WHITE);
			componentLocationLabel.setBackground(TRANSPARENT);
			componentLocationLabel.setOpaque(false);
		}
		return componentLocationLabel;
	}

	protected BackgroundImagePanel getComponentMainPanel() {
		if (componentMainPanel == null) {
			componentMainPanel = new BackgroundImagePanel();
			componentMainPanel.setMaximumSize(NEW_BANNER_SIZE);
			componentMainPanel.setPreferredSize(NEW_BANNER_SIZE);
			String profileBannerURL = account.getProfileBannerLargeURL();
			if (profileBannerURL != null) {
				try {
					configuration.getImageCacher().setImageIcon(new AbstractImageSetter() {
						@Override
						public void setImage(Image image) {
							try {
								Image scaledInstance = image.getScaledInstance(NEW_BANNER_SIZE.width,
										NEW_BANNER_SIZE.height, Image.SCALE_SMOOTH);
								getComponentMainPanel().setBackgroundImage(scaledInstance);
								getComponentMainPanel().revalidate();
							} catch (InterruptedException e) {
								logger.warn("Interrupted");
								Thread.currentThread().interrupt();
							}
						}
					}, new URL(profileBannerURL));
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				} catch (MalformedURLException e) {
					logger.error("MalformedURL (never happen?)", e);
					getComponentMessage().setText(e.getLocalizedMessage());
				}
			}
			componentMainPanel.setComponentPopupMenu(createMainPanelPopup());

			GroupLayout layout = new GroupLayout(componentMainPanel);
			componentMainPanel.setLayout(layout);
			layout.setHorizontalGroup(layout.createSequentialGroup()
					.addGap(0, 4, 4)
					.addGroup(layout.createParallelGroup()
							.addComponent(getComponentUserIcon(), Alignment.CENTER, 48, 48, 48)
							.addComponent(getComponentUpdateButton(), Alignment.CENTER)
							.addComponent(getComponentCancelButton(), Alignment.CENTER))
					.addGap(2, 8, 8)
					.addGroup(layout.createParallelGroup()
							.addGroup(layout.createSequentialGroup()
									.addComponent(getComponentScreenNameLabel())
									.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
									.addComponent(getComponentMessage()))
							.addGroup(layout.createSequentialGroup()
									.addGroup(layout.createParallelGroup()
											.addComponent(getComponentNameLabel(), Alignment.TRAILING)
											.addComponent(getComponentLocationLabel(), Alignment.TRAILING)
											.addComponent(getComponentURLLabel(), Alignment.TRAILING))
									.addGap(0, 2, 2)
									.addGroup(layout.createParallelGroup()
											.addComponent(getComponentNameField())
											.addComponent(getComponentLocationField())
											.addComponent(getComponentURLField())))
							.addComponent(getComponentDescriptionLabel())
							.addComponent(getComponentDescriptionArea()))
					.addGap(0, 4, 4));
			layout.setVerticalGroup(layout.createParallelGroup()
					.addGroup(layout.createSequentialGroup()
							.addGap(0, 4, 4)
							.addComponent(getComponentUserIcon())
							.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
							.addComponent(getComponentUpdateButton())
							.addComponent(getComponentCancelButton()))
					.addGroup(layout.createSequentialGroup()
							.addGap(0, 4, 4)
							.addGroup(layout.createBaselineGroup(true, false)
									.addComponent(getComponentScreenNameLabel())
									.addComponent(getComponentMessage()))
							.addGroup(layout.createBaselineGroup(true, false)
									.addComponent(getComponentNameLabel())
									.addComponent(getComponentNameField()))
							.addGroup(layout.createBaselineGroup(true, false)
									.addComponent(getComponentLocationLabel())
									.addComponent(getComponentLocationField()))
							.addGroup(layout.createBaselineGroup(true, false)
									.addComponent(getComponentURLLabel())
									.addComponent(getComponentURLField()))
							.addComponent(getComponentDescriptionLabel())
							.addComponent(getComponentDescriptionArea(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
							.addGap(2, 4, 4)));
		}
		return componentMainPanel;
	}

	private JLabel getComponentMessage() {
		if (componentMessage == null) {
			componentMessage = new JLabel();
			componentMessage.setForeground(Color.WHITE);
			componentMessage.setBackground(TRANSPARENT);
			componentMessage.setOpaque(false);
		}
		return componentMessage;
	}

	private JTextField getComponentNameField() {
		if (componentNameField == null) {
			componentNameField = new JTextField(account.getName());
			componentNameField.setForeground(Color.WHITE);
			componentNameField.setOpaque(false);
			componentNameField.setCaretColor(Color.LIGHT_GRAY);
			componentNameField.setBorder(new LineBorder(Color.LIGHT_GRAY, 1));
		}
		return componentNameField;
	}

	private JLabel getComponentNameLabel() {
		if (componentNameLabel == null) {
			componentNameLabel = new JLabel("名前:");
			componentNameLabel.setForeground(Color.WHITE);
			componentNameLabel.setOpaque(false);
		}
		return componentNameLabel;
	}

	private JLabel getComponentScreenNameLabel() {
		if (componentScreenNameLabel == null) {
			componentScreenNameLabel = new JLabel("@" + account.getScreenName());
			Image scaledInstance = ImageResource.getImgTwitterLogo().getImage().getScaledInstance(16, 16,
					Image.SCALE_SMOOTH);
			componentScreenNameLabel.setIcon(new ImageIcon(scaledInstance));
			componentScreenNameLabel.setForeground(Color.WHITE);

		}
		return componentScreenNameLabel;
	}

	private JTextField getComponentURLField() {
		if (componentURLField == null) {
			String url = account.getURL();
			componentURLField = new JTextField(url == null ? null : account.getURLEntity().getExpandedURL());
			componentURLField.setForeground(Color.WHITE);
			componentURLField.setBackground(TRANSPARENT);
			componentURLField.setOpaque(false);
			componentURLField.setCaretColor(Color.LIGHT_GRAY);
			componentURLField.setBorder(new LineBorder(Color.LIGHT_GRAY, 1));
		}
		return componentURLField;
	}

	private JLabel getComponentURLLabel() {
		if (componentURLLabel == null) {
			componentURLLabel = new JLabel("ホームページ:");
			componentURLLabel.setForeground(Color.WHITE);
		}
		return componentURLLabel;
	}

	private JButton getComponentUpdateButton() {
		if (componentUpdateButton == null) {
			componentUpdateButton = new JButton("更新");
			componentUpdateButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					componentUpdateButton.setEnabled(false);
					configuration.addJob(new ParallelRunnable() {
						public void run() {
							Twitter twitter = configuration.getTwitter(String.valueOf(account.getId()));
							try {
								twitter.updateProfile(getComponentNameField().getText(),
										getComponentURLField().getText(),
										getComponentLocationField().getText(),
										getComponentDescriptionArea().getText());
								EventQueue.invokeLater(new Runnable() {
									@Override
									public void run() {
										componentUpdateButton.setEnabled(true);
										getComponentMessage().setText("更新完了！");
									}
								});
							} catch (final TwitterException e1) {
								logger.warn("failed to update profile", e1);
								EventQueue.invokeLater(new Runnable() {
									public void run() {
										getComponentMessage().setText(e1.getLocalizedMessage());
									}
								});
							}
						}
					});
				}
			});
		}
		return componentUpdateButton;
	}

	private JLabel getComponentUserIcon() {
		if (componentUserIcon == null) {
			componentUserIcon = new JLabel();
			try {
				imageCacher.setImageIcon(componentUserIcon, account);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
			componentUserIcon.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			JPopupMenu popup = new JPopupMenu();
			JMenuItem editMenu = new JMenuItem("編集");
			editMenu.addActionListener(new UserIconEditActionListener());
			popup.add(editMenu);
			componentUserIcon.setComponentPopupMenu(popup);
			componentUserIcon.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					if (!e.isPopupTrigger()) {
						try {
							new ImageViewerFrame(new URL(account.getOriginalProfileImageURLHttps())).setVisible(true);
						} catch (MalformedURLException e1) {
							logger.error("Malformed URL (never happen?)", e1);
						}
					}

				}
			});
		}
		return componentUserIcon;
	}

	@Override
	public DelegateRenderer getDelegateRenderer() {
		return null;
	}

	@Override
	public Icon getIcon() {
		return userIcon;
	}

	@Override
	public TabRenderer getRenderer() {
		return nullRenderer;
	}

	@Override
	protected JScrollPane getScrollPane() {
		return super.getScrollPane();
	}

	@Override
	public String getTabId() {
		return TAB_ID;
	}

	@Override
	public String getTitle() {
		return "@" + account.getScreenName() + "編集";
	}

	@Override
	public JLabel getTitleComponent() {
		if (titleLabel == null) {
			titleLabel = createTitleLabel();
		}
		return titleLabel;
	}

	@Override
	public String getToolTip() {
		return "プロフィールを編集する";
	}

	@Override
	protected String getTwitterUrl() {
		return "https://twitter.com/settings/profile";
	}

	/**
	 * init user icon
	 */
	protected void initUserIcon() {
		try {
			imageCacher.setImageIcon(new AbstractImageSetter() {
				@Override
				public void setImage(Image image) {
					userIcon = new ImageIcon(image);
					getTitleComponent().setIcon(new ImageIcon(image.getScaledInstance(24, 24, Image.SCALE_AREA_AVERAGING)));
				}
			}, account);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	@Override
	public void serialize() {
		super.serialize();
		configProperties.setLong(getPropertyPrefix() + ".targetUserId", account.getId());
	}
}

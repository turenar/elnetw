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

package jp.mydns.turenar.twclient.gui.tab.factory;

import java.awt.EventQueue;

import javax.swing.GroupLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import jp.mydns.turenar.twclient.ClientConfiguration;
import jp.mydns.turenar.twclient.JobQueue;
import jp.mydns.turenar.twclient.gui.tab.ClientTab;
import jp.mydns.turenar.twclient.gui.tab.ClientTabFactory;
import jp.mydns.turenar.twclient.gui.tab.ListTimelineTab;
import jp.mydns.turenar.twclient.internal.TwitterRunnable;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.UserList;
import twitter4j.conf.Configuration;

import static javax.swing.GroupLayout.Alignment;

/**
 * factory for ListTimelineTab
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class ListTimelineTabFactory implements ClientTabFactory {

	/**
	 * ユーザー情報を設定するためのパネル
	 */
	public static class ListConfigPanel extends JPanel {
		private static final long serialVersionUID = 2053932696892259530L;
		private JLabel listLabel;
		private JLabel userAtMarkLabel;
		private JTextField userTextField;
		private JTextField slugField;
		private JLabel slashLabel;
		private JCheckBox idCheckBox;
		private JCheckBox useStreamCheck;
		private JCheckBox allTweetsIncludeCheck;

		/**
		 * インスタンスを生成する
		 */
		public ListConfigPanel() {
			setBorder(new TitledBorder("固有の設定"));
			initComponents();
		}

		/**
		 * すべてのツイート (リストに入っていない人によるRTや@返信など) を含むかどうか
		 *
		 * @return すべてのツイートを含むかどうか
		 */
		public boolean allTweetsIncluded() {
			return useStream() && getComponentAllTweetsIncluded().isSelected();
		}

		/*package*/ JCheckBox getComponentAllTweetsIncluded() {
			if (allTweetsIncludeCheck == null) {
				allTweetsIncludeCheck = new JCheckBox("リストに登録されているアカウントへのリプライなどを含める");
				allTweetsIncludeCheck.setToolTipText("デフォルトはオフです。オンにするにはストリームの使用をオンにしてください。");
			}
			return allTweetsIncludeCheck;
		}

		/*package*/ JCheckBox getComponentIdCheckBox() {
			if (idCheckBox == null) {
				idCheckBox = new JCheckBox("リストIDを保存");
				idCheckBox.setSelected(true);
				idCheckBox.setToolTipText("リストIDを保存すると、リスト名が変わったりユーザー名が変わっても追跡できます。\n"
						+ "しかし、リストを同名で作りなおした時に追跡できなくなります。");
			}
			return idCheckBox;
		}

		/*package*/ JLabel getComponentListLabel() {
			if (listLabel == null) {
				listLabel = new JLabel("リスト");
			}
			return listLabel;
		}

		/*package*/ JLabel getComponentSlashLabel() {
			if (slashLabel == null) {
				slashLabel = new JLabel("/");
			}
			return slashLabel;
		}

		/*package*/ JTextField getComponentSlugField() {
			if (slugField == null) {
				slugField = new JTextField();
			}
			return slugField;
		}

		/*package*/ JCheckBox getComponentUseStreamCheck() {
			if (useStreamCheck == null) {
				useStreamCheck = new JCheckBox("ストリームの使用");
				useStreamCheck.setSelected(true);
				useStreamCheck.setToolTipText("即時更新");
				useStreamCheck.addChangeListener(new ChangeListener() {
					@Override
					public void stateChanged(ChangeEvent e) {
						getComponentAllTweetsIncluded().setEnabled(useStreamCheck.isSelected());
					}
				});
			}
			return useStreamCheck;
		}

		/*package*/ JLabel getComponentUserAtMarkLabel() {
			if (userAtMarkLabel == null) {
				userAtMarkLabel = new JLabel("@");
			}
			return userAtMarkLabel;
		}

		/*package*/ JTextField getComponentUserTextField() {
			if (userTextField == null) {
				userTextField = new JTextField();
			}
			return userTextField;
		}

		/*package*/ void initComponents() {
			GroupLayout layout = new GroupLayout(this);
			setLayout(layout);
			layout.setVerticalGroup(layout.createSequentialGroup()
					.addGroup(layout.createBaselineGroup(true, false)
							.addComponent(getComponentListLabel())
							.addComponent(getComponentUserAtMarkLabel())
							.addComponent(getComponentUserTextField())
							.addComponent(getComponentSlashLabel())
							.addComponent(getComponentSlugField()))
					.addComponent(getComponentIdCheckBox())
					.addComponent(getComponentUseStreamCheck())
					.addComponent(getComponentAllTweetsIncluded()));
			layout.setHorizontalGroup(layout.createParallelGroup()
					.addGroup(layout.createSequentialGroup()
							.addGroup(layout.createParallelGroup()
									.addComponent(getComponentListLabel(), Alignment.TRAILING))
							.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
							.addGroup(layout.createSequentialGroup()
									.addComponent(getComponentUserAtMarkLabel())
									.addComponent(getComponentUserTextField())
									.addComponent(getComponentSlashLabel())
									.addComponent(getComponentSlugField())))
					.addComponent(getComponentIdCheckBox())
					.addComponent(getComponentUseStreamCheck())
					.addComponent(getComponentAllTweetsIncluded()));
		}

		/**
		 * ストリームを使用するかどうか
		 *
		 * @return ストリームを使用するかどうか
		 */
		public boolean useStream() {
			return getComponentUseStreamCheck().isSelected();
		}
	}

	private static class ListIdFetcher extends TwitterRunnable {
		private final String accountId;
		private final String listOwner;
		private final String slug;
		private final ListConfigPanel configPanel;

		public ListIdFetcher(String accountId, String listOwner, String slug,
				ListConfigPanel configPanel) {
			this.accountId = accountId;
			this.listOwner = listOwner;
			this.slug = slug;
			this.configPanel = configPanel;
		}

		@Override
		protected void access() throws TwitterException {
			Configuration conf = ClientConfiguration.getInstance().getMessageBus().getTwitterConfiguration(accountId);
			final UserList userList = new TwitterFactory(conf).getInstance().showUserList(listOwner, slug);
			EventQueue.invokeLater(new Runnable() {
				@Override
				public void run() {
					configuration.addFrameTab(new ListTimelineTab(accountId, userList.getId(), configPanel));
				}
			});
		}

		@Override
		protected void onException(final TwitterException ex) {
			EventQueue.invokeLater(new Runnable() {
				@Override
				public void run() {
					JOptionPane.showMessageDialog(null,
							"リストIDの取得に失敗しました。正しいリスト名を入力しているかどうか確認してください。\n"
									+ ex.getLocalizedMessage(), "elnetw", JOptionPane.ERROR_MESSAGE);
				}
			});
		}
	}

	/**
	 * priority for adding tab menu
	 */
	public static final int TAB_PRIORITY = 0x03000002;

	@Override
	public ClientTab getInstance(String tabId, String uniqId) {
		return new ListTimelineTab(uniqId);
	}

	@Override
	public String getName() {
		return "リスト";
	}

	@Override
	public JComponent getOtherConfigurationComponent() {
		return new ListConfigPanel();
	}

	@Override
	public int getPriority() {
		return TAB_PRIORITY;
	}

	@Override
	public ClientTab newTab(String tabId, final String accountId, JComponent otherConfigurationComponent) {
		if (!(otherConfigurationComponent instanceof ListConfigPanel)) {
			throw new AssertionError("illegal type");
		}
		ListConfigPanel configPanel = (ListConfigPanel) otherConfigurationComponent;
		String listOwner = configPanel.getComponentUserTextField().getText();
		String slug = configPanel.getComponentSlugField().getText();
		if (configPanel.getComponentIdCheckBox().isSelected()) {
			ClientConfiguration.getInstance().addJob(JobQueue.PRIORITY_MAX,
					new ListIdFetcher(accountId, listOwner, slug, configPanel));
			return null;
		} else {
			return new ListTimelineTab(accountId, listOwner, slug, configPanel);
		}
	}
}

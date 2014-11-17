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

package jp.mydns.turenar.twclient.gui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.AbstractMap;

import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

import jp.mydns.turenar.twclient.ClientConfiguration;
import jp.mydns.turenar.twclient.gui.tab.ClientTab;
import jp.mydns.turenar.twclient.gui.tab.ClientTabFactory;
import jp.mydns.turenar.twclient.twitter.TwitterUser;

import static jp.mydns.turenar.twclient.i18n.LocalizationResource.tr;

/**
 * frame that confirm adding client tab
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class AddClientTabConfirmFrame extends JFrame {
	/**
	 * あかうんとえんとりー
	 */
	protected class AccountEntry extends AbstractMap.SimpleEntry<String, String> {
		private static final long serialVersionUID = -1827398108341874564L;

		/**
		 * いんすたんすをせいせいする
		 *
		 * @param user      ゆーざーめい
		 * @param accountId たいおうするあかうんとあいでぃー
		 */
		public AccountEntry(TwitterUser user, String accountId) {
			this("@" + user.getScreenName() + " (" + user.getName() + ")", accountId);
		}

		/**
		 * いんすたんすをせいせいする
		 *
		 * @param viewData  ひょうじするないよう
		 * @param accountId あかうんとあいでぃー
		 */
		public AccountEntry(String viewData, String accountId) {
			super(viewData, accountId);
		}

		@Override
		public String toString() {
			return getKey();
		}
	}

	private static final long serialVersionUID = -5471015581336370042L;

	private final ClientConfiguration configuration = ClientConfiguration.getInstance();
	private final ClientTabFactory factory;
	private String tabId;
	private JLabel accountLabel;
	private JComboBox<AccountEntry> accountChooser;
	private JButton cancelButton;
	private JButton okButton;
	private JComponent otherConfigurationComponent;
	private JTextField titleField;
	private JLabel titleLabel;

	/**
	 * make instance
	 *
	 * @param tabId target tab id
	 */
	public AddClientTabConfirmFrame(String tabId) {
		if (tabId == null) {
			throw new NullPointerException();
		}
		this.tabId = tabId;
		factory = ClientConfiguration.getClientTabFactory(tabId);
		initComponents();
	}

	private JLabel createEmptyLabel() {
		JLabel label = new JLabel();
		Dimension zeroDimension = new Dimension(0, 0);
		label.setMaximumSize(zeroDimension);
		label.setPreferredSize(zeroDimension);
		return label;
	}

	private JComboBox<AccountEntry> getAccountChooser() {
		if (accountChooser == null) {
			DefaultComboBoxModel<AccountEntry> boxModel = new DefaultComboBoxModel<>();
			boxModel.addElement(new AccountEntry(tr("(Use reader account)"), "$reader"));
			boxModel.addElement(new AccountEntry(tr("Use writer account)"), "$writer"));
			for (String userIdStr : configuration.getAccountList()) {
				TwitterUser user = configuration.getCacheManager().getUser(Long.parseLong(userIdStr));
				boxModel.addElement(new AccountEntry(user, userIdStr));
			}
			accountChooser = new JComboBox<>(boxModel);
		}
		return accountChooser;
	}

	private JLabel getAccountLabel() {
		if (accountLabel == null) {
			accountLabel = new JLabel(tr("Account:"));
		}
		return accountLabel;
	}

	private JButton getCancelButton() {
		if (cancelButton == null) {
			cancelButton = new JButton(tr("Cancel"));
			cancelButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					dispose();
				}
			});
		}
		return cancelButton;
	}

	private JComponent getOkButton() {
		if (okButton == null) {
			okButton = new JButton(tr("Add"));
			okButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					ClientTab tab = factory.newTab(tabId,
							accountChooser.getModel().getElementAt(accountChooser.getSelectedIndex()).getValue(),
							otherConfigurationComponent);
					if (tab != null) {
						String title = getTitleField().getText();
						if (!(title == null || title.isEmpty())) {
							tab.setTitle(title);
						}
						configuration.addFrameTab(tab);
					}
					dispose();
				}
			});
		}
		return okButton;
	}

	private JTextField getTitleField() {
		if (titleField == null) {
			titleField = new JTextField();
		}
		return titleField;
	}

	private JLabel getTitleLabel() {
		if (titleLabel == null) {
			titleLabel = new JLabel(tr("Tab Title"));
		}
		return titleLabel;
	}

	private void initComponents() {
		setTitle(tr("Add tab: %s", factory.getName()));
		GroupLayout layout = new GroupLayout(this.getContentPane());
		setLayout(layout);
		otherConfigurationComponent = factory.getOtherConfigurationComponent();
		if (otherConfigurationComponent == null) {
			otherConfigurationComponent = createEmptyLabel();
		}
		layout.setHorizontalGroup(layout.createParallelGroup()
				.addGroup(layout.createSequentialGroup()
						.addGroup(layout.createParallelGroup()
								.addComponent(getAccountLabel())
								.addComponent(getTitleLabel()))
						.addGroup(layout.createParallelGroup()
								.addComponent(getAccountChooser())
								.addComponent(getTitleField())))
				.addComponent(otherConfigurationComponent)
				.addGroup(layout.createSequentialGroup()
						.addGap(4, 16, 16)
						.addComponent(getOkButton())
						.addGap(4, 4, Short.MAX_VALUE)
						.addComponent(getCancelButton())
						.addGap(4, 16, 16)));
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addGroup(layout.createBaselineGroup(true, false)
						.addComponent(getAccountLabel())
						.addComponent(getAccountChooser()))
				.addGroup(layout.createBaselineGroup(true, false)
						.addComponent(getTitleLabel())
						.addComponent(getTitleField()))
				.addComponent(otherConfigurationComponent)
				.addGroup(layout.createParallelGroup()
						.addComponent(getOkButton())
						.addComponent(getCancelButton())));
	}

	@Override
	public void setVisible(boolean b) {
		super.setVisible(b);
		pack();
	}
}

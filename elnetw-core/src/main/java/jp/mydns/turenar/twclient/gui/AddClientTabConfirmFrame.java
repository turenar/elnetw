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

import jp.mydns.turenar.twclient.ClientConfiguration;
import jp.mydns.turenar.twclient.gui.tab.ClientTab;
import jp.mydns.turenar.twclient.gui.tab.ClientTabFactory;
import jp.mydns.turenar.twclient.twitter.TwitterUser;

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

	private final ClientConfiguration configuration = ClientConfiguration.getInstance();
	private final ClientTabFactory factory;
	private String tabId;
	private JLabel accountLabel;
	private JComboBox<AccountEntry> accountChooser;
	private JButton cancelButton;
	private JButton okButton;
	private JComponent otherConfigurationComponent;

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
			boxModel.addElement(new AccountEntry("(読み取り用アカウントを使用)", "$reader"));
			boxModel.addElement(new AccountEntry("(書き込み用アカウントを使用)", "$writer"));
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
			accountLabel = new JLabel("アカウント:");
		}
		return accountLabel;
	}

	private JButton getCancelButton() {
		if (cancelButton == null) {
			cancelButton = new JButton("キャンセル");
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
			okButton = new JButton("追加");
			okButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					ClientTab tab = factory.newTab(tabId,
							accountChooser.getModel().getElementAt(accountChooser.getSelectedIndex()).getValue(),
							otherConfigurationComponent);
					if (tab != null) {
						configuration.addFrameTab(tab);
					}
					dispose();
				}
			});
		}
		return okButton;
	}

	private void initComponents() {
		setTitle("タブの追加: " + factory.getName());
		GroupLayout layout = new GroupLayout(this.getContentPane());
		setLayout(layout);
		otherConfigurationComponent = factory.getOtherConfigurationComponent();
		if (otherConfigurationComponent == null) {
			otherConfigurationComponent = createEmptyLabel();
		}
		layout.setHorizontalGroup(layout.createParallelGroup()
				.addGroup(layout.createSequentialGroup()
						.addGroup(layout.createParallelGroup()
								.addComponent(getAccountLabel()))
						.addGroup(layout.createSequentialGroup()
								.addComponent(getAccountChooser())))
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

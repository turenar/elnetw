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

import javax.swing.GroupLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.border.TitledBorder;

import jp.mydns.turenar.twclient.gui.tab.ClientTab;
import jp.mydns.turenar.twclient.gui.tab.ClientTabFactory;
import jp.mydns.turenar.twclient.gui.tab.UserInfoFrameTab;

import static jp.mydns.turenar.twclient.i18n.LocalizationResource.tr;

/**
 * factory for UserInfoViewTab
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class UserInfoViewTabFactory implements ClientTabFactory {

	/**
	 * ユーザー情報を設定するためのパネル
	 */
	protected static class UserInfoConfigPanel extends JPanel {

		private static final long serialVersionUID = -3162663908900944680L;
		private JLabel userLabel;
		private JLabel userAtMarkLabel;
		private JTextField userTextField;

		/**
		 * インスタンスを生成する
		 */
		public UserInfoConfigPanel() {
			setBorder(new TitledBorder(tr("Specific Configuration")));
			initComponents();
		}

		private JLabel getComponentUserAtMarkLabel() {
			if (userAtMarkLabel == null) {
				userAtMarkLabel = new JLabel("@");
			}
			return userAtMarkLabel;
		}

		private JLabel getComponentUserLabel() {
			if (userLabel == null) {
				userLabel = new JLabel(tr("ScreenName"));
			}
			return userLabel;
		}

		private JTextField getComponentUserTextField() {
			if (userTextField == null) {
				userTextField = new JTextField();
			}
			return userTextField;
		}

		private void initComponents() {
			GroupLayout layout = new GroupLayout(this);
			setLayout(layout);
			layout.setVerticalGroup(layout.createSequentialGroup()
					.addGroup(layout.createParallelGroup()
							.addComponent(getComponentUserLabel())
							.addComponent(getComponentUserAtMarkLabel())
							.addComponent(getComponentUserTextField())));
			layout.setHorizontalGroup(layout.createSequentialGroup()
					.addGroup(layout.createParallelGroup()
							.addComponent(getComponentUserLabel()))
					.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
					.addGroup(layout.createParallelGroup()
							.addGroup(layout.createSequentialGroup()
									.addComponent(getComponentUserAtMarkLabel())
									.addComponent(getComponentUserTextField()))));
		}
	}

	/**
	 * priority for adding tab menu
	 */
	public static final int TAB_PRIORITY = -0x0ffff000;

	@Override
	public ClientTab getInstance(String tabId, String uniqId) {
		return new UserInfoFrameTab(uniqId);
	}

	@Override
	public String getName() {
		return tr("User Timeline");
	}

	@Override
	public JComponent getOtherConfigurationComponent() {
		return new UserInfoConfigPanel();
	}

	@Override
	public int getPriority() {
		return TAB_PRIORITY;
	}

	@Override
	public ClientTab newTab(String tabId, String accountId, JComponent otherConfigurationComponent) {
		if (!(otherConfigurationComponent instanceof UserInfoConfigPanel)) {
			throw new AssertionError("illegal type");
		}
		UserInfoConfigPanel configPanel = (UserInfoConfigPanel) otherConfigurationComponent;
		String screenName = configPanel.getComponentUserTextField().getText();
		return new UserInfoFrameTab(accountId, screenName);
	}
}

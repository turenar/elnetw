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
import jp.mydns.turenar.twclient.gui.tab.SearchTab;

/**
 * factory for SearchTab
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class SearchTabFactory implements ClientTabFactory {
	/**
	 * ユーザー情報を設定するためのパネル
	 */
	protected static class SearchTabConfigPanel extends JPanel {

		private JLabel searchQueryLabel;
		private JTextField searchQueryField;

		/**
		 * インスタンスを生成する
		 */
		public SearchTabConfigPanel() {
			setBorder(new TitledBorder("固有の設定"));
			initComponents();
		}

		private JTextField getComponentSearchQueryField() {
			if (searchQueryField == null) {
				searchQueryField = new JTextField();
			}
			return searchQueryField;
		}

		private JLabel getComponentSearchQueryLabel() {
			if (searchQueryLabel == null) {
				searchQueryLabel = new JLabel("クエリ:");
			}
			return searchQueryLabel;
		}

		private void initComponents() {
			GroupLayout layout = new GroupLayout(this);
			setLayout(layout);
			layout.setVerticalGroup(layout.createSequentialGroup()
					.addGroup(layout.createParallelGroup()
							.addComponent(getComponentSearchQueryLabel())
							.addComponent(getComponentSearchQueryField())));
			layout.setHorizontalGroup(layout.createSequentialGroup()
					.addGroup(layout.createParallelGroup()
							.addComponent(getComponentSearchQueryLabel()))
					.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
					.addComponent(getComponentSearchQueryField()));
		}
	}

	public static final int PRIORITY = 0x03000001;

	@Override
	public ClientTab getInstance(String tabId, String uniqId) {
		return new SearchTab(uniqId);
	}

	@Override
	public String getName() {
		return "検索";
	}

	@Override
	public JComponent getOtherConfigurationComponent() {
		return new SearchTabConfigPanel();
	}

	@Override
	public int getPriority() {
		return PRIORITY;
	}

	@Override
	public ClientTab newTab(String tabId, String accountId, JComponent otherConfigurationComponent) {
		if (!(otherConfigurationComponent instanceof SearchTabConfigPanel)) {
			throw new AssertionError("illegal type");
		}
		SearchTabConfigPanel configPanel = (SearchTabConfigPanel) otherConfigurationComponent;
		String searchQuery = configPanel.getComponentSearchQueryField().getText();
		return new SearchTab(accountId, searchQuery);
	}

}

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

package jp.mydns.turenar.twclient.gui.config;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;

import jp.mydns.turenar.twclient.ClientConfiguration;
import jp.mydns.turenar.twclient.gui.QueryEditFrame;
import jp.mydns.turenar.twclient.gui.tab.ClientTab;

import static jp.mydns.turenar.twclient.i18n.LocalizationResource.tr;

/**
 * フィルタをごにょごにょするための {@link ConfigType}
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class QueryEditConfigType implements ConfigType, ActionListener {

	/**
	 * {@link #displayString}と {@link #propertyKey}を格納するだけのクラス
	 *
	 * @author Turenar (snswinhaiku dot lo at gmail dot com)
	 */
	protected static class KVItem {

		/** 表示名 */
		protected String displayString;
		/** プロパティキー */
		protected String propertyKey;


		/**
		 * インスタンスを生成する。
		 *
		 * @param displayString 表示名
		 * @param propertyKey   プロパティキー
		 */
		public KVItem(String displayString, String propertyKey) {
			this.displayString = displayString;
			this.propertyKey = propertyKey;
		}

		@Override
		public String toString() {
			return displayString;
		}
	}

	private JComboBox<KVItem> filterChooser;
	private ClientConfiguration configuration;
	private JButton editButton;


	/** インスタンスを生成する。 */
	public QueryEditConfigType() {
		this.configuration = ClientConfiguration.getInstance();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		JComboBox<KVItem> componentFilterChooser = getComponentFilterChooser();
		KVItem kvItem = (KVItem) componentFilterChooser.getSelectedItem();
		QueryEditFrame editFrame = new QueryEditFrame(kvItem.displayString, kvItem.propertyKey);
		editFrame.setVisible(true);
		editFrame.toFront();
	}

	@Override
	public JComponent getComponent(String configKey, String nowValue, ConfigFrame listener) {
		JPanel panel = new JPanel();
		GroupLayout layout = new GroupLayout(panel);
		layout.setHorizontalGroup(layout.createParallelGroup() //
				.addComponent(getComponentFilterChooser()) //
				.addComponent(getComponentEditButton(), Alignment.TRAILING));
		layout.setVerticalGroup(layout.createSequentialGroup() //
				.addComponent(getComponentFilterChooser()) //
				.addComponent(getComponentEditButton()));
		return panel;
	}

	private Component getComponentEditButton() {
		if (editButton == null) {
			editButton = new JButton(tr("Edit"));
			editButton.addActionListener(this);
		}
		return editButton;
	}

	private JComboBox<KVItem> getComponentFilterChooser() {
		if (filterChooser == null) {
			filterChooser = new JComboBox<>();
			filterChooser.addItem(new KVItem(tr("(global)"), "core.filter._global"));

			int count = configuration.getFrameTabCount();
			for (int i = 0; i < count; i++) {
				ClientTab tab = configuration.getFrameTab(i);
				filterChooser.addItem(new KVItem(tab.getTitle() + " (" + tab.getUniqId() + ")", "gui.tabs.data."
						+ tab.getUniqId() + ".filter.query"));
			}
		}
		return filterChooser;
	}

	@Override
	public String getValue(JComponent component) {
		return null;
	}

	@Override
	public boolean isPreferredAsMultiline() {
		return true;
	}

	@Override
	public boolean isValid(JComponent component) {
		return true;
	}
}

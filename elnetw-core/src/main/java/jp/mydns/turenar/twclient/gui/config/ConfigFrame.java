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

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.border.TitledBorder;

import jp.mydns.turenar.twclient.ClientConfiguration;
import jp.mydns.turenar.twclient.conf.ClientProperties;
import jp.mydns.turenar.twclient.gui.config.ConfigFrameBuilder.Config;

/**
 * 設定フレーム
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
@SuppressWarnings("serial")
public class ConfigFrame extends JFrame {

	private class FrameWindowListener extends WindowAdapter {

		@Override
		public void windowClosing(WindowEvent e) {
			synchronized (staticHolder) {
				openingFrame = null;
				properties.store("Auto generated by jp.mydns.turenar.twclient.gui.config.ConfigFrame");
			}
		}
	}

	/**
	 * preferred size of baseline group
	 */
	public static final int BASELINE_GROUP_PREF_SIZE = 24;

	/*package*/static ConfigFrame openingFrame;
	private static Object staticHolder = new Object();

	/**
	 * 指定された設定の配列からフレームを生成する。すでにフレームが存在したときはそのフレームを返す。
	 *
	 * @param configs       設定の配列
	 * @param configuration 実行時設定
	 * @return フレーム
	 */
	/*package*/
	static JFrame build(Config[] configs, ClientConfiguration configuration) {
		synchronized (staticHolder) {
			if (openingFrame != null) {
				return openingFrame;
			} else {
				openingFrame = new ConfigFrame(configuration);
			}
		}
		JTabbedPane tabbedPane = new JTabbedPane();
		openingFrame.add(tabbedPane);
		ClientProperties properties = configuration.getConfigProperties();
		String group = "UNDEF";
		String subgroup = "UNDEF";
		JPanel tabContent = null;
		JPanel subgroupPanel;
		GroupLayout layout = null;
		SequentialGroup verticalGroup = null; // Parallel[K V]
		ParallelGroup horizontalNameGroup = null;
		ParallelGroup horizontalValueGroup = null;
		ParallelGroup horizontalCombinedGroup = null;
		for (Config config : configs) {
			if (!group.equals(config.getGroup())) {
				group = config.getGroup();
				tabContent = new JPanel();
				tabContent.setLayout(new BoxLayout(tabContent, BoxLayout.Y_AXIS));
				subgroup = "UNDEF";
				tabbedPane.add(group, new JScrollPane(tabContent));
			}
			if (subgroup == null ? config.getSubgroup() != null : !subgroup.equals(config.getSubgroup())) {
				subgroup = config.getSubgroup();
				subgroupPanel = new JPanel();
				layout = new GroupLayout(subgroupPanel);
				subgroupPanel.setLayout(layout);
				verticalGroup = layout.createSequentialGroup();
				horizontalNameGroup = layout.createParallelGroup();
				horizontalValueGroup = layout.createParallelGroup();
				horizontalCombinedGroup = layout.createParallelGroup();


				layout.setVerticalGroup(verticalGroup);
				layout
						.setHorizontalGroup(layout.createParallelGroup()
								.addGroup(layout.createSequentialGroup()
										.addGroup(horizontalNameGroup)
										.addGap(4, 4, 4)
										.addGroup(horizontalValueGroup))
								.addGroup(horizontalCombinedGroup));
				tabContent.add(subgroupPanel);
				if (subgroup != null) {
					subgroupPanel.setBorder(new TitledBorder(subgroup));
				}
			}
			JLabel label = new JLabel(config.getDescription());
			label.setToolTipText(config.getHint());
			String configKey = config.getConfigKey();
			JComponent valueComponent =
					config.getType().getComponent(configKey,
							configKey == null ? null : properties.getProperty(configKey), openingFrame);
			valueComponent.setToolTipText(config.getHint());
			if (config.getType().isPreferredAsMultiline()) {
				verticalGroup.addGap(2, 2, 2).addComponent(label).addComponent(valueComponent);
				horizontalCombinedGroup.addComponent(label, Alignment.LEADING, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE).addComponent(valueComponent, Alignment.CENTER);
			} else {
				verticalGroup.addGroup(
						layout.createBaselineGroup(false, true).addComponent(label)
								.addComponent(valueComponent, BASELINE_GROUP_PREF_SIZE,
										BASELINE_GROUP_PREF_SIZE, BASELINE_GROUP_PREF_SIZE)
				).addGap(2, 2, 2);
				horizontalNameGroup.addComponent(label, Alignment.TRAILING, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE);
				horizontalValueGroup.addComponent(valueComponent, Alignment.LEADING, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE);
			}
		}
		openingFrame.pack();
		return openingFrame;
	}

	/*package*/final ClientProperties properties;


	private ConfigFrame(ClientConfiguration configuration) {
		super("設定");
		properties = configuration.getConfigProperties();
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		addWindowListener(new FrameWindowListener());
	}

	/**
	 * 実行時設定に反映させる。
	 *
	 * @param key   キー
	 * @param value 値
	 * @return 以前キーに関連付けられていた値
	 */
	public Object setValue(String key, String value) {
		return properties.setProperty(key, value);
	}
}

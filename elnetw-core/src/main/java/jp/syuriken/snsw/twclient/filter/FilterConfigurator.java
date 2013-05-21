package jp.syuriken.snsw.twclient.filter;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;

import jp.syuriken.snsw.twclient.ClientConfiguration;
import jp.syuriken.snsw.twclient.ClientTab;
import jp.syuriken.snsw.twclient.config.ConfigFrame;
import jp.syuriken.snsw.twclient.config.ConfigType;

/**
 * フィルタをごにょごにょするための {@link ConfigType}
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class FilterConfigurator implements ConfigType, ActionListener {

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
		 * @param propertyKey プロパティキー
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


	/**
	 * インスタンスを生成する。
	 *
	 */
	public FilterConfigurator(ClientConfiguration configuration) {
		this.configuration = configuration;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		JComboBox<KVItem> componentFilterChooser = getComponentFilterChooser();
		KVItem kvItem = (KVItem) componentFilterChooser.getSelectedItem();
		FilterEditFrame editFrame = new FilterEditFrame(configuration, kvItem.displayString, kvItem.propertyKey);
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
			editButton = new JButton("編集");
			editButton.addActionListener(this);
		}
		return editButton;
	}

	private JComboBox<KVItem> getComponentFilterChooser() {
		if (filterChooser == null) {
			filterChooser = new JComboBox<KVItem>();
			filterChooser.addItem(new KVItem("(グローバル)", "core.filter._global"));

			int count = configuration.getFrameTabCount();
			for (int i = 0; i < count; i++) {
				ClientTab tab = configuration.getFrameTab(i);
				filterChooser.addItem(new KVItem(tab.getTitle() + " (" + tab.getUniqId() + ")", "core.filter._tabs."
						+ tab.getUniqId()));
			}
		}
		return filterChooser;
	}

	@Override
	public String getValue(JComponent component) {
		return null;
	}

	@Override
	public boolean isPreferedAsMultiline() {
		return true;
	}

	@Override
	public boolean isValid(JComponent component) {
		return true;
	}

}

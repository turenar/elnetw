package jp.syuriken.snsw.twclient.config;

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

import jp.syuriken.snsw.twclient.ClientConfiguration;
import jp.syuriken.snsw.twclient.ClientProperties;
import jp.syuriken.snsw.twclient.config.ConfigFrameBuilder.Config;

/**
 * 設定フレーム
 * 
 * @author $Author$
 */
@SuppressWarnings("serial")
public class ConfigFrame extends JFrame {
	
	private class FrameWindowListener extends WindowAdapter {
		
		@Override
		public void windowClosing(WindowEvent e) {
			synchronized (staticHolder) {
				openingFrame = null;
				properties.store("Auto generated by jp.syuriken.snsw.twclient.config.ConfigFrame");
			}
		}
	}
	
	
	private static ConfigFrame openingFrame;
	
	private static Object staticHolder = new Object();
	
	
	/**
	 * 指定された設定の配列からフレームを生成する。すでにフレームが存在したときはそのフレームを返す。
	 * 
	 * @param configs 設定の配列
	 * @param configuration 実行時設定
	 * @return フレーム
	 */
	/*package*/static JFrame build(Config[] configs, ClientConfiguration configuration) {
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
		JPanel subgroupPanel = null;
		GroupLayout layout = null;
		SequentialGroup verticalGroup = null; // Parallel[K V]
		ParallelGroup horizontalNameGroup = null;
		ParallelGroup horizontalValueGroup = null;
		ParallelGroup horizontalCombinedGroup = null;
		for (Config config : configs) {
			if (group.equals(config.getGroup()) == false) {
				group = config.getGroup();
				tabContent = new JPanel();
				tabContent.setLayout(new BoxLayout(tabContent, BoxLayout.Y_AXIS));
				subgroup = "UNDEF";
				tabbedPane.add(group, new JScrollPane(tabContent));
			}
			if (subgroup == null ? config.getSubgroup() != null : subgroup.equals(config.getSubgroup()) == false) {
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
					.setHorizontalGroup(layout
						.createParallelGroup()
						.addGroup(
								layout.createSequentialGroup().addGroup(horizontalNameGroup)
									.addGroup(horizontalValueGroup)).addGroup(horizontalCombinedGroup));
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
			if (config.getType().isPreferedAsMultiline()) {
				verticalGroup.addGap(2, 2, 2).addComponent(label).addComponent(valueComponent);
				horizontalCombinedGroup.addComponent(label, Alignment.LEADING, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE).addComponent(valueComponent, Alignment.CENTER);
			} else {
				verticalGroup.addGroup(
						layout.createBaselineGroup(false, true).addComponent(label)
							.addComponent(valueComponent, 24, 24, 24)).addGap(2, 2, 2);
				horizontalNameGroup.addComponent(label, Alignment.LEADING, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE);
				horizontalValueGroup.addComponent(valueComponent, Alignment.TRAILING, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE);
			}
		}
		openingFrame.pack();
		return openingFrame;
	}
	
	
	private final ClientProperties properties;
	
	
	private ConfigFrame(ClientConfiguration configuration) {
		super("設定");
		properties = configuration.getConfigProperties();
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		addWindowListener(new FrameWindowListener());
	}
	
	/**
	 * 実行時設定に反映させる。
	 * 
	 * @param key キー
	 * @param value 値
	 * @return 以前キーに関連付けられていた値
	 */
	public Object setValue(String key, String value) {
		return properties.setProperty(key, value);
	}
}
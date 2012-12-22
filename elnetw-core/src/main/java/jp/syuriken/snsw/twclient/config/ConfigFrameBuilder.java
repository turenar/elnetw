package jp.syuriken.snsw.twclient.config;

import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JFrame;

import jp.syuriken.snsw.twclient.ClientConfiguration;

/**
 * 設定フレームのビルダー
 *
 * @author Turenar <snswinhaiku dot lo at gmail dot com>
 */
public class ConfigFrameBuilder {

	/**
	 * 設定を格納するクラス。
	 *
	 * <p>グループは、フレームのタブ名です</p>
	 * <p>サブグループは、まとめられます</p>
	 * @author Turenar <snswinhaiku dot lo at gmail dot com>
	 */
	public static class Config implements Comparable<Config> {

		private final String group;

		private final String subgroup;

		private final String configKey;

		private final String description;

		private final String hint;

		private final ConfigType type;

		private final int priority;


		/**
		 * インスタンスを生成する。
		 *
		 * @param group グループ名
		 * @param subgroup サブグループ名
		 * @param configKey 設定名 (プロパティーキー)。表示しません
		 * @param description 設定名 (説明)
		 * @param type 設定のタイプ
		 */
		public Config(String group, String subgroup, String configKey, String description, ConfigType type) {
			this(group, subgroup, configKey, description, null, type);
		}

		/**
		 * インスタンスを生成する。
		 *
		 * @param group グループ名
		 * @param subgroup サブグループ名
		 * @param configKey 設定名 (プロパティーキー)。表示しません
		 * @param description 設定名 (説明)
		 * @param hint ヒント
		 * @param type 設定のタイプ
		 */
		public Config(String group, String subgroup, String configKey, String description, String hint, ConfigType type) {
			this(group, subgroup, configKey, description, hint, type, 0);
		}

		/**
		 * インスタンスを生成する。
		 *
		 * @param group グループ名
		 * @param subgroup サブグループ名
		 * @param configKey 設定名 (プロパティーキー)。表示しません
		 * @param description 設定名 (説明)
		 * @param hint ヒント
		 * @param type 設定のタイプ
		 * @param priority 順序付け優先度
		 */
		public Config(String group, String subgroup, String configKey, String description, String hint,
				ConfigType type, int priority) {
			if (group == null || description == null || type == null) {
				throw new IllegalArgumentException("group, description, type must not be null");
			}
			this.group = group;
			this.subgroup = subgroup;
			this.configKey = configKey;
			this.description = description;
			this.hint = hint;
			this.type = type;
			this.priority = priority;
		}

		@Override
		public int compareTo(Config o) {
			int result = group.compareTo(o.group);
			if (result == 0) {
				if (subgroup == null && o.subgroup == null) { // nullの場合は前に持ってくる
					result = 0;
				} else if (subgroup == null || o.subgroup == null) {
					result = subgroup == null ? -1 : 1;
				} else {
					result = subgroup.compareTo(o.subgroup);
				}
			}
			if (result == 0) {
				result = o.getPriority() - getPriority();
			}
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof Config == false) {
				return false;
			}
			return compareTo((Config) obj) == 0;
		}

		/**
		 * 設定キーを取得する
		 *
		 * @return configKey
		 */
		public String getConfigKey() {
			return configKey;
		}

		/**
		 * 説明を取得する
		 *
		 * @return description
		 */
		public String getDescription() {
			return description;
		}

		/**
		 * グループ名を取得する
		 *
		 * @return group
		 */
		public String getGroup() {
			return group;
		}

		/**
		 * ヒントを取得する
		 *
		 * @return hint
		 */
		public String getHint() {
			return hint;
		}

		/**
		 * 順序付け優先度を取得する
		 *
		 * @return priority
		 */
		public int getPriority() {
			return priority;
		}

		/**
		 * サブグループ名を取得する
		 *
		 * @return subgroup
		 */
		public String getSubgroup() {
			return subgroup;
		}

		/**
		 * 設定のタイプを取得する
		 *
		 * @return type
		 */
		public ConfigType getType() {
			return type;
		}

		@Override
		public int hashCode() {
			int h = group.hashCode();
			h = h * 31 + subgroup.hashCode();
			h = h * 31 + priority;
			return h;
		}
	}

	/**
	 * グループの糖衣構文
	 *
	 * @author Turenar <snswinhaiku dot lo at gmail dot com>
	 */
	public final class ConfigGroup {

		private final String group;


		/*package*/ConfigGroup(String group) {
			this.group = group;
		}

		/**
		 * 設定を追加する。subgroupはnullを指定します。{@link ConfigFrameBuilder#addConfig(Config)}の糖衣構文です
		 *
		 * @param configKey 設定キー
		 * @param description 説明
		 * @param hint ヒント
		 * @param type タイプ
		 * @return このインスタンス
		 */
		public ConfigGroup addConfig(String configKey, String description, String hint, ConfigType type) {
			ConfigFrameBuilder.this.addConfig(new Config(group, null, configKey, description, hint, type));
			return this;
		}

		/**
		 * 設定を追加する。subgroupはnullを指定します。{@link ConfigFrameBuilder#addConfig(Config)}の糖衣構文です
		 *
		 * @param configKey 設定キー
		 * @param description 説明
		 * @param hint ヒント
		 * @param type タイプ
		 * @param priority 順序付け優先度
		 * @return このインスタンス
		 */
		public ConfigGroup addConfig(String configKey, String description, String hint, ConfigType type, int priority) {
			ConfigFrameBuilder.this.addConfig(new Config(group, null, configKey, description, hint, type, priority));
			return this;
		}

		/**
		 * 設定フレームビルダーのインスタンスを取得する。
		 *
		 * @return 設定フレームビルダーのインスタンス
		 */
		public ConfigFrameBuilder getBuilder() {
			return ConfigFrameBuilder.this;
		}

		/**
		 * グループ名を取得する。
		 *
		 * @return グループ名
		 */
		public String getGroupName() {
			return group;
		}

		/**
		 * このグループと指定されたサブグループ名の糖衣構文を取得する。
		 *
		 * @param subgroup サブグループ名
		 * @return サブグループの糖衣構文
		 */
		public ConfigSubgroup getSubgroup(String subgroup) {
			return new ConfigSubgroup(this, subgroup);
		}
	}

	/**
	 * サブグループの糖衣構文
	 *
	 * @author Turenar <snswinhaiku dot lo at gmail dot com>
	 */
	public final class ConfigSubgroup {

		private final String subgroup;

		private final ConfigGroup group;


		/*package*/ConfigSubgroup(ConfigGroup group, String subgroup) {
			this.group = group;
			this.subgroup = subgroup;
		}

		/**
		 * 設定を追加する。 {@link ConfigFrameBuilder#addConfig(Config)}の糖衣構文です
		 *
		 * @param configKey 設定キー
		 * @param description 説明
		 * @param hint ヒント
		 * @param type タイプ
		 * @return このインスタンス
		 */

		public ConfigSubgroup addConfig(String configKey, String description, String hint, ConfigType type) {
			ConfigFrameBuilder.this.addConfig(new Config(group.getGroupName(), subgroup, configKey, description, hint,
					type));
			return this;
		}

		/**
		 * 設定を追加する。 {@link ConfigFrameBuilder#addConfig(Config)}の糖衣構文です
		 *
		 * @param configKey 設定キー
		 * @param description 説明
		 * @param hint ヒント
		 * @param type タイプ
		 * @param priority 順序付け優先度
		 * @return このインスタンス
		 */

		public ConfigSubgroup addConfig(String configKey, String description, String hint, ConfigType type, int priority) {
			ConfigFrameBuilder.this.addConfig(new Config(group.getGroupName(), subgroup, configKey, description, hint,
					type, priority));
			return this;
		}

		/**
		 * グループ名を取得する
		 *
		 * @return グループ名
		 */
		public String getGroupName() {
			return group.getGroupName();
		}

		/**
		 * 親のグループを設定する糖衣構文を取得する。
		 *
		 * @return グループ
		 */
		public ConfigGroup getParentGroup() {
			return group;
		}

		/**
		 * サブグループ名を取得する
		 *
		 * @return サブグループ名
		 */
		public String getSubgroupName() {
			return subgroup;
		}
	}


	private ArrayList<Config> configsList = new ArrayList<Config>();

	private final ClientConfiguration configuration;


	/**
	 * インスタンスを生成する。
	 *
	 * @param configuration 設定
	 */
	public ConfigFrameBuilder(ClientConfiguration configuration) {
		this.configuration = configuration;
	}

	/**
	 * 設定を追加する。
	 *
	 * @param config 設定
	 * @return このインスタンス
	 */
	public ConfigFrameBuilder addConfig(Config config) {
		synchronized (configsList) {
			configsList.add(config);
		}
		return this;
	}

	/**
	 * グループを取得する。返り値のすべてのメソッドが糖衣構文になります。
	 *
	 * @param group グループ名。
	 * @return 糖衣された設定ビルダー
	 */
	public ConfigGroup getGroup(String group) {
		return new ConfigGroup(group);
	}

	/*package*/void show() {
		Config[] configs;
		synchronized (configsList) {
			configs = configsList.toArray(new Config[configsList.size()]);
		}
		Arrays.sort(configs);
		JFrame frame = ConfigFrame.build(configs, configuration);
		frame.setVisible(true);
	}
}

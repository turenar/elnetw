package jp.syuriken.snsw.twclient.config;

import javax.swing.JComponent;

/**
 * コンポーネントとかを生成するクラスインターフェース。設定用。
 * 
 * @author Turenar <snswinhaiku dot lo at gmail dot com>
 */
public interface ConfigType {
	
	/**
	 * コンポーネントを取得する。値が変更されたかどうかは実装元が判断してlistenerに投げること
	 * 
	 * @param configKey 設定キー
	 * @param nowValue 現在の値
	 * @param listener リスナ
	 * @return コンポーネント
	 */
	JComponent getComponent(String configKey, String nowValue, ConfigFrame listener);
	
	/**
	 * 指定されたコンポーネントの値を取得する。
	 * 
	 * @param component コンポーネント
	 * @return 値
	 */
	String getValue(JComponent component);
	
	/**
	 * 複数行 (説明とコンポーネントを別の行) にしたほうがいいかを返す。
	 * 
	 * @return 複数行にしてもらいときはtrue
	 */
	boolean isPreferedAsMultiline();
	
	/**
	 * 正しいあたいかどうかを判定する
	 * 
	 * @param component コンポーネント
	 * @return 正しいかどうか
	 */
	boolean isValid(JComponent component);
}

package jp.syuriken.snsw.twclient;

import java.awt.Component;

/**
 * タブのデータを管理するクラス
 * 
 * @author $Author: snsoftware $
 * @see DefaultClientTab
 */
public interface ClientTab {
	
	/**
	 * データを処理するレンダラを取得する
	 * 
	 * @return レンダラ
	 */
	TabRenderer getRenderer();
	
	/**
	 * タブで表示するコンポーネントを取得する
	 * 
	 * @return 表示するコンポーネント。JScrollPaneでラップしておくといいかも。
	 */
	Component getTabComponent();
	
	/**
	 * タブタイトルを取得する。"Timeline"とか
	 * 
	 * @return タイトル
	 */
	String getTitle();
}

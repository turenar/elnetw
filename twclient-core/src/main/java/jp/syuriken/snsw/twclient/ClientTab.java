package jp.syuriken.snsw.twclient;

import java.awt.Component;

import javax.swing.Icon;

/**
 * タブのデータを管理するクラス
 * 
 * @author $Author: snsoftware $
 * @see DefaultClientTab
 */
public interface ClientTab {
	
	/**
	 * タブが選択された
	 */
	void focusGained();
	
	/**
	 * タブの選択が解除された
	 */
	void focusLost();
	
	/**
	 * タブに表示するアイコン
	 * 
	 * @return アイコン
	 */
	Icon getIcon();
	
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
	
	/**
	 * タブの上にマウスを置いた時のツールチップを設定する。
	 * 
	 * @return ツールチップ
	 */
	String getToolTip();
	
}

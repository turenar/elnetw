package jp.syuriken.snsw.twclient;

import javax.swing.JMenuItem;

/**
 * アクションハンドラ。
 * 
 * @author $Author$
 */
public interface ActionHandler {
	
	/**
	 * 動作させる
	 * 
	 * @param actionName アクション名
	 * @param statusData ステータス情報。nullの可能性があります。
	 * @param frameInstance API
	 */
	void handleAction(String actionName, StatusData statusData, TwitterClientFrame frameInstance);
	
	/**
	 * メニューが表示される前に呼ばれる関数。
	 * 
	 * @param menuItem メニューアイテム 
	 * @param statusData ステータス情報
	 */
	void popupMenuWillBecomeVisible(JMenuItem menuItem, StatusData statusData);
}

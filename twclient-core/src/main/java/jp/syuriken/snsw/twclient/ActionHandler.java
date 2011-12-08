package jp.syuriken.snsw.twclient;

import javax.swing.JMenuItem;

/**
 * アクションハンドラ。
 * 
 * @author $Author$
 */
public interface ActionHandler {
	
	/**
	 * JMenuItemを作成する。これはキャッシュしないで下さい。予想外のエラーが発生する可能性があります。
	 * また、ActionCommandは設定する必要はありません。呼び出し元でoverrideされます。
	 * 
	 * @return JMenuItem
	 */
	JMenuItem createJMenuItem();
	
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

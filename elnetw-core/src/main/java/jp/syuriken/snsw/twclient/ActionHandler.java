package jp.syuriken.snsw.twclient;

import javax.swing.JMenuItem;

/**
 * アクションハンドラ。
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public interface ActionHandler {

	/**
	 * JMenuItemを作成する。これはキャッシュしないで下さい。予想外のエラーが発生する可能性があります。
	 * また、ActionCommandは設定する必要はありません。呼び出し元でoverrideされます。
	 * @param commandName TODO
	 *
	 * @return JMenuItem
	 */
	JMenuItem createJMenuItem(String commandName);

	/**
	 * 動作させる
	 *
	 * @param actionName アクション名
	 * @param statusData ステータス情報。nullの可能性があります。
	 * @param api API
	 */
	void handleAction(String actionName, StatusData statusData, ClientFrameApi api);

	/**
	 * メニューが表示される前に呼ばれる関数。
	 *
	 * @param menuItem メニューアイテム
	 * @param statusData ステータス情報
	 * @param api API
	 */
	void popupMenuWillBecomeVisible(JMenuItem menuItem, StatusData statusData, ClientFrameApi api);
}

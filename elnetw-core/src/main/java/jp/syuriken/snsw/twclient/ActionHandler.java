package jp.syuriken.snsw.twclient;

import javax.swing.JMenuItem;

import jp.syuriken.snsw.twclient.handler.IntentArguments;

/**
 * アクションハンドラ。
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public interface ActionHandler {
	/**
	 * 現在選択しているポストのデータ。StatusData
	 */
	/*public static final*/ String INTENT_ARG_NAME_SELECTING_POST_DATA = "selectingPost";

	/**
	 * JMenuItemを作成する。これはキャッシュしないで下さい。予想外のエラーが発生する可能性があります。
	 * また、ActionCommandは設定する必要はありません。呼び出し元でoverrideされます。
	 * @param args 引数
	 *
	 * @return JMenuItem
	 */
	JMenuItem createJMenuItem(IntentArguments args);

	/**
	 * 動作させる
	 *
	 * @param args 引数
	 */
	void handleAction(IntentArguments args);

	/**
	 * メニューが表示される前に呼ばれる関数。
	 *
	 * @param menuItem メニューアイテム
	 * @param args 引数
	 */
	void popupMenuWillBecomeVisible(JMenuItem menuItem, IntentArguments args);
}

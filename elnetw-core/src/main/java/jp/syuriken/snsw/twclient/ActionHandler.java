/*
 * The MIT License (MIT)
 * Copyright (c) 2011-2014 Turenai Project
 *
 * Permission is hereby granted, free of charge,
 *  to any person obtaining a copy of this software and associated documentation files (the "Software"),
 *  to deal in the Software without restriction, including without limitation the rights to
 *  use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 *  and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 *  in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 *  INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 *  IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 *  DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package jp.syuriken.snsw.twclient;

import javax.swing.JMenuItem;

import jp.syuriken.snsw.twclient.handler.IntentArguments;

/**
 * アクションハンドラ。
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public interface ActionHandler {
	/** 現在選択しているポストのデータ。StatusData */
	/*public static final*/ String INTENT_ARG_NAME_SELECTING_POST_DATA = "selectingPost";

	/**
	 * JMenuItemを作成する。これはキャッシュしないで下さい。予想外のエラーが発生する可能性があります。
	 * また、ActionCommandは設定する必要はありません。呼び出し元でoverrideされます。
	 *
	 * @param args 引数
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
	 * @param args     引数
	 */
	void popupMenuWillBecomeVisible(JMenuItem menuItem, IntentArguments args);
}

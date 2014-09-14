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

package jp.mydns.turenar.twclient.gui.tab;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JComponent;

import jp.mydns.turenar.twclient.intent.IntentArguments;

/**
 * タブのデータを管理するクラス
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 * @see AbstractClientTab
 */
public interface ClientTab {

	/**
	 * TabClosed Event
	 */
	void close();

	/** タブが選択された */
	void focusGained();

	/** タブの選択が解除された */
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
	 * タブを復元するために使うID。
	 *
	 * @return タブを復元するために使うID。
	 */
	String getTabId();

	/**
	 * タブのタイトルを取得する
	 *
	 * @return タイトル
	 */
	String getTitle();

	/**
	 * タブタイトルとして表示するコンポーネントを取得する
	 *
	 * @return タイトルコンポーネント
	 */
	JComponent getTitleComponent();

	/**
	 * タブの上にマウスを置いた時のツールチップを設定する。
	 *
	 * @return ツールチップ
	 */
	String getToolTip();

	/**
	 * 他のタブとはかぶらないユニークなIDを返す。
	 *
	 * @return ユニークID
	 */
	String getUniqId();

	/**
	 * アクションハンドラをStatusDataをつけて呼ぶメソッド。
	 * <p>
	 * {@link jp.mydns.turenar.twclient.TwitterClientFrame}からはいま選択しているポストはわからないのでこの関数ができた。
	 * ハイパーリンクのクリック時などに使用される。
	 * </p>
	 *
	 * @param command コマンド名
	 * @deprecated use {@link #handleAction(jp.mydns.turenar.twclient.intent.IntentArguments)}
	 */
	@Deprecated
	void handleAction(String command);

	/**
	 * アクションハンドラをStatusDataをつけて呼ぶメソッド。
	 * <p>
	 * {@link jp.mydns.turenar.twclient.TwitterClientFrame}からはいま選択しているポストはわからないのでこの関数ができた。
	 * ハイパーリンクのクリック時などに使用される。
	 * </p>
	 *
	 * @param args IntentArgumentsインスタンス
	 */
	void handleAction(IntentArguments args);

	/** タブとして表示できる状態となったことを通知するメソッド */
	void initTimeline();

	/**
	 * Application exiting. serialize your data.
	 */
	void serialize();
}

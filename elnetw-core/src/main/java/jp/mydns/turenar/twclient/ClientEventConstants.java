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

package jp.mydns.turenar.twclient;

/**
 * onClientEvent name constants class
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public interface ClientEventConstants {
	/** フォーカス要請: 最初のコンポーネント (arg: なし) */
	/*public static final*/ String REQUEST_FOCUS_FIRST_COMPONENT = "request focus firstComponent";
	/** フォーカス要請: 最後のコンポーネント (arg: なし) */
	/*public static final*/ String REQUEST_FOCUS_LAST_COMPONENT = "request focus lastComponent";
	/** フォーカス要請: 次のコンポーネント (arg: なし) */
	/*public static final*/ String REQUEST_FOCUS_NEXT_COMPONENT = "request focus nextComponent";
	/** フォーカス要請: 前のコンポーネント (arg: なし) */
	/*public static final*/ String REQUEST_FOCUS_PREV_COMPONENT = "request focus prevComponent";
	/** フォーカス要請: 次のユーザーの発言のコンポーネント (arg: なし) */
	/*public static final*/ String REQUEST_FOCUS_USER_NEXT_COMPONENT = "request focus userNextComponent";
	/** フォーカス要請: 前のユーザーの発言のコンポーネント (arg: なし) */
	/*public static final*/ String REQUEST_FOCUS_USER_PREV_COMPONENT = "request focus userPrevComponent";
	/** フォーカス要請: 表示範囲内の最初の発言のコンポーネント (arg: なし) */
	/*public static final*/ String REQUEST_FOCUS_WINDOW_FIRST_COMPONENT = "request focus windowFirstComponent";
	/** フォーカス要請: 表示範囲内の最後の発言のコンポーネント (arg: なし) */
	/*public static final*/ String REQUEST_FOCUS_WINDOW_LAST_COMPONENT = "request focus windowLastComponent";
	/** スクロール要請: 現在選択しているステータスを表示範囲の最後尾になるようにスクロール (arg: なし) */
	/*public static final*/ String REQUEST_SCROLL_AS_WINDOW_LAST = "scroll as window last";
	/** フォーカス要請: in_reply_toが指定されている場合、そのリプライ先の発言のコンポーネント (arg: なし) */
	/*public static final*/ String REQUEST_FOCUS_IN_REPLY_TO = "request focus inReplyTo";
	/** フォーカス要請: {@link #REQUEST_FOCUS_IN_REPLY_TO} でジャンプした際の元のコンポーネント (arg: なし) */
	/*public static final*/ String REQUEST_FOCUS_BACK_REPLIED_BY = "request focus back inReplyTo";
	/** フォーカス要請: タブコンポーネント (arg: なし) */
	/*public static final*/ String REQUEST_FOCUS_TAB_COMPONENT = "request focus tabComponent";
	/** 選択しているポストをコピー (arg: なし) */
	/*public static final*/ String REQUEST_COPY = "copy";
	/** 選択しているポストのURLをコピー (arg: なし) */
	/*public static final*/ String REQUEST_COPY_URL = "copy url";
	/** 選択しているポストのユーザーIDをコピー (arg: なし) */
	/*public static final*/ String REQUEST_COPY_USERID = "copy userid";
	/** 選択しているポストのユーザーホームをブラウザーで開く (arg: なし) */
	/*public static final*/ String REQUEST_BROWSER_USER_HOME = "browser user home";
	/** 選択しているポストをブラウザーで開く (arg: なし) */
	/*public static final*/ String REQUEST_BROWSER_STATUS = "browser status";
	/** 選択しているポストのリプライ先をブラウザーで開く (arg: なし) */
	/*public static final*/ String REQUEST_BROWSER_IN_REPLY_TO = "broser inReplyTo";
	/** 選択しているポストに含まれるURLをすべて開く (arg: なし) */
	/*public static final*/ String REQUEST_BROWSER_OPENURLS = "browser urls";
	/**
	 * 選択しているポストの一意URLを開く (arg: なし)
	 * ツイートビューから呼ばれる。
	 */
	/*public static final*/ String REQUEST_BROWSER_PERMALINK = "browser permalink";
	/**
	 * {@link jp.mydns.turenar.twclient.ClientFrameApi#setTweetViewText(String, String, int)}で設定された
	 * オーバーレイラベルがクリックされたイベント
	 * (thread: Swing Event Dispatcher Thread; arg: {@link java.awt.event.MouseEvent})
	 */
	/*public static final*/ String EVENT_CLICKED_OVERLAY_LABEL = "event overlaylabel clicked";
	/**
	 * {@link jp.mydns.turenar.twclient.ClientFrameApi#setTweetViewCreatedBy(javax.swing.Icon, String, String, int)}で設定された
	 * createdByラベルがクリックされたイベント
	 * (thread: Swing Event Dispatcher Thread; arg: {@link java.awt.event.MouseEvent})
	 */
	/*public static final*/ String EVENT_CLICKED_CREATED_BY = "event createdBy clicked";
	/**
	 * {@link jp.mydns.turenar.twclient.ClientFrameApi#setTweetViewCreatedAt(String, String, int)}で設定された
	 * createdAtラベルがクリックされたイベント
	 * (thread: Swing Event Dispatcher Thread; arg: {@link java.awt.event.MouseEvent})
	 */
	/*public static final*/ String EVENT_CLICKED_CREATED_AT = "event createdAt clicked";
	/**
	 * {@link jp.mydns.turenar.twclient.ClientFrameApi#setTweetViewCreatedBy(javax.swing.Icon, String, String, int)}で設定された
	 * ユーザーアイコンラベルがクリックされたイベント
	 * (thread: Swing Event Dispatcher Thread; arg: {@link java.awt.event.MouseEvent})
	 */
	/*public static final*/ String EVENT_CLICKED_USERICON = "event usericon clicked";
	/**
	 * arg をリストに表示させる
	 *
	 * (arg: {@link jp.mydns.turenar.twclient.gui.render.MessageRenderBase}
	 */
	/*public static final*/ String RENDER_SHOW_OBJECT = "render show object";
	/**
	 * arg をリストから削除する
	 *
	 * (arg: RenderObject uniqueId)
	 */
	/*public static final*/ String RENDER_DELETE_OBJECT = "render delete object";
	/**
	 * UserInterfaceの初期化。時間のかかるフィルタの進捗表示などに。
	 */
	/*public static final*/ String INIT_UI = "init ui";
}

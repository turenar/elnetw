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

import java.awt.Component;
import java.awt.Font;

import javax.swing.Icon;
import javax.swing.JPanel;

import jp.mydns.turenar.twclient.gui.tab.ClientTab;
import jp.mydns.turenar.twclient.intent.IntentArguments;
import jp.mydns.turenar.twclient.internal.TweetLengthUpdater;
import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.User;

/**
 * メインフレームを操作するためのAPI
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public interface ClientFrameApi extends TweetLengthUpdater {
	/** ポインタ時に何もしない */
	/*public static final*/ int DO_NOTHING_WHEN_POINTED = 0;
	/** ポインタ時にはforegroundをblueに設定する */
	/*public static final*/ int SET_FOREGROUND_COLOR_BLUE = 1;
	/** ポインタ時にはunderlineを引く */
	/*public static final*/ int UNDERLINE = 1 << 1;
	/** ポインタ時にはcursorをHANDに設定する */
	/*public static final*/ int SET_CURSOR_HAND = 1 << 2;
	/**
	 * このビットマスクの結果が違うと、セパレーターが挿入される。
	 */
	int PRIO_BITMASK = 0xfffff000;

	/** ツイートビューをクリアする。 */
	void clearTweetView();

	/** すでに入力されている内容を用いて投稿を行う。 */
	void doPost();

	/** ポストボックスをフォーカスさせる */
	void focusPostBox();

	/**
	 * コマンドを呼び出すURLを取得する。
	 *
	 * {@link #clearTweetView()}で初期化される。
	 *
	 * @param intentArguments アクションコマンド引数
	 * @return 呼び出し用URL。このインスタンス内でのみ使用でき、外部でアクセスしても何の結果ももたらさない。
	 */
	String getCommandUrl(IntentArguments intentArguments);

	/**
	 * デフォルトで使用されるフォントを取得する。
	 *
	 * @return フォント
	 */
	Font getDefaultFont();

	/**
	 * 新規フレームを作成するときの親フレームにどうぞ。
	 *
	 * @return 親フレームになることができるコンポーネント
	 */
	Component getFrame();

	/**
	 * 一時的な情報を追加するときに、この時間たったら削除してもいーよ的な時間を取得する。
	 * 若干重要度が高いときは *2 とかしてみよう！
	 *
	 * @return 一時的な情報が生き残る時間
	 */
	int getInfoSurviveTime();

	/**
	 * ログインしているユーザーを取得する。取得出来なかった場合nullの可能性あり。また、ブロックする可能性あり。
	 *
	 * @return the loginUser
	 */
	User getLoginUser();

	/**
	 * 今現在ポストボックスに入力されている文字列を返す。
	 *
	 * @return ポストボックスに入力されている文字列
	 */
	String getPostText();

	/**
	 * 現在選択しているタブを取得する。
	 *
	 * @return 選択しているタブ
	 */
	ClientTab getSelectingTab();

	/**
	 * UIに用いられるフォントを取得する。
	 *
	 * @return フォント
	 */
	Font getUiFont();

	/**
	 * Utilityインスタンスを取得する。
	 *
	 * @return インスタンス
	 */
	Utility getUtility();

	/**
	 * 例外を処理する。
	 *
	 * @param ex 例外
	 */
	void handleException(TwitterException ex);

	/**
	 * inReplyToStatusを付加する。
	 *
	 * @param status ステータス
	 * @return 前設定されたinReplyToStatus
	 */
	Status setInReplyToStatus(Status status);

	/**
	 * ポストボックスの内容を変更する。
	 *
	 * @param text 新しい内容
	 * @return 変更する前の内容
	 */
	String setPostText(String text);

	/**
	 * ポストボックスの内容を変更し、指定の場所を選択する
	 *
	 * @param text           新しい内容
	 * @param selectingStart 選択範囲の開始
	 * @param selectingEnd   選択範囲の終了
	 * @return 変更する前の内容
	 */
	String setPostText(String text, int selectingStart, int selectingEnd);

	/**
	 * ツイートビューの右上のJLabelに表示する
	 *
	 * @param createdAt     作成日時等
	 * @param toolTip       作成日時のLabelのTooltip
	 * @param pointedAction ポインタ時の動作。
	 *                      {@link #SET_FOREGROUND_COLOR_BLUE}あるいは {@link #UNDERLINE}
	 *                      ないしこれらのビット和で表してください。
	 */
	void setTweetViewCreatedAt(String createdAt, String toolTip, int pointedAction);

	/**
	 * ツイートビューの左上のJLabelに表示する
	 *
	 * @param createdBy     作成者
	 * @param toolTip       作成者のLabelのTooltip
	 * @param icon          アイコン
	 * @param pointedAction ポインタ時の動作。
	 *                      {@link #SET_FOREGROUND_COLOR_BLUE}あるいは {@link #UNDERLINE}
	 *                      ないしこれらのビット和で表してください。
	 */
	void setTweetViewCreatedBy(Icon icon, String createdBy, String toolTip, int pointedAction);

	/**
	 * ツイートビューの右に操作用パネルを表示する
	 *
	 * @param operationPanel 操作用パネル
	 */
	void setTweetViewOperationPanel(JPanel operationPanel);

	/**
	 * ツイートビューに文字列を表示する
	 *
	 * @param tweetData     ツイートビューのテキスト
	 * @param overlayString 右下右揃えで表示するオーバーレイ文字列
	 * @param pointedAction ポインタ時の動作。
	 *                      {@link #SET_FOREGROUND_COLOR_BLUE}あるいは {@link #UNDERLINE}
	 *                      ないしこれらのビット和で表してください。
	 */
	void setTweetViewText(String tweetData, String overlayString, int pointedAction);
}

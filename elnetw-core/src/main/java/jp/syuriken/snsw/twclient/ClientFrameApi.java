package jp.syuriken.snsw.twclient;

import java.awt.Font;
import java.awt.event.KeyEvent;
import java.util.Timer;

import javax.swing.Icon;
import javax.swing.JPanel;

import jp.syuriken.snsw.twclient.JobQueue.Priority;
import jp.syuriken.snsw.twclient.internal.TweetLengthUpdater;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;

/**
 * メインフレームを操作するためのAPI
 *
 * @author Turenar <snswinhaiku dot lo at gmail dot com>
 */
public interface ClientFrameApi extends TweetLengthUpdater {

	/** ポインタ時にはforegroundをblueに設定する */
	public static final int SET_FOREGROUND_COLOR_BLUE = 1;

	/** ポインタ時にはunderlineを引く */
	public static final int UNDERLINE = (1 << 1);

	/** ポインタ時に何もしない */
	public static final int DO_NOTHING_WHEN_POINTED = 0;

	/**
	 * アクションハンドラを追加する
	 *
	 * @param name ハンドラ名
	 * @param handler ハンドラ
	 * @return 同名のハンドラが以前関連付けられていたらそのインスタンス、そうでない場合null
	 */
	ActionHandler addActionHandler(String name, ActionHandler handler);

	/**
	 * ジョブを追加する
	 *
	 * @param priority 優先度
	 * @param job ジョブ
	 * @Deprecated use {@link ClientConfiguration#addJob(Priority, Runnable)}
	 */
	@Deprecated
	void addJob(Priority priority, Runnable job);

	/**
	 * ジョブを追加する
	 *
	 * @param job ジョブ
	 * @Deprecated use {@link ClientConfiguration#addJob(Runnable)}
	 */
	@Deprecated
	void addJob(Runnable job);

	/**
	 * ショートカットキーとアクションコマンドを関連付ける
	 *
	 * @param keyCode キー文字列。
	 * @param actionName アクションコマンド名
	 * @see Utility#toKeyString(int, int, boolean)
	 */
	void addShortcutKey(String keyCode, String actionName);

	/**
	 * ツイートビューをクリアする。
	 */
	void clearTweetView();

	/**
	 * すでに入力されている内容を用いて投稿を行う。
	 */
	void doPost();

	/**
	 * ポストボックスをフォーカスさせる
	 */
	void focusPostBox();

	/**
	 * ショートカットキーを用いてアクションコマンドを取得する。
	 *
	 * 指定したコンポーネント名に関連付けられたアクションコマンドがない場合、
	 * allに関連付けられたアクションコマンドの検索を試します。
	 *
	 * @param component コンポーネント名。
	 * @param keyString キー文字列。
	 * @return アクションコマンド名
	 */
	String getActionCommandByShortcutKey(String component, String keyString);

	/**
	 * 指定されたアクションコマンド名で呼び出されるアクションハンドラを取得する。
	 *
	 * @param actionCommand アクションコマンド名
	 * @return アクションハンドラ
	 */
	ActionHandler getActionHandler(String actionCommand);

	/**
	 * ClientConfigurationインスタンスを取得する。
	 *
	 * @return インスタンス
	 */
	ClientConfiguration getClientConfiguration();

	/**
	 * デフォルトで使用されるフォントを取得する。
	 *
	 * @return フォント
	 */
	Font getDefaultFont();

	/**
	 * 画像をキャッシュするオブジェクトを取得する。
	 *
	 * @return ImageCacherインスタンス
	 */
	ImageCacher getImageCacher();

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
	 * タイマーを取得する。
	 *
	 * @return タイマー
	 */
	Timer getTimer();

	/**
	 * Twitterインスタンスを取得する
	 *
	 * @return Twitterインスタンス
	 * @deprecated use {@link ClientConfiguration#getTwitterForRead()}
	 */
	@Deprecated
	Twitter getTwitter();

	/**
	 * 読み込み用のTwitterインスタンスを取得する。
	 *
	 * @return 読み込み用Twitterインスタンス
	 * @see ClientConfiguration#getTwitterForRead()
	 */
	Twitter getTwitterForRead();

	/**
	 * 書き込み用のTwitterインスタンスを取得する。
	 *
	 * @return 書き込み用のTwitterインスタンス
	 * @see ClientConfiguration#getTwitterForWrite()
	 */
	Twitter getTwitterForWrite();

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
	 * アクションコマンド名を使用してアクションハンドラを呼び出す。
	 *
	 * @param name アクションコマンド名
	 * @param statusData データ
	 */
	void handleAction(String name, StatusData statusData);

	/**
	 * 例外を処理する。
	 *
	 * @param ex 例外
	 */
	void handleException(Exception ex);

	/**
	 * 例外を処理する。
	 * @param ex 例外
	 */
	void handleException(TwitterException ex);

	/**
	 * ショートカットキーを処理する。
	 *
	 * これは {@link #getActionCommandByShortcutKey(String, String)}のラッパです。
	 *
	 * @param component コンポーネント
	 * @param e イベント
	 */
	void handleShortcutKey(String component, KeyEvent e);

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
	 * @param text 新しい内容
	 * @param selectingStart 選択範囲の開始
	 * @param selectingEnd 選択範囲の終了
	 * @return 変更する前の内容
	 */
	String setPostText(String text, int selectingStart, int selectingEnd);

	/**
	 * ツイートビューの右上のJLabelに表示する
	 *
	 * @param createdAt 作成日時等
	 * @param toolTip 作成日時のLabelのTooltip
	 * @param pointedAction ポインタ時の動作。
	 *   {@link #SET_FOREGROUND_COLOR_BLUE}あるいは {@link #UNDERLINE}
	 *   ないしこれらのビット和で表してください。
	 */
	void setTweetViewCreatedAt(String createdAt, String toolTip, int pointedAction);

	/**
	 * ツイートビューの左上のJLabelに表示する
	 *
	 * @param createdBy 作成者
	 * @param toolTip 作成者のLabelのTooltip
	 * @param icon アイコン
	 * @param pointedAction ポインタ時の動作。
	 *   {@link #SET_FOREGROUND_COLOR_BLUE}あるいは {@link #UNDERLINE}
	 *   ないしこれらのビット和で表してください。
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
	 * @param tweetData ツイートビューのテキスト
	 * @param overlayString 右下右揃えで表示するオーバーレイ文字列
	 * @param pointedAction ポインタ時の動作。
	 *   {@link #SET_FOREGROUND_COLOR_BLUE}あるいは {@link #UNDERLINE}
	 *   ないしこれらのビット和で表してください。
	 */
	void setTweetViewText(String tweetData, String overlayString, int pointedAction);

	/**
	 * ツイートビューに文字列を表示する
	 *
	 * @param tweetData ツイートビューのテキスト
	 * @param createdBy 作成者
	 * @param createdByToolTip 作成者のLabelのTooltip
	 * @param createdAt 作成日時等
	 * @param createdAtToolTip 作成日時のLabelのTooltip
	 * @param icon アイコン
	 * @param operationPanel 操作用パネル
	 * @deprecated use
	 *   {@link #clearTweetView()}
	 *   {@link #setTweetViewCreatedAt(String, String, int)}
	 *   {@link #setTweetViewCreatedBy(Icon, String, String, int)}
	 *   {@link #setTweetViewOperationPanel(JPanel)}
	 *   {@link #setTweetViewText(String, String, int)}
	 */
	@Deprecated
	void setTweetViewText(String tweetData, String createdBy, String createdByToolTip, String createdAt,
			String createdAtToolTip, Icon icon, JPanel operationPanel);

}

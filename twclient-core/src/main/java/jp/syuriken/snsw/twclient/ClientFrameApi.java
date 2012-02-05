package jp.syuriken.snsw.twclient;

import java.awt.Font;
import java.util.Timer;

import javax.swing.Icon;

import jp.syuriken.snsw.twclient.JobQueue.Priority;
import jp.syuriken.snsw.twclient.TwitterClientFrame.ConfigData;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;

/**
 * メインフレームを操作するためのAPI
 * 
 * @author $Author$
 */
public interface ClientFrameApi {
	
	/**
	 * アクションハンドラを追加する
	 * @param name ハンドラ名。"!"を含んではいけません。
	 * @param handler ハンドラ
	 * @return 以前登録されていたハンドラ。
	 */
	ActionHandler addActionHandler(String name, ActionHandler handler);
	
	/**
	 * ジョブを追加する。ParallelRunnableを継承したジョブの場合は並列的に起動する場合があります。
	 * 
	 * @param priority 優先度
	 * @param job ジョブ
	 */
	void addJob(Priority priority, Runnable job);
	
	/**
	 * ジョブを追加する。ParallelRunnableを継承したジョブの場合は並列的に起動する場合があります。
	 * 
	 * @param job ジョブ
	 */
	void addJob(Runnable job);
	
	/**
	 * ショートカットキーとアクションコマンドを関連付ける
	 * 
	 * @param keyCode キー文字列。
	 * @param actionName アクションコマンド名
	 * @see Utility#toKeyString(int, int)
	 */
	void addShortcutKey(String keyCode, String actionName);
	
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
	 * @param keyString キー文字列。
	 * @return アクションコマンド名
	 */
	String getActionCommandByShortcutKey(String keyString);
	
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
	 * 設定情報を格納したクラスを取得する。該当のプロパティを変更すると自動的に更新されます。
	 * 
	 * @return 設定情報を格納したクラス。
	 */
	ConfigData getConfigData();
	
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
	 */
	@Deprecated
	Twitter getTwitter();
	
	/**
	 * 読み込み用のTwitterインスタンスを取得する。
	 * 
	 * @return 読み込み用Twitterインスタンス
	 */
	Twitter getTwitterForRead();
	
	/**
	 * 書き込み用のTwitterインスタンスを取得する。
	 * 
	 * @return 書き込み用のTwitterインスタンス
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
	 * ツイートビューに文字列を表示する
	 * 
	 * @param tweetData ツイートビューのテキスト
	 * @param createdBy 作成者
	 * @param createdByToolTip 作成者のLabelのTooltip
	 * @param createdAt 作成日時等
	 * @param createdAtToolTip 作成日時のLabelのTooltip
	 * @param icon アイコン 
	 */
	void setTweetViewText(String tweetData, String createdBy, String createdByToolTip, String createdAt,
			String createdAtToolTip, Icon icon);
}

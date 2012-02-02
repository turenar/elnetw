package jp.syuriken.snsw.twclient;

import java.util.Timer;

import javax.swing.JPanel;

import jp.syuriken.snsw.twclient.JobQueue.Priority;
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
	
	public abstract ActionHandler addActionHandler(String name, ActionHandler handler);
	
	/**
	 * ジョブを追加する。ParallelRunnableを継承したジョブの場合は並列的に起動する場合があります。
	 * 
	 * @param priority 優先度
	 * @param job ジョブ
	 */
	public abstract void addJob(Priority priority, Runnable job);
	
	/**
	 * ジョブを追加する。ParallelRunnableを継承したジョブの場合は並列的に起動する場合があります。
	 * 
	 * @param job ジョブ
	 */
	public abstract void addJob(Runnable job);
	
	public abstract void addShortcutKey(String keyCode, String actionName);
	
	/**
	 * リストにステータスを追加する。
	 * 
	 * @param originalStatus 元となるStatus
	 */
	public abstract void addStatus(Status originalStatus);
	
	/**
	 * リストにステータスを追加する。
	 * 
	 * @param statusData StatusDataインスタンス。
	 * @return 追加された StatusPanel
	 */
	public abstract StatusPanel addStatus(StatusData statusData);
	
	/**
	 * リストにステータスを追加する。その後deltionDelayミリ秒後に該当するステータスを削除する。
	 * 
	 * @param statusData StatusDataインスタンス。
	 * @param deletionDelay 削除を予約する時間。ミリ秒
	 * @return 追加された (もしくはそのあと削除された) ステータス。
	 */
	public abstract JPanel addStatus(StatusData statusData, int deletionDelay);
	
	public abstract void doPost();
	
	/**
	 * ポストボックスをフォーカスさせる
	 */
	public abstract void focusPostBox();
	
	/**
	 * ClientConfigurationインスタンスを取得する。
	 * 
	 * @return インスタンス
	 */
	public abstract ClientConfiguration getClientConfiguration();
	
	/**
	 * 画像をキャッシュするオブジェクトを取得する。
	 * 
	 * @return ImageCacherインスタンス
	 */
	public abstract ImageCacher getImageCacher();
	
	/**
	 * 一時的な情報を追加するときに、この時間たったら削除してもいーよ的な時間を取得する。
	 * 若干重要度が高いときは *2 とかしてみよう！
	 * 
	 * @return 一時的な情報が生き残る時間
	 */
	public abstract int getInfoSurviveTime();
	
	/**
	 * ログインしているユーザーを取得する。取得出来なかった場合nullの可能性あり。また、ブロックする可能性あり。
	 * 
	 * @return the loginUser
	 */
	public abstract User getLoginUser();
	
	/**
	 * 今現在ポストボックスに入力されている文字列を返す。
	 * 
	 * @return ポストボックスに入力されている文字列
	 */
	public abstract String getPostText();
	
	/**
	 * 内部で保持しているステータスデータを取得する
	 * 
	 * @param statusId ステータスID (ユニーク)
	 * @return ステータスデータ。ない場合はnull
	 */
	public abstract StatusData getStatus(long statusId);
	
	/**
	 * タイマーを取得する。
	 * 
	 * @return タイマー
	 */
	public abstract Timer getTimer();
	
	/**
	 * Twitterインスタンスを取得する
	 * 
	 * @return Twitterインスタンス
	 */
	@Deprecated
	public abstract Twitter getTwitter();
	
	/**	 * TODO snsoftware
	 * 
	 * @return
	 */
	Twitter getTwitterForRead();
	
	/**
	 * TODO snsoftware
	 * 
	 * @return
	 */
	Twitter getTwitterForWrite();
	
	/**
	 * Utilityインスタンスを取得する。
	 * 
	 * @return インスタンス
	 */
	public abstract Utility getUtility();
	
	/**
	 * 例外を処理する。
	 * 
	 * @param ex 例外
	 */
	public abstract void handleException(Exception ex);
	
	/**
	 * 例外を処理する。
	 * @param ex 例外
	 */
	public abstract void handleException(TwitterException ex);
	
	/**
	 * ステータスを削除する。
	 * 
	 * @param statusData ステータスデータ
	 */
	public abstract void removeStatus(final StatusData statusData);
	
	/**
	 * ステータスを削除する。
	 * 
	 * @param statusData ステータスデータ
	 * @param delay 遅延 (ms)
	 */
	public abstract void removeStatus(final StatusData statusData, int delay);
	
	/**
	 * inReplyToStatusを付加する。
	 * 
	 * @param status ステータス
	 * @return 前設定されたinReplyToStatus
	 */
	public abstract Status setInReplyToStatus(Status status);
	
	/**
	* ポストボックスの内容を変更する。
	* 
	* @param text 新しい内容
	* @return 変更する前の内容
	*/
	public abstract String setPostText(String text);
	
	/**
	 * ポストボックスの内容を変更し、指定の場所を選択する
	 * @param text 新しい内容 
	 * @param selectingStart 選択範囲の開始 
	 * @param selectingEnd 選択範囲の終了
	 * @return 変更する前の内容
	 */
	public abstract String setPostText(String text, int selectingStart, int selectingEnd);
}

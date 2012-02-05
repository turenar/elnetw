package jp.syuriken.snsw.twclient;

import twitter4j.DirectMessage;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.User;

/**
 * フィルタクラス。
 * 
 * @author $Author: snsoftware $
 */
public interface MessageFilter {
	
	/**
	 * アカウントを変更したという情報のフィルタ
	 * 
	 * @param forWrite 書き込み用アカウントが変更されたかどうか。
	 * @return true=続行,false=フィルタ中止
	 */
	boolean onChangeAccount(boolean forWrite);
	
	/**
	 * クライアントメッセージのフィルタ
	 * 
	 * @param name 名前
	 * @param arg オブジェクト
	 * @return true=続行,false=フィルタ中止
	 */
	boolean onClientMessage(String name, Object arg);
	
	/**
	 * 削除通知のフィルタ
	 * 
	 * @param directMessageId ダイレクトメッセージID
	 * @param userId ユーザーID
	 * @return true=続行,false=フィルタ中止
	 */
	boolean onDeletionNotice(long directMessageId, long userId);
	
	/**
	 * 削除通知のフィルタ
	 * 
	 * @param statusDeletionNotice 削除情報
	 * @return null=フィルタ中止,null以外の場合は次のフィルタは返り値をフィルタしようとします
	 */
	StatusDeletionNotice onDeletionNotice(StatusDeletionNotice statusDeletionNotice);
	
	/**
	 * ダイレクトメッセージのフィルタ
	 * 
	 * @param message ダイレクトメッセージ
	 * @return null=フィルタ中止,null以外の場合は次のフィルタは返り値をフィルタしようとします
	 */
	DirectMessage onDirectMessage(DirectMessage message);
	
	/**
	 * 例外が発生した
	 * 
	 * @param obj 例外
	 * @return true=続行,false=フィルタ中止 
	 */
	boolean onException(Exception obj);
	
	/**
	 * ふぁぼられたをフィルタ
	 * 
	 * @param source ふぁぼ本
	 * @param target ふぁぼ先
	 * @param favoritedStatus ふぁぼったステータス
	 * @return true=続行,false=フィルタ中止
	 */
	boolean onFavorite(User source, User target, Status favoritedStatus);
	
	/**
	 * フォローをフィルタ
	 * 
	 * @param source フォロー元
	 * @param followedUser フォロー先
	 * @return true=続行,false=フィルタ中止
	 */
	boolean onFollow(User source, User followedUser);
	
	/**
	 * フレンドリストをフィルタ
	 * 
	 * @param arr フォローID配列
	 * @return null=フィルタ中止,null以外の場合は次のフィルタは返り値をフィルタしようとします
	 */
	long[] onFriendList(long[] arr);
	
	/**
	 * リツイートをフィルタ
	 * 
	 * @param source リツイート元
	 * @param target リツイートされた人
	 * @param retweetedStatus リツイートされたステータス
	 * @return true=続行,false=フィルタ中止
	 */
	boolean onRetweet(User source, User target, Status retweetedStatus);
	
	/**
	 * ステータスをフィルタ
	 * 
	 * @param status ステータス
	 * @return null=フィルタ中止,null以外の場合は次のフィルタは返り値をフィルタしようとします
	 */
	Status onStatus(Status status);
	
	/**
	 * StreamがCleanUpされたことをフィルタ
	 * 
	 * @return true=続行,false=フィルタ中止
	 */
	boolean onStreamCleanUp();
	
	/**
	 * Streamが接続されたことをフィルタ
	 * 
	 * @return true=続行,false=フィルタ中止
	 */
	boolean onStreamConnect();
	
	/**
	 * ストリームが切断されたことをフィルタ
	 * 
	 * @return true=続行,false=フィルタ中止
	 */
	boolean onStreamDisconnect();
	
	/**
	 * Streamがステータスをスキップしたことをフィルタ
	 * 
	 * @param numberOfLimitedStatuses スキップされたステータスの数
	 * @return true=続行,false=フィルタ中止
	 */
	boolean onTrackLimitationNotice(int numberOfLimitedStatuses);
	
	/**
	 * ふぁぼやめられたことの通知をフィルタ
	 * 
	 * @param source ふぁぼやめた人
	 * @param target ふぁぼやめられた人
	 * @param unfavoritedStatus ふぁぼやめられたステータス
	 * @return true=続行,false=フィルタ中止
	 */
	boolean onUnfavorite(User source, User target, Status unfavoritedStatus);
	
}

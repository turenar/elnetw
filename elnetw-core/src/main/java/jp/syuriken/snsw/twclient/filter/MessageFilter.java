package jp.syuriken.snsw.twclient.filter;

import jp.syuriken.snsw.twclient.ClientEventConstants;
import twitter4j.DirectMessage;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.User;
import twitter4j.UserList;

/**
 * フィルタクラス。
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public interface MessageFilter extends ClientEventConstants {
	/**
	 * blocked user
	 *
	 * @param source      source user
	 * @param blockedUser user blocked by source
	 * @return true=フィルタ中止, false=続行
	 */
	boolean onBlock(User source, User blockedUser);

	/**
	 * アカウントを変更したという情報のフィルタ
	 *
	 * @param forWrite 書き込み用アカウントが変更されたかどうか。
	 * @return true=フィルタ中止, false=続行
	 */
	boolean onChangeAccount(boolean forWrite);

	/**
	 * (for Stream) called before thread gets cleaned up
	 *
	 * @return true=フィルタ中止, false=続行
	 */
	boolean onCleanUp();

	/**
	 * クライアントメッセージのフィルタ
	 *
	 * @param name 名前
	 * @param arg  オブジェクト
	 * @return true=フィルタ中止, false=続行
	 */
	boolean onClientMessage(String name, Object arg);

	/**
	 * (for Stream) called after connection was established
	 *
	 * @return true=フィルタ中止, false=続行
	 */
	boolean onConnect();

	/**
	 * 削除通知のフィルタ
	 *
	 * @param directMessageId ダイレクトメッセージID
	 * @param userId          ユーザーID
	 * @return true=フィルタ中止, false=続行
	 */
	boolean onDeletionNotice(long directMessageId, long userId);

	/**
	 * 削除通知のフィルタ
	 *
	 * @param statusDeletionNotice 削除情報
	 * @return true=フィルタ中止, false=続行
	 */
	boolean onDeletionNotice(StatusDeletionNotice statusDeletionNotice);

	/**
	 * ダイレクトメッセージのフィルタ
	 *
	 * @param message ダイレクトメッセージ
	 * @return null=フィルタ中止,null以外の場合は次のフィルタは返り値をフィルタしようとします
	 */
	DirectMessage onDirectMessage(DirectMessage message);

	/**
	 * (for Stream) called after connection was disconnected
	 *
	 * @return true=フィルタ中止, false=続行
	 */
	boolean onDisconnect();

	/**
	 * 例外が発生した
	 *
	 * @param obj 例外
	 * @return true=フィルタ中止, false=続行
	 */
	boolean onException(Exception obj);

	/**
	 * ふぁぼられたをフィルタ
	 *
	 * @param source          ふぁぼ本
	 * @param target          ふぁぼ先
	 * @param favoritedStatus ふぁぼったステータス
	 * @return true=フィルタ中止, false=続行
	 */
	boolean onFavorite(User source, User target, Status favoritedStatus);

	/**
	 * フォローをフィルタ
	 *
	 * @param source       フォロー元
	 * @param followedUser フォロー先
	 * @return true=フィルタ中止, false=続行
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
	 * @param source          リツイート元
	 * @param target          リツイートされた人
	 * @param retweetedStatus リツイートされたステータス
	 * @return true=フィルタ中止, false=続行
	 */
	boolean onRetweet(User source, User target, Status retweetedStatus);

	/**
	 * ロケーション削除の通知をフィルタ。
	 *
	 * @param userId       ロケーション削除した人
	 * @param upToStatusId IDの上限
	 * @return true=フィルタ中止, false=続行
	 */
	boolean onScrubGeo(long userId, long upToStatusId);

	/**
	 * StallWarningの通知をフィルタ
	 *
	 * @param warning 警告詳細
	 * @return null=フィルタ中止, それ以外は次のフィルタは返り値をフィルタしようとします
	 */
	boolean onStallWarning(StallWarning warning);

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
	 * @return true=フィルタ中止, false=続行
	 */
	boolean onStreamCleanUp();

	/**
	 * Streamが接続されたことをフィルタ
	 *
	 * @return true=フィルタ中止, false=続行
	 */
	boolean onStreamConnect();

	/**
	 * ストリームが切断されたことをフィルタ
	 *
	 * @return true=フィルタ中止, false=続行
	 */
	boolean onStreamDisconnect();

	/**
	 * Streamがステータスをスキップしたことをフィルタ
	 *
	 * @param numberOfLimitedStatuses スキップされたステータスの数
	 * @return true=フィルタ中止, false=続行
	 */
	boolean onTrackLimitationNotice(int numberOfLimitedStatuses);

	/**
	 * ブロックを解除したことをフィルタ
	 *
	 * @param source        自分
	 * @param unblockedUser ブロック解除されたユーザー
	 * @return true=フィルタ中止, false=続行
	 */
	boolean onUnblock(User source, User unblockedUser);

	/**
	 * ふぁぼやめられたことの通知をフィルタ
	 *
	 * @param source            ふぁぼやめた人
	 * @param target            ふぁぼやめられた人
	 * @param unfavoritedStatus ふぁぼやめられたステータス
	 * @return true=フィルタ中止, false=続行
	 */
	boolean onUnfavorite(User source, User target, Status unfavoritedStatus);

	/**
	 * リスト作成をフィルタ
	 *
	 * @param listOwner リスト作成者
	 * @param list      リスト
	 * @return true=フィルタ中止, false=続行
	 */
	boolean onUserListCreation(User listOwner, UserList list);

	/**
	 * リスト削除をフィルタ
	 *
	 * @param listOwner リスト作成者
	 * @param list      リスト
	 * @return true=フィルタ中止, false=続行
	 */
	boolean onUserListDeletion(User listOwner, UserList list);

	/**
	 * リストにメンバーを追加したことをフィルタ
	 *
	 * @param addedMember 追加されたユーザー
	 * @param listOwner   リスト作成者
	 * @param list        リスト
	 * @return true=フィルタ中止, false=続行
	 */
	boolean onUserListMemberAddition(User addedMember, User listOwner, UserList list);

	/**
	 * リストからメンバーを削除したことをフィルタ
	 *
	 * @param deletedMember 削除されたユーザー
	 * @param listOwner     リスト作成者
	 * @param list          リスト
	 * @return true=フィルタ中止, false=続行
	 */
	boolean onUserListMemberDeletion(User deletedMember, User listOwner, UserList list);

	/**
	 * リストの購読をフィルタ
	 *
	 * @param subscriber 購読者
	 * @param listOwner  リスト作成者
	 * @param list       リスト
	 * @return true=フィルタ中止, false=続行
	 */
	boolean onUserListSubscription(User subscriber, User listOwner, UserList list);

	/**
	 * リストの購読解除をフィルタ
	 *
	 * @param subscriber 購読してた人
	 * @param listOwner  リスト作成者
	 * @param list       リスト
	 * @return true=フィルタ中止, false=続行
	 */
	boolean onUserListUnsubscription(User subscriber, User listOwner, UserList list);

	/**
	 * リストの更新をフィルタ
	 *
	 * @param listOwner リスト作成者
	 * @param list      リスト
	 * @return true=フィルタ中止, false=続行
	 */
	boolean onUserListUpdate(User listOwner, UserList list);

	/**
	 * ユーザープロフィールの更新をフィルタ
	 *
	 * @param updatedUser 更新したユーザー
	 * @return true=フィルタ中止, false=続行
	 */
	boolean onUserProfileUpdate(User updatedUser);
}

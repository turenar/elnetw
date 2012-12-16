package jp.syuriken.snsw.twclient.filter;

import twitter4j.DirectMessage;
import twitter4j.Status;

/**
 * フィルタを処理するクラスのためのインスタンス。
 *
 * @author Turenar <snswinhaiku dot lo at gmail dot com>
 */
public interface FilterDispatcherBase {

	/**
	 * ダイレクトメッセージをフィルター
	 *
	 * @param directMessage ダイレクトメッセージ
	 * @return フィルターするかどうか
	 */
	boolean filter(DirectMessage directMessage);

	/**
	 * ステータスをフィルター
	 *
	 * @param status ステータス
	 * @return フィルターするかどうか
	 */
	boolean filter(Status status);
}
